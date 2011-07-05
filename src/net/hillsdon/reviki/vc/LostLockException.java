package net.hillsdon.reviki.vc;

public class LostLockException extends SaveException {
  private static final long serialVersionUID = 1L;
  public LostLockException(Throwable cause) {
    super(cause);
  }
}
