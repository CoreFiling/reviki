package net.hillsdon.svnwiki.configuration;

import net.hillsdon.svnwiki.vc.PageStoreException;

/**
 * Configuration that is available once we have an SVN repository configured.
 *
 * @see DeploymentConfiguration
 * @author mth
 */
public interface Configuration {

  InterWikiLinker getInterWikiLinker() throws PageStoreException;
  
}
