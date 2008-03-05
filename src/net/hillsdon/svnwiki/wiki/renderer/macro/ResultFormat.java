package net.hillsdon.svnwiki.wiki.renderer.macro;

/**
 * Result of a macro may be in any of these formats.
 * 
 * Non-XHTML will be rendered to XHTML.
 * 
 * @author mth
 */
public enum ResultFormat {
  XHTML,
  WIKI
}
