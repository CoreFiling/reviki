package net.hillsdon.reviki.wiki.macros;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A quick and dirty parser for named arguments to macros.
 * In the wiki markup, this will look like
 *  {@code <<search:(for="blort", foo="bar")>>}
 *
 *  The bit which this parses is {@code (for="blort", foo="bar")}
 */
public class MacroArgumentParser {
  private final List<String> _argumentNames;

  public MacroArgumentParser(final String... argumentNames) {
    super();
    _argumentNames = Arrays.asList(argumentNames);
  }

  public MacroArgumentParser(final Collection<String> col, final String... argumentNames) {
    super();
    _argumentNames = new ArrayList<String>(col);
    _argumentNames.addAll(Arrays.asList(argumentNames));
  }

  public List<String> getArgumentNames() {
    return _argumentNames;
  }

  private String ltrim(String in) {
    return in.replaceFirst("^ *", "");
  }

  public Map<String, String> parse(final String in) throws ParseException {
    String inTrimmed = ltrim(in).trim();

    if (!inTrimmed.startsWith("(") || !inTrimmed.endsWith(")")) {
      throw new ParseException("arguments must be wrapped in parentheses");
    }

    String unBracketted = ltrim(inTrimmed.substring(1, inTrimmed.length() - 1));

    Pattern p = Pattern.compile("([a-zA-Z]+)\\s*=\\s*\"([^\"]+)\"\\s*(?:,\\s*)?");
    Matcher m = p.matcher(unBracketted);

    int prevEnd = 0;
    Map<String, String> arguments = new LinkedHashMap<String, String>();

    while (m.find()) {
      if (m.start() != prevEnd) {
        throw new ParseException("found something unexpected between arguments (characters " + prevEnd + " to " + m.start() + " inclusive)");
      }

      String name = m.group(1);
      String value = m.group(2);

      if (!_argumentNames.contains(name)) {
        throw new ParseException("unknown argument named '" + name + "' used. Allowable arguments are: " + _argumentNames + ".");
      }
      if (arguments.containsKey(name)) {
        throw new ParseException("duplicate argument named '" + name + "'.");
      }

      arguments.put(name, value);
      prevEnd = m.end();
    }

    if (prevEnd != unBracketted.length()) {
      throw new ParseException("found something unexpected after all recognised arguments (characters " + prevEnd + " to " + unBracketted.length() + " inclusive) '" + unBracketted + "'.");
    }

    return arguments;
  }

  public class ParseException extends Exception {
    private ParseException(final String message, final Throwable cause) {
      super(message, cause);
    }

    private ParseException(final String message) {
      super(message);
    }
  }
}