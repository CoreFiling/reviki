package net.hillsdon.svnwiki.vc;

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
