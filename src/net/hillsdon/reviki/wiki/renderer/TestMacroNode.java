/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.wiki.renderer;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import net.hillsdon.fij.accessors.Holder;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;
import net.hillsdon.reviki.wiki.renderer.macro.ResultFormat;

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
    String result = macroNode.handle(new PageReferenceImpl("WhatEver"), matcher, null, URLOutputFilter.NULL).toXHTML();
    assertTrue(result.contains("Simulated &amp; escape me please."));
  }
  
}
