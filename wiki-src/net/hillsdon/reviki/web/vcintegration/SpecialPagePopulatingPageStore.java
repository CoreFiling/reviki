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

import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.CONFIG_AUTO_PROPERTIES;
import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.CONFIG_INTER_WIKI_LINKS;
import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.CONFIG_PLUGINS;
import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.PAGE_FOOTER;
import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.PAGE_FRONT_PAGE;
import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.PAGE_HEADER;
import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.PAGE_SIDEBAR;
import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.CONFIG_ICONS;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Optional;

import net.hillsdon.reviki.vc.VersionedPageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.VersionedPageInfoImpl;
import net.hillsdon.reviki.vc.impl.AutoPropertiesApplier;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.vc.impl.SimpleDelegatingPageStore;

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
  public static final Collection<PageReference> SPECIAL_PAGES_WITH_PER_FORMAT_CONTENT = new LinkedHashSet<PageReference>(Arrays.asList(
      PAGE_FRONT_PAGE,
      PAGE_SIDEBAR
   ));
  public static final Collection<PageReference> SPECIAL_PAGES_WITH_CONTENT = new LinkedHashSet<PageReference>(Arrays.asList(
      new PageReferenceImpl("FindPage"),
      new PageReferenceImpl("ConfigCss"),
      CONFIG_PLUGINS,
      CONFIG_AUTO_PROPERTIES,
      CONFIG_INTER_WIKI_LINKS,
      CONFIG_ICONS
   ));
  public static final Collection<PageReference> SPECIAL_PAGES_WITHOUT_CONTENT = Arrays.asList(
      new PageReferenceImpl("AllPages"),
      new PageReferenceImpl("ConfigSvnLocation"),
      new PageReferenceImpl("OrphanedPages"),
      new PageReferenceImpl("RecentChanges"),
      PAGE_HEADER,
      PAGE_FOOTER
    );

  private final AutoPropertiesApplier _autoProps;

  public SpecialPagePopulatingPageStore(final PageStore delegate, final AutoPropertiesApplier autoProps) {
    super(delegate);
    _autoProps = autoProps;
  }

  @Override
  public Set<PageReference> list() throws PageStoreException {
    Set<PageReference> list = super.list();
    list.addAll(SPECIAL_PAGES_WITH_PER_FORMAT_CONTENT);
    list.addAll(SPECIAL_PAGES_WITH_CONTENT);
    list.addAll(SPECIAL_PAGES_WITHOUT_CONTENT);
    return list;
  }

  private Optional<String> getPropulatedText(final PageReference ref, final VersionedPageInfo page) throws IOException {
    if (SPECIAL_PAGES_WITH_PER_FORMAT_CONTENT.contains(ref)) {
      return Optional.of(IOUtils.toString(getClass().getResourceAsStream("prepopulated/" + page.getPath() + "." + page.getSyntax(_autoProps).value()), "UTF-8"));
    }
    if (SPECIAL_PAGES_WITH_CONTENT.contains(ref)) {
      return Optional.of(IOUtils.toString(getClass().getResourceAsStream("prepopulated/" + page.getPath()), "UTF-8"));
    }
    return Optional.absent();
  }

  private VersionedPageInfo withContentIfSpecialAndNew(final PageReference ref, VersionedPageInfo page) throws PageStoreException {
    try {
      if (page.isNewPage()) {
        Optional<String> text = getPropulatedText(ref, page);
        if (text.isPresent()) {
          page = new VersionedPageInfoImpl(getWiki(), page.getPath(), text.get(), VersionedPageInfo.UNCOMMITTED, VersionedPageInfo.UNCOMMITTED, page.getLastChangedUser(), page.getLastChangedDate(), page.getLockedBy(), page.getLockToken(), page.getLockedSince());
        }
      }
    }
    catch (IOException ex) {
      LOG.warn("Error retrieving prepopulated page.", ex);
    }
    return page;
  }

  @Override
  public VersionedPageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    VersionedPageInfo page = super.get(ref, revision);
    page = withContentIfSpecialAndNew(ref, page);
    return page;
  }

  @Override
  public VersionedPageInfo tryToLock(final PageReference ref) throws PageStoreException {
    VersionedPageInfo page = super.tryToLock(ref);
    page = withContentIfSpecialAndNew(ref, page);
    return page;
  }
}
