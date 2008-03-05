package net.hillsdon.svnwiki.web;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;

public class ConsumedPath {

  private ListIterator<String> _iterator;
  private List<String> _list;

  public ConsumedPath(final HttpServletRequest request) {
    this(request.getRequestURI(), request.getContextPath());
  }

  public ConsumedPath(final String requestURI, final String contextPath) {
    String path = requestURI.substring(contextPath.length() + 1);
    int query = path.lastIndexOf('?');
    if (query != -1) {
      path = path.substring(0, query);
    }
    _list = Arrays.asList(path.split("/"));
    _iterator = _list.listIterator();
  }

  public String peek() {
    try {
      return _list.get(_iterator.nextIndex());
    }
    catch (IndexOutOfBoundsException ex) {
      return null;
    }
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
