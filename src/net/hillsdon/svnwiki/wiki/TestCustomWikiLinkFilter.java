package net.hillsdon.svnwiki.wiki;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.vc.SimplePageStore;

import org.radeox.engine.context.BaseRenderContext;
import org.radeox.filter.context.BaseFilterContext;

public class TestCustomWikiLinkFilter extends TestCase {

  private CustomWikiLinkFilter _filter;
  private BaseFilterContext _context;

  @Override
  protected void setUp() throws Exception {
    InterWikiLinker interWikiLinker = new InterWikiLinker();
    interWikiLinker.addWiki("c2", "http://c2.com/cgi/wiki?%s");
    _filter = new CustomWikiLinkFilter(interWikiLinker);
    _context = new BaseFilterContext();
    BaseRenderContext renderContext = new BaseRenderContext();
    SimplePageStore store = new SimplePageStore();
    store.set("ExistingWikiWord", "", 0, "", "");
    renderContext.setRenderEngine(new SvnWikiRenderEngine(store));
    _context.setRenderContext(renderContext);
  }
  
  public void testNewAndExisting() {
    assertEquals("This has a <a class='new-page' href='WikiWord'>WikiWord</a>.", _filter.filter("This has a WikiWord.", _context));
    assertEquals("This has an <a class='existing-page' href='ExistingWikiWord'>ExistingWikiWord</a>.", _filter.filter("This has an ExistingWikiWord.", _context));
  }
  
  public void testInterWikiLinks() {
    assertEquals("<a class='inter-wiki' href='http://c2.com/cgi/wiki?WikiEngines'>c2:WikiEngines</a>", _filter.filter("c2:WikiEngines", _context));
  }
  
}

