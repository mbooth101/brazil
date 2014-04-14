/*
 * BServletRequest.java
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
 * The Initial Developer of the Original Code is: drach.
 * Portions created by drach are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): drach, suhler.
 *
 * Version:  2.3
 * Created by drach on 01/06/01
 * Last modified by suhler on 04/11/30 15:19:41
 *
 * Version Histories:
 *
 * 2.3 04/11/30-15:19:41 (suhler)
 *   fixed sccs version string
 *
 * 2.2 03/07/23-13:13:47 (suhler)
 *   - recompute content-length if html output is modified
 *   - don't emit the Brazil server string
 *   - remove the servlet context from the request.url (save the original in
 *   url.servlet)
 *
 * 2.1 02/10/01-16:39:15 (suhler)
 *   version change
 *
 * 1.6 01/08/13-09:10:45 (drach)
 *   Change server.props back to Properties object.
 *
 * 1.5 01/08/03-15:44:22 (drach)
 *   Add PropertiesList
 *
 * 1.4 01/06/11-17:06:07 (suhler)
 *   fix javadoc errors
 *
 * 1.3 01/06/05-22:38:56 (drach)
 *   package move
 *
 * 1.2 01/06/04-14:41:37 (suhler)
 *   package move
 *
 * 1.2 01/06/01-16:31:01 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 servlet/BServletRequest.java
 *
 * 1.1 01/06/01-16:31:00 (drach)
 *   date and time created 01/06/01 16:31:00 by drach
 *
 */

package sunlabs.brazil.servlet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.UnknownHostException;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sunlabs.brazil.handler.MapPage;

import sunlabs.brazil.properties.PropertiesList;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

/**
 *
 * This is a subclass of <code>Request</code> that is used by
 * <code>BrazilServlet</code> to map servlet style requests and responses
 * to and from Brazil style requests.
 *
 * @author	Steve Drach <drach@sun.com>
 * @version		2.3
 *
 * @see         BServletSocket
 * @see         BServletServerSocket
 * @see         BrazilServlet
 * @see         sunlabs.brazil.server.Request
 */
public class BServletRequest extends Request
{
    HttpServletRequest req;
    HttpServletResponse res;

    /**
     * Created a new http request.
     *
     * @param	server the server that owns this request.
     * @param	req the servlets request object
     * @param   res the servlets response object
     */
    BServletRequest(Server server, HttpServletRequest req, HttpServletResponse res)
	throws UnknownHostException, IOException
    {
	super(server, new BServletSocket(req, res));

	this.req = req;
	this.res = res;

	// Need to do following to assure use of our HttpOutputStream.
	try {
	    out = new HttpOutputStream(new BufferedOutputStream(sock.getOutputStream()));
	} catch (IOException e) {}
    }

    /**
     * Converts an HTTP request from the form encapsulated in a servlet
     * request object to the form expected by Brazil handlers.
     *
     * @return	<code>true</code> if the request was successfully read and
     *		parsed, <code>false</code> if the request was malformed.
     *
     * @throws	IOException
     *		if there was an IOException reading from the socket.  See
     *		the socket documentation for a description of socket
     *		exceptions.
     */
    public boolean getRequest() throws IOException
    {
	if (server.props.get("debugProps") != null) {
	    PropertiesList.debug = true;
	    if (props != null) {
		props.dump(false, "at beginning of getRequest");
	    }
	}

	/*
	 * Reset state.
	 */
	 
	requestsLeft--;
	connectionHeader = "Connection";
	/* I don't think we need to do this
	while ((props = server.props.getPrior()) != null) {
	    props.remove();
	}
	*/

	String origUrl = req.getRequestURI().trim();
	int strip = req.getContextPath().length();
	url = origUrl.substring(strip);

	method = req.getMethod().trim();
	query = req.getQueryString();
	if (query != null)
	    query = query.trim();
	else
	    query = "";
	protocol = req.getProtocol().trim();
	headers.clear();
	postData = null;

	statusCode = 200;
	statusPhrase = "OK";
	responseHeaders.clear();
	startMillis = System.currentTimeMillis();
	out.bytesWritten=0;

	log(Server.LOG_LOG, "Request " + requestsLeft + " " + toString());

	if (protocol.equals("HTTP/1.0")) {
	    version = 10;
	} else if (protocol.equals("HTTP/1.1")) {
	    version = 11;
	    // Should we turn off chunked transfer encoding?
	} else {
	    sendError(505, toString(), null);
	    return false;
	}
	    
	Enumeration names = req.getHeaderNames();
	while (names.hasMoreElements()) {
	    String name = (String)names.nextElement();
	    headers.put(name, req.getHeader(name));
	}

	/*
	 * Remember POST data.  "Transfer-Encoding: chunked" is not handled
	 * yet.
	 */

	String str;

	str = getRequestHeader("Content-Length");
	if (str != null) {
	    try {
		postData = new byte[Integer.parseInt(str)];
	    } catch (Exception e) {
		sendError(411, str, null);
		return false;
	    } catch (OutOfMemoryError e) {
		sendError(411, str, null);
		return false;
	    }
	    in.readFully(postData);
	}

	str = getRequestHeader(connectionHeader);
	if ("Keep-Alive".equalsIgnoreCase(str)) {
	    keepAlive = true;
	} else if ("close".equalsIgnoreCase(str)) {
	    keepAlive = false;
	} else if (version > 10) {
	    keepAlive = true;
	} else {
	    keepAlive = false;
	}

	/*
         * Delay initialization until we know we need these things
         */
	serverProps = new PropertiesList(server.props);
	props = new PropertiesList();
	props.addBefore(serverProps);
	props.put("url.servlet", origUrl);

	return true;
    }

    /**
     *
     * Sets response code in the servlet response object.
     */  
    public void setStatus(int code)
    {
	if (code < 0)
	    return;
	super.setStatus(code);
	res.setStatus(code);
    }

    /**
     * 
     * Returns the server's fully-qualified base URL. This is "http://"
     * followed by the server's hostname and port.
     *
     * <P>If the HTTP request header "Host" is present, it specifies the
     * hostname and port that will be used instead of the server's
     * internal name for itself. Due to bugs in certain browsers, when
     * using the server's internal name, the port number will be elided
     * if it is 80.
     *
     * @return The string representation of the server's URL.
     */
    public String serverUrl()
    {
	String host = headers.get("Host");
	if (host == null) {
	    host = server.hostName;
	}
	int port = req.getServerPort();
	if ((host.lastIndexOf(":") < 0) && (port != 80)) {
	    host += ":" + port;
	}
	return server.protocol + "://" + host;
    }

    /**
     *
     * An <code>HttpOutputStream</code> that is used by the Brazil Servlet
     * adaptor to add the web application
     * context path to the front of all response URI's so subsequent requests
     * will be mapped to the <code>BrazilServlet</code>'s context. See comments
     * in <code>BrazilServlet</code> that describe the assumptions regarding
     * the context path.
     *
     * @author	Steve Drach <drach@sun.com>
     * @version	2.3, 04/11/30
     *
     * @see      BrazilServlet
     */
    public static class HttpOutputStream extends Request.HttpOutputStream {

	private ByteArrayOutputStream baos;
	private String contextPath;
	private BServletRequest req;

	public HttpOutputStream(OutputStream out) {
	    super(out);
	}

	/**
	 *
         * If the response content type is text/html, this method
         * interposes a buffer before the response stream that
         * gathers all content for subsequent URI rewriting.
         */
	public void sendHeaders(Request request) throws IOException {
	    this.req = (BServletRequest)request;
	    Enumeration e = request.responseHeaders.keys();
	    while (e.hasMoreElements()) {
		String name = (String)e.nextElement();
		if (name.toLowerCase().equals("server")) {
		    continue;	// we're not the server any more
		}
		req.res.addHeader(name, request.responseHeaders.get(name));
	    }

	    // Need to remap URI's in HTML content
	    String type = request.responseHeaders.get("Content-Type");
	    if (type != null && type.equals("text/html")) {
		baos = new ByteArrayOutputStream(request.server.bufsize);
		out = new HttpOutputStream(baos);
		contextPath = req.props.getProperty("context_path") + "/";
	    }
	}

	/**
	 *
         * Remaps URI's in the buffered HTML content and the writes the
         * result to the response stream.
         */
	public void close() throws IOException {
	    if (contextPath == null) {
		super.close();
		return;
	    }
	    MapPage map = new MapPage(contextPath);
	    String s = map.convertHtml(baos.toString());
	    // System.err.println("Rewrote: " + map.mapCount());
	    req.res.setHeader("Content-Length", "" + s.length());
	    OutputStream out = req.sock.getOutputStream();
	    out.write(s.getBytes());
	    out.close();
	}
    }

    public void
    redirect(String url, String body)
	throws IOException
    {
	System.out.println("Redirect to " + url);
	super.redirect(url, body);
    }
}
