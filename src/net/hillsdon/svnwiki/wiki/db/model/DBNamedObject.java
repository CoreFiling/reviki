package net.hillsdon.svnwiki.wiki.db.model;

import static net.hillsdon.svnwiki.text.Strings.join;
import static net.hillsdon.fij.core.Functional.iter;
import net.hillsdon.fij.core.Transform;

public class DBNamedObject {

  public static final Transform<DBNamedObject, String> TO_NAME = new Transform<DBNamedObject, String>() {
    public String transform(final DBNamedObject in) {
      return in.getName();
    }
  };

  private final String _name;

  public DBNamedObject(final String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return getName();
  }

  public String getFriendlyName() {
    String name = getName();
    String[] parts = name.split("_");
    parts[0] = Character.toUpperCase(parts[0].charAt(0)) + parts[0].substring(1);
    return join(iter(parts), " ");
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!obj.getClass().equals(getClass())) {
      return false;
    }
    return ((DBNamedObject) obj).getName().equals(getName());
  }
  
}
