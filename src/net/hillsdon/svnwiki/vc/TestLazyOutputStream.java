package net.hillsdon.svnwiki.vc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Test for {@link LazyOutputStream}.
 * 
 * @author mth
 */
public class TestLazyOutputStream extends TestCase {

  private ByteArrayOutputStream _baos = null;
  private boolean _closed = false;
  private LazyOutputStream _lazyOutputStream;
  
  @Override
  protected void setUp() throws Exception {
    _lazyOutputStream = new LazyOutputStream() {
      @Override
      protected OutputStream lazyInit() throws IOException {
        _baos = new ByteArrayOutputStream() {

          @Override
          public void close() throws IOException {
            _closed = true;
          }
        };
        return _baos;
      }
    };
    
  }
  
  public void testInitsOnWriteAndThenDelegates() throws Exception {
    assertNull(_baos);
    _lazyOutputStream.write(new byte[] {1, 0, 1});
    assertNotNull(_baos);
    _lazyOutputStream.write(new byte[] {0, 1, 0});
    assertTrue(Arrays.equals(new byte[] {1, 0, 1, 0, 1, 0}, _baos.toByteArray()));
    assertFalse(_closed);
    _lazyOutputStream.close();
    assertTrue(_closed);
  }
  
}
