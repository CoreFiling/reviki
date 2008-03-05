/**
 * Copyright 2007 Matthew Hillsdon
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
package net.hillsdon.svnwiki.vc;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static net.hillsdon.fij.core.Functional.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
import net.hillsdon.svnwiki.vc.SVNHelper.SVNAction;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.util.SVNTimeUtil;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;

/**
 * Stores pages in an SVN repository.
 * 
 * @author mth
 */
public class SVNPageStore implements PageStore {

  private static final Predicate<ChangeInfo> CHANGE_TO_PAGE = new Predicate<ChangeInfo>() {
    public Boolean transform(final ChangeInfo in) {
      return in.getKind() == StoreKind.PAGE;
    }
  };

  /**
   * The assumed encoding of files from the repository.
   */
  private static final String UTF8 = "UTF8";

  private final SVNHelper _helper;

  private final DeletionRevisionTracker _tracker;

  /**
   * Note the repository URL can be deep, it need not refer to the root of the
   * repository itself. We put pages in the root of what we're given.
   */
  public SVNPageStore(final DeletionRevisionTracker tracker, final SVNRepository repository) {
    _tracker = tracker;
    _helper = new SVNHelper(repository);
  }

  public List<ChangeInfo> recentChanges(final int limit) throws PageStoreException {
    return _helper.log("", limit, false, 0, -1);
  }

  public List<ChangeInfo> history(final PageReference ref) throws PageStoreException {
    final ChangeInfo deletedIn = getChangeThatDeleted(ref);
    long lastRevision = deletedIn == null ? -1 : deletedIn.getRevision() - 1;
    List<ChangeInfo> changes = _helper.log(ref.getPath(), -1, true, 0, lastRevision);
    if (deletedIn != null) {
      changes.add(0, deletedIn);
    }
    return Functional.list((((filter(changes, CHANGE_TO_PAGE)))));
  }

  public Collection<PageReference> list() throws PageStoreException {
    // Should  we be returning the entries here?
    Set<PageReference> names = new LinkedHashSet<PageReference>();
    for (PageStoreEntry e : _helper.listFiles("")) {
      names.add(new PageReference(e.getName()));
    }
    return names;
  }

  public PageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    return _helper.execute(new SVNAction<PageInfo>() {
      public PageInfo perform(final SVNRepository repository) throws SVNException, PageStoreException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HashMap<String, String> properties = new HashMap<String, String>();
        SVNNodeKind kind = repository.checkPath(ref.getPath(), revision);
        if (SVNNodeKind.FILE.equals(kind)) {
          repository.getFile(ref.getPath(), revision, properties, baos);
          long actualRevision = SVNProperty.longValue(properties.get(SVNProperty.REVISION));
          long lastChangedRevision = SVNProperty.longValue(properties.get(SVNProperty.COMMITTED_REVISION));
          Date lastChangedDate = SVNTimeUtil.parseDate(properties.get(SVNProperty.COMMITTED_DATE));
          String lastChangedAuthor = properties.get(SVNProperty.LAST_AUTHOR);
          SVNLock lock = null;
          try {
            if (revision == -1 || repository.checkPath(ref.getPath(), -1) == SVNNodeKind.FILE) {
              lock = repository.getLock(ref.getPath());
            }
          }
          catch (SVNException ex) {
            // It was a file at 'revision' but is now deleted so we can't get the lock information.
            if (!isPathNotFoundError(ex)) {
              throw ex;
            }
          }
          String lockOwner = lock == null ? null : lock.getOwner();
          String lockToken = lock == null ? null : lock.getID();
          return new PageInfo(ref.getPath(), toUTF8(baos.toByteArray()), actualRevision, lastChangedRevision, lastChangedAuthor, lastChangedDate, lockOwner, lockToken);
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

      private boolean isPathNotFoundError(final SVNException ex) {
        return ex.getErrorMessage().getErrorCode() == SVNErrorCode.RA_DAV_PATH_NOT_FOUND;
      }

    });
  }

  private ChangeInfo getChangeThatDeleted(final PageReference ref) throws PageStoreAuthenticationException, PageStoreException {
    return _tracker.getChangeThatDeleted(_helper, ref.getPath());
  }
  
  public PageInfo tryToLock(final PageReference ref) throws PageStoreException {
    final PageInfo page = get(ref, -1);
    if (page.isNew()) {
      return page;
    }
    if (page.isLocked()) {
      return page;
    }

    return _helper.execute(new SVNAction<PageInfo>() {
      public PageInfo perform(final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          long revision = page.getRevision();
          Map<String, Long> pathsToRevisions = singletonMap(ref.getPath(), revision);
          repository.lock(pathsToRevisions, "Locked by svnwiki.", false, new SVNLockHandlerAdapter());
          return get(ref, revision);
        }
        catch (SVNException ex) {
          if (SVNErrorCode.FS_PATH_ALREADY_LOCKED.equals(ex.getErrorMessage().getErrorCode())) {
            // The caller will check getLockedBy().
            return get(ref, -1);
          }
          throw ex;
        }
      }
    });
  }

  public void unlock(final PageReference path, final String lockToken) throws PageStoreException {
    _helper.execute(new SVNAction<Void>() {
      public Void perform(final SVNRepository repository) throws SVNException, PageStoreException {
        repository.unlock(singletonMap(path.getPath(), lockToken), false, new SVNLockHandlerAdapter());
        return null;
      }
    });
  }

  public long set(final PageReference ref, final String lockToken, final long baseRevision, final String content, final String commitMessage) throws PageStoreAuthenticationException, PageStoreException {
    if (content.trim().length() == 0) {
      return delete(ref.getPath(), lockToken, baseRevision, commitMessage);
    }
    return set(ref.getPath(), lockToken, baseRevision, new ByteArrayInputStream(fromUTF8(content)), commitMessage);
  }

  private void checkForInterveningCommit(final SVNException ex) throws InterveningCommitException {
    if (SVNErrorCode.FS_CONFLICT.equals(ex.getErrorMessage().getErrorCode())) {
      // What to do!
      throw new InterveningCommitException(ex);
    }
  }

  private long delete(final String path, final String lockToken, final long baseRevision, final String commitMessage) throws PageStoreAuthenticationException, PageStoreException {
    _helper.execute(new SVNAction<Void>() {
      public Void perform(final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          Map<String, String> locks = lockToken == null ? Collections.<String, String> emptyMap() : Collections.<String, String> singletonMap(path, lockToken);
          ISVNEditor commitEditor = repository.getCommitEditor(commitMessage, locks, false, null);
          _helper.deleteFile(commitEditor, path, baseRevision);
          commitEditor.closeEdit();
        }
        catch (SVNException ex) {
          checkForInterveningCommit(ex);
          throw ex;
        }
        return null;
      }
    });
    return PageInfo.DELETED;
  }

  private long set(final String path, final String lockToken, final long baseRevision, final InputStream content, final String commitMessage) throws PageStoreException {
    return _helper.execute(new SVNAction<Long>() {
      public Long perform(final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          Map<String, String> locks = lockToken == null ? Collections.<String, String> emptyMap() : Collections.<String, String> singletonMap(path, lockToken);
          ISVNEditor commitEditor = repository.getCommitEditor(commitMessage, locks, false, null);
          if (baseRevision < 0) {
            _helper.createFile(commitEditor, path, content);
          }
          else {
            _helper.editFile(commitEditor, path, baseRevision, content);
          }
          return commitEditor.closeEdit().getNewRevision();
        }
        catch (SVNException ex) {
          checkForInterveningCommit(ex);
          throw ex;
        }
      }
    });
  }

  public void attach(final PageReference ref, final String storeName, final long baseRevision, final InputStream in, final String commitMessage) throws PageStoreException {
    String dir = attachmentPath(ref);
    ensureDir(dir, commitMessage);
    set(dir + "/" + storeName, null, baseRevision, in, commitMessage);
  }

  public Collection<AttachmentHistory> attachments(final PageReference ref) throws PageStoreException {
    final String attachmentPath = attachmentPath(ref);
    List<ChangeInfo> changed = _helper.execute(new SVNAction<List<ChangeInfo>>() {
      public List<ChangeInfo> perform(final SVNRepository repository) throws SVNException, PageStoreException {
        if (repository.checkPath(attachmentPath, -1).equals(SVNNodeKind.DIR)) {
          return _helper.log(attachmentPath, -1, false, 0, -1);
        }
        return Collections.emptyList();
      }
    });
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

  private void ensureDir(final String dir, final String commitMessage) throws PageStoreException {
    _helper.execute(new SVNAction<Void>() {
      public Void perform(final SVNRepository repository) throws SVNException, PageStoreException {
        if (repository.checkPath(dir, -1) == SVNNodeKind.NONE) {
          ISVNEditor commitEditor = repository.getCommitEditor(commitMessage, null);
          try {
            _helper.createDir(commitEditor, dir);
          }
          finally {
            commitEditor.closeEdit();
          }
        }
        return null;
      }
    });
  }


  private static String toUTF8(final byte[] bytes) {
    try {
      return new String(bytes, UTF8);
    }
    catch (UnsupportedEncodingException e) {
      throw new AssertionError("Java supports UTF8.");
    }
  }

  private static byte[] fromUTF8(final String string) {
    try {
      return string.getBytes(UTF8);
    }
    catch (UnsupportedEncodingException e) {
      throw new AssertionError("Java supports UTF8.");
    }
  }

  public void attachment(final PageReference ref, final String attachment, final long revision, final ContentTypedSink sink) throws NotFoundException, PageStoreException {
    final String path = SVNPathUtil.append(attachmentPath(ref), attachment);
    final OutputStream out = new OutputStream() {
      boolean _first = true;
      public void write(final int b) throws IOException {
        if (_first) {
          sink.setContentType("application/octet-stream"); 
          sink.setFileName(attachment);
          _first = false;
        }
        sink.stream().write(b);
      }
    };
    
    _helper.execute(new SVNAction<Void>() {
      public Void perform(final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          repository.getFile(path, revision, null, out);
        }
        catch (SVNException ex) {
          // FIXME: Presumably this code would be different for non-http repositories.
          if (SVNErrorCode.RA_DAV_REQUEST_FAILED.equals(ex.getErrorMessage().getErrorCode())) {
            throw new NotFoundException(ex);
          }
          throw ex;
        }
        return null;
      }
    });
  }

  public Collection<PageReference> getChangedBetween(final long start, final long end) throws PageStoreException {
    List<ChangeInfo> log = _helper.log("", -1, false, start, end);
    Set<PageReference> pages = new LinkedHashSet<PageReference>(log.size());
    for (ChangeInfo info : log) {
      if (info.getKind() == StoreKind.PAGE) {
        pages.add(new PageReference(info.getPage()));
      }
    }
    return pages;
  }

  public long getLatestRevision() throws PageStoreAuthenticationException, PageStoreException {
    return _helper.getLatestRevision();
  }

}
