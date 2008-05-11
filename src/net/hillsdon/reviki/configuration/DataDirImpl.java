package net.hillsdon.reviki.configuration;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;

import net.hillsdon.fij.io.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.apache.commons.lang.StringUtils.trimToNull;

public class DataDirImpl implements DataDir {

  private static final Log LOG = LogFactory.getLog(DataDirImpl.class);
  
  public static final String DATA_DIR_CONTEXT_PARAM = "reviki-data-dir";
  private static final String DEFAULT_CONFIG_DIR_NAME = "reviki-data";
  private static final String SEARCH_INDEX_DIR_NAME = "search-index";
  private static final String CONFIG_FILE_NAME = "reviki.properties";
  
  private final ServletContext _servletContext;
  private final String _defaultBaseDir;

  public DataDirImpl(final ServletContext servletContext) {
    this(servletContext, System.getProperty("user.home"));
  }

  public DataDirImpl(final ServletContext servletContext, final String defaultBaseDir) {
    _servletContext = servletContext;
    _defaultBaseDir = defaultBaseDir;
  }

  /**
   * @return A configuration location if we can, otherwise null.
   */
  private File getConfigurationLocation() {
    String location = trimToNull(_servletContext.getInitParameter(DATA_DIR_CONTEXT_PARAM));
    if (location == null) {
      try {
        location = Path.join(_defaultBaseDir, DEFAULT_CONFIG_DIR_NAME);
      }
      catch (SecurityException ex) {
      }
    }
    if (location == null) {
      return null;
    }
    File dir = new File(location);
    try {
      if (!dir.exists()) {
        try {
          FileUtils.forceMkdir(dir);
        }
        catch (IOException ex) {
          LOG.error("Failed to create data area.", ex);
          return null;
        }
      }
    }
    catch (SecurityException ex) {
      return null;
    }
    return dir;
  }

  private File getConfigurationFile() {
    File location = getConfigurationLocation();
    if (location != null) {
      File file = new File(location, CONFIG_FILE_NAME);
      return file;
    }
    return null;
  }

  public PersistentStringMap getProperties() {
    return new PropertiesFile(getConfigurationFile());
  }

  public File getSearchIndexDirectory(String identifier) {
    File searchDir = getWritableChildDir(getConfigurationLocation(), SEARCH_INDEX_DIR_NAME);
    return searchDir == null ? null : getWritableChildDir(searchDir, identifier);
  }
  
  private File getWritableChildDir(final File dir, final String child) {
    File indexDir = new File(dir, child);
    if (!indexDir.exists()) {
      if (!indexDir.mkdir()) {
        return null;
      }
    }
    if (indexDir.isDirectory() && indexDir.canWrite()) {
      return indexDir;
    }
    return null;
  }
  
}