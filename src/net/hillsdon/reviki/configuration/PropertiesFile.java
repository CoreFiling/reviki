package net.hillsdon.reviki.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Wrapper for Properties to enable testing. 
 * 
 * @author mth
 */
public class PropertiesFile extends AbstractPropertiesStore implements PersistentStringMap {

  private final File _file;

  public PropertiesFile(final File file) {
    _file = file;
  }
  
  public synchronized void load() throws IOException {
    if (_file != null) {
      getDelegate().clear();
      getDelegate().load(new FileInputStream(_file));
    }
  }

  public synchronized void save() throws IOException {
    if (_file != null) {
      getDelegate().store(new FileOutputStream(_file), "reviki configuration file");
    }
  }
  
}
