package net.hillsdon.svnwiki.wiki.db.model;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionSource {
  
  // Useful for testing.
  ConnectionSource FAIL  = new ConnectionSource() {
    public Connection connect() throws SQLException {
      throw new SQLException("This connection source doesn't work.");
    }
  };
  
  Connection connect() throws SQLException;

}
