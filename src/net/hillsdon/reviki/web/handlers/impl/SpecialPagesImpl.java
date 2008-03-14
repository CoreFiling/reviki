package net.hillsdon.reviki.web.handlers.impl;

import java.util.LinkedHashMap;

import net.hillsdon.reviki.web.handlers.SpecialPage;
import net.hillsdon.reviki.web.handlers.SpecialPages;

/**
 * Aggregates the special pages.
 * 
 * @author mth
 */
public class SpecialPagesImpl extends LinkedHashMap<String, SpecialPage> implements SpecialPages {
  
  private static final long serialVersionUID = 1L;

  public SpecialPagesImpl(final SpecialPage... specialPages) {
    for (SpecialPage page : specialPages) {
      put(page.getName(), page);
    }
  }
  
}
