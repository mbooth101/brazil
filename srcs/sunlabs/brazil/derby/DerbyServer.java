/*
 * DerbyServer.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2005 Sun Microsystems, Inc.
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
 * Version:  1.1
 * Created by suhler on 05/05/13
 * Last modified by suhler on 05/05/13 10:54:15
 *
 * Version Histories:
 *
 * 1.2 05/05/13-10:54:16 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 derby/DerbyServer.java
 *
 * 1.1 05/05/13-10:54:15 (suhler)
 *   date and time created 05/05/13 10:54:15 by suhler
 *
 */

package sunlabs.brazil.derby;

import org.apache.derby.drda.NetworkServerControl;
import sunlabs.brazil.server.*;
import java.net.InetAddress;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Template for starting derby in network server mode.
 * <dl class=props>
 * <dt>port
 * <dd>The port the server will listen on (defaults to 1527)
 * <dt>log
 * <dd>If true, log connections
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		@(#)DerbyServer.java	1.1
 */

public class DerbyServer implements Handler {
    public boolean
    init(Server server, String prefix) {
	String portStr = server.props.getProperty(prefix + "port");
	String host = server.props.getProperty(prefix + "host", "localhost");
	int port = 1527;
	try {
	    port = Integer.decode(portStr).intValue();
	} catch (Exception e) {}

	try {
	    NetworkServerControl nsc =
	        new NetworkServerControl(InetAddress.getByName(host), port);
	    nsc.start(new PrintWriter(System.err));
	    for(int i=0;i<10;i++) {
		System.out.println("Starting...");
		if (isReady(nsc)) break;
	        Thread.sleep(200);
	    }
	    nsc.logConnections(server.logLevel >= Server.LOG_LOG);
	} catch (Exception e) {
	    System.err.println("Can't start Derby network server: " + e);
	    return false;
	}
	return true;
    }

    public static boolean isReady(NetworkServerControl nsc) {
	try {
	    nsc.ping();
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }


    public boolean
    respond(Request request) throws IOException {
	return false;
    }
}
