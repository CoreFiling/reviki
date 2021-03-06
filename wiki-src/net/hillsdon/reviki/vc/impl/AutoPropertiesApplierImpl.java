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
/*
 * Code adapted from {@link DefaultSVNOptions}, which takes a java.io.File
 * and can't be made to get the properties from a different source.
 *
 * Original copyright follows.
 *
 * ====================================================================
 * Copyright (c) 2004-2007 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */

package net.hillsdon.reviki.vc.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;

import com.google.common.base.Function;

import net.hillsdon.reviki.vc.AutoProperties;

/**
 *
 *
 * @author mth
 */
public class AutoPropertiesApplierImpl implements AutoPropertiesApplier {

  private final AutoProperties _autoprops;

  /**
   * Replaced by reading from {@link #_autoprops} in {@link #read()}.
   */
  private Map<String, String> _current = Collections.emptyMap();

  public AutoPropertiesApplierImpl(final AutoProperties autoprops) {
    _autoprops = autoprops;
  }

  @Override
  public void read() {
    _current = _autoprops.read();
  }

  @Override
  public Map<String, String> apply(final String filename) {
    final Map<String, String> result = new LinkedHashMap<String, String>();
    final Map<String, String> autoprops = _current;
    for (Map.Entry<String, String> entry : autoprops.entrySet()) {
      final String pattern = entry.getKey();
      final String value = entry.getValue();
      if (value != null && !"".equals(value) && DefaultSVNOptions.matches(pattern, filename)) {
        for (StringTokenizer tokens = new StringTokenizer(value, ";"); tokens.hasMoreTokens();) {
          String token = tokens.nextToken().trim();
          int i = token.indexOf('=');
          if (i < 0) {
            result.put(token, "");
          }
          else {
            String name = token.substring(0, i).trim();
            String pValue = i == token.length() - 1 ? "" : token.substring(i + 1).trim();
            if (!"".equals(name.trim())) {
              if (pValue.startsWith("\"") && pValue.endsWith("\"") && pValue.length() > 1) {
                pValue = pValue.substring(1, pValue.length() - 1);
              }
              result.put(name, pValue);
            }
          }
        }
      }
    }
    return result;
  }

  public static Function<String, String> syntaxForFilename(final AutoPropertiesApplier propsApplier) {
    return new Function<String, String>() {
      @Override
      public String apply(final String filename) {
        if (propsApplier == null) {
          // This will only happen in tests.
          return "reviki";
        }
        propsApplier.read();
        return propsApplier.apply(filename).get("reviki:syntax");
      }
    };
  }

}
