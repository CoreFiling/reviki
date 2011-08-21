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
package net.hillsdon.reviki.vc.impl;

import static java.lang.String.format;
import static net.hillsdon.fij.text.Strings.fromUTF8;
import static net.hillsdon.reviki.wiki.macros.AttrMacro.REVIKI_ATTRIBUTE_PREFIX;

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

import net.hillsdon.fij.io.LazyOutputStream;
import net.hillsdon.fij.text.Strings;
import net.hillsdon.reviki.vc.AlreadyLockedException;
import net.hillsdon.reviki.vc.AttachmentHistory;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ChangeType;
import net.hillsdon.reviki.vc.ConflictException;
import net.hillsdon.reviki.vc.ContentTypedSink;
import net.hillsdon.reviki.vc.InterveningCommitException;
import net.hillsdon.reviki.vc.LostLockException;
import net.hillsdon.reviki.vc.MimeIdentifier;
import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.VersionedPageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStoreAuthenticationException;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.PageStoreInvalidException;
import net.hillsdon.reviki.vc.RenameException;
import net.hillsdon.reviki.vc.SaveException;
import net.hillsdon.reviki.vc.StoreKind;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.internal.util.SVNDate;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.io.ISVNEditor;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

/**
 * Stores pages in an SVN repository.
 *
 * @author mth
 */
public class SVNPageStore extends AbstractPageStore {

  static final class SVNRenameAction extends SVNEditAction {
    private final PageReference _toPath;
    private final boolean _needToMoveAttachmentDir;
    private final PageReference _fromPath;
    private final long _baseRevision;

    SVNRenameAction(final String commitMessage, final PageReference toPath, final boolean needToMoveAttachmentDir, final PageReference fromPath, final long baseRevision) {
      super(commitMessage);
      _toPath = toPath;
      _needToMoveAttachmentDir = needToMoveAttachmentDir;
      _fromPath = fromPath;
      _baseRevision = baseRevision;
    }

    @Override
    protected void driveCommitEditor(final ISVNEditor commitEditor, final BasicSVNOperations operations) throws SVNException, IOException, RenameException {
      try {
        operations.moveFile(commitEditor, _fromPath.getPath(), _baseRevision, _toPath.getPath());
        if (_needToMoveAttachmentDir) {
          operations.moveDir(commitEditor, _fromPath.getAttachmentPath(), _baseRevision, _toPath.getAttachmentPath());
        }
      }
      catch (SVNException e) {
        if (SVNErrorCode.RA_DAV_REQUEST_FAILED.equals(e.getErrorMessage().getErrorCode())) {
           throw new RenameException(e);
        }
        throw e;
      }
    }
  }

  private static final Predicate<ChangeInfo> IS_CHANGE_TO_PAGE = new Predicate<ChangeInfo>() {
    public boolean apply(final ChangeInfo in) {
      return in.getKind() == StoreKind.PAGE;
    }
  };

  private final String _wiki;
  private final BasicSVNOperations _operations;
  private final DeletedRevisionTracker _tracker;
  private final MimeIdentifier _mimeIdentifier;
  private final AutoPropertiesApplier _autoPropertiesApplier;


  /**
   * Note the repository URL can be deep, it need not refer to the root of the
   * repository itself. We put pages in the root of what we're given.
   */
  public SVNPageStore(final String wiki, final DeletedRevisionTracker tracker, final BasicSVNOperations operations, final AutoPropertiesApplier autoPropertiesApplier, final MimeIdentifier mimeIdentifier) {
    _wiki = wiki;
    _tracker = tracker;
    _operations = operations;
    _autoPropertiesApplier = autoPropertiesApplier;
    _mimeIdentifier = mimeIdentifier;
  }

  public List<ChangeInfo> recentChanges(final long limit) throws PageStoreException {
    return _operations.log("", limit, LogEntryFilter.DESCENDANTS, true, 0, -1);
  }

  public List<ChangeInfo> history(final PageReference ref) throws PageStoreException {
    final List<ChangeInfo> changes = new ArrayList<ChangeInfo>();
    final ChangeInfo deletedIn = getChangeThatDeleted(ref);
    long lastRevision = deletedIn == null ? -1 : deletedIn.getRevision() - 1;
    // We follow all the previous locations.
    String path = ref.getPath();
    while (path != null && changes.addAll(_operations.log(path, -1, LogEntryFilter.PATH_ONLY, true, 0, lastRevision))) {
      if (!changes.isEmpty()) {
        ChangeInfo last = changes.get(changes.size() - 1);
        path = last.getCopiedFrom();
        lastRevision = last.getCopiedFromRevision();
      }
    }
    if (deletedIn != null) {
      changes.add(0, deletedIn);
    }
    List<ChangeInfo> result = ImmutableList.copyOf(Iterables.filter(changes, IS_CHANGE_TO_PAGE));
    return Ordering.from(DeletesAfterOtherSameRevisionChanges.INSTANCE).sortedCopy(result);
  }

  public Set<PageReference> list() throws PageStoreException {
    Set<PageReference> names = new LinkedHashSet<PageReference>();
    for (String page : _tracker.currentExistingEntries()) {
      names.add(new PageReferenceImpl(page));
    }
    return names;
  }

  public VersionedPageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Map<String, String> properties = new HashMap<String, String>();
    SVNNodeKind kind = _operations.checkPath(ref.getPath(), revision);
    if (SVNNodeKind.FILE.equals(kind)) {
      _operations.getFile(ref.getPath(), revision, properties, baos);
      long actualRevision = SVNProperty.longValue(properties.get(SVNProperty.REVISION));
      long lastChangedRevision = SVNProperty.longValue(properties.get(SVNProperty.COMMITTED_REVISION));
      Date lastChangedDate = SVNDate.parseDate(properties.get(SVNProperty.COMMITTED_DATE));
      String lastChangedAuthor = properties.get(SVNProperty.LAST_AUTHOR);
      String lockOwner = null;
      String lockToken = null;
      Date lockedSince = null;
      final Map<String, String> filteredProperties = Maps.filterKeys(properties, new Predicate<String>() {
        public boolean apply(String key) {
          return key.startsWith(REVIKI_ATTRIBUTE_PREFIX);
        }
      });
      Map<String, String> attributes = stripPrefix(filteredProperties, REVIKI_ATTRIBUTE_PREFIX);
      try {
        if (revision == -1 || _operations.checkPath(ref.getPath(), -1) == SVNNodeKind.FILE) {
          SVNLock lock = _operations.getLock(ref.getPath());
          if (lock != null) {
            lockOwner = lock.getOwner();
            lockToken = lock.getID();
            lockedSince = lock.getCreationDate();
          }
        }
      }
      catch (NotFoundException ex) {
        // It was a file at 'revision' but is now deleted so we can't get the lock information.
      }
      return new VersionedPageInfoImpl(_wiki, ref.getPath(), Strings.toUTF8(baos.toByteArray()), actualRevision, lastChangedRevision, lastChangedAuthor, lastChangedDate, lockOwner, lockToken, lockedSince, attributes);
    }
    else if (SVNNodeKind.NONE.equals(kind)) {
      long pseudoRevision = VersionedPageInfo.UNCOMMITTED;
      long lastChangedRevision = VersionedPageInfo.UNCOMMITTED;
      String lastChangedAuthor = null;
      Date lastChangedDate = null;
      final ChangeInfo deletingChange = getChangeThatDeleted(ref);
      if (deletingChange != null) {
        pseudoRevision = VersionedPageInfo.DELETED;
        lastChangedRevision = deletingChange.getRevision();
        lastChangedAuthor = deletingChange.getUser();
        lastChangedDate = deletingChange.getDate();
      }
      return new VersionedPageInfoImpl(_wiki, ref.getPath(), "", pseudoRevision, lastChangedRevision, lastChangedAuthor, lastChangedDate, null, null, null);
    }
    else {
      throw new PageStoreException(format("Unexpected node kind '%s' at '%s'", kind, ref));
    }
  }

  private Map<String, String> stripPrefix(Map<String, String> properties, String propertyPrefix) {
    Map<String, String> attributes = new LinkedHashMap<String, String>();
    for(Map.Entry<String, String> entry: properties.entrySet()) {
      attributes.put(entry.getKey().substring(propertyPrefix.length()), entry.getValue());
    }
    return attributes;
  }

  private ChangeInfo getChangeThatDeleted(final PageReference ref) throws PageStoreAuthenticationException, PageStoreException {
    return _tracker.getChangeThatDeleted(ref.getPath());
  }

  public VersionedPageInfo tryToLock(final PageReference ref) throws PageStoreException {
    final VersionedPageInfo page = get(ref, -1);
    if (page.isNewPage()) {
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

  public long set(final PageInfo page, final String lockToken, final long baseRevision, final String commitMessage) throws PageStoreAuthenticationException, PageStoreException {
    final String path = page.getPath();
    final String content = page.getContent();
    if (content.trim().length() == 0) {
      return delete(path, lockToken, baseRevision, commitMessage);
    }
    return _operations.execute(new SVNEditAction(commitMessage, createLocksMap(path, lockToken)) {
      @Override
      protected void driveCommitEditor(final ISVNEditor commitEditor, final BasicSVNOperations operations) throws SVNException, IOException, SaveException {
        try {
          Map<String, String> properties = addPrefix(page.getAttributes(), REVIKI_ATTRIBUTE_PREFIX);
          String dir = SVNPathUtil.removeTail(page.getPath());
          commitEditor.openDir(dir, baseRevision);
          set(commitEditor, path, baseRevision, new ByteArrayInputStream(Strings.fromUTF8(content)), properties);
          commitEditor.closeDir();
        }
        catch (SVNException e) {
          if (SVNErrorCode.RA_DAV_REQUEST_FAILED.equals(e.getErrorMessage().getErrorCode())) {
            throw new LostLockException(e);
          }
          else if (SVNErrorCode.FS_CONFLICT.equals(e.getErrorMessage().getErrorCode())) {
            throw new ConflictException(e);
          }
          throw e;
        }
      }
    });
  }

  private Map<String, String> addPrefix(Map<String, String> attributes, String prefix) {
    Map<String, String> properties = new LinkedHashMap<String, String>();
    for(Map.Entry<String, String> entry: attributes.entrySet()) {
      properties.put(REVIKI_ATTRIBUTE_PREFIX + entry.getKey(), entry.getValue());
    }
    return properties;
  }

  private long delete(final String path, final String lockToken, final long baseRevision, final String commitMessage) throws PageStoreAuthenticationException, PageStoreException {
    _operations.execute(new SVNEditAction(commitMessage, createLocksMap(path, lockToken)) {
      @Override
      protected void driveCommitEditor(final ISVNEditor commitEditor, final BasicSVNOperations operations) throws SVNException, IOException {
        _operations.delete(commitEditor, path, baseRevision);
      }
    });
    return VersionedPageInfo.DELETED;
  }

  public long deleteAttachment(final PageReference pageRef, final String attachmentName, final long baseRevision, final String commitMessage) throws PageStoreAuthenticationException, PageStoreException {
    final String path = SVNPathUtil.append(pageRef.getAttachmentPath(), attachmentName);
    return _operations.execute(new SVNEditAction(commitMessage) {
      @Override
      protected void driveCommitEditor(final ISVNEditor commitEditor, final BasicSVNOperations operations) throws SVNException, IOException {
        _operations.delete(commitEditor, path, baseRevision);
      }
    });
  }

  private void set(final ISVNEditor commitEditor, final String path, final long baseRevision, final InputStream content, final Map<String, String> attributes) throws SVNException, IOException {
    if (baseRevision < 0) {
      _operations.create(commitEditor, path, content, attributes);
    }
    else {
      _operations.edit(commitEditor, path, baseRevision, content, attributes);
    }
  }

  public void attach(final PageReference pageRef, final String storeName, final long baseRevision, final InputStream in, final String commitMessage) throws PageStoreException {
    _autoPropertiesApplier.read();

    final boolean addLinkToPage;
    final VersionedPageInfo versionedPageInfo;
    final long latestRevision;
    if (baseRevision < 0) {
      latestRevision = getLatestRevision();
      versionedPageInfo = get(pageRef, latestRevision);
      addLinkToPage = !versionedPageInfo.isLocked();
    }
    else {
      versionedPageInfo = null;
      addLinkToPage = false;
      latestRevision = -1;
    }

    final String dir = pageRef.getAttachmentPath();
    final boolean needToCreateAttachmentDir =  _operations.checkPath(dir, baseRevision) == SVNNodeKind.NONE;
    _operations.execute(new SVNEditAction(commitMessage) {
      @Override
      protected void driveCommitEditor(final ISVNEditor commitEditor, final BasicSVNOperations operations) throws SVNException, IOException {
        if (needToCreateAttachmentDir) {
          operations.createDirectory(commitEditor, dir);
        }
        else {
          commitEditor.openDir(dir, baseRevision);
        }
        set(commitEditor, dir + "/" + storeName, baseRevision, in, new LinkedHashMap<String, String>());
        commitEditor.closeDir();

        if (addLinkToPage) {
          final boolean isImage = _mimeIdentifier.isImage(storeName);
          final String link = (isImage ? "{{" : "[[") + "attachments/" + storeName + "|" + storeName + (isImage ? "}}" : "]]");
          final String newContent = versionedPageInfo.getContent() + Strings.CRLF + link + Strings.CRLF;
          commitEditor.openDir(SVNPathUtil.removeTail(pageRef.getPath()), -1);
          if(versionedPageInfo.isNewPage()) {
            // create the page
            set(commitEditor, pageRef.getPath(), -1, new ByteArrayInputStream(fromUTF8(newContent)), new LinkedHashMap<String, String>());
          }
          else {
            set(commitEditor, pageRef.getPath(), latestRevision, new ByteArrayInputStream(fromUTF8(newContent)), new LinkedHashMap<String, String>());
          }
          commitEditor.closeDir();
        }
      }
    });
  }

  public Collection<AttachmentHistory> attachments(final PageReference ref) throws PageStoreException {
    final String attachmentPath = ref.getAttachmentPath();
    final Map<String, AttachmentHistory> results = new LinkedHashMap<String, AttachmentHistory>();
    if (_operations.checkPath(attachmentPath, -1).equals(SVNNodeKind.DIR)) {
      final Collection<ChangeInfo> changed = _operations.log(attachmentPath, -1, LogEntryFilter.DESCENDANTS, false, 0, -1);
      for (ChangeInfo change : changed) {
        if (change.getKind() == StoreKind.ATTACHMENT) {
          AttachmentHistory history = results.get(change.getName());
          if (history == null) {
            history = new AttachmentHistory(change.getChangeType()==ChangeType.DELETED);
            results.put(change.getName(), history);
          }
          if(change.getChangeType()!=ChangeType.DELETED) history.getVersions().add(change);
        }
      }
      // We need to log and ls - consider the case of copying an attachment *directory*.
      for (SVNDirEntry attachment : _operations.ls(attachmentPath)) {
        if (!results.containsKey(attachment.getName())) {
          AttachmentHistory history = new AttachmentHistory(false);
          ChangeInfo change = new ChangeInfo(ref.getName(), attachment.getName(), attachment.getAuthor(), attachment.getDate(), attachment.getRevision(), attachment.getCommitMessage(), StoreKind.ATTACHMENT, ChangeType.ADDED, null, -1);
          if(change.getChangeType()!=ChangeType.DELETED) history.getVersions().add(change);
          results.put(attachment.getName(), history);
        }
      }
    }

    return results.values();
  }

  public void attachment(final PageReference ref, final String attachment, final long revision, final ContentTypedSink sink) throws NotFoundException, PageStoreException {
    final String path = SVNPathUtil.append(ref.getAttachmentPath(), attachment);
    final Map<String, String> properties = new HashMap<String, String>();
    // Get the properties
    _operations.getFile(path, revision, properties, null);

    // If the mimetype property was set, replace the default setting in the sink
    final String mimetype = properties.get(SVNProperty.MIME_TYPE);

    // Create output and set the content type and file name
    final OutputStream out = new LazyOutputStream() {
      @Override
      protected OutputStream lazyInit() throws IOException {
        if (mimetype!=null) {
          sink.setContentType(mimetype);
        }
        else {
          sink.setContentType("application/octet-stream");
        }
        sink.setFileName(attachment);
        return sink.stream();
      }
    };

    // Get the file
    _operations.getFile(path, revision, null, out);
  }

  public Collection<PageReference> getChangedBetween(final long start, final long end) throws PageStoreException {
    List<ChangeInfo> log = _operations.log("", -1, LogEntryFilter.DESCENDANTS, true, start, end);
    Set<PageReference> pages = new LinkedHashSet<PageReference>(log.size());
    for (ChangeInfo info : log) {
      if (info.getKind() == StoreKind.PAGE) {
        pages.add(new PageReferenceImpl(info.getPage()));
      }
    }
    return pages;
  }

  public long getLatestRevision() throws PageStoreAuthenticationException, PageStoreException {
    return _operations.getLatestRevision();
  }

  public long copy(final PageReference from, final long fromRevision, final PageReference to, final String commitMessage) throws PageStoreException {
    return _operations.execute(new SVNEditAction(commitMessage) {
      @Override
      protected void driveCommitEditor(final ISVNEditor commitEditor, final BasicSVNOperations operations) throws SVNException, IOException {
        _operations.copy(commitEditor, from.getPath(), fromRevision, to.getPath());
      }
    });
  }

  public long rename(final PageReference from, final PageReference to, final long baseRevision, final String commitMessage) throws InterveningCommitException, PageStoreException {
    final boolean needToMoveAttachmentDir =  _operations.checkPath(from.getAttachmentPath(), baseRevision) == SVNNodeKind.DIR;
    return _operations.execute(new SVNRenameAction(commitMessage, to, needToMoveAttachmentDir, from, baseRevision));
  }

  // Exposed for testing
  Map<String, String> createLocksMap(final String path, final String lockToken) {
    if ("".equals(lockToken)) {
      // Something's gone wrong if we end up with the empty string here (null means not locked)
      // and it causes hard-to-debug problems if we carry on.
      throw new IllegalArgumentException("Empty lock token");
    }
    return lockToken == null ? Collections.<String, String> emptyMap() : Collections.<String, String> singletonMap(path, lockToken);
  }

  public void assertValid() throws PageStoreInvalidException, PageStoreAuthenticationException {
    try {
      if (_operations.checkPath("", -1) == SVNNodeKind.NONE) {
        throw new PageStoreInvalidException();
      }
    }
    catch (PageStoreAuthenticationException e) {
      throw e;
    }
    catch (PageStoreException e) {
      // Assume this always means we're broken.  If that's not true then we'll need to push this
      // into operations and check the different SVNKit failure codes there.
      throw new PageStoreInvalidException(e);
    }
  }

  public String getWiki() throws PageStoreException {
    return _wiki;
  }

}
