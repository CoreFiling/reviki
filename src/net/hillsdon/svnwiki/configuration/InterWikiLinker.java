package net.hillsdon.svnwiki.configuration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.wiki.UnknownWikiException;

/**
 * Can create links to external wikis given a wiki name and page name.
 * 
 * @author mth
 */
public class InterWikiLinker {

  private Map<String, String> _links = new LinkedHashMap<String, String>();

  /**
   * @param wikiName Wiki name.  Will overwrite any previous entry with the same wiki name.
   * @param formatString Absolute URI template with one %s which will be replaced by the page name when creating links.
   */
  public void addWiki(final String wikiName, final String formatString) {
    _links.put(wikiName, formatString);
  }

  /**
   * @param wikiName Wiki name.
   * @param pageName Page name/
   * @return A link.
   * @throws UnknownWikiException If wikiName is unknown.
   * @see #addWiki(String, String)
   */
  public String url(final String wikiName, final String pageName) throws UnknownWikiException {
    String formatString = _links.get(wikiName);
    if (formatString == null) {
      throw new UnknownWikiException();
    }
    return String.format(formatString, Escape.url(pageName));
  }
  
  /**
   * Exposed for testing.
   * @return Unmodifiable map from wiki name for format string as provided in {@link #addWiki(String, String)}. 
   */
  Map<String, String> getWikiToFormatStringMap() {
    return Collections.unmodifiableMap(_links);
  }

  public boolean hasWiki(final String name) {
    return _links.keySet().contains(name);
  }
  
}
