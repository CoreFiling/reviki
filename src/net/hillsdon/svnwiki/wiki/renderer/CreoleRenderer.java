package net.hillsdon.svnwiki.wiki.renderer;

public class CreoleRenderer {

  private static class Heading extends RuleTreeNode {
    public Heading(final int number) {
      super(String.format("(?:^|\n)={%d}(.+?)(?:\n|$)", number), "h" + number);
    }
  }

  private static class List extends RuleTreeNode {
    public List(final String match, final String tag) {
      super("(?:^|\\n)(" + match + "[^*#].*(\\n|$)([*#]{2}.*(\\n|$))*)+", tag, 0);
    }
  }

  /**
   * Start here.
   */
  private static final RuleTreeNode ROOT;
  static {
    RuleTreeNode root = new RuleTreeNode("", "");
    RuleTreeNode noWiki = new RuleTreeNode("(?:^|\\n)[{][{][{]\\n?(.*?(\\n.*?)*?)[}][}][}](\\n|$)", "pre");
    RuleTreeNode paragraph = new RuleTreeNode("(?:^|\n)(.+?)(?:$|\n\n)", "p");
    RuleTreeNode italic = new RuleTreeNode("//(.*?)//", "em");
    RuleTreeNode bold = new RuleTreeNode("[*][*](.*?)[*][*]", "strong");
    RuleTreeNode unorderedList = new List("\\*", "ul");
    RuleTreeNode orderedList = new List("#", "ol");
    RuleTreeNode listItem = new RuleTreeNode("[*#](.+?)($|\\n", "li")
                              .setChildren(bold, italic);
    root.setChildren(
        noWiki.setChildren(), 
        new Heading(5).setChildren(bold, italic),
        new Heading(4).setChildren(bold, italic), 
        new Heading(3).setChildren(bold, italic), 
        new Heading(2).setChildren(bold, italic), 
        new Heading(1).setChildren(bold, italic),
        orderedList.setChildren(listItem),
        unorderedList.setChildren(listItem),
        paragraph.setChildren(bold, italic, orderedList, unorderedList), 
        italic.setChildren(bold, italic), 
        bold.setChildren(bold, italic));
    ROOT = root;
  }
  
  public String render(final String in) {
    return ROOT.render(in);
  }
  
}
