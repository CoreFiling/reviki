package net.hillsdon.reviki.wiki.macros;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;
import net.hillsdon.reviki.search.SearchMatch;

public class TestAbstractListOfPagesMacro extends TestCase {

  public void test() throws Exception {
    AbstractListOfPagesMacro macro = new AbstractListOfPagesMacro() {
      public String getName() {
        return "foo";
      }
      @Override
      protected Collection<SearchMatch> getPages(final String remainder) throws Exception {
        return Arrays.asList(new SearchMatch(true, "wiki", "foo bar", null, null), new SearchMatch(true, "wiki", "ABC", null, null));
      }
      @Override
      protected Collection<String> getAllowedArgs() {
        return Collections.emptySet();
      }
    };
    String result = macro.handle(null, "", null);
    assertEquals("  * [[ABC]]\n  * [[foo bar]]\n", result);
  }

}
