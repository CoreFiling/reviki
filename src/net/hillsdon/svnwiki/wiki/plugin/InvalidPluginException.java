package net.hillsdon.svnwiki.wiki.plugin;

public class InvalidPluginException extends Exception {

  private static final long serialVersionUID = 1L;

  public InvalidPluginException(final Throwable t) {
    super(t);
  }

  public InvalidPluginException(final String message) {
    super(message);
  }

}
