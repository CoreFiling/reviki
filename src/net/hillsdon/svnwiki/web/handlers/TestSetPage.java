package net.hillsdon.svnwiki.web.handlers;

import static org.easymock.EasyMock.createMock;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.InvalidInputException;
import net.hillsdon.svnwiki.web.common.MockHttpServletRequest;

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
    _response = null;
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
      _request.setParameter("save", "");
      _request.setParameter("copy", "");
      _page.handlePage(ConsumedPath.EMPTY, _request, _response, CALLED_ON_PAGE);
      fail();
    }
    catch (InvalidInputException expected) {
      assertEquals("Exactly one action must be specified.", expected.getMessage());
    }
  }
  
  public void testSaveRequiresLockToken() throws Exception {
  }
  
  public void testSaveRequiresBaseRevision() throws Exception {
  }
  
  public void testSaveRequiresContent() throws Exception {
  }
  
  public void testCopyRequiresOneOfToPageOrFromPage() throws Exception {
  }
  
  public void testCopyTo() throws Exception {
  }
  
  public void testCopyFrom() throws Exception {
  }
  
  public void testRenameRequiresToPage() throws Exception {
  }
  
  public void testRenameTo() throws Exception {
  }

  public void testUnlockOnlyUnlocksIfLockTokenProvided() throws Exception {
  }
  
}
