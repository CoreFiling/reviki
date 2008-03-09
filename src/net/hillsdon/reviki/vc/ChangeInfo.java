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
package net.hillsdon.reviki.vc;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Describes a change.
 * 
 * @author mth
 */
public class ChangeInfo {

  private static final Pattern TAGS_THEN_CONTENT = Pattern.compile("(\\[(.*?)\\])*(.*)", Pattern.DOTALL);
  
  public static final String NO_COMMENT_MESSAGE_TAG = "[reviki commit]";
  public static final String MINOR_EDIT_MESSAGE_TAG = "[minor edit]\n";
  
  private final String _name;
  private final String _page;
  private final String _user;
  private final Date _date;
  private final long _revision;
  private final String _commitMessage;
  private final StoreKind _kind;
  private final ChangeType _changeType;
  
  public ChangeInfo(final String page, final String name, final String user, final Date date, final long revision, final String commitMessage, StoreKind kind, ChangeType changeType) {
    _page = page;
    _name = name;
    _user = user;
    _date = date;
    _revision = revision;
    _kind = kind;
    _changeType = changeType;
    _commitMessage = commitMessage.trim();
  }
  
  public ChangeType getChangeType() {
    return _changeType;
  }

  public String getName() {
    return _name;
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

  public boolean isMinorEdit() {
    return stripFinalURL().contains(MINOR_EDIT_MESSAGE_TAG);
  }
  
  public String getDescription() {
    Matcher matcher = TAGS_THEN_CONTENT.matcher(stripFinalURL());
    matcher.matches();
    String comment = matcher.group(3).trim();
    return comment.length() == 0 ? "None" : comment;
  }

  private String stripFinalURL() {
    int nl = _commitMessage.lastIndexOf("\n");
    if (nl != -1) {
      String lastLine = _commitMessage.substring(nl + 1).trim();
      if (lastLine.startsWith("http://") || lastLine.startsWith("https://")) {
        return _commitMessage.substring(0, nl).trim();
      }
    }
    return _commitMessage;
  }
  
  public String getCommitMessage() {
    return _commitMessage;
  }
  
  public StoreKind getKind() {
    return _kind;
  }
  
  public boolean isAttachment() {
    return getKind() == StoreKind.ATTACHMENT;
  }
  
  public boolean isDeletion() {
    return getChangeType() == ChangeType.DELETED;
  }
  
  @Override
  public String toString() {
    return _changeType.toString() + " of " + _page;
  }
  
}
