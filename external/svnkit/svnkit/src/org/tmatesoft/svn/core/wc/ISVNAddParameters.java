/*
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
package org.tmatesoft.svn.core.wc;

import java.io.File;


/**
 * @version 1.1.1
 * @author  TMate Software Ltd.
 * @see     DefaultSVNCommitParameters
 */
public interface ISVNAddParameters {

    public static final Action ADD_AS_BINARY = new Action();

    public static final Action ADD_AS_IS = new Action();
    
    public static final Action REPORT_ERROR = new Action();
    
    public Action onInconsistentEOLs(File file);

    /**
     * This class is simply used to define an action add 
     * operation should undertake in case of a inconsistent EOLs. 
     * 
     * @version 1.1
     * @author  TMate Software Ltd.
     */
    public static class Action {
        private Action() {
        }
    }
}
