package net.hillsdon.reviki.configuration;

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

}
