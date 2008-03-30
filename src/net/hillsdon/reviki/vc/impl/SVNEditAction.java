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

import java.io.IOException;
import java.util.Map;

import net.hillsdon.reviki.vc.InterveningCommitException;
import net.hillsdon.reviki.vc.PageStoreException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;

/**
 * Does a commit of some sort.
 * 
 * @author mth
 */
public abstract class SVNEditAction implements SVNAction<Long> {
  
  private static final Log LOG = LogFactory.getLog(SVNEditAction.class);
  
  private final String _commitMessage;
  private final Map<String, String> _locks;

  public SVNEditAction(final String commitMessage) {
    this(commitMessage, null);
  }

  public SVNEditAction(final String commitMessage, final Map<String, String> locks) {
    _commitMessage = commitMessage;
    _locks = locks;
  }
  
  public Long perform(BasicSVNOperations operations, final SVNRepository repository) throws SVNException, PageStoreException, IOException {
    ISVNEditor commitEditor = null;
    try {
      commitEditor = repository.getCommitEditor(_commitMessage, _locks, false, null);
      commitEditor.openRoot(-1);
      driveCommitEditor(commitEditor, operations);
      commitEditor.closeDir();
      return commitEditor.closeEdit().getNewRevision();
    }
    catch (SVNException ex) {
      // We try clean-up as advised but re-throw the original error for handling.
      if (commitEditor != null) {
        try {
          commitEditor.abortEdit();
        }
        catch (SVNException abortError) {
          LOG.warn("Failed to abort after failed transaction.", abortError);
        }
      }
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
   * @param operations TODO
   * @throws SVNException On failure. 
   * @throws IOException On failure. 
   */
  protected abstract void driveCommitEditor(final ISVNEditor commitEditor, BasicSVNOperations operations) throws SVNException, IOException;
  
}
