package net.hillsdon.reviki.vc.impl;

import java.util.Map;

import net.hillsdon.reviki.vc.PageStoreException;

import org.tmatesoft.svn.core.io.SVNRepository;

/**
 * Returns appropriate properties for a file.
 * 
 * Split into {@link #read()} and {@link #apply(String)}
 * as {@link SVNRepository} isn't re-entrant.
 * 
 * @author mth
 */
public interface AutoPropertiesApplier {
  
  /**
   * Updates the known properties from the page store if necessary,
   * 
   * @throws PageStoreException If we fail to read them.
   */
  void read() throws PageStoreException;

  /**
   * Applies the current
   *  
   * @param filename
   * @return
   */
  Map<String, String> apply(String filename);
  
}
