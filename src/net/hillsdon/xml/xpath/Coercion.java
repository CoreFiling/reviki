package net.hillsdon.xml.xpath;


import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Type safe wrappers for {@link XPathConstants}.
 * 
 * Custom type conversions are also provided for convenience.
 * 
 * @author mth
 *
 * @param <T> The return type for the constant.
 */
public abstract class Coercion<T> {

  /**
   * Standard.  Corresponding to {@link XPathConstants#NODESET}.
   */
  public static final Coercion<NodeList> NODESET = new Coercion<NodeList>(XPathConstants.NODESET) {
    public NodeList cast(XPathContext context, Object o) {
      return (NodeList) o;
    }
  };
  /**
   * Standard.  Corresponding to {@link XPathConstants#BOOLEAN}.
   */
  public static final Coercion<Boolean> BOOLEAN = new Coercion<Boolean>(XPathConstants.BOOLEAN) {
    public Boolean cast(XPathContext context, Object o) {
      return (Boolean) o;
    }
  };
  /**
   * Standard.  Corresponding to {@link XPathConstants#STRING}.
   */
  public static final Coercion<String> STRING = new Coercion<String>(XPathConstants.STRING) {
    public String cast(XPathContext context, Object o) {
      return (String) o;
    }
  };
  /**
   * Standard.  Corresponding to {@link XPathConstants#NUMBER}.
   */
  public static final Coercion<Double> NUMBER = new Coercion<Double>(XPathConstants.NUMBER) {
    public Double cast(XPathContext context, Object o) {
      return (Double) o;
    }
  };
  /**
   * Standard.  Corresponding to {@link XPathConstants#NODE}.
   */
  public static final Coercion<Node> NODE = new Coercion<Node>(XPathConstants.NODE) {
    public Node cast(XPathContext context, Object o) {
      return (Node) o;
    }
  };

  /**
   * Custom.  Casts to Element. 
   */
  public static final Coercion<Element> ELEMENT = new Coercion<Element>(XPathConstants.NODE) {
    public Element cast(XPathContext context, Object o) throws XPathReturnTypeException {
      if (o instanceof Element) {
        return ((Element) o);
      }
      throw new XPathReturnTypeException("Element expected but was: " + o);
    }
  };

  /**
   * Custom.  More convenient version of {@link #NODESET}. 
   */
  public static final Coercion<List<Node>> NODES = new ListCoercion<Node>(NODE);
  
  /**
   * Custom.  More convenient version of {@link #NODESET} with Element casting. 
   */
  public static final Coercion<List<Element>> ELEMENTS = new ListCoercion<Element>(ELEMENT);

  /**
   * Custom.  Like {@link #NODE} but as the context node of a new {@link XPathAccessImpl}. 
   */
  public static final Coercion<XPathContext> CONTEXT = new Coercion<XPathContext>(XPathConstants.NODE) {
    public XPathContext cast(final XPathContext context, final Object o) throws XPathReturnTypeException {
      return new XPathContextImpl(context, NODE.cast(context, o));
    }
  };

  /**
   * Custom.  Like {@link #NODES} but each as the context node of a new {@link XPathAccessImpl}. 
   */
  public static final Coercion<List<XPathContext>> CONTEXTS = new ListCoercion<XPathContext>(CONTEXT);

  private final QName _constant;

  /**
   * @param constant The {@link XPathConstants} entry corresponding to the type {@link #cast(XPathContext, Object)} assumes.
   */
  public Coercion(final QName constant) {
    _constant = constant;
  }

  public QName constant() {
    return _constant;
  }

  public abstract T cast(XPathContext context, Object o) throws XPathReturnTypeException;

}