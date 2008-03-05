package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.StringWriter;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.vc.SimplePageStore;

public class TestRadeoxMarkupRenderer extends TestCase {

  public void testIdentifiesWikiWords() throws IOException {
    SimplePageStore pageStore = new SimplePageStore();
    MarkupRenderer renderer = new RadeoxMarkupRenderer(pageStore);
    StringWriter writer = new StringWriter();
    renderer.render("A WikiWord is in this sentence.", writer);
    assertEquals("A <a href='WikiWord'>WikiWord</a> is in this sentence.", writer.toString());
  }
  
}
