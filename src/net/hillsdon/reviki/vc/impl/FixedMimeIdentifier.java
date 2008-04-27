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
