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
package net.hillsdon.fij.core;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * General purpose functional utilities.
 * 
 * @author mth
 */
public class Functional {

  private static abstract class CheckedIter<T> implements Iterator<T> {

    public T next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return internalNext();
    }

    protected abstract T internalNext();

    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

  public static <T> Iterator<T> iter(final T... array) {
    return new CheckedIter<T>() {

      private int _index = 0;

      public boolean hasNext() {
        return _index < array.length;
      }
      protected T internalNext() {
        return array[_index++];
      }

    };
  }

  public static <T> Iterable<T> iterable(final Iterator<T> iter) {
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        return iter;
      }
    };
  }

  public static <In, Out> Iterator<Out> map(final Iterable<? extends In> in, final Transform<In, ? extends Out> transform) {
    return map(in.iterator(), transform);
  }

  public static <In, Out> Iterator<Out> map(final Iterator<? extends In> in, final Transform<In, ? extends Out> transform) {
    return new Iterator<Out>() {
      public boolean hasNext() {
        return in.hasNext();
      }
      public Out next() {
        return transform.transform(in.next());
      }
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static <T> Iterator<T> filter(final Iterable<? extends T> in, final Predicate<T> predicate) {
    return filter(in.iterator(), predicate);
  }

  public static <T> Iterator<T> filter(final Iterator<? extends T> in, final Predicate<T> predicate) {
    return new Iterator<T>() {

      private boolean _hasNext = false;
      private T _next;
      
      {
        findMatchingNext();
      }

      public boolean hasNext() {
        findMatchingNext();
        return _hasNext;
      }

      private void findMatchingNext() {
        if (_hasNext) {
          return;
        }
        while (in.hasNext()) {
          T next = in.next();
          if (predicate.transform(next)) {
            _next = next;
            _hasNext = true;
            break;
          }
        }
      }

      public T next() {
        findMatchingNext();
        _hasNext = false;
        try {
          return _next;
        }
        finally {
          _next = null;
        }
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  public static <T> List<T> list(final Iterator<T> iter) {
    return list(iterable(iter));
  }

  public static <T> List<T> list(final Iterable<T> iterable) {
    if (iterable instanceof List) {
      return (List<T>) iterable;
    }
    List<T> result = new ArrayList<T>();
    for (T o : iterable) {
      result.add(o);
    }
    return result;
  }

  public static <T> Set<T> set(final T... items) {
    return set(asList(items));
  }

  public static <T> Set<T> set(final Iterator<T> iter) {
    return set(iterable(iter));
  }

  public static <T> Set<T> set(final Iterable<T> iterable) {
    if (iterable instanceof LinkedHashSet) {
      return (Set<T>) iterable;
    }
    Set<T> result = new LinkedHashSet<T>();
    for (T o : iterable) {
      result.add(o);
    }
    return result;
  }

}
