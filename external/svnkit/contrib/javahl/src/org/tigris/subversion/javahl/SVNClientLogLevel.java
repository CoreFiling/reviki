/**
 * @copyright
 * ====================================================================
 * Copyright (c) 2003-2004 CollabNet.  All rights reserved.
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
package org.tigris.subversion.javahl;
/**
 * class for the constants of the logging levels.
 */
public interface SVNClientLogLevel
{
    /**
     * Log nothing
     */
    public static final int NoLog = 0;
    /**
     * Log fatal error
     */
    public static final int ErrorLog = 1;
    /**
     * Log exceptions thrown
     */
    public static final int ExceptionLog = 2;
    /**
     * Log the entry and exits of the JNI code
     */
    public static final int EntryLog = 3;
}
