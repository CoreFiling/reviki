package net.hillsdon.fij.generators;

import java.util.Iterator;
import java.util.NoSuchElementException;



/**
 * Adapts the Generator interface to the more complicated Iterator
 * protocol.
 * 
 * Ideally we'd have yield like Python but at least this simplifies
 * things somewhat.
 * 
 * @param <T> Return type.
 */
public class GeneratorIterator<T> implements Iterator<T> {

  private enum State {
    NOT_LOOKED,
    HAS_NEXT,
    EXHAUSTED,
  }
  
  private final Generator<T> _generator;
  private State _state = State.NOT_LOOKED;
  private T _next;
  
  public GeneratorIterator(Generator<T> generator) {
    _generator = generator;
  }
  
  private boolean updateNext() {
    if (_state == State.NOT_LOOKED) {
      try {
        _next = _generator.next();
        _state = State.HAS_NEXT;
      }
      catch (StopIteration ex) {
        _state = State.EXHAUSTED;
      }
    }
    return _state == State.HAS_NEXT;
  }
  
  public boolean hasNext() {
    return updateNext();
  }

  public T next() {
    if (updateNext()) {
      _state = State.NOT_LOOKED;
      return _next;
    }
    throw new NoSuchElementException();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

}
