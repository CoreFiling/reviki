package net.hillsdon.reviki.configuration;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Interface to make testing code that uses a {@link Properties} file easier.
 * 
 * @author mth
 */
public interface PersistentStringMap extends Map<String, String> {

  void load() throws IOException;
  
  void save() throws IOException;
  
}
