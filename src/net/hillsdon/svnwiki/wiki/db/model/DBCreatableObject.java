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
