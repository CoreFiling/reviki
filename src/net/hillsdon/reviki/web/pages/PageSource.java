package net.hillsdon.reviki.web.pages;

import net.hillsdon.reviki.vc.PageReference;

public interface PageSource {

  Page get(PageReference pageReference);

}
