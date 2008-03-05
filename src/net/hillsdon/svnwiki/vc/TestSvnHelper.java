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

import java.util.Collections;

import junit.framework.TestCase;

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

public class TestSvnHelper extends TestCase {

  public void testAttachmentDir() {
    ChangeInfo changeInfo = changeInfo("/wiki2/AttachmentsTest11969665985340-attachments");
    assertEquals(StoreKind.OTHER, changeInfo.getKind());
    assertEquals("AttachmentsTest11969665985340-attachments", changeInfo.getName());
  }

  public void testAttachment() {
    ChangeInfo changeInfo = changeInfo("/wiki2/AttachmentsTest11969665985340-attachments/file.txt");
    assertEquals(StoreKind.ATTACHMENT, changeInfo.getKind());
    assertEquals("file.txt", changeInfo.getName());
    assertEquals("AttachmentsTest11969665985340", changeInfo.getPage());
  }

  public void testPage() {
    ChangeInfo changeInfo = changeInfo("/wiki2/ThisIsAPage");
    assertEquals(StoreKind.PAGE, changeInfo.getKind());
    assertEquals("ThisIsAPage", changeInfo.getPage());
  }
  
  private ChangeInfo changeInfo(final String path) {
    return SVNHelper.classifiedChange(new SVNLogEntry(Collections.singletonMap(path, new SVNLogEntryPath(path, 'M', null, -1)), -1, "", null, ""), "/wiki2", path);
  }
  
}
