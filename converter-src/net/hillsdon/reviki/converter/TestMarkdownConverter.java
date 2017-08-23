package net.hillsdon.reviki.converter;

import java.io.IOException;

import junit.framework.TestCase;

public class TestMarkdownConverter extends TestCase {

  public void testRevikiLinks() throws Exception {
    assertEquals("FRC", convert("FRC"));
    assertEquals("[FB-1234](https://jira.int.corefiling.com/browse/FB-1234)", convert("FB-1234"));
    assertEquals("[~foo](https://jira.int.corefiling.com/secure/ViewProfile.jspa?name=foo)", convert("[~foo]"));
    assertEquals("[svndev:123456](https://sview-vc.int.corefiling.com/view/svn-dev.int.corefiling.com?view=revision&revision=123456)", convert("svndev:123456"));
  }

  private String convert(final String reviki) throws IOException {
    return new MarkdownConverter().convert(reviki);
  }

}
