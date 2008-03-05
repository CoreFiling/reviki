package net.hillsdon.svnwiki.wiki.xquery;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.wiki.renderer.Macro;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;

public class XQueryMacro implements Macro {

  private static Serializer createSerializer(final OutputStream out) {
    Serializer serializer = new Serializer();
    serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
    serializer.setOutputProperty(Serializer.Property.ENCODING, "UTF-8");
    serializer.setOutputProperty(Serializer.Property.INDENT, "yes");
    serializer.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
    serializer.setOutputStream(out);
    return serializer;
  }

  public String getName() {
    return "xquery";
  }

  public String handle(final PageReference page, final String remainder) {
    Processor processor = new Processor(false);
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      XQueryExecutable compiled = processor.newXQueryCompiler().compile(remainder);
      XQueryEvaluator loaded = compiled.load();
      loaded.setErrorListener(new NullErrorListener());
      loaded.setURIResolver(new NoFileSchemeURIResolver());
      loaded.run(createSerializer(baos));
      return baos.toString("UTF-8");
    }
    catch (SaxonApiException e) {
      return "<p class='error'>" + Escape.html(e.getMessage()) + "</p><pre>" + Escape.html(remainder) + "</pre>";
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Java supports UTF-8!", e);
    }
  }

}
