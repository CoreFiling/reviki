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
package net.hillsdon.svnwiki.plugins.xquery;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.wiki.renderer.macro.Macro;
import net.hillsdon.svnwiki.wiki.renderer.macro.ResultFormat;
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

  public ResultFormat getResultFormat() {
    return ResultFormat.XHTML;
  }

}
