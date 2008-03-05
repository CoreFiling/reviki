package net.hillsdon.svnwiki.vc;


import junit.framework.TestCase;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

public class TestSvnkit extends TestCase {

  private static final String URL = "http://localhost/svn/usr/mth/wiki/";

  public void test() throws PageStoreException, SVNException {
    String username = "mth";
    String password = "xxx";

    DAVRepositoryFactory.setup();
    SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIDecoded(URL));
    repository.setAuthenticationManager(new BasicAuthenticationManager(username, password));
    PageStore store = new SVNPageStore(repository);
    PageInfo page = store.get("Blort.wiki");
    store.set("Blort.wiki", page.getRevision(), "New content");
    System.err.println(store.get("Blort.wiki"));
  }

}
