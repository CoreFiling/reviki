/**
 * Copyright 2008 Matthew Hillsdon
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
package net.hillsdon.reviki.vc.impl;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import net.hillsdon.reviki.vc.VersionedPageInfo;

/**
 * Contents at a particular revision.
 *
 * @author mth
 */
public class VersionedPageInfoImpl extends PageInfoImpl implements VersionedPageInfo {

  public static final long UNCOMMITTED = -2;
  public static final long DELETED = -3;

  private final long _revision;
  private final long _lastChangedRevision;
  private final String _lastChangedAuthor;
  private final Date _lastChangedDate;

  private final String _lockedBy;
  private final String _lockToken;
  private final Date _lockedSince;

  public VersionedPageInfoImpl(final String wiki, final String path, final String content, final long revision, final long lastChangedRevision, final String lastChangedAuthor, final Date lastChangedDate, final String lockedBy, final String lockToken, Date lockedSince) {
    this(wiki, path, content, revision, lastChangedRevision, lastChangedAuthor, lastChangedDate, lockedBy, lockToken, lockedSince, new LinkedHashMap<String, String>());
  }

  public VersionedPageInfoImpl(final String wiki, final String path, final String content, final long revision, final long lastChangedRevision, final String lastChangedAuthor, final Date lastChangedDate, final String lockedBy, final String lockToken, Date lockedSince, Map<String, String> attributes) {
    super(wiki, path, content, attributes);
    _revision = revision;
    _lastChangedRevision = lastChangedRevision;
    _lastChangedAuthor = lastChangedAuthor;
    _lastChangedDate = lastChangedDate;
    _lockedBy = lockedBy;
    _lockToken = lockToken;
    _lockedSince = lockedSince;
  }

  public VersionedPageInfoImpl(final VersionedPageInfo pageInfo, String content) {
    super(pageInfo.getWiki(), pageInfo.getPath(), content, pageInfo.getAttributes());
    _revision = pageInfo.getRevision();
    _lastChangedRevision = pageInfo.getLastChangedRevision();
    _lastChangedAuthor = pageInfo.getLastChangedUser();
    _lastChangedDate = pageInfo.getLastChangedDate();
    _lockedBy = pageInfo.getLockedBy();
    _lockToken = pageInfo.getLockToken();
    _lockedSince = pageInfo.getLockedSince();
  }

  public VersionedPageInfoImpl(final VersionedPageInfo pageInfo, Map<String, String> attributes) {
    super(pageInfo.getWiki(), pageInfo.getPath(), pageInfo.getContent(), attributes);
    _revision = pageInfo.getRevision();
    _lastChangedRevision = pageInfo.getLastChangedRevision();
    _lastChangedAuthor = pageInfo.getLastChangedUser();
    _lastChangedDate = pageInfo.getLastChangedDate();
    _lockedBy = pageInfo.getLockedBy();
    _lockToken = pageInfo.getLockToken();
    _lockedSince = pageInfo.getLockedSince();
  }

  public long getRevision() {
    return _revision;
  }

  public String getRevisionName() {
    if (isNewPage()) {
      return "New";
    }
    return "r" + getLastChangedRevision();
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

  public Date getLockedSince() {
    return _lockedSince;
  }

  public boolean isNewPage() {
    return _revision == UNCOMMITTED || _revision == DELETED;
  }

  public boolean isDeleted() {
    return _revision == DELETED;
  }

  public boolean isNewOrLockedByUser(final String user) {
    return isNewPage() || isLockedByUser(user);
  }

  private boolean isLockedByUser(final String user) {
    final String lockedBy = getLockedBy();
    return lockedBy != null && lockedBy.equals(user);
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

  public VersionedPageInfo withAlternativeContent(final String content) {
    return new VersionedPageInfoImpl(this, content);
  }

  public VersionedPageInfo withAlternativeAttributes(final Map<String, String> attributes) {
    return new VersionedPageInfoImpl(this, attributes);
  }

  public VersionedPageInfo withoutLockToken() {
    return new VersionedPageInfoImpl(
      super.getWiki(),
      super.getPath(),
      super.getContent(),
      _revision,
      _lastChangedRevision,
      _lastChangedAuthor,
      _lastChangedDate,
      _lockedBy,
      "",
      _lockedSince);
  }
}