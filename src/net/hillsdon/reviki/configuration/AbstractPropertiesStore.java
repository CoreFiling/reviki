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
package net.hillsdon.reviki.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public abstract class AbstractPropertiesStore implements PersistentStringMap {

  private final Properties _delegate = new Properties();

  public AbstractPropertiesStore() {
    super();
  }

  public void clear() {
    getDelegate().clear();
  }

  public boolean containsKey(final Object key) {
    return getDelegate().containsKey(key);
  }

  public boolean containsValue(final Object value) {
    return getDelegate().containsValue(value);
  }

  @SuppressWarnings("unchecked")
  public Set<java.util.Map.Entry<String, String>> entrySet() {
    Set set = getDelegate().entrySet();
    return set;
  }

  public String get(final Object key) {
    return (String) getDelegate().get(key);
  }

  public boolean isEmpty() {
    return getDelegate().isEmpty();
  }

  @SuppressWarnings("unchecked")
  public Set<String> keySet() {
    Set set = getDelegate().keySet();
    return set;
  }

  public String put(final String key, final String value) {
    return (String) getDelegate().put(key, value);
  }

  public void putAll(final Map<? extends String, ? extends String> m) {
    getDelegate().putAll(m);
  }

  public String remove(final Object key) {
    return (String) getDelegate().remove(key);
  }

  public int size() {
    return getDelegate().size();
  }

  @SuppressWarnings("unchecked")
  public Collection<String> values() {
    Collection collection = getDelegate().values();
    return collection;
  }

  protected Properties getDelegate() {
    return _delegate;
  }

  public synchronized void load() throws IOException {
    InputStream in = inputStream();
    if (in == null) {
      return;
    }
    try {
      _delegate.clear();
      _delegate.load(in);
    }
    finally {
      in.close();
    }
  }
  
  public synchronized void save() throws IOException {
    OutputStream out = outputStream();
    if (out == null) {
      return;
    }
    try {
      _delegate.store(out, "reviki configuration file");
    }
    finally {
      out.close();
    }
  }
  
  protected abstract OutputStream outputStream() throws IOException;
  protected abstract InputStream inputStream() throws IOException;

}
