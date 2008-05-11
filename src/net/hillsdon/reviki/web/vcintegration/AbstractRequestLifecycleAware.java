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

import net.hillsdon.fij.core.Transform;

public abstract class AbstractRequestLifecycleAware<T> implements RequestLifecycleAware {

  private final RequestLocal<T> _requestLocal;
  
  public AbstractRequestLifecycleAware(final Transform<HttpServletRequest, T> transform) {
    _requestLocal = new RequestLocal<T>(transform);
  }
  
  public final void create(HttpServletRequest request) {
    _requestLocal.create(request);
  }

  public final void destroy() {
    _requestLocal.destroy();
  }
  
  protected T get() {
    return _requestLocal.get();
  }

}
