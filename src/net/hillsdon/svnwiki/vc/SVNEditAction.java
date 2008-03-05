package net.hillsdon.svnwiki.vc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;

/**
 * Does a commit of some sort.
 * 
 * @author mth
 */
public abstract class SVNEditAction implements SVNAction<Long> {
  
  private final String _commitMessage;
  private final Map<String, String> _locks;

  public SVNEditAction(final String commitMessage) {
    this(commitMessage, null);
  }

  public SVNEditAction(final String commitMessage, final Map<String, String> locks) {
    _commitMessage = commitMessage;
    _locks = locks;
  }
  
  public Long perform(final SVNRepository repository) throws SVNException, PageStoreException, IOException {
    try {
      ISVNEditor commitEditor = repository.getCommitEditor(_commitMessage, _locks, false, null);
      driveCommitEditor(commitEditor);
      return commitEditor.closeEdit().getNewRevision();
    }
    catch (SVNException ex) {
      checkForInterveningCommit(ex);
      throw ex;
    }
  }

  private void checkForInterveningCommit(final SVNException ex) throws InterveningCommitException {
    if (SVNErrorCode.FS_CONFLICT.equals(ex.getErrorMessage().getErrorCode())) {
      // What to do!
      throw new InterveningCommitException(ex);
    }
  }

  /**
   * Actually do something.
   * @throws SVNException On failure. 
   * @throws IOException On failure. 
   */
  protected abstract void driveCommitEditor(final ISVNEditor commitEditor) throws SVNException, IOException;

  
  protected void createDir(final ISVNEditor commitEditor, final String dir) throws SVNException {
    commitEditor.openRoot(-1);
    commitEditor.addDir(dir, null, -1);
    commitEditor.closeDir();
    commitEditor.closeDir();
  }

  protected void copyFile(final ISVNEditor commitEditor, final String fromPath, final long fromRevision, final String toPath) throws SVNException {
    String dir = SVNPathUtil.removeTail(toPath);
    commitEditor.openRoot(-1);
    commitEditor.openDir(dir, -1);
    commitEditor.addFile(toPath, fromPath, fromRevision);
    commitEditor.closeDir();
    commitEditor.closeDir();
  }

  protected void moveFile(final ISVNEditor commitEditor, final String fromPath, final long baseRevision, final String toPath) throws SVNException {
    String dir = SVNPathUtil.removeTail(toPath);
    commitEditor.openRoot(-1);
    commitEditor.openDir(dir, -1);
    commitEditor.deleteEntry(fromPath, baseRevision);
    commitEditor.addFile(toPath, fromPath, baseRevision);
    commitEditor.closeDir();
    commitEditor.closeDir();
  }

  protected void createFile(final ISVNEditor commitEditor, final String filePath, final String mimeType, final InputStream data) throws SVNException {
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

  protected void deleteFile(final ISVNEditor commitEditor, final String filePath, final long baseRevision) throws SVNException {
    commitEditor.openRoot(-1);
    commitEditor.deleteEntry(filePath, baseRevision);
    commitEditor.closeDir();
  }
  
  protected void editFile(final ISVNEditor commitEditor, final String filePath, final long baseRevision, final InputStream newData) throws SVNException {
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
