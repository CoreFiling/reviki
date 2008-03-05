/**
 * 
 */
package net.hillsdon.svnwiki.wiki.renderer;

import net.hillsdon.svnwiki.configuration.Configuration;
import net.hillsdon.svnwiki.configuration.InterWikiLinker;
import net.hillsdon.svnwiki.vc.PageStoreException;

public class FakeConfiguration implements Configuration {
  public InterWikiLinker getInterWikiLinker() throws PageStoreException {
    InterWikiLinker linker = new InterWikiLinker();
    linker.addWiki("foo", "http://www.example.com/foo/Wiki?%s");
    return linker;
  }
}