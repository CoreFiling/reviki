package net.hillsdon.reviki.wiki.renderer.result;

import java.util.Arrays;

import junit.framework.TestCase;

public class TestTagResultNode extends TestCase {

  public void testAddsCssClassNoChildren() {
    TagResultNode hr = new TagResultNode("hr");
    assertEquals("<hr class='wiki-content' />", hr.toXHTML());
  }
  
  public void testAddsCssClassWithChildren() {
    TagResultNode hr = new TagResultNode("div", Arrays.<ResultNode>asList(new LiteralResultNode("Child")));
    assertEquals("<div class='wiki-content'>Child</div>", hr.toXHTML());
  }
  
}
