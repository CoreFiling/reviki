package net.hillsdon.svnwiki.web;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;

public class ConsumedPath {

  private Iterator<String> _iterator;

  public ConsumedPath(final HttpServletRequest request) {
    this(request.getRequestURI(), request.getContextPath());
  }

  public ConsumedPath(final String requestURI, final String contextPath) {
    String path = requestURI.substring(contextPath.length() + 1);
    int query = path.lastIndexOf('?');
    if (query != -1) {
      path = path.substring(0, query);
    }
    _iterator = Arrays.asList(path.split("/")).iterator();
  }

  public String next() {
    try {
      return _iterator.next();
    }
    catch (NoSuchElementException ex) {
      return null;
    }
  }

  public boolean hasNext() {
    return _iterator.hasNext();
  }
  
}
