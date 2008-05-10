package net.hillsdon.reviki.configuration;

import java.io.File;

import org.tmatesoft.svn.core.SVNURL;

class PropertiesPerWikiConfiguration implements WikiConfiguration {

  private final PropertiesDeploymentConfiguration _deploymentConfiguration;
  private final String _wikiName;

  public PropertiesPerWikiConfiguration(final PropertiesDeploymentConfiguration deploymentConfiguration, final String wikiName) {
    _deploymentConfiguration = deploymentConfiguration;
    _wikiName = wikiName;
  }
  
  public File getSearchIndexDirectory() {
    return _deploymentConfiguration.getSearchIndexDirectory(_wikiName);
  }

  public void setUrl(final String location) {
    _deploymentConfiguration.setUrl(_wikiName, location);
  }
  
  public SVNURL getUrl() {
    return _deploymentConfiguration.getUrl(_wikiName);
  }

  public String getWikiName() {
    return _wikiName;
  }

  public void save() {
    _deploymentConfiguration.save();
  }

  public boolean isComplete() {
    return _deploymentConfiguration.isComplete(_wikiName);
  }

  public boolean isEditable() {
    return _deploymentConfiguration.isEditable();
  }
  
  public String getFixedBaseUrl() {
    return _deploymentConfiguration.getFixedBaseUrl(_wikiName);
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof PropertiesPerWikiConfiguration) {
      String givenWikiName = ((PropertiesPerWikiConfiguration) obj)._wikiName;
      return _wikiName == null ? givenWikiName == null : _wikiName.equals(givenWikiName);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return _deploymentConfiguration.getClass().hashCode() ^ (_wikiName == null ? 0 : _wikiName.hashCode());
  }

}