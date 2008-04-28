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
package net.hillsdon.reviki.web.taglib;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.web.urls.ApplicationUrls;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.ResourceUrls;
import net.hillsdon.reviki.web.urls.WikiUrls;

/**
 * Uses an {@link InternalLinker} to create links to wiki pages.
 * 
 * @copyright
 * @author mth
 */
public class ResourceUrlTag extends TagSupport {

  private static final long serialVersionUID = 1L;
  private String _path;

  public String getPath() {
    return _path;
  }

  public void setPath(final String path) {
    _path = path;
  }

  public int doStartTag() throws JspException {
    try {
      final ServletRequest request = pageContext.getRequest();
      final ApplicationUrls application = (ApplicationUrls) request.getAttribute(ApplicationUrls.KEY);
      final WikiUrls wiki = (WikiUrls) request.getAttribute(WikiUrls.KEY);
      if (wiki != null) {
        outputUrl(wiki);
      }
      else if (application != null) {
        outputUrl(application);
      }
    }
    catch (IOException e) {
      throw new JspException(e);
    }
    return SKIP_BODY;
  }

  private void outputUrl(final ResourceUrls resourceUrls) throws IOException {
    JspWriter out = pageContext.getOut();
    out.write(Escape.html(resourceUrls.resource(getPath())));
  }
  
}

