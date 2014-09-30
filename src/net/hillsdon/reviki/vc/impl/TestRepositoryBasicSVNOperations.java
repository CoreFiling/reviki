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
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

public class TestRepositoryBasicSVNOperations extends TestCase {

  public void testSVNPathUtilIsWeird() throws Exception {
    assertEquals(/* What no slash? */ "Foo", SVNPathUtil.append("/", "Foo"));
  }
  
  public void testDetectMimeTypeResetsInputStream() throws Exception {
    BufferedInputStream stream = new BufferedInputStream(new ByteArrayInputStream(new byte[] {0}));
    String mimeType = RepositoryBasicSVNOperations.detectMimeType(stream);
    assertEquals(SVNFileUtil.BINARY_MIME_TYPE, mimeType);
    assertEquals(0, stream.read());
    assertEquals(-1, stream.read());
  }

  public void testAttachmentDir() throws Exception {
    ChangeInfo changeInfo = changeInfo("/wiki2", "/wiki2/AttachmentsTest11969665985340-attachments");
    assertEquals(StoreKind.OTHER, changeInfo.getKind());
    assertEquals("AttachmentsTest11969665985340-attachments", changeInfo.getName());
  }

  public void testAttachment() throws Exception {
    ChangeInfo changeInfo = changeInfo("/wiki2", "/wiki2/AttachmentsTest11969665985340-attachments/file.txt");
    assertEquals(StoreKind.ATTACHMENT, changeInfo.getKind());
    assertEquals("file.txt", changeInfo.getName());
    assertEquals("AttachmentsTest11969665985340", changeInfo.getPage());
  }

  public void testAttachmentSlashAsRootPath() throws Exception {
    ChangeInfo changeInfo = changeInfo("/", "/AttachmentsTest11969665985340-attachments/file.txt");
    assertEquals(StoreKind.ATTACHMENT, changeInfo.getKind());
    assertEquals("file.txt", changeInfo.getName());
    assertEquals("AttachmentsTest11969665985340", changeInfo.getPage());
  }

  public void testPage() throws Exception {
    ChangeInfo changeInfo = changeInfo("/wiki2", "/wiki2/ThisIsAPage");
    assertEquals("ThisIsAPage", changeInfo.getPage());
    assertEquals(StoreKind.PAGE, changeInfo.getKind());
    assertNull(changeInfo.getRenamedTo());
  }

  public void testPageSlashAsRootPath() throws Exception {
    ChangeInfo changeInfo = changeInfo("/", "/ThisIsAPage");
    assertEquals(StoreKind.PAGE, changeInfo.getKind());
    assertEquals("ThisIsAPage", changeInfo.getPage());
  }

  private SVNRepository getTestRepo() throws Exception {
    DAVRepositoryFactory.setup();
    return SVNRepositoryFactory.create(SVNURL.parseURIEncoded("http://svn.example.com/svn"));
  }

  private ChangeInfo changeInfo(String rootPath, final String path) throws Exception {
    return RepositoryBasicSVNOperations.classifiedChange(getTestRepo(), new SVNLogEntry(Collections.singletonMap(path, new SVNLogEntryPath(path, 'M', null, -1)), -1, "", null, ""), rootPath, path);
  }

  public void testPageRenameInWiki() throws Exception {
    // NB. Testing renaming into another wiki isn't possible here as a call to 'svn info' will occur when trying to work out the repository root.
    String rootPath = "/wiki2";
    String path = rootPath + "/ThisIsAPage";
    ChangeInfo changeInfo = RepositoryBasicSVNOperations.classifiedChange(getTestRepo(), new SVNLogEntry(Collections.singletonMap(path, new SVNLogEntryPath(rootPath + "/NewPage", 'A', path, 3)), 11, "", null, ""), rootPath, path);
    assertNotNull(changeInfo.getRenamedTo());
    assertEquals("NewPage", changeInfo.getRenamedTo().getPageName());
  }

  public void testPathMunging() throws Exception {
    assertEquals("", RepositoryBasicSVNOperations.fixFullLoggedPath(""));
    assertEquals("", RepositoryBasicSVNOperations.fixFullLoggedPath("/"));
    assertEquals("/test", RepositoryBasicSVNOperations.fixFullLoggedPath("/test/"));
    assertEquals("/test", RepositoryBasicSVNOperations.fixFullLoggedPath("/test"));
    assertEquals("/test", RepositoryBasicSVNOperations.fixFullLoggedPath("test/"));
    assertEquals("/test", RepositoryBasicSVNOperations.fixFullLoggedPath("test"));
  }
  
}
