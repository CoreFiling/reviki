package net.hillsdon.svnwiki.vc;

public class InterveningCommitException extends PageStoreException {
  private static final long serialVersionUID = 1L;
  public InterveningCommitException(Throwable cause) {
    super(cause);
  }
}
