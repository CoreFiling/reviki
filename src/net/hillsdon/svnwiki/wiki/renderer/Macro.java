package net.hillsdon.svnwiki.wiki.renderer;

import net.hillsdon.svnwiki.vc.PageReference;

/**
 * A very simple macro interface.
 *
 * We'll probably want to have standardized parameter parsing outside of the macro.
 * 
 * @author mth
 */
public interface Macro {

  String getName();

  String handle(PageReference page, String remainder);
  
}
