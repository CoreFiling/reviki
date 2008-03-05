package net.hillsdon.svnwiki.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

/**
 * Configuration derived from ConfigXXX pages in the wiki.
 * 
 * @author mth
 */
public class PageStoreConfiguration implements Configuration {

  private final PageStore _store;

  public PageStoreConfiguration(final PageStore store) {
    _store = store;
  }
  
  /**
   * @return An interwiki linker populated according to ConfigInterWikiLinks
   *         which should be lines of the form:
   *         c2 http://c2.com/cgi/wiki?%s
   *         where %s is a placeholder for the page name. 
   */
  public InterWikiLinker getInterWikiLinker() throws PageStoreException {
    PageInfo page = _store.get(new PageReference("ConfigInterWikiLinks"), -1);
    InterWikiLinker linker = new InterWikiLinker();
    if (!page.isNew()) {
      parseLinkEntries(linker, page.getContent());
    }
    return linker;
  }

  private void parseLinkEntries(final InterWikiLinker linker, final String data) {
    try {
      BufferedReader reader = new BufferedReader(new StringReader(data));
      String line;
      while ((line = reader.readLine()) != null) {
        int spaceIndex = line.indexOf(' ');
        if (spaceIndex != -1) {
          String wikiName = line.substring(0, spaceIndex).trim();
          String formatString = line.substring(spaceIndex + 1).trim();
          linker.addWiki(wikiName, formatString);
        }
      }
    }
    catch (IOException ex) {
      throw new RuntimeException("I/O error reading from memory!", ex);
    }
  }

}
