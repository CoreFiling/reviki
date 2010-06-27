package net.hillsdon.reviki.wiki.plugin;

import java.util.Collections;
import java.util.List;

/**
 * A plugin at a revision.  The plugin may not exist at the revision (this is
 * currently used to track deleted plugins).
 */
class PluginAtRevision implements ContributedImplementationProvider {

  private final Plugin _plugin;
  private final long _revision;

  /**
   * @param plugin The plugin, null if deleted.
   * @param revision The revision at which we observed the update.
   */
  PluginAtRevision(final Plugin plugin, final long revision) {
    _plugin = plugin;
    _revision = revision;
  }

  public <T> List<T> getImplementations(final Class<T> clazz) {
    if (_plugin == null) {
      return Collections.emptyList();
    }
    return _plugin.getImplementations(clazz);
  }

  public long getRevision() {
    return _revision;
  }

}