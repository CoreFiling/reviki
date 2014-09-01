package net.hillsdon.reviki.wiki.renderer;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;

/**
 * A renderer to XSL-FO, and various formats which can be produced from it. This
 * uses the Docbook renderer as the source, and uses Apache Fop to perform the
 * transformations. As Fop depends on different versions of things to Reviki, it
 * can't easily be linked into the Jar. Thus, it it executed at runtime, as a
 * standalone program, to do what we want.
 *
 * @author msw
 */
public class XSLFORenderer extends MarkupRenderer<InputStream> {
  /** Posible output types. */
  public static enum FoOutput {
    XSLFO("-foout", "text/xml; charset=utf-8"), RTF("-rtf", "text/rtf; charset=utf-8"), PDF("-pdf", "application/pdf"), PS("-ps", "application/postscript");

    public final String arg;

    public final String mime;

    FoOutput(final String arg, final String mime) {
      this.arg = arg;
      this.mime = mime;
    }
  };

  /** The underlying docbook renderer. */
  private final DocbookRenderer _docbook;

  /** The selected output format. */
  private final FoOutput _format;

  /** The path to the Java executable. */
  private static final String JAVA_PATH = System.getProperty("java.home") + "/bin/java";

  /** The directory where FOP lives. */
  private static final File FOP_DIR;

  /** Relative path to the fop jar. */
  private static final String FOP_JAR = "fop.jar";

  /** Path to dependencies of fop. */
  private static final String FOP_CLASSPATH;

  /** Path to the docbook xsl file. */
  private static final String XSL_PATH;

  static {
    Properties props = new Properties();

    try {
      String clazz = XSLFORenderer.class.getResource("XSLFORenderer.class").toString();
      String workingdir = clazz.split("file:")[1].split("WEB-INF")[0];
      props.load(new FileInputStream(workingdir + "WEB-INF/fop.properties"));
    }
    catch (IOException e) {
      // Try the system properties
      props = System.getProperties();
    }

    FOP_DIR = new File(props.getProperty("fop.dir", "/usr/local/bin"));
    FOP_CLASSPATH = props.getProperty("fop.classpath", ".");
    XSL_PATH = props.getProperty("docbook.styledir", "/usr/share/sgml/docbook/xsl-stylesheets-1.78.1/") + "fo/docbook.xsl";
  }

  public XSLFORenderer(final DocbookRenderer docbook) {
    this(docbook, FoOutput.XSLFO);
  }

  public XSLFORenderer(final DocbookRenderer docbook, final FoOutput format) {
    _docbook = docbook;
    _format = format;
  }

  @Override
  public ASTNode parse(final PageInfo page) throws IOException, PageStoreException {
    return _docbook.parse(page);
  }

  @Override
  public InputStream render(final ASTNode ast, final URLOutputFilter urlOutputFilter) {
    String docbook = _docbook.buildString(ast, urlOutputFilter);

    try {
      ProcessBuilder builder = new ProcessBuilder(
          JAVA_PATH, "-cp", FOP_CLASSPATH, "-jar", FOP_JAR,
          "-xml", "-",       // read xml from stdin.
          "-xsl", XSL_PATH,  // use this xsl file for transformation.
          _format.arg, "-"); // dump the result, in the requested format, to
                             // stdout.

      builder.directory(FOP_DIR);
      Process process = builder.start();

      InputStream stdout = process.getInputStream();
      OutputStream stdin = process.getOutputStream();

      // Write XML to stdin
      BufferedWriter writer;
      try {
        writer = new BufferedWriter(new OutputStreamWriter(stdin, "UTF-8"));
      }
      catch (UnsupportedEncodingException e) {
        writer = new BufferedWriter(new OutputStreamWriter(stdin));
      }
      writer.write(docbook);
      writer.flush();
      writer.close();

      // Read response from stdout
      return stdout;
    }
    catch (Exception e) {
      try {
        return new ByteArrayInputStream(("error " + e).getBytes("UTF-8"));
      }
      catch (UnsupportedEncodingException e2) {
        return new ByteArrayInputStream(("error " + e).getBytes());
      }
    }
  }

  @Override
  public String getContentType() {
    return _format.mime;
  }
}
