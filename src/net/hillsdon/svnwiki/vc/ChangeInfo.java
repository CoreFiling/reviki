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
  
  public ChangeInfo(String page, String user, Date date) {
    _page = page;
    _user = user;
    _date = date;
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
  
}
