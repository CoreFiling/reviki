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

import net.hillsdon.reviki.vc.AutoProperties;
import net.hillsdon.reviki.vc.PageStoreException;

import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;

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

  public void read() throws PageStoreException {
    _current = _autoprops.read();
  }
  
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

}
