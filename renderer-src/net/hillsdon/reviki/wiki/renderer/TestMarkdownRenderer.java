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

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.wiki.renderer.creole.JsonDrivenRenderingTest;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;
import net.hillsdon.reviki.wiki.renderer.macro.ResultFormat;

public class TestMarkdownRenderer extends JsonDrivenRenderingTest {

  private final Macro _revikiMacro = new Macro() {
    @Override
    public String handle(final PageInfo page, final String remainder) throws Exception {
      return "//Reviki: " + (remainder == null ? "(no args)" : remainder) + "//";
    }
    @Override
    public ResultFormat getResultFormat() {
      return ResultFormat.WIKI;
    }
    @Override
    public String getName() {
      return "revikimacro";
    }
  };

  private final Macro _htmlMacro = new Macro() {
    @Override
    public String handle(final PageInfo page, final String remainder) throws Exception {
      return "<i>HTML: " + (remainder == null ? "(no args)" : remainder) + "</i>";
    }
    @Override
    public ResultFormat getResultFormat() {
      return ResultFormat.XHTML;
    }
    @Override
    public String getName() {
      return "htmlmacro";
    }
  };

  public TestMarkdownRenderer() throws JsonParseException, IOException {
    super(TestMarkdownRenderer.class.getResource("markdown.json"));
    macros = Suppliers.ofInstance((List<Macro>) ImmutableList.of(_revikiMacro, _htmlMacro));
  }

  @Override
  protected String render(final String input) throws IOException, PageStoreException {
    MarkdownRenderer renderer = new MarkdownRenderer(pageStore, linkHandler, imageHandler, macros);
    return renderer.render(input).get();
  }
}
