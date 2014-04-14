/*
 * RestartingMultiHostHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2006-2007 Sun Microsystems, Inc.
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
 * Version:  1.2
 * Created by suhler on 06/01/06
 * Last modified by suhler on 07/01/29 15:36:32
 *
 * Version Histories:
 *
 * 1.2 07/01/29-15:36:32 (suhler)
 *   remove unused imports
 *
 * 1.2 06/01/06-13:47:04 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/RestartingMultiHostHandler.java
 *
 * 1.1 06/01/06-13:47:03 (suhler)
 *   date and time created 06/01/06 13:47:03 by suhler
 *
 */

package sunlabs.brazil.handler;

import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

/**
 * Allow the configuration for one virtual host to be restarted.
 */

public class RestartingMultiHostHandler extends MultiHostHandler {

    /**
     * See if the server needs to be restarted.  If so, create it, 
     * reinitialize it, and return it.
     * this causes the sub-server to be restarted.  Any pending requests
     * continue to use the prior configuration.
     * This fuctionallity probably doesn't belong here,
     * but I haven't yet figured out where else it should go.
     *
     * @param server		the sub-server for this host
     * @param request		The original request
     */

    protected Server doServer(Request request, Server server) {
	String prefix = server.prefix;
	if (prefix.endsWith(".")) {
	    prefix = prefix.substring(0, prefix.length()-1);
        }
	String restart = server.props.getProperty(prefix + ".restart");
	if (restart != null && restart.equals(request.url)) {
            Server sub = subServer(request.server, prefix);
	    if (sub!=null && sub.init()) {
	        return sub;
	    } 
	} 
        return server;
    }
}
