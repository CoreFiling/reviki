package net.hillsdon.svnwiki.web;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.PageStoreFactory;
import net.hillsdon.svnwiki.vc.SVNPageStore;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import sun.misc.BASE64Decoder;

/**
 * Creates a page store that will authenticate with credentials provided in
 * the given request's 'Authorization' header (for basic auth only).
 * 
 * FIXME:
 *   We need to get ourselves a base64 codec as the JDK <strong>still</strong>
 *   doesn't provide one outside of the non-portable com.sun packages.
 *   
 * @author mth
 */
public class BasicAuthPassThroughPageStoreFactory implements PageStoreFactory {

  static class UsernamePassword {
    private final String _username;
    private final String _password;
    public UsernamePassword(final String username, final String password) {
      _username = username;
      _password = password;
    }
    public String getUsername() {
      return _username;
    }
    public String getPassword() {
      return _password;
    }
    @Override
    public boolean equals(final Object obj) {
      if (obj instanceof UsernamePassword) {
        UsernamePassword other = (UsernamePassword) obj;
        return (_username == null ? other._username == null : _username.equals(other._username))
           && (_password == null ? other._password == null : _password.equals(other._password));
      }
      return false;
    }
    @Override
    public int hashCode() {
      return (_username == null ? 0 : _username.hashCode()) ^ (_password == null ? 0 : _password.hashCode());
    }
    @Override
    public String toString() {
      return _username + " " + _password;
    }
  }

  private SVNURL _url;
  
  /**
   * @param url Repository URL.
   */
  public BasicAuthPassThroughPageStoreFactory(final SVNURL url) {
    _url = url;
  }

  static UsernamePassword getBasicAuthCredentials(String authorization) {
    String username = null;
    String password = null;
    if (authorization != null) {
      authorization = authorization.trim();
      if (authorization.toLowerCase(Locale.US).startsWith("basic")) {
        int separator = authorization.lastIndexOf(' ');
        if (separator != -1) {
          String token = authorization.substring(separator + 1);
          // RFC2617 doesn't seem to mention encoding...
          try {
            String usernamePassword = new String(new BASE64Decoder().decodeBuffer(token));
            int firstColon = usernamePassword.indexOf(':');
            if (firstColon != -1) {
              username = usernamePassword.substring(0, firstColon);
              password = usernamePassword.substring(firstColon + 1);
            }
          }
          catch (IOException e) {
            // Ignore base64 decode error.
          }
        }
      }
    }
    return new UsernamePassword(username, password);
  }
  
  public PageStore newInstance(final HttpServletRequest request) throws PageStoreException {
    try {
      DAVRepositoryFactory.setup();
      SVNRepository repository = SVNRepositoryFactory.create(_url);
      UsernamePassword credentials = getBasicAuthCredentials(request.getHeader("Authorization"));
      repository.setAuthenticationManager(new BasicAuthenticationManager(credentials.getUsername(), credentials.getPassword()));
      request.setAttribute(RequestAttributes.USERNAME, credentials.getUsername());
      return new FrontPagePopulatingPageStore(new SVNPageStore(repository));
    }
    catch (SVNException ex) {
      throw new PageStoreException(ex);
    }
  }
  
}
