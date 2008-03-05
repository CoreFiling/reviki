package net.hillsdon.svnwiki.wiki.renderer;

import junit.framework.TestCase;

public class TestCreoleRenderer extends TestCase {

  private CreoleRenderer _render;

  @Override
  protected void setUp() throws Exception {
    // Ignore wiki links for now.
    _render = new CreoleRenderer(new RegexMatchToTag("$^", "", null, null, null));
  }

  public void testStrikethrough() {
    assertEquals("<p><del>stuff</del></p>", _render.render("--stuff--"));
  }

  public void testHorizontalRule() {
    assertEquals("<p>foo<hr />bar</p>", _render.render("foo\n ---- \nbar"));
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
    assertEquals("<p>foo\n</p><p>\nbar</p>", _render.render("foo\n\nbar"));
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
  
  public void testNestedLists() {
    assertEquals("<ul><li>Outer<ul>\n<li>Inner</li></ul></li></ul>", _render.render("*Outer\n**Inner"));
  }
  
  public void testHtmlEscaping() {
    assertEquals("Inner text.", "<p><em>&lt;script&gt;</em></p>", _render.render("//<script>//"));
    assertEquals("Skipped text.", "<p>&lt;script&gt;<em>foo</em></p>", _render.render("<script>//foo//"));
  }
  
}
