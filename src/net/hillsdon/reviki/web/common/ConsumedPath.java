/**
 * Copyright 2007 Matthew Hillsdon
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
package net.hillsdon.reviki.web.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;

public class ConsumedPath {

  public static final ConsumedPath EMPTY = new ConsumedPath(Collections.<String>emptyList());
  
  private ListIterator<String> _iterator;
  private List<String> _list;

  public ConsumedPath(final HttpServletRequest request) {
    this(request.getRequestURI(), request.getContextPath());
  }

  public ConsumedPath(final String requestURI, final String contextPath) {
    this(createPartList(requestURI, contextPath));
  }
  
  /**
   * Suitable for testing.
   * @param parts The remaining parts.
   */
  public ConsumedPath(final List<String> parts) {
    _list = parts;
    _iterator = parts.listIterator();
  }

  private static List<String> createPartList(final String requestURI, final String contextPath) {
    String path = requestURI.substring(contextPath.length() + 1);
    int query = path.lastIndexOf('?');
    if (query != -1) {
      path = path.substring(0, query);
    }
    List<String> parts = new ArrayList<String>();
    for (String part : path.split("/")) {
      try {
        parts.add(URLDecoder.decode(part, "UTF-8"));
      }
      catch (UnsupportedEncodingException ex) {
        parts.add(part);
      }
    }
    return parts;
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
