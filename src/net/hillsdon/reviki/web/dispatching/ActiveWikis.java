package net.hillsdon.reviki.web.dispatching;

import net.hillsdon.reviki.configuration.PerWikiInitialConfiguration;

public interface ActiveWikis {

  WikiHandler addWiki(PerWikiInitialConfiguration configuration);

}
