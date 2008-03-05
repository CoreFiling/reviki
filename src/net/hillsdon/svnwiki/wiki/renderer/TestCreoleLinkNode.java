package net.hillsdon.svnwiki.wiki.renderer;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.SimplePageStore;
import net.hillsdon.svnwiki.wiki.InternalLinker;

public class TestCreoleLinkNode extends TestCase {

  private CreoleLinkNode _node;

  @Override
  protected void setUp() throws Exception {
    _node = new CreoleLinkNode(new SvnWikiLinkPartHandler(new InternalLinker("/context", "wiki", new SimplePageStore()), new FakeConfiguration()));
  }
  
  public void testInternal() {
    assertEquals("<a class='new-page' href='/context/pages/wiki/FooPage'>Tasty</a>", _node.handle(new PageReference("WhereEver"), _node.find("[[FooPage|Tasty]]")));
  }

  public void testInterWiki() {
    assertEquals("<a class='inter-wiki' href='http://www.example.com/foo/Wiki?FooPage'>Tasty</a>", _node.handle(new PageReference("WhereEver"), _node.find("[[foo:FooPage|Tasty]]")));
  }
  
  public void testExternal() {
    assertEquals("<a class='external' href='http://www.example.com'>Tasty</a>", _node.handle(new PageReference("WhereEver"), _node.find("[[http://www.example.com|Tasty]]")));
  }
  
}
