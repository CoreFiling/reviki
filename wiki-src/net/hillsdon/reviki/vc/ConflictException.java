package net.hillsdon.reviki.vc;


public class ConflictException extends SaveException {
  private static final long serialVersionUID = 1L;
  public ConflictException(Throwable cause) {
    super(cause);
  }
}
