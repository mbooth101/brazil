/*
 * ServerProcess.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2004 Sun Microsystems, Inc.
 *
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version 
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is included as the file "license.terms",
 * and also available at http://www.sun.com/
 * 
 * The Original Code is from:
 *    Brazil project web application toolkit release 2.3.
 * The Initial Developer of the Original Code is: suhler.
 * Portions created by suhler are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): suhler.
 *
 * Version:  2.2
 * Created by suhler on 99/11/15
 * Last modified by suhler on 04/11/30 15:19:43
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:43 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:37:29 (suhler)
 *   version change
 *
 * 1.3 00/12/11-13:32:47 (suhler)
 *   doc typo
 *
 * 1.2 00/10/05-09:03:17 (suhler)
 *   added docs
 *
 * 1.2 99/11/15-16:28:21 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/ServerProcess.java
 *
 * 1.1 99/11/15-16:28:20 (suhler)
 *   date and time created 99/11/15 16:28:20 by suhler
 *
 */

package com.sun.server;

/**
 * This is a wrapper around the native code used by the Java Web Server
 * to set the effective user and group id's on Solaris.
 * It expects the "server.so" file used by the
 * <a href=http://www.sun.com/software/jwebserver>Java Webserver 2.0</a>.
 * Make sure you rename the file <code>server.so</code>
 * in the distribution to lib<code>com_sun_server_ServerProcess.so</code>, and
 * put it where it will be found by <b>System.loadLibrary</b>.
 *
 * @author		Colin Stevens
 * @version		2.2
 */

public class ServerProcess {
    static {
        System.loadLibrary("com_sun_server_ServerProcess");
    }

    public native static boolean setUser(String userName);
    public native static boolean setGroup(String groupName);
}
