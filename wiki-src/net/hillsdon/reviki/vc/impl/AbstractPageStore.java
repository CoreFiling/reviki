package net.hillsdon.reviki.vc.impl;

import java.util.Collection;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.SimpleAttachmentHistory;

public abstract class AbstractPageStore implements PageStore {

  public boolean exists(PageReference page) throws PageStoreException {
    return list().contains(page);
  }

  public Collection<? extends SimpleAttachmentHistory> listAttachments(PageReference ref) throws PageStoreException {
    return attachments(ref);
  }
}