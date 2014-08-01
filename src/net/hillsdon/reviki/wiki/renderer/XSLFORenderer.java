package net.hillsdon.reviki.wiki.renderer;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class XSLFORenderer extends MarkupRenderer<InputStream> {
  /** Posible output types. */
  public static enum FoOutput {
    XSLFO("-foout", "text/xml; charset=utf-8"),
    RTF("-rtf", "text/rtf; charset=utf-8"),
    PDF("-pdf", "application/pdf"),
    PS("-ps", "application/postscript");

    public final String arg;

    public final String mime;

    FoOutput(String arg, String mime) {
      this.arg = arg;
      this.mime = mime;
    }
  };

  /** The underlying docbook renderer. */
  private final WrappedXMLRenderer _docbook;

  /** The selected output format. */
  private final FoOutput _format;

  /** Path to the fop script. */
  private static final String FOP_PATH = "/home/local/msw/fop-1.1/fop";

  /** Path to the docbook xsl file. */
  private static final String XSL_PATH = "/home/local/msw/Downloads/docbook/fo/docbook.xsl";

  /** The length of the last generated output. */
  private Optional<Integer> _length;

  public XSLFORenderer(PageStore pageStore, LinkPartsHandler linkHandler, LinkPartsHandler imageHandler, Supplier<List<Macro>> macros, FoOutput format) {
    this(new DocbookRenderer(pageStore, linkHandler, imageHandler, macros), format);
  }

  public XSLFORenderer(DocbookRenderer docbook) {
    this(docbook, FoOutput.XSLFO);
  }

  public XSLFORenderer(DocbookRenderer docbook, FoOutput format) {
    _docbook = new WrappedXMLRenderer(docbook);
    _format = format;
    _length = Optional.<Integer> absent();
  }

  @Override
  public ASTNode render(final PageInfo page) throws IOException, PageStoreException {
    return _docbook.render(page);
  }

  @Override
  public InputStream build(ASTNode ast, URLOutputFilter urlOutputFilter) {
    String docbook = _docbook.buildString(ast, urlOutputFilter);

    try {
      ProcessBuilder builder = new ProcessBuilder(FOP_PATH, "-xml", "-", "-xsl", XSL_PATH, _format.arg, "-");
      Process process = builder.start();

      InputStream stdout = process.getInputStream();
      OutputStream stdin = process.getOutputStream();

      // Write XML to stdin
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
      writer.write(docbook);
      writer.flush();
      writer.close();

      // Read response from stdout
      byte[] result = IOUtils.toByteArray(stdout);
      _length = Optional.of(new Integer(result.length));
      System.out.println(_length);
      return new ByteArrayInputStream(result);
    }
    catch (Exception e) {
      return new ByteArrayInputStream(("error " + e).getBytes(StandardCharsets.UTF_8));
    }
  }

  @Override
  public String getContentType() {
    return _format.mime;
  }
}
