package net.hillsdon.svnwiki.web.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.wiki.InternalLinker;

/**
 * Uses an {@link InternalLinker} to create links to wiki pages.
 * 
 * @copyright
 * @author mth
 */
public class WikiUrlTag extends TagSupport {

  private static final long serialVersionUID = 1L;
  private String _page;

  public String getPage() {
    return _page;
  }

  public void setPage(final String page) {
    _page = page;
  }

  public int doStartTag() throws JspException {
    try {
      InternalLinker linker = (InternalLinker) pageContext.getRequest().getAttribute("internalLinker");
      JspWriter out = pageContext.getOut();
      out.write(Escape.html(linker.url(getPage())));
    }
    catch (IOException e) {
      throw new JspException(e);
    }
    return SKIP_BODY;
  }
  
}

