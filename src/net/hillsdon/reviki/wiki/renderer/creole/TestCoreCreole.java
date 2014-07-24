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
package net.hillsdon.reviki.wiki.renderer.creole;

import java.io.IOException;
import java.util.Collections;

import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.CreoleLinkNode;

import org.codehaus.jackson.JsonParseException;

public class TestCoreCreole extends JsonDrivenRenderingTest {

  public TestCoreCreole() throws JsonParseException, IOException {
    super(TestCoreCreole.class.getResource("core-creole.json"));
  }

  @Override
  protected String render(final String input) {
    RenderNode[] inlineLinks = new RenderNode[2];

    inlineLinks[0] = new CreoleLinkNode(linkHandler);
    inlineLinks[1] = new CreoleImageNode(imageHandler);

    return new CreoleRenderer(CreoleRenderer.NONE, inlineLinks).render(new PageInfoImpl("", "", input, Collections.<String, String> emptyMap()), URLOutputFilter.NULL).toXHTML();
  }

}
