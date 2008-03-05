package net.hillsdon.svnwiki.web;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

public class Configuration {

  private SVNURL _url = null;

  public SVNURL getUrl() {
    return _url;
  }

  public void setUrl(final String url) throws IllegalArgumentException {
    try {
      _url = SVNURL.parseURIDecoded(url);
    }
    catch (SVNException e) {
      throw new IllegalArgumentException("Invalid SVN URL", e);
    }
  }

  public boolean isComplete() {
    return _url != null;
  }

}
