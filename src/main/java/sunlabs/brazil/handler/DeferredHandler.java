/*
 * DeferredHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.
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
 * Version:  2.1
 * Created by suhler on 00/05/12
 * Last modified by suhler on 02/10/01 16:36:38
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:36:38 (suhler)
 *   version change
 *
 * 1.4 01/08/03-18:21:54 (suhler)
 *   remove training  ws from classnames before trying to instantiate
 *
 * 1.3 00/12/11-13:28:51 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.2 00/05/22-14:04:13 (suhler)
 *   doc updates
 *
 * 1.2 00/05/12-16:18:44 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/DeferredHandler.java
 *
 * 1.1 00/05/12-16:18:43 (suhler)
 *   date and time created 00/05/12 16:18:43 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.util.StringTokenizer;
import java.io.IOException;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

/**
 * Wrap another handler, deferring its initialization until request time.
 * This allows handlers to be configured, but not initialized until
 * a first use is attempted.
 *<p>
 * Normally, when a handler's class is first resolved, if any of the
 * dependent classes are not available, an error will occur, terminating the
 * server.  Using this handler, other handlers can be conditionally configured
 * based on the availability of other specified classes at run time.
 * <p>
 * NOTE: This functionallity should be integrated into the <code>
 * ChainHandler</code>, eliminating the need for this one.
 * <p>
 * Request Properties
 * <dl class=props>
 *<dt>handler<dd>The token representing the handler to conditionally configure.
 *		 This is used as the handler's prefix
 *<dt>requires<dd>The names of classes required to be resolvable before
 *		  configuring the handler
 *<dt>[handler].prefix<dd>Used to trigger the configuration
 *<dt>[handler].class<dd>The name of the handler class.
 *</dl>
 */

public class DeferredHandler implements Handler {
    String prefix;		// our prefix
    String token;		// our handlers token
    Server server;		// the server to init the handler with
    String trigger;		// prefix to trigger initialization
    Handler handler;		// the handler to start
    boolean initialized = false;// we tried to initialize this
    boolean installed=false;	// already initialized

    /**
     * Remember the server for deferred initialization.
     */

    public boolean
    init(Server server, String prefix) {
	this.server = server;
	this.prefix = prefix;
	handler=null;
	token =  server.props.getProperty(prefix + "handler");
	return (token != null);
    }

    /**
     * Dispatch to the handler, installing it if needed
     */

    public boolean
    respond(Request request) throws IOException {
	if (installed) {
	    return handler.respond(request);
	} else if (!initialized) {
	    String urlPrefix = request.props.getProperty(token + ".prefix","/");
	    if (!request.url.startsWith(urlPrefix)) {
		return false;
	    }
	    initialized = true;
	    request.log(Server.LOG_DIAGNOSTIC,prefix,
			"Attempting to install handler");

	    /* make sure we can find all required classes */

	    StringTokenizer st = new StringTokenizer(
			request.props.getProperty(prefix + "requires", ""));
	    while (st.hasMoreTokens()) {
		String require = null;
		try {
		    require = st.nextToken();
		    request.log(Server.LOG_DIAGNOSTIC,prefix,
			"locating " + require);
		    Class.forName(require);
		} catch (Exception e) {
		    request.log(Server.LOG_WARNING,prefix,
				"Can't locate " + require);
		    request.log(Server.LOG_DIAGNOSTIC,prefix, e.toString());
		    return false;
		}
	    }

	    /* Now init (and run) the handler */

	    try {
		String name = request.props.getProperty(token + ".class");
		request.log(Server.LOG_DIAGNOSTIC,prefix,
			"Instantiating " + name);
		handler = (Handler) Class.forName(name.trim()).newInstance();
		if (handler.init(server, token + ".")) {
		    installed = true;
		    request.log(Server.LOG_DIAGNOSTIC, prefix,
				"Handler " + name + " installed");
		    return respond(request);
		} else {
		    request.log(Server.LOG_DIAGNOSTIC,prefix,
				"Handler " + name + " NOT installed");
		}
	    } catch (Exception e) {
		request.log(Server.LOG_WARNING,prefix, e.toString());
	    }
	    return false;
	} else {		// didn't install
	    return false;
	}
    }
}
