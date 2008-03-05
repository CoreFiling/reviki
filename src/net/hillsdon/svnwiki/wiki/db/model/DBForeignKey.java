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
