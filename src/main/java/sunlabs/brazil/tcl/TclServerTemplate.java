/*
 * TclServerTemplate.java
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
 * The Initial Developer of the Original Code is: cstevens.
 * Portions created by cstevens are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): cstevens, suhler.
 *
 * Version:  2.4
 * Created by cstevens on 99/10/19
 * Last modified by suhler on 04/11/30 15:11:27
 *
 * Version Histories:
 *
 * 2.4 04/11/30-15:11:27 (suhler)
 *   fixed sccs version string
 *
 * 2.3 03/08/01-16:19:31 (suhler)
 *   fixes for javadoc
 *
 * 2.2 03/07/07-14:45:22 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.20.1.1 03/07/07-14:08:50 (suhler)
 *   use addClosingTag() convenience method
 *
 * 2.1 02/10/01-16:37:25 (suhler)
 *   version change
 *
 * 1.20 01/08/14-16:40:10 (suhler)
 *   add "eval" option for consistency with python
 *
 * 1.19 01/03/12-17:42:35 (cstevens)
 *   Merged changes between child workspace "/home/cstevens/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.18 01/03/06-09:10:40 (suhler)
 *   move interp creation to first use, to avoid redundant interp creation.
 *   Allow <tcl>...</tcl>
 *
 * 1.17.1.1 01/02/12-17:15:17 (cstevens)
 *   debugging
 *
 * 1.17 00/12/11-13:33:12 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.16 00/11/15-09:49:29 (suhler)
 *   add the session id into the tcl namespace
 *
 * 1.15 00/10/05-15:52:07 (cstevens)
 *   lint
 *
 * 1.14 00/07/06-16:00:14 (suhler)
 *   doc updates
 *
 * 1.13 00/07/05-14:14:03 (cstevens)
 *   TclServerTemplate can load init script from resource fork or file system.
 *   TclServerTemplate passes "server" and "prefix" to init script.
 *
 * 1.12 00/06/30-10:31:32 (cstevens)
 *   Merged changes between child workspace "/home/cstevens/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.10.1.1 00/06/30-10:29:58 (cstevens)
 *   request reused too much
 *
 * 1.11 00/06/29-10:47:32 (suhler)
 *   fixed single character typo
 *   .
 *
 * 1.10 00/06/20-07:38:48 (suhler)
 *   expose handler prefix, and prefix and request to init script
 *
 * 1.9 00/05/31-13:52:23 (suhler)
 *   name change
 *
 * 1.8 00/04/20-13:11:20 (cstevens)
 *   Automatically "package require java" into new interps
 *
 * 1.7 00/04/12-16:05:41 (cstevens)
 *   If debug turned on, put errorInfo in an HTML comment if error occurred in
 *   script.
 *
 * 1.6 00/02/11-08:58:19 (suhler)
 *   better memory management
 *
 * 1.5 99/10/27-13:21:32 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.4 99/10/26-18:55:22 (cstevens)
 *   Changed TclServerTemplate and BSLTemplate to use the config parameter "debug"
 *   to decide whether to emit comments into the resultant HTML file.
 *
 * 1.3.1.1 99/10/22-09:59:51 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.2.1.1 99/10/22-09:57:04 (suhler)
 *   ???
 *
 * 1.3 99/10/21-18:27:07 (cstevens)
 *   Merged changes between child workspace "/home/cstevens/ws/brazil/naws" and
 *   parent workspace "/net/mack/export/ws/brazil/naws".
 *
 * 1.1.1.1 99/10/21-18:23:57 (cstevens)
 *   TclServerTemplate: added configuration property to source an initialization
 *   script into the Tcl Interp the first time the Interp is used.
 *
 * 1.2 99/10/20-15:08:49 (suhler)
 *   fixed imports
 *
 * 1.2 99/10/19-19:00:57 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 tcl/TclServerTemplate.java
 *   Name history : 1 0 tclHandlers/TclServerTemplate.java
 *
 * 1.1 99/10/19-19:00:56 (cstevens)
 *   date and time created 99/10/19 19:00:56 by cstevens
 *
 */

package sunlabs.brazil.tcl;

import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.handler.ResourceHandler;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.util.Format;

import tcl.lang.Interp;
import tcl.lang.ReflectObject;
import tcl.lang.TCL;
import tcl.lang.TclException;
import tcl.lang.TclObject;
import tcl.lang.TclTemplateChannel;
import tcl.lang.TclUtil;

import java.io.IOException;
import java.util.Properties;

/**
 * The <code>TclServerTemplate</code> looks for each
 * <code>&lt;server&nbsp;language="tcl"&gt;</code>
 * (or <code>&lt;"tcl"&gt;</code>)
 * tag in an HTML page and treats the following data up to the next
 * <code>&lt;/server&gt;</code> tag as a Tcl script to evaluate.
 * If the optional attribute <code>eval</code> is present,
 * the all ${...} constructs are replaced using
 * {@link sunlabs.brazil.util.Format#subst}
 * before being passed to the Tcl interpreter.
 * <p>
 * The reason that Tcl scripts are included in an HTML page is usually
 * to generate dynamic, server-side content.  After running this template,
 * everything between and including the <code>&lt;server&gt;</code> and
 * <code>&lt;/server&gt;</code> tags is replaced with the result of
 * evaluating the Tcl script as follows: <ul>
 * <li> Anything printed to the standard output of the Tcl interpreter is
 *      added to the HTML document (for instance, <code>puts "hello"</code>).
 * <li> Additionally, if the Tcl script returns a value, that value is
 *	added to the HTML document (for instance, <code>return "bob"</code>).
 * </ul>
 * Multiple <code>puts</code> and a final <code>return</code> can both be
 * used within a single Tcl fragment.
 * <p>
 * All Tcl fragments within a given page are evaluated in the same Tcl
 * interpreter.  The Tcl interpreter actually lives for the entire duration
 * of this <code>Template</code> object, so the user can implement
 * persistence across requests.
 * <p>
 * The following configuration parameters are used to initialize this
 * template. <dl class=props>
 * <dt> script
 *	<dd> The name of the Tcl script to evaluate when the interpreter is
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
 * Before evaluating each HTML document, this class variables
 * in the Tcl interpreter, which can be used to interact back with Java to
 * do things like set the response headers: <dl>
 * <dt> request
 *	<dd> Exposes the {@link sunlabs.brazil.server.Request} Java object.  
 *	It is set anew at each request.
 * <dt> prefix
 *	<dd> Exposes the handler prefix String.
 * <dt> server
 *	<dd> Exposes the handler {@link sunlabs.brazil.server.Server} object.
 * <dt> SessionId
 *	<dd> Exposes the session id for this interp, or "none".
 * </dl>
 *
 * If a serialized version of this object is reconstituted, the init
 * method must be called again.
 *
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version	2.4
 */

public class TclServerTemplate extends Template {
    private static final String SCRIPT = "script";
    private static final String DEBUG = "debug";
    boolean newPage;

    Interp interp = null;

    /*
     * In a template, transient variables don't last longer than the request.
     */

    transient TclTemplateChannel chan;
    transient TclObject request;
    transient boolean debug;

    /**
     * Defer setting up the interpreter until its first use.
     * This way we don't initialize a tcl interp (a heavyweight 
     * operation) until its actually needed.
     */

    public boolean
    init(RewriteContext hr) {
	hr.addClosingTag("tcl");
	newPage=true;
	return true;
    }

    /**
     * Called at the first <b>tcl</b> code in the document
     * <code>TclServerTemplate</code> is asked to process.
     * <p>
     * Redirects the standard output of the Tcl interpreter to the
     * resultant HTML document and exposes the <code>Request</code>
     * object as a Tcl variable.
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
     *		<li> if this class previously evaluated a Tcl fragment that
     *		     redefined the scalar Tcl variables <code>request</code>
     *		     or <code>server</code> as array variables.
     *		</ol>
     *		If <code>false</code> is returned, an error message is logged.
     */

    public boolean
    setup(RewriteContext hr) {
	Properties props = hr.request.props;

	if (interp == null) {
	    interp = new Interp();

	    String script = props.getProperty(hr.prefix + SCRIPT);
	    String session = (hr.sessionId == null) ? "none" : hr.sessionId;
	    try {
		interp.eval("package require java");
		interp.eval("set prefix " + hr.prefix);
		interp.eval("set SessionId " + session);

		TclObject server = ReflectObject.newInstance(interp,
			Server.class, hr.server);

		TclUtil.setVar(interp, "server", server, 0);

		if (script != null) {
		    String body = ResourceHandler.getResourceString(props,
			    hr.prefix, script);
		    interp.eval(body);
		}
	    } catch (IOException e) {
		hr.request.log(Server.LOG_ERROR, "reading init script", script);
		return false;
	    } catch (TclException e) {
		e.printStackTrace();
		if (e.getCompletionCode() != TCL.RETURN) {
		    hr.request.log(Server.LOG_ERROR, "loading java package",
			    e.toString());
		    return false;
		}
	    }
	}

	debug = (props.getProperty(hr.prefix + DEBUG) != null);

	try {
	    chan = new TclTemplateChannel(interp, hr);
	    request = ReflectObject.newInstance(interp, Request.class,
		    hr.request);
	    interp.eval("set request " + request);
	} catch (TclException e) {
	    hr.request.log(Server.LOG_WARNING, hr.prefix,
		    "Setting up TCL environ: " + e.toString());
	    done(hr);
	    return false;
	}
	return true;
    }

    /**
     * Called after the HTML document has been processed.
     * <p>
     * Releases the resources allocated by <code>init</code>.  This method
     * should be called to ensure that the <code>HtmlRewriter</code> and
     * all its attendant data structures are not preserved indefinitely
     * (until the next request).
     *
     * @param	hr
     *		The request and associated HTML document that was processed.
     *
     * @return	<code>true</code> always, indicating that this document was
     *		successfully processed to completion.
     */
    public boolean
    done(RewriteContext hr)
    {
	if (chan != null) {
	    chan.unregister();
	}
	if (request != null) {
	    request.release();
	}
	chan=null;
	request=null;
	return true;
    }
	
    /**
     * Processes the <code>&lt;server&gt;</code> tag.  Substitues the
     * result of evaluating the following Tcl script into the resultant
     * HTML document.
     * <p>
     * Note: Currently, there is no mechanism for other language interpreters
     * to share the same <code>server</code> tag.  Use the
     * <code>&lt;tcl&gt;</code> tag instead.
     *
     * @param	hr
     *		The request and associated HTML document that will be
     *		processed.
     */

    public void
    tag_server(RewriteContext hr) {
	String server = hr.getBody();
	if ("tcl".equals(hr.get("language")) == false) {
	    return;
	}
	tag_tcl(hr);
    }

    /**
     * Processes the <code>&lt;tcl&gt;</code> tag.  Substitues the
     * result of evaluating the following Tcl script into the resultant
     * HTML document.
     *
     * @param	hr
     *		The request and associated HTML document that will be
     *		processed.
     */

    public void
    tag_tcl(RewriteContext hr) {
	debug(hr);
	boolean eval = hr.isTrue("eval");

	if (newPage) {
	    newPage=false;
	    setup(hr);
	}

	hr.accumulate(false);
	hr.nextToken();
	String script = hr.getBody();
	if (eval) {
	    script =  Format.subst(hr.request.props, script,true);
	}
	try {
	    interp.eval(script);
	} catch (TclException e) {
	    if (e.getCompletionCode() == TCL.RETURN) {
		String result = interp.getResult().toString();
		if (result.length() > 0) {
		    hr.append(result);
		}
	    } else {
		hr.append("\n<!-- server-side Tcl: error code " +
			e.getCompletionCode() + ": " +
			interp.getResult() + " -->\n");
		if (debug) {
		    TclObject errorInfo;
		    try {
			errorInfo = interp.getVar("errorInfo", null,
				TCL.GLOBAL_ONLY);
		    } catch (Exception e2) {
			errorInfo = null;
		    }
		    hr.append("\n<!-- server-side Tcl: error info: " +
			    errorInfo + " -->\n");
		}
	    }
	}

	hr.nextToken();
	if (debug) {
	    hr.append("<!-- " + hr.getBody() + " -->");
	}
	hr.accumulate(true);
    }
}
