package net.hillsdon.svnwiki.vc;

/**
 * SVN provides no way to do svn log or similar on a delete URL
 * without knowing the last URL at which it existed.  They
 * recommend using svn log -v on the parent directory to find
 * that revision.  On a wiki with many revisions this is intolerably
 * slow as all the action is in a single directory.
 * 
 * We therefore keep up to date by reading the svn log an remember
 * revisions in which files were deleted.
 *
 * @author mth
 *
 */
public interface DeletedRevisionTracker {

  ChangeInfo getChangeThatDeleted(SVNHelper helper, String path) throws PageStoreAuthenticationException, PageStoreException;

}
