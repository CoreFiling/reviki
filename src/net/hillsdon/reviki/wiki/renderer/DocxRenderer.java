package net.hillsdon.reviki.wiki.renderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.*;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.ast.*;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class DocxRenderer extends MarkupRenderer<InputStream> {

  private final PageStore _pageStore;

  private final LinkPartsHandler _linkHandler;

  private final LinkPartsHandler _imageHandler;

  private final Supplier<List<Macro>> _macros;

  public DocxRenderer(PageStore pageStore, LinkPartsHandler linkHandler, LinkPartsHandler imageHandler, Supplier<List<Macro>> macros) {
    _pageStore = pageStore;
    _linkHandler = linkHandler;
    _imageHandler = imageHandler;
    _macros = macros;

    renderer = new DocxVisitor();
  }

  @Override
  public ASTNode render(PageInfo page) throws IOException, PageStoreException {
    return CreoleRenderer.render(_pageStore, page, _linkHandler, _imageHandler, _macros);
  }

  @Override
  public String getContentType() {
    return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
  }

  /*
   * Generating a docx is very side-effectful - so all the methods return null,
   * except for visitPage which bundles the generated document into a stream.
   */
  private static final class DocxVisitor extends ASTRenderer<InputStream> {

    /** Styles for headings. We only go up to h6 in the ast. */
    private static final String HEADING1_STYLE = "Heading 1";

    private static final String HEADING2_STYLE = "Heading 2";

    private static final String HEADING3_STYLE = "Heading 3";

    private static final String HEADING4_STYLE = "Heading 4";

    private static final String HEADING5_STYLE = "Heading 5";

    private static final String HEADING6_STYLE = "Heading 6";

    /** The default text style. */
    private static final String TEXT_BODY_STYLE = "Text Body";

    /** Styles for tables. */
    private static final String TABLE_STYLE = "Padded Table";

    private static final String TABLE_HEADER_STYLE = "Table Heading";

    private static final String TABLE_CONTENTS_STYLE = "Table Contents";

    /** Style for both block and inline code. */
    private static final String CODE_STYLE = "Preformatted Text";

    /** Style for paragraphs-turned-into-horizontal rules. */
    private static final String HORIZONTAL_RULE_STYLE = "Horizontal Line";

    /** Style for hyperlinks. */
    private static final String HYPERLINK_STYLE = "InternetLink";

    /** The number style IDs for ordered and unordered lists. */
    private static final BigInteger _ordered = BigInteger.valueOf(2);

    private static final BigInteger _unordered = BigInteger.valueOf(1);

    /** The numbering definitions. */
    private static final Numbering _numbering;

    /** Custom styles. */
    private static final List<Style> _styles;

    /** Docx files are arranged into a "package" of smaller xml files. */
    private final WordprocessingMLPackage _package;

    /** The "main" part of the document, which has reference to other parts. */
    private final MainDocumentPart _mainPart;

    /** This is the actual document being rendered. */
    private final Document _document;

    /** And this is the body of the document! */
    private final Body _body;

    /** All objects have to come from a factory. */
    private final ObjectFactory _factory;

    /** The stack of containing contexts for new inline objects. */
    private final Stack<ContentAccessor> _contexts = new Stack<ContentAccessor>();

    /** The stack of containing contexts for new block objects. */
    private final Stack<ContentAccessor> _blockContexts = new Stack<ContentAccessor>();

    /** The stack of numbering styles for nested lists. */
    private final Stack<PPrBase.NumPr> _numberings = new Stack<PPrBase.NumPr>();

    /*
     * We can't nest bold/italic/strikethrough/etc text - so we need to keep
     * track of which bits are active when we generate "runs" - bits of plain
     * text.
     */
    private BooleanDefaultTrue _bold;

    private BooleanDefaultTrue _italic;

    private BooleanDefaultTrue _strike;

    /** Style to apply to all paragraphs, if set. */
    private String _paragraphStyle = null;

    /**
     * Set up numbering and styles
     */
    static {
      ObjectFactory factory = new ObjectFactory();

      /* ***** Lists. */
      _numbering = factory.createNumbering();

      // Construct the ordered lists
      NumberFormat[] orderedFmats = new NumberFormat[] { NumberFormat.DECIMAL, NumberFormat.LOWER_LETTER, NumberFormat.LOWER_ROMAN };
      String[] orderedStrs = new String[] { "%C)" }; // "C" is replaced with the
                                                     // appropriate number
                                                     // counter.
      Numbering.AbstractNum ordered = constructAbstractNumbering(_ordered, orderedFmats, orderedStrs);
      Numbering.Num orderedNum = constructConcreteNumbering(_ordered);

      // Construct the unordered lists
      NumberFormat[] unorderedFmats = new NumberFormat[] { NumberFormat.BULLET };
      String[] unorderedStrs = new String[] { "•", "◦", "▪" };
      Numbering.AbstractNum unordered = constructAbstractNumbering(_unordered, unorderedFmats, unorderedStrs);
      Numbering.Num unorderedNum = constructConcreteNumbering(_unordered);

      // Add the lists to the numbering.
      _numbering.getAbstractNum().add(ordered);
      _numbering.getNum().add(orderedNum);

      _numbering.getAbstractNum().add(unordered);
      _numbering.getNum().add(unorderedNum);

      /* ***** Styles. */
      _styles = new ArrayList<Style>();

      // Spacing for normal text, values from libreoffice
      PPrBase.Spacing spacing = factory.createPPrBaseSpacing();
      spacing.setAfter(BigInteger.valueOf(140));
      spacing.setLine(BigInteger.valueOf(288));
      spacing.setLineRule(STLineSpacingRule.AUTO);
      spacing.setBefore(BigInteger.ZERO);

      Style textBody = constructStyle(TEXT_BODY_STYLE, "Normal", "paragraph", Optional.<JcEnumeration> absent(), Optional.of(spacing), false);
      Style tableContents = constructStyle(TABLE_CONTENTS_STYLE, "TextBody", "paragraph", Optional.<JcEnumeration> absent(), Optional.<PPrBase.Spacing> absent(), false);
      Style tableHeader = constructStyle(TABLE_HEADER_STYLE, "TableContents", "paragraph", Optional.of(JcEnumeration.CENTER), Optional.<PPrBase.Spacing> absent(), true);

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
      _styles.add(textBody);
      _styles.add(tableContents);
      _styles.add(tableHeader);
    }

    public DocxVisitor() {
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
      _contexts.push(_body);
      _blockContexts.push(_body);

      // Set-up the formatting
      _bold = _factory.createBooleanDefaultTrue();
      _bold.setVal(new Boolean(false));

      _italic = _factory.createBooleanDefaultTrue();
      _italic.setVal(new Boolean(false));

      _strike = _factory.createBooleanDefaultTrue();
      _strike.setVal(new Boolean(false));

      // Apply the statically-constructed numbering definitions.
      try {
        NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
        ndp.setJaxbElement(_numbering);
        _mainPart.addTargetPart(ndp);
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }

      // Apply the statically-constructed style definitions.
      _mainPart.getStyleDefinitionsPart().getJaxbElement().getStyle().addAll(_styles);
    }

    /**
     * Construct a new concrete numbering. A concrete numbering links a
     * numbering ID to an abstract numbering - which defines what it actually
     * looks like.
     *
     * @param id The ID of the abstract numbering.
     * @return A new concrete numbering with a matching ID.
     */
    private static Numbering.Num constructConcreteNumbering(BigInteger id) {
      ObjectFactory factory = new ObjectFactory();
      Numbering.Num num = factory.createNumberingNum();

      num.setAbstractNumId(factory.createNumberingNumAbstractNumId());
      num.getAbstractNumId().setVal(id);
      num.setNumId(id);

      return num;
    }

    /**
     * Construct a new abstract numbering. An abstract numbering defines what
     * symbols and indentation the various sublists have.
     *
     * @param id The ID of the numbering, this is used to relate a paragraph to
     *          the numbering.
     * @param fmats Array of number formats to cycle through.
     * @param strs Array of bullet strings to cycle through. In these strings,
     *          "C" will be replaced with the appropriate counter number,
     *          allowing ordered lists to be produced.
     * @return A new abstract numbering, with up to 10 levels of list nesting.
     */
    private static Numbering.AbstractNum constructAbstractNumbering(BigInteger id, NumberFormat[] fmats, String[] strs) {
      ObjectFactory factory = new ObjectFactory();

      Numbering.AbstractNum abstractNum = factory.createNumberingAbstractNum();
      abstractNum.setAbstractNumId(id);
      abstractNum.setMultiLevelType(factory.createNumberingAbstractNumMultiLevelType());
      abstractNum.getMultiLevelType().setVal("multilevel");

      // Construct all the numbering levels, cycling through the formats and
      // display strings.
      for (int ilvl = 0; ilvl < 10; ilvl++) {
        long counter = ilvl + 1;

        NumberFormat fmat = fmats[ilvl % fmats.length];
        String str = strs[ilvl % strs.length];

        Lvl lvl = constructNumberingLevel(fmat, ilvl, str.replace("C", "" + counter));
        abstractNum.getLvl().add(lvl);
      }

      return abstractNum;
    }

    /**
     * Construct a new numbering level. A numbering level defines the symbol and
     * indentation to use at this particular nesting, when following its
     * containing abstract numbering.
     *
     * @param fmat The number format.
     * @param ilvl The indentation level (0-based).
     * @param str The bullet string.
     * @return A new numbering level, for this level of indentation.
     */
    private static Lvl constructNumberingLevel(NumberFormat fmat, long ilvl, String str) {
      ObjectFactory factory = new ObjectFactory();

      Lvl lvl = factory.createLvl();

      // Set the nesting level
      lvl.setIlvl(BigInteger.valueOf(ilvl));

      // Numbering starts from 1
      lvl.setStart(factory.createLvlStart());
      lvl.getStart().setVal(BigInteger.ONE);

      // Set the numbering format
      lvl.setNumFmt(factory.createNumFmt());
      lvl.getNumFmt().setVal(fmat);

      // Set the display text
      lvl.setLvlText(factory.createLvlLvlText());
      lvl.getLvlText().setVal(str);

      // Set the justification
      lvl.setLvlJc(factory.createJc());
      lvl.getLvlJc().setVal(JcEnumeration.LEFT);

      // Set the indentation
      BigInteger indent = BigInteger.valueOf(360 * (ilvl + 1));
      BigInteger hanging = BigInteger.valueOf(360);
      CTTabStop tabstop = factory.createCTTabStop();
      tabstop.setVal(STTabJc.NUM);
      tabstop.setPos(indent);

      lvl.setPPr(factory.createPPr());
      lvl.getPPr().setTabs(factory.createTabs());
      lvl.getPPr().getTabs().getTab().add(tabstop);
      lvl.getPPr().setInd(factory.createPPrBaseInd());
      lvl.getPPr().getInd().setLeft(indent);
      lvl.getPPr().getInd().setHanging(hanging);

      return lvl;
    }

    private static Style constructStyle(String name, String basedOn, String type, Optional<JcEnumeration> justification, Optional<PPrBase.Spacing> spacing, boolean bold) {
      ObjectFactory factory = new ObjectFactory();

      // Create the style
      Style style = factory.createStyle();
      style.setStyleId(styleNameToId(name));
      style.setBasedOn(factory.createStyleBasedOn());
      style.getBasedOn().setVal(basedOn);
      style.setName(factory.createStyleName());
      style.getName().setVal(name);
      style.setType(type);

      // Paragraph formatting
      style.setPPr(factory.createPPr());

      if (justification.isPresent()) {
        style.getPPr().setJc(factory.createJc());
        style.getPPr().getJc().setVal(justification.get());
      }

      if (spacing.isPresent()) {
        style.getPPr().setSpacing(spacing.get());
      }

      // Run formatting
      style.setRPr(factory.createRPr());
      style.getRPr().setB(new BooleanDefaultTrue());
      style.getRPr().getB().setVal(new Boolean(bold));

      return style;
    }

    /**
     * Style IDs are names with no spaces.
     */
    private static String styleNameToId(String name) {
      return name.replace(" ", "");
    }

    /**
     * Build the document by visiting the children, and then serialise it to an
     * input stream.
     */
    @Override
    public InputStream visitPage(Page node) {
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
    private InputStream withFormatting(BooleanDefaultTrue fmat, ASTNode node) {
      fmat.setVal(true);
      visitASTNode(node);
      fmat.setVal(false);

      return nullval();
    }

    /**
     * Like withContextSimple, but also adds the context to the containing block
     */
    private InputStream withContext(ContentAccessor ctx, ASTNode node, boolean block) {
      _blockContexts.peek().getContent().add(ctx);
      return withContextSimple(ctx, node, block);
    }

    /** Push a context, visit children, and pop the context. */
    private InputStream withContextSimple(ContentAccessor ctx, ASTNode node, boolean block) {
      _contexts.push(ctx);
      if (block) {
        _blockContexts.push(ctx);
      }

      visitASTNode(node);

      if (block) {
        _blockContexts.pop();
      }
      _contexts.pop();

      return nullval();
    }

    /** Construct a new numbering, push it, visit children, and pop. */
    private InputStream withNumbering(BigInteger type, ASTNode node) {
      // Construct a new list context at the appropriate indentation level and
      // push it to the stack.
      PPrBase.NumPr numpr = _factory.createPPrBaseNumPr();
      numpr.setIlvl(_factory.createPPrBaseNumPrIlvl());
      numpr.setNumId(_factory.createPPrBaseNumPrNumId());

      numpr.getNumId().setVal(type);

      if (_numberings.isEmpty()) {
        numpr.getIlvl().setVal(BigInteger.valueOf(0));
      }
      else {
        BigInteger last = _numberings.peek().getIlvl().getVal();
        numpr.getIlvl().setVal(last.add(BigInteger.ONE));
      }

      _numberings.push(numpr);
      visitASTNode(node);
      _numberings.pop();

      return nullval();
    }

    /** Style a paragraph. */
    public void paraStyle(P paragraph, String style) {
      if (paragraph.getPPr() == null) {
        paragraph.setPPr(_factory.createPPr());
      }

      if (paragraph.getPPr().getPStyle() == null) {
        paragraph.getPPr().setPStyle(_factory.createPPrBasePStyle());
      }

      paragraph.getPPr().getPStyle().setVal(styleNameToId(style));
    }

    /** Style a run. */
    public void runStyle(R run, String style) {
      if (run.getRPr() == null) {
        run.setRPr(_factory.createRPr());
      }

      if (run.getRPr().getRStyle() == null) {
        run.getRPr().setRStyle(_factory.createRStyle());
      }

      run.getRPr().getRStyle().setVal(style);
    }

    /** Set the text of a run. */
    public void runText(R run, String str) {
      Text text = _factory.createText();
      text.setValue(str);
      run.getContent().add(_factory.createRT(text));
    }

    /** Make a new run, adding it to the current context. */
    public R constructRun() {
      R run = _factory.createR();
      _contexts.peek().getContent().add(run);
      return run;
    }

    @Override
    public InputStream visitBold(Bold node) {
      return withFormatting(_bold, node);
    }

    @Override
    public InputStream visitCode(Code node) {
      R run = constructRun();
      runStyle(run, CODE_STYLE);
      runText(run, node.getText());

      return nullval();
    }

    @Override
    public InputStream visitHeading(Heading node) {
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
    public InputStream visitHorizontalRule(HorizontalRule node) {
      P hrule = _factory.createP();
      _document.getContent().add(hrule);
      paraStyle(hrule, HORIZONTAL_RULE_STYLE);

      return nullval();
    }

    @Override
    public InputStream renderImage(String target, String title, Image node) {
      // First we get a relation pointing to the image
      BinaryPartAbstractImage imagePart;
      org.docx4j.dml.wordprocessingDrawing.Inline inline;
      try {
        imagePart = BinaryPartAbstractImage.createLinkedImagePart(_package, new URL(target));
        inline = imagePart.createImageInline(target, title, 1, 2, true);
      }
      catch (Exception e) {
        // This shouldn't happen because we were able to successfully construct
        // the URL in the first place.
        return nullval();
      }

      // Then we add it to the current paragraph.
      R run = constructRun();

      Drawing drawing = _factory.createDrawing();
      run.getContent().add(drawing);

      drawing.getAnchorOrInline().add(inline);

      return nullval();
    }

    @Override
    public InputStream visitInlineCode(InlineCode node) {
      R run = constructRun();
      runStyle(run, CODE_STYLE);
      runText(run, node.getText());

      return nullval();
    }

    @Override
    public InputStream visitItalic(Italic node) {
      return withFormatting(_italic, node);
    }

    @Override
    public InputStream visitLinebreak(Linebreak node) {
      _contexts.peek().getContent().add(_factory.createBr());

      return nullval();
    }

    @Override
    public InputStream renderLink(String target, String title, Link node) {
      P.Hyperlink hyperlink = _factory.createPHyperlink();
      hyperlink.setAnchor(target);

      R run = _factory.createR();
      hyperlink.getContent().add(run);
      runStyle(run, HYPERLINK_STYLE);
      runText(run, title);

      _contexts.peek().getContent().add(hyperlink);

      return nullval();
    }

    @Override
    public InputStream visitListItem(ListItem node) {
      // A list item is just a paragraph with some numbering applied.
      P listitem = _factory.createP();
      listitem.setPPr(_factory.createPPr());
      listitem.getPPr().setNumPr(_numberings.peek());

      return withContext(listitem, node, false);
    }

    @Override
    public InputStream visitMacroNode(MacroNode node) {
      // If in block position, render to a new paragraph.
      if (node.isBlock()) {
        P para = _factory.createP();
        _blockContexts.peek().getContent().add(para);
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
    public InputStream visitOrderedList(OrderedList node) {
      return withNumbering(_ordered, node);
    }

    @Override
    public InputStream visitParagraph(Paragraph node) {
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
    public InputStream visitStrikethrough(Strikethrough node) {
      return withFormatting(_strike, node);
    }

    @Override
    public InputStream visitTable(Table node) {
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

    @Override
    public InputStream visitTableCell(TableCell node) {
      Tc tablecell = _factory.createTc();
      _blockContexts.peek().getContent().add(tablecell);

      P para = _factory.createP();
      tablecell.getContent().add(para);
      paraStyle(para, TABLE_CONTENTS_STYLE);

      return withContextSimple(para, node, false);
    }

    @Override
    public InputStream visitTableHeaderCell(TableHeaderCell node) {
      Tc tablecell = _factory.createTc();
      _blockContexts.peek().getContent().add(tablecell);

      P para = _factory.createP();
      tablecell.getContent().add(para);
      paraStyle(para, TABLE_HEADER_STYLE);

      return withContextSimple(para, node, false);
    }

    @Override
    public InputStream visitTableRow(TableRow node) {
      return withContext(_factory.createTr(), node, true);
    }

    @Override
    public InputStream visitTextNode(TextNode node) {
      R run = constructRun();

      // Set formatting
      run.setRPr(_factory.createRPr());
      run.getRPr().setB(_bold);
      run.getRPr().setI(_italic);
      run.getRPr().setStrike(_strike);

      // Set text
      runText(run, node.getText());

      return nullval();
    }

    @Override
    public InputStream visitUnorderedList(UnorderedList node) {
      return withNumbering(_unordered, node);
    }
  }
}
