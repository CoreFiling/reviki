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

import java.net.URISyntaxException;
import java.util.Collections;

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.vc.impl.SimplePageStore;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.UnknownWikiException;
import net.hillsdon.reviki.web.urls.impl.ExampleDotComWikiUrls;

public class TestCreoleLinkNode extends TestCase {

  private CreoleLinkNode _node;

  @Override
  protected void setUp() throws Exception {
    SimplePageStore pages = new SimplePageStore();
    pages.set(new PageInfoImpl(null, "ExistingPage", "Content", Collections.<String, String>emptyMap()), "", -1, "");
    pages.set(new PageInfoImpl(null, "ExistingPage1.1", "Content", Collections.<String, String>emptyMap()), "", -1, "");
    _node = new CreoleLinkNode(new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.ANCHOR, pages, new InternalLinker(new ExampleDotComWikiUrls()), new FakeConfiguration()));
  }

  public void testInternal() throws URISyntaxException, UnknownWikiException {
    assertEquals("<a rel='nofollow' class='new-page' href='http://www.example.com/reviki/pages/test-wiki/FooPage'>Tasty</a>", _node.handle(new PageInfoImpl("WhereEver"), _node.find("[[FooPage|Tasty]]"), null, URLOutputFilter.NULL).toHTML());
    assertEquals("<a class='existing-page' href='http://www.example.com/reviki/pages/test-wiki/ExistingPage'>Tasty</a>", _node.handle(new PageInfoImpl("WhereEver"), _node.find("[[ExistingPage|Tasty]]"), null, URLOutputFilter.NULL).toHTML());
  }

  public void testInternalWithDot() throws URISyntaxException, UnknownWikiException {
    assertEquals("<a class='existing-page' href='http://www.example.com/reviki/pages/test-wiki/ExistingPage1.1'>Tasty</a>", _node.handle(new PageInfoImpl("WhereEver"), _node.find("[[ExistingPage1.1|Tasty]]"), null, URLOutputFilter.NULL).toHTML());
  }

  public void testInternalWithAnchor() throws URISyntaxException, UnknownWikiException {
    assertEquals("<a class='existing-page' href='http://www.example.com/reviki/pages/test-wiki/ExistingPage#anchor'>Tasty</a>", _node.handle(new PageInfoImpl("WhereEver"), _node.find("[[ExistingPage#anchor|Tasty]]"), null, URLOutputFilter.NULL).toHTML());
  }

  public void testInterWiki() throws URISyntaxException, UnknownWikiException {
    assertEquals("<a class='inter-wiki' href='http://www.example.com/foo/Wiki?FooPage'>Tasty</a>", _node.handle(new PageInfoImpl("WhereEver"), _node.find("[[foo:FooPage|Tasty]]"), null, URLOutputFilter.NULL).toHTML());
  }

  public void testExternal() throws URISyntaxException, UnknownWikiException {
    assertEquals("<a class='external' href='http://www.example.com'>Tasty</a>", _node.handle(new PageInfoImpl("WhereEver"), _node.find("[[http://www.example.com|Tasty]]"), null, URLOutputFilter.NULL).toHTML());
    // No text, we use URL.  Useful if we fail to match some links.
    assertEquals("<a class='external' href='http://www.example.com/'>http://www.example.com/</a>", _node.handle(new PageInfoImpl("WhereEver"), _node.find("[[http://www.example.com/]]"), null, URLOutputFilter.NULL).toHTML());
    // Backward external link!
    assertEquals("<a rel='nofollow' class='new-page' href='http://www.example.com/reviki/pages/test-wiki/Tasty'>http://www.example.com</a>", _node.handle(new PageInfoImpl("WhereEver"), _node.find("[[Tasty|http://www.example.com]]"), null, URLOutputFilter.NULL).toHTML());
  }

  public void testAttachments() throws URISyntaxException, UnknownWikiException {
    // The class isn't too clever here.
    assertEquals("<a class='attachment' href='http://www.example.com/reviki/pages/test-wiki/WhereEver/attachments/attachment.txt'>Read this</a>", _node.handle(new PageInfoImpl("WhereEver"), _node.find("[[attachment.txt|Read this]]"), null, URLOutputFilter.NULL).toHTML());
    assertEquals("<a class='attachment' href='http://www.example.com/reviki/pages/test-wiki/ElseWhere/attachments/attachment.txt'>Read this too</a>", _node.handle(new PageInfoImpl("WhereEver"), _node.find("[[ElseWhere/attachment.txt|Read this too]]"), null, URLOutputFilter.NULL).toHTML());
  }

  public void testAttachmentWithoutExtension() throws URISyntaxException, UnknownWikiException {
    assertEquals("<a class='attachment' href='http://www.example.com/reviki/pages/test-wiki/WhereEver/attachments/attachment'>Read this</a>", _node.handle(new PageInfoImpl("WhereEver"), _node.find("[[attachments/attachment|Read this]]"), null, URLOutputFilter.NULL).toHTML());
  }

  public void testInterWikiAttachment() {
    // This'd be nice, e.g. other:SomePage/attached.txt
  }

}
