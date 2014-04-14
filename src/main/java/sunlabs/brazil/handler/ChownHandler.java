/*
 * ChownHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2004 Sun Microsystems, Inc.
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
 * Created by suhler on 00/10/02
 * Last modified by suhler on 04/11/30 15:19:42
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:42 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:37:29 (suhler)
 *   version change
 *
 * 1.8 02/07/24-10:48:51 (suhler)
 *   doc updates
 *
 * 1.7 02/06/11-17:12:14 (suhler)
 *   fixed typo GroupName -> groupName
 *
 * 1.6 00/12/11-13:32:52 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.5 00/10/31-10:20:36 (suhler)
 *   doc fixes
 *
 * 1.4 00/10/17-09:36:49 (suhler)
 *   return true on success
 *
 * 1.3 00/10/17-08:40:25 (suhler)
 *   catch unsatisfied link exceptions
 *   .
 *
 * 1.2 00/10/05-09:03:27 (suhler)
 *   added docs
 *
 * 1.2 00/10/02-17:30:25 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/ChownHandler.java
 *
 * 1.1 00/10/02-17:30:24 (suhler)
 *   date and time created 00/10/02 17:30:24 by suhler
 *
 */

package sunlabs.brazil.handler;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import java.io.IOException;
import com.sun.server.ServerProcess;

/**
 * Handler for changing the group and owner of the server.
 * This handler  expects the "server.so" file used by the
 * <a href=http://www.sun.com/software/jwebserver>
 * Java Webserver 2.0</a>
 * Make sure you rename the file lib<code>server.so</code>
 * in the distribution to lib<code>com_sun_server_ServerProcess.so</code>, and
 * put it where it will be found by <b>System.loadLibrary</b>.
 * <br>
 * Note:  If the native library is unalvailable on your platform, 
 * try the <i>RunAs</i> handler, that includes the native source code.
 * <p>
 * Properties:
 * <dl class=props>
 * <dt>userName	<dd>name of the user to run as
 * <dt>groupName  <dd>The name of the group to run as
 * </dl>
 *
 * @author      Stephen Uhler
 * @version		2.2
 */

public class ChownHandler implements Handler {

    /**
     * set up the Unix user and group.
     * We could return false, so our respond method would never be called, 
     * but some containers cause the server to exit on false returns.
     */

    public boolean
    init(Server server, String prefix) {
	String user = server.props.getProperty(prefix + "userName");
	String group = server.props.getProperty(prefix + "groupName");
	try {
	    if (user != null && ServerProcess.setUser(user)) {
		System.out.println("Setting server to run as user: " + user);
	    }
	    if (group != null && ServerProcess.setGroup(group)) {
		System.out.println("Setting server to run as group: " + group);
	    }
	} catch (UnsatisfiedLinkError e) {
	    System.out.println(prefix + " not available: " + e.getMessage());
	    return false;
	}
	return true;
    }

    /**
     * Nothing to respond to
     */

    public boolean
    respond(Request request) throws IOException {
	return false;
    }
}
