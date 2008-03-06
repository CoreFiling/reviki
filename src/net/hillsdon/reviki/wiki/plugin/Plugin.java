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
