package net.hillsdon.fij.generators;

/**
 * <p>A safer iteration protocol where the implementer doesn't have to 
 * worry about hasNext().</p>  
 * 
 * <p>This comes at the expense of looking ahead one item so may not be
 * suitable if calls to next are expensive.</p>
 * 
 * <p>Don't try too hard to fight Java, Iterator is only awkward for
 * the implementor so write generators but use iterators, wrapping
 * generators in {@link GeneratorIterator}.</p>
 * 
 * @author mth
 *
 * @param <T> Return type.
 */
public interface Generator<T> {
  
  /**
   * @return The next value.
   * @throws StopIteration If there are no more values.
   * @throws IterationFailed If there's a problem we can't otherwise report (due to need to stay Iterator compatible).
   */
  T next() throws StopIteration, IterationFailed;
  
}