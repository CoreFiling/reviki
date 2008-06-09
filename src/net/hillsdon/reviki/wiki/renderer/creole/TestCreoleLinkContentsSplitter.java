package net.hillsdon.reviki.wiki.renderer.creole;

import junit.framework.TestCase;

public class TestCreoleLinkContentsSplitter extends TestCase {

  private static final String URL = "http://www.example.com/foo";
  
  private CreoleLinkContentsSplitter _splitter;
  
  @Override
  protected void setUp() throws Exception {
    _splitter = new CreoleLinkContentsSplitter();
  }
  
  public void testOddInputs() {
    // Not much fussed what happens with weird inputs, we just can't explode.
    _splitter.split("");
    _splitter.split(" ");
    _splitter.split("  ");
    _splitter.split("|");
    _splitter.split("foo|");
    _splitter.split("| ");
    _splitter.split("a||b");
  }
  
  public void testExpectedInputs() {
    assertEquals(new LinkParts(URL, null, URL), _splitter.split(URL));
    assertEquals(new LinkParts("Some text", null, URL), _splitter.split(URL + "|" + "Some text"));
    assertEquals(new LinkParts("foo:Bar", "foo", "Bar"), _splitter.split("foo:Bar"));
    assertEquals(new LinkParts("text", "foo", "Bar"), _splitter.split("foo:Bar|text"));
    assertEquals(new LinkParts("Bar", null, "Bar"), _splitter.split("Bar"));
    assertEquals(new LinkParts("text", null, "Bar"), _splitter.split("Bar|text"));
  }
  
}
