package net.hillsdon.svnwiki.wiki.db.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.hillsdon.fij.collections.DelegatingTransformMap;
import net.hillsdon.fij.collections.TransformMap;

public class DBSchema extends DBCreatableObject {

  private TransformMap<String, DBTable> _tables = new DelegatingTransformMap<String, DBTable>(DBNamedObject.TO_NAME);
  
  public DBSchema(String name) {
    super(name);
  }

  public TransformMap<String, DBTable> tables() {
    return _tables;
  }
  
  @Override
  public boolean exists(Connection connection) throws SQLException {
    ResultSet schemas = connection.getMetaData().getSchemas(null, getName());
    try {
      return schemas.next();
    }
    finally {
      schemas.close();
    }
  }

  public String toSql() {
    return "create schema " + getName();
  }


}
