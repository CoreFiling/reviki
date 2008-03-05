package net.hillsdon.svnwiki.vc;

/**
 * The result of a listing.
 * 
 * @author mth
 */
public class PageStoreEntry {

  private final String _name;
  private final long _revision;
  
  public PageStoreEntry(final String name, final long revision) {
    _name = name;
    _revision = revision;
  }

  public String getName() {
    return _name;
  }
  
  public long getRevision() {
    return _revision;
  }
  
}
