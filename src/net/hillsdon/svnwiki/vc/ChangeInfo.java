package net.hillsdon.svnwiki.vc;

import java.util.Date;

/**
 * Describes a change.
 * 
 * @author mth
 */
public class ChangeInfo {

  public static final String NO_COMMENT_MESSAGE_TAG = "[svnwiki commit]";
  public static final String MINOR_EDIT_MESSAGE_TAG = "[minor edit]\n";
  private final String _path;
  private final String _user;
  private final Date _date;
  private final long _revision;
  private final String _commitMessage;
  
  public ChangeInfo(final String path, final String user, final Date date, final long revision, final String commitMessage) {
    _path = path;
    _user = user;
    _date = date;
    _revision = revision;
    _commitMessage = commitMessage.trim();
  }

  /**
   * @return The last path component.
   */
  public String getName() {
    return _path.substring(_path.lastIndexOf('/') + 1);
  }

  public String getPath() {
    return _path;
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
    String description = stripFinalURL();
    if (description.contains(MINOR_EDIT_MESSAGE_TAG)) {
      return description.substring(MINOR_EDIT_MESSAGE_TAG.length());
    }
    if (description.contains(NO_COMMENT_MESSAGE_TAG)) {
      return "None";
    }
    return description;
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
  
}
