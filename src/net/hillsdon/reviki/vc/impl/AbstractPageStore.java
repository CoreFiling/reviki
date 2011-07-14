package net.hillsdon.reviki.vc.impl;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;

public abstract class AbstractPageStore implements PageStore {

  public boolean exists(PageReference page) throws PageStoreException {
    return list().contains(page);
  }

}