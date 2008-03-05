package net.hillsdon.fij.generators;

public class GeneratorUtil {

  public static <T> Generator<T> emptyGenerator() {
    return new Generator<T>() {
      public T next() throws StopIteration {
        throw new StopIteration();
      }
    };
  }
  
  private GeneratorUtil() {
  }
  
}
