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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.impl.ResponseSessionURLOutputFilter;

/**
 * Uses an {@link InternalLinker} to create links to wiki pages.
 *
 * @copyright
 * @author mth
 */
public abstract class AbstractWikiLinkTag extends TagSupport {

  private static final long serialVersionUID = 1L;
  private String _wiki;
  private String _page;
  private String _extraPath = null;
  private String _query = null;
  private boolean _session = true;

  public String getPage() {
    return _page;
  }

  public void setPage(final String page) {
    _page = page;
  }

  public String getWiki() {
    return _wiki;
  }

  public void setWiki(final String wiki) {
    _wiki = wiki;
  }

  public String getExtraPath() {
    return _extraPath;
  }

  public void setExtraPath(final String extraPath) {
    _extraPath = extraPath;
  }

  public String getQuery() {
    return _query;
  }

  public void setQuery(String query) {
    _query = query;
  }

  public boolean isSession() {
    return _session;
  }

  public void setSession(final boolean session) {
    _session = session;
  }

  public int doStartTag() throws JspException {
    try {
      InternalLinker linker = (InternalLinker) pageContext.getRequest().getAttribute("internalLinker");
      final HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
      if (linker != null) {
        JspWriter out = pageContext.getOut();
        URLOutputFilter urlOutputFilter = isSession() ? new ResponseSessionURLOutputFilter((HttpServletRequest) pageContext.getRequest(), response) : URLOutputFilter.NULL;
        out.write(doOutput(linker, urlOutputFilter));
      }
    }
    catch (IOException e) {
      throw new JspException(e);
    }
    return SKIP_BODY;
  }

  protected abstract String doOutput(InternalLinker linker, URLOutputFilter urlOutputFilter);

}

