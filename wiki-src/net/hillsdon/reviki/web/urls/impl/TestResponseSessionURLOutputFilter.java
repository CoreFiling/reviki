/**
 * Copyright 2010 Matthew Hillsdon
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

import net.hillsdon.reviki.web.common.MockHttpServletRequest;
import junit.framework.TestCase;

/**
 * Test for {@link ResponseSessionURLOutputFilter}.
 * 
 * @author js
 */
public class TestResponseSessionURLOutputFilter extends TestCase {
  public void testStandard() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContextPath("/reviki");
    request.setRequestURL("http://www.example.com/reviki/some/page");
    request.setRequestURI("/reviki/some/page");
    
    // Shouldn't require response
    ResponseSessionURLOutputFilter filter = new ResponseSessionURLOutputFilter(request, null);
    
    // Same URL
    assertEquals(true, filter.shouldAppendSession("http://www.example.com/reviki/some/page"));
    // Explicit port
    assertEquals(true, filter.shouldAppendSession("http://www.example.com:80/reviki/some/page"));
    // Deeper URL
    assertEquals(true, filter.shouldAppendSession("http://www.example.com/reviki/some/page/deeper"));
    // Shallower URL (but within context path)
    assertEquals(true, filter.shouldAppendSession("http://www.example.com/reviki/some"));
    assertEquals(true, filter.shouldAppendSession("http://www.example.com/reviki/another"));
    
    // Wrong scheme
    assertEquals(false, filter.shouldAppendSession("https://www.example.com/reviki/some/page"));
    // Wrong port
    assertEquals(false, filter.shouldAppendSession("http://www.example.com:8080/reviki/some/page"));
    // Wrong host
    assertEquals(false, filter.shouldAppendSession("http://www.xample.com/reviki/some/page"));
    // Wrong context path
    assertEquals(false, filter.shouldAppendSession("http://www.example.com/vqwiki/some/page"));
    // Typical external link
    assertEquals(false, filter.shouldAppendSession("http://www.google.com"));
    // Invalid absolute URL
    assertEquals(false, filter.shouldAppendSession("http:/www.example.com/reviki/some/page"));
    assertEquals(false, filter.shouldAppendSession("http:www.example.com/reviki/some/page"));
  }

  public void testRootContext() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContextPath(""); // Docs explicitly state "" not "/"
    request.setRequestURL("http://www.example.com/some/page");
    request.setRequestURI("/some/page");
    
    // Shouldn't require response
    ResponseSessionURLOutputFilter filter = new ResponseSessionURLOutputFilter(request, null);
    
    // Same URL
    assertEquals(true, filter.shouldAppendSession("http://www.example.com/some/page"));
    // Deeper URL
    assertEquals(true, filter.shouldAppendSession("http://www.example.com/some/page/deeper"));
    // Shallower URL
    assertEquals(true, filter.shouldAppendSession("http://www.example.com/some"));
    // Root with "/"
    assertEquals(true, filter.shouldAppendSession("http://www.example.com/"));
    // Root without "/"
    assertEquals(true, filter.shouldAppendSession("http://www.example.com/"));
  }
  
  public void testRelative() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContextPath("/reviki");
    request.setRequestURL("http://www.example.com/reviki/some/page");
    request.setRequestURI("/reviki/some/page");
    
    // Shouldn't require response
    ResponseSessionURLOutputFilter filter = new ResponseSessionURLOutputFilter(request, null);
    
    // Host-relative
    assertEquals(true, filter.shouldAppendSession("/reviki/some/page"));
    // Path-relative
    assertEquals(true, filter.shouldAppendSession("otherpage"));
    // Malformed URLs shouldn't be mistaken for relative
    assertEquals(false, filter.shouldAppendSession("http:/somepage"));
    assertEquals(false, filter.shouldAppendSession("http:somepage"));
  }
  
}

