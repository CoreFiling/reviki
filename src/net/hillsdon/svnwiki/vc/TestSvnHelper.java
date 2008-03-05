package net.hillsdon.svnwiki.vc;

import java.util.Collections;

import junit.framework.TestCase;

import org.tmatesoft.svn.core.SVNLogEntry;

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
  
  private ChangeInfo changeInfo(String path) {
    return SVNHelper.classifiedChange(new SVNLogEntry(Collections.singletonMap(path, null), -1, "", null, ""), "/wiki2", path);
  }
  
}
