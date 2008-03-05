package net.hillsdon.fij.accessors;

public class Holder<T> implements MutableAccessor<T> {

  private T _value;

  public Holder(T value) {
    _value = value;
  }
  
  public void set(final T value) {
    _value = value;
  }

  public T get() {
    return _value;
  }

}
