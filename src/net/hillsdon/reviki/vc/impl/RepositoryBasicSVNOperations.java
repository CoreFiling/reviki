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

import static java.util.Collections.singletonMap;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.hillsdon.reviki.vc.AlreadyLockedException;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ChangeType;
import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.vc.VersionedPageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStoreAuthenticationException;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.StoreKind;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.ISVNFileCheckoutTarget;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * The real impl, using an {@link SVNRepository}.
 *
 * Currently some error handling may depend on it being a DAVRepository, this needs review.
 *
 * @author mth
 */
public class RepositoryBasicSVNOperations implements BasicSVNOperations {

  private final SVNRepository _repository;
  private final AutoPropertiesApplier _autoPropertiesApplier;

  public RepositoryBasicSVNOperations(final SVNRepository repository, final AutoPropertiesApplier autoPropertiesApplier) {
    _repository = repository;
    _autoPropertiesApplier = autoPropertiesApplier;
  }

  public List<ChangeInfo> log(final String path, final long limit, final LogEntryFilter logEntryFilter, final boolean stopOnCopy, final long startRevision, final long endRevision) throws PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<List<ChangeInfo>>() {
      public List<ChangeInfo> perform(final BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
        final List<ChangeInfo> entries = new LinkedList<ChangeInfo>();
        // Start and end reversed to get newest changes first.
        SVNRepository repos = getSVNReposForRevision(_repository, endRevision);
        final String[] rootPath = {repos.getRepositoryPath("")};
        repos.log(new String[] { path }, endRevision, startRevision, true, stopOnCopy, limit, new ISVNLogEntryHandler() {
          public void handleLogEntry(final SVNLogEntry logEntry) throws SVNException {
            // Has the wiki root been renamed?  If so then follow the rename.
            if (logEntry.getChangedPaths().containsKey(rootPath[0])) {
              SVNLogEntryPath changedPath = (SVNLogEntryPath) logEntry.getChangedPaths().get(rootPath[0]);
              if (changedPath.getCopyPath() != null) {
                rootPath[0] = changedPath.getCopyPath();
              }
            }
            entries.addAll(logEntryToChangeInfos(rootPath[0], path, logEntry, logEntryFilter));
          }
        });
        return entries;
      }
    });
  }

  @SuppressWarnings("unchecked")
  private List<ChangeInfo> logEntryToChangeInfos(final String rootPath, final String loggedPath, final SVNLogEntry entry, final LogEntryFilter logEntryFilter) {
    final String fullLoggedPathFromAppend = SVNPathUtil.append(rootPath, loggedPath);
    final String fullLoggedPath = fixFullLoggedPath(fullLoggedPathFromAppend);
    final List<ChangeInfo> results = new LinkedList<ChangeInfo>();
    for (Map.Entry<String, SVNLogEntryPath> pathEntry : (Iterable<Map.Entry<String, SVNLogEntryPath>>) entry.getChangedPaths().entrySet()) {
      final String changedPath = pathEntry.getKey();
      if (logEntryFilter.accept(fullLoggedPath, pathEntry.getValue())) {
        ChangeInfo change = classifiedChange(entry, rootPath, changedPath);
        // Might want to put this at a higher level if we can ever do
        // something useful with 'other' changes.
        if (change.getKind() != StoreKind.OTHER) {
          results.add(change);
        }
      }
    }
    return results;
  }

  static String fixFullLoggedPath(String fullLoggedPathFromAppend) {
    boolean hasTrailing = fullLoggedPathFromAppend.endsWith("/");
    boolean hasLeading = fullLoggedPathFromAppend.startsWith("/");

    // Always remove trailing "/"
    if (hasTrailing) {
      fullLoggedPathFromAppend = fullLoggedPathFromAppend.substring(0, fullLoggedPathFromAppend.length()-1);
    }
    // Ensure leading "/" unless empty string
    if (!hasLeading && !"".equals(fullLoggedPathFromAppend)) {
      fullLoggedPathFromAppend = "/" + fullLoggedPathFromAppend;
    }
    return fullLoggedPathFromAppend;
  }

  public String getRoot() throws PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<String>() {
      public String perform(final BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
        return _repository.getRepositoryPath("");
      }
    });
  }

  public <T> T execute(final SVNAction<T> action) throws PageStoreException, PageStoreAuthenticationException {
    try {
      return action.perform(this, _repository);
    }
    catch (PageStoreException ex) {
      throw ex;
    }
    catch (SVNAuthenticationException ex) {
      throw new PageStoreAuthenticationException(ex);
    }
    catch (Exception ex) {
      throw new PageStoreException(ex);
    }
  }

  private static final Pattern PAGE_PATH = Pattern.compile("[^/]*");
  private static final Pattern ATTACHMENT_PATH = Pattern.compile("([^/]*?)-attachments/(.*)");
  static ChangeInfo classifiedChange(final SVNLogEntry entry, String rootPath, final String path) {
    StoreKind kind = StoreKind.OTHER;
    // Be sure the root path ends with a slash because the 'path' will always have the slash.
    if (!rootPath.endsWith("/")) {
      rootPath = rootPath + "/";
    }
    String name = path.length() > rootPath.length() ? path.substring(rootPath.length()) : path;
    String page = null;
    Matcher matcher = PAGE_PATH.matcher(name);
    if (matcher.matches() && !name.endsWith("-attachments")) {
      kind = StoreKind.PAGE;
      page = name;
    }
    else {
      matcher = ATTACHMENT_PATH.matcher(name);
      if (matcher.matches()) {
        kind = StoreKind.ATTACHMENT;
        page = matcher.group(1);
        name = matcher.group(2);
      }
    }
    String user = entry.getAuthor();
    Date date = entry.getDate();
    SVNLogEntryPath logForPath = (SVNLogEntryPath) entry.getChangedPaths().get(path);
    
    PageReference renamedTo = null;
    for (Object entryPath : entry.getChangedPaths().values()) {
      SVNLogEntryPath logEntryPath = (SVNLogEntryPath) entryPath;
      if (ChangeType.forCode(logEntryPath.getType()).equals(ChangeType.ADDED) && path.equals(logEntryPath.getCopyPath())) {
        renamedTo = new PageReferenceImpl(logEntryPath.getPath());
      }
    }
    String copiedFrom = logForPath.getCopyPath();
    long copiedFromRevision = -1;
    if (SVNPathUtil.isAncestor(rootPath, copiedFrom)) {
      copiedFrom = SVNPathUtil.tail(copiedFrom);
      copiedFromRevision = logForPath.getCopyRevision();
    }
    else {
      copiedFrom = null;
    }
    return new ChangeInfo(page, name, user, date, entry.getRevision(), entry.getMessage(), kind, ChangeType.forCode(logForPath.getType()), copiedFrom, copiedFromRevision, renamedTo);
  }

  public void unlock(final PageReference ref, final String lockToken) throws PageStoreAuthenticationException, PageStoreException {
    execute(new SVNAction<Void>() {
      public Void perform(final BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          repository.unlock(singletonMap(ref.getPath(), lockToken), true, new SVNLockHandlerAdapter());
        }
        catch (SVNException ex) {
          // FIXME: Presumably this code would be different for non-http repositories.
          if (SVNErrorCode.RA_DAV_REQUEST_FAILED.equals(ex.getErrorMessage().getErrorCode())) {
            // We get this when the page has already been unlocked.
            return null;
          }
          throw ex;
        }
        return null;
      }
    });
  }

  public void lock(final PageReference ref, final long revision) throws AlreadyLockedException, PageStoreAuthenticationException, PageStoreException {
    execute(new SVNAction<VersionedPageInfo>() {
      public VersionedPageInfo perform(final BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          Map<String, Long> pathsToRevisions = singletonMap(ref.getPath(), revision);
          repository.lock(pathsToRevisions, "Locked by reviki.", false, new SVNLockHandlerAdapter());
        }
        catch (SVNException ex) {
          if (SVNErrorCode.FS_PATH_ALREADY_LOCKED.equals(ex.getErrorMessage().getErrorCode())) {
            // The caller will check getLockedBy().
            throw new AlreadyLockedException(ex);
          }
          throw ex;
        }
        return null;
      }
    });

  }

  public void getFiles(final long revision, final Map<String, Map<String, String>> properties, final Map<String, ? extends OutputStream> outputStreams) throws NotFoundException, PageStoreAuthenticationException, PageStoreException {
    final long effectiveRevision = revision >= 0 ? revision : getLatestRevision();
    execute(new SVNAction<Void>() {
      public Void perform(final BasicSVNOperations operation, final SVNRepository repository) throws SVNException, PageStoreException {
        ISVNFileCheckoutTarget coTarget = new ISVNFileCheckoutTarget() {
          public OutputStream getOutputStream(final String path) throws SVNException {
            // filePropertyChange doesn't give us this one.
            filePropertyChanged(path, "svn:entry:revision", SVNPropertyValue.create(Long.toString(effectiveRevision)));
            return outputStreams.get(path);
          }

          public void filePropertyChanged(final String path, final String key, final SVNPropertyValue value) throws SVNException {
            if (properties != null) {
              Map<String, String> propsForPath = properties.get(path);
              if (propsForPath != null) {
                propsForPath.put(key, value.getString());
              }
            }
          }
        };
        try {
          getSVNReposForRevision(repository, revision).checkoutFiles(revision, outputStreams.keySet().toArray(new String[outputStreams.size()]), coTarget);
        }
        catch (SVNException ex) {
          // FIXME: This used to check the code, but checkoutFiles gives much more random codes than getFile
          throw new NotFoundException(ex);
        }
        return null;
      }
    });
  }

  private SVNRepository getSVNReposForRevision(final SVNRepository repository, final long revision) throws SVNException {
    SVNWCClient client = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true), repository.getAuthenticationManager()).getWCClient();
    SVNInfo info1 = client.doInfo(repository.getLocation(), SVNRevision.HEAD, SVNRevision.create(revision));

    SVNRepository reposForRev = SVNRepositoryFactory.create(info1.getURL());
    reposForRev.setAuthenticationManager(repository.getAuthenticationManager());

    return reposForRev;
  }

  public void getFile(final String path, final long revision, final Map<String, String> properties, final OutputStream out) throws NotFoundException, PageStoreAuthenticationException, PageStoreException {
    // Not quite all the tests pass if we implement getFile in terms of getFiles, and it would be less efficient
    // getFiles(revision, Collections.singletonMap(path, properties), Collections.singletonMap(path, out));
    execute(new SVNAction<Void>() {
      @SuppressWarnings("unchecked")
      public Void perform(final BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
        final SVNProperties props1 = properties == null ? null : new SVNProperties();
        try {
          try {
            repository.getFile(path, revision, props1, out);
          }
          catch (SVNException ex) {
            // Try again using the location of the wiki root as it was in the given revision
            getSVNReposForRevision(repository, revision).getFile(path, revision, props1, out);
          }

          if(properties != null) {
            final Map<String, SVNPropertyValue> props2= props1.asMap();
            for(Map.Entry<String, SVNPropertyValue> entry : props2.entrySet()) {
              properties.put(entry.getKey(), entry.getValue().getString());
            }
          }
        }
        catch (SVNException ex) {
          // FIXME: Presumably this code would be different for non-http repositories.
          if (SVNErrorCode.FS_NOT_FOUND.equals(ex.getErrorMessage().getErrorCode())) {
            throw new NotFoundException(ex);
          }
          throw ex;
        }
        return null;
      }
    });
  }

  public long getLatestRevision() throws PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<Long>() {
      public Long perform(final BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
        return repository.getLatestRevision();
      }
    });
  }

  public void createDirectory(final ISVNEditor commitEditor, final String dir) throws SVNException {
    commitEditor.addDir(dir, null, -1);
  }

  public SVNNodeKind checkPath(final String path, final long revision) throws PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<SVNNodeKind>() {
      public SVNNodeKind perform(final BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
        SVNNodeKind kind = repository.checkPath(path, revision);
        if (SVNNodeKind.NONE.equals(kind)) {
          kind = getSVNReposForRevision(repository, revision).checkPath(path, revision);
        }
        return kind;
      }
    });
  }

  static String detectMimeType(final BufferedInputStream bis) throws IOException {
    // Currently they analyse 1024 bytes, let's be cautious in case this changes.
    bis.mark(4086);
    try {
      return SVNFileUtil.detectMimeType(bis);
    }
    finally {
      bis.reset();
    }
  }

  public void create(final ISVNEditor commitEditor, final String path, final InputStream content, Map<String, String> attributes) throws SVNException, IOException {
    final BufferedInputStream bis = new BufferedInputStream(content);
    final String autoDetectedMimeType = detectMimeType(bis);

    commitEditor.addFile(path, null, -1);
    commitEditor.applyTextDelta(path, null);
    SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
    String checksum = deltaGenerator.sendDelta(path, bis, commitEditor, true);
    final Map<String, String> autoProperties = _autoPropertiesApplier.apply(path);
    final Map<String, String> allProperties = new LinkedHashMap<String, String>();
    allProperties.putAll(autoProperties);
    allProperties.putAll(attributes);
    setProperties(commitEditor, path, allProperties);
    if (!allProperties.containsKey(SVNProperty.MIME_TYPE) && autoDetectedMimeType != null) {
      commitEditor.changeFileProperty(path, SVNProperty.MIME_TYPE, SVNPropertyValue.create(autoDetectedMimeType));
    }
    commitEditor.closeFile(path, checksum);
  }

  public void edit(final ISVNEditor commitEditor, final String path, final long baseRevision, final InputStream content, Map<String, String> attributes)  throws SVNException {
    commitEditor.openFile(path, baseRevision);
    commitEditor.applyTextDelta(path, null);
    SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
    // We don't keep the base around so we can't provide it here.
    String checksum = deltaGenerator.sendDelta(path, content, commitEditor, true);
    setProperties(commitEditor, path, attributes);
    commitEditor.closeFile(path, checksum);
  }

  private void setProperties(final ISVNEditor commitEditor, final String path, final Map<String, String> properties) throws SVNException{
    if (properties!=null) {
      for (Map.Entry<String, String> entry : properties.entrySet()) {
        commitEditor.changeFileProperty(path, entry.getKey(), SVNPropertyValue.create(entry.getValue()));
      }
    }
  }

  public void copy(final ISVNEditor commitEditor, final String fromPath, final long fromRevision, final String toPath) throws SVNException {
    String dir = SVNPathUtil.removeTail(toPath);
    commitEditor.openDir(dir, -1);
    commitEditor.addFile(toPath, fromPath, fromRevision);
    commitEditor.closeDir();
  }

  public void delete(final ISVNEditor commitEditor, final String path, final long baseRevision) throws SVNException {
    try {
      commitEditor.deleteEntry(path, baseRevision);
    }
    catch (SVNException ex) {
      // We just ignore this - older versions of SVNKit didn't explain at all.
      if (!SVNErrorCode.FS_NOT_FOUND.equals(ex.getErrorMessage().getErrorCode())) {
        throw ex;
      }
    }
  }

  public SVNLock getLock(final String path) throws NotFoundException, PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<SVNLock>() {
      public SVNLock perform(final BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
        return repository.getLock(path);
      }
    });
  }

  public void moveFile(final ISVNEditor commitEditor, final String fromPath, final long baseRevision, final String toPath) throws SVNException {
    String dir = SVNPathUtil.removeTail(toPath);
    commitEditor.openDir(dir, -1);
    commitEditor.deleteEntry(fromPath, baseRevision);
    commitEditor.addFile(toPath, fromPath, baseRevision);
    commitEditor.closeDir();
  }

  public void moveDir(final ISVNEditor commitEditor, final String fromPath, final long baseRevision, final String toPath) throws SVNException {
    String dir = SVNPathUtil.removeTail(toPath);
    commitEditor.openDir(dir, -1);
    commitEditor.deleteEntry(fromPath, baseRevision);
    commitEditor.addDir(toPath, fromPath, baseRevision);
    commitEditor.closeDir();
  }

  public List<SVNDirEntry> ls(final String path) throws PageStoreException {
    return execute(new SVNAction<List<SVNDirEntry>>() {
      public List<SVNDirEntry> perform(final BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException, IOException {
        List<SVNDirEntry> list = new ArrayList<SVNDirEntry>();
        repository.getDir(path, -1, null, list);
        return list;
      }
    });
  }

  public void dispose() {
    _repository.closeSession();
  }

}
