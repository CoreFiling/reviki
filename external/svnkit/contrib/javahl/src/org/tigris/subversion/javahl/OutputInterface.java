package org.tigris.subversion.javahl;

import java.io.IOException;

/**
 * @copyright
 * ====================================================================
 * Copyright (c) 2004 CollabNet.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://subversion.tigris.org/license-1.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 *
 * This software consists of voluntary contributions made by many
 * individuals.  For exact contribution history, see the revision
 * history and logs, available at http://subversion.tigris.org/.
 * ====================================================================
 * @endcopyright
 */
/**
 * interface to send data to subversion
 * used by SVNAdmin.load
 */
public interface OutputInterface
{
    /**
     * write the bytes in data to java
     * @param data          the data to be writtem
     * @throws IOException  throw in case of problems.
     */
    public int write(byte[] data) throws IOException;

    /**
     * close the output
     * @throws IOException throw in case of problems.
     */
    public void close() throws IOException;
}
