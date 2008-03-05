package net.hillsdon.svnwiki.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles a HTTP request.
 * 
 * @author mth
 */
public interface RequestHandler {

  void handle(ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception;
  
}
