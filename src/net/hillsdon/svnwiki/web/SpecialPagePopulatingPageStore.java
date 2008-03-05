package net.hillsdon.svnwiki.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.SimpleDelegatingPageStore;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Returns default FrontPage etc if there isn't one in the repository.
 * 
 * @author mth
 */
public class SpecialPagePopulatingPageStore extends SimpleDelegatingPageStore {

  private static final Log LOG = LogFactory.getLog(SpecialPagePopulatingPageStore.class);
  private static final Collection<PageReference> SPECIAL_PAGES_WITH_CONTENT = new LinkedHashSet<PageReference>(Arrays.asList(
      new PageReference("FrontPage"), new PageReference("FindPage")
   )); 
  private static final Collection<PageReference> SPECIAL_PAGES_WITHOUT_CONTENT = Arrays.asList(new PageReference("RecentChanges"), new PageReference("AllPages"));
  
  public SpecialPagePopulatingPageStore(final PageStore delegate) {
    super(delegate);
  }

  @Override
  public Collection<PageReference> list() throws PageStoreException {
    Collection<PageReference> list = super.list();
    list.addAll(SPECIAL_PAGES_WITH_CONTENT);
    list.addAll(SPECIAL_PAGES_WITHOUT_CONTENT);
    return list;
  }
  
  public PageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    PageInfo page = super.get(ref, revision);
    try {
      if (page.isNew() && SPECIAL_PAGES_WITH_CONTENT.contains(ref)) {
        String text = IOUtils.toString(getClass().getResourceAsStream("prepopulated/" + page.getPath()), "UTF-8");
        page = new PageInfo(page.getPath(), text, PageInfo.UNCOMMITTED, PageInfo.UNCOMMITTED, page.getLastChangedUser(), page.getLastChangedDate(), page.getLockedBy(), page.getLockToken());
      }
    }
    catch (IOException ex) {
      LOG.warn("Error retrieving prepopulated page.", ex);
    }
    return page;
  }

}
