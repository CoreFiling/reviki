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
   * Edit pages by calling this method.  They don't need to exist yet.
   * 
   * @param path A page name.s
   * @param baseRevision Used to check the edited copy was the latest.
   * @param content The new content.
   * @throws InterveningCommitException If base revision is not the same as the head at the point immediately prior to the commit.
   * @throws PageStoreException If something else goes wrong.
   */
  void set(String path, long baseRevision, String content) throws InterveningCommitException, PageStoreException;

}
