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

import org.tmatesoft.svn.cli.SVNCommand;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.admin.ISVNAdminEventHandler;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;
import org.tmatesoft.svn.core.wc.admin.SVNAdminEvent;
import org.tmatesoft.svn.core.wc.admin.SVNAdminEventAction;


/**
 * @version 1.1.2
 * @author  TMate Software Ltd.
 * @since   1.1.2
 */
public class SVNAdminRemoveLocksCommand extends SVNCommand implements ISVNAdminEventHandler {
    private PrintStream myOut;

    public void run(PrintStream out, PrintStream err) throws SVNException {
        if (!getCommandLine().hasPaths()) {
            SVNCommand.println(out, "svnadmin: Repository argument required");
            System.exit(1);
        }
        File reposRoot = new File(getCommandLine().getPathAt(0));  
        String[] paths = new String[getCommandLine().getPathCount() - 1];
        for (int i = 1; i < getCommandLine().getPathCount() - 1; i++) {
            paths[i] = getCommandLine().getPathAt(i);
        }
        
        myOut = out;
        SVNAdminClient adminClient = getClientManager().getAdminClient();
        adminClient.setEventHandler(this);
        adminClient.doRemoveLocks(reposRoot, paths);
    }

    public void run(InputStream in, PrintStream out, PrintStream err) throws SVNException {
        run(out, err);
    }

    public void handleAdminEvent(SVNAdminEvent event, double progress) throws SVNException {
        if (event != null) {
            if (event.getAction() == SVNAdminEventAction.UNLOCKED || event.getAction() == SVNAdminEventAction.NOT_LOCKED || 
                    event.getAction() == SVNAdminEventAction.UNLOCK_FAILED) {
                SVNCommand.println(myOut, event.getMessage());
            } 
        }
    }

    public void checkCancelled() throws SVNCancelException {
    }

    public void handleEvent(SVNEvent event, double progress) throws SVNException {
    }

}
