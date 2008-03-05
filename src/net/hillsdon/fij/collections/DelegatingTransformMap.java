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
package net.hillsdon.fij.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.hillsdon.fij.core.Transform;

public class DelegatingTransformMap<K, V> implements TransformMap<K, V> {

  private final Map<K, V> _map;
  private final Transform<? super V, K> _transform;
  private Collection<V> _values = new Collection<V>() {

    public boolean add(final V e) {
      return put(e) != null;
    }

    public boolean addAll(final Collection<? extends V> c) {
      boolean added = false;
      for (V e : c) {
        added |= add(e);
      }
      return added;
    }

    public void clear() {
      _map.clear();
    }

    public boolean contains(final Object o) {
      return _map.values().contains(o);
    }

    public boolean containsAll(final Collection<?> c) {
      return _map.values().containsAll(c);
    }

    public boolean isEmpty() {
      return _map.values().isEmpty();
    }

    public Iterator<V> iterator() {
      return _map.values().iterator();
    }

    public boolean remove(final Object o) {
      return _map.values().remove(o);
    }

    public boolean removeAll(final Collection<?> c) {
      return _map.values().removeAll(c);
    }

    public boolean retainAll(final Collection<?> c) {
      return _map.values().retainAll(c);
    }

    public int size() {
      return _map.values().size();
    }

    public Object[] toArray() {
      return _map.values().toArray();
    }

    public <T> T[] toArray(final T[] a) {
      return _map.values().toArray(a);
    }

    @Override
    public boolean equals(final Object obj) {
      return _map.values().equals(obj);
    }

    @Override
    public int hashCode() {
      return _map.values().hashCode();
    }

  };

  public DelegatingTransformMap(final Transform<? super V, K> transform) {
    this(transform, new LinkedHashMap<K, V>());
  }

  public DelegatingTransformMap(final Transform<? super V, K> transform, final Map<K, V> map) {
    if (!map.isEmpty()) {
      throw new IllegalArgumentException("Cannot create with non-empty map!");
    }
    _transform = transform;
    _map = map;
  }

  public Collection<V> values() {
    return _values;
  }

  public boolean add(final V v) {
    return values().add(v);
  }

  public void clear() {
    _map.clear();
  }

  public boolean containsKey(final Object key) {
    return _map.containsKey(key);
  }

  public boolean containsValue(final Object value) {
    return _map.containsValue(value);
  }

  public Set<Entry<K, V>> entrySet() {
    return _map.entrySet();
  }

  public V get(final Object key) {
    return _map.get(key);
  }

  public boolean isEmpty() {
    return _map.isEmpty();
  }

  public Set<K> keySet() {
    return _map.keySet();
  }

  public V put(final K key, final V value) {
    return _map.put(key, value);
  }

  public void putAll(final Map<? extends K, ? extends V> m) {
    _map.putAll(m);
  }

  public V remove(final Object key) {
    return _map.remove(key);
  }

  public int size() {
    return _map.size();
  }

  public boolean equals(final Object o) {
    return _map.equals(o);
  }

  public int hashCode() {
    return _map.hashCode();
  }

  public V put(V e) {
    return _map.put(_transform.transform(e), e);
  }

}
