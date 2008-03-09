package net.hillsdon.reviki.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
  
  @Override
  protected InputStream inputStream() throws IOException {
    return _file == null ? null : new FileInputStream(_file);
  }

  @Override
  protected OutputStream outputStream() throws IOException {
    return _file == null ? null : new FileOutputStream(_file);
  }
  
}
