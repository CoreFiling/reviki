package net.hillsdon.svnwiki.wiki.db.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Builds a DBModel from the metadata available from a JDBC connection.
 * 
 * @author mth
 */
public class DBModelFactory {

  public DBModel create(final ConnectionSource connectionSource) throws SQLException {
    Connection connection = connectionSource.connect();
    DatabaseMetaData metadata = connection.getMetaData();
    try {
      return create(connectionSource, metadata);
    }
    finally {
      connection.close();
    }
  }

  private DBModel create(final ConnectionSource connectionSource, final DatabaseMetaData metadata) throws SQLException {
    DBModel dbmodel = new DBModel(connectionSource);
    populate(connectionSource, metadata, dbmodel);
    Map<String, DBTable> tables = new LinkedHashMap<String, DBTable>();
    for (DBSchema schema : dbmodel.schemas().values()) {
      for (DBTable table : schema.tables().values()) {
        tables.put(table.getQualifiedName(), table);
      }
    }
    for (DBTable t : tables.values()) {
      fillColumns(metadata, t);
    }
    crossReference(metadata, tables);
    return dbmodel;
  }

  private void fillColumns(final DatabaseMetaData metadata, final DBTable table) throws SQLException {
    ResultSet rs = metadata.getColumns(null, table.getSchema().getName(), table.getName(), null);
    try {
      List<DBColumn> columns = new ArrayList<DBColumn>();
      while (rs.next()) {
        String columnName = lc(rs.getString("COLUMN_NAME"));
        int dataType = rs.getInt("DATA_TYPE");
        int columnSize = rs.getInt("COLUMN_SIZE");
        columns.add(new DBColumn(table, columnName, dataType, columnSize));
      }
      table.setColumns(columns);
    }
    finally {
      rs.close();
    }
  }

  private void crossReference(final DatabaseMetaData metadata, final Map<String, DBTable> tables) throws SQLException {
    for (DBTable table : tables.values()) {
      ResultSet rs = metadata.getPrimaryKeys(null, table.getSchema().getName(), table.getName());
      while (rs.next()) {
        table.getPrimaryKey().add(table.getColumn(lc(rs.getString("COLUMN_NAME"))));
      }
    }

    for (DBTable table : tables.values()) {
      ResultSet rs = metadata.getImportedKeys(null, table.getSchema().getName(), table.getName());
      Map<String, DBForeignKey> fkByName = new LinkedHashMap<String, DBForeignKey>();
      while (rs.next()) {
        // There may be more than one fk per table.
        String fkName = lc(rs.getString("FK_NAME"));
        DBTable referenced = tables.get(lc(rs.getString("PKTABLE_SCHEM")) + "." + lc(rs.getString("PKTABLE_NAME")));
        DBColumn byColumn = table.getColumn(lc(rs.getString("FKCOLUMN_NAME")));
        DBForeignKey fk = fkByName.get(fkName);
        if (fk == null) {
          fk = new DBForeignKey(fkName, new LinkedHashSet<DBColumn>(), referenced);
          fkByName.put(fkName, fk);
          table.getForeignKeys().add(fk);
        }
        fk.getKeyColumns().add(byColumn);
      }
    }
  }

  // lower-case for consistency.
  private String lc(final String in) {
    return in == null ? null : in.toLowerCase(Locale.US);
  }
  
  private void populate(final ConnectionSource connectionSource, final DatabaseMetaData metadata, final DBModel model) throws SQLException {
    ResultSet rs = metadata.getTables(null, null, null, null);
    try {
      while (rs.next()) {
        String schemaName = lc(rs.getString("TABLE_SCHEM"));
        String type = lc(rs.getString("TABLE_TYPE"));
        String name = lc(rs.getString("TABLE_NAME"));
        if ("view".equals(type)) {
          // TODO
        }
        else if ("table".equals(type)) {
          DBSchema schema = model.schemas().get(schemaName);
          if (schema == null) {
            schema = new DBSchema(schemaName);
            model.schemas().put(schema);
          }
          schema.tables().put(new DBTable(connectionSource, schema, name));
        }
      }
    }
    finally {
      rs.close();
    }
  }

}
