package net.hillsdon.svnwiki.vc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
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

  @SuppressWarnings("unchecked")
  public ChangeInfo[] recentChanges() throws PageStoreException {
    try {
      List<SVNLogEntry> entries = new ArrayList<SVNLogEntry>();
      _repository.log(new String[] {""}, entries, 0, -1, true, true);
      Set<ChangeInfo> results = new LinkedHashSet<ChangeInfo>(entries.size());
      String rootPath = _repository.getRepositoryPath("");
      for (ListIterator<SVNLogEntry> iter = entries.listIterator(entries.size()); iter.hasPrevious();) {
        SVNLogEntry entry = iter.previous();
        for (String path : (Collection<String>) entry.getChangedPaths().keySet()) {
          if (path.length() > rootPath.length()) {
            String name = path.substring(rootPath.length() + 1);
            String user = entry.getAuthor();
            Date date = entry.getDate();
            results.add(new ChangeInfo(name, user, date));
          }
        }
      }
      return results.toArray(new ChangeInfo[results.size()]);
    }
    catch (SVNAuthenticationException ex) {
      throw new PageStoreAuthenticationException(ex);
    }
    catch (SVNException ex) {
      throw new PageStoreException(ex);
    }
  }
  
  public String[] list() throws PageStoreException {
    try {
      List<SVNDirEntry> entries = new ArrayList<SVNDirEntry>();
      _repository.getDir("", -1, false, entries);
      List<String> results = new ArrayList<String>(entries.size());
      for (SVNDirEntry e : entries) {
        if (SVNNodeKind.FILE.equals(e.getKind())) {
          results.add(e.getName());
        }
      }
      return results.toArray(new String[results.size()]);
    }
    catch (SVNAuthenticationException ex) {
      throw new PageStoreAuthenticationException(ex);
    }
    catch (SVNException ex) {
      throw new PageStoreException(ex);
    }
  }

  public PageInfo get(final String path) throws PageStoreException {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      HashMap<String, String> properties = new HashMap<String, String>();
      
      // We really want (kind, revision) back to avoid a race here...
      SVNNodeKind kind = _repository.checkPath(path, SVNRevision.HEAD.getNumber());
      if (SVNNodeKind.FILE.equals(kind)) {
        _repository.getFile(path, SVNRevision.HEAD.getNumber(), properties, baos);
        long revision = Long.parseLong(properties.get(SVNProperty.REVISION));
        return new PageInfo(path, toUTF8(baos.toByteArray()), revision);
      }
      else if (SVNNodeKind.NONE.equals(kind)) {
        return new PageInfo(path, "", PageInfo.UNCOMMITTED);
      }
      else {
        throw new PageStoreException(String.format("Unexpected node kind '%s' at '%s'", kind, path));
      }
    }
    catch (SVNAuthenticationException ex) {
      throw new PageStoreAuthenticationException(ex);
    }
    catch (SVNException ex) {
      throw new PageStoreException(ex);
    }
  }

  public void set(final String path, final long baseRevision, final String content) throws PageStoreException  {
    try {
      ISVNEditor commitEditor = _repository.getCommitEditor("[automated commit]", null);
      if (baseRevision == PageInfo.UNCOMMITTED) {
        createFile(commitEditor, path, fromUTF8(content));
      }
      else {
        editFile(commitEditor, path, baseRevision, fromUTF8(content));
      }
      commitEditor.closeEdit();
    }
    catch (SVNAuthenticationException ex) {
      throw new PageStoreAuthenticationException(ex);
    }
    catch (SVNException ex) {
      if (SVNErrorCode.FS_CONFLICT.equals(ex.getErrorMessage().getErrorCode())) {
        // What to do!
        throw new InterveningCommitException(ex);
      }
      throw new PageStoreException(ex);
    }
  }

  private void createFile(ISVNEditor commitEditor, String filePath, byte[] data) throws SVNException {
    commitEditor.openRoot(-1);
    commitEditor.addFile(filePath, null, -1);
    commitEditor.applyTextDelta(filePath, null);
    SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
    String checksum = deltaGenerator.sendDelta(filePath, new ByteArrayInputStream(data), commitEditor, true);
    commitEditor.closeFile(filePath, checksum);
    commitEditor.closeDir();
  }

  private void editFile(final ISVNEditor commitEditor, final String filePath, final long baseRevision, final byte[] newData) throws SVNException {
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
      throw new AssertionError("Java supports UTF8.");
    }
  }
  
  private static byte[] fromUTF8(String string) {
    try {
      return string.getBytes(UTF8);
    }
    catch (UnsupportedEncodingException e) {
      throw new AssertionError("Java supports UTF8.");
    }
  }
  
}