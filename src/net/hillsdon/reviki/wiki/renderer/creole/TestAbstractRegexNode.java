package net.hillsdon.reviki.wiki.renderer.creole;

import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.UnknownWikiException;
import net.hillsdon.reviki.wiki.renderer.result.CompositeResultNode;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;
import junit.framework.TestCase;

public class TestAbstractRegexNode extends TestCase {

  public void testRendering() {
    RenderNode node1 = new AbstractRegexNode("testNode") {
      public ResultNode handle(PageInfo page, Matcher matcher, RenderNode parent, URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException {
        return new HtmlEscapeResultNode("processed1");
      }
      @Override
      public Matcher find(String text) {
        Pattern pattern = Pattern.compile("\\p{Alnum}+:\\p{Alnum}+");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher : null;
      }
    };

    RenderNode node2 = new AbstractRegexNode("testNode2") {
      public ResultNode handle(PageInfo page, Matcher matcher, RenderNode parent, URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException {
        return new HtmlEscapeResultNode("processed2");
      }
      @Override
      public Matcher find(String text) {
        Pattern pattern = Pattern.compile("\\p{Alnum}+::\\p{Alnum}+");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher : null;
      }
    };
    node1.addChildren(node1);
    node1.addChildren(node2);
    List<ResultNode> result = node1.render(new PageInfoImpl("SomePage"), "sometext some:wiki some::moretext", null, null);
    CompositeResultNode composite = new CompositeResultNode(result);
    assertEquals("sometext processed1 processed2", composite.toXHTML());
  }

  public void testRenderingWithException() {
    // test that throwing an exception doesn't affect rendering of the rest of the text
    RenderNode node1 = new AbstractRegexNode("testNode") {
      public ResultNode handle(PageInfo page, Matcher matcher, RenderNode parent, URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException {
        throw new UnknownWikiException();
      }
      @Override
      public Matcher find(String text) {
        Pattern pattern = Pattern.compile("\\p{Alnum}+:\\p{Alnum}+");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher : null;
      }
    };
    RenderNode node2 = new AbstractRegexNode("testNode2") {
      public ResultNode handle(PageInfo page, Matcher matcher, RenderNode parent, URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException {
        return new HtmlEscapeResultNode("processed");
      }
      @Override
      public Matcher find(String text) {
        Pattern pattern = Pattern.compile("\\p{Alnum}+::\\p{Alnum}+");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher : null;
      }
    };
    node1.addChildren(node1);
    node1.addChildren(node2);
    List<ResultNode> result = node1.render(new PageInfoImpl("SomePage"), "sometext some:wiki some::moretext", null, null);
    CompositeResultNode composite = new CompositeResultNode(result);
    assertEquals("sometext some:wiki processed", composite.toXHTML());
  }
}
