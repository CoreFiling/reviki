package net.hillsdon.reviki.vc.impl;

import net.hillsdon.reviki.vc.PageReference;

/**
 * A reference to a page at a specific revision.
 *
 * @author pjt
 */
public class PageRevisionReference {

  private final PageReference _reference;
  private final long _revision;

  public PageRevisionReference(final PageReference reference, final long revision) {
    _reference = reference;
    _revision = revision;
  }

  public PageReference getPage() {
    return _reference;
  }

  public long getRevision() {
    return _revision;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_reference == null) ? 0 : _reference.hashCode());
    result = prime * result + (int) (_revision ^ (_revision >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PageRevisionReference other = (PageRevisionReference) obj;
    if (_reference == null) {
      if (other._reference != null) {
        return false;
      }
    }
    else if (!_reference.equals(other._reference)) {
      return false;
    }
    if (_revision != other._revision) {
      return false;
    }
    return true;
  }

}
