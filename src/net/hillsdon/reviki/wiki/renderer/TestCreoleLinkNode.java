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

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.impl.SimplePageStore;
import net.hillsdon.reviki.wiki.InternalLinker;

public class TestCreoleLinkNode extends TestCase {

  private CreoleLinkNode _node;

  @Override
  protected void setUp() throws Exception {
    SimplePageStore pages = new SimplePageStore();
    pages.set(new PageReference("ExistingPage"), "", -1, "Content", "");
    _node = new CreoleLinkNode(new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.ANCHOR, new InternalLinker("/context", "wiki", pages), new FakeConfiguration()));
  }
  
  public void testInternal() {
    assertEquals("<a rel='nofollow' class='new-page' href='/context/pages/wiki/FooPage'>Tasty</a>", _node.handle(new PageReference("WhereEver"), _node.find("[[FooPage|Tasty]]"), null).toXHTML());
    assertEquals("<a class='existing-page' href='/context/pages/wiki/ExistingPage'>Tasty</a>", _node.handle(new PageReference("WhereEver"), _node.find("[[ExistingPage|Tasty]]"), null).toXHTML());
  }

  public void testInterWiki() {
    assertEquals("<a class='inter-wiki' href='http://www.example.com/foo/Wiki?FooPage'>Tasty</a>", _node.handle(new PageReference("WhereEver"), _node.find("[[foo:FooPage|Tasty]]"), null).toXHTML());
  }
  
  public void testExternal() {
    assertEquals("<a class='external' href='http://www.example.com'>Tasty</a>", _node.handle(new PageReference("WhereEver"), _node.find("[[http://www.example.com|Tasty]]"), null).toXHTML());
  }
  
  public void testAttachments() {
    // The class isn't too clever here.
    assertEquals("<a class='attachment' href='WhereEver/attachments/attachment.txt'>Read this</a>", _node.handle(new PageReference("WhereEver"), _node.find("[[attachment.txt|Read this]]"), null).toXHTML());
    assertEquals("<a class='attachment' href='ElseWhere/attachments/attachment.txt'>Read this too</a>", _node.handle(new PageReference("WhereEver"), _node.find("[[ElseWhere/attachment.txt|Read this too]]"), null).toXHTML());
  }
  
  public void testInterWikiAttachment() {
    // This'd be nice, e.g. other:SomePage/attached.txt
  }
  
}
