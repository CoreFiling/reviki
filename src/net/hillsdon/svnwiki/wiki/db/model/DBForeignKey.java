package net.hillsdon.svnwiki.wiki.db.model;

import java.util.Set;

public class DBForeignKey extends DBNamedObject {

  private final DBTable _references;
  private final Set<DBColumn> _fk;

  public DBForeignKey(final String name, final Set<DBColumn> fk, final DBTable references) {
    super(name);
    _fk = fk;
    _references = references;
  }

  public DBTable getReferences() {
    return _references;
  }

  public Set<DBColumn> getKeyColumns() {
    return _fk;
  }

  @Override
  public String toString() {
    return _fk + "->" + _references.getName();
  }

}
