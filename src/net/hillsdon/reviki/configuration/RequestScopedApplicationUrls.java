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

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.fij.core.Transform;
import net.hillsdon.reviki.web.vcintegration.RequestLifecycleAware;
import net.hillsdon.reviki.web.vcintegration.RequestLocal;
import net.hillsdon.reviki.wiki.WikiUrls;

public class RequestScopedApplicationUrls implements ApplicationUrls, RequestLifecycleAware {

  private RequestLocal<ApplicationUrls> _requestLocal;

  public RequestScopedApplicationUrls() {
    _requestLocal = new RequestLocal<ApplicationUrls>(new Transform<HttpServletRequest, ApplicationUrls>() {
      public ApplicationUrls transform(final HttpServletRequest in) {
        return new ApplicationUrlsImpl(in);
      }
    });
  }
  
  public WikiUrls get(final String name) {
    return _requestLocal.get().get(name);
  }

  public String list() {
    return _requestLocal.get().list();
  }

  public String url(final String relative) {
    return _requestLocal.get().url(relative);
  }

  public void create(final HttpServletRequest request) {
    _requestLocal.create(request);
  }

  public void destroy() {
    _requestLocal.destroy();
  }

}
