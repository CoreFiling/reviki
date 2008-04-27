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

public class RequestLifecycleAwareManagerImpl implements RequestLifecycleAwareManager {

  private final RequestLifecycleAware[] _requestLifecycleAware;

  public RequestLifecycleAwareManagerImpl(final RequestLifecycleAware... requestLifecycleAware) {
    _requestLifecycleAware = requestLifecycleAware;
  }
  
  public void requestStarted(final HttpServletRequest request) {
    for (RequestLifecycleAware aware : _requestLifecycleAware) {
      aware.create(request);
    }
  }

  public void requestComplete() {
    for (RequestLifecycleAware aware : _requestLifecycleAware) {
      aware.destroy();
    }
  }
  
}
