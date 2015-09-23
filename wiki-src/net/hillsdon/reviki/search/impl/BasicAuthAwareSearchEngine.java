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
package net.hillsdon.reviki.search.impl;

import static net.hillsdon.reviki.web.common.ViewTypeConstants.CTYPE_TEXT;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.search.QuerySyntaxException;
import net.hillsdon.reviki.search.SearchEngine;
import net.hillsdon.reviki.search.SearchMatch;
import net.hillsdon.reviki.vc.AutoProperties;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreAuthenticationException;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.AutoPropertiesApplierImpl;
import net.hillsdon.reviki.web.common.ViewTypeConstants;
import net.hillsdon.reviki.web.vcintegration.BasicAuthPassThroughBasicSVNOperationsFactory;
import net.hillsdon.reviki.web.vcintegration.RequestLifecycleAware;
import net.hillsdon.reviki.web.vcintegration.RequestScopedThreadLocalBasicSVNOperations;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Filter search results so that only pages from wikis that your user can view are returned: https://jira.int.corefiling.com/browse/REVIKI-523
 *
 * @author swhite
 */
public class BasicAuthAwareSearchEngine implements SearchEngine {

  private static final Log LOG = LogFactory.getLog(BasicAuthAwareSearchEngine.class);

  private final SearchEngine _delegate;
  private final DeploymentConfiguration _config;
  private final ThreadLocal<HttpServletRequest> _request = new ThreadLocal<HttpServletRequest>();

  public BasicAuthAwareSearchEngine(final SearchEngine delegate, final DeploymentConfiguration config) {
    _delegate = delegate;
    _config = config;
  }

  public RequestLifecycleAware getRequestLifecycleAware() {
    return new RequestLifecycleAware() {
      @Override
      public void destroy() {
        _request.set(null);
      }
      
      @Override
      public void create(HttpServletRequest request) {
        _request.set(request);
      }
    };
  }
  public void index(final PageInfo page, boolean buildingIndex) throws IOException, PageStoreException {
    _delegate.index(page, buildingIndex);
  }

  public boolean isRestrictedWiki(final WikiConfiguration wiki) {
    return !(null == wiki.getSVNUser() || "".equals(wiki.getSVNUser()));
  }

  public Set<SearchMatch> search(final String query, final boolean provideExtracts, boolean singleWiki) throws IOException, QuerySyntaxException, PageStoreException {
    // Assuming that there are any restricted wikis configured then to avoid leaking any information we must either:
    // 1) Silently drop restricted results, or
    // 2) Ask the user to log in whether or not their query results in hits to a restricted wiki.
    // We implement option 1 for CTYPE_TEXT requests and option 2 otherwise.
    if ((_request.get().getHeader("Authorization") == null)
        && !ViewTypeConstants.is(_request.get(), CTYPE_TEXT)) {
      for (WikiConfiguration wiki: _config.getWikis()) {
        if (isRestrictedWiki(wiki)) {
          throw new PageStoreAuthenticationException("Log in to obtain search results");
        }
      }
    }
    if ("force".equals(_request.get().getParameter("login"))) {
      throw new PageStoreAuthenticationException("Log in to obtain search results"); 
    }
    final Map<String, Boolean> wikiAccessOkCache = new LinkedHashMap<String, Boolean>();
	  Set<SearchMatch> results = new LinkedHashSet<SearchMatch>();
    results.addAll(_delegate.search(query, provideExtracts, singleWiki));
    CollectionUtils.filter(results, new Predicate() {
      @Override
      public boolean evaluate(final Object o) {
        final SearchMatch match = (SearchMatch) o;
        Boolean accessOk = wikiAccessOkCache.get(match.getWiki());
        if (accessOk == null) {
          // Determine if the user is allowed access to the matched wiki, based on:
          // * Whether or not a username and password are required to index the wiki, if not then assume everyone is allowed access 
          // * If a username is required then determine if the current user has access.  This is slower, hence the short circuit described in the first point.
          try {
            WikiConfiguration configuration = _config.getConfiguration(match.getWiki());
            if (isRestrictedWiki(configuration)) {
              RequestScopedThreadLocalBasicSVNOperations operations = new RequestScopedThreadLocalBasicSVNOperations(new BasicAuthPassThroughBasicSVNOperationsFactory(configuration.getUrl(), new AutoPropertiesApplierImpl(new AutoProperties() {
                public Map<String, String> read() {
                  return new LinkedHashMap<String, String>();
                }
              })));
              operations.create(_request.get());
              try {
                operations.checkPath(match.getPage(), -1);
              }
              finally {
                operations.destroy();
              }
            }
            accessOk = true;
          }
          catch (PageStoreAuthenticationException ex) {
            accessOk = false;
          }
          catch (PageStoreException ex) { 
            LOG.error("Exception determining access to wiki: " + match.getWiki(), ex);
            return false;
          }
          wikiAccessOkCache.put(match.getWiki(), accessOk);
          LOG.debug("access to results in " + match.getWiki() + ": " + accessOk);
        }
        return accessOk;
      }
    });
    return results;
  }

  public long getHighestSyncedRevision() throws IOException {
    return _delegate.getHighestIndexedRevision();
  }

  public long getHighestIndexedRevision() throws IOException {
    return _delegate.getHighestIndexedRevision();
  }

  public void rememberHighestIndexedRevision(long revision) throws IOException {
    _delegate.rememberHighestIndexedRevision(revision);
  }

  public boolean isIndexBeingBuilt() throws IOException {
    return _delegate.isIndexBeingBuilt();
  }

  public void setIndexBeingBuilt(boolean buildingIndex) throws IOException {
    _delegate.setIndexBeingBuilt(buildingIndex);
  }

  public void delete(final String wiki, final String path, boolean buildingIndex) throws IOException {
    _delegate.delete(wiki, path, buildingIndex);
  }

  public String escape(final String in) {
    return _delegate.escape(in);
  }

  public Set<String> incomingLinks(final String page) throws IOException, PageStoreException {
    return _delegate.incomingLinks(page);
  }

  public Set<String> outgoingLinks(final String page) throws IOException, PageStoreException {
    return _delegate.outgoingLinks(page);
  }
}
