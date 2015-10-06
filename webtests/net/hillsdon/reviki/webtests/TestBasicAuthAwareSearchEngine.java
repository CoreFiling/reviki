package net.hillsdon.reviki.webtests;

import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import com.google.common.collect.ImmutableSet;

import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.search.SearchMatch;
import net.hillsdon.reviki.search.impl.BasicAuthAwareSearchEngine;
import net.hillsdon.reviki.search.impl.LuceneSearcher;
import net.hillsdon.reviki.vc.PageStoreAuthenticationException;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.web.common.ViewTypeConstants;
import net.hillsdon.reviki.web.pages.impl.TextFormatSearchResults;
import net.hillsdon.reviki.wiki.MarkupRenderer;

/**
 * This isn't really a web test, but it is a functional test as it depends on a real SVN server.  Thus lives in webTests.
 *
 * Some of the support code duplicates that in TestLuceneSearcher. */
public class TestBasicAuthAwareSearchEngine extends RequestLifecycleTest {
  private static final String WIKI_NAME = "wiki";
  private static final String WIKI_NAME2 = "wiki2";
  private static final String PAGE_THE_NAME = "TheName";
  private static final String PAGE_THE_NAME2 = "TheName2";
  private static final Set<SearchMatch> JUST_THE_PAGE = unmodifiableSet(singleton(new SearchMatch(true, WIKI_NAME, PAGE_THE_NAME, null)));
  private File _dir;
  private File _dir2;
  private LuceneSearcher _searcher;
  private LuceneSearcher _searcher2;
  private DeploymentConfiguration _config;

  private static File createTempDir() throws IOException {
    File file = File.createTempFile("testDir", "");
    assertTrue(file.delete());
    assertTrue(file.mkdir());
    return file;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    _dir = createTempDir();
    _dir2 = createTempDir();
    final WikiConfiguration publicWikiConfiguration = new WikiConfiguration() {
      public void setUrl(String url) throws IllegalArgumentException {
      }

      @Override
      public void setSVNUser(String user) {
      }

      @Override
      public void setSVNPassword(String pass) {
      }

      @Override
      public void save() {
      }

      @Override
      public boolean isEditable() {
        return false;
      }

      @Override
      public boolean isComplete() {
        return false;
      }

      @Override
      public String getWikiName() {
        return WIKI_NAME;
      }

      @Override
      public SVNURL getUrl() {
        try {
          return SVNURL.parseURIEncoded(System.getProperty("wiki.svn"));
        }
        catch (SVNException ex) {
          throw new RuntimeException(ex);
        }
      }

      @Override
      public File getSearchIndexDirectory() {
        return _dir;
      }

      @Override
      public String getSVNUser() {
        return null;
      }
 
      @Override
      public String getSVNPassword() {
        return null;
      }

      @Override
      public List<File> getOtherSearchIndexDirectories() {
        return Arrays.asList(_dir2);
      }
 
      @Override
      public String getFixedBaseUrl(String wikiName) {
        return "http://public.example.com/";
      }

      @Override
      public String getFixedBaseUrl() {
        return "http://public.example.com/";
      }
    };
    _searcher = new LuceneSearcher(publicWikiConfiguration.getWikiName(), publicWikiConfiguration.getSearchIndexDirectory(), publicWikiConfiguration.getOtherSearchIndexDirectories(), MarkupRenderer.AS_IS);

    final WikiConfiguration restrictedWikiConfiguration = new WikiConfiguration() {
      public void setUrl(String url) throws IllegalArgumentException {
      }

      @Override
      public void setSVNUser(String user) {
      }

      @Override
      public void setSVNPassword(String pass) {
      }

      @Override
      public void save() {
      }
 
      @Override
      public boolean isEditable() {
        return false;
      }

      @Override
      public boolean isComplete() {
        return false;
      }

      @Override
      public String getWikiName() {
        return WIKI_NAME2;
      }

      @Override
      public SVNURL getUrl() {
        try {
          return SVNURL.parseURIEncoded(System.getProperty("wiki.svn"));
        }
        catch (SVNException ex) {
          throw new RuntimeException(ex);
        }
      }

      @Override
      public File getSearchIndexDirectory() {
        return _dir2;
      }

      @Override
      public String getSVNUser() {
        return System.getProperty("wiki.username");
      }

      @Override
      public String getSVNPassword() {
        return System.getProperty("wiki.password");
      }

      @Override
      public List<File> getOtherSearchIndexDirectories() {
        return Arrays.asList(_dir);
      }

      @Override
      public String getFixedBaseUrl(String wikiName) {
        return "http://restricted.example.com/";
      }

      @Override
      public String getFixedBaseUrl() {
        return "http://restricted.example.com/";
      }
    };
    _searcher2 = new LuceneSearcher(restrictedWikiConfiguration.getWikiName(), restrictedWikiConfiguration.getSearchIndexDirectory(), restrictedWikiConfiguration.getOtherSearchIndexDirectories(), MarkupRenderer.AS_IS);

    _config = new DeploymentConfiguration() {

      @Override
      public void save() {
      }

      @Override
      public void load() {
      }

      @Override
      public boolean isEditable() {
        return false;
      }

      @Override
      public List<WikiConfiguration> getWikis() {
        return Arrays.asList(new WikiConfiguration[]{restrictedWikiConfiguration, publicWikiConfiguration});
      }

      @Override
      public WikiConfiguration getConfiguration(String wikiName) {
        if (WIKI_NAME.equals(wikiName)) {
          return publicWikiConfiguration;
        }
        else if (WIKI_NAME2.equals(wikiName)) {
          return restrictedWikiConfiguration;
        }
        return null;
      }
    };
    _searcher.index(new PageInfoImpl(WIKI_NAME, PAGE_THE_NAME, "some content", Collections.<String, String>emptyMap()), true);
    _searcher2.index(new PageInfoImpl(WIKI_NAME2, PAGE_THE_NAME2, "some other content", Collections.<String, String>emptyMap()), true);
  }

  /**
   * Without auth text results should filter out wiki2.
   * This behaviour is important, scripts may rely on these results (e.g. https://svn-dev.int.corefiling.com/svn/usr/js/scripts/weekly-report.py ).
   */
  public void testTextResultNoAuth() throws Exception {
    BasicAuthAwareSearchEngine se = new BasicAuthAwareSearchEngine(_searcher, _config);

    startRequest(se.getRequestLifecycleAware(), ViewTypeConstants.CTYPE_TEXT, false);
    try {
      assertEquals(JUST_THE_PAGE, se.search("content", false, false));
    }
    finally {
      se.getRequestLifecycleAware().destroy();
    }
  }

  /**
   * Try getting text format results.
   * Make sure that the parameters on the login URL that's returned do the right things.
   * https://jira.int.corefiling.com/browse/REVIKI-653
   */
  public void testTestResultLoginLinkWorks() throws Exception {
    BasicAuthAwareSearchEngine se = new BasicAuthAwareSearchEngine(_searcher, _config);

    final Map<String, String> params = new LinkedHashMap<String, String>();
    params.put("query", "content");

    StringWriter sw = new StringWriter();

    startRequest(se.getRequestLifecycleAware(), ViewTypeConstants.CTYPE_TEXT, false, params);
    try {
      Set<SearchMatch> results = se.search(params.get("content"), false, false);
      TextFormatSearchResults view = new TextFormatSearchResults(results);
      PrintWriter pw = new PrintWriter(sw);
      view.render(request, pw);
    }
    finally {
      se.getRequestLifecycleAware().destroy();
    }

    String LOG_IN_TEXT = "Log in to see all results";
    String[] logInLine = null;
    for (String l: sw.toString().split("\r*\n")) {
      if (l.contains(LOG_IN_TEXT)) {
        logInLine = l.split(";", 3);
      }
    }
    assertNotNull("Expecting to find a line containing: '" + LOG_IN_TEXT + "'", logInLine);

    // Set up params as provided in the link
    params.clear();
    for (NameValuePair p: URLEncodedUtils.parse(new URI(logInLine[2]).getQuery(), Charset.forName("UTF-8"))) {
      params.put(p.getName(), p.getValue());
    }
    assertEquals("query should be as originally provided", "content", params.get("query"));

    // Trying without auth should fail
    startRequest(se.getRequestLifecycleAware(), ViewTypeConstants.CTYPE_DEFAULT, false, params);
    try {
      Set<SearchMatch> results = se.search(params.get("content"), false, false);
      assertFalse("Authentication required to obtain search results from log in link", true);
    }
    catch (PageStoreAuthenticationException ex) {
      // Good
    }
    finally {
      se.getRequestLifecycleAware().destroy();
    }

    // Trying with auth should work
    startRequest(se.getRequestLifecycleAware(), ViewTypeConstants.CTYPE_DEFAULT, true, params);
    try {
      Set<SearchMatch> results = se.search(params.get("content"), false, false);
    }
    finally {
      se.getRequestLifecycleAware().destroy();
    }
  }

  /** Without auth normal results page should require auth */
  public void testDefaultResultsNoAuth() throws Exception {
    BasicAuthAwareSearchEngine se = new BasicAuthAwareSearchEngine(_searcher, _config);

    startRequest(se.getRequestLifecycleAware(), ViewTypeConstants.CTYPE_DEFAULT, false);
    try {
      se.search("content", false, false);
      assertFalse("Authentication required to obtain search results", true);
    }
    catch (PageStoreAuthenticationException ex) {
      // Good
    }
    finally {
      se.getRequestLifecycleAware().destroy();
    }
  }

  /** Without auth normal results can be returned if restricted to the current wiki (https://jira.int.corefiling.com/browse/REVIKI-654) */
  public void testDefaultResultsNoAuthSingleWiki() throws Exception {
    BasicAuthAwareSearchEngine se = new BasicAuthAwareSearchEngine(_searcher, _config);

    startRequest(se.getRequestLifecycleAware(), ViewTypeConstants.CTYPE_DEFAULT, false);
    try {
      assertEquals(JUST_THE_PAGE, se.search("content", false, true));
    }
    finally {
      se.getRequestLifecycleAware().destroy();
    }
  }

  /** With auth both results should appear in the results for all CTYPEs. */
  public void testResultsWithAuth() throws Exception {
    BasicAuthAwareSearchEngine se = new BasicAuthAwareSearchEngine(_searcher, _config);

    Set<SearchMatch> both = unmodifiableSet(ImmutableSet.of(new SearchMatch(true, WIKI_NAME, PAGE_THE_NAME, null), new SearchMatch(true, WIKI_NAME2, PAGE_THE_NAME2, null)));

    startRequest(se.getRequestLifecycleAware(), ViewTypeConstants.CTYPE_TEXT, true);
    try {
      assertEquals(both, se.search("content", false, false));
    }
    finally {
      se.getRequestLifecycleAware().destroy();
    }

    startRequest(se.getRequestLifecycleAware(), ViewTypeConstants.CTYPE_DEFAULT, true);
    try {
      assertEquals(both, se.search("content", false, false));
    }
    finally {
      se.getRequestLifecycleAware().destroy();
    }
  }
}
