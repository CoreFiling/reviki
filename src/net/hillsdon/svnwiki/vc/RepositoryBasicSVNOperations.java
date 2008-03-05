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

import static java.util.Collections.singletonMap;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNDirEntry;
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

/**
 * The real impl, using an {@link SVNRepository}.
 * 
 * Currently some error handling may depend on it being a DAVRepository, this needs review.
 * 
 * @author mth
 */
public class RepositoryBasicSVNOperations implements BasicSVNOperations {

  private interface SVNAction<T> {
    T perform(SVNRepository repository) throws SVNException, PageStoreException, IOException;
  }
  
  private final SVNRepository _repository;

  public RepositoryBasicSVNOperations(final SVNRepository repository) {
    _repository = repository;
  }

  public List<ChangeInfo> log(final String path, final long limit, final boolean pathOnly, final long startRevision, final long endRevision) throws PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<List<ChangeInfo>>() {
      public List<ChangeInfo> perform(final SVNRepository repository) throws SVNException, PageStoreException {
        final String rootPath = getRoot();
        final List<ChangeInfo> entries = new LinkedList<ChangeInfo>();
        // Start and end reversed to get newest changes first.
        _repository.log(new String[] {path}, endRevision, startRevision, true, true, limit, new ISVNLogEntryHandler() {
          public void handleLogEntry(final SVNLogEntry logEntry) throws SVNException {
            entries.addAll(logEntryToChangeInfos(rootPath, path, pathOnly, logEntry));
          }
        });
        return entries;
      }
    });
  }
  
  public Collection<String> listFiles(final String dir) throws PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<Collection<String>>() {
      public Collection<String> perform(final SVNRepository repository) throws SVNException, PageStoreException {
        final Set<String> results = new LinkedHashSet<String>();
        Collection<SVNDirEntry> entries = new ArrayList<SVNDirEntry>();
        _repository.getDir(dir, -1, null, SVNDirEntry.DIRENT_KIND, entries);
        for (SVNDirEntry e : entries) {
          if (SVNNodeKind.FILE.equals(e.getKind())) {
            results.add(e.getName());
          }
        }
        return results;
      }
    });
  }

  @SuppressWarnings("unchecked")
  private List<ChangeInfo> logEntryToChangeInfos(final String rootPath, final String path, final boolean pathOnly, final SVNLogEntry entry) {
    List<ChangeInfo> results = new LinkedList<ChangeInfo>();
    for (String changedPath : (Iterable<String>) entry.getChangedPaths().keySet()) {
      if (!pathOnly || (changedPath.length() > rootPath.length() && changedPath.substring(rootPath.length() + 1).equals(path))) {
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
      public String perform(final SVNRepository repository) throws SVNException, PageStoreException {
        return _repository.getRepositoryPath("");
      }
    });
  }

  private <T> T execute(final SVNAction<T> action) throws PageStoreException, PageStoreAuthenticationException {
    try {
      return action.perform(_repository);
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
    return new ChangeInfo(page, name, user, date, entry.getRevision(), entry.getMessage(), kind, ChangeType.forCode(logForPath.getType()));
  }

  public void unlock(final PageReference ref, final String lockToken) throws PageStoreAuthenticationException, PageStoreException {
    execute(new SVNAction<Void>() {
      public Void perform(final SVNRepository repository) throws SVNException, PageStoreException {
        repository.unlock(singletonMap(ref.getPath(), lockToken), false, new SVNLockHandlerAdapter());
        return null;
      }
    });
  }
  
  public void lock(final PageReference ref, final long revision) throws AlreadyLockedException, PageStoreAuthenticationException, PageStoreException {
    execute(new SVNAction<PageInfo>() {
      public PageInfo perform(final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          Map<String, Long> pathsToRevisions = singletonMap(ref.getPath(), revision);
          repository.lock(pathsToRevisions, "Locked by svnwiki.", false, new SVNLockHandlerAdapter());
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
      public Void perform(final SVNRepository repository) throws SVNException, PageStoreException {
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
      public Long perform(final SVNRepository repository) throws SVNException, PageStoreException {
        return repository.getLatestRevision();
      }
    });
  }

  public void ensureDir(final String dir, final String commitMessage) throws PageStoreException {
    execute(new SVNAction<Void>() {
      public Void perform(final SVNRepository repository) throws SVNException, PageStoreException {
        if (repository.checkPath(dir, -1) == SVNNodeKind.NONE) {
          ISVNEditor commitEditor = repository.getCommitEditor(commitMessage, null);
          try {
            createDir(commitEditor, dir);
          }
          finally {
            commitEditor.closeEdit();
          }
        }
        return null;
      }
    });
  }

  private void checkForInterveningCommit(final SVNException ex) throws InterveningCommitException {
    if (SVNErrorCode.FS_CONFLICT.equals(ex.getErrorMessage().getErrorCode())) {
      // What to do!
      throw new InterveningCommitException(ex);
    }
  }
  
  public SVNNodeKind checkPath(final String path, final long revision) throws PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<SVNNodeKind>() {
      public SVNNodeKind perform(final SVNRepository repository) throws SVNException, PageStoreException {
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
  
  public long create(final String path, final String commitMessage, final InputStream content) throws InterveningCommitException, PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<Long>() {
      public Long perform(final SVNRepository repository) throws SVNException, PageStoreException, IOException {
        try {
          BufferedInputStream bis = new BufferedInputStream(content);
          String mimeType = detectMimeType(bis);
          ISVNEditor commitEditor = repository.getCommitEditor(commitMessage, null, false, null);
          createFile(commitEditor, path, mimeType, bis);
          return commitEditor.closeEdit().getNewRevision();
        }
        catch (SVNException ex) {
          checkForInterveningCommit(ex);
          throw ex;
        }
      }
    });
  }

  public long edit(final String path, final long baseRevision, final String commitMessage, final String lockToken, final InputStream content) throws PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<Long>() {
      public Long perform(final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          ISVNEditor commitEditor = repository.getCommitEditor(commitMessage, createLocksMap(path, lockToken), false, null);
          editFile(commitEditor, path, baseRevision, content);
          return commitEditor.closeEdit().getNewRevision();
        }
        catch (SVNException ex) {
          checkForInterveningCommit(ex);
          throw ex;
        }
      }
    });
  }

  public void delete(final String path, final long baseRevision, final String commitMessage, final String lockToken) throws InterveningCommitException, PageStoreAuthenticationException, PageStoreException {
    execute(new SVNAction<Void>() {
      public Void perform(final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          ISVNEditor commitEditor = repository.getCommitEditor(commitMessage, createLocksMap(path, lockToken), false, null);
          deleteFile(commitEditor, path, baseRevision);
          commitEditor.closeEdit();
        }
        catch (SVNException ex) {
          checkForInterveningCommit(ex);
          throw ex;
        }
        return null;
      }

    });
  }

  private Map<String, String> createLocksMap(final String path, final String lockToken) {
    return lockToken == null ? Collections.<String, String> emptyMap() : Collections.<String, String> singletonMap(path, lockToken);
  }

  public SVNLock getLock(final String path) throws NotFoundException, PageStoreAuthenticationException, PageStoreException {
    return execute(new SVNAction<SVNLock>() {
      public SVNLock perform(final SVNRepository repository) throws SVNException, PageStoreException {
        return repository.getLock(path);
      }
    });
  }

  private void createDir(final ISVNEditor commitEditor, final String dir) throws SVNException {
    commitEditor.openRoot(-1);
    commitEditor.addDir(dir, null, -1);
    commitEditor.closeDir();
    commitEditor.closeDir();
  }

  private void createFile(final ISVNEditor commitEditor, final String filePath, final String mimeType, final InputStream data) throws SVNException {
    String dir = SVNPathUtil.removeTail(filePath);
    commitEditor.openRoot(-1);
    commitEditor.openDir(dir, -1);
    commitEditor.addFile(filePath, null, -1);
    commitEditor.applyTextDelta(filePath, null);
    SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
    String checksum = deltaGenerator.sendDelta(filePath, data, commitEditor, true);
    if (mimeType != null) {
      commitEditor.changeFileProperty(filePath, SVNProperty.MIME_TYPE, mimeType);
    }
    commitEditor.closeFile(filePath, checksum);
    commitEditor.closeDir();
    commitEditor.closeDir();
  }

  private void deleteFile(final ISVNEditor commitEditor, final String filePath, final long baseRevision) throws SVNException {
    commitEditor.openRoot(-1);
    commitEditor.deleteEntry(filePath, baseRevision);
    commitEditor.closeDir();
  }
  
  private void editFile(final ISVNEditor commitEditor, final String filePath, final long baseRevision, final InputStream newData) throws SVNException {
    commitEditor.openRoot(-1);
    commitEditor.openFile(filePath, baseRevision);
    commitEditor.applyTextDelta(filePath, null);
    SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
    // We don't keep the base around so we can't provide it here.
    String checksum = deltaGenerator.sendDelta(filePath, newData, commitEditor, true);
    commitEditor.closeFile(filePath, checksum);
    commitEditor.closeDir();
  }
  
}
