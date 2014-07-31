package net.hillsdon.reviki.wiki.renderer;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;

public class WrappedXMLRenderer extends MarkupRenderer<String> {
  /** The wrapped renderer. */
  private final MarkupRenderer<Document> _renderer;

  /**
   * @param wraps The XML renderer to produce a string from.
   */
  public WrappedXMLRenderer(MarkupRenderer<Document> wraps) {
    _renderer = wraps;
  }

  @Override
  public ASTNode render(PageInfo page) throws IOException, PageStoreException {
    return _renderer.render(page);
  }

  @Override
  public String build(ASTNode ast, URLOutputFilter urlOutputFilter) {
    try {
      Document doc = _renderer.build(ast, urlOutputFilter);
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(doc), new StreamResult(writer));
      return writer.getBuffer().toString();
    }
    catch (Exception e) {
      return "error: " + e;
    }
  }

  @Override
  public String getContentType() {
    return "text/xml";
  }
}
