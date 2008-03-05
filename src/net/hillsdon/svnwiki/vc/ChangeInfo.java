package net.hillsdon.svnwiki.vc;

import java.util.Date;

/**
 * Describes a change.
 * 
 * @author mth
 */
public class ChangeInfo {

  private final String _page;
  private final String _user;
  private final Date _date;
  private final long _revision;
  
  public ChangeInfo(String page, String user, Date date, long revision) {
    _page = page;
    _user = user;
    _date = date;
    _revision = revision;
  }

  public String getPage() {
    return _page;
  }

  public String getUser() {
    return _user;
  }

  public Date getDate() {
    return _date;
  }
  
  public long getRevision() {
    return _revision;
  }
  
}
