package net.hillsdon.svnwiki.web;

import static net.hillsdon.svnwiki.web.BasicAuthPassThroughPageStoreFactory.getBasicAuthCredentials;
import junit.framework.TestCase;
import net.hillsdon.svnwiki.web.BasicAuthPassThroughPageStoreFactory.UsernamePassword;

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
