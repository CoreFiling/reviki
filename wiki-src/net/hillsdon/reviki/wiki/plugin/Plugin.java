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
package net.hillsdon.reviki.wiki.plugin;

import java.net.URL;
import java.util.List;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

public class Plugin implements ContributedImplementationProvider {

  private final PicoContainer _container;

  public Plugin(final String name, final URL url, final PicoContainer context) throws InvalidPluginException {
    MutablePicoContainer container = new DefaultPicoContainer(context);
    for (Class<?> clazz : getContributedClasses(url)) {
      container.addComponent(clazz);
    }
    _container = container;
  }

  public <T> List<T> getImplementations(final Class<T> clazz) {
    return _container.getComponents(clazz);
  }
  
  private List<Class<?>> getContributedClasses(final URL url) throws InvalidPluginException {
    return new PluginClassLoader(url, getClass().getClassLoader()).getContributedClasses();
  }
  
}
