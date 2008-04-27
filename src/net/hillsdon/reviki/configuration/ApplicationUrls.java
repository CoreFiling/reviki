package net.hillsdon.reviki.configuration;

import net.hillsdon.reviki.wiki.WikiUrls;

public interface ApplicationUrls {

  /**
   * Prefer adding methods to using this one.
   * 
   * @param relative With leading '/'.
   * @return An absolute URL within this application.
   */
  String url(String relative);
  
  /**
   * @return URL for the wiki list.
   */
  String list();
  
  /**
   * Note the returned value may be specific to the current request.
   * 
   * @param name The wiki name (null for default).
   * @return The relevant URLs.
   */
  WikiUrls get(String name);
  
}
