package net.hillsdon.reviki.wiki.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.bind.JAXBElement;

import net.hillsdon.reviki.vc.impl.SimplePageStore;
import net.hillsdon.reviki.web.common.ViewTypeConstants;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.impl.ExampleDotComWikiUrls;
import net.hillsdon.reviki.wiki.renderer.DocxRenderer.DocxVisitor;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Page;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import org.apache.commons.io.IOUtils;
import org.docx4j.wml.*;

import com.google.common.base.Optional;
import com.google.common.base.Suppliers;

import junit.framework.TestCase;

/**
 * Because the docx renderer is very side-effectful, this only attempts to test
 * the more clean parts of it. Hopefully that's enough.
 */
public class TestDocxRenderer extends TestCase {
  private DocxRenderer _renderer;

  private DocxVisitor _visitor;

  private ObjectFactory _factory;

  public void setUp() {
    _visitor = new DocxVisitor(URLOutputFilter.NULL);
    _factory = new ObjectFactory();

    SvnWikiRenderer svnrenderer = new SvnWikiRenderer(new FakeConfiguration(), new SimplePageStore(), new InternalLinker(new ExampleDotComWikiUrls()), Suppliers.ofInstance(Collections.<Macro> emptyList()));
    _renderer = (DocxRenderer) svnrenderer.getRenderers().getRenderer(ViewTypeConstants.CTYPE_DOCX);
  }

  /** Test that we can construct concrete numberings. */
  public void testConstructNumbering() {
    Numbering.Num num = DocxVisitor.constructConcreteNumbering(BigInteger.ONE);
    assertTrue(num.getAbstractNumId().getVal().equals(BigInteger.ONE));
  }

  /** Test that we can construct abstract numberings. */
  public void testConstructAbstractNumbering() {
    BigInteger id = BigInteger.ONE;
    NumberFormat[] fmats = new NumberFormat[] { NumberFormat.BULLET, NumberFormat.DECIMAL };
    String[] strs = new String[] { "a", "b", "%C" };
    Numbering.AbstractNum anum = DocxVisitor.constructAbstractNumbering(id, fmats, strs);

    assertTrue(anum.getAbstractNumId().equals(id));

    // We have 10 levels of indentation
    assertEquals(10, anum.getLvl().size());

    // Which cycle through the formats and strings
    BigInteger lastindent = BigInteger.ZERO;
    for (int i = 0; i < anum.getLvl().size(); i++) {
      Lvl lvl = anum.getLvl().get(i);

      BigInteger ilvl = BigInteger.valueOf(i);
      NumberFormat fmat = fmats[i % fmats.length];
      // C is replaced with the counter value.
      String str = strs[i % strs.length].replace("C", "" + (i + 1));

      assertTrue(lvl.getIlvl().equals(ilvl));
      assertTrue(lvl.getNumFmt().getVal().equals(fmat));
      assertTrue(lvl.getLvlText().getVal().equals(str));

      // Indentation also increases
      BigInteger indent = lvl.getPPr().getInd().getLeft();
      assertEquals(-1, lastindent.compareTo(indent));

      lastindent = indent;
    }
  }

  /** Test that we can construct numbering levels. */
  public void testConstructNumberingLevel() {
    NumberFormat fmat = NumberFormat.CHICAGO;
    long ilvl = 10;
    String str = "woo";

    Lvl lvl = DocxVisitor.constructNumberingLevel(fmat, ilvl, str);

    assertTrue(lvl.getIlvl().equals(BigInteger.valueOf(ilvl)));
    assertTrue(lvl.getNumFmt().getVal().equals(fmat));
    assertTrue(lvl.getLvlText().getVal().equals(str));
  }

  /** Test that we can construct styles. */
  public void testConstructStyle() {
    String name = "Hello World";
    String basedOn = "Test Style";
    String type = "paragraph";
    Optional<JcEnumeration> justification = Optional.of(JcEnumeration.RIGHT);

    Optional<PPrBase.Spacing> spacing = Optional.of(_factory.createPPrBaseSpacing());
    spacing.get().setAfter(BigInteger.valueOf(140));
    spacing.get().setLine(BigInteger.valueOf(288));
    spacing.get().setLineRule(STLineSpacingRule.AUTO);
    spacing.get().setBefore(BigInteger.ZERO);

    boolean bold = true;

    Style style = DocxVisitor.constructStyle(name, basedOn, type, justification, spacing, bold);
    Style style2 = DocxVisitor.constructStyle(name, basedOn, type, justification, spacing, false);

    assertTrue(style.getName().getVal().equals(name));
    assertTrue(style.getStyleId().equals(DocxVisitor.styleNameToId(name)));
    assertTrue(style.getBasedOn().getVal().equals(DocxVisitor.styleNameToId(basedOn)));
    assertTrue(style.getType().equals(type));
    assertTrue(style.getPPr().getJc().getVal().equals(justification.get()));
    assertTrue(style.getPPr().getSpacing().equals(spacing.get()));
    assertEquals(style.getRPr().getB().isVal(), bold);

    // RPr is only set if there is something (a bold, in the case of these
    // styles) to go in it.
    assertNull(style2.getRPr());
  }

  /** Test that we can get the true name (id) of a style. */
  public void testStyleNameToId() {
    assertTrue(DocxVisitor.styleNameToId("hello world").equals("helloworld"));
    assertTrue(DocxVisitor.styleNameToId(" foo").equals("foo"));
    assertTrue(DocxVisitor.styleNameToId("bar ").equals("bar"));
    assertTrue(DocxVisitor.styleNameToId(" foo bar baz ").equals("foobarbaz"));
  }

  /** Test that we can enter and exit regular contexts. */
  public void testContexts() {
    assertEquals(1, _visitor._contexts.size());
    assertEquals(1, _visitor._blockContexts.size());

    P paragraph = _factory.createP();
    _visitor.enterContext(paragraph, false);

    assertEquals(2, _visitor._contexts.size());
    assertEquals(1, _visitor._blockContexts.size());

    Body body = _factory.createBody();
    _visitor.enterContext(body, true);

    assertEquals(3, _visitor._contexts.size());
    assertEquals(2, _visitor._blockContexts.size());

    _visitor.exitContext(true);

    assertEquals(2, _visitor._contexts.size());
    assertEquals(1, _visitor._blockContexts.size());

    _visitor.exitContext(false);

    assertEquals(1, _visitor._contexts.size());
    assertEquals(1, _visitor._blockContexts.size());
  }

  /** Test we can enter and exit list contexts. */
  public void testListContexts() {
    BigInteger id = DocxVisitor.UNORDERED_LIST_ID;

    assertEquals(0, _visitor._numberings.size());

    _visitor.enterListContext(id);

    assertEquals(1, _visitor._numberings.size());

    PPrBase.NumPr numpr = _visitor._numberings.peek();
    assertTrue(numpr.getNumId().getVal().equals(id));
    assertTrue(numpr.getIlvl().getVal().equals(BigInteger.ZERO));

    _visitor.enterListContext(id);

    assertEquals(2, _visitor._numberings.size());

    numpr = _visitor._numberings.peek();
    assertTrue(numpr.getNumId().getVal().equals(id));
    assertTrue(numpr.getIlvl().getVal().equals(BigInteger.ONE));

    _visitor.exitListContext();

    assertEquals(1, _visitor._numberings.size());

    _visitor.exitListContext();

    assertEquals(0, _visitor._numberings.size());
  }

  /** Test that we can apply styles to paragraphs. */
  public void testParaStyle() {
    P paragraph = _factory.createP();
    String style1 = "foo";
    String style2 = "foo";

    DocxVisitor.paraStyle(paragraph, style1);
    assertTrue(paragraph.getPPr().getPStyle().getVal().equals(style1));

    DocxVisitor.paraStyle(paragraph, style2);
    assertTrue(paragraph.getPPr().getPStyle().getVal().equals(style2));
  }

  /** Test that we can set the text of a run. */
  @SuppressWarnings("unchecked")
  public void testRunText() {
    R run = _factory.createR();
    String text = "hello world";

    DocxVisitor.runText(run, text);

    assertTrue(((JAXBElement<Text>) run.getContent().get(0)).getValue().getValue().equals(text));
  }

  /** Test that we can construct a run and push it to a context. */
  public void testConstructRun() {
    P paragraph = _factory.createP();
    _visitor.enterContext(paragraph, false);

    _visitor._bold.setVal(Boolean.TRUE);
    _visitor._italic.setVal(Boolean.TRUE);

    R run = _visitor.constructRun(false);

    assertTrue(paragraph.getContent().contains(run));
    assertNull(run.getRPr());

    run = _visitor.constructRun(true);

    assertTrue(run.getRPr().getB().isVal());
    assertTrue(run.getRPr().getI().isVal());
    assertFalse(run.getRPr().getStrike().isVal());
  }

  /** Test that we can set the font of a run. */
  public void testRPrFont() {
    RPr rpr = _factory.createRPr();
    String font = "A Font";

    DocxVisitor.runFont(rpr, font);

    assertTrue(rpr.getRFonts().getAscii().equals(font));
    assertTrue(rpr.getRFonts().getHAnsi().equals(font));
  }

  /** Test that we can commit things to the context. */
  public void testContextCommit() {
    P paragraph = _factory.createP();
    R run = _factory.createR();

    _visitor.commitBlock(paragraph);
    _visitor._contexts.push(paragraph);
    _visitor.commitInline(run);

    assertTrue(_visitor._blockContexts.peek().getContent().contains(paragraph));
    assertTrue(paragraph.getContent().contains(run));
    assertFalse(_visitor._blockContexts.peek().getContent().contains(run));
  }

  /** Test that we can align table cells. */
  public void testTableCellValign() {
    Tc tablecell = _factory.createTc();

    DocxVisitor.applyValign(tablecell, "top");
    assertTrue(tablecell.getTcPr().getVAlign().getVal().equals(STVerticalJc.TOP));

    DocxVisitor.applyValign(tablecell, "middle");
    assertTrue(tablecell.getTcPr().getVAlign().getVal().equals(STVerticalJc.CENTER));

    DocxVisitor.applyValign(tablecell, "center");
    assertTrue(tablecell.getTcPr().getVAlign().getVal().equals(STVerticalJc.CENTER));

    DocxVisitor.applyValign(tablecell, "centre");
    assertTrue(tablecell.getTcPr().getVAlign().getVal().equals(STVerticalJc.CENTER));

    DocxVisitor.applyValign(tablecell, "bottom");
    assertTrue(tablecell.getTcPr().getVAlign().getVal().equals(STVerticalJc.BOTTOM));
  }

  /** Sanity check: check that we actually get output. */
  public void testSanity() {
    Page page = new Page(new ArrayList<ASTNode>());
    InputStream is = _renderer.build(page, URLOutputFilter.NULL);

    assertNotNull(is);

    try {
      byte[] rendered = IOUtils.toByteArray(is);
      assertFalse(0 == rendered.length);
    }
    catch (IOException e) {
      assertFalse("Failed to extract byte array", true);
    }

  }
}
