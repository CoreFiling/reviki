package net.hillsdon.svnwiki.wiki.db.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DriverManagerConnectionSource implements ConnectionSource {

  private final String _url;
  private final String _username;
  private final String _password;

  public DriverManagerConnectionSource(final String driverClass, final String url, final String username, final String password) throws ClassNotFoundException {
    _url = url;
    _username = username;
    _password = password;
    Class.forName(driverClass);
  }

  public Connection connect() throws SQLException {
    return DriverManager.getConnection(_url, _username, _password);
  }

}
