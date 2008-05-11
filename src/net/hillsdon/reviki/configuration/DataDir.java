package net.hillsdon.reviki.configuration;

import java.io.File;



/**
 * Where we store the reviki data.
 */
public interface DataDir {

  /**
   * @param identifier An identifier.
   * @return A search index directory specific to that identifier.
   */
  File getSearchIndexDirectory(String identifier);
  
  /**
   * @return A map, may not always be persistent though.
   * @see PersistentStringMap#isPersistable()
   */
  PersistentStringMap getProperties();
  
}
