package net.hillsdon.svnwiki.wiki.db.model;

import java.sql.Connection;
import java.sql.SQLException;

import net.hillsdon.fij.collections.DelegatingTransformMap;
import net.hillsdon.fij.collections.TransformMap;

/**
 * A model of the database schema including relationships between relations at
 * the pk/fk level.
 * 
 * @author mth
 */
public class DBModel implements ConnectionSource {

  private final TransformMap<String, DBSchema> _schemas = new DelegatingTransformMap<String, DBSchema>(DBNamedObject.TO_NAME);
  private final ConnectionSource _connectionSource;

  public DBModel(ConnectionSource connectionSource) {
    _connectionSource = connectionSource;
  }

  public DBTable getTable(final String schemaName, final String tableName) {
    DBSchema schema = schemas().get(schemaName);
    if (schema == null) {
      return null;
    }
    return schema.tables().get(tableName);
  }

  public TransformMap<String, DBSchema> schemas() {
    return _schemas;
  }

  public Connection connect() throws SQLException {
    return _connectionSource.connect();
  }

  public DBTable getTable(String fqName) {
    String[] parts = fqName.split("\\.");
    String schema = null;
    String table = null;
    if (parts.length < 1 || parts.length > 2) {
      throw new IllegalArgumentException("Must be schema.table: " + fqName);
    }
    if (parts.length == 1) {
      table = parts[0];
    }
    if (parts.length == 2) {
      schema = parts[0];
      table = parts[1];
    }
    return getTable(schema, table);
  }

}
