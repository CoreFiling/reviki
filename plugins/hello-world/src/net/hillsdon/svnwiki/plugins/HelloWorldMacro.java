package net.hillsdon.reviki.plugins;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;
import net.hillsdon.reviki.wiki.renderer.macro.ResultFormat;

public class HelloWorldMacro implements Macro {

  public String getName() {
    return "hello-world";
  }

  public ResultFormat getResultFormat() {
    return ResultFormat.WIKI;
  }

  public String handle(final PageReference page, final String remainder) throws Exception {
    return "Hello " + remainder;
  }
  
}
