/**
 * Copyright 2008 Matthew Hillsdon
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
package net.hillsdon.reviki.configuration;

import java.util.Collection;

public interface DeploymentConfiguration {

  /**
   * @param wikiName The wiki name to store data under.
   * @return A wiki configuration specific to <code>wikiName</code>
   */
  WikiConfiguration getConfiguration(String wikiName);

  /**
   * Load the configuration into memory.
   */
  void load();

  /**
   * Save the configuration to disk.
   */
  void save();

  /**
   * @return true if changes can be persisted.
   *              (note it is possible for this to change over time).
   */
  boolean isEditable();

  /**
   * @return Names of the configured wikis.
   */
  Collection<String> getWikiNames();

}
