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
package org.tmatesoft.svn.core.internal.io.fs;

import org.tmatesoft.svn.core.SVNNodeKind;

/**
 * @version 1.1.1
 * @author  TMate Software Ltd.
 */
public class FSEntry implements Comparable {

    private FSID myId;
    private SVNNodeKind myType;
    private String myName;

    public FSEntry() {
    }

    public FSEntry(FSID id, SVNNodeKind type, String name) {
        myId = id;
        myType = type;
        myName = name;
    }

    public void setId(FSID id) {
        myId = id;
    }

    public void setType(SVNNodeKind type) {
        myType = type;
    }

    public void setName(String name) {
        myName = name;
    }

    public FSID getId() {
        return myId;
    }

    public SVNNodeKind getType() {
        return myType;
    }

    public String getName() {
        return myName;
    }

    public String toString() {
        return myType + " " + myId;
    }

    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (obj == null || obj.getClass() != FSEntry.class) {
            return 1;
        }
        FSEntry entry = (FSEntry) obj;
        return myName.toLowerCase().compareTo(entry.myName.toLowerCase());
    }
    
    
}
