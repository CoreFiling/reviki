package net.hillsdon.svnwiki.wiki.plugin;

import static net.hillsdon.svnwiki.web.vcintegration.SpecialPagePopulatingPageStore.PLUGINS_PAGE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.hillsdon.svnwiki.vc.AttachmentHistory;
import net.hillsdon.svnwiki.vc.ContentTypedSink;
import net.hillsdon.svnwiki.vc.NotFoundException;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.picocontainer.DefaultPicoContainer;

/**
 * An aggregate of all the current plugins.
 * 
 * @author mth
 */
public class PluginsImpl implements Plugins {

  private static final Log LOG = LogFactory.getLog(PluginsImpl.class);
  
  /**
   * A plugin at a revision.
   */
  private static class PluginAtRevision {
    private final Plugin _plugin;
    private final long _revision;
    public PluginAtRevision(final Plugin plugin, final long revision) {
      _plugin = plugin;
      _revision = revision;
    }
  }
  
  private final DefaultPicoContainer _context;
  private final ConcurrentMap<String, PluginAtRevision> _active = new ConcurrentHashMap<String, PluginAtRevision>();
  private final PageStore _store;
  
  /**
   */
  public PluginsImpl(final PageStore store) {
    _store = store;
    _context = new DefaultPicoContainer();
  }
  
  public <T> List<T> getImplementations(final Class<T> clazz) {
    List<T> implementations = new ArrayList<T>();
    for (PluginAtRevision plugin : _active.values()) {
      implementations.addAll(plugin._plugin.getImplementations(clazz));
    }
    return implementations;
  }

  public synchronized void syncWithExternalCommits() throws PageStoreException, IOException {
    for (AttachmentHistory attachment : _store.attachments(PLUGINS_PAGE)) {
      PluginAtRevision plugin = _active.get(attachment.getName());
      if (plugin == null || plugin._revision < attachment.getRevision()) {
        updatePlugin(attachment.getName(), attachment.getRevision());
      }
    }
  }

  private void updatePlugin(final String name, final long revision) throws NotFoundException, PageStoreException, IOException {
    LOG.info("Updating " + name + " to revision " + revision);
    final File jar = File.createTempFile("cached-", name);
    jar.deleteOnExit();
    final OutputStream stream = new FileOutputStream(jar);
    try {
      _store.attachment(PLUGINS_PAGE, name, revision, new ContentTypedSink() {
        public void setContentType(final String contentType) {
        }
        public void setFileName(final String attachment) {
        }
        public OutputStream stream() throws IOException {
          return stream;
        }
      });
    }
    finally {
      IOUtils.closeQuietly(stream);
    }
    URL url = jar.toURI().toURL();
    try {
      _active.put(name, new PluginAtRevision(new Plugin(name, url, _context), revision));
    }
    catch (InvalidPluginException ex) {
      // Some way to indicate the error to the user would be nice...
      _active.remove(name);
      LOG.error("Invalid plugin uploaded.", ex);
    }
  }

  public void addPluginAccessibleComponent(final Object component) {
    _context.addComponent(component);
  }
  
}
