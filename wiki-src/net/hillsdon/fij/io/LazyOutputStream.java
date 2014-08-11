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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Delays creating of the real stream.
 * 
 * @author mth
 */
public abstract class LazyOutputStream extends OutputStream {

  private OutputStream _delegate = null;

  protected abstract OutputStream lazyInit() throws IOException;
  
  private void ensureInitialized() throws IOException {
    if (_delegate == null) {
      _delegate = lazyInit();
    }
  }
  
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    ensureInitialized();
    _delegate.write(b, off, len);
  }

  @Override
  public void write(byte[] b) throws IOException {
    ensureInitialized();
    _delegate.write(b);
  }

  @Override
  public void write(int b) throws IOException {
    ensureInitialized();
    _delegate.write(b);
  }

  @Override
  public void close() throws IOException {
    ensureInitialized();
    _delegate.close();
  }

  @Override
  public void flush() throws IOException {
    ensureInitialized();
    _delegate.flush();
  }
  
}
