package net.hillsdon.svnwiki.vc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import junit.framework.AssertionFailedError;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * Stores pages in an SVN repository.
 * 
 * @author mth
 */
public class SVNPageStore implements PageStore {

  /**
   * The assumed encoding of files from the repository.
   */
  private static final String UTF8 = "UTF8";
  
  private final SVNRepository _repository;

  /**
   * Note the repository URL can be deep, it need not refer to 
   * the root of the repository itself.  We put pages in the root
   * of what we're given.
   */
  public SVNPageStore(final SVNRepository repository) {
    _repository = repository;
  }

  public PageInfo get(final String path) throws PageStoreException {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      HashMap<String, String> properties = new HashMap<String, String>();
      _repository.getFile("Foo.wiki", SVNRevision.HEAD.getNumber(), properties, baos);
      long revision = Long.parseLong(properties.get(SVNProperty.REVISION));
      return new PageInfo(toUTF8(baos.toByteArray()), revision);
    }
    catch (SVNException ex) {
      throw new PageStoreException(ex);
    }
  }

  public void set(final String path, final long baseRevision, final String content) throws PageStoreException  {
    try {
      ISVNEditor commitEditor = _repository.getCommitEditor("[automated commit]", null);
      modifyFile(commitEditor, path, baseRevision, fromUTF8(content));
      commitEditor.closeEdit();
    }
    catch (SVNException ex) {
      throw new PageStoreException(ex);
    }
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
  
  private static String toUTF8(byte[] bytes) {
    try {
      return new String(bytes, UTF8);
    }
    catch (UnsupportedEncodingException e) {
      throw new AssertionFailedError("Java supports UTF8.");
    }
  }
  
  private static byte[] fromUTF8(String string) {
    try {
      return string.getBytes(UTF8);
    }
    catch (UnsupportedEncodingException e) {
      throw new AssertionFailedError("Java supports UTF8.");
    }
  }
  
}