package net.hillsdon.reviki.di;

import net.hillsdon.reviki.web.dispatching.WikiHandler;

public interface WikiSession extends Session {
  
  WikiHandler getWikiHandler();
  
}