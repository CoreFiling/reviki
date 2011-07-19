package net.hillsdon.reviki.web.urls;

import junit.framework.TestCase;

/**
 * Test for {@link InterWikiLinker}.
 *
 * @author mth
 */
public class TestInterWikiLinker extends TestCase {

  private InterWikiLinker _linker;

  @Override
  protected void setUp() throws Exception {
    _linker = new InterWikiLinker();
  }

  public void testSubstitution() throws Exception {
    _linker.addWiki("foo", "%s%s");
    assertEquals("barbar", _linker.uri("foo", "bar", null).toASCIIString());
    assertEquals("%20%20", _linker.uri("foo", " ", null).toASCIIString());
    assertEquals("++", _linker.uri("foo", "+", null).toASCIIString());
  }

  public void testSubstitutionWithPercentEscapes() throws Exception {
    // This used to go wrong because of foolish use of String.format.
    String urlPrefix = "https://bugs.example.org/buglist.cgi?product=True%20North%20iXBRL%20Module&";
    _linker.addWiki("foo", urlPrefix + "priority=P%s");
    assertEquals(urlPrefix + "priority=P2", _linker.uri("foo", "2", null).toASCIIString());
  }

}
