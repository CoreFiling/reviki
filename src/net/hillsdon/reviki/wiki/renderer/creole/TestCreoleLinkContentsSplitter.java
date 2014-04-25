package net.hillsdon.reviki.wiki.renderer.creole;

import java.net.URI;

import junit.framework.TestCase;

public class TestCreoleLinkContentsSplitter extends TestCase {

  private static final String EXAMPLE_URI_STRING = "http://www.example.com/foo";
  private static final String EXAMPLE_URI_STRING_RESERVED = "http://www.example.com/a b \\c";
  private static final String EXAMPLE_URI_STRING_RESERVED_ESCAPED = "http://www.example.com/a%20b%20%5Cc";
  private static final URI EXAMPLE_URI = URI.create(EXAMPLE_URI_STRING);
  private static final URI EXAMPLE_UNESCAPED_URI = URI.create(EXAMPLE_URI_STRING_RESERVED_ESCAPED);
  
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
  
  public void testExpectedInputsURI() {
    assertEquals(new LinkParts(EXAMPLE_URI_STRING, EXAMPLE_URI), _splitter.split(EXAMPLE_URI_STRING));
    assertEquals(new LinkParts("Some text", EXAMPLE_URI), _splitter.split(EXAMPLE_URI_STRING + "|" + "Some text"));
  }

  public void testReservedCharsBug13502() {
    assertEquals(new LinkParts("Some text", EXAMPLE_UNESCAPED_URI), _splitter.split(EXAMPLE_URI_STRING_RESERVED + "|" + "Some text"));
  }
    
  public void testExpectedInputsWiki() {
    assertEquals(new LinkParts("foo:Bar", "foo", "Bar", null, null), _splitter.split("foo:Bar"));
    assertEquals(new LinkParts("text", "foo", "Bar", null, null), _splitter.split("foo:Bar|text"));
    assertEquals(new LinkParts("Bar", null, "Bar", null, null), _splitter.split("Bar"));
    assertEquals(new LinkParts("text", null, "Bar", null, null), _splitter.split("Bar|text"));
  }
  
}
