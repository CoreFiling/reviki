package net.hillsdon.reviki.converter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.google.common.base.Optional;

import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.wiki.renderer.creole.Creole;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleTokens;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.Visitor;

public class Converter {

  private final InputStream _input;

  private static Optional<ParseTree> tryParse(final CommonTokenStream tokens, final Creole parser, final PredictionMode pmode) {
    parser.getInterpreter().setPredictionMode(pmode);
    try {
      return Optional.of((ParseTree) parser.creole());
    }
    catch (Exception e) {
      tokens.reset();
      parser.reset();

      return Optional.<ParseTree> absent();
    }
  }

  public Converter(final InputStream input) {
    _input = input;
  }

  public void writeTo(final OutputStream output) throws IOException {
    IOUtils.copy(IOUtils.toInputStream(convertToString(_input)), output);
  }

  private static String convertToString(final InputStream stream) throws IOException {
    CreoleTokens lexer = new CreoleTokens(null);
    lexer.setInputStream(new ANTLRInputStream(stream));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    Creole parser = new Creole(tokens);

    // First try parsing in SLL mode. This is really fast for pages with no
    // parse errors.
    Optional<ParseTree> tree = tryParse(tokens, parser, PredictionMode.SLL);

    if (!tree.isPresent()) {
      tree = tryParse(tokens, parser, PredictionMode.LL);
    }

    if (!tree.isPresent()) {
      throw new RuntimeException("Failed to parse given reviki");
    }

    LinkPartsHandler handler = new NullLinksPartHandler();

    Visitor visitor = new Visitor(new PageInfoImpl(""), handler, handler);

    return new MarkdownCreoleRenderer().visit(visitor.visit(tree.get()));
  }

	public static void main(final String[] args) throws IOException {
	  InputStream stream = System.in;
	  if (args.length == 1) {
	    if (!args[0].equals("-")) {
	      stream = new FileInputStream(args[0]);
	    }
	  }
	  else {
	    System.out.println("Usage: java -jar reviki-converter.jar <input_file | - >");
	    System.out.println();
	    System.out.println("  input_file  : reviki formatted file to convert");
	    System.out.println("                use '-' to read from stdin");
	    System.exit(1);
	  }

	  Converter converter = new Converter(stream);
	  ByteArrayOutputStream baos = new ByteArrayOutputStream();
    converter.writeTo(baos);
    System.out.print(baos.toString("UTF-8"));
  }

}
