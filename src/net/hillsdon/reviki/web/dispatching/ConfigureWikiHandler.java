package net.hillsdon.reviki.web.dispatching;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.InvalidInputException;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.RedirectView;
import net.hillsdon.reviki.web.common.RequestHandler;
import net.hillsdon.reviki.web.common.View;

public class ConfigureWikiHandler implements RequestHandler {
  
  private final WikiConfiguration _perWikiConfiguration;
  private final DeploymentConfiguration _configuration;
  private final ActiveWikis _activeWikis;

  public ConfigureWikiHandler(final DeploymentConfiguration configuration, final ActiveWikis activeWikis, final WikiConfiguration perWikiConfiguration) {
    _configuration = configuration;
    _activeWikis = activeWikis;
    _perWikiConfiguration = perWikiConfiguration;
  }

  public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    if (!_configuration.isEditable()) {
      throw new InvalidInputException("Editing of the configuration is currently disabled.");
    }
    request.setAttribute("configuration", _perWikiConfiguration);
    if ("POST".equals(request.getMethod())) {
      String url = request.getParameter("url");
      try {
        _perWikiConfiguration.setUrl(url);
        _perWikiConfiguration.save();
        if (_perWikiConfiguration.isComplete()) {
          _activeWikis.addWiki(_perWikiConfiguration);
        }
        return new RedirectView(request.getRequestURI());
      }
      catch (IllegalArgumentException ex) {
        request.setAttribute("error", ex.getMessage());
        return new JspView("Configuration");
      }
    }
    else {
      return new JspView("Configuration");
    }
  }
  
}