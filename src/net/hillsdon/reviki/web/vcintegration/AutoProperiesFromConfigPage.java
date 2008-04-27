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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.hillsdon.reviki.vc.AutoProperties;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.CachingPageStore;
import net.hillsdon.reviki.vc.impl.SimplePageStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.CONFIG_AUTO_PROPERTIES;

/**
 * Currently we re-parse all the while (but use the caching page store).
 * 
 * @author mth
 */
public class AutoProperiesFromConfigPage implements AutoProperties {

  private static final Log LOG = LogFactory.getLog(AutoProperiesFromConfigPage.class);
  
  // Currently initialized by setter injection, safe default here.
  private CachingPageStore _store = new SimplePageStore();

  public Map<String, String> read() {
    try {
      PageInfo text = _store.get(CONFIG_AUTO_PROPERTIES, -1);
      return parseAutoProperties(text.getContent());
    }
    catch (PageStoreException e) {
      LOG.warn("Failed to retrieve the auto-properties page.", e);
      return Collections.emptyMap();
    }
  }

  private Map<String, String> parseAutoProperties(final String content) {
    final Map<String, String> properties = new LinkedHashMap<String, String>();
    for (String line : content.split("\n")) {
      line = line.trim();
      if (line.startsWith("#")) {
        continue;
      }
      int equals = line.indexOf('=');
      if (equals != -1) {
        String before = line.substring(0, equals).trim();
        String after = line.substring(equals + 1).trim();
        properties.put(before, after);
      }
    }
    return properties;
  }

  public void setPageStore(CachingPageStore pageStore) {
    _store = pageStore;
  }

}
