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
package net.hillsdon.reviki.wiki;

import java.io.IOException;
import java.util.Collections;

import com.google.common.base.Optional;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.ast.*;

/**
 * Interface for something that renders wiki markup in some other format.
 *
 * @author mth
 */
public abstract class MarkupRenderer<T> {
  /**
   * This directive controls the vertical alignment of table cells. It takes a
   * single parameter, of ehich acceptable values are "top", "middle", and
   * "bottom". If unspecified or disabled, the default vertical alignment, which
   * may be output format dependent, will be used.
   */
  public static final String TABLE_ALIGNMENT_DIRECTIVE = "table-alignment";

  /**
   * Useful for testing.
   */
  public static final MarkupRenderer<ASTNode> AS_IS = new MarkupRenderer<ASTNode>() {
    public ASTNode parse(final PageInfo page) throws IOException, PageStoreException {
      return new Raw(page.getContent());
    }

    public ASTNode render(final ASTNode ast, final URLOutputFilter urlOutputFilter) {
      return ast;
    }
  };

  /**
   * Render a page to an AST.
   */
  public abstract ASTNode parse(PageInfo page) throws IOException, PageStoreException;

  /**
   * Like {@link #render(PageInfo)}, but takes the page contents.
   */
  public final Optional<T> render(final String contents) {
    PageInfo page = new PageInfoImpl("", "", contents, Collections.<String, String> emptyMap());
    return render(page);
  }

  /**
   * Like {@link #render(PageInfo, URLOutputFilter)}, but with the null URL
   * output filter.
   */
  public final Optional<T> render(final PageInfo page) {
    return render(page, URLOutputFilter.NULL);
  }

  /**
   * Like {@link #render(PageInfo, URLOutputFilter)}, but takes the page
   * contents.
   */
  public final Optional<T> render(final String contents, final URLOutputFilter urlOutputFilter) {
    PageInfo page = new PageInfoImpl("", "", contents, Collections.<String, String> emptyMap());
    return render(page, urlOutputFilter);
  }

  /**
   * Render a page, and then turn it into the desired output type.
   */
  public final Optional<T> render(final PageInfo page, final URLOutputFilter urlOutputFilter) {
    try {
      ASTNode rendered = parse(page);
      return Optional.of(render(rendered, urlOutputFilter));
    }

    catch (IOException e) {
      return Optional.<T> absent();
    }

    catch (PageStoreException e) {
      return Optional.<T> absent();
    }
  }

  /**
   * Like {@link #render(ASTNode, URLOutputFilter)}, but with the null URL
   * output filter.
   */
  public final T render(final ASTNode ast) throws IOException, PageStoreException {
    return render(ast, URLOutputFilter.NULL);
  }

  /**
   * Render a page, and then turn it into the desired output type.
   */
  public abstract T render(final ASTNode ast, final URLOutputFilter urlOutputFilter) throws IOException, PageStoreException;

  /**
   * Return the MIME type of the generated output.
   */
  public String getContentType() {
    return "application/octet-stream";
  }
}
