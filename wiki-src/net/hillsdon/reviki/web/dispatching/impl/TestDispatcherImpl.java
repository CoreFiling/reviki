package net.hillsdon.reviki.web.dispatching.impl;

import java.net.URI;

import junit.framework.TestCase;

public class TestDispatcherImpl extends TestCase {

  private DispatcherImpl _dispatcher;

  protected void setUp() throws Exception {
    _dispatcher = new DispatcherImpl(null, null, null, null, null, null);
  }

  public void testStripJsessionid() throws Exception {
    // Test that the path obtained from _dispatcher deals with encoded characters properly and strips the jssesionid parameter
    assertEquals(_dispatcher.getStrippedPath(URI.create("http://example.com/reviki/foo/bar")), "/reviki/foo/bar");
    assertEquals(_dispatcher.getStrippedPath(URI.create("/reviki/foo/bar/\u20ac;jsessionid=2313")), "/reviki/foo/bar/\u20ac");
    assertEquals(_dispatcher.getStrippedPath(URI.create("/reviki/foo/bar/A%20B")), "/reviki/foo/bar/A B");
    assertEquals(_dispatcher.getStrippedPath(URI.create("/reviki/foo/bar/A%3FB")), "/reviki/foo/bar/A?B");
    assertEquals(_dispatcher.getStrippedPath(URI.create("/reviki/foo/bar/A+B;jsessionid=2313?rename")), "/reviki/foo/bar/A+B");
  }
}
