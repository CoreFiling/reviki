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
import java.util.List;

import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ChangeNotificationDispatcher;
import net.hillsdon.reviki.vc.ChangeSubscriber;
import net.hillsdon.reviki.vc.PageStoreAuthenticationException;
import net.hillsdon.reviki.vc.PageStoreException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

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
      List<ChangeInfo> logs = _operations.log("", -1, LogEntryFilter.DESCENDANTS, true, _lastSynced + 1, latest);
      if (!logs.isEmpty()) {
        notifyListeners(latest, ImmutableList.copyOf(Iterables.reverse(logs)));
      }
    }
    // Some subscribers might not have been synchronised, e.g. searcher if the index is only being built
    _lastSynced = _subscribers[0].getHighestSyncedRevision();
    for (int i = 1; i < _subscribers.length; i++) {
      _lastSynced = Math.min(_lastSynced, _subscribers[i].getHighestSyncedRevision());
    }
  }

  private void notifyListeners(final long upto, final List<ChangeInfo> chronological) throws PageStoreException, IOException {
    for (ChangeSubscriber subscriber : _subscribers) {
      final long subscriberHighestSynced = subscriber.getHighestSyncedRevision();
      List<ChangeInfo> relevant = ImmutableList.copyOf(Iterables.filter(chronological, new Predicate<ChangeInfo>() {
        public boolean apply(final ChangeInfo in) {
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
