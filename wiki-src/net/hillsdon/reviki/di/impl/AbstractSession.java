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
package net.hillsdon.reviki.di.impl;

import net.hillsdon.reviki.di.Session;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;

public abstract class AbstractSession implements Session {

  private final MutablePicoContainer _container;
  private final PicoContainer _parentContainer;

  public AbstractSession(final AbstractSession parent, final Object... bonusImplementations) {
    _parentContainer = parent == null ? null : parent._container;
    MutablePicoContainer container = new PicoBuilder(_parentContainer).withCaching().withLifecycle().build();
    for (Object o : bonusImplementations) {
      container.addComponent(o);
    }
    configure(container);
    _container = container;
  }
  
  public void start() {
    _container.start();
  }
  
  protected PicoContainer getParentContainer() {
    return _parentContainer;
  }
  
  protected PicoContainer getContainer() {
    return _container;
  }
  
}
