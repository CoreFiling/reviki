package net.hillsdon.svnwiki.wiki.plugin;

import java.util.List;

/**
 * The rest of the wiki isn't wired up with dependency injection,
 * and the plugins can change over time anyway, so we specifically
 * ask for the plugin provided implementations where relevant.
 * 
 * @author mth
 */
public interface ContributedImplementationProvider {
  
  <T> List<T> getImplementations(Class<T> clazz);
  
}
