package net.hillsdon.svnwiki.vc;

import java.io.IOException;


/**
 * Something that needs syncing with external commits before its use
 * will represent the latest repository contents.
 * 
 * @author mth
 */
public interface NeedsSync {

  void syncWithExternalCommits() throws PageStoreException, IOException;

}
