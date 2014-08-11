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
package net.hillsdon.reviki.web.urls.impl;

import junit.framework.TestCase;
import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.web.common.MockHttpServletRequest;

/**
 * Test for {@link WikiUrlsImpl}.
 * 
 * @author mth
 */
public class TestApplicationUrls extends TestCase {

  private MockHttpServletRequest _request;
  private ApplicationUrlsImpl _urls;
  private DeploymentConfiguration _deploymentConfiguration;

  @Override
  protected void setUp() throws Exception {
    _request = new MockHttpServletRequest();
    _request.setContextPath("/reviki");
    _request.setRequestURL("http://www.example.com/reviki/some/page");
    _request.setRequestURI("/reviki/some/page");
    _urls = new ApplicationUrlsImpl(_request, _deploymentConfiguration);
  }

  public void testUrl() {
    assertEquals("http://www.example.com/reviki/resources/foo", _urls.url("/resources/foo"));
  }
  
  public void testList() {
    assertEquals("http://www.example.com/reviki/list", _urls.list());
  }
  
}
