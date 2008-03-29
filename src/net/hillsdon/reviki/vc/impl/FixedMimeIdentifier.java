package net.hillsdon.reviki.vc.impl;

import static java.util.Collections.unmodifiableSet;
import static net.hillsdon.fij.core.Functional.set;

import java.util.Locale;
import java.util.Set;

import net.hillsdon.reviki.vc.MimeIdentifier;

/**
 * Would be better not to hard-code but this is a start...
 * 
 * @author mth
 */
public class FixedMimeIdentifier implements MimeIdentifier {

  private static final Set<String> IMAGE_EXTENSIONS = unmodifiableSet(set(
    "bmp",
    "cgm",
    "gif",
    "jpeg", 
    "jpg",
    "svg",
    "png"
  ));
  
  public boolean isImage(final String fileName) {
    int dot = fileName.indexOf('.');
    return IMAGE_EXTENSIONS.contains(fileName.substring(dot + 1).toLowerCase(Locale.US));
  }

}
