package net.hillsdon.reviki.configuration;

import java.util.Collection;

public interface DeploymentConfiguration {

  /**
   * @param actualWikiName The wiki name to store data under.
   * @param givenWikiName The wiki name to use, possibly null for the default.
   * @return A wiki configuration specific to <code>actualWikiName</code>
   */
  WikiConfiguration getConfiguration(String actualWikiName, String givenWikiName);

  /**
   * Load the configuration into memory.
   */
  void load();

  /**
   * Save the configuration to disk.
   */
  void save();

  /**
   * @return true if changes can be persisted.
   *              (note it is possible for this to change over time).
   */
  boolean isEditable();

  /**
   * Sets a wiki as default.
   * 
   * @param wikiName The name of a wiki.
   */
  void setDefaultWiki(String wikiName);

  /**
   * @return The default wiki or null if none.
   */
  String getDefaultWiki();

  /**
   * @return Names of the configured wikis.
   */
  Collection<String> getWikiNames();

}
