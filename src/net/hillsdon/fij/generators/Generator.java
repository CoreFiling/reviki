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