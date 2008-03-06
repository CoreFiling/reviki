/**
 * Copyright 2007 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.vc;

import java.util.Date;

/**
 * Contents at a particular revision.
 * 
 * @author mth
 */
public class PageInfo extends PageReference {

  public static final long UNCOMMITTED = -2;
  public static final long DELETED = -3;

  private String _content;
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

  public PageInfo(final PageInfo pageInfo) {
    super(pageInfo.getPath());
    _content = pageInfo.getContent();
    _revision = pageInfo.getRevision();
    _lastChangedRevision = pageInfo.getLastChangedRevision();
    _lastChangedAuthor = pageInfo.getLastChangedUser();
    _lastChangedDate = pageInfo.getLastChangedDate();
    _lockedBy = pageInfo.getLockedBy();
    _lockToken = pageInfo.getLockToken();
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
    return _revision == UNCOMMITTED || _revision == DELETED;
  }
  
  public boolean isDeleted() {
    return _revision == DELETED;
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
  
  public PageInfo alternativeContent(final String content) {
    PageInfo other = new PageInfo(this);
    other._content = content;
    return other;
  }
  
}
