package net.hillsdon.svnwiki.web.dispatching;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.configuration.ConfigurationLocation;
import net.hillsdon.svnwiki.configuration.PerWikiInitialConfiguration;
import net.hillsdon.svnwiki.vc.NotFoundException;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.RequestHandler;

public class WikiChoice implements RequestHandler {

  private final Map<String, RequestHandler> _wikis = new ConcurrentHashMap<String, RequestHandler>();

  private final ConfigurationLocation _configuration;

  public class ConfigureWikiHandler implements RequestHandler {
    
    private final PerWikiInitialConfiguration _perWikiConfiguration;

    public ConfigureWikiHandler(final PerWikiInitialConfiguration perWikiConfiguration) {
      _perWikiConfiguration = perWikiConfiguration;
    }

    public void handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
      if ("GET".equals(request.getMethod())) {
        showView(request, response);
      }
      else if ("POST".equals(request.getMethod())) {
        String url = request.getParameter("url");
        try {
          _perWikiConfiguration.setUrl(url);
          _perWikiConfiguration.save();
          if (_perWikiConfiguration.isComplete()) {
            addWiki(_perWikiConfiguration);
          }
          response.sendRedirect(request.getRequestURI());
        }
        catch (IllegalArgumentException ex) {
          request.setAttribute("error", ex.getMessage());
          showView(request, response);
        }
      }
    }
    
    private void showView(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
      request.setAttribute("configuration", _perWikiConfiguration);
      request.getRequestDispatcher("/WEB-INF/templates/Configuration.jsp").include(request, response);
    }
    
  }
  
  public WikiChoice(final ConfigurationLocation configuration) {
    _configuration = configuration;
  }

  private WikiHandler addWiki(final PerWikiInitialConfiguration configuration) {
    WikiHandler handler = new WikiHandler(configuration);
    _wikis.put(configuration.getWikiName(), handler);
    return handler;
  }

  public void handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    // Skip 'pages' path segment
    path.next();
    request.setAttribute("wikiName", path.peek());
    RequestHandler handler = getWikiHandler(path);
    handler.handle(path, request, response);
  }

  private RequestHandler getWikiHandler(final ConsumedPath path) throws NotFoundException {
    String wikiName = path.next();
    if (wikiName == null) {
      throw new NotFoundException();
    }
    RequestHandler wiki = _wikis.get(wikiName);
    boolean reconfigure = "ConfigSvnLocation".equals(path.peek());
    if (wiki == null || reconfigure) {
      PerWikiInitialConfiguration configuration = new PerWikiInitialConfiguration(_configuration, wikiName);
      // At the moment we lazily install wiki handlers.  Fix this when adding a wiki list?
      if (configuration.isComplete() && !reconfigure) {
        return addWiki(configuration);
      }
      return new ConfigureWikiHandler(configuration);
    }
    return wiki;
  }

}
