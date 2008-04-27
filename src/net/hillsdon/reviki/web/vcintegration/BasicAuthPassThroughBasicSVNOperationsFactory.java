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
package net.hillsdon.reviki.web.vcintegration;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.reviki.vc.impl.AutoPropertiesApplier;
import net.hillsdon.reviki.vc.impl.BasicSVNOperations;
import net.hillsdon.reviki.vc.impl.BasicSVNOperationsFactory;
import net.hillsdon.reviki.vc.impl.RepositoryBasicSVNOperations;
import net.hillsdon.reviki.web.common.RequestAttributes;

import org.apache.commons.codec.binary.Base64;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

/**
 * Creates a page store that will authenticate with credentials provided in
 * the given request's 'Authorization' header (for basic auth only).
 *   
 * @author mth
 */
public class BasicAuthPassThroughBasicSVNOperationsFactory implements BasicSVNOperationsFactory {

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
        return (_username == null ? other._username == null : _username.equals(other._username)) && (_password == null ? other._password == null : _password.equals(other._password));
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

  private final SVNURL _url;
  private final AutoPropertiesApplier _autoPropertiesApplier;

  public BasicAuthPassThroughBasicSVNOperationsFactory(final SVNURL url, final AutoPropertiesApplier autoPropertiesApplier) {
    _url = url;
    _autoPropertiesApplier = autoPropertiesApplier;
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
            String usernamePassword = new String(Base64.decodeBase64(token.getBytes("ASCII")));
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

  public BasicSVNOperations transform(final HttpServletRequest request) {
    DAVRepositoryFactory.setup();
    SVNRepository repository = createRepository();
    UsernamePassword credentials = getBasicAuthCredentials(request.getHeader("Authorization"));
    repository.setAuthenticationManager(new BasicAuthenticationManager(credentials.getUsername(), credentials.getPassword()));
    request.setAttribute(RequestAttributes.USERNAME, credentials.getUsername());
    return new RepositoryBasicSVNOperations(repository, _autoPropertiesApplier);
  }

  private SVNRepository createRepository() {
    try {
      return SVNRepositoryFactory.create(_url);
    }
    catch (SVNException ex) {
      throw new RuntimeException("Invalid repository.", ex);
    }
  }

}
