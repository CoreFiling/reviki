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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This initial implementation is an in-memory cache so will repopulate itself on every restart.
 * 
 * @author mth
 */
public class InMemoryDeletedRevisionTracker implements DeletedRevisionTracker {

  private final Map<String, ChangeInfo> _deletions = new ConcurrentHashMap<String, ChangeInfo>();
 
  public ChangeInfo getChangeThatDeleted(final BasicSVNOperations helper, final String path) throws PageStoreAuthenticationException, PageStoreException {
    return _deletions.get(path);
  }

  public void handleChanges(final long upto, final List<ChangeInfo> chronological) throws PageStoreException, IOException {
    for (ChangeInfo change : chronological) {
      final String page = change.getPage();
      if (page != null) {
        if (change.getChangeType() == ChangeType.DELETED) {
          _deletions.put(page, change);
        }
        else {
          _deletions.remove(page);
        }
      }
    }
  }

  public long getHighestSyncedRevision() throws IOException {
    return 0;
  }
  
}
