package net.hillsdon.svnwiki.vc;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * A verson control based store of wiki pages.
 * 
 * @author mth
 */
public interface PageStore {

  /**
   * @return A list of all pages.
   * @throws PageStoreException If something goes wrong.
   */
  Collection<PageReference> list() throws PageStoreException;

  /**
   * @param limit Maximum number of entries to return.
   * @return Names of recently changed pages.
   * @throws PageStoreException If something goes wrong.
   */
  List<ChangeInfo> recentChanges(int limit) throws PageStoreException;

  /**
   * @param ref The path.
   * @return Changes, most recent first.
   * @throws PageStoreException If something goes wrong.
   */
  List<ChangeInfo> history(PageReference ref) throws PageStoreException;
  
  /**
   * The page info may represent a page that doesn't exist yet,
   * check the revision number.
   * 
   * @param ref A page name.
   * @param revision Revision, -1 for head.
   * @return Information (including current content) for the page.
   * @throws PageStoreException If something goes wrong.
   */
  PageInfo get(PageReference ref, long revision) throws PageStoreException;
  
  /**
   * The page info may represent a page that doesn't exist yet,
   * check the revision number.
   * 
   * If the page is an existing page then try to take out a lock.
   * 
   * Check the lock owner on the returning page, you may not get the
   * lock.
   * 
   * @param ref A page name.
   * @return Information (including current content) for the page.
   * @throws PageStoreException If something goes wrong.
   */
  PageInfo tryToLock(PageReference ref) throws PageStoreException;

  /**
   * @param ref Page.
   * @param lockToken The token for the lock, see {@link PageInfo#getLockToken()}. 
   * @return Information (including current content) for the page.
   * @throws PageStoreException If something goes wrong.
   */
  void unlock(PageReference ref, String lockToken) throws PageStoreException;
  
  /**
   * Edit pages by calling this method.  They don't need to exist yet.
   * 
   * @param ref A page name.s
   * @param lockToken  The token for the edit lock, if any, see {@link PageInfo#getLockToken()}.
   * @param baseRevision Used to check the edited copy was the latest.
   * @param content The new content.
   * @param commitMessage An optional commit message.
   * @return TODO
   * @throws InterveningCommitException If base revision is not the same as the head at the point immediately prior to the commit.
   * @throws PageStoreException If something else goes wrong.
   */
  long set(PageReference ref, String lockToken, long baseRevision, String content, String commitMessage) throws InterveningCommitException, PageStoreException;

  /**
   * Add an attachment to a page.
   * 
   * @param ref The page name.
   * @param storeName The name to store the attachment as.
   * @param baseRevision The base revision.
   * @param in Data read from here.
   * @param commitMessage An optional commit message.
   * @throws PageStoreException If something goes wrong. 
   */
  void attach(PageReference ref, String storeName, long baseRevision, InputStream in, String commitMessage) throws PageStoreException;

  /**
   * All attachments for the given page, with information on previous versions of the same.
   * 
   * @param ref A page name.
   * @return File names of all attachments.
   * @throws PageStoreException If something goes wrong. 
   */
  Collection<AttachmentHistory> attachments(PageReference ref) throws PageStoreException;

  /**
   * @param ref Page.
   * @param attachment Attachment on that page.
   * @param revision The revision to fetch, -1 for head.
   * @param sink Attachment is written here.
   * @throws NotFoundException If the attachment is not present in the given revision. 
   * @throws PageStoreException On other failure.
   */
  void attachment(PageReference ref, String attachment, long revision, ContentTypedSink sink) throws PageStoreException, NotFoundException;

  /**
   * @param revision A revision. 
   * @return Pages changed after that revision.
   * @throws PageStoreException 
   */
  Collection<PageReference> getChangedAfter(long revision) throws PageStoreException;
  
}
