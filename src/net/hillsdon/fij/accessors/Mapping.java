package net.hillsdon.fij.accessors;

/**
 * Map is too big for some purposes.
 * 
 * Do not add directly to this interface!
 * 
 * @author mth
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public interface Mapping<K, V> {

  V get(K k);
  
  void set(K k, V v);
  
}
