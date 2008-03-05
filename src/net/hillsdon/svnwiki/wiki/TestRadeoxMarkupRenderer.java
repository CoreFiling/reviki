package net.hillsdon.svnwiki.wiki;

import java.io.StringWriter;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.configuration.PageStoreConfiguration;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.SimplePageStore;

public class TestRadeoxMarkupRenderer extends TestCase {

  public void testIdentifiesWikiWords() throws Exception {
    SimplePageStore pageStore = new SimplePageStore();
    pageStore.set("ExistingPage", null, -1, "content", "");
    MarkupRenderer renderer = new RadeoxMarkupRenderer(new PageStoreConfiguration(pageStore), pageStore);
    StringWriter writer = new StringWriter();
    renderer.render(new PageReference("WikiPage"), "An ExistingPage and a PageWeStillNeedToWrite.", writer);
    assertEquals("An <a class='existing-page' href='ExistingPage'>ExistingPage</a> and a <a class='new-page' href='PageWeStillNeedToWrite'>PageWeStillNeedToWrite</a>.", writer.toString());
  }
  
}
