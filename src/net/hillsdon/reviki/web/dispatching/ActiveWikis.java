package net.hillsdon.reviki.web.dispatching;

import net.hillsdon.reviki.configuration.WikiConfiguration;

public interface ActiveWikis {

  WikiHandler addWiki(WikiConfiguration configuration);

}
