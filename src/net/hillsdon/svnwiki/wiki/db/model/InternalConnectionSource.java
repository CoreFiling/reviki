package net.hillsdon.svnwiki.wiki.db.model;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connection source for the internal database.
 * 
 * This database is used for configuration and can be used
 * for user tables if the user does not want to configure
 * an external database. 
 * 
 * @author mth
 */
public class InternalConnectionSource implements ConnectionSource {

  private static final String DERBY_SYSTEM_HOME = "derby.system.home";
  private final String _url;

  public InternalConnectionSource() throws ClassNotFoundException {
    ensureDerbySystemHomeDir();
    Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
    _url = "jdbc:derby:" + getClass().getName() + ";create=true";
  }

  private void ensureDerbySystemHomeDir() {
    String derbySystemHome = System.getProperty(DERBY_SYSTEM_HOME);
    if (derbySystemHome == null) {
      // Pick a sensible one.
      String homeDir = System.getProperty("user.home");
      String dataDir = homeDir + File.separator + ".dbwiki";
      File dir = new File(dataDir);
      if (dir.exists()) {
        if (!dir.isDirectory()) {
          // Ignore for now.
          return;
        }
      }
      else {
        if (!dir.mkdir()) {
          // Ignore for now.
          return;
        }
      }
      System.setProperty(DERBY_SYSTEM_HOME, dir.getAbsolutePath());
    }
  }
  
  public Connection connect() throws SQLException {
    return DriverManager.getConnection(_url);
  }

}
