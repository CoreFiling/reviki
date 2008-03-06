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

import java.util.Collections;
import java.util.Date;

import junit.framework.TestCase;

public class TestAttachmentHistory extends TestCase {

  public void testGetPreviousVersions() {
    AttachmentHistory history = new AttachmentHistory();
    ChangeInfo firstCommit = new ChangeInfo("FooPage", "FooPage", "mth", new Date(), 1, "Added.", StoreKind.ATTACHMENT, ChangeType.MODIFIED);
    ChangeInfo secondCommit = new ChangeInfo("FooPage", "FooPage", "mth", new Date(), 1, "Latest edit.", StoreKind.ATTACHMENT, ChangeType.MODIFIED);
    history.getVersions().add(secondCommit);
    history.getVersions().add(firstCommit);
    assertEquals(secondCommit, history.getLatestVersion());
    assertEquals(Collections.singletonList(firstCommit), history.getPreviousVersions());
  }
  
}
