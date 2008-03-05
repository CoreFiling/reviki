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

import static net.hillsdon.fij.core.IterableUtils.reversed;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This initial implementation is an in-memory cache so will repopulate
 * itself on every restart.
 * 
 * @author mth
 */
public class InMemoryDeletedRevisionTracker implements DeletedRevisionTracker {

  private final Map<String, ChangeInfo> _deletions = new ConcurrentHashMap<String, ChangeInfo>();
  private long _lastTracked;
 
  private void catchUp(final BasicSVNOperations helper) throws PageStoreAuthenticationException, PageStoreException {
    long latest = helper.getLatestRevision();
    if (latest > _lastTracked) {
      for (ChangeInfo change : reversed(helper.log("", -1, false, _lastTracked, latest))) {
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
    _lastTracked = latest;
  }
  
  public ChangeInfo getChangeThatDeleted(final BasicSVNOperations helper, final String path) throws PageStoreAuthenticationException, PageStoreException {
    catchUp(helper);
    return _deletions.get(path);
  }
  
}
