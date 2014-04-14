/*
 * TestServer.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.
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
 * The Initial Developer of the Original Code is: cstevens.
 * Portions created by cstevens are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): cstevens, suhler.
 *
 * Version:  2.1
 * Created by cstevens on 99/11/09
 * Last modified by suhler on 02/10/01 16:37:35
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:37:35 (suhler)
 *   version change
 *
 * 1.3 02/07/18-15:04:59 (suhler)
 *   add docs
 *
 * 1.2 00/04/24-13:05:07 (cstevens)
 *   wildcard imports
 *
 * 1.2 99/11/09-20:22:05 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 tests/TestServer.java
 *
 * 1.1 99/11/09-20:22:04 (cstevens)
 *   date and time created 99/11/09 20:22:04 by cstevens
 *
 */

package tests;

import sunlabs.brazil.server.Server;

import java.net.ServerSocket;
import java.util.Properties;

/**
 * Version of Server for testing purposes.  Arranges for
 * all output to be captured in one place so the ouput of a test
 * may be compared with the expected output.
 */

public class TestServer
    extends Server
{
    StringBuffer log = new StringBuffer("\n");
    
    public
    TestServer(ServerSocket listen, String handler, Properties props)
    {
	super(listen, handler, props);
    }

    public void
    log(int level, Object obj, String message)
    {
	log.append(Thread.currentThread().getName() + ": ");
	if (obj != null) {
	    log.append(obj).append(": ");
	}
	log.append(message).append('\n');
    }

    public String
    toString()
    {
	return log.toString();
    }
}

    
    
