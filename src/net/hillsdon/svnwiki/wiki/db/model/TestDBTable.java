package net.hillsdon.svnwiki.wiki.db.model;

import java.sql.Types;

import junit.framework.TestCase;

public class TestDBTable extends TestCase {

  private DBTable _table;

  public void setUp() {
    _table = new DBTable(ConnectionSource.FAIL, new DBSchema("schema"), "name");
    DBColumn code1 = new DBColumn(_table, "code1", Types.INTEGER, 0);
    DBColumn code2 = new DBColumn(_table, "code2", Types.INTEGER, 0);
    
    _table.getColumns().add(code1);
    _table.getColumns().add(code2);
  
    _table.getPrimaryKey().add(code1);
    _table.getPrimaryKey().add(code2);

    _table.getColumns().add(new DBColumn(_table, "name", Types.VARCHAR, 1024));
    _table.getColumns().add(new DBColumn(_table, "age", Types.INTEGER, 0));
  }
  
  public void testToSql() {
    String expected =
      "create table schema.name (\n" +
      "  code1 INTEGER,\n" +
      "  code2 INTEGER,\n" +
      "  name VARCHAR(1024),\n" +
      "  age INTEGER,\n" +
      "  primary key (code1, code2)\n" +
      ")\n";
    String actual = _table.toSql();
    assertEquals(expected, actual);
  }

  
  public void testByPrimaryKeySelectSql() {
    assertEquals("select * from schema.name where code1 = ? and code2 = ?", _table.primaryKeySelectSql());
  }
  
}
