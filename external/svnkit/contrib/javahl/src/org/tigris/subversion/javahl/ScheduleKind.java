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
 * The schedule states an entry can be in.
 */
public interface ScheduleKind
{
    /** exists, but uninteresting */
    public static final int normal = 0;

    /** Slated for addition */
    public static final int add = 1;

    /** Slated for deletion */
    public static final int delete = 2;

    /** Slated for replacement (delete + add) */
    public static final int replace = 3;
}
