package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.StringWriter;

import junit.framework.TestCase;

public class TestRadeoxMarkupRenderer extends TestCase {

  private static final String EXAMPLE = "~~Hello~~";

  public void test() throws IOException {
    MarkupRenderer renderer = new RadeoxMarkupRenderer();
    StringWriter writer = new StringWriter();
    renderer.render(EXAMPLE, writer);
    assertEquals("<i class=\"italic\">Hello</i>", writer.toString());
  }
  
}
