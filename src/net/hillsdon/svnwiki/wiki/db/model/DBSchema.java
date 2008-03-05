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
import java.sql.ResultSet;
import java.sql.SQLException;

import net.hillsdon.fij.collections.DelegatingTransformMap;
import net.hillsdon.fij.collections.TransformMap;

public class DBSchema extends DBCreatableObject {

  private TransformMap<String, DBTable> _tables = new DelegatingTransformMap<String, DBTable>(DBNamedObject.TO_NAME);
  
  public DBSchema(final String name) {
    super(name);
  }

  public TransformMap<String, DBTable> tables() {
    return _tables;
  }
  
  @Override
  public boolean exists(final Connection connection) throws SQLException {
    ResultSet schemas = connection.getMetaData().getSchemas();
    try {
      while (schemas.next()) {
        if (schemas.getString("TABLE_SCHEM").equalsIgnoreCase(getName())) {
          return true;
        }
      }
      return false;
    }
    finally {
      schemas.close();
    }
  }

  public String toSql() {
    return "create schema " + getName();
  }


}
