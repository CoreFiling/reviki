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
package net.hillsdon.fij.generators;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p>Adapts iterator to the generator protocol.  Included for completeness,
 * in general this isn't the best way round to do things.</p>
 * 
 * <p>Note the implementation relys on the iterator correctly throwing
 * NoSuchElementException!</p> 
 * 
 * @author mth
 *
 * @param <T> Return type.
 */
public class IteratorGenerator<T> implements Generator<T> {

  private final Iterator<T> _iterator;
  
  public IteratorGenerator(final Iterator<T> iterator) {
    _iterator = iterator;
  }
  
  public T next() throws StopIteration {
    try {
      return _iterator.next();
    }
    catch (NoSuchElementException ex) {
      throw new StopIteration();
    }
  }

}
