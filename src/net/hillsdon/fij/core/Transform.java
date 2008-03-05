package net.hillsdon.fij.core;

public interface Transform<In, Out> {

  Out transform(In in);
  
}
