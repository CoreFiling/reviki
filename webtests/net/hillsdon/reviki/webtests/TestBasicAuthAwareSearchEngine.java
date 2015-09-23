package net.hillsdon.reviki.webtests;

import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import com.google.common.collect.ImmutableSet;

import junit.framework.TestCase;
import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.search.SearchMatch;
import net.hillsdon.reviki.search.impl.BasicAuthAwareSearchEngine;
import net.hillsdon.reviki.search.impl.LuceneSearcher;
import net.hillsdon.reviki.vc.PageStoreAuthenticationException;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.web.common.ViewTypeConstants;
import net.hillsdon.reviki.web.vcintegration.RequestLifecycleAware;
import net.hillsdon.reviki.wiki.MarkupRenderer;

/**
 * This isn't really a web test, but it is a functional test as it depends on a real SVN server.  Thus lives in webTests.
 * 
 * Some of the support code duplicates that in TestLuceneSearcher. */
public class TestBasicAuthAwareSearchEngine extends TestCase {
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

  /** Without auth text results should filter out wiki2 */
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

  private void startRequest(final RequestLifecycleAware lifecycle, final String cType, final boolean authProvided) {
    final Map<String, String> params = new LinkedHashMap<String, String>();
    params.put(ViewTypeConstants.PARAM_CTYPE, cType);

    final Map<String, String> headers = new LinkedHashMap<String, String>();
    if (authProvided) {
      headers.put("Authorization", "basic " + Base64.encodeBase64String((System.getProperty("wiki.username") + ":" + System.getProperty("wiki.password")).getBytes()));
    }

    lifecycle.create(new HttpServletRequest() {
      @Override
      public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
      }

      @Override
      public void setAttribute(String arg0, Object arg1) {
      }

      @Override
      public void removeAttribute(String arg0) {
      }

      @Override
      public boolean isSecure() {
        return false;
      }

      @Override
      public int getServerPort() {
        return 0;
      }

      @Override
      public String getServerName() {
        return null;
      }

      @Override
      public String getScheme() {
        return null;
      }

      @Override
      public RequestDispatcher getRequestDispatcher(String arg0) {
        return null;
      }

      @Override
      public int getRemotePort() {
        return 0;
      }

      @Override
      public String getRemoteHost() {
        return null;
      }

      @Override
      public String getRemoteAddr() {
        return null;
      }

      @Override
      public String getRealPath(String arg0) {
        return null;
      }

      @Override
      public BufferedReader getReader() throws IOException {
        return null;
      }

      @Override
      public String getProtocol() {
        return null;
      }

      @Override
      public String[] getParameterValues(String arg0) {
        return null;
      }

      @Override
      public Enumeration getParameterNames() {
        return null;
      }

      @Override
      public Map getParameterMap() {
        return params;
      }

      @Override
      public String getParameter(final String param) {
        return params.get(param);
      }

      @Override
      public Enumeration getLocales() {
        return null;
      }

      @Override
      public Locale getLocale() {
        return null;
      }

      @Override
      public int getLocalPort() {
        return 0;
      }

      @Override
      public String getLocalName() {
        return null;
      }

      @Override
      public String getLocalAddr() {
        return null;
      }

      @Override
      public ServletInputStream getInputStream() throws IOException {
        return null;
      }

      @Override
      public String getContentType() {
        return null;
      }

      @Override
      public int getContentLength() {
        return 0;
      }

      @Override
      public String getCharacterEncoding() {
        return null;
      }

      @Override
      public Enumeration getAttributeNames() {
        return null;
      }

      @Override
      public Object getAttribute(String arg0) {
        return null;
      }

      @Override
      public boolean isUserInRole(String arg0) {
        return false;
      }

      @Override
      public boolean isRequestedSessionIdValid() {
        return false;
      }

      @Override
      public boolean isRequestedSessionIdFromUrl() {
        return false;
      }

      @Override
      public boolean isRequestedSessionIdFromURL() {
        return false;
      }

      @Override
      public boolean isRequestedSessionIdFromCookie() {
        return false;
      }

      @Override
      public Principal getUserPrincipal() {
        return null;
      }

      @Override
      public HttpSession getSession(boolean arg0) {
        return null;
      }

      @Override
      public HttpSession getSession() {
        return null;
      }

      @Override
      public String getServletPath() {
        return null;
      }

      @Override
      public String getRequestedSessionId() {
        return null;
      }

      @Override
      public StringBuffer getRequestURL() {
        return null;
      }

      @Override
      public String getRequestURI() {
        return null;
      }

      @Override
      public String getRemoteUser() {
        return null;
      }

      @Override
      public String getQueryString() {
        return null;
      }

      @Override
      public String getPathTranslated() {
        return null;
      }

      @Override
      public String getPathInfo() {
        return null;
      }

      @Override
      public String getMethod() {
        return null;
      }

      @Override
      public int getIntHeader(String arg0) {
        return 0;
      }

      @Override
      public Enumeration getHeaders(String arg0) {
        return null;
      }

      @Override
      public Enumeration getHeaderNames() {
        return null;
      }

      @Override
      public String getHeader(String header) {
        return headers.get(header);
      }

      @Override
      public long getDateHeader(String arg0) {
        return 0;
      }

      @Override
      public Cookie[] getCookies() {
        return null;
      }

      @Override
      public String getContextPath() {
        return null;
      }

      @Override
      public String getAuthType() {
        return null;
      }
    });
  }
}
