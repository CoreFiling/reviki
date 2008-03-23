package net.hillsdon.reviki.di.impl;

import net.hillsdon.reviki.di.Session;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;

public abstract class AbstractSession implements Session {

  private final PicoContainer _container;
  private final PicoContainer _parentContainer;

  public AbstractSession(final AbstractSession parent, final Object... bonusImplementations) {
    _parentContainer = parent == null ? null : parent._container;
    MutablePicoContainer container = new PicoBuilder(_parentContainer).withCaching().build();
    for (Object o : bonusImplementations) {
      container.addComponent(o);
    }
    configure(container);
    _container = container;
  }
  
  protected PicoContainer getParentContainer() {
    return _parentContainer;
  }
  
  protected PicoContainer getContainer() {
    return _container;
  }
  
}
