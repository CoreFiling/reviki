package net.hillsdon.fij.core;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class IterableUtils {

  public static <T> Iterable<T> reversed(final List<T> in) {
    if (in == null) {
      throw new NullPointerException("Iterable may not be null.");
    }
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          private final ListIterator<T> _underlying;
          {
            _underlying = in.listIterator(in.size());
          }
          public boolean hasNext() {
            return _underlying.hasPrevious();
          }
          public T next() {
            return _underlying.previous();
          }
          public void remove() {
            _underlying.remove();
          }
        };
      }
    };
  }
  
}
