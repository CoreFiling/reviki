package net.hillsdon.reviki.configuration;

import net.hillsdon.reviki.wiki.WikiUrls;

public interface ApplicationUrls {

  /**
   * @return URL for the wiki list.
   */
  String list();
  
  /**
   * @param name The wiki name (null for default).
   * @return The relevant URLs.
   */
  WikiUrls get(String name);
  
}
