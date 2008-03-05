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
