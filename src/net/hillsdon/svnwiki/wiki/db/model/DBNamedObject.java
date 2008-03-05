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

import static net.hillsdon.fij.core.Functional.iter;
import static net.hillsdon.fij.text.Strings.join;
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
