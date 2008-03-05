package net.hillsdon.svnwiki.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

public class PageStoreConfiguration implements Configuration {

  private final PageStore _store;

  public PageStoreConfiguration(final PageStore store) {
    _store = store;
  }
  
  public InterWikiLinker getInterWikiLinker() throws PageStoreException {
    PageInfo page = _store.get("InterWikiLinks", -1);
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
