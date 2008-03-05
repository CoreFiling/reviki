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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author mth
 */
public class DBColumn extends DBNamedObject implements HasSqlRepresentation {

  private final DBTable _table;
  private final int _dataType;
  private final int _columnSize;

  public DBColumn(final DBTable table, final String columnName, final int dataType, final int columnSize) {
    super(columnName);
    _table = table;
    _dataType = dataType;
    _columnSize = columnSize;
  }

  public int getDataType() {
    return _dataType;
  }

  public int getColumnSize() {
    return _columnSize;
  }

  public DBTable getTable() {
    return _table;
  }

  /**
   * @return Name suitable for use in directly in an SQL statement.
   */
  public String sqlName() {
    // QUERY: This works for Postgres, might be different elsewhere?
    return "\"" + getName() + "\"";
  }

  /**
   * @param value
   *          A value for this column.
   * @return A value suitable for use in directly in an SQL statement.
   */
  public String sqlValue(Object value) {
    if (value == null) {
      return "NULL";
    }
    switch (_dataType) {
      case Types.VARCHAR:
      case Types.CHAR:
        return sqlString(value);
    }
    return String.valueOf(value);
  }

  private String sqlString(Object value) {
    return "'" + value + "'";
  }

  /**
   * If this column, in its own right, is a fk, then return the fk.
   */
  public DBForeignKey fk() {
    Set<DBColumn> justUs = Collections.singleton(this);
    for (DBForeignKey fk : _table.getForeignKeys()) {
      if (fk.getKeyColumns().equals(justUs)) {
        return fk;
      }
    }
    return null;
  }

  public Map<DBColumn, Object> fkLink(final ResultSet rs) throws SQLException {
    DBForeignKey fk = fk();
    if (fk == null) {
      return null;
    }
    Map<DBColumn, Object> map = new LinkedHashMap<DBColumn, Object>();
    Iterator<DBColumn> primaryKey = fk.getReferences().getPrimaryKey().iterator();
    Iterator<DBColumn> foreignKey = fk.getKeyColumns().iterator();
    while (primaryKey.hasNext() && foreignKey.hasNext()) {
      DBColumn pcol = primaryKey.next();
      DBColumn fcol = foreignKey.next();
      map.put(pcol, rs.getObject(fcol.getName()));
    }
    return map;
  }

  /**
   * @return This column as part of a create table statement.
   */
  public String toSql() {
    // FIXME: Escaping!
    StringBuilder sb = new StringBuilder();
    sb.append(getName());
    sb.append(' ');
    sb.append(typeToString());
    if (_columnSize > 0) {
      sb.append('(');
      sb.append(_columnSize);
      sb.append(')');
    }
    return sb.toString();
  }

  private String typeToString() {
    switch (_dataType) {
      case Types.BIT: return "BIT";
      case Types.TINYINT: return "TINYINT";
      case Types.SMALLINT: return "SMALLINT";
      case Types.INTEGER: return "INTEGER";
      case Types.BIGINT: return "BIGINT";
      case Types.FLOAT: return "FLOAT";
      case Types.REAL: return "REAL";
      case Types.DOUBLE: return "DOUBLE";
      case Types.NUMERIC: return "NUMERIC";
      case Types.DECIMAL: return "DECIMAL";
      case Types.CHAR: return "CHAR";
      case Types.VARCHAR: return "VARCHAR";
      case Types.LONGVARCHAR: return "LONGVARCHAR";
      case Types.DATE: return "DATE";
      case Types.TIME: return "TIME";
      case Types.TIMESTAMP: return "TIMESTAMP";
      case Types.BINARY: return "BINARY";
      case Types.VARBINARY: return "VARBINARY";
      case Types.LONGVARBINARY: return "LONGVARBINARY";
      case Types.NULL: return "NULL";
      case Types.JAVA_OBJECT: return "JAVA_OBJECT";
      case Types.DISTINCT: return "DISTINCT";
      case Types.STRUCT: return "STRUCT";
      case Types.ARRAY: return "ARRAY";
      case Types.BLOB: return "BLOB";
      case Types.CLOB: return "CLOB";
      case Types.REF: return "REF";
      case Types.DATALINK: return "DATALINK";
      case Types.BOOLEAN: return "BOOLEAN";
      // TODO: Enable when we depend on 1.6.
      //case Types.ROWID: return "ROWID";
      //case Types.NCHAR: return "NCHAR";
      //case Types.NVARCHAR: return "NVARCHAR";
      //case Types.LONGNVARCHAR: return "LONGNVARCHAR";
      //case Types.NCLOB: return "NCLOB";
      //case Types.SQLXML: return "SQLXML";
      case Types.OTHER: return "OTHER";
      default:
        throw new IllegalArgumentException("Unknown type: " + _dataType);
    }
  }

}
