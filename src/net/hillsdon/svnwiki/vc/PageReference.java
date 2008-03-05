package net.hillsdon.svnwiki.vc;

import net.hillsdon.svnwiki.text.WikiWordUtils;

public class PageReference implements Comparable<PageReference> {

  private final String _path;

  public PageReference(final String path) {
    _path = path;
  }

  public String getTitle() {
    return WikiWordUtils.pathToTitle(getPath()).toString();
  }
  
  public String getPath() {
    return _path;
  }

  public int compareTo(final PageReference o) {
    return _path.compareTo(o._path);
  }

  @Override
  public String toString() {
    return _path;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof PageReference) {
      return ((PageReference) obj)._path.equals(_path);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return _path.hashCode();
  }
  
}

