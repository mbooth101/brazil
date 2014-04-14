/*
 * ReflectHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1998-2002 Sun Microsystems, Inc.
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
 * Contributor(s): cstevens, suhler.
 *
 * Version:  2.1
 * Created by suhler on 98/09/14
 * Last modified by suhler on 02/10/01 16:36:23
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:36:23 (suhler)
 *   version change
 *
 * 1.9 00/05/31-13:45:51 (suhler)
 *   doc cleanup
 *
 * 1.8 00/04/20-11:48:19 (cstevens)
 *   copyright.
 *
 * 1.7 00/04/12-15:52:30 (cstevens)
 *   imports
 *
 * 1.6 00/03/29-14:34:44 (cstevens)
 *   pedantic
 *
 * 1.5 99/09/15-14:39:08 (cstevens)
 *   Rewritign http server to make it easier to proxy requests.
 *
 * 1.4 99/03/30-09:30:06 (suhler)
 *   documentation update
 *
 * 1.3 99/02/17-17:13:17 (cstevens)
 *   ReflectHandler only reflected query data if query was specified as GET and
 *   not as POST.
 *
 * 1.2 98/09/21-14:54:11 (suhler)
 *   changed the package names
 *
 * 1.2 98/09/14-18:03:08 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 handlers/ReflectHandler.java
 *   Name history : 1 0 ReflectHandler.java
 *
 * 1.1 98/09/14-18:03:07 (suhler)
 *   date and time created 98/09/14 18:03:07 by suhler
 *
 */

package sunlabs.brazil.handler;

import sunlabs.brazil.server.Handler; // Interface that defines a handler
import sunlabs.brazil.server.Request; // Encapulates an http request
import sunlabs.brazil.server.Server;  // Contains data specific to the server

import sunlabs.brazil.util.http.HttpUtil;

import java.io.IOException;            // Request methods can throw this

/* Other classes used by this handler */

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Handler for reflecting query data back to the client.
 * This is the example handler to demonstrate how a typical
 * handler is witten.  If query data is present, it is formatted into
 * an HTML table, and displayed to the user.
 *
 * @author		Stephen Uhler
 * @version		2.1, 02/10/01
 */

public class ReflectHandler implements Handler {

	/**
	 * Initialize the handler.
	 * Handler objects are created by the server using newInstance().
	 * The init method is called first, and exactly one for each instance,
	 * and may be used for one-time initializations.
	 * This handler doesn't require any.
	 *
	 * @param server  A reference to the server.
	 * @param prefix  A string identifying this instance of the
	 *                handler.  It is used by the
	 *		  {@link sunlabs.brazil.server.ChainHandler} to
	 *                provide the prefix to be prepended onto each
	 *                property intended for this handler.
	 * @return true  Only if the handler is successfully initialized.
	 */

	public boolean
	init(Server server, String prefix)
	{
	    return true;
	}

	/** 
	 * Dispatch and handle the request.
	 * This version justs reflects the HTTP header information.
	 * It is commonly placed downstream of the
	 * {@link CgiHandler} to allow HTML forms to be tested before
	 * the cgi script is written.
	 * @param request  The request object contains all of the information
	 *                 about the request, as well as methods to manipulate
	 *                 it.  Although multiple threads may call this method
	 *                 connurrently, each will have its own request object.
	 */

	public boolean
	respond(Request request)
	    throws IOException
	{

		//  If there is no query information, return false, allowing
		//  another handler in the chain to process the request.

	        Dictionary table = request.getQueryData(null);
		if (table.size() == 0) {
			return false;
		}
		String queryTable = formatTable(table,"Query Data");

		String result = "<title>Request Reflector</title>"
				+ "<h1>Request Summary</h1>"
				+ queryTable
				+ formatTable(request.headers,"Http Data")
				+ "<h1>Other  Stuff</h1>"
				+ "method=<b>" + request.method + "</b><br>"
				+ "url=<b>" + request.url + "</b><br>"
				+ "version=<b>" + request.protocol
				+ "</b><br>"
				+ formatTable(request.props, "Server State");
		request.sendResponse(result);
		return true;
	}

	/**
	 * Turn a hash table into html format.  This is a static method
	 * so it may be used in other handlers.
	 * @param table		The table to format
	 * @return		The html fragment
	 */

	public static String formatTable(Dictionary data, String caption) {
		StringBuffer html = new StringBuffer();
		html.append("<table border=1>\r\n");
		html.append("<caption>" + caption + "</caption>\r\n");
		Enumeration keys = data.keys();
		while(keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			html.append("<tr><th>"
				+ HttpUtil.htmlEncode(key)
				+ "</th><td>"
				+ HttpUtil.htmlEncode((String) data.get(key))
				+ "</td></tr>\r\n");
		}
		html.append("</table>\r\n");
		return html.toString();
	}
}
