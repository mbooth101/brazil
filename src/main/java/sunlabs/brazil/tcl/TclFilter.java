/*
 * TclFilter.java
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
 * Contributor(s): cstevens, suhler.
 *
 * Version:  2.2
 * Created by suhler on 99/08/06
 * Last modified by suhler on 04/11/30 15:19:44
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:44 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:37:21 (suhler)
 *   version change
 *
 * 1.14 02/07/24-10:49:18 (suhler)
 *   doc updates
 *
 * 1.13 01/12/10-14:51:27 (suhler)
 *   doc lint
 *
 * 1.12 00/12/11-13:33:18 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.11 00/07/07-17:02:56 (suhler)
 *   remove System.out.println(s)
 *
 * 1.10 00/05/24-11:29:35 (suhler)
 *   fix content type bug
 *
 * 1.9 00/05/19-11:50:49 (suhler)
 *   doc fixes
 *
 * 1.8 00/04/24-13:03:58 (cstevens)
 *   moved to sunlabs.brazil.tcl package.
 *
 * 1.7 00/04/12-15:51:07 (cstevens)
 *   unused imports
 *
 * 1.6 00/02/11-08:57:58 (suhler)
 *   better diagnostics
 *
 * 1.5 99/10/06-12:38:29 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.3.1.3 99/10/06-12:30:10 (suhler)
 *   use mime headers
 *
 * 1.4 99/10/01-11:27:28 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.3.1.2 99/09/17-13:55:36 (suhler)
 *   removed diagnostics
 *
 * 1.3.1.1 99/09/03-11:36:04 (suhler)
 *   better arguments passed to filter proc
 *
 * 1.3 99/09/01-15:31:38 (suhler)
 *   lint
 *
 * 1.2 99/08/30-09:38:08 (suhler)
 *   fix imports
 *
 * 1.2 99/08/06-08:42:23 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 3 2 tcl/TclFilter.java
 *   Name history : 2 1 filter/TclFilter.java
 *   Name history : 1 0 tail/TclFilter.java
 *
 * 1.1 99/08/06-08:42:22 (suhler)
 *   date and time created 99/08/06 08:42:22 by suhler
 *
 */

package sunlabs.brazil.tcl;

import sunlabs.brazil.filter.Filter;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.util.http.MimeHeaders;

import tcl.lang.Interp;
import tcl.lang.ReflectObject;
import tcl.lang.TCL;
import tcl.lang.TclException;
import tcl.lang.TclObject;
import tcl.lang.TclUtil;

import java.io.File;

/**
 * Wrapper for writing FilterHandler filters in TCL.
 * Runs a tcl startup script when the handler is initialized.
 * Any time a request is made, the tcl <b>filter</b> proc
 * (which should be defined in the init script) is called,
 * provided with the
 * {@link sunlabs.brazil.server.Request} object as an argument.
 * <p>
 * This provides a bare-bones tcl interface.  The startup script
 * should provide a friendlier interface.
 * <p>
 * This handler requires the <code>tcl.jar</code>
 * jar file, a version of jacl included in the release.
 * <p>
 * The following server properties are used:
 * <dl class=props>
 * <dt>script	<dd> The name of the TCL file sourced on startup.
 *		The {@link #init} parameters are made available as the global
 *		variables <code>prefix</code> and <code>server</code>.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		2.2
 */

public class TclFilter implements Filter {
    Server server;
    String propsPrefix;
    String scriptName;

    static final String SCRIPT = "script";

    /**
     * extract the filter properties.
     */

    public boolean
    init(Server server, String prefix) {
    	this.server = server;
    	propsPrefix = prefix;
	// System.out.println("initing (" + prefix + ")");
	scriptName = server.props.getProperty(prefix + SCRIPT,prefix + "tcl");
	File scriptFile = new File(scriptName);
	if (!scriptFile.isAbsolute()) {
	    scriptFile = new File(server.props.getProperty(FileHandler.ROOT,"."), scriptName);
	}
	scriptName = scriptFile.getAbsolutePath();
	server.log(Server.LOG_DIAGNOSTIC, prefix, "Using: " + scriptName);
	return true;
    }

    /**
     * We don't need to look at the request.
     */

    public boolean
    respond(Request request) {
	return false;
    }

    /**
     * For now, only filter text/html.  This restriction will be
     * lifted once I figure out how
     */

    public boolean
    shouldFilter(Request request, MimeHeaders headers) {
	String type = headers.get("content-type");
	return (type != null && type.startsWith("text/html"));
    }

    /*
     * Find (or create) the interpreter.
     * Call the tcl callback script.
     * The <code>request</code> object reference is appended to the
     * <code>callback</code> parameter.
     * @return	true, if the callback script returns "true" or "1".
     */

    public byte[]
    filter(Request request, MimeHeaders headers, byte[] content) {

	/*
	 * Find the proper interp, creating it if needed.  If newly created
	 * (or if no SessionID variable was found) - initialize the interp
	 */

	String sessionId = request.props.getProperty("SessionID","common");
	request.log(Server.LOG_DIAGNOSTIC, "  Using session: " + sessionId);
	Interp interp = (Interp) SessionManager.getSession(sessionId,
		propsPrefix + "TCL", Interp.class);

    	int code = 0;
    	synchronized (interp) {
	    setupInterp(interp, sessionId);
	    try {
		// System.out.println("running eval");
		TclUtil.setVar(interp, "content", new String(content),
			TCL.GLOBAL_ONLY);
		interp.eval("filter" + " " +
		    ReflectObject.newInstance(interp, Request.class, request) +
		    " " + ReflectObject.newInstance(interp, MimeHeaders.class,
			headers)
		    );
		TclObject res = interp.getVar("content", TCL.GLOBAL_ONLY);
		content =  res.toString().getBytes();
	    } catch (TclException e) {
		code = e.getCompletionCode();
		String trace = e.toString();
		// System.out.println("Tcl Oops: "  + code + " " + e);
		if (code == 1) {
		    try {
			trace = interp.getVar("errorInfo", TCL.GLOBAL_ONLY).toString();
		    } catch (Exception e1) {}
		}
		request.log(Server.LOG_WARNING, propsPrefix + trace);
	    }
	}  // end sync block
	return content;
    }

    /**
     * Setup a tcl interpreter for this session
     */

    private void
    setupInterp(Interp interp, String id) {
	try {
	    interp.getVar("SessionID", TCL.GLOBAL_ONLY);
	    return;
	} catch (TclException e) {
	    // System.out.println("Creating new TCL interp");
	}
	try {
	    TclUtil.setVar(interp, "tcl_interactive", "0", TCL.GLOBAL_ONLY);
	    TclUtil.setVar(interp, "argv0", scriptName, TCL.GLOBAL_ONLY);
	    TclUtil.setVar(interp, "prefix", propsPrefix, TCL.GLOBAL_ONLY);
	    TclUtil.setVar(interp, "SessionID", id, TCL.GLOBAL_ONLY);
	    TclUtil.setVar(interp, "server",
		    ReflectObject.newInstance(interp, Server.class, server), 
		    TCL.GLOBAL_ONLY);
	    // System.out.println("About to run: " + scriptName);
	    interp.evalFile(scriptName);
	    // System.out.println("DONE");
	} catch (TclException e) {
	    int code = e.getCompletionCode();
	    String trace = e.toString();
	    if (code == 1) {
		try {
		    trace = interp.getVar("errorInfo",
			    TCL.GLOBAL_ONLY).toString();
		} catch (Exception e1) {}
	    }
        server.log(Server.LOG_WARNING, null, trace);
	}
    return;
    }
}
