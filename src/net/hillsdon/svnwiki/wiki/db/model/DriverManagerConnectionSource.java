/**
 * Copyright 2007 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
