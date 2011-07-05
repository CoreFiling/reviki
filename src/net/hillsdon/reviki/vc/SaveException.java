package net.hillsdon.reviki.vc;

public abstract class SaveException extends PageStoreException {
  private static final long serialVersionUID = 1L;
  public SaveException(Throwable cause) {
      super(cause);
  }
}
