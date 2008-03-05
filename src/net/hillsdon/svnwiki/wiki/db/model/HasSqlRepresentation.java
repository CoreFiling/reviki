package net.hillsdon.svnwiki.wiki.db.model;

import net.hillsdon.fij.core.Transform;

/**
 * Can be modelled in SQL, e.g. query, table, column.
 */
public interface HasSqlRepresentation {
  
  public static final Transform<HasSqlRepresentation, String> TO_SQL = new Transform<HasSqlRepresentation, String>() {
    public String transform(final HasSqlRepresentation in) {
      return in.toSql();
    }
  };

  String toSql();
  
}
