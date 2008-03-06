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
