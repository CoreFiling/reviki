package net.hillsdon.reviki.di;

import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.web.dispatching.Dispatcher;


public interface ApplicationSession extends Session {
  
  Dispatcher getDispatcher();
  
  WikiSession createWikiSession(WikiConfiguration configuration);
  
}