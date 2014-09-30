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
/**
 * 
 */
package net.hillsdon.reviki.wiki.renderer;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.tmatesoft.svn.core.SVNURL;

import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.vc.impl.SimplePageStore;
import net.hillsdon.reviki.web.urls.ApplicationUrls;
import net.hillsdon.reviki.web.urls.WikiUrls;
import net.hillsdon.reviki.web.urls.impl.PageStoreConfiguration;
import net.hillsdon.reviki.web.urls.impl.WikiUrlsImpl;

public class FakeConfiguration extends PageStoreConfiguration {
  public FakeConfiguration() {
    this("foo", "http://www.example.com/");
  }

  public FakeConfiguration(final String wikiName, final String baseUrl) {
    super(new SimplePageStore(), new ApplicationUrls() {
      
      @Override
      public String resource(String path) {
        return url("/resources/" + path);
      }
      
      @Override
      public String url(String relative) {
        return baseUrl + relative;
      }
      
      @Override
      public String list() {
        return url("/list");
      }
      
      @Override
      public Set<WikiUrls> getAvailableWikiUrls() {
        return Collections.singleton(get(wikiName));
      }
      
      @Override
      public WikiUrls get(String name) {
        return new WikiUrlsImpl(this, new WikiConfiguration() {
          public void setUrl(String url) throws IllegalArgumentException {
          }
          
          @Override
          public void setSVNUser(String user) {
          }
          
          @Override
          public void setSVNPassword(String pass) {
          }
          
          @Override
          public void save() {
          }
          
          @Override
          public boolean isEditable() {
            return false;
          }
          
          @Override
          public boolean isComplete() {
            return false;
          }
          
          @Override
          public String getWikiName() {
            return wikiName;
          }
          
          @Override
          public SVNURL getUrl() {
            return null;
          }
          
          @Override
          public File getSearchIndexDirectory() {
            return null;
          }
          
          @Override
          public String getSVNUser() {
            return null;
          }
          
          @Override
          public String getSVNPassword() {
            return null;
          }
          
          @Override
          public List<File> getOtherSearchIndexDirectories() {
            return null;
          }
          
          @Override
          public String getFixedBaseUrl(String wikiName) {
            return baseUrl;
          }
          
          @Override
          public String getFixedBaseUrl() {
            return baseUrl;
          }
        });
      }
    });
  }
}