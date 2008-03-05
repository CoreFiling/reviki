package net.hillsdon.svnwiki.vc;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * A verson control based store of wiki pages with attachments.
 * 
 * All methods throw {@link PageStoreException} if they fail,
 * sometimes more informative subclasses individually documented.
 * 
 * If there is an authentication failure {@link PageStoreAuthenticationException}
 * will be thrown.
 * 
 * @author mth
 */
public interface PageStore {
  
  /**
   * @return The latest (highest) revision number.
   */
  long getLatestRevision() throws PageStoreAuthenticationException, PageStoreException;
  
  /**
   * @return A list of all pages.
   */
  Collection<PageReference> list() throws PageStoreException;

  /**
   * @param limit Maximum number of entries to return.
   * @return Names of recently changed pages.
   */
  List<ChangeInfo> recentChanges(int limit) throws PageStoreException;

  /**
   * @param ref The path.
   * @return Changes, most recent first.
   */
  List<ChangeInfo> history(PageReference ref) throws PageStoreException;

  /**
   * TODO: Refactor to return ChangeInfo and unify with recentChanges/history somehow.
   * 
   * @param start Start revision (inclusive). 
   * @param end  End revision (inclusive).
   * @return Pages changed after that revision.
   */
  Collection<PageReference> getChangedBetween(long start, long end) throws PageStoreException;

  /**
   * The page info may represent a page that doesn't exist yet (or has been deleted,
   * we don't currently distinguish) check {@link PageInfo#isNew()}.
   * 
   * @param ref A page name.
   * @param revision Revision, -1 for head.
   * @return Information (including current content) for the page.
   */
  PageInfo get(PageReference ref, long revision) throws PageStoreException;
  
  /**
   * If the page is an existing page then try to take out a lock.
   * 
   * Check the lock owner on the returning page, you may not get the
   * lock.
   * 
   * For further documentation see {@link #get(PageReference, long)}.
   * 
   * @param ref A page name.
   * @return Information (including current content) for the page.
   */
  PageInfo tryToLock(PageReference ref) throws PageStoreException;

  /**
   * @param ref Page.
   * @param lockToken The token for the lock, see {@link PageInfo#getLockToken()}. 
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
   * @return The new revision number.
   * @throws InterveningCommitException If base revision is not the same as the head at the point immediately prior to the commit.
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
   */
  void attach(PageReference ref, String storeName, long baseRevision, InputStream in, String commitMessage) throws PageStoreException;

  /**
   * All attachments for the given page, with information on previous versions of the same.
   * 
   * @param ref A page name.
   * @return File names of all attachments.
   */
  Collection<AttachmentHistory> attachments(PageReference ref) throws PageStoreException;

  /**
   * @param ref Page.
   * @param attachment Attachment on that page.
   * @param revision The revision to fetch, -1 for head.
   * @param sink Attachment is written here.
   * @throws NotFoundException If the attachment is not present in the given revision. 
   */
  void attachment(PageReference ref, String attachment, long revision, ContentTypedSink sink) throws PageStoreException, NotFoundException;

}
