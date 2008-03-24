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
package net.hillsdon.fij.io;

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
