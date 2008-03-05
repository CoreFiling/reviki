package net.hillsdon.svnwiki.vc;

import java.io.IOException;
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
  Collection<String> list() throws PageStoreException;

  /**
   * @return Names of recently changed pages.
   * @throws PageStoreException If something goes wrong.
   */
  List<ChangeInfo> recentChanges() throws PageStoreException;

  /**
   * @param path The path.
   * @return Changes, in most recent first.
   * @throws PageStoreException If something goes wrong.
   */
  List<ChangeInfo> history(String path) throws PageStoreException;
  
  /**
   * The page info may represent a page that doesn't exist yet,
   * check the revision number.
   * 
   * @param path A page name.
   * @param revisoin Revision, -1 for head.
   * @return Information (including current content) for the page.
   * @throws PageStoreException If something goes wrong.
   */
  PageInfo get(String path, long revision) throws PageStoreException;
  
  /**
   * The page info may represent a page that doesn't exist yet,
   * check the revision number.
   * 
   * If the page is an existing page then try to take out a lock.
   * 
   * Check the lock owner on the returning page, you may not get the
   * lock.
   * 
   * @param path A page name.
   * @return Information (including current content) for the page.
   * @throws PageStoreException If something goes wrong.
   */
  PageInfo tryToLock(String path) throws PageStoreException;

  /**
   * @param page Page.
   * @param lockToken TODO
   * @return Information (including current content) for the page.
   * @throws PageStoreException If something goes wrong.
   */
  void unlock(String page, String lockToken) throws PageStoreException;
  
  /**
   * Edit pages by calling this method.  They don't need to exist yet.
   * 
   * @param path A page name.s
   * @param lockToken TODO
   * @param baseRevision Used to check the edited copy was the latest.
   * @param content The new content.
   * @param commitMessage TODO
   * @throws InterveningCommitException If base revision is not the same as the head at the point immediately prior to the commit.
   * @throws PageStoreException If something else goes wrong.
   */
  void set(String path, String lockToken, long baseRevision, String content, String commitMessage) throws InterveningCommitException, PageStoreException;

  /**
   * Add an attachment to a page.
   * 
   * @param page The page name.
   * @param storeName The name to store the attachment as.
   * @param baseRevision TODO
   * @param in Data read from here.
   * @throws PageStoreException If something goes wrong. 
   */
  void attach(String page, String storeName, long baseRevision, InputStream in) throws PageStoreException;

  /**
   * @param page A page name.
   * @return File names of all attachments.
   * @throws PageStoreException If something goes wrong. 
   */
  Collection<PageStoreEntry> attachments(String page) throws PageStoreException;

  /**
   * @param page Page.
   * @param attachment Attachment on that page.
   * @param sink Attachment is written here.
   * @throws PageStoreException On failure.
   * @throws IOException 
   */
  void attachment(String page, String attachment, ContentTypedSink sink) throws PageStoreException, NotFoundException;
  
}
