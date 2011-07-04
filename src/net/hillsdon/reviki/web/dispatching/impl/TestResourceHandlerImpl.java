package net.hillsdon.reviki.web.dispatching.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.MockHttpServletRequest;
import net.hillsdon.reviki.web.common.MockRequestDispatcher;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.urls.impl.ApplicationUrlsImpl;
import junit.framework.TestCase;

public class TestResourceHandlerImpl extends TestCase {

  private MockHttpServletRequest _request;
  private ApplicationUrlsImpl _urls;
  private DeploymentConfiguration _deploymentConfiguration;
  private ResourceHandlerImpl _handler;
  private HttpServletResponse _response;
  private MockRequestDispatcher _dispatcher;

  @Override
  protected void setUp() throws Exception {
    _dispatcher = new MockRequestDispatcher();
    _request = new MockHttpServletRequest();
    _request.setContextPath("/reviki");
    _request.setRequestURL("http://www.example.com/reviki/some/page");
    _request.setRequestURI("/reviki/some/page");
    
    _response = null;
    _urls = new ApplicationUrlsImpl(_request, _deploymentConfiguration);
    _handler = new ResourceHandlerImpl();
  }

  public void testResource() throws Exception {
    final ConsumedPath path = new ConsumedPath("/foo", "");
    _request.setRequestDispatcher("/resources/foo", _dispatcher);
    final View view = _handler.handle(path, _request, _response);
    view.render(_request, _response);
    assertEquals(_request, _dispatcher.getForwardedRequests().get(0));
  }
  
  public void testDeepResource() throws Exception {
    final ConsumedPath path = new ConsumedPath("/foo/bar", "");
    _request.setRequestDispatcher("/resources/foo/bar", _dispatcher);
    final View view = _handler.handle(path, _request, _response);
    view.render(_request, _response);
    assertEquals(_request, _dispatcher.getForwardedRequests().get(0));
  }
}
