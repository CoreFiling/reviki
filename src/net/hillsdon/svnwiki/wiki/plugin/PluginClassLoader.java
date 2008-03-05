package net.hillsdon.svnwiki.wiki.plugin;

import java.net.URL;
import java.net.URLClassLoader;

public class PluginClassLoader extends URLClassLoader {

  public PluginClassLoader(final URL jarUrl, final ClassLoader parent) {
    super(new URL[] {jarUrl}, parent);
    findResource("/META-INF/MANIFEST.MF");
  }

}
