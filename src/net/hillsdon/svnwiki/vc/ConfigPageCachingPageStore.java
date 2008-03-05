package net.hillsdon.svnwiki.vc;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * The configuration pages are often accessed, e.g. ConfigSideBar,
 * ConfigInterWikiLinks.  We cache them, forgoing instant response
 * to external commits.
 * 
 * @author mth
 */
public class ConfigPageCachingPageStore extends SimpleDelegatingPageStore {

  private static final String CONFIG_PREFIX = "Config";
  private Map<PageReference, PageInfo> _cache = new LinkedHashMap<PageReference, PageInfo>();

  public ConfigPageCachingPageStore(final PageStore delegate) {
    super(delegate);
  }

  @Override
  public PageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    if (revision == -1 && _cache.containsKey(ref)) {
      return _cache.get(ref);
    }
    PageInfo pageInfo = super.get(ref, revision);
    if (isConfigPage(ref.getPath())) {
      _cache.put(ref, pageInfo);
    }
    return pageInfo;
  }

  @Override
  public long set(final PageReference ref, final String lockToken, final long baseRevision, final String content, final String commitMessage) throws InterveningCommitException, PageStoreException {
    if (isConfigPage(ref.getPath())) {
      _cache = new LinkedHashMap<PageReference, PageInfo>();
    }
    return super.set(ref, lockToken, baseRevision, content, commitMessage);
  }

  static boolean isConfigPage(final String pageName) {
    return pageName.startsWith(CONFIG_PREFIX) 
           && pageName.length() > CONFIG_PREFIX.length()
           && Character.isUpperCase(pageName.charAt(CONFIG_PREFIX.length()));
  }
  
  public PageStore getUnderlying() {
    return getDelegate();
  }

}
