package net.hillsdon.svnwiki.vc;

import java.util.Date;

/**
 * Contents at a particular revision.
 * 
 * @author mth
 */
public class PageInfo extends PageReference {

  public static final long UNCOMMITTED = -2;

  private final String _content;

  private final long _revision;

  private final long _lastChangedRevision;
  
  private final String _lastChangedAuthor;
  
  private final Date _lastChangedDate;
  
  private final String _lockedBy;

  private final String _lockToken;
  
  public PageInfo(final String path, final String content, final long revision, final long lastChangedRevision, final String lastChangedAuthor, final Date lastChangedDate, final String lockedBy, final String lockToken) {
    super(path);
    _content = content;
    _revision = revision;
    _lastChangedRevision = lastChangedRevision;
    _lastChangedAuthor = lastChangedAuthor;
    _lastChangedDate = lastChangedDate;
    _lockedBy = lockedBy;
    _lockToken = lockToken;
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

  public String getLockedBy() {
    return _lockedBy;
  }
  
  public boolean isLocked() {
    return _lockedBy != null;
  }

  public String getLockToken() {
    return _lockToken;
  }
  
  public boolean isNew() {
    return _revision == UNCOMMITTED;
  }

  public boolean lockedByUserIfNeeded(final String user) {
    return isNew() || user.equals(getLockedBy());
  }
  
  public long getLastChangedRevision() {
    return _lastChangedRevision;
  }
  
  public String getLastChangedUser() {
    return _lastChangedAuthor;
  }
  
  public Date getLastChangedDate() {
    return _lastChangedDate;
  }
  
}
