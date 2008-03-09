package net.hillsdon.reviki.configuration;

import java.io.File;
import java.util.Collection;

import org.tmatesoft.svn.core.SVNURL;

public interface DeploymentConfiguration {

  SVNURL getUrl(String wikiName);

  File getSearchIndexDirectory(String wikiName);

  File getWritableChildDir(File dir, String child);

  void setUrl(String wikiName, String url) throws IllegalArgumentException;

  boolean isComplete(String wikiName);

  void load();

  void save();

  /**
   * @return true if changes can be persisted.
   *              (note it is possible for this to change over time).
   */
  boolean isEditable();

  void setDefaultWiki(String wikiName);

  String getDefaultWiki();

  Collection<String> getWikiNames();

}
