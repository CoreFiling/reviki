package net.hillsdon.svnwiki.wiki.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;

/**
 * A really simple system for plugins.
 * 
 * The relative entries in manifest entry Class-Path are assumed to point to jars
 * in the root of the plugin jar.
 * 
 * The classes constributed by this plugin are defined by the Plugin-Contributions
 * manifest parameter (space separated).
 * 
 * @author mth
 */
public class PluginClassLoader extends URLClassLoader {

  public PluginClassLoader(final URL jarUrl, final ClassLoader parent) throws InvalidPluginException {
    super(new URL[] {jarUrl}, parent);
    try {
      URL last = getManifestURL(jarUrl);
      
      Manifest manifest = new Manifest(last.openStream());
      Attributes attrs = manifest.getMainAttributes();
      
      String[] classPathEntries = attrs.getValue("Class-Path").split("\\s");
      cacheClassPathEntries(jarUrl, classPathEntries);
      String[] pluginContributions = attrs.getValue("Plugin-Contributions").split("\\s");
      loadPluginContributionClasses(pluginContributions);
    }
    catch (URISyntaxException ex) {
      throw new InvalidPluginException(ex);
    }
    catch (IOException ex) {
      throw new InvalidPluginException(ex);
    }
  }

  private void loadPluginContributionClasses(final String[] pluginContributions) {
  }

  private void cacheClassPathEntries(final URL jarUrl, final String[] classPathEntries) throws IOException {
    for (String entry : classPathEntries) {
      URL entryUrl = new URL("jar:" + jarUrl.toString() + "!/" + entry);
      File file = File.createTempFile("cached-", entry);
      file.deleteOnExit();
      InputStream in = entryUrl.openStream();
      FileOutputStream out = null;
      try {
        out = new FileOutputStream(file);
        IOUtils.copy(in, out);
      }
      catch (IOException ex) {
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
      }
      addURL(file.toURI().toURL());
    }
  }

  private URL getManifestURL(final URL jarUrl) throws IOException, URISyntaxException, InvalidPluginException {
    Enumeration<URL> enumeration = getResources("META-INF/MANIFEST.MF");
    URL last = null;
    while (enumeration.hasMoreElements()) {
      last = enumeration.nextElement();
    }
    if (last == null) {
      throw new InvalidPluginException("No manifest found in given jar.");
    }
    return last;
  }

}
