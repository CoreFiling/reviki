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
package net.hillsdon.reviki.web.handlers;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.hillsdon.reviki.configuration.PerWikiInitialConfiguration;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.InvalidInputException;
import net.hillsdon.reviki.web.common.MockHttpServletRequest;
import net.hillsdon.reviki.web.common.RedirectView;
import net.hillsdon.reviki.web.common.RequestBasedWikiUrls;

/**
 * Test for {@link SetPage}.
 * 
 * @author mth
 */
public class TestSetPage extends TestCase {

  private static final PageReference CALLED_ON_PAGE = new PageReference("CalledOnPage");
  
  private SetPage _page;
  private PageStore _store;
  private MockHttpServletRequest _request;

  private HttpServletResponse _response;

  @Override
  protected void setUp() throws Exception {
    _store = createMock(PageStore.class);
    _page = new SetPage(_store);
    _request = new MockHttpServletRequest();
    _request.setContextPath("/reviki");
    _request.setRequestURL("http://www.example.com/reviki/pages/" + CALLED_ON_PAGE.getPath());
    _request.setRequestURI("/reviki/pages/" + CALLED_ON_PAGE.getPath());
    _response = null;
    RequestBasedWikiUrls.create(_request, new PerWikiInitialConfiguration(null, "", ""));
  }
  
  public void testNoActionIsInvalidInputException() throws Exception {
    try {
      _page.handlePage(ConsumedPath.EMPTY, _request, _response, CALLED_ON_PAGE);
      fail();
    }
    catch (InvalidInputException expected) {
    }
  }
  
  public void testClashingOptionsIsInvalidInputException() throws Exception {
    try {
      _request.setParameter(SetPage.SUBMIT_SAVE, "");
      _request.setParameter(SetPage.SUBMIT_COPY, "");
      _page.handlePage(ConsumedPath.EMPTY, _request, _response, CALLED_ON_PAGE);
      fail();
    }
    catch (InvalidInputException expected) {
      assertEquals("Exactly one action must be specified.", expected.getMessage());
    }
  }
  
  public void testSaveRequiresLockToken() throws Exception {
    try {
      _request.setParameter(SetPage.SUBMIT_SAVE, "");
      _request.setParameter(SetPage.PARAM_CONTENT, "Content");
      _request.setParameter(SetPage.PARAM_COMMIT_MESSAGE, "Message");
      _request.setParameter(SetPage.PARAM_BASE_REVISION, "1");
      _page.handlePage(ConsumedPath.EMPTY, _request, _response, CALLED_ON_PAGE);
      fail();
    }
    catch (InvalidInputException expected) {
      assertTrue(expected.getMessage().contains(SetPage.PARAM_LOCK_TOKEN));
    }
  }
  
  public void testSaveRequiresBaseRevision() throws Exception {
    try {
      _request.setParameter(SetPage.SUBMIT_SAVE, "");
      _request.setParameter(SetPage.PARAM_LOCK_TOKEN, "dooby");
      _request.setParameter(SetPage.PARAM_CONTENT, "Content");
      _request.setParameter(SetPage.PARAM_COMMIT_MESSAGE, "Message");
      _page.handlePage(ConsumedPath.EMPTY, _request, _response, CALLED_ON_PAGE);
      fail();
    }
    catch (InvalidInputException expected) {
      assertTrue(expected.getMessage().contains(SetPage.PARAM_BASE_REVISION));
    }
  }

  public void testSaveDelegatesToPageStoreThenRedirectsToView() throws Exception {
    _request.setParameter(SetPage.SUBMIT_SAVE, "");
    _request.setParameter(SetPage.PARAM_BASE_REVISION, "1");
    _request.setParameter(SetPage.PARAM_LOCK_TOKEN, "dooby");
    _request.setParameter(SetPage.PARAM_CONTENT, "Content");
    _request.setParameter(SetPage.PARAM_COMMIT_MESSAGE, "Message");

    final String expectedCommitMessage = "Message\n" + _request.getRequestURL();
    final String expectedContent = "Content" + SetPage.CRLF;
    expect(_store.set(CALLED_ON_PAGE, "dooby", 1, expectedContent, expectedCommitMessage)).andReturn(2L).once();
    replay(_store);
    RedirectView view = (RedirectView) _page.handlePage(ConsumedPath.EMPTY, _request, _response, CALLED_ON_PAGE);
    assertEquals(_request.getRequestURL().toString(), view.getURL());
    verify(_store);
  }

  public void testCommitMessageIndicatesMinorEditIfAndOnlyIfParameterSet() throws Exception {
    _request.setParameter(SetPage.PARAM_COMMIT_MESSAGE, "Message");
    assertEquals("Message\n" + _request.getRequestURL(), SetPage.createLinkingCommitMessage(_request));
    _request.setParameter(SetPage.PARAM_MINOR_EDIT, "");
    assertEquals("[minor edit]\nMessage\n" + _request.getRequestURL(), SetPage.createLinkingCommitMessage(_request));
  }
  
  public void testCopyRequiresOneOfToPageOrFromPage() throws Exception {
    try {
      _request.setParameter(SetPage.SUBMIT_COPY, "");
      _page.handlePage(ConsumedPath.EMPTY, _request, _response, CALLED_ON_PAGE);
      fail();
    }
    catch (InvalidInputException ex) {
      assertTrue(ex.getMessage().contains(SetPage.PARAM_TO_PAGE));
      assertTrue(ex.getMessage().contains(SetPage.PARAM_FROM_PAGE));
    }
  }
  
  public void testCopyTo() throws Exception {
    _request.setParameter(SetPage.SUBMIT_COPY, "");
    _request.setParameter(SetPage.PARAM_TO_PAGE, "ToPage");
    expect(_store.copy(CALLED_ON_PAGE, -1, new PageReference("ToPage"), "[reviki commit]\n" + _request.getRequestURL())).andReturn(2L).once();
    replay(_store);
    RedirectView view = (RedirectView) _page.handlePage(ConsumedPath.EMPTY, _request, _response, CALLED_ON_PAGE);
    assertEquals(RequestBasedWikiUrls.get(_request).page("ToPage"), view.getURL());
    verify(_store);
  }
  
  public void testCopyFrom() throws Exception {
    _request.setParameter(SetPage.SUBMIT_COPY, "");
    _request.setParameter(SetPage.PARAM_FROM_PAGE, "FromPage");
    expect(_store.copy(new PageReference("FromPage"), -1, CALLED_ON_PAGE, "[reviki commit]\n" + _request.getRequestURL())).andReturn(2L).once();
    replay(_store);
    RedirectView view = (RedirectView) _page.handlePage(ConsumedPath.EMPTY, _request, _response, CALLED_ON_PAGE);
    assertEquals(RequestBasedWikiUrls.get(_request).page(CALLED_ON_PAGE.getPath()), view.getURL());
    verify(_store);
  }
  
  public void testRenameRequiresToPage() throws Exception {
    _request.setParameter(SetPage.SUBMIT_RENAME, "");
    try {
      _page.handlePage(ConsumedPath.EMPTY, _request, _response, CALLED_ON_PAGE);
      fail();
    }
    catch (InvalidInputException ex) {
      assertTrue(ex.getMessage().contains(SetPage.PARAM_TO_PAGE));
    }
  }
  
  public void testRenameTo() throws Exception {
    _request.setParameter(SetPage.SUBMIT_RENAME, "");
    _request.setParameter(SetPage.PARAM_TO_PAGE, "ToPage");
    expect(_store.rename(CALLED_ON_PAGE, new PageReference("ToPage"), -1, "[reviki commit]\n" + _request.getRequestURL())).andReturn(2L).once();
    replay(_store);
    RedirectView view = (RedirectView) _page.handlePage(ConsumedPath.EMPTY, _request, _response, CALLED_ON_PAGE);
    assertEquals(RequestBasedWikiUrls.get(_request).page("ToPage"), view.getURL());
    verify(_store);
  }

  public void testUnlockDoesNothingIfNoLockTokenProvided() throws Exception {
    _request.setParameter(SetPage.SUBMIT_UNLOCK, "");
    replay(_store);
    RedirectView view = (RedirectView) _page.handlePage(ConsumedPath.EMPTY, _request, _response, CALLED_ON_PAGE);
    assertEquals(_request.getRequestURL().toString(), view.getURL());
    verify(_store);
  }

  public void testUnlockUnlocksIfLockTokenProvided() throws Exception {
    _request.setParameter(SetPage.SUBMIT_UNLOCK, "");
    _request.setParameter(SetPage.PARAM_LOCK_TOKEN, "dooby");
    _store.unlock(CALLED_ON_PAGE, "dooby");
    expectLastCall().once();
    replay(_store);
    RedirectView view = (RedirectView) _page.handlePage(ConsumedPath.EMPTY, _request, _response, CALLED_ON_PAGE);
    assertEquals(_request.getRequestURL().toString(), view.getURL());
    verify(_store);
  }
  
}
