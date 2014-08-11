package net.hillsdon.reviki.vc.impl;

import net.hillsdon.reviki.vc.AttachmentHistory;

import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class AttachmentListingDebug {

  public static void main(String[] args) throws Exception {
    DAVRepositoryFactory.setup();
    SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIDecoded("https://svn-dev-plaintext.int.corefiling.com/svn/web/wiki/dsl"));
    repository.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager());
    RepositoryBasicSVNOperations operations = new RepositoryBasicSVNOperations(repository, null);
    SVNPageStore store = new SVNPageStore("wiki", new InMemoryDeletedRevisionTracker(), operations, null, null);
    for (AttachmentHistory attachment : store.attachments(new PageReferenceImpl("Printing"))) {
      System.err.println(attachment.getName());
    }
  }
  
}
