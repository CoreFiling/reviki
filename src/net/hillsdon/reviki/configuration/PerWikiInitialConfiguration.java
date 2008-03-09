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

import org.tmatesoft.svn.core.SVNURL;


public class PerWikiInitialConfiguration {

  private final PropertiesDeploymentConfiguration _configuration;
  private final String _wikiName;
  private final String _givenWikiName;

  /**
   * @param configuration Main configuration.
   * @param givenWikiName Name we were accessed as, either wikiName or null if we're the default.
   * @param wikiName Name of this wiki.
   */
  public PerWikiInitialConfiguration(final PropertiesDeploymentConfiguration configuration, final String givenWikiName, final String wikiName) {
    _configuration = configuration;
    _givenWikiName = givenWikiName;
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

  public String getGivenWikiName() {
    return _givenWikiName;
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof PerWikiInitialConfiguration) {
      String givenWikiName = ((PerWikiInitialConfiguration) obj)._givenWikiName;
      return _givenWikiName == null ? givenWikiName == null : _givenWikiName.equals(givenWikiName);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return getClass().hashCode() ^ (_givenWikiName == null ? 0 : _givenWikiName.hashCode());
  }
  
}
