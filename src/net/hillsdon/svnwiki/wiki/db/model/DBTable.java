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

import static java.lang.String.format;
import static net.hillsdon.fij.core.Functional.map;
import static net.hillsdon.fij.text.Strings.join;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// A table could export 'selectors' for each pk/unique col set that could
// be used for default queries.
public class DBTable extends DBCreatableObject implements ConnectionSource {

  private static final String INDENT = "  ";

  private final DBSchema _schema;
  private List<DBColumn> _columns = new ArrayList<DBColumn>();
  private final Set<DBForeignKey> _foreignKeys = new LinkedHashSet<DBForeignKey>();
  private final Set<DBColumn> _primaryKey = new LinkedHashSet<DBColumn>();
  private final ConnectionSource _connectionSource;

  public DBTable(final ConnectionSource connectionSource, final DBSchema schema, final String name) {
    super(name);
    _connectionSource = connectionSource;
    _schema = schema;
  }

  public DBSchema getSchema() {
    return _schema;
  }

  public List<DBColumn> getColumns() {
    return _columns;
  }

  public void setColumns(final List<DBColumn> columns) {
    _columns = columns;
  }

  @Override
  public String toString() {
    return getName() + _columns;
  }

  public Set<DBColumn> getPrimaryKey() {
    return _primaryKey;
  }

  public DBColumn getColumn(final String string) {
    for (DBColumn c : _columns) {
      if (string.equals(c.getName())) {
        return c;
      }
    }
    throw new IllegalArgumentException(format("No such column '%s' only: ", string) + getColumns());
  }

  public String getQualifiedName() {
    return getSchema() + "." + getName();
  }

  public Set<DBForeignKey> getForeignKeys() {
    return _foreignKeys;
  }

  public Map<DBColumn, Object> primaryKey(final ResultSet rs) throws SQLException {
    Map<DBColumn, Object> primaryKey = new LinkedHashMap<DBColumn, Object>();
    for (DBColumn c : getPrimaryKey()) {
      primaryKey.put(c, rs.getObject(c.getName()));
    }
    return primaryKey;
  }

  public String toSql() {
    String columns = join(map(getColumns(), HasSqlRepresentation.TO_SQL), INDENT, ",\n", null);
    String primaryKey = INDENT + "primary key (" + join(map(getPrimaryKey(), DBNamedObject.TO_NAME), ", ") + ')';
    String start = "create table " + getQualifiedName() + " (\n";
    String end = "\n)\n";
    return start + columns + primaryKey + end;
  }

  public ResultSet row(final Connection connection, final Map<DBColumn, ? extends Object> primaryKey) throws SQLException {
    if (!primaryKey.keySet().equals(getPrimaryKey())) {
      throw new IllegalArgumentException(format("Must specify all primary key columns.  Only got %s, need %s.", primaryKey, getPrimaryKey()));
    }

    String sql = primaryKeySelectSql();
    PreparedStatement sm = connection.prepareStatement(sql);
    bindParameters(primaryKey, sm);
    sm.execute();
    return sm.getResultSet();
  }

  String primaryKeySelectSql() {
    String select = "select * from " + getQualifiedName();
    String where = join(map(getPrimaryKey(), DBNamedObject.TO_NAME), null, " = ?", " and ");
    String sql = select + " where " + where;
    return sql;
  }

  private void bindParameters(final Map<DBColumn, ? extends Object> parameters, final PreparedStatement sm) throws SQLException {
    int i = 1;
    for (DBColumn pkCol : getPrimaryKey()) {
      Object value = parameters.get(pkCol);
      sm.setObject(i++, value);
    }
  }

  public ResultSet select(final Connection connection) throws SQLException {
    Statement sm = connection.createStatement();
    sm.execute("select * from " + getQualifiedName());
    return sm.getResultSet();
  }

  public Connection connect() throws SQLException {
    return _connectionSource.connect();
  }

  @Override
  public boolean exists(final Connection connection) throws SQLException {
    ResultSet tables = connection.getMetaData().getTables(null, getSchema().getName(), getName(), null);
    try {
      return tables.next();
    }
    finally {
      tables.close();
    }
  }

  @Override
  public void create(Connection connection) throws SQLException {
    getSchema().createIfDoesntExist(connection);
    super.create(connection);
  }
  
}
