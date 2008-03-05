package net.hillsdon.svnwiki.vc;

import net.hillsdon.svnwiki.text.WikiWordUtils;

public class PageReference {

  private final String _path;

  public PageReference(String path) {
    _path = path;
  }

  public String getTitle() {
    return WikiWordUtils.pathToTitle(getPath()).toString();
  }
  
  public String getPath() {
    return _path;
  }

}

