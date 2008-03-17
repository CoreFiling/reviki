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
