package net.hillsdon.reviki.vc;

import java.io.IOException;
import java.util.List;


/**
 * Something that needs syncing with external commits before its use
 * will represent the latest repository contents.
 * 
 * Implementers will get to see exactly the changes they care about
 * (revision-wise).
 * 
 * @author mth
 */
public interface ChangeSubscriber {

  /**
   * @return The highest synchronized revision before subscribing.
   * @throws IOException If we fail to get hold of the value.
   */
  long getHighestSyncedRevision() throws IOException;
  
  /**
   * @param upto The revision we've tracked up to.
   * @param chronological The changes up to that revision.
   * 
   * @throws PageStoreException On failure to handle.
   * @throws IOException On failure to handle.
   */
  void handleChanges(long upto, List<ChangeInfo> chronological) throws PageStoreException, IOException;

}
