package net.hillsdon.fij.core;

/**
 * A factory creates new objects.
 * 
 * @author mth
 *
 * @param <T> Return type.
 */
public interface Factory<T> {
  
  T newInstance();
  
}
