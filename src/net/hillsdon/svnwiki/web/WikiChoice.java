package net.hillsdon.svnwiki.web;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.configuration.InitialConfiguration;
import net.hillsdon.svnwiki.vc.NotFoundException;

public class WikiChoice implements RequestHandler {

  private Map<String, RequestHandler> _wikis = new LinkedHashMap<String, RequestHandler>();
  
  public WikiChoice(final InitialConfiguration configuration) throws IOException {
    _wikis.put("pages", new WikiHandler(configuration));
  }

  public void handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    String wikiName = path.next();
    RequestHandler wiki = _wikis.get(wikiName);
    if (wiki == null) {
      throw new NotFoundException();
    }
    wiki.handle(path, request, response);
  }

}
