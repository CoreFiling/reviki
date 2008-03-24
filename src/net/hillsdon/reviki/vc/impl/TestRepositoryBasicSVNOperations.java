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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.util.Collections;

import junit.framework.TestCase;

import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.StoreKind;

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;

public class TestRepositoryBasicSVNOperations extends TestCase {

  public void testDetectMimeTypeResetsInputStream() throws Exception {
    BufferedInputStream stream = new BufferedInputStream(new ByteArrayInputStream(new byte[] {0}));
    String mimeType = RepositoryBasicSVNOperations.detectMimeType(stream);
    assertEquals(SVNFileUtil.BINARY_MIME_TYPE, mimeType);
    assertEquals(0, stream.read());
    assertEquals(-1, stream.read());
  }

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
    return RepositoryBasicSVNOperations.classifiedChange(new SVNLogEntry(Collections.singletonMap(path, new SVNLogEntryPath(path, 'M', null, -1)), -1, "", null, ""), "/wiki2", path);
  }
  
  
}
