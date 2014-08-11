package net.hillsdon.reviki.wiki.renderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.io.IOUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.*;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.ast.*;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

/**
 * Render a docx file. The output is all styled with Styles (with the exception
 * of runs of bold, italic, or strikethrough text) to enable easy editing of the
 * resultant document.
 *
 * @author msw
 */
public class DocxRenderer extends CreoleBasedRenderer<InputStream> {
  public DocxRenderer(final PageStore pageStore, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros) {
    super(pageStore, linkHandler, imageHandler, macros);
  }

  @Override
  public InputStream render(ASTNode ast, URLOutputFilter urlOutputFilter) {
    DocxVisitor visitor = new DocxVisitor(_pageStore, _page, urlOutputFilter);
    return visitor.visit(ast);
  }

  @Override
  public String getContentType() {
    return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
  }

  /*
   * Generating a docx is very side-effectful - so all the methods return null,
   * except for visitPage which bundles the generated document into a stream.
   */
  protected static final class DocxVisitor extends ASTRenderer<InputStream> {

    /** Styles for headings. We only go up to h6 in the ast. */
    public static final String HEADING1_STYLE = "Heading 1";

    public static final String HEADING2_STYLE = "Heading 2";

    public static final String HEADING3_STYLE = "Heading 3";

    public static final String HEADING4_STYLE = "Heading 4";

    public static final String HEADING5_STYLE = "Heading 5";

    public static final String HEADING6_STYLE = "Heading 6";

    /** The default text style. */
    public static final String TEXT_BODY_STYLE = "Text Body";

    /** Styles for tables. */
    public static final String TABLE_STYLE = "Padded Table";

    public static final String TABLE_HEADER_STYLE = "Table Heading";

    public static final String TABLE_CONTENTS_STYLE = "Table Contents";

    /** Style for block code. */
    public static final String CODE_STYLE = "Preformatted Text";

    /** Font for block and inline code. */
    public static final String CODE_FONT = "Courier New";

    /** Style for paragraphs-turned-into-horizontal rules. */
    public static final String HORIZONTAL_RULE_STYLE = "Horizontal Line";

    /** The number style IDs for ordered and unordered lists. */
    public static final BigInteger ORDERED_LIST_ID = BigInteger.valueOf(1);

    public static final BigInteger UNORDERED_LIST_ID = BigInteger.valueOf(2);

    /** Custom styles. */
    public static final List<Style> CUSTOM_STYLES;

    /** For looking up attachments. */
    protected final PageStore _pageStore;

    protected final PageInfo _page;

    /** Docx files are arranged into a "package" of smaller xml files. */
    protected final WordprocessingMLPackage _package;

    /** The "main" part of the document, which has reference to other parts. */
    protected final MainDocumentPart _mainPart;

    /** This is the actual document being rendered. */
    protected final Document _document;

    /** And this is the body of the document! */
    protected final Body _body;

    /** All objects have to come from a factory. */
    protected final ObjectFactory _factory;

    /** The stack of containing contexts for new inline objects. */
    protected final Stack<ContentAccessor> _contexts = new Stack<ContentAccessor>();

    /** The stack of containing contexts for new block objects. */
    protected final Stack<ContentAccessor> _blockContexts = new Stack<ContentAccessor>();

    /** The stack of numbering styles for nested lists. */
    protected final Stack<PPrBase.NumPr> _numberings = new Stack<PPrBase.NumPr>();

    /*
     * We can't nest bold/italic/strikethrough/etc text - so we need to keep
     * track of which bits are active when we generate "runs" - bits of plain
     * text.
     */
    protected BooleanDefaultTrue _bold;

    protected BooleanDefaultTrue _italic;

    protected BooleanDefaultTrue _strike;

    /** Style to apply to all paragraphs, if set. */
    protected String _paragraphStyle = null;

    /**
     * Set up styles
     */
    static {
      ObjectFactory factory = new ObjectFactory();

      /* ***** Styles. */
      CUSTOM_STYLES = new ArrayList<Style>();

      // Spacing for normal text, values from libreoffice
      PPrBase.Spacing spacing = factory.createPPrBaseSpacing();
      spacing.setAfter(BigInteger.valueOf(140));
      spacing.setLine(BigInteger.valueOf(288));
      spacing.setLineRule(STLineSpacingRule.AUTO);
      spacing.setBefore(BigInteger.ZERO);

      Style textBody = constructStyle(TEXT_BODY_STYLE, "Normal", "paragraph", Optional.<JcEnumeration> absent(), Optional.of(spacing), false);
      Style code = constructStyle(CODE_STYLE, TEXT_BODY_STYLE, "paragraph", Optional.<JcEnumeration> absent(), Optional.of(spacing), false);
      Style tableContents = constructStyle(TABLE_CONTENTS_STYLE, TEXT_BODY_STYLE, "paragraph", Optional.<JcEnumeration> absent(), Optional.<PPrBase.Spacing> absent(), false);
      Style tableHeader = constructStyle(TABLE_HEADER_STYLE, TABLE_CONTENTS_STYLE, "paragraph", Optional.of(JcEnumeration.CENTER), Optional.<PPrBase.Spacing> absent(), true);
      Style horizontalRule = constructStyle(HORIZONTAL_RULE_STYLE, "Normal", "paragraph", Optional.<JcEnumeration> absent(), Optional.<PPrBase.Spacing> absent(), false);

      // Set code font
      code.setRPr(factory.createRPr());
      runFont(code.getRPr(), CODE_FONT);

      // Set horizontal rule bottom border
      CTBorder border = factory.createCTBorder();
      border.setVal(STBorder.SINGLE);
      border.setColor("black");
      border.setSz(BigInteger.valueOf(2));

      horizontalRule.setPPr(factory.createPPr());
      horizontalRule.getPPr().setPBdr(factory.createPPrBasePBdr());
      horizontalRule.getPPr().getPBdr().setBottom(border);

      // The table style is very different to text styles, so it's just
      // constructed here.
      Style tblstyle = factory.createStyle();

      tblstyle.setStyleId(styleNameToId(TABLE_STYLE));

      tblstyle.setName(factory.createStyleName());
      tblstyle.getName().setVal(TABLE_STYLE);

      tblstyle.setBasedOn(factory.createStyleBasedOn());
      tblstyle.getBasedOn().setVal("TableGrid");

      tblstyle.setTblPr(factory.createCTTblPrBase());

      // Set the cell margins
      TblWidth margin = factory.createTblWidth();
      margin.setW(BigInteger.valueOf(55)); // 55 twentieths of a point, seems to
                                           // look nice.

      tblstyle.getTblPr().setTblCellMar(factory.createCTTblCellMar());
      tblstyle.getTblPr().getTblCellMar().setTop(margin);
      tblstyle.getTblPr().getTblCellMar().setBottom(margin);
      tblstyle.getTblPr().getTblCellMar().setLeft(margin);
      tblstyle.getTblPr().getTblCellMar().setRight(margin);

      // Finally, save the styles so they can be added to documents.
      CUSTOM_STYLES.add(textBody);
      CUSTOM_STYLES.add(code);
      CUSTOM_STYLES.add(tableContents);
      CUSTOM_STYLES.add(tableHeader);
      CUSTOM_STYLES.add(horizontalRule);
    }

    public DocxVisitor(PageStore pageStore, PageInfo page, URLOutputFilter urlOutputFilter) {
      super(urlOutputFilter);

      _pageStore = pageStore;
      _page = page;

      try {
        _package = WordprocessingMLPackage.createPackage();
        _mainPart = _package.getMainDocumentPart();
      }
      catch (InvalidFormatException e) {
        throw new RuntimeException(e);
      }

      _factory = new ObjectFactory();
      _document = _mainPart.getContents();
      _body = _factory.createBody();

      _document.setBody(_body);
      enterContext(_body, true);

      // Set-up the formatting
      _bold = _factory.createBooleanDefaultTrue();
      _bold.setVal(Boolean.FALSE);

      _italic = _factory.createBooleanDefaultTrue();
      _italic.setVal(Boolean.FALSE);

      _strike = _factory.createBooleanDefaultTrue();
      _strike.setVal(Boolean.FALSE);

      // Apply the default numbering.
      try {
        NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
        ndp.unmarshalDefaultNumbering();
        _mainPart.addTargetPart(ndp);
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }

      // Apply the statically-constructed style definitions.
      _mainPart.getStyleDefinitionsPart().getJaxbElement().getStyle().addAll(CUSTOM_STYLES);
    }

    protected static Style constructStyle(final String name, final String basedOn, final String type, final Optional<JcEnumeration> justification, final Optional<PPrBase.Spacing> spacing, final boolean bold) {
      ObjectFactory factory = new ObjectFactory();

      // Create the style
      Style style = factory.createStyle();
      style.setStyleId(styleNameToId(name));
      style.setBasedOn(factory.createStyleBasedOn());
      style.getBasedOn().setVal(styleNameToId(basedOn));
      style.setName(factory.createStyleName());
      style.getName().setVal(name);
      style.setType(type);

      // Paragraph formatting
      if (justification.isPresent() || spacing.isPresent()) {
        style.setPPr(factory.createPPr());

        if (justification.isPresent()) {
          style.getPPr().setJc(factory.createJc());
          style.getPPr().getJc().setVal(justification.get());
        }

        if (spacing.isPresent()) {
          style.getPPr().setSpacing(spacing.get());
        }
      }

      // Run formatting
      if (bold) {
        style.setRPr(factory.createRPr());
        style.getRPr().setB(new BooleanDefaultTrue());
      }

      return style;
    }

    /**
     * Style IDs are names with no spaces.
     */
    protected static String styleNameToId(final String name) {
      return name.replace(" ", "");
    }

    /** Style a paragraph. */
    protected static void paraStyle(final P paragraph, final String style) {
      ObjectFactory factory = new ObjectFactory();

      if (paragraph.getPPr() == null) {
        paragraph.setPPr(factory.createPPr());
      }

      if (paragraph.getPPr().getPStyle() == null) {
        paragraph.getPPr().setPStyle(factory.createPPrBasePStyle());
      }

      paragraph.getPPr().getPStyle().setVal(styleNameToId(style));
    }

    /** Set the text of a run. */
    protected static void runText(final R run, final String str) {
      ObjectFactory factory = new ObjectFactory();

      Text text = factory.createText();
      text.setValue(str);
      run.getContent().add(factory.createRT(text));
    }

    /** Set the font of some run properties. */
    protected static void runFont(RPr props, final String font) {
      ObjectFactory factory = new ObjectFactory();

      props.setRFonts(factory.createRFonts());
      props.getRFonts().setAscii(font);
      props.getRFonts().setHAnsi(font);
    }

    /** Apply alignment to a table cell. */
    protected static void applyValign(final Tc tablecell, final String valign) {
      ObjectFactory factory = new ObjectFactory();

      tablecell.setTcPr(factory.createTcPr());
      tablecell.getTcPr().setVAlign(factory.createCTVerticalJc());

      if (valign.equals("top")) {
        tablecell.getTcPr().getVAlign().setVal(STVerticalJc.TOP);
      }
      else if (valign.equals("middle") || valign.equals("center") || valign.equals("centre")) {
        tablecell.getTcPr().getVAlign().setVal(STVerticalJc.CENTER);
      }
      else if (valign.equals("bottom")) {
        tablecell.getTcPr().getVAlign().setVal(STVerticalJc.BOTTOM);
      }
    }

    /**
     * Build the document by visiting the children, and then serialise it to an
     * input stream.
     */
    @Override
    public InputStream visitPage(final Page node) {
      // Build document
      visitASTNode(node);

      // And turn it into a bytestream
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      try {
        _package.save(out);
      }
      catch (Docx4JException e) {
        System.err.println("Error outputting document: " + e);
      }

      return new ByteArrayInputStream(out.toByteArray());
    }

    /** Set some formatting, visit children, and unset the formatting. */
    protected InputStream withFormatting(final BooleanDefaultTrue fmat, final ASTNode node) {
      fmat.setVal(true);
      visitASTNode(node);
      fmat.setVal(false);

      return nullval();
    }

    /**
     * Like withContextSimple, but also adds the context to the containing
     * block.
     */
    protected InputStream withContext(final ContentAccessor ctx, final ASTNode node, final boolean block) {
      commitBlock(ctx);
      return withContextSimple(ctx, node, block);
    }

    /** Push a context, visit children, and pop the context. */
    protected InputStream withContextSimple(final ContentAccessor ctx, final ASTNode node, final boolean block) {
      enterContext(ctx, block);
      visitASTNode(node);
      exitContext(block);

      return nullval();
    }

    /** Push a context. */
    protected void enterContext(final ContentAccessor ctx, final boolean block) {
      _contexts.push(ctx);
      if (block) {
        _blockContexts.push(ctx);
      }
    }

    /** Pop a context. */
    protected void exitContext(final boolean block) {
      if (block) {
        _blockContexts.pop();
      }
      _contexts.pop();
    }

    /** Construct a new numbering, push it, visit children, and pop. */
    protected InputStream withNumbering(final BigInteger type, final ASTNode node) {
      enterListContext(type);
      visitASTNode(node);
      exitListContext();

      return nullval();
    }

    /** Construct and push a new list context. */
    protected void enterListContext(final BigInteger type) {
      // Construct a new list context at the appropriate indentation level and
      // push it to the stack.
      PPrBase.NumPr numpr = _factory.createPPrBaseNumPr();
      numpr.setIlvl(_factory.createPPrBaseNumPrIlvl());
      numpr.setNumId(_factory.createPPrBaseNumPrNumId());

      numpr.getNumId().setVal(type);

      if (_numberings.isEmpty()) {
        numpr.getIlvl().setVal(BigInteger.ZERO);
      }
      else {
        BigInteger last = _numberings.peek().getIlvl().getVal();
        numpr.getIlvl().setVal(last.add(BigInteger.ONE));
      }

      _numberings.push(numpr);
    }

    /** Leave a list context. */
    protected void exitListContext() {
      _numberings.pop();
    }

    /** Make a new run, adding it to the current context. */
    public R constructRun(final boolean applyFormatting) {
      R run = _factory.createR();
      commitInline(run);

      if (applyFormatting) {
        run.setRPr(_factory.createRPr());
        run.getRPr().setB(new BooleanDefaultTrue());
        run.getRPr().setI(new BooleanDefaultTrue());
        run.getRPr().setStrike(new BooleanDefaultTrue());

        run.getRPr().getB().setVal(_bold.isVal());
        run.getRPr().getI().setVal(_italic.isVal());
        run.getRPr().getStrike().setVal(_strike.isVal());
      }

      return run;
    }

    /** Save a block to the top block context. */
    protected void commitBlock(Object o) {
      _blockContexts.peek().getContent().add(o);
    }

    /** Save an inline element to the top context. */
    protected void commitInline(Object o) {
      _contexts.peek().getContent().add(o);
    }

    @Override
    public InputStream visitBold(final Bold node) {
      return withFormatting(_bold, node);
    }

    @Override
    public InputStream visitCode(final Code node) {
      P code = _factory.createP();
      paraStyle(code, CODE_STYLE);

      R run = constructRun(false);
      runText(run, node.getText());

      code.getContent().add(run);
      commitBlock(code);

      return nullval();
    }

    @Override
    public InputStream visitHeading(final Heading node) {
      P heading = _factory.createP();

      // Apply the style
      switch (node.getLevel()) {
        case 1:
          paraStyle(heading, HEADING1_STYLE);
          break;
        case 2:
          paraStyle(heading, HEADING2_STYLE);
          break;
        case 3:
          paraStyle(heading, HEADING3_STYLE);
          break;
        case 4:
          paraStyle(heading, HEADING4_STYLE);
          break;
        case 5:
          paraStyle(heading, HEADING5_STYLE);
          break;
        default:
          paraStyle(heading, HEADING6_STYLE);
      }

      // Finally render the contents of the heading
      return withContext(heading, node, false);
    }

    @Override
    public InputStream visitHorizontalRule(final HorizontalRule node) {
      P hrule = _factory.createP();
      commitBlock(hrule);
      paraStyle(hrule, HORIZONTAL_RULE_STYLE);

      return nullval();
    }

    protected InputStream imageError(final Image node, final String msg, final Exception exc) {
      System.err.println(msg);
      exc.printStackTrace();

      return renderBrokenImage(node);
    }

    @Override
    public InputStream renderImage(final String target, final String title, final Image node) {
      // First we get a relation pointing to the image
      BinaryPartAbstractImage imagePart;
      org.docx4j.dml.wordprocessingDrawing.Inline inline;

      String filename = target.substring(target.lastIndexOf("/") + 1);
      byte[] bytes;

      // First we need to get the image bytes
      try {
        // Try to fetch the image from the page store
        bytes = _pageStore.attachmentBytes(_page, filename, -1);
      }
      catch (NotFoundException e) {
        // Try to fetch it by URL
        try {
          bytes = IOUtils.toByteArray(new URL(target).openStream());
        }
        catch (MalformedURLException e1) {
          // This should never happen because we were able to construct the URL
          // in the first place.
          return imageError(node, "The impossible happened.", e1);
        }
        catch (IOException e1) {
          return imageError(node, "Failed to retrieve image by URL.", e1);
        }
      }
      catch (PageStoreException e) {
        return imageError(node, "Failed to fetch file from page store.", e);
      }

      // Then we embed it into the document
      try {
        imagePart = BinaryPartAbstractImage.createImagePart(_package, bytes);
        inline = imagePart.createImageInline(filename, title, 1, 2, false);
      }
      catch (Exception e) {
        return imageError(node, "Something went wrong embedding the image.", e);
      }

      // Then we add it to the current paragraph.
      R run = constructRun(false);

      Drawing drawing = _factory.createDrawing();
      run.getContent().add(drawing);

      drawing.getAnchorOrInline().add(inline);

      return nullval();
    }

    @Override
    public InputStream visitInlineCode(final InlineCode node) {
      R run = constructRun(false);
      run.setRPr(_factory.createRPr());
      runFont(run.getRPr(), CODE_FONT);
      runText(run, node.getText());

      return nullval();
    }

    @Override
    public InputStream visitItalic(final Italic node) {
      return withFormatting(_italic, node);
    }

    @Override
    public InputStream visitLinebreak(final Linebreak node) {
      R run = constructRun(false);
      run.getContent().add(_factory.createBr());

      return nullval();
    }

    @Override
    public InputStream renderLink(final String target, final String title, final Link node) {
      P.Hyperlink hyperlink = _factory.createPHyperlink();
      hyperlink.setAnchor(target);

      R run = _factory.createR();
      hyperlink.getContent().add(run);
      runText(run, title);

      commitInline(hyperlink);

      return nullval();
    }

    @Override
    public InputStream visitListItem(final ListItem node) {
      // A list item is just a paragraph with some numbering applied.
      P listitem = _factory.createP();
      listitem.setPPr(_factory.createPPr());
      listitem.getPPr().setNumPr(_numberings.peek());

      return withContext(listitem, node, false);
    }

    @Override
    public InputStream visitMacroNode(final MacroNode node) {
      // If in block position, render to a new paragraph.
      if (node.isBlock()) {
        P para = _factory.createP();
        commitBlock(para);
        _contexts.push(para);
      }

      visitTextNode(node);

      // And then remove the paragraph afterwards.
      if (node.isBlock()) {
        _contexts.pop();
      }

      return nullval();
    }

    @Override
    public InputStream visitOrderedList(final OrderedList node) {
      return withNumbering(ORDERED_LIST_ID, node);
    }

    @Override
    public InputStream visitParagraph(final Paragraph node) {
      P paragraph = _factory.createP();

      // If there's a paragraph style currently in effect, apply it.
      if (_paragraphStyle == null) {
        paraStyle(paragraph, TEXT_BODY_STYLE);
      }
      else {
        paraStyle(paragraph, _paragraphStyle);
      }

      return withContext(paragraph, node, false);
    }

    @Override
    public InputStream visitStrikethrough(final Strikethrough node) {
      return withFormatting(_strike, node);
    }

    @Override
    public InputStream visitTable(final Table node) {
      Tbl table = _factory.createTbl();

      // Set the style to our custom one.
      table.setTblPr(_factory.createTblPr());
      table.getTblPr().setTblStyle(_factory.createCTTblPrBaseTblStyle());
      table.getTblPr().getTblStyle().setVal(styleNameToId(TABLE_STYLE));

      // Set the preferred width to 9638 twentieths of a point - the width of a
      // page with default margins.
      //
      // The default width if unspecified is 0, which is interpreted as
      // "grow the table until everything fits", as all widths are preferred.
      // HOWEVER, libreoffice does not cope well with this, producing a table so
      // wide as to go off the right edge of the page. The weird behaviour
      // doesn't stop there, though, it's also dependent on units - dxa is the
      // default, but the results of setting the width to 1 and 1dxa are very
      // different.
      //
      // There is a bug report on the libreoffice bugzilla about this, but it's
      // been open since 2013 with no progress.
      //
      // See http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/tblW_2.html
      // for details on the correct interpretation of width.
      table.getTblPr().setTblW(_factory.createTblWidth());
      table.getTblPr().getTblW().setW(BigInteger.valueOf(9638));
      table.getTblPr().getTblW().setType("dxa");

      return withContext(table, node, true);
    }

    /**
     * Apply the vertical alignment directive to table cells.
     */
    protected void valign(final Tc tablecell) {
      if (isEnabled(TABLE_ALIGNMENT_DIRECTIVE)) {
        applyValign(tablecell, unsafeGetArgs(TABLE_ALIGNMENT_DIRECTIVE).get(0));
      }
    }

    @Override
    public InputStream visitTableCell(final TableCell node) {
      Tc tablecell = _factory.createTc();
      commitBlock(tablecell);
      valign(tablecell);

      P para = _factory.createP();
      tablecell.getContent().add(para);
      paraStyle(para, TABLE_CONTENTS_STYLE);

      return withContextSimple(para, node, false);
    }

    @Override
    public InputStream visitTableHeaderCell(final TableHeaderCell node) {
      Tc tablecell = _factory.createTc();
      commitBlock(tablecell);
      valign(tablecell);

      P para = _factory.createP();
      tablecell.getContent().add(para);
      paraStyle(para, TABLE_HEADER_STYLE);

      return withContextSimple(para, node, false);
    }

    @Override
    public InputStream visitTableRow(final TableRow node) {
      return withContext(_factory.createTr(), node, true);
    }

    @Override
    public InputStream visitTextNode(final TextNode node) {
      R run = constructRun(true);

      // Docx is linebreak sensitive - despite being an XML-based format.
      runText(run, node.getText().replace("\r", "").replace("\n", ""));

      return nullval();
    }

    @Override
    public InputStream visitUnorderedList(final UnorderedList node) {
      return withNumbering(UNORDERED_LIST_ID, node);
    }
  }
}