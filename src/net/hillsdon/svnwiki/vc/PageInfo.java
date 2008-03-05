package net.hillsdon.svnwiki.vc;

/**
 * Contents at a particular revision.
 * 
 * @author mth
 */
public class PageInfo {

  public static final long UNCOMMITTED = -2;

  private final String _path;

  private final String _content;

  private final long _revision;

  public PageInfo(final String path, final String content, final long revision) {
    _path = path;
    _content = content;
    _revision = revision;
  }

  public String getPath() {
    return _path;
  }
  
  public String getContent() {
    return _content;
  }

  public long getRevision() {
    return _revision;
  }

  public String getRevisionName() {
    if (isNew()) {
      return "New";
    }
    return "r" + _revision;
  }

  public boolean isNew() {
    return _revision == UNCOMMITTED;
  }
  
}
