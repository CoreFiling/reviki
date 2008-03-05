package net.hillsdon.svnwiki.vc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An attachment with information on all versions.
 * 
 * @author mth
 */
public class AttachmentHistory {

  private final List<ChangeInfo> _versionsMostRecentFirst = new ArrayList<ChangeInfo>();
  
  public String getName() {
    return getLatestVersion().getName();
  }
  
  public long getRevision() {
    return getLatestVersion().getRevision();
  }
  
  public ChangeInfo getLatestVersion() {
    return _versionsMostRecentFirst.get(0);
  }
  
  public List<ChangeInfo> getPreviousVersions() {
    if (_versionsMostRecentFirst.size() < 2) {
      return Collections.emptyList();
    }
    return _versionsMostRecentFirst.subList(1, _versionsMostRecentFirst.size() - 1);
  }

  /**
   * @return Live list of versions.
   */
  List<ChangeInfo> getVersions() {
    return _versionsMostRecentFirst;
  }

  @Override
  public String toString() {
    return getName() +  getVersions();
  }
  
}
