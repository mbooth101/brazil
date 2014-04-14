/*
 * JavaScriptTemplate.java
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
 * Version:  2.4
 * Created by drach on 02/07/19
 * Last modified by suhler on 04/11/30 15:19:39
 *
 * Version Histories:
 *
 * 2.4 04/11/30-15:19:39 (suhler)
 *   fixed sccs version string
 *
 * 2.3 03/08/01-16:21:57 (suhler)
 *   javadoc fixes
 *
 * 2.2 03/07/07-14:44:34 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.2.1.1 03/07/07-14:04:54 (suhler)
 *   use addClosingTag() convenience method
 *
 * 2.1 02/10/01-16:34:49 (suhler)
 *   version change
 *
 * 1.2 02/07/19-17:01:29 (drach)
 *   Fixed up the comments.
 *
 * 1.2 02/07/19-16:12:01 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 javascript/JavaScriptTemplate.java
 *
 * 1.1 02/07/19-16:12:00 (drach)
 *   date and time created 02/07/19 16:12:00 by drach
 *
 */

package sunlabs.brazil.javascript;

import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.handler.ResourceHandler;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.util.Format;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.util.Properties;

/**
 * The <code>JavaScriptTemplate</code> looks for each
 * <code>&lt;server&nbsp;language="javascript"&gt;</code> (or
 *  <code>&lt;javascript&gt;</code>)
 * tag in an HTML page and treats the following data up to the next
 * <code>&lt;/server&gt;</code> (or  <code>&lt;/javascript&gt;</code>)
 * tag as a JavaScript script to evaluate.
 * <p>
 * The reason that JavaScript scripts are included in an HTML page is usually
 * to generate dynamic, server-side content.  After running this template,
 * everything between and including the <code>&lt;server&gt;</code> and
 * <code>&lt;/server&gt;</code> (or <code>&lt;javascript&gt;</code> and
 * <code>&lt;/javascript&gt;</code> tags is replaced by all output written
 * to the JavaScript standard output stream (if any).
 * <p>
 * All JavaScript fragments within a given page are evaluated in the same JavaScript
 * interpreter.  The JavaScript interpreter actually lives for the entire duration
 * of this <code>Template</code> object, so the user can implement
 * persistence across requests.
 * <p>
 * The following configuration parameters are used to initialize this
 * template. <dl class=props>
 * <dt> script
 *	<dd> The name of the JavaScript script to evaluate when the interpreter is
 *	created.  This script is only evaluated when the interp is created,
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
 * in the JavaScript interpreter, which can be used to interact back with Java to
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
 * constructs of the form ${...} are substituted before processing the
 * script.
 * <p>
 * Here's a simple example of a JavaScript template:
 * <pre><code>
 * &lt;html&gt;
 * &lt;head&gt;
 * &lt;title&gt;JavaScript Example&lt;/title&gt;
 * &lt;/head&gt;
 * &lt;body&gt;
 * &lt;javascript&gt;
 * var s = "request=" + request;
 * s += "&lt;br&gt;request.serverUrl is " + request.serverUrl();
 * s += "&lt;br&gt;server hostName is " + server.hostName;
 * s += "&lt;br&gt;prefix is " + prefix;
 * s += "&lt;br&gt; &lt;table&gt;";
 * 
 * // This is an example of using Java in JavaScript with LiveConnect
 * var e = request.props.propertyNames();
 * while (e.hasMoreElements()) {
 *   var prop = e.nextElement();
 *   s += "&lt;tr&gt;&lt;td&gt;" + prop + "&lt;/td&gt;";
 *   s += "&lt;td&gt;" + request.props.getProperty(prop) + "&lt;/td&gt;&lt;/tr&gt;";
 * }
 * 
 * s += "&lt;/table&gt;";
 * 
 * // The last value computed or expressed is returned
 * s;
 * 
 * &lt;/javascript&gt;
 * &lt;/body&gt;
 * &lt;/html&gt;
 * </code></pre>
 *
 * @author	Steve Drach
 * @version		2.4
 */

public class JavaScriptTemplate extends Template {
    private static final String SCRIPT = "script";

    Context cx = null;
    Scriptable scope = null;
    Thread current = null;

    /**
     * Called at the beginning of each HTML document that this
     * <code>JavaScriptTemplate</code> is asked to process.
     * <p>
     * The first time this method is called, the initialization script is
     * sourced into the interpreter, based on the configuration properties
     * in the <code>Request</code>
     *
     * @param	hr
     *		The request and associated HTML document that will be
     *		processed.
     *
     * @return	<code>true</code>.
     */

    public boolean
    init(RewriteContext hr) {
	hr.addClosingTag("javascript");
	return super.init(hr);
    }

    private boolean
    setup(RewriteContext hr) {
	Properties props = hr.request.props;

	scope = cx.initStandardObjects(null);

	String script = props.getProperty(hr.prefix + SCRIPT);
	try {
	    scope.put("prefix", scope, hr.prefix);
	    scope.put("server", scope, Context.toObject(hr.server, scope));

	    if (script != null) {
		String body = ResourceHandler.getResourceString(props,
								hr.prefix, script);
		System.err.println("startup: " + body);
		cx.evaluateString(scope, body, script, 1, null);
	    }
	} catch (IOException e) {
	    hr.request.log(Server.LOG_ERROR, "reading init script", script);
	    Context.exit();
	    return false;
	} catch (JavaScriptException e) {
	    hr.request.log(Server.LOG_ERROR, "initializing JavaScript",
			   e.toString());
	    Context.exit();
	    return false;
	}

	return true;
    }

    /**
     * Processes the <code>&lt;server&gt;</code> tag.  Substitutes the
     * result of evaluating the following JavaScript script into the resultant
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
	if ("javascript".equals(hr.get("language")) == false) {
	    return;
	}
	tag_javascript(hr);
    }
	
    /**
     * Processes the <code>&lt;javascript&gt;</code> tag.  Substitutes the
     * result of evaluating the following JavaScript script into the resultant
     * HTML document.
     *
     * @param	hr
     *		The request and associated HTML document that will be
     *		processed.
     */

    public void
    tag_javascript(RewriteContext hr) {
	Thread thread;

	debug(hr);
	boolean eval = hr.isTrue("eval");
	hr.accumulate(false);
	hr.nextToken();
	String script = hr.getBody();

	if (eval) {
	    script =  Format.subst(hr.request.props, script);
	}

	hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, "Setting request");

	if ((thread = Thread.currentThread()) != current) {
	    if (cx != null) {
		Context.exit();
	    }
	    cx = Context.enter();
	    current = thread;
	}

	if (scope == null) {
	    if (!setup(hr)) {
		hr.accumulate(true);
		return;
	    }
	}

	scope.put("request", scope, Context.toObject(hr.request, scope));

	hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, script);

	Object result = null;
	try {
	    result = cx.evaluateString(scope, script, "template", 1, null);
	} catch (JavaScriptException e) {
	    hr.append("\n<!-- server-side JavaScript: error code " +
		    e.toString() + " -->\n");
	    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, e.toString());
	}

	hr.nextToken();
	if (result != cx.getUndefinedValue()) {
	    hr.append(cx.toString(result));
	}
	hr.accumulate(true);
    }
}
