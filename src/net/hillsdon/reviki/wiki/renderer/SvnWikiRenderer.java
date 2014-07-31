/**
 * Copyright 2008 Matthew Hillsdon
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
package net.hillsdon.reviki.wiki.renderer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.common.ViewTypeConstants;
import net.hillsdon.reviki.web.urls.Configuration;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import com.google.common.base.Supplier;

public class SvnWikiRenderer extends MarkupRenderer<String> {
  private final RendererRegistry _registry;

  public SvnWikiRenderer(final Configuration configuration, final PageStore pageStore, final InternalLinker internalLinker, final Supplier<List<Macro>> macros) {
    final LinkPartsHandler linkHandler = new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.ANCHOR, pageStore, internalLinker, configuration);
    final LinkPartsHandler imageHandler = new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.IMAGE, pageStore, internalLinker, configuration);

    _registry = new RendererRegistry(new HtmlRenderer(pageStore, linkHandler, imageHandler, macros));

    MarkupRenderer<InputStream> docbook = new MarkupRenderer<InputStream>() {
      private final DocbookRenderer _renderer = new DocbookRenderer(pageStore, linkHandler, imageHandler, macros);

      @Override
      public ASTNode render(PageInfo page) throws IOException, PageStoreException {
        return _renderer.render(page);
      }

      @Override
      public InputStream build(ASTNode ast, URLOutputFilter urlOutputFilter) {
        try {
          Document doc = _renderer.build(ast, urlOutputFilter);
          TransformerFactory tf = TransformerFactory.newInstance();
          Transformer transformer = tf.newTransformer();
          transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
          transformer.setOutputProperty(OutputKeys.INDENT, "yes");
          StringWriter writer = new StringWriter();
          transformer.transform(new DOMSource(doc), new StreamResult(writer));
          String xml = writer.getBuffer().toString();
          return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e) {
          String error = "error: " + e;
          return new ByteArrayInputStream(error.getBytes(StandardCharsets.UTF_8));
        }
      }

      @Override
      public String getContentType() {
        return "text/xml";
      }
    };

    _registry.addStreamOutputRenderer(ViewTypeConstants.CTYPE_DOCBOOK, docbook);

    MarkupRenderer<InputStream> raw = new MarkupRenderer<InputStream>() {
      private PageInfo _page;

      @Override
      public ASTNode render(final PageInfo page) {
        _page = page;
        return CreoleRenderer.render(pageStore, page, linkHandler, imageHandler, macros);
      }

      @Override
      public InputStream build(ASTNode ast, URLOutputFilter urlOutputFilter) {
        return new ByteArrayInputStream(_page.getContent().getBytes(StandardCharsets.UTF_8));
      }

      @Override
      public String getContentType() {
        // This is a cludge. We should represent 'special' pages better.
        if (_page.getPath().equals("ConfigCss")) {
          return "text/css";
        }
        else {
          return "text/plain";
        }
      }
    };

    _registry.addStreamOutputRenderer(ViewTypeConstants.CTYPE_RAW, raw);
  }

  /**
   * Return a source of renderers.
   */
  public RendererRegistry getRenderers() {
    return _registry;
  }

  @Override
  public ASTNode render(PageInfo page) throws IOException, PageStoreException {
    MarkupRenderer<String> renderer = _registry.getPageOutputRenderer(ViewTypeConstants.CTYPE_DEFAULT);
    return renderer.render(page);
  }

  @Override
  public String build(ASTNode ast, URLOutputFilter urlOutputFilter) {
    MarkupRenderer<String> renderer = _registry.getPageOutputRenderer(ViewTypeConstants.CTYPE_DEFAULT);
    return renderer.build(ast, urlOutputFilter);
  }
}
