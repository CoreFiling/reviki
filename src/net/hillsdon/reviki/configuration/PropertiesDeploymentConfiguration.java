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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

  private static final Log LOG = LogFactory.getLog(PropertiesDeploymentConfiguration.class);
  
  // Properties file keys:
  private static final String KEY_PREFIX_SVN_URL = "svn-url-";
  private static final String KEY_PREFIX_BASE_URL = "base-url-";
  
  private final PersistentStringMap _properties;
  private final DataDir _dataDir;

  public PropertiesDeploymentConfiguration(final DataDir dataDir) {
    _dataDir = dataDir;
    _properties = dataDir.getProperties();
  }

  public WikiConfiguration getConfiguration(final String wikiName) {
    return new PropertiesPerWikiConfiguration(this, wikiName);
  }
  
  public SVNURL getUrl(final String wikiName) {
    String url = _properties.get(KEY_PREFIX_SVN_URL + wikiName);
    try {
      return SVNURL.parseURIDecoded(url);
    }
    catch (SVNException ex) {
      LOG.error("Invalid URL in properties.", ex);
      return null;
    }
  }
  
  public String getFixedBaseUrl(final String wikiName) {
    return _properties.get(KEY_PREFIX_BASE_URL + wikiName);
  }

  File getSearchIndexDirectory(final String wikiName) {
    return _dataDir.getSearchIndexDirectory(wikiName);
  }
  
  void setUrl(final String wikiName, final String url) throws IllegalArgumentException {
    try {
      SVNURL svnUrl = SVNURL.parseURIDecoded(url);
      _properties.put(KEY_PREFIX_SVN_URL + wikiName, svnUrl.toDecodedString());
    }
    catch (SVNException e) {
      throw new IllegalArgumentException("Invalid SVN URL", e);
    }
  }

  boolean isComplete(final String wikiName) {
    return getUrl(wikiName) != null;
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

  
  public synchronized void load() {
    try {
      _properties.load();
    }
    catch (IOException ex) {
      LOG.error("Failed to load properties.", ex);
    }
  }

  public synchronized void save() {
    try {
      _properties.save();
    }
    catch (IOException ex) {
      LOG.error("Failed to save properties.", ex);
    }
  }
  
  public boolean isEditable() {
    return _properties.isPersistable();
  }

}
