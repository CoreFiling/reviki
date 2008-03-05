package net.hillsdon.fij.core;

public class InversePredicate<T> implements Predicate<T> {

  private final Predicate<T> _predicate;

  public InversePredicate(final Predicate<T> predicate) {
    _predicate = predicate;
  }

  public Boolean transform(final T in) {
    return !_predicate.transform(in);
  }

}
