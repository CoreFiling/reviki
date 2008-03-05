package net.hillsdon.svnwiki.vc;

/**
 * A verson control based store of wiki pages.
 * 
 * @author mth
 */
public interface PageStore {

  PageInfo get(String path) throws PageStoreException;

  void set(String path, long baseRevision, String content) throws PageStoreException;

}
