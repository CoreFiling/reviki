package net.hillsdon.svnwiki.vc;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;

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
import java.util.List;
import java.util.Map;

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

  /**
   * We don't actually do 'recent' in terms of date as that's less useful.
   */
  private static final int RECENT_CHANGES_HISTORY_SIZE = 15;

  /**
   * The assumed encoding of files from the repository.
   */
  private static final String UTF8 = "UTF8";

  private final SVNHelper _helper;

  /**
   * Note the repository URL can be deep, it need not refer to the root of the
   * repository itself. We put pages in the root of what we're given.
   */
  public SVNPageStore(final SVNRepository repository) {
    _helper = new SVNHelper(repository);
  }

  public List<ChangeInfo> recentChanges() throws PageStoreException {
    return _helper.execute(new SVNAction<List<ChangeInfo>>() {
      public List<ChangeInfo> perform(final SVNRepository repository) throws SVNException {
        return _helper.log("", RECENT_CHANGES_HISTORY_SIZE);
      }
    });
  }

  public List<ChangeInfo> history(final String path) throws PageStoreException {
    return _helper.execute(new SVNAction<List<ChangeInfo>>() {
      public List<ChangeInfo> perform(final SVNRepository repository) throws SVNException {
        return _helper.log(path, -1);
      }
    });
  }


  public Collection<String> list() throws PageStoreException {
    return _helper.execute(new SVNAction<Collection<String>>() {
      public Collection<String> perform(final SVNRepository repository) throws SVNException {
        return _helper.listFiles("");
      }
    });
  }


  public PageInfo get(final String path, final long revision) throws PageStoreException {
    return _helper.execute(new SVNAction<PageInfo>() {
      public PageInfo perform(final SVNRepository repository) throws SVNException, PageStoreException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HashMap<String, String> properties = new HashMap<String, String>();

        SVNNodeKind kind = repository.checkPath(path, revision);
        if (SVNNodeKind.FILE.equals(kind)) {
          repository.getFile(path, revision, properties, baos);
          long actualRevision = SVNProperty.longValue(properties.get(SVNProperty.REVISION));
          long lastChangedRevision = SVNProperty.longValue(properties.get(SVNProperty.COMMITTED_REVISION));
          Date lastChangedDate = SVNTimeUtil.parseDate(properties.get(SVNProperty.COMMITTED_DATE));
          String lastChangedAuthor = properties.get(SVNProperty.LAST_AUTHOR);
          SVNLock lock = repository.getLock(path);
          String lockOwner = lock == null ? null : lock.getOwner();
          String lockToken = lock == null ? null : lock.getID();
          return new PageInfo(path, toUTF8(baos.toByteArray()), actualRevision, lastChangedRevision, lastChangedAuthor, lastChangedDate, lockOwner, lockToken);
        }
        else if (SVNNodeKind.NONE.equals(kind)) {
          // Distinguishing between 'uncommitted' and 'deleted' would be useful
          // for history.
          return new PageInfo(path, "", PageInfo.UNCOMMITTED, PageInfo.UNCOMMITTED, null, null, null, null);
        }
        else {
          throw new PageStoreException(format("Unexpected node kind '%s' at '%s'", kind, path));
        }
      }
    });
  }

  public PageInfo tryToLock(final String path) throws PageStoreException {
    final PageInfo page = get(path, -1);
    if (page.isNew()) {
      return page;
    }

    return _helper.execute(new SVNAction<PageInfo>() {
      public PageInfo perform(final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          long revision = page.getRevision();
          Map<String, Long> pathsToRevisions = singletonMap(path, revision);
          repository.lock(pathsToRevisions, "Locked by svnwiki.", false, new SVNLockHandlerAdapter());
          return get(path, revision);
        }
        catch (SVNException ex) {
          if (SVNErrorCode.FS_PATH_ALREADY_LOCKED.equals(ex.getErrorMessage().getErrorCode())) {
            // The caller will check getLockedBy().
            return get(path, -1);
          }
          throw ex;
        }
      }
    });
  }

  public void unlock(final String path, final String lockToken) throws PageStoreException {
    _helper.execute(new SVNAction<Void>() {
      public Void perform(final SVNRepository repository) throws SVNException, PageStoreException {
        repository.unlock(singletonMap(path, lockToken), false, new SVNLockHandlerAdapter());
        return null;
      }
    });
  }

  public void set(final String path, final String lockToken, final long baseRevision, final String content, final String commitMessage)
      throws PageStoreException {
    set(path, lockToken, baseRevision, new ByteArrayInputStream(fromUTF8(content)), commitMessage);
  }

  private void set(final String path, final String lockToken, final long baseRevision, final InputStream content, final String commitMessage) throws PageStoreException {
    _helper.execute(new SVNAction<Void>() {
      public Void perform(final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          Map<String, String> locks = lockToken == null ? Collections.<String, String> emptyMap() : Collections.<String, String> singletonMap(path, lockToken);
          ISVNEditor commitEditor = repository.getCommitEditor(commitMessage, locks, false, null);
          if (baseRevision == PageInfo.UNCOMMITTED) {
            _helper.createFile(commitEditor, path, content);
          }
          else {
            _helper.editFile(commitEditor, path, baseRevision, content);
          }
          commitEditor.closeEdit();
        }
        catch (SVNException ex) {
          if (SVNErrorCode.FS_CONFLICT.equals(ex.getErrorMessage().getErrorCode())) {
            // What to do!
            throw new InterveningCommitException(ex);
          }
          throw ex;
        }
        return null;
      }
    });
  }

  public void attach(final String page, final String storeName, final InputStream in) throws PageStoreException {
    String dir = attachmentPath(page);
    ensureDir(dir);
    set(dir + "/" + storeName, null, PageInfo.UNCOMMITTED, in, "");
  }

  public Collection<String> attachments(final String page) throws PageStoreException {
    return _helper.execute(new SVNAction<Collection<String>>() {
      public Collection<String> perform(final SVNRepository repository) throws SVNException {
        String attachmentPath = attachmentPath(page);
        if (repository.checkPath(attachmentPath, -1).equals(SVNNodeKind.DIR)) {
          return _helper.listFiles(attachmentPath);
        }
        return Collections.emptySet();
      }
    });
  }

  private String attachmentPath(final String page) {
    return page + "-attachments";
  }

  private void ensureDir(final String dir) throws PageStoreException {
    _helper.execute(new SVNAction<Void>() {
      public Void perform(final SVNRepository repository) throws SVNException, PageStoreException {
        if (repository.checkPath(dir, -1) == SVNNodeKind.NONE) {
          ISVNEditor commitEditor = repository.getCommitEditor("[svnwiki commit] Add attachments dir.", null);
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

  public void attachment(final String page, final String attachment, final ContentTypedSink sink) throws NotFoundException, PageStoreException {
    final String path = SVNPathUtil.append(attachmentPath(page), attachment);
    final OutputStream out = new OutputStream() {
      boolean first = true;
      public void write(int b) throws IOException {
        if (first) {
          sink.setContentType("application/octet-stream"); 
          sink.setFileName(attachment);
          first = false;
        }
        sink.stream().write(b);
      }
    };
    
    _helper.execute(new SVNAction<Void>() {
      public Void perform(final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          repository.getFile(path, -1, null, out);
        }
        catch (SVNException ex) {
          if (SVNErrorCode.RA_DAV_REQUEST_FAILED.equals(ex.getErrorMessage().getErrorCode())) {
            throw new NotFoundException(ex);
          }
          throw ex;
        }
        return null;
      }
    });
  }

}
