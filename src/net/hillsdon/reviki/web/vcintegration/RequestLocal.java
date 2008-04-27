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

import net.hillsdon.fij.accessors.Accessor;
import net.hillsdon.fij.core.Transform;

/**
 * A thread-local bound to the request lifecycle.
 * 
 * @author mth
 *
 * @param <T> Class.
 */
public class RequestLocal<T> implements Accessor<T>, RequestLifecycleAware {

  private final ThreadLocal<T> _threadLocal = new ThreadLocal<T>();
  private final Transform<HttpServletRequest, T> _factory;

  public RequestLocal(final Transform<HttpServletRequest, T> factory) {
    _factory = factory;
  }

  public void create(final HttpServletRequest request) {
    _threadLocal.set(_factory.transform(request));
  }
  
  public void destroy() {
    _threadLocal.set(null);
  }

  public T get() {
    return _threadLocal.get();
  }

}
