package net.hillsdon.reviki.wiki.macros;

import java.util.Map;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.wiki.renderer.context.PageRenderContext;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;
import net.hillsdon.reviki.wiki.renderer.macro.ResultFormat;

/**
 * This marks a piece of wiki markup as being the value for a key value pair.
 * e.g. {@code <<keyedValue:(key="foo", value="wiki **mark**up">>}
 */
public class KeyedValue implements Macro {
  private final MacroArgumentParser _parser = new MacroArgumentParser("key", "value");

  public String getName() {
    return "keyedValue";
  }

  public ResultFormat getResultFormat() {
    return ResultFormat.WIKI;
  }

  public String handle(PageReference page, String remainder, PageRenderContext context) throws Exception {
    Map<String, String> args = _parser.parse(remainder);

    String key = args.get("key");
    String value = args.get("value");

    context.setPageProperties(key, value);
    return value;
  }
}
