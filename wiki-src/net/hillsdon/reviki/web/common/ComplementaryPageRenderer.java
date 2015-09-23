package net.hillsdon.reviki.web.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.VersionedPageInfo;
import net.hillsdon.reviki.web.urls.impl.ResponseSessionURLOutputFilter;
import net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences;
import net.hillsdon.reviki.wiki.MarkupRenderer;

/**
 * A class to render complementary content pages.
 * i.e ConfigHeader, ConfigFooter and ConfigSideBar.
 *
 * The methods on this class are intended to be used as
 * properties in JSP templates. Each complementary page
 * is only rendered if its property is used by the template.
 * This avoids unnecessary rendering.
 *
 * @author pwc
 */
public class ComplementaryPageRenderer {
  private final MarkupRenderer<String> _renderer;
  private final PageStore _pageStore;
  private final HttpServletRequest _request;
  private final HttpServletResponse _response;

  public ComplementaryPageRenderer(final HttpServletRequest request, final HttpServletResponse response, final MarkupRenderer<String> renderer, final PageStore pageStore) {
    _renderer = renderer;
    _pageStore = pageStore;
    _request = request;
    _response = response;
  }

  /**
   * Render the latest revision of a page.
   *
   * @param pageRef The page to render.
   * @return The rendered page as a String.
   * @throws PageStoreException
   */
  private String getRenderedPage(final PageReference pageRef) throws PageStoreException {
    VersionedPageInfo page = _pageStore.get(pageRef, -1);
    return _renderer.render(page, new ResponseSessionURLOutputFilter(_request, _response)).get();
  }

  /**
   * Render the latest revision of ConfigHeader.
   *
   * @return The rendered ConfigHeader page as a String.
   * @throws PageStoreException
   */
  public String getRenderedHeader() throws PageStoreException {
    return getRenderedPage(BuiltInPageReferences.PAGE_HEADER);
  }

  /**
   * Render the latest revision of ConfigFooter.
   *
   * @return The rendered ConfigFooter page as a String.
   * @throws PageStoreException
   */
  public String getRenderedFooter() throws PageStoreException {
    return getRenderedPage(BuiltInPageReferences.PAGE_FOOTER);
  }

  /**
   * Render the latest revision of ConfigSideBar.
   *
   * @return The rendered ConfigSideBar page as a String.
   * @throws PageStoreException
   */
  public String getRenderedSideBar() throws PageStoreException {
    return getRenderedPage(BuiltInPageReferences.PAGE_SIDEBAR);
  }
}
