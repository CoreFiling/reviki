/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

/**
 * Wherein we go to mad lengths to store the SVN URL and search index somewhere.
 * 
 * Additional run-time configuration options are stored in SVN rather than
 * on the file-system so that they benefit from versioning and backup.
 * 
 * @author mth
 */
public class PropertiesDeploymentConfiguration implements DeploymentConfiguration {

  private static final String DEFAULT_CONFIG_DIR_NAME = "reviki-data";
  private static final String SEARCH_INDEX_DIR_NAME = "search-index";
  private static final String CONFIG_FILE_NAME = "reviki.properties";
  // Properties file keys:
  private static final String KEY_PREFIX_SVN_URL = "svn-url-";
  private static final String KEY_PREFIX_BASE_URL = "base-url-";

  /**
   * @return A configuration location if we can, otherwise null.
   */
  private static File getConfigurationLocation() {
    String location = null;
    try {
      location = System.getProperty("reviki.data");
    }
    catch (SecurityException ex) {
    }
    if (location == null) {
      try {
        location = System.getenv("REVIKI_DATA");
      }
      catch (SecurityException ex) {
      }
    }
    if (location == null) {
      try {
        String home = System.getProperty("user.home");
        location = home + File.separator + DEFAULT_CONFIG_DIR_NAME;
      }
      catch (SecurityException ex) {
      }
    }
    if (location == null) {
      return null;
    }
    File dir = new File(location);
    try {
      if (!dir.exists()) {
        if (!dir.mkdir()) {
          return null;
        }
      }
    }
    catch (SecurityException ex) {
      return null;
    }
    return dir;
  }

  private static File getConfigurationFile() {
    File location = getConfigurationLocation();
    if (location != null) {
      File file = new File(location, CONFIG_FILE_NAME);
      return file;
    }
    return null;
  }

  private class PropertiesPerWikiConfiguration implements WikiConfiguration {

    private final String _wikiName;

    public PropertiesPerWikiConfiguration(final String wikiName) {
      _wikiName = wikiName;
    }
    
    public File getSearchIndexDirectory() {
      return PropertiesDeploymentConfiguration.this.getSearchIndexDirectory(_wikiName);
    }

    public void setUrl(final String location) {
      PropertiesDeploymentConfiguration.this.setUrl(_wikiName, location);
    }
    
    public SVNURL getUrl() {
      return PropertiesDeploymentConfiguration.this.getUrl(_wikiName);
    }

    public String getWikiName() {
      return _wikiName;
    }

    public void save() {
      PropertiesDeploymentConfiguration.this.save();
    }

    public boolean isComplete() {
      return PropertiesDeploymentConfiguration.this.isComplete(_wikiName);
    }

    public boolean isEditable() {
      return PropertiesDeploymentConfiguration.this.isEditable();
    }
    
    public String getFixedBaseUrl() {
      return PropertiesDeploymentConfiguration.this.getFixedBaseUrl(_wikiName);
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
      return getClass().hashCode() ^ (_wikiName == null ? 0 : _wikiName.hashCode());
    }

  }

  private final PersistentStringMap _properties;

  public PropertiesDeploymentConfiguration() {
    this(new PropertiesFile(getConfigurationFile()));
  }

  PropertiesDeploymentConfiguration(final PersistentStringMap properties) {
    _properties = properties;
  }
  
  public WikiConfiguration getConfiguration(final String wikiName) {
    return new PropertiesPerWikiConfiguration(wikiName);
  }
  
  public SVNURL getUrl(final String wikiName) {
    String url = _properties.get(KEY_PREFIX_SVN_URL + wikiName);
    try {
      return SVNURL.parseURIDecoded(url);
    }
    catch (SVNException ex) {
      return null;
    }
  }
  
  public String getFixedBaseUrl(final String wikiName) {
    return _properties.get(KEY_PREFIX_BASE_URL + wikiName);
  }

  private File getSearchIndexDirectory(final String wikiName) {
    File searchDir = getWritableChildDir(getConfigurationLocation(), SEARCH_INDEX_DIR_NAME);
    return searchDir == null ? null : getWritableChildDir(searchDir, wikiName);
  }
  
  private File getWritableChildDir(final File dir, final String child) {
    File indexDir = new File(dir, child);
    if (!indexDir.exists()) {
      if (!indexDir.mkdir()) {
        return null;
      }
    }
    if (indexDir.isDirectory() && indexDir.canWrite()) {
      return indexDir;
    }
    return null;
  }
  
  private void setUrl(final String wikiName, final String url) throws IllegalArgumentException {
    try {
      SVNURL svnUrl = SVNURL.parseURIDecoded(url);
      _properties.put(KEY_PREFIX_SVN_URL + wikiName, svnUrl.toDecodedString());
    }
    catch (SVNException e) {
      throw new IllegalArgumentException("Invalid SVN URL", e);
    }
  }

  private boolean isComplete(final String wikiName) {
    return getUrl(wikiName) != null;
  }
  
  public synchronized void load() {
    try {
      _properties.load();
    }
    catch (IOException e) {
      // We ignore errors for now.
    }
  }

  public synchronized void save() {
    try {
      _properties.save();
    }
    catch (IOException e) {
      // We ignore errors for now.
    }
  }
  
  public boolean isEditable() {
    final File parent = getConfigurationLocation();
    if (parent != null) {
      final File file = new File(parent, CONFIG_FILE_NAME);
      return file.exists() ? file.canWrite() : parent.canWrite();
    }
    return false;
  }

  public Collection<String> getWikiNames() {
    List<String> names = new ArrayList<String>();
    for (String key : _properties.keySet()) {
      if (key.startsWith(KEY_PREFIX_SVN_URL)) {
        names.add(key.substring(KEY_PREFIX_SVN_URL.length(), key.length()));
      }
    }
    return names;
  }

}
