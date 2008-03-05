package net.hillsdon.svnwiki.configuration;

import java.io.File;

import org.tmatesoft.svn.core.SVNURL;


public class PerWikiInitialConfiguration {

  private final ConfigurationLocation _configuration;
  private final String _wikiName;

  public PerWikiInitialConfiguration(final ConfigurationLocation configuration, final String wikiName) {
    _configuration = configuration;
    _wikiName = wikiName;
  }
  
  public File getSearchIndexDirectory() {
    return _configuration.getSearchIndexDirectory(_wikiName);
  }

  public void setUrl(final String location) {
    _configuration.setUrl(_wikiName, location);
  }
  
  public SVNURL getUrl() {
    return _configuration.getUrl(_wikiName);
  }

  public String getWikiName() {
    return _wikiName;
  }

  public void save() {
    _configuration.save();
  }

  public boolean isComplete() {
    return _configuration.isComplete(_wikiName);
  }
  
}
