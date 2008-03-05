package net.hillsdon.svnwiki.web.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.web.common.ConsumedPath;

/**
 * Interface for request handling relating to a specific wiki page.
 * 
 * @author mth
 */
public interface PageRequestHandler {

  void handlePage(ConsumedPath path, HttpServletRequest request, HttpServletResponse response, PageReference page) throws Exception;

}
