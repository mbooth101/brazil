/*
 * DynamicConfigHandler.java
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
 * The Initial Developer of the Original Code is: cstevens.
 * Portions created by cstevens are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): cstevens, suhler.
 *
 * Version:  2.1
 * Created by cstevens on 00/03/02
 * Last modified by suhler on 02/10/01 16:36:37
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:36:37 (suhler)
 *   version change
 *
 * 1.6 01/01/22-19:02:52 (cstevens)
 *   Remove RechainableProperties from Request.  In order to insert shared
 *   Properties objects, use request.addSharedProps() instead of manually
 *   rechaining the properties using props.setDefaults().
 *
 * 1.5 00/12/11-13:28:41 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.4 00/04/12-15:54:25 (cstevens)
 *   imports
 *
 * 1.3 00/03/29-16:09:02 (cstevens)
 *   Eliminated dummy server from DynamicConfigHandler.
 *
 * 1.2 00/03/10-17:09:14 (cstevens)
 *   Uses rechainable Properties object to insert new Server properties behind
 *   the Request properties before dispatching to the wrapped handler's respond
 *   method.
 *
 * 1.2 00/03/02-17:43:41 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/DynamicConfigHandler.java
 *
 * 1.1 00/03/02-17:43:40 (cstevens)
 *   date and time created 00/03/02 17:43:40 by cstevens
 *
 */

package sunlabs.brazil.handler;

import sunlabs.brazil.server.ChainHandler;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

/**
 * The <code>DynamicConfigHandler</code> allows the user to change the
 * configuration of the server and its handlers on the fly.  This handler
 * can therefore be used to swap in and out functionality, for example,
 * by dynamically adding a new <code>AuthHandler</code> to add a new
 * form of authentication to a running server.
 * <p>
 * This handler uses a special set of URLs to allow a new set of
 * configuration properties to be uploaded.  The new configuration
 * replaces the old configuration.
 * <p>
 * The name of another <code>Handler</code> is supplied when this
 * <code>DynamicConfigHandler</code> is initialized.  This <code>Handler</code>
 * is the helper or sub-handler for the <code>DynamicConfigHandler</code>.
 * When the <code>DynamicConfigHandler</code> receives a regular HTTP
 * request (that matches the URL prefix described below), it redirects
 * that request to the <code>respond</code> method of the sub-handler.  
 * <p>
 * The uploaded configuration properties are kept in a separate properties
 * object from the server's properties.  The server's properties
 * are in fact not accessible from the sub-handler; the sub-handler can
 * only access and/or change the properties owned by the
 * <code>DynamicConfigHandler</code>.
 * <p>
 * This handler uses the following configuration properties: <dl class=props>
 *
 * <dt> handler
 * <dd> The name of the initial sub-handler that this
 *	<code>DynamicConfigHandler</code> will use to process requests.  When
 *	new properties are uploaded, the sub-handler will be replaced with
 *	whatever is specified in the newly uploaded <code>handler</code>
 *	property.
 *
 * <dt> prefix
 * <dd> Only URLs beginning with this string will be redirected to the
 *	sub-handler.  This property belongs to the
 *	<code>DynamicConfigHandler</code> and is <b>not</b> changed when
 *	new properties are uploaded.  The default is "/".
 *
 * <dt> config
 * <dd> URLs beginning with this prefix can be used to upload or download
 *	new configuration properties to this handler, which will also
 *	dynamically change which sub-handler is installed.  This property
 *	belongs to the <code>DynamicConfigHandler</code> and is <b>not</b>
 *	changed when new properties are uploaded.  The default
 *	is "/config/".
 *      <p>
 *	Properties may be uploaded by sending them as "name=value" pairs in
 *	the body of a POST or in the "?" query parameters.  The URL for
 *	uploading properties is "<i>config</i>/set".
 *	<p>
 *	The current set of properties may be retrieved from this handler by
 *	sending the URL "<i>config</i>/get"
 * </dl>
 *
 * A sample set of configuration parameters illustrating how to use this
 * handler follows:
 * <pre>
 * handler=sunlabs.brazil.server.ChainHandler
 * port=8081
 * log=5
 *
 * handlers=dyn cgi
 *
 * dyn.class=sunlabs.brazil.server.DynamicConfigHandler
 * dyn.prefix=/sparky/
 * dyn.config=/config-sparky/
 * dyn.handler=chain
 *
 * chain.class=sunlabs.brazil.server.ChainHandler
 * chain.handlers=foo baz garply
 *
 * foo.class=sunlabs.brazil.handler.HomeDirHandler
 * foo.home=/home/users/
 *
 * baz.class=sunlabs.brazil.handler.FileHandler
 *
 * garply.class=sunlabs.brazil.handler.NotFoundHandler
 * garply.root="/errors/"
 * garply.fileName="nofile.html"
 *
 * cgi.class = sunlabs.brazil.handler.CgiHandler
 * .
 * .
 * .
 * </pre>
 * These parameters set up a normal <code>Server</code> on port 8081,
 * running a <code>ChainHandler</code> which dispatches to a
 * <code>DynamicConfigHandler</code> and a <code>CgiHandler</code>.
 * <p>
 * The <code>DynamicConfigHandler</code> will listen for HTTP requests
 * beginning with "/sparky/" and dispatch to its dynamically generated
 * list of handlers, and listen for requests beginning with "/config-sparky/"
 * to dynamically change that set of handlers.
 * <p>
 * To give this <code>DynamicConfigHandler</code> something to do, an initial
 * set of handlers is provided with the same prefix ("dyn") as the
 * <code>DynamicConfigHandler</code> itself.  The prefix is stripped off
 * those properties and the revised set of properties is passed to the
 * <code>DynamicConfigHandler</code> to initialize its dynamically
 * configurable world.
 *
 *
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version	2.1, 02/10/01
 */
public class DynamicConfigHandler
    implements Handler
{
    private static final String URL_PREFIX = "prefix";
    private static final String CONFIG_PREFIX = "config";
    private static final String HANDLER = "handler";
    
    private static final String CONFIG_SET = "/set";
    private static final String CONFIG_GET = "/get";

    String urlPrefix = "/";
    String configPrefix = "/config";

    Server server;
    String prefix;
    Properties props;

    String name;
    Handler handler;

    /**
     * Initializes this <code>DynamicConfigHandler</code> by loading the
     * initial handler.  An initial handler does not need to be defined,
     * however, since the handler configuration can be downloaded later.
     *
     * @param	server
     *		The HTTP server that created this handler.
     *
     * @param	prefix
     *		A prefix to prepend to all of the keys that this
     *		handler uses to extract configuration information.
     *
     * @return	<code>false</code> if the initial handler was specified but
     *		could not be initialized, <code>true</code> otherwise.
     */
    public boolean
    init(Server server, String prefix)
    {
	this.server = server;
	this.prefix = prefix;
	this.props = server.props;

	urlPrefix = props.getProperty(prefix + URL_PREFIX, urlPrefix);
	configPrefix = props.getProperty(prefix + CONFIG_PREFIX, configPrefix);

	name = props.getProperty(prefix + HANDLER);
	if (name == null) {
	    return true;
	}
	handler = ChainHandler.initHandler(server, prefix + HANDLER + ".",
		name);

	return (handler != null);
    }

    /**
     * Responds to an HTTP request by examining the "Host:" request header
     * and dispatching to the main handler of the server that handles
     * that virtual host.  If the "Host:" request header was not specified,
     * or named a virtual host that was not initialized in <code>init</code>
     * from the list of virtual hosts, this method returns without
     * handling the request.
     *
     * @param	request
     *		The HTTP request to be forwarded to one of the sub-servers.
     *
     * @return	<code>true</code> if the sub-server handled the message,
     *		<code>false</code> if it did not.  <code>false</code> is
     *		also returned if the "Host:" was unspecified or unknown.
     */
    public boolean
    respond(Request request)
	throws IOException
    {
	if (request.url.startsWith(configPrefix)) {
	    if (request.url.endsWith(CONFIG_GET)) {
		request.keepAlive = false;
		request.sendHeaders(200, "text/plain", -1);
		props.save(request.out, null);
		return true;
	    } else if (request.url.endsWith(CONFIG_SET)) {
		Properties local = new Properties(server.props);
		request.getQueryData(local);

		String name = local.getProperty(HANDLER, "");
		Handler h = ChainHandler.initHandler(server, "", name);

		String result = "result=properties uploaded";
		if (h == null) {
		    result = "error=properties malformed";
		} else {
		    handler = h;
		    props = local;
		}
		request.sendResponse(result, "text/plain");
		return true;
	    }
	} else if (request.url.startsWith(urlPrefix)) {
	    request.log(Server.LOG_DIAGNOSTIC, prefix,
		    "invoking handler: " + name);

	    /*
	     * XXX Defaults for my props are the original server props.
	     * Defaults for request's props are ????
	     *
	     * 1. Should my defaults be null instead?
	     * 2. Should make my defaults be the ???? from the request.
	     *    (Threading issue: concurrent requests w/ differing
	     *    defaults for their properties).
	     */

	    try {
		if (props != server.props) {
		    request.addSharedProps(props);
		}
		return handler.respond(request);
	    } finally {
		request.removeSharedProps(props);
	    }
	}
	return false;
    }
}

	
