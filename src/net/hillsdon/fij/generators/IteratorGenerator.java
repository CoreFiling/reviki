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
