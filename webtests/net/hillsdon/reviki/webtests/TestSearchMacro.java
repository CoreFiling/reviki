package net.hillsdon.reviki.webtests;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TestSearchMacro extends WebTestSupport {

  public void testNoResults() throws Exception {
    String searchingFor = uniqueWikiPageName("ThisDoesNotExist");
    String searchingOn = uniqueWikiPageName("SearchMacroTest");
    HtmlPage page = editWikiPage(searchingOn, "Search Macro Results: <<search:path:" + searchingFor + ">>", "", "Search Macro test", true);
    assertTrue(page.asText().contains("Search Macro Results:"));
    assertFalse(page.asText().contains(searchingFor));
    HtmlPage results = search(getWikiPage("FrontPage"), searchingFor);
    assertTrue(results.asText().contains("Create new page " + searchingFor));
  }

  public void testWithoutAttributes() throws Exception {
    String searchingFor = uniqueWikiPageName("SearchMacroSearchingFor");
    editWikiPage(searchingFor, "Should be found by macro", "", "Search Macro Test", true);
    String searchingOn = uniqueWikiPageName("SearchMacroTest");
    HtmlPage searchingOnPage = editWikiPage(searchingOn, "Search Macro Results: <<search:path:" + searchingFor + ">>", "", "Search Macro test", true);
    String searchingOnPageAsText = searchingOnPage.asText();
    assertTrue(searchingOnPageAsText.contains("Search Macro Results:"));
    assertTrue(searchingOnPageAsText.contains(searchingFor));
    assertSearchFindsPageUsingQuery(searchingOnPage, searchingFor, "path:" + searchingFor);
  }

  public void testNonMatchingAttributes() throws Exception {
    String searchingFor = uniqueWikiPageName("SearchMacroSearchingFor");
    editWikiPage(searchingFor, "Should not be found by macro", "status = new", "Search Macro Test", true);
    String searchingOn = uniqueWikiPageName("SearchMacroTest");
    HtmlPage searchingOnPage = editWikiPage(searchingOn, "Search Macro Results: <<search:path:" + searchingFor + " AND @status:completed>>", "", "Search Macro Test", true);
    String searchingOnPageAsText = searchingOnPage.asText();
    assertTrue(searchingOnPageAsText.contains("Search Macro Results:"));
    assertFalse(searchingOnPageAsText.contains(searchingFor));
    assertSearchFindsPageUsingQuery(searchingOnPage, searchingFor, "path:" + searchingFor);
  }

  public void testMatchingAttributes() throws Exception {
    String searchingFor = uniqueWikiPageName("SearchMacroSearchingFor");
    editWikiPage(searchingFor, "Should not be found by macro", "status = completed", "Search Macro Test", true);
    String searchingOn = uniqueWikiPageName("SearchMacroTest");
    HtmlPage searchingOnPage = editWikiPage(searchingOn, "Search Macro Results: <<search:path:" + searchingFor + " AND @status:completed>>", "", "Search Macro Test", true);
    String searchingOnPageAsText = searchingOnPage.asText();
    assertTrue(searchingOnPageAsText.contains("Search Macro Results:"));
    assertTrue(searchingOnPageAsText.contains(searchingFor));
    assertSearchFindsPageUsingQuery(searchingOnPage, searchingFor, "path:" + searchingFor);
  }

  public void testNegativeMatchingAttributes() throws Exception {
    String searchingFor = uniqueWikiPageName("SearchMacroSearchingFor");
    editWikiPage(searchingFor, "Should not be found by macro", "status = completed", "Search Macro Test", true);
    String searchingOn = uniqueWikiPageName("SearchMacroTest");
    HtmlPage searchingOnPage = editWikiPage(searchingOn, "Search Macro Results: <<search:path:" + searchingFor + " AND NOT @status:completed>>", "", "Search Macro Test", true);
    String searchingOnPageAsText = searchingOnPage.asText();
    assertTrue(searchingOnPageAsText.contains("Search Macro Results:"));
    assertFalse(searchingOnPageAsText.contains(searchingFor));
    assertSearchFindsPageUsingQuery(searchingOnPage, searchingFor, "path:" + searchingFor);
  }

  public void testOr() throws Exception {
    String searchingFor = uniqueWikiPageName("SearchMacroSearchingFor");
    editWikiPage(searchingFor, "Should be found by macro", "status = completed", "Search Macro Test", true);
    String searchingOn = uniqueWikiPageName("SearchMacroTest");
    HtmlPage searchingOnPage = editWikiPage(searchingOn, "Search Macro Results: <<search:path:someRandomPath OR @status:completed>>", "", "Search Macro Test", true);
    String searchingOnPageAsText = searchingOnPage.asText();
    assertTrue(searchingOnPageAsText.contains("Search Macro Results:"));
    assertTrue(searchingOnPageAsText.contains(searchingFor));
    assertSearchFindsPageUsingQuery(searchingOnPage, searchingFor, "path:" + searchingFor);
  }

  public void testAttributeValueWithSpace() throws Exception {
    String searchingFor = uniqueWikiPageName("SearchMacroSearchingFor");
    editWikiPage(searchingFor, "Should be found by macro", "status = completed story", "Search Macro Test", true);
    String searchingOn = uniqueWikiPageName("SearchMacroTest");
    HtmlPage searchingOnPage = editWikiPage(searchingOn, "Search Macro Results: <<search:@status:\"completed story\">>", "", "Search Macro Test", true);
    String searchingOnPageAsText = searchingOnPage.asText();
    assertTrue(searchingOnPageAsText.contains("Search Macro Results:"));
    assertTrue(searchingOnPageAsText.contains(searchingFor));
    assertSearchFindsPageUsingQuery(searchingOnPage, searchingFor, "path:" + searchingFor);
  }

  public void testAttributeKeyWithColon() throws Exception {
    String searchingFor = uniqueWikiPageName("SearchMacroSearchingFor");
    editWikiPage(searchingFor, "Should be found by macro", "story:status = completed", "Search Macro Test", true);
    String searchingOn = uniqueWikiPageName("SearchMacroTest");
    HtmlPage searchingOnPage = editWikiPage(searchingOn, "Search Macro Results: <<search:@\"story:status\":\"completed\">>", "", "Search Macro Test", true);
    String searchingOnPageAsText = searchingOnPage.asText();
    assertTrue(searchingOnPageAsText.contains("Search Macro Results:"));
    assertTrue(searchingOnPageAsText.contains(searchingFor));
    assertSearchFindsPageUsingQuery(searchingOnPage, searchingFor, "path:" + searchingFor);
  }
  
  public void testBackLinksOnReferencedPage() throws Exception {
    String refs = uniqueWikiPageName("Refs");
    String findMe = uniqueWikiPageName("FindMe");
    editWikiPage(refs, String.format("Macro: <<search:path:%s>>", findMe), "", "Search Macro Test", true);
    
    editWikiPage(findMe, "I'm here", "", "Search Macro Test", true);
    
    HtmlPage refsPage = getWikiPage(refs);
    String refsPageAsText = refsPage.asText();
    assertTrue(refsPageAsText.contains(findMe));
    
    //editWikiPage(refs, String.format("Macro: <<search:path:%s>>", findMe), "", "prompt", false);
    HtmlPage findMePage = getWikiPage(findMe);
    String findMePageAsText = findMePage.asText();
    assertTrue(findMePageAsText.contains(refs));   
  }
}
