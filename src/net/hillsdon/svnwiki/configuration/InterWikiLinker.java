package net.hillsdon.svnwiki.configuration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.hillsdon.svnwiki.wiki.UnknownWikiException;

public class InterWikiLinker {

  private Map<String, String> _links = new LinkedHashMap<String, String>();

  public String link(final String wikiName, final String pageName) throws UnknownWikiException {
    String formatString = _links.get(wikiName);
    if (formatString == null) {
      throw new UnknownWikiException();
    }
    return String.format(formatString, pageName);
  }

  public void addWiki(final String wikiName, final String formatString) {
    _links.put(wikiName, formatString);
  }
  
  public Map<String, String> getWikiToFormatStringMap() {
    return Collections.unmodifiableMap(_links);
  }
  
}
