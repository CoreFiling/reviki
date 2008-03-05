package net.hillsdon.svnwiki.wiki.renderer;

import junit.framework.TestCase;

public class TestCreoleRenderer extends TestCase {

  private CreoleRenderer _render;

  @Override
  protected void setUp() throws Exception {
    _render = new CreoleRenderer();
  }
  
  public void testLineBreak() {
    assertEquals("<p>foo<br />bar</p>", _render.render("foo\\bar"));
  }
  
  public void testTitles() {
    assertEquals("<h1>Foo</h1>", _render.render("=Foo"));
    assertEquals("<h2>Foo</h2>", _render.render("==Foo"));
    assertEquals("<h3>Foo</h3>", _render.render("===Foo"));
    assertEquals("<h4>Foo</h4>", _render.render("====Foo"));
    assertEquals("<h5>Foo</h5>", _render.render("=====Foo"));
  }
  
  public void testParagraphs() {
    assertEquals("<p>foo</p><p>bar</p>", _render.render("foo\n\nbar"));
  }
  
  public void testNoWikiBlock() {
    assertEquals("<pre>//not italic//</pre>", _render.render("{{{//not italic//}}}"));
  }
  
  public void testStrongAndEmphasis() {
    assertEquals("<p><em><strong>foo</strong></em></p>", _render.render("//**foo**//"));
    assertEquals("<p><strong><em>foo</em></strong></p>", _render.render("**//foo//**"));
  }
  
  public void testLists() {
    assertEquals("<ul><li> foo</li>\n<li> bar</li></ul>", _render.render("* foo\n* bar"));
    assertEquals("<ol><li>foo</li>\n<li> bar</li></ol>", _render.render("#foo\n# bar"));
  }
  
}
