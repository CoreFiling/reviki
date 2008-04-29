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
package net.hillsdon.reviki.web.handlers.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.handlers.ListWikis;
import net.hillsdon.reviki.web.urls.ApplicationUrls;
import net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences;

public class ListWikisImpl implements ListWikis {

  /**
   * Used by the UI to render the list of wikis.
   * 
   * @author mth
   */
  public static class WikiDescriptor {

    private final String _name;
    private final String _frontPage;

    public WikiDescriptor(final String name, final String frontPage) {
      _name = name;
      _frontPage = frontPage;
    }
    
    public String getName() {
      return _name;
    }
    
    public String getFrontPageUrl() {
      return _frontPage;
    }
    
  }
  
  private final ApplicationUrls _applicationUrls;
  private final DeploymentConfiguration _configuration;

  public ListWikisImpl(final DeploymentConfiguration configuration, final ApplicationUrls applicationUrls) {
    _configuration = configuration;
    _applicationUrls = applicationUrls;
  }
  
  public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws PageStoreException, IOException, ServletException {
    final List<WikiDescriptor> descriptors = new ArrayList<WikiDescriptor>();
    for (String name : _configuration.getWikiNames()) {
      String frontPage = _applicationUrls.get(name).page(BuiltInPageReferences.PAGE_FRONT_PAGE.getPath());
      descriptors.add(new WikiDescriptor(name, frontPage));
    }
    request.setAttribute("descriptors", descriptors);
    request.setAttribute("configuration", _configuration);
    return new JspView("ListWikis");
  }

}
