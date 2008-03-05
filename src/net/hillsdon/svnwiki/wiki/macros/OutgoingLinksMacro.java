package net.hillsdon.svnwiki.wiki.macros;

import java.io.IOException;
import java.util.Collection;

import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.wiki.graph.WikiGraph;

public class OutgoingLinksMacro extends AbstractListOfPagesMacro {

  private final WikiGraph _wikiGraph;

  public OutgoingLinksMacro(final WikiGraph wikiGraph) {
    _wikiGraph = wikiGraph;
  }
  
  public String getName() {
    return "incomingLinks";
  }

  @Override
  protected Collection<String> getPages(final String remainder) throws IOException, PageStoreException {
    return _wikiGraph.incomingLinks(remainder);
  }

}
