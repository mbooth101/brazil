/*
 * PythonServerTemplate.java
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
 * Contributor(s): drach, suhler.
 *
 * Version:  2.4
 * Created by suhler on 00/09/08
 * Last modified by suhler on 04/11/30 15:19:40
 *
 * Version Histories:
 *
 * 2.4 04/11/30-15:19:40 (suhler)
 *   fixed sccs version string
 *
 * 2.3 03/08/01-16:19:36 (suhler)
 *   fixes for javadoc
 *
 * 2.2 03/07/07-14:45:11 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.11.1.1 03/07/07-14:05:01 (suhler)
 *   use addClosingTag() convenience method
 *
 * 2.1 02/10/01-16:38:53 (suhler)
 *   version change
 *
 * 1.11 02/07/18-14:06:26 (suhler)
 *   don't do \
 *   \XXX expansions with eval
 *
 * 1.10 01/07/16-16:50:52 (suhler)
 *   use new Template convenience methods
 *
 * 1.6.1.1 01/06/04-11:06:58 (suhler)
 *   jython -vs- jpython
 *
 * 1.9 01/05/24-17:07:55 (drach)
 *   Add closing tag </python> every time init() is called.
 *
 * 1.8 01/05/14-08:52:30 (drach)
 *   Use new debug method in super class Template.
 *
 * 1.7 01/05/14-08:33:31 (drach)
 *   Jython apparently doesn't have a classBasedException option.
 *
 * 1.6 01/04/06-08:51:29 (suhler)
 *   added "eval" attribute to perform ${...} substitutions before running script
 *
 * 1.5 00/12/11-13:31:45 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.4 00/10/06-16:04:19 (suhler)
 *   fixed initialization bug, add python tracebacks when log=5
 *
 * 1.3 00/09/13-14:48:41 (suhler)
 *   move to python package, add <python> tag
 *
 * 1.2 00/09/08-19:40:11 (suhler)
 *   capture stdout of pyton interp
 *
 * 1.2 00/09/08-18:11:17 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 python/PythonServerTemplate.java
 *   Name history : 1 0 tcl/PythonServerTemplate.java
 *
 * 1.1 00/09/08-18:11:16 (suhler)
 *   date and time created 00/09/08 18:11:16 by suhler
 *
 */

package sunlabs.brazil.python;

import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.handler.ResourceHandler;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.util.Format;

import org.python.core.PyException;
import org.python.core.Options;
import org.python.util.PythonInterpreter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The <code>PythonServerTemplate</code> looks for each
 * <code>&lt;server&nbsp;language="python"&gt;</code> (or
 *  <code>&lt;python&gt;</code>)
 * tag in an HTML page and treats the following data up to the next
 * <code>&lt;/server&gt;</code> (or  <code>&lt;/python&gt;</code>)
 * tag as a python script to evaluate.
 * <p>
 * The reason that python scripts are included in an HTML page is usually
 * to generate dynamic, server-side content.  After running this template,
 * everything between and including the <code>&lt;server&gt;</code> and
 * <code>&lt;/server&gt;</code> (or <code>&lt;python&gt;</code> and
 * <code>&lt;/python&gt;</code> tags is replaced by all output written
 * to the Python standard output stream (if any).
 * <p>
 * All Python fragments within a given page are evaluated in the same Python
 * interpreter.  The Python interpreter actually lives for the entire duration
 * of this <code>Template</code> object, so the user can implement
 * persistence across requests.
 * <p>
 * The following configuration parameters are used to initialize this
 * template. <dl class=props>
 * <dt> script
 *	<dd> The name of the Python script to evaluate when the interpreter is
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
 *	replaces the <code>&lt;server&gt;</code> and
 *	<code>&lt;/server&gt;</code> tags with comments, so the user
 *	can keep track of where the dynamically generated content is coming
 *	from by examining the comments in the resultant HTML document.
 *	By default, the <code>&lt;server&gt;</code> and
 *	<code>&lt;/server&gt;</code> are completely eliminated from the
 *	HTML document rather than changed into comments.
 * </dl>
 * <p>
 * Before evaluating each HTML document, this class sets variables
 * in the Python interpreter, which can be used to interact back with Java to
 * do things like set the response headers: <dl>
 * <dt> request
 *	<dd> Exposes the {@link sunlabs.brazil.server.Request} Java object.  
 *	It is set anew at each request.
 * <dt> prefix
 *	<dd> Exposes the handler prefix String.
 * <dt> server
 *	<dd> Exposes the handler {@link sunlabs.brazil.server.Server} object.
 * </dl>
 * If the attribute <code>eval</code> is present as an attribute, all
 * constructs off the form ${...} are substituted before processing the
 * script.
 *
 * @author	Stephen Uhler
 * @version		2.4
 */

public class PythonServerTemplate
    extends Template {
    private static final String SCRIPT = "script";

    PythonInterpreter interp = null;
    ByteArrayOutputStream stdout;

    /*
     * Called at the beginning of each HTML document that this
     * <code>PythonServerTemplate</code> is asked to process.
     * <p>
     * The first time this method is called, the initialization script is
     * sourced into the interpreter, based on the configuration properties
     * in the <code>Request</code>
     *
     * @param	hr
     *		The request and associated HTML document that will be
     *		processed.
     *
     * @return	<code>true</code> interpreter was successfully initialized
     *		<code>false</code> otherwise.  About the only way that the
     *		initialization could fail would be <ol>
     *		<li> there was an error sourcing the initialization script.
     *		</ol>
     *		If <code>false</code> is returned, an error message is logged.
     */

    public boolean
    init(RewriteContext hr) {
	Properties props = hr.request.props;

	if (interp == null) {
	    stdout = new ByteArrayOutputStream();
	    try {
		Options.classBasedExceptions = false;
	    } catch (NoSuchFieldError e) {};
	    if (hr.server.logLevel >= Server.LOG_DIAGNOSTIC) {
		Options.showJavaExceptions = true;
	    }
	    interp = new PythonInterpreter();

	    String script = props.getProperty(hr.prefix + SCRIPT);
	    // System.err.println("Creating Python interp");
	    try {
		interp.set("prefix", hr.prefix);
		interp.set("server", hr.server);

		if (script != null) {
		    String body = ResourceHandler.getResourceString(props,
			    hr.prefix, script);
		    // System.err.println("startup: " + body);
		    interp.exec(body);
		}
	    } catch (IOException e) {
		hr.request.log(Server.LOG_ERROR, "reading init script", script);
		return false;
	    } catch (PyException e) {
		hr.request.log(Server.LOG_ERROR, "initializing Python",
			    e.toString());
		return false;
	    }
	    // System.out.println("Init done");
	}

	hr.addClosingTag("python");

	try {
	    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, "Setting request");
	    interp.set("request", hr.request);
	} catch (PyException e) {
	    hr.request.log(Server.LOG_WARNING, hr.prefix,
		    "Setting up Python request: " + e.toString());
	    done(hr);
	    return false;
	}
	return super.init(hr);
    }

    /**
     * Processes the <code>&lt;server&gt;</code> tag.  Substitues the
     * result of evaluating the following Python script into the resultant
     * HTML document.
     * <p>
     * Note: Currently, there is no mechanism for other language interpreters
     * to share the same <code>server</code> tag.
     *
     * @param	hr
     *		The request and associated HTML document that will be
     *		processed.
     */

    public void
    tag_server(RewriteContext hr) {
	if ("python".equals(hr.get("language")) == false) {
	    return;
	}
	tag_python(hr);
    }
	
    /**
     * Processes the <code>&lt;python&gt;</code> tag.  Substitues the
     * result of evaluating the following Python script into the resultant
     * HTML document.
     *
     * @param	hr
     *		The request and associated HTML document that will be
     *		processed.
     */

    public void
    tag_python(RewriteContext hr) {
	debug(hr);
	boolean eval = hr.isTrue("eval");
	hr.accumulate(false);
	hr.nextToken();
	interp.setOut(stdout);
	String script = hr.getBody();

	if (eval) {
	    script =  Format.subst(hr.request.props, script, true);
	}
	hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, script);
	try {
	    interp.exec(script);
	} catch (PyException e) {
	    hr.append("\n<!-- server-side Python: error code " +
		    e.toString() + " -->\n");
	    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, e.toString());
	}
	interp.setOut(System.out);
	hr.nextToken();
	hr.append(stdout.toString());
	stdout.reset();
	hr.accumulate(true);
    }
}
