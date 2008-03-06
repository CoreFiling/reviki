package net.hillsdon.svnwiki.wiki.plugin;

import net.hillsdon.svnwiki.vc.ChangeSubscriber;


/**
 * All the available plugins.
 * 
 * @author mth
 */
public interface Plugins extends ContributedImplementationProvider, ChangeSubscriber {
  
  /**
   * @param component A component plugins may depend on.
   */
  void addPluginAccessibleComponent(Object component);
  
}
