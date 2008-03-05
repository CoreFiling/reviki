package net.hillsdon.svnwiki.wiki.db.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DBCreatableObject extends DBNamedObject implements HasSqlRepresentation {

  public DBCreatableObject(String name) {
    super(name);
  }

  /** Inherent race condition, nothing we can do afaict. */
  public void createIfDoesntExist(final Connection connection) throws SQLException {
    if (!exists(connection)) {
      create(connection);
    }
  }
  
  public void create(final Connection connection) throws SQLException {
    Statement sm = connection.createStatement();
    try {
      sm.execute(toSql());
    }
    finally {
      sm.close();
    }
  }

  public abstract boolean exists(Connection connection) throws SQLException;
  
}
