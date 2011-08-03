package net.hillsdon.reviki.web.taglib;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.VersionedPageInfo;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.web.urls.ApplicationUrls;
import net.hillsdon.reviki.web.urls.InternalLinker;

public class IconUrlTag extends TagSupport {

  private static final long serialVersionUID = 1L;
  private String _name;
  private boolean _session = true;

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public boolean isSession() {
    return _session;
  }

  public void setSession(final boolean session) {
    _session = session;
  }

  public int doStartTag() throws JspException {
    try {
      final String configIconsPage = "ConfigIcons";
      final ServletRequest request = pageContext.getRequest();
      final ApplicationUrls application = (ApplicationUrls) request.getAttribute(ApplicationUrls.KEY);
      final InternalLinker linker = (InternalLinker) request.getAttribute("internalLinker");
      final PageStore store = (PageStore) request.getAttribute("pageStore");
      JspWriter out = pageContext.getOut();
      try {
        VersionedPageInfo iconPage = store.get(new PageReferenceImpl(configIconsPage), -1);
        if(store != null && !iconPage.isNewPage()) {
          out.write(linker.uri(configIconsPage).toASCIIString() + "/attachments/" + _name);
        }
        else {
          out.write(Escape.html(application.resource(_name)));
        }
      }
      catch (Exception e) {
        out.write(Escape.html(application.resource(_name)));
      }
    }
    catch (IOException e) {
      throw new JspException(e);
    }
    return SKIP_BODY;
  }
}
