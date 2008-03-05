package net.hillsdon.fij.accessors;


public interface MutableAccessor<T> extends Accessor<T> {

  void set(T value);
  
}
