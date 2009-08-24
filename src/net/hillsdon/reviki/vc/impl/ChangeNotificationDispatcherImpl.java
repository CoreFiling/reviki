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

import static net.hillsdon.fij.core.Functional.filter;
import static net.hillsdon.fij.core.Functional.list;
import static net.hillsdon.fij.core.IterableUtils.reversed;

import java.io.IOException;
import java.util.List;

import net.hillsdon.fij.core.Predicate;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ChangeNotificationDispatcher;
import net.hillsdon.reviki.vc.ChangeSubscriber;
import net.hillsdon.reviki.vc.PageStoreAuthenticationException;
import net.hillsdon.reviki.vc.PageStoreException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Tracks the SVN log and dispatches ChangeInfo lists to subscribers.
 * 
 * @author mth
 */
public class ChangeNotificationDispatcherImpl implements ChangeNotificationDispatcher {

  private static final Log LOG = LogFactory.getLog(ChangeNotificationDispatcherImpl.class);
  
  private final BasicSVNOperations _operations;
  private final ChangeSubscriber[] _subscribers;

  private long _lastSynced = Long.MAX_VALUE;

  public ChangeNotificationDispatcherImpl(final BasicSVNOperations operations, final ChangeSubscriber... subscribers) throws IOException {
    _operations = operations;
    _subscribers = subscribers;
    for (ChangeSubscriber subscriber : subscribers) {
      _lastSynced = Math.min(_lastSynced, subscriber.getHighestSyncedRevision());
    }
  }
 
  public synchronized void sync() throws PageStoreAuthenticationException, PageStoreException, IOException {
    long latest = _operations.getLatestRevision();
    if (latest > _lastSynced) {
      List<ChangeInfo> logs = _operations.log("", -1, false, true, _lastSynced + 1, latest);
      if (!logs.isEmpty()) {
        notifyListeners(latest, list(reversed(logs)));
      }
    }
    _lastSynced = latest;
  }
  
  private void notifyListeners(final long upto, final List<ChangeInfo> chronological) throws PageStoreException, IOException {
    for (ChangeSubscriber subscriber : _subscribers) {
      final long subscriberHighestSynced = subscriber.getHighestSyncedRevision();
      List<ChangeInfo> relevant = list(filter(chronological, new Predicate<ChangeInfo>() {
        public Boolean transform(final ChangeInfo in) {
          return in.getRevision() > subscriberHighestSynced;
        }
      }));
      if (!relevant.isEmpty()) {
        long start = System.currentTimeMillis();
        subscriber.handleChanges(upto, relevant);
        LOG.debug("Notified " + subscriber.getClass().getSimpleName() + " of " + relevant.size() + " changes in " + (System.currentTimeMillis() - start));
      }
    }
  }

  long getLastSynced() {
    return _lastSynced;
  }
  
}
