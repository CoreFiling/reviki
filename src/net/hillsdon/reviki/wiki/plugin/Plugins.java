package net.hillsdon.reviki.wiki.plugin;

import net.hillsdon.reviki.vc.ChangeSubscriber;


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
