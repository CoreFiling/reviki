package net.hillsdon.svnwiki.wiki.plugin;

import net.hillsdon.svnwiki.vc.NeedsSync;


/**
 * All the available plugins.
 * 
 * @author mth
 */
public interface Plugins extends ContributedImplementationProvider, NeedsSync {
  
  /**
   * @param component A component plugins may depend on.
   */
  void addPluginAccessibleComponent(Object component);
  
}
