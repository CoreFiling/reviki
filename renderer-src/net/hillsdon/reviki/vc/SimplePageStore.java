package net.hillsdon.reviki.vc;

import java.util.Collection;
import java.util.Set;

/**
 * A simplified PageStore.
 *
 * @author msw
 */
public interface SimplePageStore {
  /**
   * @return A list of all pages.
   */
  Set<PageReference> list() throws PageStoreException;

  /**
   * @param page A page name.
   * @return Whether page exists.
   */
  boolean exists(PageReference page) throws PageStoreException;

  /**
   * All attachments for the given page.
   *
   * @param ref A page name.
   * @return File names of all attachments.
   */
  Collection<? extends SimpleAttachmentHistory> listAttachments(PageReference ref) throws PageStoreException;

  /**
   * @param ref Page.
   * @param attachment Attachment on that page.
   * @param revision The revision to fetch, -1 for head.
   * @throws NotFoundException If the attachment is not present in the given revision.
   */
  byte[] attachmentBytes(PageReference ref, String attachment, long revision) throws PageStoreException, NotFoundException;
}
