package net.hillsdon.svnwiki.vc;

/**
 * Thrown if we try to set with a baseRevision that is not the repository head revision.
 * 
 * We try to avoid this by using svn lock.
 * 
 * @author mth
 */
public class InterveningCommitException extends PageStoreException {
  private static final long serialVersionUID = 1L;
  public InterveningCommitException(Throwable cause) {
    super(cause);
  }
}
