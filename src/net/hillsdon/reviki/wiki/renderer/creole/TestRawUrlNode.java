package net.hillsdon.reviki.wiki.renderer.creole;


import net.hillsdon.reviki.wiki.renderer.creole.CreoleRenderer.RawUrlNode;

/**
 * Test for {@link RawUrlNode}.
 * 
 * @author mth
 */
public class TestRawUrlNode extends NodeTestCase {
  
  public TestRawUrlNode() {
    super(new RawUrlNode());
  }

  public void test() {
    assertFinds("file:/some/where");
    assertFinds("http://www.example.com/where");
    assertFinds("mailto:someone@example.com");
  }
  
}
