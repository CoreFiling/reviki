package net.hillsdon.svnwiki.configuration;

import net.hillsdon.svnwiki.vc.PageStoreException;

public interface Configuration {

  InterWikiLinker getInterWikiLinker() throws PageStoreException;
  
}
