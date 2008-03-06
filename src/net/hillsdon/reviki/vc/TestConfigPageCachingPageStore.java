/**
 * Copyright 2007 Matthew Hillsdon
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
package net.hillsdon.reviki.vc;

import static net.hillsdon.reviki.vc.ConfigPageCachingPageStore.isConfigPage;
import junit.framework.TestCase;

public class TestConfigPageCachingPageStore extends TestCase {

  public void testIsConfigPage() {
    assertFalse(isConfigPage("Config"));
    assertTrue(isConfigPage("ConfigFoo"));
    assertFalse(isConfigPage("ConfiguringStuff"));
  }
  
  public void testDoesntCacheOldRevisionsOfConfigPages() throws Exception {
    PageReference ref = new PageReference("ConfigFoo");
    ConfigPageCachingPageStore store = new ConfigPageCachingPageStore(new SimplePageStore());
    store.getUnderlying().set(ref, "", 1, "Hey there", "Initial commit");
    assertFalse(store.get(ref, 2).isNew());
    assertFalse(store.isCached(ref));
  }
  
}
