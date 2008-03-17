/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.vc;

import static java.lang.String.format;
import static net.hillsdon.fij.core.Functional.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.hillsdon.fij.core.Functional;
import net.hillsdon.fij.core.Predicate;
import net.hillsdon.fij.text.Strings;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.util.SVNTimeUtil;
import org.tmatesoft.svn.core.io.ISVNEditor;

/**
 * Stores pages in an SVN repository.
 * 
 * @author mth
 */
public class SVNPageStore implements PageStore {

  private static final Predicate<ChangeInfo> IS_CHANGE_TO_PAGE = new Predicate<ChangeInfo>() {
    public Boolean transform(final ChangeInfo in) {
      return in.getKind() == StoreKind.PAGE;
    }
  };

  private final BasicSVNOperations _operations;

  private final DeletedRevisionTracker _tracker;

  /**
   * Note the repository URL can be deep, it need not refer to the root of the
   * repository itself. We put pages in the root of what we're given.
   */
  public SVNPageStore(final DeletedRevisionTracker tracker, final BasicSVNOperations operations) {
    _tracker = tracker;
    _operations = operations;
  }

  public List<ChangeInfo> recentChanges(final int limit) throws PageStoreException {
    return _operations.log("", limit, false, true, 0, -1);
  }

  public List<ChangeInfo> history(final PageReference ref) throws PageStoreException {
    final List<ChangeInfo> changes = new ArrayList<ChangeInfo>();
    final ChangeInfo deletedIn = getChangeThatDeleted(ref);
    long lastRevision = deletedIn == null ? -1 : deletedIn.getRevision() - 1;
    
    // We follow all the previous locations.
    String path = ref.getPath();
    while (path != null && changes.addAll(_operations.log(path, -1, true, true, 0, lastRevision))) {
      if (!changes.isEmpty()) {
        ChangeInfo last = changes.get(changes.size() - 1);
        path = last.getCopiedFrom();
        lastRevision = last.getCopiedFromRevision();
      }
    }
    if (deletedIn != null) {
      changes.add(0, deletedIn);
    }
    List<ChangeInfo> result = Functional.list(filter(changes, IS_CHANGE_TO_PAGE));
    Collections.sort(result, DeletesAfterOtherSameRevisionChanges.INSTANCE);
    return result;
  }

  public Set<PageReference> list() throws PageStoreException {
    Set<PageReference> names = new LinkedHashSet<PageReference>();
    for (String page : _operations.listFiles("")) {
      names.add(new PageReference(page));
    }
    return names;
  }

  public PageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    HashMap<String, String> properties = new HashMap<String, String>();
    SVNNodeKind kind = _operations.checkPath(ref.getPath(), revision);
    if (SVNNodeKind.FILE.equals(kind)) {
      _operations.getFile(ref.getPath(), revision, properties, baos);
      long actualRevision = SVNProperty.longValue(properties.get(SVNProperty.REVISION));
      long lastChangedRevision = SVNProperty.longValue(properties.get(SVNProperty.COMMITTED_REVISION));
      Date lastChangedDate = SVNTimeUtil.parseDate(properties.get(SVNProperty.COMMITTED_DATE));
      String lastChangedAuthor = properties.get(SVNProperty.LAST_AUTHOR);
      String lockOwner = null;
      String lockToken = null;
      try {
        if (revision == -1 || _operations.checkPath(ref.getPath(), -1) == SVNNodeKind.FILE) {
          SVNLock lock = _operations.getLock(ref.getPath());
          if (lock != null) {
            lockOwner = lock.getOwner();
            lockToken = lock.getID();
          }
        }
      }
      catch (NotFoundException ex) {
        // It was a file at 'revision' but is now deleted so we can't get the lock information.
      }
      return new PageInfo(ref.getPath(), Strings.toUTF8(baos.toByteArray()), actualRevision, lastChangedRevision, lastChangedAuthor, lastChangedDate, lockOwner, lockToken);
    }
    else if (SVNNodeKind.NONE.equals(kind)) {
      long pseudoRevision = PageInfo.UNCOMMITTED;
      long lastChangedRevision = PageInfo.UNCOMMITTED; 
      String lastChangedAuthor = null;
      Date lastChangedDate = null;
      final ChangeInfo deletingChange = getChangeThatDeleted(ref);
      if (deletingChange != null) {
        pseudoRevision = PageInfo.DELETED;
        lastChangedRevision = deletingChange.getRevision();
        lastChangedAuthor = deletingChange.getUser();
        lastChangedDate = deletingChange.getDate();
      }
      return new PageInfo(ref.getPath(), "", pseudoRevision, lastChangedRevision, lastChangedAuthor, lastChangedDate, null, null);
    }
    else {
      throw new PageStoreException(format("Unexpected node kind '%s' at '%s'", kind, ref));
    }
  }

  private ChangeInfo getChangeThatDeleted(final PageReference ref) throws PageStoreAuthenticationException, PageStoreException {
    return _tracker.getChangeThatDeleted(_operations, ref.getPath());
  }
  
  public PageInfo tryToLock(final PageReference ref) throws PageStoreException {
    final PageInfo page = get(ref, -1);
    if (page.isNew()) {
      return page;
    }
    if (page.isLocked()) {
      return page;
    }
    final long revision = page.getRevision();
    try {
      _operations.lock(ref, revision);
    }
    catch (AlreadyLockedException ex) {
      // Just return, the caller will check whether they've locked the page.
    }
    return get(ref, revision);
  }

  public void unlock(final PageReference path, final String lockToken) throws PageStoreException {
    _operations.unlock(path, lockToken);
  }

  public long set(final PageReference ref, final String lockToken, final long baseRevision, final String content, final String commitMessage) throws PageStoreAuthenticationException, PageStoreException {
    if (content.trim().length() == 0) {
      return delete(ref.getPath(), lockToken, baseRevision, commitMessage);
    }
    return set(ref.getPath(), lockToken, baseRevision, new ByteArrayInputStream(Strings.fromUTF8(content)), commitMessage);
  }

  private long delete(final String path, final String lockToken, final long baseRevision, final String commitMessage) throws PageStoreAuthenticationException, PageStoreException {
    _operations.delete(path, baseRevision, commitMessage, lockToken);
    return PageInfo.DELETED;
  }

  private long set(final String path, final String lockToken, final long baseRevision, final InputStream content, final String commitMessage) throws PageStoreException {
    if (baseRevision < 0) {
      return _operations.create(path, commitMessage, content);
    }
    else {
      return _operations.edit(path, baseRevision, commitMessage, lockToken, content);
    }
  }

  public void attach(final PageReference ref, final String storeName, final long baseRevision, final InputStream in, final String commitMessage) throws PageStoreException {
    String dir = attachmentPath(ref);
    _operations.ensureDir(dir, commitMessage);
    set(dir + "/" + storeName, null, baseRevision, in, commitMessage);
  }

  public Collection<AttachmentHistory> attachments(final PageReference ref) throws PageStoreException {
    final String attachmentPath = attachmentPath(ref);
    Collection<ChangeInfo> changed = Collections.emptyList();
    if (_operations.checkPath(attachmentPath, -1).equals(SVNNodeKind.DIR)) {
      changed = _operations.log(attachmentPath, -1, false, false, 0, -1);
    }
    Map<String, AttachmentHistory> results = new LinkedHashMap<String, AttachmentHistory>();
    for (ChangeInfo change : changed) {
      if (change.getKind() == StoreKind.ATTACHMENT) {
        AttachmentHistory history = results.get(change.getName());
        if (history == null) {
          history = new AttachmentHistory();
          results.put(change.getName(), history);
        }
        history.getVersions().add(change);
      }
    }
    return results.values();
  }
  
  private String attachmentPath(final PageReference ref) {
    return ref.getPath() + "-attachments";
  }

  public void attachment(final PageReference ref, final String attachment, final long revision, final ContentTypedSink sink) throws NotFoundException, PageStoreException {
    final String path = SVNPathUtil.append(attachmentPath(ref), attachment);
    final OutputStream out = new LazyOutputStream() {
      protected OutputStream lazyInit() throws IOException {
        sink.setContentType("application/octet-stream"); 
        sink.setFileName(attachment);
        return sink.stream();
      }
    };
    _operations.getFile(path, revision, null, out);
  }

  public Collection<PageReference> getChangedBetween(final long start, final long end) throws PageStoreException {
    List<ChangeInfo> log = _operations.log("", -1, false, true, start, end);
    Set<PageReference> pages = new LinkedHashSet<PageReference>(log.size());
    for (ChangeInfo info : log) {
      if (info.getKind() == StoreKind.PAGE) {
        pages.add(new PageReference(info.getPage()));
      }
    }
    return pages;
  }

  public long getLatestRevision() throws PageStoreAuthenticationException, PageStoreException {
    return _operations.getLatestRevision();
  }

  public long copy(final PageReference from, final long fromRevision, final PageReference to, final String commitMessage) throws PageStoreException {
    return _operations.copy(from.getPath(), fromRevision, to.getPath(), commitMessage);
  }

  public long rename(final PageReference from, final PageReference to, final long baseRevision, final String commitMessage) throws InterveningCommitException, PageStoreException {
    final String fromAttachmentDir = attachmentPath(from);
    final String toAttachmentDir = attachmentPath(to);
    final boolean needToMoveAttachmentDir =  _operations.checkPath(fromAttachmentDir, baseRevision) == SVNNodeKind.DIR;
    return _operations.execute(new SVNEditAction(commitMessage) {
      protected void driveCommitEditor(final ISVNEditor commitEditor) throws SVNException, IOException {
        moveFile(commitEditor, from.getPath(), baseRevision, to.getPath());
        if (needToMoveAttachmentDir) {
          moveDir(commitEditor, fromAttachmentDir, baseRevision, toAttachmentDir);
        }
      }
    });
  }

}
