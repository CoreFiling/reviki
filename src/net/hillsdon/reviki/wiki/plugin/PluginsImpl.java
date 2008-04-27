/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.wiki.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.hillsdon.fij.core.IterableUtils;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ContentTypedSink;
import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.StoreKind;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.picocontainer.DefaultPicoContainer;

import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.CONFIG_PLUGINS;

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

  public void handleChanges(final long upto, final List<ChangeInfo> chronological) throws PageStoreException, IOException {
    // We want to do the most recent first to prevent repeated work.
    for (ChangeInfo change : IterableUtils.reversed(chronological)) {
      if (change.getKind() == StoreKind.ATTACHMENT && change.getPage().equals(CONFIG_PLUGINS.getPath())) {
        PluginAtRevision plugin = _active.get(change.getName());
        if (plugin == null || plugin._revision < change.getRevision()) {
          updatePlugin(change.getName(), change.getRevision());
        }
      }
    }
  }

  private void updatePlugin(final String name, final long revision) throws NotFoundException, PageStoreException, IOException {
    LOG.info("Updating " + name + " to revision " + revision);
    final File jar = File.createTempFile("cached-", name);
    jar.deleteOnExit();
    final OutputStream stream = new FileOutputStream(jar);
    try {
      _store.attachment(CONFIG_PLUGINS, name, revision, new ContentTypedSink() {
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

  public long getHighestSyncedRevision() throws IOException {
    return 0;
  }
  
}
