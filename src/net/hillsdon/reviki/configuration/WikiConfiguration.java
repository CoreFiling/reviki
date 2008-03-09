package net.hillsdon.reviki.configuration;

import java.io.File;

import org.tmatesoft.svn.core.SVNURL;

/**
 * Configuration details for a particular wiki.
 * 
 * @author mth
 */
public interface WikiConfiguration {

  /**
   * @return The name of the wiki this is the configuration for.
   */
  String getWikiName();

  /**
   * @return The given wiki name, null if we're the default wiki.
   */
  String getGivenWikiName();

  /**
   * @return The SVN URL for our data store.
   */
  SVNURL getUrl();

  /**
   * @param url The URL.
   * @throws IllegalArgumentException If the URI is not a valid SVNURL.
   */
  void setUrl(String url) throws IllegalArgumentException;

  /**
   * @return The directory to store the search engine index in or null if not possible.
   */
  File getSearchIndexDirectory();

  /**
   * @return true if the configuration is OK.
   */
  boolean isComplete();

  /**
   * Save the wiki configuration.
   */
  void save();

  /**
   * @return true if changes can be persisted.
   *              (note it is possible for this to change over time).
   */
  boolean isEditable();

}
