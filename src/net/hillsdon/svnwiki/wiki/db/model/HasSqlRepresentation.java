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
