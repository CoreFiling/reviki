package net.hillsdon.fij.core;

@SuppressWarnings("unchecked")
public final class Predicates {

  private static final Predicate ALL = new Predicate() {
    public Boolean transform(Object in){
      return true;
    }
  };

  public static <T> Predicate<T> all() {
    return (Predicate<T>) ALL;
  }

  private Predicates() {
  }
  
}
