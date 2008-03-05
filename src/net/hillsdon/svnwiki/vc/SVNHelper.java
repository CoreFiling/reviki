package net.hillsdon.svnwiki.vc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;

/**
 * Provides exception translation and low-level functionality.
 * 
 * @author mth
 */
public class SVNHelper {

  public interface SVNAction<T> {
    T perform(SVNRepository repository) throws SVNException, PageStoreException;
  }
  
  private final SVNRepository _repository;

  public SVNHelper(final SVNRepository repository) {
    _repository = repository;
  }

  public List<ChangeInfo> log(final String path, final long limit) throws SVNException {
    final String rootPath = getRoot();
    final List<ChangeInfo> entries = new LinkedList<ChangeInfo>();
    _repository.log(new String[] {path}, -1, 0, true, true, limit, new ISVNLogEntryHandler() {
      public void handleLogEntry(final SVNLogEntry logEntry) throws SVNException {
        entries.addAll(logEntryToChangeInfos(rootPath, path, logEntry));
      }
    });
    return entries;
  }
  
  public Collection<PageStoreEntry> listFiles(final String dir) throws SVNException {
    List<SVNDirEntry> entries = new ArrayList<SVNDirEntry>();
    _repository.getDir(dir, -1, false, entries);
    // Need order, contains() is a reasonable operation however.
    Set<PageStoreEntry> results = new LinkedHashSet<PageStoreEntry>(entries.size());
    for (SVNDirEntry e : entries) {
      if (SVNNodeKind.FILE.equals(e.getKind())) {
        results.add(new PageStoreEntry(e.getName(), e.getRevision()));
      }
    }
    return results;
  }


  @SuppressWarnings("unchecked")
  private List<ChangeInfo> logEntryToChangeInfos(final String rootPath, final String path, final SVNLogEntry entry) throws SVNException {
    List<ChangeInfo> results = new LinkedList<ChangeInfo>();
    String user = entry.getAuthor();
    Date date = entry.getDate();
    // FIXME: This is messy. If we're querying the root we want to report on all
    // changed
    // pages, otherwise we only want to report on the page we're interested in.
    if ("".equals(path)) {
      for (String changedPath : (Iterable<String>) entry.getChangedPaths().keySet()) {
        if (changedPath.length() > rootPath.length()) {
          String name = changedPath.substring(rootPath.length() + 1);
          results.add(new ChangeInfo(name, user, date, entry.getRevision(), entry.getMessage()));
        }
      }
    }
    else {
      results.add(new ChangeInfo(path, user, date, entry.getRevision(), entry.getMessage()));
    }
    return results;
  }

  public String getRoot() throws SVNException {
    return _repository.getRepositoryPath("");
  }

  public <T> T execute(final SVNAction<T> action) throws PageStoreException, PageStoreAuthenticationException {
    try {
      return action.perform(_repository);
    }
    catch (SVNAuthenticationException ex) {
      throw new PageStoreAuthenticationException(ex);
    }
    catch (SVNException ex) {
      throw new PageStoreException(ex);
    }
  }

  public void createDir(final ISVNEditor commitEditor, final String dir) throws SVNException {
    commitEditor.openRoot(-1);
    commitEditor.addDir(dir, null, -1);
    commitEditor.closeDir();
    commitEditor.closeDir();
  }

  public void createFile(final ISVNEditor commitEditor, final String filePath, final InputStream data) throws SVNException {
    String dir = SVNPathUtil.removeTail(filePath);
    commitEditor.openRoot(-1);
    commitEditor.openDir(dir, -1);
    commitEditor.addFile(filePath, null, -1);
    commitEditor.applyTextDelta(filePath, null);
    SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
    String checksum = deltaGenerator.sendDelta(filePath, data, commitEditor, true);
    commitEditor.closeFile(filePath, checksum);
    commitEditor.closeDir();
    commitEditor.closeDir();
  }

  public void editFile(final ISVNEditor commitEditor, final String filePath, final long baseRevision, final InputStream newData) throws SVNException {
    commitEditor.openRoot(-1);
    commitEditor.openFile(filePath, baseRevision);
    commitEditor.applyTextDelta(filePath, null);
    SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
    // We don't keep the base around so we can't provide it here.
    String checksum = deltaGenerator.sendDelta(filePath, newData, commitEditor, true);
    commitEditor.closeFile(filePath, checksum);
    commitEditor.closeDir();
  }

}
