package net.hillsdon.svnwiki.vc;

/**
 * Contents at a particular revision.
 * 
 * @author mth
 */
public class PageInfo {

  private final String _content;

  private final long _revision;

  public PageInfo(final String content, final long revision) {
    _content = content;
    _revision = revision;
  }

  public String getContent() {
    return _content;
  }

  public long getRevision() {
    return _revision;
  }

}
