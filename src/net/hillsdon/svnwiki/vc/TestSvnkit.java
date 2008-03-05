package net.hillsdon.svnwiki.vc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import junit.framework.TestCase;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.SVNRevision;

public class TestSvnkit extends TestCase {

  private static final String URL = "http://localhost/svn/usr/mth/wiki/";

  public static class PageInfo {

    private final String _content;

    private final long _baseRevision;

    public PageInfo(final String content, final long baseRevision) {
      _content = content;
      _baseRevision = baseRevision;
    }

    private String getContent() {
      return _content;
    }

    public long getBaseRevision() {
      return _baseRevision;
    }

  }

  public static class PageStore {

    private static final String UTF8 = "UTF8";
    
    private final SVNRepository _repository;

    public PageStore(final SVNRepository repository) {
      _repository = repository;
    }

    public PageInfo get(final String path) throws SVNException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      HashMap<String, String> properties = new HashMap<String, String>();
      _repository.getFile("Foo.wiki", SVNRevision.HEAD.getNumber(), properties, baos);
      long revision = Long.parseLong(properties.get(SVNProperty.REVISION));
      try {
        return new PageInfo(new String(baos.toByteArray(), "UTF-8"), revision);
      }
      catch (UnsupportedEncodingException e) {
        throw new Error("UTF8 is supported by Java.");
      }
    }

    public void set(final String path, final long baseRevision, final String content) throws SVNException {
      ISVNEditor commitEditor = _repository.getCommitEditor("[automated commit]", null);
      try {
        modifyFile(commitEditor, path, baseRevision, content.getBytes(UTF8));
      }
      catch (UnsupportedEncodingException e) {
        throw new Error("UTF8 is supported by Java.");
      }
      commitEditor.closeEdit();
    }

    private void modifyFile(final ISVNEditor commitEditor, final String filePath, final long baseRevision, final byte[] newData) throws SVNException {
      commitEditor.openRoot(-1);
      commitEditor.openFile(filePath, baseRevision);
      commitEditor.applyTextDelta(filePath, null);
      SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
      // We don't keep the base around so we can't provide it here.
      String checksum = deltaGenerator.sendDelta(filePath, new ByteArrayInputStream(newData), commitEditor, true);
      commitEditor.closeFile(filePath, checksum);
      commitEditor.closeDir();
    }
    
  }

  public void test() throws SVNException {
    String username = "mth";
    String password = "xxx";

    DAVRepositoryFactory.setup();
    SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIDecoded(URL));
    repository.setAuthenticationManager(new BasicAuthenticationManager(username, password));
    PageStore store = new PageStore(repository);
    PageInfo page = store.get("Foo.wiki");
    String content = page.getContent();
    
    store.set("Foo.wiki", page.getBaseRevision(), content + " extra! ");
  }

}
