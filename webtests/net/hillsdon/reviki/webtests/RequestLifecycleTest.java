package net.hillsdon.reviki.webtests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;

import net.hillsdon.reviki.web.common.RequestAttributes;
import net.hillsdon.reviki.web.common.ViewTypeConstants;
import net.hillsdon.reviki.web.vcintegration.RequestLifecycleAware;
import net.hillsdon.reviki.wiki.renderer.creole.RenderingTest;

/** A test that can start a dummy HttpServletRequest. */
public abstract class RequestLifecycleTest extends RenderingTest {
  protected HttpServletRequest request;

  public RequestLifecycleTest() {
    super();
  }

  protected void startRequest(final RequestLifecycleAware lifecycle, final String cType, final boolean authProvided) {
    final Map<String, String> params = new LinkedHashMap<String, String>();
    startRequest(lifecycle, cType, authProvided, params);
  }

  protected void startRequest(final RequestLifecycleAware lifecycle, final String cType, final boolean authProvided, final Map<String, String> params) {
    params.put(ViewTypeConstants.PARAM_CTYPE, cType);

    final Map<String, String> headers = new LinkedHashMap<String, String>();
    if (authProvided) {
      headers.put("Authorization", "basic " + Base64.encodeBase64String((System.getProperty("wiki.username") + ":" + System.getProperty("wiki.password")).getBytes()));
    }

    request = new HttpServletRequest() {
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
      public Object getAttribute(final String attr) {
        if (RequestAttributes.LINK_RESOLUTION_CONTEXT.equals(attr)) {
          return resolver;
        }
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
    };
    lifecycle.create(request);
  }

}
