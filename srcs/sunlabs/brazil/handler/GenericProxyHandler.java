/*
 * GenericProxyHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1998-2007 Sun Microsystems, Inc.
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
 * Contributor(s): cstevens, rinaldo, suhler.
 *
 * Version:  2.5
 * Created by suhler on 98/09/14
 * Last modified by suhler on 07/03/21 15:53:25
 *
 * Version Histories:
 *
 * 2.5 07/03/21-15:53:25 (suhler)
 *   add call to hand over our pageMapper
 *
 * 2.4 07/01/29-15:34:58 (suhler)
 *   - added passHost flag to pass the origin http "host" header instead of the
 *   target one
 *   - added noErrorReturn flag.  If set, the handler returns false instead of
 *   returning an error code to the client
 *   - made MapPage an instance variable (WARNING: this effects sub-classes)
 *   .
 *
 * 2.3 07/01/14-15:14:18 (suhler)
 *   diagniostic fixes
 *
 * 2.2 06/11/13-15:03:29 (suhler)
 *   - pass response status from target to client
 *   - change system..println to Log()
 *
 * 2.1 02/10/01-16:36:22 (suhler)
 *   version change
 *
 * 1.32 02/08/01-13:45:55 (suhler)
 *   change "requestHeaders" to "headers", and use the same syntax/semantics as
 *   the PollHandler and IncludeTemplate.
 *   Make sure the host ehader is set and nut duplicated
 *
 * 1.31 02/07/24-10:45:44 (suhler)
 *   doc updates
 *
 * 1.30 01/09/23-17:35:44 (suhler)
 *   Added "requestheaders" properties, allowing arbitrary http headers
 *   to be sent to the up-stream server
 *
 * 1.29 01/01/16-14:25:54 (suhler)
 *   don't return from "finally" clauses
 *
 * 1.28 00/12/11-13:26:44 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.27 00/05/31-13:45:28 (suhler)
 *   doc cleanup
 *
 * 1.26 00/04/20-11:48:04 (cstevens)
 *   copyright.
 *
 * 1.25 00/03/29-14:34:26 (cstevens)
 *   unused method
 *
 * 1.24 00/03/10-17:02:08 (cstevens)
 *   Removing unused member variables
 *
 * 1.23 00/02/25-09:02:47 (suhler)
 *   temporary patches to fix redirection and "host" problems.
 *
 * 1.22 00/02/11-12:42:23 (suhler)
 *
 * 1.21 00/02/08-10:00:32 (suhler)
 *   Missing else, caused crashes
 *
 * 1.20 00/02/03-14:24:20 (suhler)
 *   better diagnostics
 *
 * 1.19 00/02/02-14:44:57 (suhler)
 *   turn on url mapping diagnostics if log>5
 *
 * 1.18 99/10/26-18:50:42 (cstevens)
 *   case
 *
 * 1.17 99/10/26-17:06:48 (cstevens)
 *   Get rid of public variables Request.server and Request.sock
 *   In all cases, Request.server was not necessary in the Handler.
 *   Request.sock was changed to Request.getSock(); it is still rarely used for
 *   diagnostics and loggin (e.g., ChainSawHandler).
 *   Change GenericProxyHandler (which actually filters data from other sites, and
 *   is not a generic proxy) to use Request.sendResponse(InputStream, ...) to
 *   stream the data, instead of fetching the entire contents into a big byte array
 *   and then sending the byte array to Request.sendResponse(byte[], ...).
 *
 * 1.16 99/10/14-14:57:02 (cstevens)
 *   resolve wilcard imports.
 *
 * 1.15 99/10/06-12:21:09 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.13.1.1 99/10/06-12:16:39 (suhler)
 *   rewrite to use caching proxy (not complete)
 *
 * 1.14 99/10/01-11:25:46 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.13 99/09/15-14:38:36 (cstevens)
 *   Rewriting http server.
 *
 * 1.12 99/09/01-12:15:12 (suhler)
 *   add a flush() to make compatible with TailHandler
 *
 * 1.11 99/06/28-10:48:07 (suhler)
 *   re-written to separate out link rewriting stuff
 *
 * 1.10 99/05/25-09:19:28 (suhler)
 *   revert to previous version. HtmlMunge was fixed accordingly
 *
 * 1.9 99/05/24-19:10:36 (suhler)
 *   chenged to use HtmlMunge semantics
 *
 * 1.8 99/03/30-09:29:01 (suhler)
 *   documentation update
 *
 * 1.7 98/11/18-14:53:34 (suhler)
 *   Lost this in the re-write.  Allow subclasses to return "false" by setting
 *   the content to null in modifyContent()
 *
 * 1.6 98/11/18-13:57:03 (suhler)
 *   Re-wrote proxy code to fixe suspected (but not confirmed)
 *   thread race conditions by eliminating all instance variable that
 *   are "writable" by respond()
 *
 * 1.5 98/11/15-15:30:44 (rinaldo)
 *   Create new Distribution
 *
 * 1.4 98/11/08-10:27:58 (suhler)
 *   Don't pass connection header through proxy.
 *   Somewhere there must be a list of headers that should not be
 *   proxied.  Some day
 *
 * 1.3 98/09/21-14:53:50 (suhler)
 *
 * 1.2 98/09/20-15:37:32 (suhler)
 *   - make sure url prefix has leading /
 *   - elide unused variable
 *   .
 *
 * 1.2 98/09/14-18:03:05 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 handlers/GenericProxyHandler.java
 *   Name history : 1 0 GenericProxyHandler.java
 *
 * 1.1 98/09/14-18:03:04 (suhler)
 *   date and time created 98/09/14 18:03:04 by suhler
 *
 */

package sunlabs.brazil.handler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.UnknownHostException;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.http.HttpRequest;
import sunlabs.brazil.util.http.MimeHeaders;
import sunlabs.brazil.util.Format;

/**
 * Handler for implementing a virtual web site.
 * This causes another web site to "appear" inside our document root.
 * This classes is intended to be sub-classed, so some of the methods
 * in this implementation don't do too much.
 *
 * All of the appropriate links in HTML documents on the virtual
 * site are rewritten, so they appear to be local references.
 * This can be used on a firewall in conjunction with
 * {@link AclSwitchHandler}
 * to provide authenticated access to selected web sites.
 * <p>
 * Properties:
 * <dl class=props>
 * <dt>prefix	<dd>URL prefix must match
 * <dt>host	<dd>name of host site to proxy to.
 * <dt>port	<dd>Host port to proxy to (defaults to 80).
 * <dt>proxyHost<dd>Which proxy host to use (if any)
 *		 to contact "host".
 * <dt>proxyPort<dd>The proxy's port (defaults to 80)
 * <dt>headers	<dd>A list of white space delimited tokens that refer to
 *		additional HTTP headers that are added onto the polled
 *		request.  For each token the server properties
 *		<code>[token].name</code> and <code>[token].value</code>
 *		define a new http header.
 * <dt>passHost <dd>If true, the original browser host string is passed to the
 *		target, otherwise the mapped hostname is used, in which case the
 *		http header "X-Host-Orig" will contain the original host name.
 * <dt>noErrorReturn
 * <dd>If true, then if the proxy request fails, the response method
 * returns "false", and places the reason for failure in the "errorCode"
 * and "errorMsg" request properties.
 * Otherwise, and error response is generated. The default is (erroneously)
 * false for historical reasons.
 * </dl>
 *
 * @author      Stephen Uhler
 * @version	2.5, 07/03/21
 */

public class GenericProxyHandler implements Handler {
//    protected Server server;
    protected String prefix;

    protected MapPage mapper = null;   // our url rewriter.
    protected String host;	   // The host containing the actual page
    protected int port;		   // The port for above (defaults to 80)
    protected String proxyHost;	   // The proxy server (if any)
    protected int proxyPort;	   // The proxy server's port (defaults to 80)
    protected String urlPrefix;	   // The url prefix that triggers this handler
    protected String requestPrefix;  // The host/port prefix
    protected String tokens;		// our tokens in server.props
    protected boolean passHost;		// if true, pass-thru the original http host header
    protected boolean noErrorReturn=false;  // if true.

    /**
     * Handler configuration property <b>prefix</b>.
     * Only URL's that begin with this string are considered by this handler.
     * The default is (/).
     */
    public static final String PREFIX = "prefix";   // URL prefix for proxy
    /**
     * Handler configuration property <b>host</b>.
     * The actual host site to appear on our site (required)
     */
    public static final String HOST = "host";     // The host to proxy these requests to
    /**
     * Handler configuration property <b>port</b>.
     * The actual port on the host site (defaults to 80).
     */
    public static final String PORT = "port";     // The host port to proxy these requests to
    /**
     * Handler configuration property <b>proxyHost</b>.
     * The name of a proxy to use (if any) to get to the <b>host</b>.
     */
    public static final String PROXY_HOST = "proxyHost";     // The proxy server (if any)
    /**
     * Handler configuration property <b>proxyPort</b>.
     * The proxy port to use to get to the <b>host</b>. defaults to 80.
     */
    public static final String PROXY_PORT = "proxyPort";     // The proxy server port (if any)
    public static final String NL = "\r\n";		// line terminator

    /**
     * Do one-time setup.
     * get and process the handler properties.
     * we can contact the server identified by the <b>host</b> parameter.
     */

    public boolean
    init(Server server, String prefix) {
//	this.server = server;
	this.prefix = prefix;

	if (server.logLevel > server.LOG_DIAGNOSTIC) {
	    MapPage.log = true;
	    HttpRequest.displayAllHeaders = true;
	}

	passHost=Format.isTrue(server.props.getProperty(prefix + "passHost"));
	noErrorReturn=Format.isTrue(server.props.getProperty(prefix +
	    "noErrorReturn"));
	System.out.println(prefix + ": passHost=" + passHost + 
		" noErrorReturn=" + noErrorReturn);

	/* XXX
	 * Need to add tag map entries here for a specific host
	 */

	host = server.props.getProperty(prefix + HOST);
	if (host == null) {
	    server.log(Server.LOG_WARNING, prefix, "no host to proxy to");
	    return false;
	}

	urlPrefix = server.props.getProperty(prefix + PREFIX, "/");
	if (urlPrefix.indexOf('/') != 0) {
	    urlPrefix = "/" + urlPrefix;
	}
	if (!urlPrefix.endsWith("/")) {
	    urlPrefix += "/";
	}

	port = 80;
	try {
	    String str = server.props.getProperty(prefix + PORT);
	    port = Integer.decode(str).intValue();
	} catch (Exception e) {}

	proxyHost = server.props.getProperty(prefix + PROXY_HOST);
	proxyPort = 80;
	try {
	    String str = server.props.getProperty(prefix + PROXY_PORT);
	    proxyPort = Integer.decode(str).intValue();
	} catch (Exception e) {}
	
	if (port == 80) {
	    requestPrefix = "http://" + host;
	} else {
	    requestPrefix = "http://" + host + ":" + port;
	}
	tokens = server.props.getProperty(prefix + "headers");
        mapper = new MapPage(urlPrefix);
	return true;
    }

    /**
     * If this is one of "our" url's, fetch the document from
     * the destination server, and return it as if it was local.
     */

    public boolean
    respond(Request request)
    throws IOException {
    	if (!isMine(request)) {
	    return false;
	}	

	String url = request.url.substring(urlPrefix.length());
	if (!url.startsWith("/")) {
	    url = "/" + url;
	}

	if (!request.query.equals("")) {
	    url += "?" + request.query;
	}
	request.log(Server.LOG_DIAGNOSTIC,
			"  Request headers: " +  request.headers);

	HttpRequest target = new HttpRequest(requestPrefix + url);
	// System.out.println("Fetching: " + requestPrefix + url);
	if (proxyHost != null) {
	    target.setProxy(proxyHost, proxyPort);
	}
	target.setMethod(request.method);

	HttpRequest.removePointToPointHeaders(request.headers, false);
        request.headers.remove("if-modified-since");  // wrong spot XXX
	request.headers.copyTo(target.requestHeaders);

	/* XXX This doesn't belong here! - the proxy should do it */

	if (!passHost) {
	    String orig = target.requestHeaders.get("host");
	    if (orig != null) {
		target.requestHeaders.remove("host");
		target.requestHeaders.put("X-Host-Orig", orig);
	    }
	}

	if (tokens != null) {
	    target.addHeaders(tokens, request.props);
	}
	target.requestHeaders.putIfNotPresent("Host",host);

        boolean code=true;
	try {
	    if (request.postData != null) {
		OutputStream out = target.getOutputStream();
		out.write(request.postData);
		out.close();
	    }

	    // Connect to target and read the response headers

	    request.log(Server.LOG_DIAGNOSTIC, prefix,
		"fetching " + target.url + "...");
	    target.connect();
	    request.log(Server.LOG_DIAGNOSTIC, prefix,
		"... Got response code: " + target.status);
	    HttpRequest.removePointToPointHeaders(target.responseHeaders, true);
	    target.responseHeaders.copyTo(request.responseHeaders);

	    // Now filter the output, writing the header and content if true

	    request.log(Server.LOG_DIAGNOSTIC,
			"  Response headers: " +  target.responseHeaders);
	    if (shouldFilter(request.responseHeaders)) {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		target.getInputStream().copyTo(out);

		request.log(Server.LOG_DIAGNOSTIC,
			"  parsing/modifying " + out.size() + " bytes");
		byte[] content = modifyContent(request, out.toByteArray());

		if (content == null) {	// This is wrong!!
		    request.log(Server.LOG_DIAGNOSTIC,
			    "  null content, returning false");
		    code=false;
		} else {
		    String type = request.responseHeaders.get("Content-Type");
		    request.setStatus(target.getResponseCode());
		    request.sendResponse(content, null);
		}
	    } else {
		request.log(Server.LOG_DIAGNOSTIC, "Delivering normal content");
		request.sendResponse(target.getInputStream(),
			target.getContentLength(), null,
			target.getResponseCode());
	    }
        } catch (InterruptedIOException e) {
            /*
             * Read timeout while reading from the remote side.  We use a
             * read timeout in case the target never responds.
             */
	    code = doError(request, 408, "Timeout / No response");
        } catch (UnknownHostException e) {
	    code = doError(request, 503, urlPrefix +  " Not reachable");
        } catch (ConnectException e) {
	    code = doError(request, 500, "Connection refused");
	} catch (IOException e) {
	    code = doError(request,500, "Error retrieving response: " + e);
	    e.printStackTrace();
        } finally {
            target.close();
	    // System.out.println("Finally (proxy): " + code);
        }
	return code;
    }

    /*
     * Either send an error response, set the properties
     */

    boolean doError(Request request, int code, String msg) {
	if (noErrorReturn) {
	    request.props.put(prefix + "errorCode", "" + code);
	    request.props.put(prefix + "errorMsg", "" + msg);
	    return false;
	} else {
	    request.sendError(code, msg);
	    return true;
	}
    }

    /**
     * See if the content needs to be filtered.
     * Return "true" if "modifyContent" should be called
     * @param headers	Vector of mime headers for data to proxy
     */

    protected boolean shouldFilter(MimeHeaders headers) {
	String type = headers.get("Content-Type");
	// System.out.println("Modify?? " + type);
     	return (headers.get("location") != null || 
		(type != null && type.indexOf("text/html") >= 0));
    }

    /**
     * See if this is one of my requests.
     * This method can be overridden to do more sophisticated mappings.
     * @param request		The standard request object
     */

    public boolean isMine(Request request) {
	return  request.url.startsWith(urlPrefix);
    }

    /**
     * Return a reference to our page mapper, to allow futzing with the page
     * maps from the outside
     */

    public MapPage getMapper() {
	 return mapper;
    }

    /**
     * Rewrite the links in an html file so they resolve correctly
     * in proxy mode.
     *
     * @param request	The original request to this "proxy"
     * @param headers	The vector of mime headers for the proxy request
     * @return	 	true if the headers and content should be sent to the client, false otherwise
     *			Modifies "headers" as a side effect
     */

    public byte[] modifyContent(Request request, byte[] content) {
	byte[] result;
	result = mapper.convertHtml(new String(content)).getBytes();

	/*
	 * Now fix the content length
	 */

	request.responseHeaders.put("Content-Length", result.length);

	/*
	 * Rewrite the location header, if any
	 */

	String location = request.responseHeaders.get("location");
	if (location != null) {
	    request.setStatus(302);	// XXX doesn't belong here, should copy
						// targets status
	    String fixed = mapper.convertString(location);
	    if (fixed != null) {
		String newLocation = request.serverUrl() + fixed;
		request.responseHeaders.put("Location", newLocation);
	    }
	}

	/*
	 * Fix the domain specifiers on set-cookie requests
	 */

	String cookie = request.responseHeaders.get("set-cookie");
	if (cookie != null) {
	    System.out.println("Need to map: " + cookie);
	}

	return result;
    }
}
