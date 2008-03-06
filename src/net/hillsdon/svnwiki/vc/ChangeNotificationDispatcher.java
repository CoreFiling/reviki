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

import static net.hillsdon.fij.core.Functional.list;
import static net.hillsdon.fij.core.IterableUtils.reversed;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Tracks the SVN log and dispatches ChangeInfo lists to subscribers.
 * 
 * @author mth
 */
public class ChangeNotificationDispatcher {

  private static final Log LOG = LogFactory.getLog(ChangeNotificationDispatcher.class);
  
  private final BasicSVNOperations _operations;
  private final ChangeSubscriber[] _subscribers;

  private long _lastSynced = Long.MAX_VALUE;

  public ChangeNotificationDispatcher(final BasicSVNOperations operations, final ChangeSubscriber... subscribers) throws IOException {
    _operations = operations;
    _subscribers = subscribers;
    for (ChangeSubscriber subscriber : subscribers) {
      _lastSynced = Math.min(_lastSynced, subscriber.getHighestSyncedRevision());
    }
  }
 
  public synchronized void sync() throws PageStoreAuthenticationException, PageStoreException, IOException {
    long latest = _operations.getLatestRevision();
    try {
      if (latest > _lastSynced) {
        List<ChangeInfo> logs = _operations.log("", -1, false, true, _lastSynced, latest);
        if (!logs.isEmpty()) {
          notifyListeners(latest, list(reversed(logs)));
        }
      }
    }
    finally {
      _lastSynced = latest;
    }
  }
  
  private void notifyListeners(final long upto, final List<ChangeInfo> chronological) throws PageStoreException, IOException {
    for (ChangeSubscriber subscriber : _subscribers) {
      // Only tell folk what they need to know
      ListIterator<ChangeInfo> iter = chronological.listIterator();
      while (iter.hasNext() && iter.next().getRevision() <= subscriber.getHighestSyncedRevision()) {
        // Consume.
      }
      List<ChangeInfo> relevant = chronological.subList(iter.nextIndex(), chronological.size());
      LOG.debug("Notifying of " + subscriber.getClass().getSimpleName() + " of " + relevant.size() + " changes.");
      if (!relevant.isEmpty()) {
        subscriber.handleChanges(upto, relevant);
      }
    }
  }

}
