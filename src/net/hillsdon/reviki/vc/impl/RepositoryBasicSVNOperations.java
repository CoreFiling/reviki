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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.hillsdon.reviki.vc.AlreadyLockedException;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ChangeType;
import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStoreAuthenticationException;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.StoreKind;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;

import static java.util.Collections.singletonMap;

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

  public List<ChangeInfo> log(final String path, final long limit, final boolean pathOnly, final boolean stopOnCopy, final long startRevision, final long endRevision) throws PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<List<ChangeInfo>>() {
      public List<ChangeInfo> perform(BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
        final String rootPath = getRoot();
        final List<ChangeInfo> entries = new LinkedList<ChangeInfo>();
        // Start and end reversed to get newest changes first.
        _repository.log(new String[] {path}, endRevision, startRevision, true, stopOnCopy, limit, new ISVNLogEntryHandler() {
          public void handleLogEntry(final SVNLogEntry logEntry) throws SVNException {
            entries.addAll(logEntryToChangeInfos(rootPath, path, logEntry, pathOnly));
          }
        });
        return entries;
      }
    });
  }
  
  @SuppressWarnings("unchecked")
  private List<ChangeInfo> logEntryToChangeInfos(final String rootPath, final String loggedPath, final SVNLogEntry entry, final boolean pathOnly) {
    final String fullLoggedPath = SVNPathUtil.append(rootPath, loggedPath);
    final List<ChangeInfo> results = new LinkedList<ChangeInfo>();
    for (String changedPath : (Iterable<String>) entry.getChangedPaths().keySet()) {
      if (SVNPathUtil.isAncestor(rootPath, changedPath) && (!pathOnly || fullLoggedPath.equals(changedPath))) {
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

  public String getRoot() throws PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<String>() {
      public String perform(BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
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
  static ChangeInfo classifiedChange(final SVNLogEntry entry, final String rootPath, final String path) {
    StoreKind kind = StoreKind.OTHER;
    String name = path.length() > rootPath.length() ? path.substring(rootPath.length() + 1) : path;
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
    String copiedFrom = logForPath.getCopyPath();
    long copiedFromRevision = -1;
    if (SVNPathUtil.isAncestor(rootPath, copiedFrom)) {
      copiedFrom = SVNPathUtil.tail(copiedFrom);
      copiedFromRevision = logForPath.getCopyRevision();
    }
    else {
      copiedFrom = null;
    }
    return new ChangeInfo(page, name, user, date, entry.getRevision(), entry.getMessage(), kind, ChangeType.forCode(logForPath.getType()), copiedFrom, copiedFromRevision);
  }

  public void unlock(final PageReference ref, final String lockToken) throws PageStoreAuthenticationException, PageStoreException {
    execute(new SVNAction<Void>() {
      public Void perform(BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          repository.unlock(singletonMap(ref.getPath(), lockToken), false, new SVNLockHandlerAdapter());
        }
        catch (SVNException ex) {
          // FIXME: Presumably this code would be different for non-http repositories.
          if (SVNErrorCode.RA_DAV_REQUEST_FAILED.equals(ex.getErrorMessage().getErrorCode())) {
            // We get this when the page has already been unlocked.
            return null;
          }
        }
        return null;
      }
    });
  }
  
  public void lock(final PageReference ref, final long revision) throws AlreadyLockedException, PageStoreAuthenticationException, PageStoreException {
    execute(new SVNAction<PageInfo>() {
      public PageInfo perform(BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
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
  
  public void getFile(final String path, final long revision, final Map<String, String> properties, final OutputStream out) throws NotFoundException, PageStoreAuthenticationException, PageStoreException {
    execute(new SVNAction<Void>() {
      public Void perform(BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          repository.getFile(path, revision, properties, out);
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
  
  public long getLatestRevision() throws PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<Long>() {
      public Long perform(BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
        return repository.getLatestRevision();
      }
    });
  }

  public void createDirectory(ISVNEditor commitEditor, final String dir) throws SVNException {
    commitEditor.addDir(dir, null, -1);
  }

  public SVNNodeKind checkPath(final String path, final long revision) throws PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<SVNNodeKind>() {
      public SVNNodeKind perform(BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
        return repository.checkPath(path, revision);
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
  
  public void create(ISVNEditor commitEditor, final String path, final InputStream content) throws SVNException, IOException {
    final BufferedInputStream bis = new BufferedInputStream(content);
    final String autoDetectedMimeType = detectMimeType(bis);
    
    commitEditor.addFile(path, null, -1);
    commitEditor.applyTextDelta(path, null);
    SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
    String checksum = deltaGenerator.sendDelta(path, bis, commitEditor, true);
    
    final Map<String, String> autoprops = _autoPropertiesApplier.apply(path);
    for (Map.Entry<String, String> entry : autoprops.entrySet()) {
      commitEditor.changeFileProperty(path, entry.getKey(), entry.getValue());
    }
    if (!autoprops.containsKey(SVNProperty.MIME_TYPE) && autoDetectedMimeType != null) {
      commitEditor.changeFileProperty(path, SVNProperty.MIME_TYPE, autoDetectedMimeType);
    }
    
    commitEditor.closeFile(path, checksum);
  }

  public void edit(ISVNEditor commitEditor, final String path, final long baseRevision, final InputStream content) throws SVNException {
    commitEditor.openFile(path, baseRevision);
    commitEditor.applyTextDelta(path, null);
    SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
    // We don't keep the base around so we can't provide it here.
    String checksum = deltaGenerator.sendDelta(path, content, commitEditor, true);
    commitEditor.closeFile(path, checksum);
  }

  public void copy(final ISVNEditor commitEditor, final String fromPath, final long fromRevision, final String toPath) throws SVNException {
    String dir = SVNPathUtil.removeTail(toPath);
    commitEditor.openDir(dir, -1);
    commitEditor.addFile(toPath, fromPath, fromRevision);
    commitEditor.closeDir();
  }

  public void delete(ISVNEditor commitEditor, final String path, final long baseRevision) throws SVNException {
    commitEditor.deleteEntry(path, baseRevision);
  }

  public SVNLock getLock(final String path) throws NotFoundException, PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<SVNLock>() {
      public SVNLock perform(BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException {
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
  
}
