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
package net.hillsdon.reviki.web.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A filter that adds Cache-Control headers to resources.
 * 
 * @author mth
 */
public class CacheResourcesFilter implements Filter {

  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
    if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
      if (isResourceAccess((HttpServletRequest) request)) {
        ((HttpServletResponse) response).setHeader("Cache-Control", "max-age=86400");
      }
    }
    chain.doFilter(request, response);
  }

  private boolean isResourceAccess(final HttpServletRequest request) {
    if (!"GET".equals(request.getMethod())) {
      return false;
    }
    
    String uri = request.getRequestURI();
    // Kludge... this page should cope on its own really.
    if (uri.endsWith("/ConfigCss")) {
      return true;
    }
    return request.getParameterMap().isEmpty()
       && (uri.endsWith(".css") 
        || uri.endsWith(".js")); 
  }
  
  public void init(final FilterConfig config) throws ServletException {
  }

  public void destroy() {
  }

}
