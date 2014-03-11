/*
 * TclHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2006 Sun Microsystems, Inc.
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
 * Version:  2.3
 * Created by suhler on 99/04/04
 * Last modified by suhler on 06/11/13 15:05:55
 *
 * Version Histories:
 *
 * 2.3 06/11/13-15:05:55 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.2 04/11/30-15:19:44 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:37:19 (suhler)
 *   version change
 *
 * 1.19 02/07/24-10:49:12 (suhler)
 *   doc updates
 *
 * 1.18 01/07/20-11:33:08 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.17 01/07/17-14:17:56 (suhler)
 *   use MatchUrl
 *
 * 1.16 00/12/11-13:33:06 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.15 00/05/31-13:52:09 (suhler)
 *   docs
 *
 * 1.14 00/04/24-13:03:33 (cstevens)
 *   moved to sunlabs.brazil.tcl
 *
 * 1.13 00/04/12-16:04:46 (cstevens)
 *   imports
 *
 * 1.12 99/10/21-18:26:40 (cstevens)
 *   Merged changes between child workspace "/home/cstevens/ws/brazil/naws" and
 *   parent workspace "/net/mack/export/ws/brazil/naws".
 *
 * 1.10.1.1 99/10/21-18:21:46 (cstevens)
 *   jImport
 *
 * 1.11 99/10/20-15:08:31 (suhler)
 *   fixed imports
 *
 * 1.10 99/10/11-12:37:42 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.9 99/10/07-13:01:46 (cstevens)
 *   javadoc lint
 *
 * 1.8.1.1 99/10/06-12:39:07 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.8 99/10/01-11:27:31 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.7.1.1 99/09/17-13:56:01 (suhler)
 *   changed docs
 *
 * 1.7 99/08/06-12:06:34 (suhler)
 *   removed DEBUG option, the same effect can be obtained by insetting
 *   the session ID variable
 *
 * 1.6 99/07/30-15:40:44 (suhler)
 *   Use the sessionHandler to create a different interp per session.
 *   Modify virtual -> virtual2 to test the new handler
 *
 * 1.5 99/07/19-11:59:52 (suhler)
 *   added javadoc documentation
 *
 * 1.4 99/06/28-10:54:05 (suhler)
 *   added mutex
 *
 * 1.3 99/04/20-17:22:06 (suhler)
 *   fixed url checking
 *   added "temp" tcl callable proxy method
 *
 * 1.2 99/04/05-12:01:38 (suhler)
 *   better documentation, error checking
 *
 * 1.2 99/04/04-17:54:45 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 tcl/TclHandler.java
 *   Name history : 1 0 tclHandlers/TclHandler.java
 *
 * 1.1 99/04/04-17:54:44 (suhler)
 *   date and time created 99/04/04 17:54:44 by suhler
 *
 */

package sunlabs.brazil.tcl;

import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.session.SessionManager;

import tcl.lang.Interp;
import tcl.lang.ReflectObject;
import tcl.lang.TCL;
import tcl.lang.TclException;
import tcl.lang.TclUtil;

import java.io.File;
import java.io.IOException;

/**
 * Handler for writing handlers in tcl.
 * Anytime a request is made, call the <b>respond</b> callback
 * in the tcl code, which is provided with the
 * {@link sunlabs.brazil.server.Request} object as an argument.
 * <p>
 * This provides a bare-bones tcl interface.  The startup script, which is
 * sourced once upon startup, 
 * should provide a friendlier interface.
 * <p>
 * One Tcl interpreter is started for each session.
 * The SessionID property of the request is used to choose the session.
 * if no Session ID is available, a single interpreter is used for each
 * request.
 * The interpreter is Initialized the first time it is referenced for a
 * session, or if the "SessionID" variable is NOT set.
 * <p>
 * This handler requires <code>tcl.jar</code>, a
 * version of jacl, included in the release.
 * <p>
 * The following server properties are used:
 * <dl class=props>
 * <dt>callback	<dd>The name of the TCL script to call at each request.
 *		Defaults to <code>respond</code>.
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URL that triggers this handler.
 * (See {@link MatchString}).
 * <dt>script	<dd> The name of the TCL file sourced on startup.
 *		The {@link #init} parameters a make available as the global
 *		variables <code>prefix</code> and <code>server</code>.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		2.3
 */

public class TclHandler implements Handler {
    Server server;
    String propsPrefix;
    String scriptName;
    String callback;
    MatchString isMine;            // check for matching url

    static final String SCRIPT = "script";
    static final String CALLBACK = "callback";

    /**
     * Create a tcl interp, extract the properties, and run the init script
     */

    public boolean
    init(Server server, String prefix) {
    	this.server = server;
    	propsPrefix = prefix;
	scriptName = server.props.getProperty(prefix + SCRIPT,prefix + "tcl");
	isMine = new MatchString(prefix, server.props);
	callback = server.props.getProperty(prefix + CALLBACK,"respond");
	File scriptFile = new File(scriptName);
	if (!scriptFile.isAbsolute()) {
	    scriptFile = new File(server.props.getProperty(FileHandler.ROOT,"."), scriptName);
	}
	scriptName = scriptFile.getAbsolutePath();
	server.log(Server.LOG_DIAGNOSTIC, prefix, "Using: " + scriptName);
	return true;
    }

    /*
     * Find (or create) the interpreter.
     * Call the tcl callback script.
     * The <code>request</code> object reference is appended to the
     * <code>callback</code> parameter.
     * @return	true, if the callback script returns "true" or "1".
     */

    public boolean
    respond(Request request) throws IOException {
        if (!isMine.match(request.url)) {
            return false;
	}

	/*
	 * Find the proper interp, creating it if needed.  If newly created
	 * (or if no SessionID variable was found) - initialize the interp
	 */

	// request.props.list(System.out);
	String sessionId = request.props.getProperty("SessionID","common");
	request.log(Server.LOG_DIAGNOSTIC, "  Using session: " + sessionId);
	Interp interp = (Interp) SessionManager.getSession(sessionId, "TCL",
		Interp.class);
	setupInterp(interp, sessionId);

    	int code = 0;
	String result = interp.getResult().toString();
    	synchronized (interp) {
	    try {
		interp.eval(callback + " " +
			ReflectObject.newInstance(interp, Request.class, request));
	    } catch (TclException e) {
		code = e.getCompletionCode();
		String trace = e.toString();
		System.out.println("Tcl Oops: "  + code + " " + e);
		if (code == 1) {
		    try {
			trace = interp.getVar("errorInfo", TCL.GLOBAL_ONLY).toString();
		    } catch (Exception e1) {}
		}
		request.log(Server.LOG_WARNING, propsPrefix + trace);
	    }
	    result = interp.getResult().toString();
	}  // end sync block
	if ((code==0 || code==2) && (result.equalsIgnoreCase("true") ||
		result.equals("1"))) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Setup a tcl interpreter for this session
     */

    private void
    setupInterp(Interp newInterp, String id) {
	try {
	    newInterp.getVar("SessionID", TCL.GLOBAL_ONLY);
	    return;
	} catch (TclException e) {
	    System.out.println("New interp: " + e);
	}
	try {
	    TclUtil.setVar(newInterp, "tcl_interactive", "0", TCL.GLOBAL_ONLY);
	    TclUtil.setVar(newInterp, "argv0", scriptName, TCL.GLOBAL_ONLY);
	    TclUtil.setVar(newInterp, "prefix", propsPrefix, TCL.GLOBAL_ONLY);
	    TclUtil.setVar(newInterp, "SessionID", id, TCL.GLOBAL_ONLY);
	    TclUtil.setVar(newInterp, "server",
		    ReflectObject.newInstance(newInterp, Server.class, server), 
		    TCL.GLOBAL_ONLY);
	    newInterp.evalFile(scriptName);
	} catch (TclException e) {
	    int code = e.getCompletionCode();
	    String trace = e.toString();
	    if (code == 1) {
		try {
		    trace = newInterp.getVar("errorInfo",
			    TCL.GLOBAL_ONLY).toString();
		} catch (Exception e1) {}
	    }
	    server.log(Server.LOG_WARNING, null, trace);
	}
    return;
    }
}
