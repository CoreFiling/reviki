package net.hillsdon.svnwiki.wiki;

import junit.framework.TestCase;

public class TestRenderedPage extends TestCase {

  private RenderedPageFactory _factory;

  @Override
  protected void setUp() throws Exception {
    _factory = new RenderedPageFactory(MarkupRenderer.AS_IS);
  }
  
  private RenderedPage create(String content) throws Exception {
    return _factory.create("FrontPage", content);
  }

  public void testGetPage() throws Exception {
    RenderedPage rendered = create("");
    rendered.getPage().equals("FrontPage");
  }
  
  public void testAsText() throws Exception {
    RenderedPage rendered = create("<p>Hello</p><p><strong>Dude</strong></p>");
    // Not ideal but it ought to suffice.
    assertEquals("Hello\n Dude", rendered.asText());
  }
  
}
