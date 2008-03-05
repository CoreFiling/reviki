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
 *   doesn't provide one outside of the non-portabl com.sun packages.
 *   
 * @author mth
 */
public class BasicAuthPassThroughPageStoreFactory implements PageStoreFactory {

  static class UsernamePassword {
    private final String _username;
    private final String _password;
    public UsernamePassword(String username, String password) {
      _username = username;
      _password = password;
    }
    public String getUsername() {
      return _username;
    }
    public String getPassword() {
      return _password;
    }
  }

  private SVNURL _url;
  
  /**
   * @param url Repository URL.
   * @throws PageStoreException If the URL is not valid.
   */
  public BasicAuthPassThroughPageStoreFactory(String url) throws PageStoreException {
    try {
      _url = SVNURL.parseURIDecoded(url);
    }
    catch (SVNException e) {
      throw new PageStoreException(e);
    }
  }

  UsernamePassword getBasicAuthCredentials(final HttpServletRequest request) {
    String username = null;
    String password = null;
    String authorization = request.getHeader("Authorization");
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
      UsernamePassword credentials = getBasicAuthCredentials(request);
      repository.setAuthenticationManager(new BasicAuthenticationManager(credentials.getUsername(), credentials.getPassword()));
      return new SVNPageStore(repository);
    }
    catch (SVNException ex) {
      throw new PageStoreException(ex);
    }
  }
  
}
