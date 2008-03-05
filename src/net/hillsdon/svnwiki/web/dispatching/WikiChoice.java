package net.hillsdon.svnwiki.web.dispatching;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.configuration.DeploymentConfiguration;
import net.hillsdon.svnwiki.configuration.PerWikiInitialConfiguration;
import net.hillsdon.svnwiki.vc.NotFoundException;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.RequestHandler;

public class WikiChoice implements RequestHandler {

  private final Map<PerWikiInitialConfiguration, RequestHandler> _wikis = new ConcurrentHashMap<PerWikiInitialConfiguration, RequestHandler>();
  private final DeploymentConfiguration _configuration;
  private final ServletContext _servletContext;

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
  
  public WikiChoice(ServletContext servletContext, final DeploymentConfiguration configuration) {
    _servletContext = servletContext;
    _configuration = configuration;
  }

  private WikiHandler addWiki(final PerWikiInitialConfiguration configuration) {
    WikiHandler handler = new WikiHandler(configuration, _servletContext.getContextPath());
    _wikis.put(configuration, handler);
    return handler;
  }

  public void handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    PerWikiInitialConfiguration configuration = getWikiConfiguration(path);
    request.setAttribute("wikiName", configuration.getWikiName());
    request.setAttribute("wikiIsValid", configuration.isComplete());

    RequestHandler handler = getWikiHandler(configuration, path);
    handler.handle(path, request, response);
  }

  private RequestHandler getWikiHandler(final PerWikiInitialConfiguration configuration, final ConsumedPath path) throws NotFoundException {
    RequestHandler wiki = _wikis.get(configuration);
    boolean reconfigure = "ConfigSvnLocation".equals(path.peek());
    if (wiki == null || reconfigure) {
      // At the moment we lazily install wiki handlers.  Fix this when adding a wiki list?
      if (configuration.isComplete() && !reconfigure) {
        return addWiki(configuration);
      }
      return new ConfigureWikiHandler(configuration);
    }
    return wiki;
  }

  private PerWikiInitialConfiguration getWikiConfiguration(final ConsumedPath path) throws NotFoundException {
    boolean asDefault = false;
    String wikiName = path.peek();
    if (wikiName == null) {
      throw new NotFoundException();
    }
    if (wikiName.matches("^\\p{Lu}.*")) {
      // It's not a subwiki, rather a request for a page in the default wiki.
      asDefault = true;
      wikiName = _configuration.getDefaultWiki();
      if (wikiName == null) {
        throw new NotFoundException();
      }
    }
    else {
      path.next();
    }
    
    return new PerWikiInitialConfiguration(_configuration, asDefault ? null : wikiName, wikiName);
  }

}
