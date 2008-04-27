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
package net.hillsdon.reviki.web.vcintegration;

import java.util.Collection;

import net.hillsdon.reviki.vc.PageReference;

import static java.util.Collections.unmodifiableCollection;

import static java.util.Arrays.asList;

/**
 * Constants for refering to standard built-in pages.
 * 
 * @author mth
 */
public final class BuiltInPageReferences {

  public static final PageReference PAGE_FRONT_PAGE = new PageReference("FrontPage");
  public static final PageReference PAGE_RECENT_CHANGES = new PageReference("RecentChanges");
  
  public static final PageReference PAGE_SIDEBAR = new PageReference("ConfigSideBar");
  public static final PageReference PAGE_HEADER = new PageReference("ConfigHeader");
  public static final PageReference PAGE_FOOTER = new PageReference("ConfigFooter");

  public static final PageReference CONFIG_AUTO_PROPERTIES = new PageReference("ConfigAutoProperties");
  public static final PageReference CONFIG_INTER_WIKI_LINKS = new PageReference("ConfigInterWikiLinks");
  public static final PageReference CONFIG_PLUGINS = new PageReference("ConfigPlugins");
  public static final PageReference CONFIG_CSS = new PageReference("ConfigCss");

  public static final Collection<PageReference> COMPLIMENTARY_CONTENT_PAGES = unmodifiableCollection(asList(PAGE_SIDEBAR, PAGE_HEADER, PAGE_FOOTER));



  private BuiltInPageReferences() {
  }
  
}
