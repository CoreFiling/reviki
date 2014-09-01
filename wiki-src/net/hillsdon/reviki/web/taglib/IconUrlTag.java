package net.hillsdon.reviki.web.taglib;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.VersionedPageInfo;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.UnknownWikiException;

/**
 * A reference to an icon on ConfigIcons if present, falling back to a common resource.
 *
 * @author mth
 */
public class IconUrlTag extends TagSupport {

  private static final long serialVersionUID = 1L;
  private String _name;

  public String getName() {
    return _name;
  }

  public void setName(final String name) {
    _name = name;
  }

  @Override
  public int doStartTag() throws JspException {
    try {
      pageContext.getOut().write(Escape.html(iconUrl()));
    }
    catch (IOException e) {
      throw new JspException(e);
    }
    return SKIP_BODY;
  }

  private String iconUrl() {
    final String configIconsPage = "ConfigIcons";
    final ServletRequest request = pageContext.getRequest();
    final InternalLinker linker = (InternalLinker) request.getAttribute("internalLinker");
    final PageStore store = (PageStore) request.getAttribute("pageStore");
    try {
      if (store != null && linker != null) {
        VersionedPageInfo iconPage = store.get(new PageReferenceImpl(configIconsPage), -1);
        // This is a bit weird, wouldn't it make sense to check for the icon?
        if(!iconPage.isNewPage()) {
          return linker.uri(configIconsPage).toASCIIString() + "/attachments/" + _name;
        }
      }
    }
    catch (UnknownWikiException e) {
      // Fall through for now.
    }
    catch (URISyntaxException e) {
      // Fall through for now.
    }
    catch (PageStoreException e) {
      // Fall through for now.
    }
    return PageContextAccess.getBestResourceUrls(pageContext).resource(_name);
  }

}
