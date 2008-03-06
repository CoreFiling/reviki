package net.hillsdon.svnwiki.plugins;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.wiki.renderer.macro.Macro;
import net.hillsdon.svnwiki.wiki.renderer.macro.ResultFormat;

public class HelloWorldMacro implements Macro {

  public String getName() {
    return "hello-world";
  }

  public ResultFormat getResultFormat() {
    return ResultFormat.WIKI;
  }

  public String handle(final PageReference page, final String remainder) throws Exception {
    return "Hello, World!";
  }
  
}
