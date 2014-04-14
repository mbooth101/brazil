/*
 * GroovyServerTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2004 Sun Microsystems, Inc.
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
 * Version:  1.3
 * Created by suhler on 04/12/16
 * Last modified by suhler on 04/12/30 13:44:32
 *
 * Version Histories:
 *
 * 1.3 04/12/30-13:44:32 (suhler)
 *   minor doc fixes
 *
 * 1.2 04/12/17-14:28:22 (suhler)
 *   Defer starting the groovy intepreter until the first time it's needed
 *
 * 1.2 04/12/16-20:44:08 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 groovy/GroovyServerTemplate.java
 *
 * 1.1 04/12/16-20:44:07 (suhler)
 *   date and time created 04/12/16 20:44:07 by suhler
 *
 */

package sunlabs.brazil.groovy;

import java.io.IOException;

import sunlabs.brazil.server.Server;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.handler.ResourceHandler;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilationFailedException;

/**
 * The <code>GroovyServerTemplate</code> looks for each
 * <code>&lt;groovy&gt;</code>
 * tag in an HTML page and treats the following data up to the next
 * <code>&lt;/groovy&gt;</code>)
 * tag as a groovy script to evaluate.
 * <p>
 * All Groovy fragments within a given page are evaluated in the same Groovy
 * interpreter.  The Groovy interpreter actually lives for the entire duration
 * of this <code>Template</code> object, so the user can implement
 * persistence across requests:  Each session gets its own interpreter.
 * <p>
 * The following configuration parameters are used to initialize this
 * template. <dl class=props>
 * <dt> script
 *	<dd> The name of the Groovy script to evaluate when the interpreter is
 *	created.  This script only evaluated when the interp is created,
 *	not on every request. The variables <code>prefix</code> and
 *	<code>server</code> are set before this file is evaluated, and
 *	are references to the parameters passed to a <code>handler</code>
 *	init method.
 * <dt> root
 *	<dd> The document root, if the script is a relative file name.
 *	If the "root" property under the template prefix is not found, the
 *	global "root" property is used.  If the global "root" property is
 *	not found, the current directory is used.
 * <dt> debug
 *	<dd> If this configuration parameter is present, this class
 *	replaces the <code>&lt;groovy&gt;</code> and
 *	<code>&lt;/groovy&gt;</code> tags with comments, so the user
 *	can keep track of where the dynamically generated content is coming
 *	from by examining the comments in the resultant HTML document.
 *	By default, the <code>&lt;groovy&gt;</code> and
 *	<code>&lt;/groovy&gt;</code> are completely eliminated from the
 *	HTML document rather than changed into comments.
 * </dl>
 * <p>
 * Before evaluating each HTML document, this class sets variables
 * in the Groovy interpreter, which can be used to interact back with Java to
 * do things like set the response headers: <dl>
 * <dt> request
 *	<dd> Exposes the {@link sunlabs.brazil.server.Request} Java object.  
 *	It is set anew at each request.
 * <dt> prefix
 *	<dd> Exposes the handler prefix String.
 * <dt> server
 *	<dd> Exposes the handler {@link sunlabs.brazil.server.Server} object.
 * </dl>
 *
 * @author	Stephen Uhler
 * @version		GroovyServerTemplate.java
 */

public class GroovyServerTemplate extends Template {
    private static final String SCRIPT = "script";

    GroovyShell interp=null;		// our groovy interpreter
    Binding binding=null;		// Brazil - groovy interactionv
    boolean bad=false;			// bad init, don't retry

    /**
     * Initialize the groovy interperter (1st time only) and set the
     * current request object.
     */

    public boolean
    init(RewriteContext hr) {
	hr.addClosingTag("groovy");
	return super.init(hr);
    }

    /**
     * Set up the interp and run the init script (if any).
     */

    boolean
    setup(RewriteContext hr) {
	if (interp == null) {
	    try {
		binding = new Binding();
		interp = new GroovyShell(binding);
	    } catch (NoClassDefFoundError e) {
		hr.request.log(Server.LOG_ERROR, hr.prefix,
			"Missing Groovy libraries" + e);
		return false;
	    }
	    binding.setVariable("prefix", hr.prefix);
	    binding.setVariable("server", hr.server);

	    String script = hr.request.props.getProperty(hr.prefix + SCRIPT);
	    if (script != null) {
		try {
		    String body = ResourceHandler.getResourceString(
			    hr.request.props, hr.prefix, script);
		hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, body);
			interp.evaluate(body);
		} catch (IOException e) {
		    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
			"Can't read groovy script: " + e);
		    return false;
		} catch (CompilationFailedException e) {
		    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
			"Can't compile groovy script: " + e);
		    return false;
		} catch (groovy.lang.GroovyRuntimeException e) {
		    debug(hr, "Groovy Runtime error: " + e.getMessage());
		    return false;
		}
	    }
	}
	binding.setVariable("request", hr.request);
	return true;
    }

    public void
    tag_groovy(RewriteContext hr) {
	debug(hr);
	hr.accumulate(false);
	hr.nextToken();
	String script = hr.getBody();
	hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, script);
	debug(hr, script);
	if (!bad && setup(hr)) {
	    try {
		Object result = interp.evaluate(script);
		hr.append(result.toString());
	    } catch (CompilationFailedException e) {
		debug(hr, "Bad Script: " + e.getMessage());
	    } catch (IOException e) {
		debug(hr, "I/O problem: " + e.getMessage());
	    } catch (groovy.lang.GroovyRuntimeException e) {
		debug(hr, "Groovy error: " + e.getMessage());
	    }
	} else {
	    bad = true;
	}
	hr.nextToken();
	hr.accumulate(true);
    }
}
