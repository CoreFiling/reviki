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
package net.hillsdon.fij.collections;

import java.util.Map;

/**
 * A map with a values() that supports add, finding the
 * key according to a transform.
 * 
 * Note this is contrary to the contract of Map.
 * 
 * @author mth
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public interface TransformMap<K, V> extends Map<K, V> {
  
  /**
   * @param e Entry.
   * @return As for Map.put().
   */
  V put(V e);
  
}
