package net.hillsdon.svnwiki.wiki;

import java.util.Date;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.vc.PageInfo;

public class TestRenderedPage extends TestCase {

  private RenderedPageFactory _factory;

  @Override
  protected void setUp() throws Exception {
    _factory = new RenderedPageFactory(MarkupRenderer.AS_IS);
  }
  
  private RenderedPage create(String content) throws Exception {
    return _factory.create(new PageInfo("FrontPage", content, -1, -1, "mth", new Date(), null, null));
  }

  public void testGetPage() throws Exception {
    RenderedPage rendered = create("");
    rendered.getPage().getPath().equals("FrontPage");
  }
  
  public void testAsText() throws Exception {
    RenderedPage rendered = create("<p>Hello</p><p><strong>Dude</strong></p>");
    // Not ideal but it ought to suffice.
    assertEquals("Hello\n Dude", rendered.asText());
  }
  
}
