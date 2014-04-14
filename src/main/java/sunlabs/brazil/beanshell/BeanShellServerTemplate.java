/*
 * BeanShellServerTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2002-2004 Sun Microsystems, Inc.
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
 * The Initial Developer of the Original Code is: drach.
 * Portions created by drach are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): drach, suhler.
 *
 * Version:  2.5
 * Created by drach on 02/01/28
 * Last modified by suhler on 04/11/30 15:19:38
 *
 * Version Histories:
 *
 * 2.5 04/11/30-15:19:38 (suhler)
 *   fixed sccs version string
 *
 * 2.4 03/08/01-16:19:41 (suhler)
 *   fixes for javadoc
 *
 * 2.3 03/07/07-13:59:21 (suhler)
 *   use new addClosingTag convenience method
 *
 * 2.2 02/11/14-14:48:48 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.6.1.1 02/11/14-14:47:54 (suhler)
 *   make setup protected, so we can subclass
 *
 * 2.1 02/10/01-16:39:54 (suhler)
 *   version change
 *
 * 1.6 02/07/19-14:24:42 (suhler)
 *   Request object reference wasn't being reset on each request (duh!)
 *
 * 1.5 02/07/18-14:03:46 (suhler)
 *   only process ${...} constructs using eval
 *
 * 1.4 02/06/05-14:31:46 (suhler)
 *   only create an interp when its needed.
 *
 * 1.3 02/02/06-13:24:43 (drach)
 *   Fix example so it looks right.
 *
 * 1.2 02/01/29-14:52:45 (drach)
 *   Add a simple example.
 *
 * 1.2 02/01/28-14:39:27 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 beanshell/BeanShellServerTemplate.java
 *
 * 1.1 02/01/28-14:39:26 (drach)
 *   date and time created 02/01/28 14:39:26 by drach
 *
 */

package sunlabs.brazil.beanshell;

import sunlabs.brazil.handler.ResourceHandler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.util.Format;

import bsh.EvalError;
import bsh.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

/**
 * The <code>BeanShellServerTemplate</code> looks for one of the
 * starting tags
 * <code>&lt;server&nbsp;language="beanshell"&gt;</code>,
 * <code>&lt;beanshell&gt;</code>, or
 * <code>&lt;bsh&gt;</code>
 * in an HTML page and treats the following data up to the corresponding
 * ending tag (
 * <code>&lt;/server&gt;</code>,
 * <code>&lt;/beanshell&gt;</code>, or
 * <code>&lt;/bsh&gt;</code>)
 * as a BeanShell script to evaluate.  For information on BeanShell, see
 * www.beanshell.org.
 * <p>
 * The reason that BeanShell scripts are included in an HTML page is
 * usually to generate dynamic, server-side content.  After running
 * this template, everything between and including the starting tag
 * and and the ending tag is replaced by all output written to the
 * BeanShell output stream (if any).
 * <p>
 * All BeanShell fragments within a given page are evaluated in the
 * same BeanShell interpreter.  The BeanShell interpreter actually
 * lives for the entire duration of this <code>Template</code> object,
 * so the user can implement persistence across requests.
 * <p>
 * The following configuration parameters are used to initialize this
 * template.
 * <dl class=props>
 * <dt> script
 *	<dd> The name of the BeanShell script to evaluate when the
 *	interpreter is created.  This script is only evaluated when
 *	the interpreter is created, not on every request. The
 *	variables <code>prefix</code> and <code>server</code> are set
 *	before this file is evaluated, and are references to the
 *	parameters passed to a <code>handler</code> init method.
 * <dt> root
 *	<dd> The document root, if the script is a relative file name.
 *	If the "root" property under the template prefix is not found,
 *	the global "root" property is used.  If the global "root"
 *	property is not found, the current directory is used.
 * <dt> debug
 *	<dd> If this configuration parameter is present, this class
 *	replaces the starting and ending tags with comments, so the
 *	user can keep track of where the dynamically generated content
 *	is coming from by examining the comments in the resultant HTML
 *	document.  By default, the starting and ending tags are
 *	completely eliminated from the HTML document rather than
 *	changed into comments.
 * </dl>
 * <p>
 * Before evaluating each HTML document, this class sets variables in
 * the BeanShell interpreter, which can be used to interact back with
 * Java to do things like set the response headers:
 * <dl>
 * <dt> request
 *	<dd> Exposes the {@link sunlabs.brazil.server.Request} Java object.  
 *	It is set anew at each request.
 * <dt> prefix
 *	<dd> Exposes the handler prefix String.
 * <dt> server
 *	<dd> Exposes the handler {@link sunlabs.brazil.server.Server} object.
 * </dl>
 * If the attribute <code>eval</code> is present as an attribute, all
 * constructs off the form ${...} are substituted before processing
 * the script.
 * <p>
 * Here's a simple example of a BeanShell template:
 * <pre><code>
 * &lt;html&gt;
 * &lt;head&gt;
 * &lt;title&gt;BeanShell Examples&lt;/title&gt;
 * &lt;/head&gt;
 * &lt;body&gt;
 * The global variables &lt;code&gt;request&lt;/code&gt;,
 * &lt;code&gt;prefix&lt;/code&gt;, and &lt;code&gt;server&lt;/code&gt;
 * are already defined.  Here's how to add a new property:
 * &lt;bsh&gt;
 * sum = 3 + 4 + 5;
 * request.props.put("sum", Integer.toString(sum));
 * &lt;/bsh&gt;
 * And here's a way to list the properties contained in the request:
 * &lt;table&gt;
 * &lt;bsh&gt;
 * e = request.props.propertyNames();
 * while (e.hasMoreElements()) {
 *   name = e.nextElement();
 *   value = request.props.getProperty(name);
 *   print("&lt;tr&gt;&lt;td&gt;" + name + "&lt;/td&gt;&lt;td&gt;" 
 *         + value + "&lt;/td&gt;&lt;/tr&gt;");
 * }
 * &lt;/bsh&gt;
 * &lt;/table&gt;
 * &lt;/body&gt;
 * &lt;/html&gt;
 * </code></pre>
 *
 * @author	Steve Drach
 * @version		2.5
 */

public class BeanShellServerTemplate
    extends Template {
    private static final String SCRIPT = "script";

    Interpreter bsh = null;
    ByteArrayOutputStream stdout;

    /**
     * Called at the beginning of each HTML document that this
     * <code>BeanShellServerTemplate</code> is asked to process.
     * <p>
     * The first time this method is called, the initialization script
     * is sourced into the interpreter, based on the configuration
     * properties in the <code>Request</code>
     *
     * @param	hr
     *		The request and associated HTML document that will be
     *		processed.
     *
     * @return	<code>true</code> interpreter was successfully
     *  	initialized <code>false</code> otherwise.  About the
     *  	only way that the initialization could fail would be
     *  	due to an error sourcing the initialization script. If
     *  	<code>false</code> is returned, an error message is
     *  	logged.
     */

    public boolean
    init(RewriteContext hr) {
	Properties props = hr.request.props;
	hr.addClosingTag("beanshell");
	hr.addClosingTag("bsh");
	return super.init(hr);
    }

    /**
     * Only create the interp on first use; not at each page
     */

    protected boolean
    setup(RewriteContext hr) {
	Properties props = hr.request.props;

	if (bsh == null) {
	    stdout = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(stdout);
	    bsh = new Interpreter(new InputStreamReader(System.in), ps, ps,
				  false);

	    String script = props.getProperty(hr.prefix + SCRIPT);
	    try {
		bsh.set("prefix", hr.prefix);
		bsh.set("server", hr.server);

		if (script != null) {
		    String body = ResourceHandler.getResourceString(props,
			    hr.prefix, script);
		    bsh.eval(body);
		}
	    } catch (IOException e) {
		hr.request.log(Server.LOG_ERROR, "reading init script", script);
		return false;
	    } catch (EvalError e) {
		hr.request.log(Server.LOG_ERROR, "initializing BeanShell",
			    e.toString());
		return false;
	    }
	}
	return true;
    }

    /**
     * Processes the <code>&lt;server&gt;</code> tag.  Substitutes the
     * result of evaluating the following BeanShell script into the
     * resultant HTML document.
     *
     * @param	hr
     *		The request and associated HTML document that will be
     *		processed.
     */

    public void
    tag_server(RewriteContext hr) {
	String language = hr.get("language");
	if ("beanshell".equals(language) ||
	    "bsh".equals(language)) {
	    tag_beanshell(hr);
	}
    }
	
    /**
     * Processes the <code>&lt;beanshell&gt;</code> tag.  Substitutes
     * the result of evaluating the following BeanShell script into
     * the resultant HTML document.
     *
     * @param	hr
     *		The request and associated HTML document that will be
     *		processed.
     *
     */

    public void
    tag_beanshell(RewriteContext hr) {
	debug(hr);
	boolean eval = hr.isTrue("eval");
	hr.accumulate(false);
	hr.nextToken();
	String script = hr.getBody();

	if (eval) {
	    script =  Format.subst(hr.request.props, script, true);
	}
	hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, script);
	if (setup(hr)) {
	    try {
	        bsh.set("request", hr.request);
	        bsh.eval(script);
	    } catch (EvalError e) {
	        hr.append("\n<!-- server-side BeanShell: error code " +
		        e.toString() + " -->\n");
	        hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, e.toString());
	    }
	    hr.nextToken();
	    hr.append(stdout.toString());
	    stdout.reset();
	} else {
	    debug(hr, "Interpreter didn't initialize");
	}
	hr.accumulate(true);
    }

    /**
     * Processes the <code>&lt;bsh&gt;</code> tag.  Substitutes the
     * result of evaluating the following BeanShell script into the
     * resultant HTML document.
     *
     * @param	hr
     *		The request and associated HTML document that will be
     *		processed.
     */      

    public void
    tag_bsh(RewriteContext hr) {
	tag_beanshell(hr);
    }
}
