package net.hillsdon.reviki.web.vcintegration;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.hillsdon.fij.text.Strings;
import net.hillsdon.reviki.vc.impl.SimplePageStore;

public class TestAutoPropertiesFromConfigPage extends TestCase {

  private SimplePageStore _store;
  private AutoProperiesFromConfigPage _autoProperiesFromConfigPage;

  @Override
  protected void setUp() throws Exception {
    _store = new SimplePageStore();
    _autoProperiesFromConfigPage = new AutoProperiesFromConfigPage();
    _autoProperiesFromConfigPage.setPageStore(_store);
  }
  
  public void testCopesWhenNoPage() throws Exception {
    assertTrue(_autoProperiesFromConfigPage.read().isEmpty());
  }
  
  public void testParsesIgnoringInvalidLinesAndComments() throws Exception {
    final String text = 
      " *.png = svn:mime-type=image/png  " + Strings.CRLF
    + " # *.jpg = svn:mime-type=image/jpeg" + Strings.CRLF
    + "*.tga svn:mime-typeimage/tga" + Strings.CRLF
    + "" + Strings.CRLF
    + "README = svn:mime-type=text/plain;svn:eol-style=native" + Strings.CRLF;
    
    _store.set(SpecialPagePopulatingPageStore.CONFIG_AUTO_PROPERTIES, "", 0, text, "");
    Map<String, String> expected = new LinkedHashMap<String, String>();
    expected.put("*.png", "svn:mime-type=image/png");
    expected.put("README", "svn:mime-type=text/plain;svn:eol-style=native");
    assertEquals(expected, _autoProperiesFromConfigPage.read());
  }
  
}
