/*
 * TclRePollHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2004 Sun Microsystems, Inc.
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
 * Created by suhler on 01/03/06
 * Last modified by suhler on 04/11/30 15:19:44
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:44 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:37:25 (suhler)
 *   version change
 *
 * 1.5 02/07/11-15:03:59 (suhler)
 *   use new fillProps api's
 *
 * 1.4 01/12/10-14:51:33 (suhler)
 *   doc lint
 *
 * 1.3 01/11/21-11:44:12 (suhler)
 *   doc fixes
 *
 * 1.2 01/07/11-16:27:52 (suhler)
 *   doc fixes
 *
 * 1.2 01/03/06-11:25:37 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 tcl/TclRePollHandler.java
 *
 * 1.1 01/03/06-11:25:36 (suhler)
 *   date and time created 01/03/06 11:25:36 by suhler
 *
 */

package sunlabs.brazil.tcl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import sunlabs.brazil.handler.RePollHandler;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.http.HttpRequest;
import tcl.lang.Interp;
import tcl.lang.ReflectObject;
import tcl.lang.TCL;
import tcl.lang.TclException;
import tcl.lang.TclUtil;
import tcl.lang.TclObject;

/**
 * Post-process all "polled" properties with tcl code.
 * The following server properties are used:
 * <dl class=props>
 * <dt>script	<dd>The name of the TCL script to call at each request.
 *		The interpreter is created with the following 
 *		global variables.  The script is
 *		"evaluted" once on startup.
 *		<ul>
 *		<li><b>prefix</b> The handler prefix
 *		<li><b>server</b> The current server instance
 *		<li><b>logLevel</b> The current server log level setting
 *		<li><b>argv0</b> The name of the running script
 *		</ul>
 *	  	The tcl procedure "process" is called with the
 *		java properties object.  Any modifications to that
 *		object are done here.
 * <dt>debug	<dd>If set, the "script" is sourced each time.
 * </dl>
 */

public class TclRePollHandler extends RePollHandler {
    String scriptName;
    Interp interp;
    boolean debug;

    /**
     * Create a tcl interp, extract the properties, and run the init script
     */

    public boolean
    init(Server server, String prefix) {
	scriptName = server.props.getProperty(prefix + "script",prefix + "tcl");
	debug = (server.props.getProperty(prefix + "debug") != null);
	File scriptFile = new File(scriptName);
	if (!scriptFile.isAbsolute()) {
	    scriptFile = new File(server.props.getProperty(
	    		FileHandler.ROOT,"."), scriptName);
	}
	scriptName = scriptFile.getAbsolutePath();
	server.log(Server.LOG_DIAGNOSTIC, prefix, "Using: " + scriptName);
	try {
	    interp = new Interp();
	    TclUtil.setVar(interp, "tcl_interactive", "0", TCL.GLOBAL_ONLY);
	    TclUtil.setVar(interp, "argv0", scriptName, TCL.GLOBAL_ONLY);
	    TclUtil.setVar(interp, "prefix", prefix, TCL.GLOBAL_ONLY);
	    TclUtil.setVar(interp, "logLevel", "" + server.logLevel,
		    TCL.GLOBAL_ONLY);
	    TclUtil.setVar(interp, "server",
		    ReflectObject.newInstance(interp, Server.class, server), 
		    TCL.GLOBAL_ONLY);
	    interp.eval("package require java");
	    interp.evalFile(scriptName);
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
	    return false;
	}
	return super.init(server, prefix);
    }

    /**
     * Fill the properties from the input stream
     */

    public void
    fillProps(Properties props, HttpRequest target) throws IOException {
    	super.fillProps(props, target);
	try {
	    if (debug) {
		interp.evalFile(scriptName);
	    }
	    interp.eval("process " + 
		ReflectObject.newInstance(interp, Properties.class, props));
	} catch (TclException e) {
	    if (e.getCompletionCode() == TCL.ERROR) {
		TclObject errorInfo;
		try {
		    errorInfo = interp.getVar("errorInfo", null,
			    TCL.GLOBAL_ONLY);
		} catch (Exception e2) {
		    errorInfo = null;
		}
		System.out.println("Tcl Error: " + errorInfo);
	    }
	}
    }
}
