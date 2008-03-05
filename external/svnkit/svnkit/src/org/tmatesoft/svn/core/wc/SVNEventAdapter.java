/*
 * ====================================================================
 * Copyright (c) 2004-2007 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.tmatesoft.svn.core.wc;

import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNException;


/**
 * @version 1.1.1
 * @author  TMate Software Ltd.
 */
public class SVNEventAdapter implements ISVNEventHandler {

    public void checkCancelled() throws SVNCancelException {
    }

    public void handleEvent(SVNEvent event, double progress) throws SVNException {
    }

}
