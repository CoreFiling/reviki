package net.hillsdon.svnwiki.wiki.renderer;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import net.hillsdon.fij.accessors.Holder;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.wiki.renderer.macro.Macro;
import net.hillsdon.svnwiki.wiki.renderer.macro.ResultFormat;

/**
 * Test for {@link MacroNode}.
 * 
 * @author mth
 */
public class TestMacroNode extends TestCase {

  public void testHandlesExceptionsInternally() throws Exception {
    Macro npes = new Macro() {
      public String getName() {
        return "npes";
      }
      public ResultFormat getResultFormat() {
        return ResultFormat.WIKI;
      }
      public String handle(final PageReference page, final String remainder) throws Exception {
        throw new NullPointerException("Simulated & escape me please.");
      }
    };
    MacroNode macroNode = new MacroNode(new Holder<List<Macro>>(asList(npes)), false);
    Matcher matcher = Pattern.compile("([a-z]+) ([a-z]+)").matcher("npes remainder");
    matcher.matches();
    String result = macroNode.handle(new PageReference("WhatEver"), matcher, null);
    assertTrue(result.contains("Simulated &amp; escape me please."));
  }
  
}
