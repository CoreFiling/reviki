/**
 * Copyright 2007 Matthew Hillsdon
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
package net.hillsdon.svnwiki.web.common;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Partial implementation for testing.  Only those methods overridden in
 * this class do anything useful.
 * 
 * @author mth
 */
public final class MockHttpServletRequest extends NullHttpServletRequest {

  private Map<String, Object> _attributes = new LinkedHashMap<String, Object>();
  private Map<String, List<String>> _parameters = new LinkedHashMap<String, List<String>>();
  private String _requestURI = "";
  private String _requestURL = "";
  private String _contextPath = "";

  @Override
  public void setAttribute(final String key, final Object value) {
    _attributes.put(key, value);
  }

  public void setContextPath(final String contextPath) {
    _contextPath = contextPath;
  }
  
  @Override
  public String getContextPath() {
    return _contextPath;
  }
  
  @Override
  public Object getAttribute(final String key) {
    return _attributes.get(key);
  }
  
  public void setParameter(final String name, final String value) {
    setParameter(name, Collections.singletonList(value));
  }
  
  public void setParameter(final String name, final List<String> value) {
    _parameters.put(name, value);
  }
  
  @Override
  public String[] getParameterValues(final String name) {
    List<String> list = _parameters.get(name);
    return list == null ? null : list.toArray(new String[list.size()]);
  }
  
  @Override
  public String getParameter(final String name) {
    List<String> entries = _parameters.get(name);
    if (entries != null && entries.size() > 0) {
      return entries.get(0);
    }
    return null;
  }

  @Override
  public String getRequestURI() {
    return _requestURI;
  }
  
  @Override
  public StringBuffer getRequestURL() {
    return new StringBuffer(_requestURL);
  }
  
  public void setRequestURL(final String requestURL) {
    _requestURL = requestURL;
  }
  
  public void setRequestURI(final String requestURI) {
    _requestURI = requestURI;
  }
  
}
