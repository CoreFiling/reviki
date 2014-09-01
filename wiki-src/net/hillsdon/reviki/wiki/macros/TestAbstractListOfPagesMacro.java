package net.hillsdon.reviki.wiki.macros;

import java.util.Arrays;
import java.util.Collection;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;

import junit.framework.TestCase;

public class TestAbstractListOfPagesMacro extends TestCase {

  public void test() throws Exception {
    AbstractListOfPagesMacro macro = new AbstractListOfPagesMacro() {
      public String getName() {
        return "foo";
      }
      @Override
      protected Collection<String> getPages(final String remainder) throws Exception {
        return Arrays.asList("foo bar", "ABC");
      }
    };
    String result = macro.handle(null, null);
    assertEquals("  * [[ABC]]\n  * [[foo bar]]", result);
  }

  public void testCurrentPage() throws Exception {
    AbstractListOfPagesMacro macro = new AbstractListOfPagesMacro() {
      public String getName() {
        return "foo";
      }
      @Override
      protected Collection<String> getPages(final String remainder) throws Exception {
        return Arrays.asList(remainder);
      }
    };
    String pageName = "SomePage";
    PageInfo page = new PageInfoImpl(pageName);
    String result = macro.handle(page, "@_currentPage");
    assertEquals("  * [[" + page.getPath() + "]]", result);
  }

}
