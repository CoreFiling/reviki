package net.hillsdon.reviki.web.pages.impl;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.web.pages.DefaultPage;
import net.hillsdon.reviki.web.pages.Page;
import net.hillsdon.reviki.web.pages.PageSource;
import net.hillsdon.reviki.web.pages.SpecialPages;

public class PageSourceImpl implements PageSource {

  private final SpecialPages _specialPages;
  private final DefaultPage _defaultPage;

  public PageSourceImpl(final SpecialPages specialPages, final DefaultPage defaultPage) {
    _specialPages = specialPages;
    _defaultPage = defaultPage;
  }

  public Page get(final PageReference pageReference) {
    Page page = _specialPages.get(pageReference.getPath());
    return page != null ? page : _defaultPage;
  }

}
