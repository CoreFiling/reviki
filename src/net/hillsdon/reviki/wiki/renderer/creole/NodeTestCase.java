package net.hillsdon.reviki.wiki.renderer.creole;

import java.util.regex.Matcher;

import junit.framework.TestCase;

public abstract class NodeTestCase extends TestCase {

  private final RenderNode _node;

  public NodeTestCase(RenderNode node) {
    _node = node;
  }

  public void assertFinds(final String text) {
    Matcher match = _node.find(text);
    assertNotNull("No match", match);
    assertEquals("Partial match only", text, match.group());
  }

}