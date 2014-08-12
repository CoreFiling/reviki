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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.vc.SimpleAttachmentHistory;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.vc.PageStoreException;

/**
 * Simple PageStore implementation used for tests.
 *
 * @author mth
 */
public class DummyPageStore implements SimplePageStore {
  public Set<PageReference> list() throws PageStoreException {
    return Collections.emptySet();
  }

  public boolean exists(PageReference page) throws PageStoreException {
    return false;
  }

  public Collection<? extends SimpleAttachmentHistory> listAttachments(PageReference ref) throws PageStoreException {
    return Collections.emptyList();
  }

  public byte[] attachmentBytes(PageReference ref, String attachment, long revision) throws PageStoreException, NotFoundException {
    throw new NotFoundException();
  }
}