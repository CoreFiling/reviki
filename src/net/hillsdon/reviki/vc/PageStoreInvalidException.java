package net.hillsdon.reviki.vc;


/**
 * Thrown when the {@link PageStore} isn't set up properly and shouldn't be used.
 * 
 * @author pjt
 */
public class PageStoreInvalidException extends Exception {
  
  private static final long serialVersionUID = 1L;

  public PageStoreInvalidException() {
    super();
  }

  public PageStoreInvalidException(String message, Throwable cause) {
    super(message, cause);
  }

  public PageStoreInvalidException(String message) {
    super(message);
  }

  public PageStoreInvalidException(Throwable cause) {
    super(cause);
  }
  
}
