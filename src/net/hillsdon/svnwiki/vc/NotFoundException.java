package net.hillsdon.svnwiki.vc;


public class NotFoundException extends PageStoreException {
  private static final long serialVersionUID = 1L;
  public NotFoundException(final Throwable cause) {
    super(cause);
  }
}
