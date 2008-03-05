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
package org.tmatesoft.svn.cli.command;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import org.tmatesoft.svn.cli.SVNArgument;
import org.tmatesoft.svn.cli.SVNCommand;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.admin.ISVNAdminEventHandler;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;
import org.tmatesoft.svn.core.wc.admin.SVNAdminEvent;
import org.tmatesoft.svn.core.wc.admin.SVNAdminEventAction;

/**
 * @version 1.1.1
 * @author  TMate Software Ltd.
 * @since   1.1.1
 */
public class SVNAdminDumpCommand extends SVNCommand implements ISVNAdminEventHandler {
    private boolean myIsQuiet;
    private PrintStream myOut;
    
    public void run(InputStream in, PrintStream out, PrintStream err) throws SVNException {
        run(out, err);
    }

    public void run(PrintStream out, PrintStream err) throws SVNException {
        if (!getCommandLine().hasPaths()) {
            SVNCommand.println(out, "jsvnadmin: Repository argument required");
            System.exit(1);
        }
        File reposRoot = new File(getCommandLine().getPathAt(0));  
        
        SVNRevision[] revRange = getStartEndRevisions();
        SVNRevision rStart = revRange[0];
        SVNRevision rEnd = revRange[1];

        boolean isIncremental = getCommandLine().hasArgument(SVNArgument.INCREMENTAL);
        boolean useDeltas = getCommandLine().hasArgument(SVNArgument.DELTAS);
        
        myIsQuiet = getCommandLine().hasArgument(SVNArgument.QUIET); 
        myOut = err;

        SVNAdminClient adminClient = getClientManager().getAdminClient();
        adminClient.setEventHandler(this);
        adminClient.doDump(reposRoot, out, rStart, rEnd, isIncremental, useDeltas);
    }

    public void handleAdminEvent(SVNAdminEvent event, double progress) throws SVNException {
        if (!myIsQuiet && event != null && event.getAction() == SVNAdminEventAction.REVISION_DUMPED) {
            myOut.println(event.getMessage());
        }
    }
    
    public void handleEvent(SVNEvent event, double progress) throws SVNException {
    }    
    
    public void checkCancelled() throws SVNCancelException {
    }
}
