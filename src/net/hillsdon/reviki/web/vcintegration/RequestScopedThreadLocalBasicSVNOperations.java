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

import net.hillsdon.reviki.vc.impl.BasicSVNOperations;
import net.hillsdon.reviki.vc.impl.BasicSVNOperationsFactory;
import net.hillsdon.reviki.vc.impl.DelegatingBasicSVNOperations;

/**
 * Allow us to pass a BasicSVNOperations implementation into various objects but
 *  a) use authentication from the current request
 *  b) work with the thread-safety limitations of 
 *     {@link org.tmatesoft.svn.core.io.SVNRepository}.
 * 
 * @author mth
 */
public final class RequestScopedThreadLocalBasicSVNOperations extends DelegatingBasicSVNOperations implements RequestLifecycleAware {

  private final RequestLocal<BasicSVNOperations> _requestLocal;
  
  public RequestScopedThreadLocalBasicSVNOperations(final BasicSVNOperationsFactory factory) {
    _requestLocal = new RequestLocal<BasicSVNOperations>(factory);
  }
  
  public void create(final HttpServletRequest request) {
    _requestLocal.create(request);
  }
  
  public void destroy() {
    _requestLocal.destroy();
  }
  
  @Override
  protected BasicSVNOperations getDelegate() {
    return _requestLocal.get();
  }
  
}
