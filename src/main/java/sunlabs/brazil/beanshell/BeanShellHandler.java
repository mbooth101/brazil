/*
 * BeanShellHandler.java
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
 * Created by suhler on 02/02/14
 * Last modified by suhler on 06/11/13 15:09:44
 *
 * Version Histories:
 *
 * 2.3 06/11/13-15:09:44 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.2 04/11/30-15:19:37 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:39:55 (suhler)
 *   version change
 *
 * 1.2 02/02/14-15:01:59 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 beanshell/BeanShellHandler.java
 *
 * 1.1 02/02/14-15:01:58 (suhler)
 *   date and time created 02/02/14 15:01:58 by suhler
 *
 */

package sunlabs.brazil.beanshell;

import java.io.IOException;
import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.handler.ResourceHandler;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * The <code>BeanShellHandler</code>
 * permits handlers to be written in "beanshell".
 * <p>
 * The reason one would write a BeanShell handler, instead of
 * coding the handler directly in Java, is for ease of handler development
 * and maintanence; a BeanShell handler may be modified at will while the
 * server is running, permitting rapid development.  Once functional, 
 * the code is easily converted into a traditional handler.
 * <p>
 * The beanshell script is expected to contain both <code>init</code>
 * and <code>respond</code> methods, which are invoked by the server
 * just like an ordinary handler.
 * <dl class=props>
 * <dt> script
 * <dd> The name of the BeanShell script to use as the handler.
 * 	Normally, the script is read only once. (defaults to <i>prefix</i>.bsh)
 * <dt> root
 *	<dd> The script directory, if the script is a relative file name.
 *	If the "root" property under the prefix is not found,
 *	the global "root" property is used.  If the global "root"
 *	property is not found, the current directory is used.
 * <dt> debug
 *	<dd> If this configuration parameter is present, the <code>
 *	script</code> is re-read on each request, and a new interperter is
 *	created and initialized.  The call to <code>init</code> is
 *      deferred until <i>request</i> time, and called before each
 *	call to <code>respond</code>.
 *      <p>This allows beanshell scripts to be debugged interatively 
 *	from scratch.
 * </dl>
 *
 * @author	Stephen Uhler
 * @version		2.3
 */

public class BeanShellHandler implements Handler {
    private static final String SCRIPT = "script";
    MatchString isMine;            // check for matching url
    Interpreter bsh = null;	   // our interpreter.
    Server server;		   // our server reference
    String prefix;		   // our props prefix
    String script;		   // the script name

    public boolean
    init(Server server, String prefix) {
	this.server = server;
	this.prefix = prefix;
	isMine = new MatchString(prefix, server.props);
	script = server.props.getProperty(prefix + SCRIPT, prefix + "bsh");
        if (server.props.getProperty(prefix + "debug") == null) {
            return init();
	} else {
	    server.log(Server.LOG_DIAGNOSTIC, prefix,
		  "Debugging enabled, init deferred");
	    return true;
	}
    }

    boolean
    init() {
	server.log(Server.LOG_DIAGNOSTIC, prefix,
	      "creating bsh interp");
	
	bsh = new Interpreter();
	try {
           String body = ResourceHandler.getResourceString(server.props,
			    prefix, script);
           bsh.eval(body);
	   bsh.set("_prefix", prefix);
	   bsh.set("_server", server);
	   bsh.eval("_result = init(_server, _prefix)");
	   Boolean get = (Boolean) bsh.get("_result");
	   return get.booleanValue();
	} catch (IOException e) {
	    server.log(Server.LOG_ERROR, prefix, "reading init script: " +
		 e.getMessage());
	    return false;
	} catch (EvalError e) {
	    server.log(Server.LOG_ERROR, "initializing BeanShell",
			    e.toString());
	    return false;
	}
    }

    public boolean
    respond(Request request) throws IOException {
       if (!isMine.match(request.url)) {
	   return false;
       }
       String debug = request.props.getProperty(prefix + "debug");
       if ((debug != null || bsh == null) && (init() == false)) {
          return false;
       }
       try {
          bsh.set("_request", request);
          bsh.eval("_result = respond(_request);");
          Boolean get = (Boolean) bsh.get("_result");
          return get.booleanValue();
       } catch (EvalError e) {
          request.log(Server.LOG_ERROR, isMine.prefix(), e.getMessage());
          return false;
       }
    }
}
