package net.hillsdon.reviki.wiki.renderer.creole;

import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.UnknownWikiException;
import net.hillsdon.reviki.wiki.renderer.result.CompositeResultNode;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;
import junit.framework.TestCase;

public class TestAbstractRegexNode extends TestCase {

  private static final String WIKI_CONTENT = "sometext some:wiki some::moretext";

  private static final String RE_LINK_STYLE_1 = "\\p{Alnum}+:\\p{Alnum}+";

  private static final String RE_LINK_STYLE_2 = "\\p{Alnum}+::\\p{Alnum}+";

  public void testRendering() {
    RenderNode node1 = new AbstractRegexNodeStub(RE_LINK_STYLE_1, "processed1", false);
    RenderNode node2 = new AbstractRegexNodeStub(RE_LINK_STYLE_2, "processed2", false);
    node1.addChildren(node1);
    node1.addChildren(node2);
    List<ResultNode> result = node1.render(new PageInfoImpl("SomePage"), WIKI_CONTENT, null, null);
    CompositeResultNode composite = new CompositeResultNode(result);
    assertEquals("sometext processed1 processed2", composite.toXHTML());
  }

  public void testRenderingWithException() {
    // test that throwing an exception in node.handle() doesn't affect rendering of the rest of
    // the text
    RenderNode node1 = new AbstractRegexNodeStub(RE_LINK_STYLE_1, "processed1", true);
    RenderNode node2 = new AbstractRegexNodeStub(RE_LINK_STYLE_2, "processed2", false);
    node1.addChildren(node1);
    node1.addChildren(node2);
    List<ResultNode> result = node1.render(new PageInfoImpl("SomePage"), WIKI_CONTENT, null, null);
    CompositeResultNode composite = new CompositeResultNode(result);
    assertEquals("sometext some:wiki processed2", composite.toXHTML());
  }

  private static class AbstractRegexNodeStub extends AbstractRegexNode {
    private final String _replacementText;

    private final boolean _shouldThrow;

    private AbstractRegexNodeStub(String matchRe, String replacementText, boolean shouldThrow) {
      super(matchRe);
      _replacementText = replacementText;
      _shouldThrow = shouldThrow;
    }

    public ResultNode handle(PageInfo page, Matcher matcher, RenderNode parent, URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException {
      if (_shouldThrow) {
        throw new UnknownWikiException();
      }
      return new HtmlEscapeResultNode(_replacementText);
    }
  }
}
