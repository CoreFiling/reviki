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

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.fij.core.Factory;
import net.hillsdon.fij.core.Transform;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.impl.AbstractDelegatingPageStore;

/**
 * Allow us to pass a PageStore into various objects but
 *  a) use authentication from the current request
 *  b) work with the thread-safety limitations of 
 *     {@link org.tmatesoft.svn.core.io.SVNRepository}.
 * 
 * @author mth
 */
public final class RequestScopedPageStore extends AbstractDelegatingPageStore implements RequestLifecycleAware {

  private final RequestLocal<PageStore> _requestLocal;
  
  public RequestScopedPageStore(final Factory<PageStore> factory) {
    _requestLocal = new RequestLocal<PageStore>(new Transform<HttpServletRequest, PageStore>() {
      public PageStore transform(final HttpServletRequest in) {
        // We don't care about the request.
        return factory.newInstance();
      }
    });
  }
  
  public void create(final HttpServletRequest request) {
    _requestLocal.create(request);
  }
  
  public void destroy() {
    _requestLocal.destroy();
  }
  
  @Override
  protected PageStore getDelegate() {
    return _requestLocal.get();
  }
  
}
