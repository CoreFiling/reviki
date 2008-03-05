package net.hillsdon.svnwiki.web.vcintegration;

import static net.hillsdon.svnwiki.web.vcintegration.BasicAuthPassThroughPageStoreFactory.getBasicAuthCredentials;
import junit.framework.TestCase;
import net.hillsdon.svnwiki.web.vcintegration.BasicAuthPassThroughPageStoreFactory.UsernamePassword;

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
