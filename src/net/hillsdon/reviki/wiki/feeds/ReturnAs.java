package net.hillsdon.reviki.wiki.feeds;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Type safe wrappers for {@link XPathConstants}.
 * 
 * @author mth
 *
 * @param <T> The return type for the constant.
 */
public abstract class ReturnAs<T> {

  public static final ReturnAs<NodeList> NODESET = new ReturnAs<NodeList>(XPathConstants.NODESET) {
    public NodeList cast(Object o) {
      return (NodeList) o;
    }
  };
  public static final ReturnAs<Boolean> BOOLEAN = new ReturnAs<Boolean>(XPathConstants.BOOLEAN) {
    public Boolean cast(Object o) {
      return (Boolean) o;
    }
  };
  public static final ReturnAs<String> STRING = new ReturnAs<String>(XPathConstants.STRING) {
    public String cast(Object o) {
      return (String) o;
    }
  };
  public static final ReturnAs<Double> NUMBER = new ReturnAs<Double>(XPathConstants.NUMBER) {
    public Double cast(Object o) {
      return (Double) o;
    }
  };
  public static final ReturnAs<Node> NODE = new ReturnAs<Node>(XPathConstants.NODE) {
    public Node cast(Object o) {
      return (Node) o;
    }
  };

  private final QName _constant;

  public ReturnAs(QName constant) {
    _constant = constant;
  }

  public QName constant() {
    return _constant;
  }

  public abstract T cast(Object o);

}