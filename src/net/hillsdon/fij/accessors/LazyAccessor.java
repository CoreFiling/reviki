package net.hillsdon.fij.accessors;

import net.hillsdon.fij.core.Factory;

public class LazyAccessor<T> implements Accessor<T> {

  private final Factory<T> _factory;
  private boolean _initialized = false;
  private T _cached;
  
  public LazyAccessor(final Factory<T> factory) {
    _factory = factory;
  }
  
  public T get() {
    if (!_initialized) {
      _cached = _factory.newInstance();
      _initialized = true;
    }
    return _cached;
  }

}
