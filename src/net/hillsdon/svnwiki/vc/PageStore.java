package net.hillsdon.svnwiki.vc;

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
  String[] list() throws PageStoreException;

  /**
   * @return Names of recently changed pages.
   * @throws PageStoreException If something goes wrong.
   */
  ChangeInfo[] recentChanges() throws PageStoreException;
  
  /**
   * The page info may represent a page that doesn't exist yet,
   * check the revision number.
   * 
   * @param path A page name.
   * @return Information (including current content) for the page.
   * @throws PageStoreException If something goes wrong.
   */
  PageInfo get(String path) throws PageStoreException;
  
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
   * @throws InterveningCommitException If base revision is not the same as the head at the point immediately prior to the commit.
   * @throws PageStoreException If something else goes wrong.
   */
  void set(String path, String lockToken, long baseRevision, String content) throws InterveningCommitException, PageStoreException;


}
