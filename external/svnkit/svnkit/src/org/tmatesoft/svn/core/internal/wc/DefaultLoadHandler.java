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
package org.tmatesoft.svn.core.internal.wc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNRevisionProperty;
import org.tmatesoft.svn.core.internal.delta.SVNDeltaReader;
import org.tmatesoft.svn.core.internal.io.fs.FSCommitter;
import org.tmatesoft.svn.core.internal.io.fs.FSDeltaConsumer;
import org.tmatesoft.svn.core.internal.io.fs.FSFS;
import org.tmatesoft.svn.core.internal.io.fs.FSHooks;
import org.tmatesoft.svn.core.internal.io.fs.FSRevisionNode;
import org.tmatesoft.svn.core.internal.io.fs.FSRevisionRoot;
import org.tmatesoft.svn.core.internal.io.fs.FSTransactionInfo;
import org.tmatesoft.svn.core.internal.io.fs.FSTransactionRoot;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.admin.ISVNAdminEventHandler;
import org.tmatesoft.svn.core.wc.admin.SVNAdminEvent;
import org.tmatesoft.svn.core.wc.admin.SVNAdminEventAction;
import org.tmatesoft.svn.core.wc.admin.SVNUUIDAction;
import org.tmatesoft.svn.util.SVNDebugLog;



/**
 * @version 1.1.1
 * @author  TMate Software Ltd.
 */
public class DefaultLoadHandler implements ISVNLoadHandler {
    
    private FSFS myFSFS;
    private RevisionBaton myCurrentRevisionBaton;
    private NodeBaton myCurrentNodeBaton;
    private boolean myIsUsePreCommitHook;
    private boolean myIsUsePostCommitHook;
    private Map myRevisionsMap;
    private String myParentDir;
    private SVNUUIDAction myUUIDAction;
    private SVNDeltaReader myDeltaReader;
    private SVNDeltaGenerator myDeltaGenerator;
    private ISVNAdminEventHandler myProgressHandler;
    private CharsetDecoder myDecoder;
    
    public DefaultLoadHandler(boolean usePreCommitHook, boolean usePostCommitHook, SVNUUIDAction uuidAction, String parentDir, ISVNAdminEventHandler progressHandler, CharsetDecoder decoder) {
        myProgressHandler = progressHandler;
        myIsUsePreCommitHook = usePreCommitHook;
        myIsUsePostCommitHook = usePostCommitHook;
        myUUIDAction = uuidAction;
        myParentDir = SVNPathUtil.canonicalizeAbsPath(parentDir);
        myRevisionsMap = new HashMap();
        myDecoder = decoder;
    }
    
    public void setFSFS(FSFS fsfs) {
        myFSFS = fsfs;
    }
    
    public void closeRevision() throws SVNException {
        if (myCurrentRevisionBaton != null) {
            RevisionBaton baton = myCurrentRevisionBaton;
            myCurrentRevisionBaton = null;
            
            if (baton.myRevision <= 0) {
                return;
            }
            
            long oldRevision = baton.myRevision;
            if (myIsUsePreCommitHook) {
                try {
                    FSHooks.runPreCommitHook(myFSFS.getRepositoryRoot(), baton.myTxn.getTxnId());
                } catch (SVNException svne) {
                    try {
                        FSCommitter.abortTransaction(myFSFS, baton.myTxn.getTxnId());
                    } catch (SVNException svne2) {
                        //
                    }
                    throw svne;
                }
            }
            
            long newRevision = -1;
            try {
                newRevision = baton.getCommitter().commitTxn();
            } catch (SVNException svne) {
                try {
                    FSCommitter.abortTransaction(myFSFS, baton.myTxn.getTxnId());
                } catch (SVNException svne2) {
                    //
                }
                throw svne;
            }
            if (baton.myDatestamp == null) {
                myFSFS.setRevisionProperty(baton.myRevision, SVNRevisionProperty.DATE, null);
            }
            
            if (myIsUsePostCommitHook) {
                try {
                    FSHooks.runPostCommitHook(myFSFS.getRepositoryRoot(), newRevision);
                } catch (SVNException svne) {
                    SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.REPOS_POST_COMMIT_HOOK_FAILED, "Commit succeeded, but post-commit hook failed");
                    SVNErrorManager.error(err, svne);
                }
            }
            
            myRevisionsMap.put(new Long(oldRevision), new Long(newRevision));
            if (baton.myDatestamp != null) {
                myFSFS.setRevisionProperty(newRevision, SVNRevisionProperty.DATE, baton.myDatestamp);
            }
            
            String message;
            if (newRevision == baton.myRevision) {
                message = "\n------- Committed revision " + newRevision + " >>>";
            } else {
                message = "\n------- Committed new rev " + newRevision + " (loaded from original rev " + baton.myRevision + ") >>>";
            }
            if (myProgressHandler != null) {
                SVNAdminEvent event = new SVNAdminEvent(newRevision, baton.myRevision, SVNAdminEventAction.REVISION_LOADED, message); 
                myProgressHandler.handleAdminEvent(event, ISVNEventHandler.UNKNOWN);
            }
        }
    }

    public void openRevision(Map headers) throws SVNException {
        myCurrentRevisionBaton = new RevisionBaton();
        long revision = -1;
        if (headers.containsKey(SVNAdminHelper.DUMPFILE_REVISION_NUMBER)) {
            try {
                revision = Long.parseLong((String) headers.get(SVNAdminHelper.DUMPFILE_REVISION_NUMBER)); 
            } catch (NumberFormatException nfe) {
                SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.STREAM_MALFORMED_DATA, "Cannot parse revision ({0}) in dump file", headers.get(SVNAdminHelper.DUMPFILE_REVISION_NUMBER));
                SVNErrorManager.error(err);
            }
        }
        
        myCurrentRevisionBaton.myRevision = revision;
        long headRevision = myFSFS.getYoungestRevision();
        myCurrentRevisionBaton.myRevisionOffset = revision - (headRevision + 1);
        
        if (revision > 0) {
            myCurrentRevisionBaton.myTxn = FSTransactionRoot.beginTransaction(headRevision, 0, myFSFS);
            myCurrentRevisionBaton.myTxnRoot = myFSFS.createTransactionRoot(myCurrentRevisionBaton.myTxn.getTxnId());
            String message = "<<< Started new transaction, based on original revision " + revision;
            if (myProgressHandler != null) {
                SVNAdminEvent event = new SVNAdminEvent(revision, SVNAdminEventAction.REVISION_LOAD, message); 
                myProgressHandler.handleAdminEvent(event, ISVNEventHandler.UNKNOWN);
            }
        }
    }

    public void openNode(Map headers) throws SVNException {
        if (myCurrentRevisionBaton.myRevision == 0) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.STREAM_MALFORMED_DATA, "Malformed dumpstream: Revision 0 must not contain node records");
            SVNErrorManager.error(err);
        }
        
        myCurrentNodeBaton = createNodeBaton(headers);
        String message;
        switch (myCurrentNodeBaton.myAction) {
            case SVNAdminHelper.NODE_ACTION_CHANGE:
                message = "     * editing path : " + myCurrentNodeBaton.myPath + " ...";
                if (myProgressHandler != null) {
                    SVNAdminEvent event = new SVNAdminEvent(SVNAdminEventAction.REVISION_LOAD_EDIT_PATH, myCurrentNodeBaton.myPath, message); 
                    myProgressHandler.handleAdminEvent(event, ISVNEventHandler.UNKNOWN);
                }
                break;
            case SVNAdminHelper.NODE_ACTION_DELETE:
                message = "     * deleting path : " + myCurrentNodeBaton.myPath + " ...";
                if (myProgressHandler != null) {
                    SVNAdminEvent event = new SVNAdminEvent(SVNAdminEventAction.REVISION_LOAD_DELETE_PATH, myCurrentNodeBaton.myPath, message); 
                    myProgressHandler.handleAdminEvent(event, ISVNEventHandler.UNKNOWN);
                }
                myCurrentRevisionBaton.getCommitter().deleteNode(myCurrentNodeBaton.myPath);
                break;
            case SVNAdminHelper.NODE_ACTION_ADD:
                message = "     * adding path : " + myCurrentNodeBaton.myPath + " ...";
                if (myProgressHandler != null) {
                    SVNAdminEvent event = new SVNAdminEvent(SVNAdminEventAction.REVISION_LOAD_ADD_PATH, myCurrentNodeBaton.myPath, message); 
                    myProgressHandler.handleAdminEvent(event, ISVNEventHandler.UNKNOWN);
                }
                maybeAddWithHistory(myCurrentNodeBaton);
                break;
            case SVNAdminHelper.NODE_ACTION_REPLACE:
                message = "     * replacing path : " + myCurrentNodeBaton.myPath + " ...";
                if (myProgressHandler != null) {
                    SVNAdminEvent event = new SVNAdminEvent(SVNAdminEventAction.REVISION_LOAD_REPLACE_PATH, myCurrentNodeBaton.myPath, message); 
                    myProgressHandler.handleAdminEvent(event, ISVNEventHandler.UNKNOWN);
                }
                myCurrentRevisionBaton.getCommitter().deleteNode(myCurrentNodeBaton.myPath);
                maybeAddWithHistory(myCurrentNodeBaton);
                break;
            default:
                SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.STREAM_UNRECOGNIZED_DATA, "Unrecognized node-action on node ''{0}''", myCurrentNodeBaton.myPath);
                SVNErrorManager.error(err);
        }
    }

    public void parseUUID(String uuid) throws SVNException {
        if (myUUIDAction == SVNUUIDAction.IGNORE_UUID) {
            return;
        }
        
        if (myUUIDAction != SVNUUIDAction.FORCE_UUID) {
            long latestRevision = myFSFS.getYoungestRevision();
            if (latestRevision != 0) {
                return;
            }
        }

        myFSFS.setUUID(uuid);
    }

    public void closeNode() throws SVNException {
        myCurrentNodeBaton = null;
        SVNDebugLog.getDefaultLog().info(" done.\n");
    }

    public void applyTextDelta() throws SVNException {
        FSDeltaConsumer fsConsumer = myCurrentRevisionBaton.getConsumer();
        fsConsumer.applyTextDelta(myCurrentNodeBaton.myPath, null);
    }

    public void setFullText() throws SVNException {
        FSDeltaConsumer fsConsumer = myCurrentRevisionBaton.getConsumer();
        fsConsumer.applyText(myCurrentNodeBaton.myPath);
    }

    public void parseTextBlock(InputStream dumpStream, int contentLength, boolean isDelta) throws SVNException {
        FSDeltaConsumer fsConsumer = myCurrentRevisionBaton.getConsumer();

        try {
            if (isDelta) {
                applyTextDelta();
            } else {
                setFullText();
            }
            
            byte[] buffer = null;
            if (contentLength == 0) {
                getDeltaGenerator().sendDelta(myCurrentNodeBaton.myPath, SVNFileUtil.DUMMY_IN, fsConsumer, false);
            } else {
                buffer = new byte[SVNAdminHelper.STREAM_CHUNK_SIZE];
                try {
                    while (contentLength > 0) {
                        int numToRead = contentLength > SVNAdminHelper.STREAM_CHUNK_SIZE ? SVNAdminHelper.STREAM_CHUNK_SIZE : contentLength;
                        int numRead = dumpStream.read(buffer, 0, numToRead);
                        
                        if (numRead != numToRead) {
                            SVNAdminHelper.generateIncompleteDataError();
                        }
                        
                        if (isDelta) {
                            SVNDeltaReader deltaReader = getDeltaReader();
                            deltaReader.nextWindow(buffer, 0, numRead, myCurrentNodeBaton.myPath, fsConsumer);
                        } else {
                            getDeltaGenerator().sendDelta(myCurrentNodeBaton.myPath, buffer, numRead, fsConsumer);
                        }
                        contentLength -= numRead;
                    }
                } catch (IOException ioe) {
                    SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, ioe.getLocalizedMessage());
                    SVNErrorManager.error(err, ioe);
                }

                fsConsumer.textDeltaEnd(myCurrentNodeBaton.myPath);
            }
        } catch (SVNException svne) {
            fsConsumer.abort(); 
        }
    }
    
    public int parsePropertyBlock(InputStream dumpStream, int contentLength, boolean isNode) throws SVNException {
        int actualLength = 0;
        StringBuffer buffer = new StringBuffer();
        String line = null;
        
        try {
            while (contentLength != actualLength) {
                buffer.setLength(0);
                line = SVNFileUtil.readLineFromStream(dumpStream, buffer, myDecoder);
                
                if (line == null) {
                    SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.STREAM_MALFORMED_DATA, "Incomplete or unterminated property block");
                    SVNErrorManager.error(err);
                }
                
                //including '\n'
                actualLength += line.length() + 1;
                if ("PROPS-END".equals(line)) {
                    break;
                } else if (line.charAt(0) == 'K' && line.charAt(1) == ' ') {
                    int len = 0;
                    try {
                        len = Integer.parseInt(line.substring(2));    
                    } catch (NumberFormatException nfe) {
                        SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.STREAM_MALFORMED_DATA, "Malformed dumpfile header: can't parse node property key length");
                        SVNErrorManager.error(err, nfe);
                    }
                    
                    byte[] buff = new byte[len];
                    actualLength += SVNAdminHelper.readKeyOrValue(dumpStream, buff, len);
                    String propName = new String(buff, "UTF-8");
                    
                    buffer.setLength(0);
                    line = SVNFileUtil.readLineFromStream(dumpStream, buffer, myDecoder);
                    if (line == null) {
                        SVNAdminHelper.generateIncompleteDataError();
                    }
                    
                    //including '\n'
                    actualLength += line.length() + 1;
                    if (line.charAt(0) == 'V' && line.charAt(1) == ' ') {
                        try {
                            len = Integer.parseInt(line.substring(2));    
                        } catch (NumberFormatException nfe) {
                            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.STREAM_MALFORMED_DATA, "Malformed dumpfile header: can't parse node property value length");
                            SVNErrorManager.error(err, nfe);
                        }
    
                        buff = new byte[len];
                        actualLength += SVNAdminHelper.readKeyOrValue(dumpStream, buff, len);
                        String propValue = new String(buff, "UTF-8");
                        if (isNode) {
                            setNodeProperty(propName, propValue);
                        } else {
                            setRevisionProperty(propName, propValue);
                        }
                    } else {
                        SVNAdminHelper.generateStreamMalformedError();
                    }
                } else if (line.charAt(0) == 'D' && line.charAt(1) == ' ') {
                    int len = 0;
                    try {
                        len = Integer.parseInt(line.substring(2));    
                    } catch (NumberFormatException nfe) {
                        SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.STREAM_MALFORMED_DATA, "Malformed dumpfile header: can't parse node property key length");
                        SVNErrorManager.error(err, nfe);
                    }
                    
                    byte[] buff = new byte[len];
                    actualLength += SVNAdminHelper.readKeyOrValue(dumpStream, buff, len);
                    
                    if (!isNode) {
                        SVNAdminHelper.generateStreamMalformedError();
                    }
                    
                    String propName = new String(buff, "UTF-8");
                    setNodeProperty(propName, null);
                } else {
                    SVNAdminHelper.generateStreamMalformedError();
                }
            }
        } catch (IOException ioe) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, ioe.getLocalizedMessage());
            SVNErrorManager.error(err, ioe);
        }
        
        return actualLength;
    }

    public void removeNodeProperties() throws SVNException {
        FSTransactionRoot txnRoot = myCurrentRevisionBaton.myTxnRoot;
        FSRevisionNode node = txnRoot.getRevisionNode(myCurrentNodeBaton.myPath);
        Map props = node.getProperties(myFSFS);
        
        for (Iterator propNames = props.keySet().iterator(); propNames.hasNext();) {
            String propName = (String) propNames.next();
            myCurrentRevisionBaton.getCommitter().changeNodeProperty(myCurrentNodeBaton.myPath, propName, null);
        }
    }

    public void setNodeProperty(String propertyName, String propertyValue) throws SVNException {
        myCurrentRevisionBaton.getCommitter().changeNodeProperty(myCurrentNodeBaton.myPath, propertyName, propertyValue);
    }

    public void setRevisionProperty(String propertyName, String propertyValue) throws SVNException {
        if (myCurrentRevisionBaton.myRevision > 0) {
            myFSFS.setTransactionProperty(myCurrentRevisionBaton.myTxn.getTxnId(), propertyName, propertyValue);
            if (SVNRevisionProperty.DATE.equals(propertyName)) {
                myCurrentRevisionBaton.myDatestamp = propertyValue;
            }
        } else if (myCurrentRevisionBaton.myRevision == 0) {
            long youngestRevision = myFSFS.getYoungestRevision();
            if (youngestRevision == 0) {
                myFSFS.setRevisionProperty(0, propertyName, propertyValue);
            }
        }
    }

    public void setUsePreCommitHook(boolean use) {
        myIsUsePreCommitHook = use;
    }
    
    public void setUsePostCommitHook(boolean use) {
        myIsUsePostCommitHook = use;
    }
    
    public void setParentDir(String parentDir) {
        myParentDir = parentDir;
    }

    public void setUUIDAction(SVNUUIDAction action) {
        myUUIDAction = action;
    }
    
    private SVNDeltaReader getDeltaReader() {
        if (myDeltaReader == null) {
            myDeltaReader = new SVNDeltaReader();
        } 
        return myDeltaReader;
    }

    private SVNDeltaGenerator getDeltaGenerator() {
        if (myDeltaGenerator == null) {
            myDeltaGenerator = new SVNDeltaGenerator();
        }
        return myDeltaGenerator;
    }

    private void maybeAddWithHistory(NodeBaton nodeBaton) throws SVNException {
        if (nodeBaton.myCopyFromPath == null) {
            if (nodeBaton.myKind == SVNNodeKind.FILE) {
                myCurrentRevisionBaton.getCommitter().makeFile(nodeBaton.myPath);
            } else if (nodeBaton.myKind == SVNNodeKind.DIR) {
                myCurrentRevisionBaton.getCommitter().makeDir(nodeBaton.myPath);
            }
        } else {
            long srcRevision = nodeBaton.myCopyFromRevision - myCurrentRevisionBaton.myRevisionOffset;
            Long copyFromRevision = new Long(nodeBaton.myCopyFromRevision);
            
            if (myRevisionsMap.containsKey(copyFromRevision)) {
                Long revision = (Long) myRevisionsMap.get(copyFromRevision);
                srcRevision = revision.longValue();
            }
            
            if (!SVNRevision.isValidRevisionNumber(srcRevision)) {
                SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.FS_NO_SUCH_REVISION, "Relative source revision {0,number,integer} is not available in current repository", new Long(srcRevision));
                SVNErrorManager.error(err);
            }
            
            FSRevisionRoot copyRoot = myFSFS.createRevisionRoot(srcRevision);
            myCurrentRevisionBaton.getCommitter().makeCopy(copyRoot, nodeBaton.myCopyFromPath, nodeBaton.myPath, true);
            SVNDebugLog.getDefaultLog().info("COPIED...");
        }
    }
    
    private NodeBaton createNodeBaton(Map headers) throws SVNException {
        NodeBaton baton = new NodeBaton();
        baton.myKind = SVNNodeKind.UNKNOWN;
        if (headers.containsKey(SVNAdminHelper.DUMPFILE_NODE_PATH)) {
            String nodePath = (String) headers.get(SVNAdminHelper.DUMPFILE_NODE_PATH); 
            if (myParentDir != null) {
                baton.myPath = SVNPathUtil.concatToAbs(myParentDir, nodePath.startsWith("/") ? nodePath.substring(1) : nodePath);
            } else {
                baton.myPath = SVNPathUtil.canonicalizeAbsPath(nodePath);
            }
        }
        
        if (headers.containsKey(SVNAdminHelper.DUMPFILE_NODE_KIND)) {
            baton.myKind = SVNNodeKind.parseKind((String) headers.get(SVNAdminHelper.DUMPFILE_NODE_KIND));
        }
        
        baton.myAction = SVNAdminHelper.NODE_ACTION_UNKNOWN;
        if (headers.containsKey(SVNAdminHelper.DUMPFILE_NODE_ACTION)) {
            String action = (String) headers.get(SVNAdminHelper.DUMPFILE_NODE_ACTION);
            if ("change".equals(action)) {
                baton.myAction = SVNAdminHelper.NODE_ACTION_CHANGE;
            } else if ("add".equals(action)) {
                baton.myAction = SVNAdminHelper.NODE_ACTION_ADD;
            } else if ("delete".equals(action)) {
                baton.myAction = SVNAdminHelper.NODE_ACTION_DELETE;
            } else if ("replace".equals(action)) {
                baton.myAction = SVNAdminHelper.NODE_ACTION_REPLACE;
            }
        }
        
        baton.myCopyFromRevision = -1;
        if (headers.containsKey(SVNAdminHelper.DUMPFILE_NODE_COPYFROM_REVISION)) {
            try {
                baton.myCopyFromRevision = Long.parseLong((String) headers.get(SVNAdminHelper.DUMPFILE_NODE_COPYFROM_REVISION)); 
            } catch (NumberFormatException nfe) {
                SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.STREAM_MALFORMED_DATA, "Cannot parse revision ({0}) in dump file", headers.get(SVNAdminHelper.DUMPFILE_NODE_COPYFROM_REVISION));
                SVNErrorManager.error(err);
            }
        }
        
        if (headers.containsKey(SVNAdminHelper.DUMPFILE_NODE_COPYFROM_PATH)) {
            String copyFromPath = (String) headers.get(SVNAdminHelper.DUMPFILE_NODE_COPYFROM_PATH);
            if (myParentDir != null) {
                baton.myCopyFromPath = SVNPathUtil.concatToAbs(myParentDir, copyFromPath.startsWith("/") ? copyFromPath.substring(1) : copyFromPath);
            } else {
                baton.myCopyFromPath = SVNPathUtil.canonicalizeAbsPath(copyFromPath);
            }
        }
        
        if (headers.containsKey(SVNAdminHelper.DUMPFILE_TEXT_CONTENT_LENGTH)) {
            baton.myTextChecksum = (String) headers.get(SVNAdminHelper.DUMPFILE_TEXT_CONTENT_LENGTH);
        }        
        return baton;
    }
    
    private class RevisionBaton {
        FSTransactionInfo myTxn;
        FSTransactionRoot myTxnRoot;
        long myRevision;
        long myRevisionOffset;
        String myDatestamp;
        
        private FSCommitter myCommitter;
        private FSDeltaConsumer myDeltaConsumer;
        
        public FSDeltaConsumer getConsumer() {
            if (myDeltaConsumer == null) {
                myDeltaConsumer = new FSDeltaConsumer("", myTxnRoot, myFSFS, getCommitter(), null, null);
            }
            return myDeltaConsumer;
        }
        
        public FSCommitter getCommitter() {
            if (myCommitter == null) {
                myCommitter = new FSCommitter(myFSFS, myTxnRoot, myTxn, null, null);
            }
            return myCommitter;
        }
    }
    
    private class NodeBaton {
        String myPath;
        SVNNodeKind myKind;
        int myAction;
        long myCopyFromRevision;
        String myCopyFromPath;
        String myTextChecksum;
    }
}
