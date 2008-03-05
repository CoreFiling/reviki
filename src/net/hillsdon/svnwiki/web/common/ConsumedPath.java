package net.hillsdon.svnwiki.web.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
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
    _list = new ArrayList<String>();
    for (String part : path.split("/")) {
      try {
        _list.add(URLDecoder.decode(part, "UTF-8"));
      }
      catch (UnsupportedEncodingException ex) {
        _list.add(part);
      }
    }
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
