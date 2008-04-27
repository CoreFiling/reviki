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
package net.hillsdon.reviki.vc.impl;

import java.util.Map;

import net.hillsdon.reviki.vc.PageStoreException;

import org.tmatesoft.svn.core.io.SVNRepository;

/**
 * Returns appropriate properties for a file.
 * 
 * Split into {@link #read()} and {@link #apply(String)}
 * as {@link SVNRepository} isn't re-entrant.
 * 
 * @author mth
 */
public interface AutoPropertiesApplier {
  
  /**
   * Updates the known properties from the page store if necessary,
   * 
   * @throws PageStoreException If we fail to read them.
   */
  void read() throws PageStoreException;

  /**
   * Applies the current
   *  
   * @param filename
   * @return
   */
  Map<String, String> apply(String filename);
  
}
