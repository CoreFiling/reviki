package net.hillsdon.reviki.web.dispatching.impl;

import junit.framework.TestCase;
import net.hillsdon.reviki.web.common.MockHttpServletRequest;

public class TestBaseUrlFilter extends TestCase {

  private MockHttpServletRequest _request;

  @Override
  protected void setUp() throws Exception {
    _request = new MockHttpServletRequest();
    _request.setContextPath("/reviki");
    _request.setRequestURL("http://www.example.com/reviki/some/page");
    _request.setRequestURI("/reviki/some/page");
  }
  
  public void testDerivesBaseURL() {
    assertEquals("http://www.example.com/reviki", BaseUrlFilter.getBaseUrl(_request));
  }
  
}
