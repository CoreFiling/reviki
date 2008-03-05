package net.hillsdon.svnwiki.web.handlers;

import static net.hillsdon.svnwiki.web.common.RequestParameterReaders.getRequiredString;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.InvalidInputException;
import net.hillsdon.svnwiki.web.common.RequestAttributes;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;

public class EditorForPage implements PageRequestHandler {

  public static final String PARAM_PREVIEW = "preview";
  
  private final PageStore _store;
  private final MarkupRenderer _renderer;

  public EditorForPage(final PageStore store, final MarkupRenderer renderer) {
    _store = store;
    _renderer = renderer;
  }

  public void handlePage(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws PageStoreException, IOException, ServletException, InvalidInputException {
    PageInfo pageInfo = _store.tryToLock(page);
    request.setAttribute("pageInfo", pageInfo);
    if (!pageInfo.lockedByUserIfNeeded((String) request.getAttribute(RequestAttributes.USERNAME))) {
      request.setAttribute("flash", "Could not lock the page.");
      request.getRequestDispatcher("/WEB-INF/templates/ViewPage.jsp").include(request, response);
      return;
    }
    else {
      if (request.getParameter(EditorForPage.PARAM_PREVIEW) != null) {
        pageInfo = pageInfo.alternativeContent(getRequiredString(request, SetPage.PARAM_CONTENT));
        request.setAttribute("pageInfo", pageInfo);
        StringWriter out = new StringWriter();
        _renderer.render(pageInfo, pageInfo.getContent(), out);
        request.setAttribute("preview", out.toString());
      }
      request.getRequestDispatcher("/WEB-INF/templates/EditPage.jsp").include(request, response);
    }
  }

}
