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
package net.hillsdon.reviki.web.pages.impl;

import static net.hillsdon.reviki.web.pages.impl.DefaultPageImpl.PARAM_LOCK_TOKEN;
import static net.hillsdon.reviki.web.pages.impl.DefaultPageImpl.SUBMIT_COPY;
import static net.hillsdon.reviki.web.pages.impl.DefaultPageImpl.SUBMIT_SAVE;
import static net.hillsdon.reviki.web.pages.impl.DefaultPageImpl.SUBMIT_UNLOCK;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.hillsdon.fij.text.Strings;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.impl.CachingPageStore;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.InvalidInputException;
import net.hillsdon.reviki.web.common.MockHttpServletRequest;
import net.hillsdon.reviki.web.common.RedirectView;
import net.hillsdon.reviki.web.common.RequestBasedWikiUrls;
import net.hillsdon.reviki.web.pages.DefaultPage;

/**
 * Test for {@link SetPageImpl}.
 * 
 * @author mth
 */
public class TestDefaultPageImplSet extends TestCase {

  private static final PageReference CALLED_ON_PAGE = new PageReference("CalledOnPage");
  
  private CachingPageStore _store;
  private MockHttpServletRequest _request;
  private HttpServletResponse _response;

  private DefaultPage _page;

  @Override
  protected void setUp() throws Exception {
    _store = createMock(CachingPageStore.class);
    _page = new DefaultPageImpl(_store, null, null, null);
    _request = new MockHttpServletRequest();
    _request.setContextPath("/reviki");
    _request.setRequestURL("http://www.example.com/reviki/pages/" + CALLED_ON_PAGE.getPath());
    _request.setRequestURI("/reviki/pages/" + CALLED_ON_PAGE.getPath());
    _response = null;
    WikiConfiguration configuration = createMock(WikiConfiguration.class);
    expect(configuration.getGivenWikiName()).andReturn("foo").anyTimes();
    replay(configuration);
    RequestBasedWikiUrls.create(_request, configuration);
  }
  
  public void testNoActionIsInvalidInputException() throws Exception {
    try {
      _page.set(CALLED_ON_PAGE, ConsumedPath.EMPTY, _request, _response);
      fail();
    }
    catch (InvalidInputException expected) {
    }
  }
  
  public void testClashingOptionsIsInvalidInputException() throws Exception {
    try {
      _request.setParameter(SUBMIT_SAVE, "");
      _request.setParameter(SUBMIT_COPY, "");
      _page.set(CALLED_ON_PAGE, ConsumedPath.EMPTY, _request, _response);
      fail();
    }
    catch (InvalidInputException expected) {
      assertEquals("Exactly one action must be specified.", expected.getMessage());
    }
  }
  
  public void testSaveRequiresLockToken() throws Exception {
    try {
      _request.setParameter(DefaultPageImpl.SUBMIT_SAVE, "");
      _request.setParameter(DefaultPageImpl.PARAM_CONTENT, "Content");
      _request.setParameter(DefaultPageImpl.PARAM_COMMIT_MESSAGE, "Message");
      _request.setParameter(DefaultPageImpl.PARAM_BASE_REVISION, "1");
      _page.set(CALLED_ON_PAGE, ConsumedPath.EMPTY, _request, _response);
      fail();
    }
    catch (InvalidInputException expected) {
      assertTrue(expected.getMessage().contains(DefaultPageImpl.PARAM_LOCK_TOKEN));
    }
  }
  
  public void testSaveRequiresBaseRevision() throws Exception {
    try {
      _request.setParameter(DefaultPageImpl.SUBMIT_SAVE, "");
      _request.setParameter(DefaultPageImpl.PARAM_LOCK_TOKEN, "dooby");
      _request.setParameter(DefaultPageImpl.PARAM_CONTENT, "Content");
      _request.setParameter(DefaultPageImpl.PARAM_COMMIT_MESSAGE, "Message");
      _page.set(CALLED_ON_PAGE, ConsumedPath.EMPTY, _request, _response);
      fail();
    }
    catch (InvalidInputException expected) {
      assertTrue(expected.getMessage().contains(DefaultPageImpl.PARAM_BASE_REVISION));
    }
  }

  public void testSaveDelegatesToPageStoreThenRedirectsToView() throws Exception {
    _request.setParameter(DefaultPageImpl.SUBMIT_SAVE, "");
    _request.setParameter(DefaultPageImpl.PARAM_BASE_REVISION, "1");
    _request.setParameter(DefaultPageImpl.PARAM_LOCK_TOKEN, "dooby");
    _request.setParameter(DefaultPageImpl.PARAM_CONTENT, "Content");
    _request.setParameter(DefaultPageImpl.PARAM_COMMIT_MESSAGE, "Message");

    final String expectedCommitMessage = "Message\n" + _request.getRequestURL();
    final String expectedContent = "Content" + Strings.CRLF;
    expect(_store.set(CALLED_ON_PAGE, "dooby", 1, expectedContent, expectedCommitMessage)).andReturn(2L).once();
    replay(_store);
    RedirectView view = (RedirectView) _page.set(CALLED_ON_PAGE, ConsumedPath.EMPTY, _request, _response);
    assertEquals(_request.getRequestURL().toString(), view.getURL());
    verify(_store);
  }

  public void testCommitMessageIndicatesMinorEditIfAndOnlyIfParameterSet() throws Exception {
    _request.setParameter(DefaultPageImpl.PARAM_COMMIT_MESSAGE, "Message");
    assertEquals("Message\n" + _request.getRequestURL(), DefaultPageImpl.createLinkingCommitMessage(_request));
    _request.setParameter(DefaultPageImpl.PARAM_MINOR_EDIT, "");
    assertEquals("[minor edit]\nMessage\n" + _request.getRequestURL(), DefaultPageImpl.createLinkingCommitMessage(_request));
  }
  
  public void testCopyRequiresOneOfToPageOrFromPage() throws Exception {
    try {
      _request.setParameter(DefaultPageImpl.SUBMIT_COPY, "");
      _page.set(CALLED_ON_PAGE, ConsumedPath.EMPTY, _request, _response);
      fail();
    }
    catch (InvalidInputException ex) {
      assertTrue(ex.getMessage().contains(DefaultPageImpl.PARAM_TO_PAGE));
      assertTrue(ex.getMessage().contains(DefaultPageImpl.PARAM_FROM_PAGE));
    }
  }
  
  public void testCopyTo() throws Exception {
    _request.setParameter(DefaultPageImpl.SUBMIT_COPY, "");
    _request.setParameter(DefaultPageImpl.PARAM_TO_PAGE, "ToPage");
    expect(_store.copy(CALLED_ON_PAGE, -1, new PageReference("ToPage"), "[reviki commit]\n" + _request.getRequestURL())).andReturn(2L).once();
    replay(_store);
    RedirectView view = (RedirectView) _page.set(CALLED_ON_PAGE, ConsumedPath.EMPTY, _request, _response);
    assertEquals(RequestBasedWikiUrls.get(_request).page("ToPage"), view.getURL());
    verify(_store);
  }
  
  public void testCopyFrom() throws Exception {
    _request.setParameter(DefaultPageImpl.SUBMIT_COPY, "");
    _request.setParameter(DefaultPageImpl.PARAM_FROM_PAGE, "FromPage");
    expect(_store.copy(new PageReference("FromPage"), -1, CALLED_ON_PAGE, "[reviki commit]\n" + _request.getRequestURL())).andReturn(2L).once();
    replay(_store);
    RedirectView view = (RedirectView) _page.set(CALLED_ON_PAGE, ConsumedPath.EMPTY, _request, _response);
    assertEquals(RequestBasedWikiUrls.get(_request).page(CALLED_ON_PAGE.getPath()), view.getURL());
    verify(_store);
  }
  
  public void testRenameRequiresToPage() throws Exception {
    _request.setParameter(DefaultPageImpl.SUBMIT_RENAME, "");
    try {
      _page.set(CALLED_ON_PAGE, ConsumedPath.EMPTY, _request, _response);
      fail();
    }
    catch (InvalidInputException ex) {
      assertTrue(ex.getMessage().contains(DefaultPageImpl.PARAM_TO_PAGE));
    }
  }
  
  public void testRenameTo() throws Exception {
    _request.setParameter(DefaultPageImpl.SUBMIT_RENAME, "");
    _request.setParameter(DefaultPageImpl.PARAM_TO_PAGE, "ToPage");
    expect(_store.rename(CALLED_ON_PAGE, new PageReference("ToPage"), -1, "[reviki commit]\n" + _request.getRequestURL())).andReturn(2L).once();
    replay(_store);
    RedirectView view = (RedirectView) _page.set(CALLED_ON_PAGE, ConsumedPath.EMPTY, _request, _response);
    assertEquals(RequestBasedWikiUrls.get(_request).page("ToPage"), view.getURL());
    verify(_store);
  }

  public void testUnlockDoesNothingIfNoLockTokenProvided() throws Exception {
    _request.setParameter(DefaultPageImpl.SUBMIT_UNLOCK, "");
    replay(_store);
    RedirectView view = (RedirectView) _page.set(CALLED_ON_PAGE, ConsumedPath.EMPTY, _request, _response);
    assertEquals(_request.getRequestURL().toString(), view.getURL());
    verify(_store);
  }

  public void testUnlockUnlocksIfLockTokenProvided() throws Exception {
    _request.setParameter(SUBMIT_UNLOCK, "");
    _request.setParameter(PARAM_LOCK_TOKEN, "dooby");
    _store.unlock(CALLED_ON_PAGE, "dooby");
    expectLastCall().once();
    replay(_store);
    RedirectView view = (RedirectView) _page.set(CALLED_ON_PAGE, ConsumedPath.EMPTY, _request, _response);
    assertEquals(_request.getRequestURL().toString(), view.getURL());
    verify(_store);
  }
  
}
