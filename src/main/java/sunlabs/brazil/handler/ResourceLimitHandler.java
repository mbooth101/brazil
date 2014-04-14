/*
 * ResourceLimitHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2002-2006 Sun Microsystems, Inc.
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
 * Version:  2.3
 * Created by suhler on 02/04/26
 * Last modified by suhler on 06/11/13 15:05:24
 *
 * Version Histories:
 *
 * 2.3 06/11/13-15:05:24 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.2 04/11/30-15:19:43 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:37:33 (suhler)
 *   version change
 *
 * 1.2 02/04/26-15:44:52 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/ResourceLimitHandler.java
 *
 * 1.1 02/04/26-15:44:51 (suhler)
 *   date and time created 02/04/26 15:44:51 by suhler
 *
 */

package sunlabs.brazil.handler;

import sunlabs.brazil.handler.ResourceHandler;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.MatchString;
import java.io.IOException;

/**
 * Handler for server resource management.
 * This handler monitors various system load parameters, and
 * rejects each request with a short message if any resource
 * limit is exceeded.  The properties are evaluated at init time, 
 * to minimize the per-request overhead of this monitor.
 * <p>
 * Properties:
 * <dl class=props>
 * <dt>memory	<dd>The minimum # of remaining bytes available to the vm
 * <dt>threads  <dd>The Max number of active threads
 * <dt>file  <dd> The file name or resource of the html file
 *		to return if resources run low.  Defaults to
 *		"busy.html".
 * <dt>retry <dd>The number of seconds to request the client wait
 *		 before retrying the request.
 * </dl>
 *
 * @author      Stephen Uhler
 * @version		2.3
 */

public class ResourceLimitHandler implements Handler {
    MatchString isMine;         // check for matching url
    long bytes=0;		// minimum bytes left; 0-> don't check
    int threads=0;		// max threads used
    String message;		// the message to return
    String retry;		// number of seconds.

    public boolean
    init(Server server, String prefix) {
	isMine = new MatchString(prefix, server.props);
	retry = server.props.getProperty(prefix + "retry");
	try {
	    String str = server.props.getProperty(prefix + "memory");
	    bytes = Long.parseLong(str);
	} catch (Exception e) {}
	try {
	    String str = server.props.getProperty(prefix + "threads");
	    threads = Integer.parseInt(str);
	} catch (Exception e) {}
	try {
	    String str = server.props.getProperty(prefix + "file", "busy.html");
            message = ResourceHandler.getResourceString(
			server.props, prefix, str);
	} catch (IOException e) {
	    message="<title>Busy</title><h1>Busy, please try later</h1>";
	}
	return true;
    }

    public boolean
    respond(Request request) throws IOException {
        if (!isMine.match(request.url)) {
	    return false;
        }
	/*
        System.out.println("Free=" + Runtime.getRuntime().freeMemory() +
	  "/" + bytes + " threads=" +  Thread.activeCount() + "/" +
	  threads);
	*/
        if ((bytes > 0 && Runtime.getRuntime().freeMemory() < bytes) ||
            (threads > 0 && Thread.activeCount() > threads)) {
	    if (retry != null) {
	        request.addHeader("Retry-After", retry);
	    }
	    request.log(Server.LOG_LOG, isMine.prefix(), 
		"Resource limits exceeded");
	    request.sendResponse(message, "text/html", 503);
	    return true;
        } else {
	    return false;
        }
    }
}
