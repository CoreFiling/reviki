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
      final ISVNEditor commitEditor = repository.getCommitEditor(_commitMessage, _locks, false, null);
      commitEditor.openRoot(-1);
      driveCommitEditor(commitEditor);
      commitEditor.closeDir();
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
    commitEditor.addDir(dir, null, -1);
    commitEditor.closeDir();
  }

  protected void copyFile(final ISVNEditor commitEditor, final String fromPath, final long fromRevision, final String toPath) throws SVNException {
    String dir = SVNPathUtil.removeTail(toPath);
    commitEditor.openDir(dir, -1);
    commitEditor.addFile(toPath, fromPath, fromRevision);
    commitEditor.closeDir();
  }

  protected void moveFile(final ISVNEditor commitEditor, final String fromPath, final long baseRevision, final String toPath) throws SVNException {
    String dir = SVNPathUtil.removeTail(toPath);
    commitEditor.openDir(dir, -1);
    commitEditor.deleteEntry(fromPath, baseRevision);
    commitEditor.addFile(toPath, fromPath, baseRevision);
    commitEditor.closeDir();
  }

  protected void moveDir(final ISVNEditor commitEditor, final String fromPath, final long baseRevision, final String toPath) throws SVNException {
    String dir = SVNPathUtil.removeTail(toPath);
    commitEditor.openDir(dir, -1);
    commitEditor.deleteEntry(fromPath, baseRevision);
    commitEditor.addDir(toPath, fromPath, baseRevision);
    commitEditor.closeDir();
  }
  
  protected void createFile(final ISVNEditor commitEditor, final String filePath, final String mimeType, final InputStream data) throws SVNException {
    String dir = SVNPathUtil.removeTail(filePath);
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
