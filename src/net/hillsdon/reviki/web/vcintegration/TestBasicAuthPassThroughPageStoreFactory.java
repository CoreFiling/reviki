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

import static net.hillsdon.reviki.web.vcintegration.BasicAuthPassThroughBasicSVNOperationsFactory.getBasicAuthCredentials;
import junit.framework.TestCase;
import net.hillsdon.reviki.web.vcintegration.BasicAuthPassThroughBasicSVNOperationsFactory.UsernamePassword;

public class TestBasicAuthPassThroughPageStoreFactory extends TestCase {

  public void testNoHeaderMeansNullUsernameAndPassword() {
    assertEquals(new UsernamePassword(null, null), getBasicAuthCredentials(null));
  }

  public void testNonBasicMeansNullUsernameAndPassword() {
    assertEquals(new UsernamePassword(null, null), getBasicAuthCredentials("Digest QWxhZGRpbjpvcGVuIHNlc2FtZQ=="));
  }
  
  public void testExampleFromRFC() {
    assertEquals(new UsernamePassword("Aladdin", "open sesame"), getBasicAuthCredentials("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ=="));
  }

  
}
