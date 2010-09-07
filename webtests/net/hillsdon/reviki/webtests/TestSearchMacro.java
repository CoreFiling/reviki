package net.hillsdon.reviki.webtests;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import net.hillsdon.fij.text.Escape;

/**
 * Test for the Search Macro. For the search form at the top of the pages and the backlinks at the bottom see {@link TestSearch}
 */
public class TestSearchMacro extends WebTestSupport {

  public void testSearchByPath() throws Exception {
    final String targetPageName = uniqueWikiPageName("SearchTarget");
    editWikiPage(targetPageName, "This page should be found by a search macro", "", true);

    final String sourcePageName = uniqueWikiPageName("SearchSource");
    editWikiPage(sourcePageName, "<<search:(search=\"path:" + targetPageName + "\")>>", "", true);

    HtmlPage page = getWikiPage(sourcePageName);
    assertAnchorPresentByHrefContains(page, Escape.urlEncodeUTF8(targetPageName));

    editWikiPage(sourcePageName, "<<search:(search=\"path:SearchTarget*\")>>", "", false);

    page = getWikiPage(sourcePageName);
    assertAnchorPresentByHrefContains(page, Escape.urlEncodeUTF8(targetPageName));

    //TODO put the asterisk in the middle.
  }

  public void testSearchByOutgoingLinks() throws Exception {
    final String referred = uniqueWikiPageName("SearchTargetReferred");
    editWikiPage(referred, "This page should be found by a search macro", "", true);
    final String refers = uniqueWikiPageName("SearchTargetRefers");
    editWikiPage(refers, referred, "", true);

    final String sourcePageName = uniqueWikiPageName("SearchSource");
    editWikiPage(sourcePageName, "<<search:(search=\"outgoing-links:" + referred + "\")>>", "", true);

    HtmlPage page = getWikiPage(sourcePageName);
    assertAnchorPresentByHrefContains(page, Escape.urlEncodeUTF8(refers));


    final String refers2 = uniqueWikiPageName("SearchTargetRefersTwo");
    editWikiPage(refers2, referred, "", true);

    editWikiPage(sourcePageName, "<<search:(search=\"outgoing-links:" + referred + " AND NOT path:" + refers + "\")>>", "", false);

    page = getWikiPage(sourcePageName);
    assertAnchorAbsentByHrefContains(page, Escape.urlEncodeUTF8(refers));
    assertAnchorPresentByHrefContains(page, Escape.urlEncodeUTF8(refers2));
  }

  public void testSearchResultWithSpaceByPath() throws Exception {
    final String targetPageName = uniqueWikiPageName("Search Target");
    editWikiPage(targetPageName, "This page should be found by a search macro", "", true);

    final String sourcePageName = uniqueWikiPageName("SearchSource");
    editWikiPage(sourcePageName, "<<search:(search=\"path:" + targetPageName.replace(" ", "?") + "\")>>", "", true);

    HtmlPage page = getWikiPage(sourcePageName);
    assertAnchorPresentByHrefContains(page, Escape.urlEncodeUTF8(targetPageName));
  }

  public void testSearchSortAndGroup() throws Exception {
    final String referred = uniqueWikiPageName("SearchTargetReferred");
    editWikiPage(referred, "This page should be found by a search macro", "", true);

    final String refers = uniqueWikiPageName("SearchTargetRefersOne");
    editWikiPage(refers, referred + "\n <<keyedValue:(key=\"sortFoo\", value=\"1\")>>\n<<keyedValue:(key=\"groupFoo\", value=\"foo\")>>", "", true);

    final String refers2 = uniqueWikiPageName("SearchTargetRefersTwo");
    editWikiPage(refers2, referred + "\n <<keyedValue:(key=\"sortFoo\", value=\"2\")>>\n<<keyedValue:(key=\"groupFoo\", value=\"bar\")>>", "", true);

    final String refers3 = uniqueWikiPageName("SearchTargetRefersThree");
    editWikiPage(refers3, referred + "\n <<keyedValue:(key=\"sortFoo\", value=\"3\")>>\n<<keyedValue:(key=\"groupFoo\", value=\"foo\")>>", "", true);

    final String refers4 = uniqueWikiPageName("SearchTargetRefersFour");
    editWikiPage(refers4, referred + "\n <<keyedValue:(key=\"sortFoo\", value=\"4\")>>\n<<keyedValue:(key=\"groupFoo\", value=\"bar\")>>", "", true);


    //no sort or group
    final String sourcePageName = uniqueWikiPageName("SearchSource");
    editWikiPage(sourcePageName, "<<search:(search=\"outgoing-links:" + referred + "\")>>", "", true);

    HtmlPage page = getWikiPage(sourcePageName);
    //expect alphabetical order (i.e. Three before Two)
    assertAnchorOrderByHrefContains(page, Escape.urlEncodeUTF8(refers4), Escape.urlEncodeUTF8(refers), Escape.urlEncodeUTF8(refers3), Escape.urlEncodeUTF8(refers2));

    //just sort
    editWikiPage(sourcePageName, "<<search:(search=\"outgoing-links:" + referred + "\", sort=\"sortFoo\")>>", "", false);

    page = getWikiPage(sourcePageName);
    assertAnchorOrderByHrefContains(page, Escape.urlEncodeUTF8(refers), Escape.urlEncodeUTF8(refers2), Escape.urlEncodeUTF8(refers3), Escape.urlEncodeUTF8(refers4));

    //just group
    editWikiPage(sourcePageName, "<<search:(search=\"outgoing-links:" + referred + "\", group=\"groupFoo\")>>", "", false);

    page = getWikiPage(sourcePageName);
    assertAnchorOrderByHrefContains(page, Escape.urlEncodeUTF8(refers4), Escape.urlEncodeUTF8(refers2), Escape.urlEncodeUTF8(refers), Escape.urlEncodeUTF8(refers3));

    //sort and group
    editWikiPage(sourcePageName, "<<search:(search=\"outgoing-links:" + referred + "\", sort=\"sortFoo\", group=\"groupFoo\")>>", "", false);

    page = getWikiPage(sourcePageName);
    assertAnchorOrderByHrefContains(page, Escape.urlEncodeUTF8(refers2), Escape.urlEncodeUTF8(refers4), Escape.urlEncodeUTF8(refers), Escape.urlEncodeUTF8(refers3));

    assertFalse("Couldn't find first heading", page.getByXPath("//h4[. = \"bar\" and //a[contains(@href, \"" + Escape.urlEncodeUTF8(refers2) + "\")] and //a[contains(@href, \"" + Escape.urlEncodeUTF8(refers4) + "\")]]").isEmpty());
    assertFalse("Couldn't find second heading", page.getByXPath("//h4[. = \"foo\" and //a[contains(@href, \"" + Escape.urlEncodeUTF8(refers) + "\")] and //a[contains(@href, \"" + Escape.urlEncodeUTF8(refers3) + "\")]]").isEmpty());
  }

  //TODO ungrouped results
  //TODO search based on keyedValue
}
