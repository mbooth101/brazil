/*
 * SupplyHandler.java
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
 * Last modified by suhler on 02/10/01 16:36:24
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:36:24 (suhler)
 *   version change
 *
 * 1.9 01/08/14-16:38:05 (suhler)
 *   doc lint
 *
 * 1.8 00/12/11-13:27:02 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.7 00/04/20-11:48:28 (cstevens)
 *   copyright.
 *
 * 1.6 99/10/14-14:57:12 (cstevens)
 *   resolve wilcard imports.
 *
 * 1.5 99/10/01-11:26:00 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.4 99/09/15-14:39:17 (cstevens)
 *   Rewritign http server to make it easier to proxy requests.
 *
 * 1.3 99/03/30-09:30:45 (suhler)
 *   documentation update
 *
 * 1.2 98/09/21-14:54:51 (suhler)
 *   changed the package names
 *
 * 1.2 98/09/14-18:03:11 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 handlers/SupplyHandler.java
 *   Name history : 1 0 SupplyHandler.java
 *
 * 1.1 98/09/14-18:03:10 (suhler)
 *   date and time created 98/09/14 18:03:10 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.StringTokenizer;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

/**
 * Sample Handler for dispatching different users to different url's
 * based on a special http authentication header.
 * This is a re-implementation of the supplier.net content server
 * using the new server apis (e.g. its not used for anything anymore).
 * This handler was originally designed to be a "virtual web site", where
 * credentials are passed in from an upstream proxy.  Those credentials are
 * used to provide different views based on the particular credentials supplied.
 *
 * The following configuration properties are used:
 * <dl class=props>
 *  <dt>mapFile	<dd> properties file
 *  <dt>prefix	<dd> url prefix
 *  <dt>default	<dd> default map
 *  <dt>header  <dd> http header (authentication)
 *  <dt>realm	<dd> The authentication realm (basic)
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		2.1, 02/10/01
 */

public class SupplyHandler implements Handler {
    private Properties map;		// The authorization mapping table
    private String propsPrefix;		// My prefix in the global properties file
    private String urlPrefix;		// The prefix to look for to map
    private String authHeader;		// header containing auth info
    private String realm;		// The authentication realm
    private static final String MAP = "mapFile";	// properties file
    private static final String PREFIX = "prefix";	// url prefix
    private static final String DEFAULT = "default";	// default map
    private static final String HEADER = "header";	// http header
    private static final String REALM = "realm";	// realm to look for

    public boolean
    init(Server server, String prefix) {
	propsPrefix = prefix;
	authHeader = server.props.getProperty(propsPrefix + HEADER,
		"authorization");
	realm = server.props.getProperty(propsPrefix + REALM, "basic");
	urlPrefix = server.props.getProperty(propsPrefix + PREFIX,"");
	if (urlPrefix.equals(""))  {
	    server.log(Server.LOG_WARNING, prefix, "handler can't find " + PREFIX);
	    return false;
	}
	if (!urlPrefix.endsWith("/")) {
	    urlPrefix += "/";
	}
	String mapFile = server.props.getProperty(propsPrefix + MAP,"");
	try {
	    FileInputStream in = new FileInputStream(mapFile);
	    map = new Properties();
	    map.load(in);
	    in.close();
	} catch (Exception e) {
	    server.log(Server.LOG_ERROR, propsPrefix + MAP, ": ("  + mapFile + ") " + e.toString());
	    return false;
	}
	return true;
    }

    /** 
     * Dispatch and handle the request.
     * This version looks at the supplier id, rewrites the url based on
     * that supplier, then lets the default handler do it.
     */

    public boolean
    respond(Request request) {
    	if (!request.url.startsWith(urlPrefix)) {
	    return false;
    	}	

	request.log(Server.LOG_INFORMATIONAL, propsPrefix + "..  handling request");
    	if (request.headers.get(authHeader) == null) {
	    request.sendError(404,"Not Authorized - no credentials supplied", "");
	    return true;
    	}

    	StringTokenizer auth = new
		StringTokenizer((String) request.headers.get(authHeader));
    	String realm = auth.nextToken();
    	if (!realm.equals(realm)) {
	    request.sendError(404,"Not Authorized - Invalid realm",
		"Realm: " + auth + " need: " + realm);
	    return true;
    	}

	String src = auth.nextToken();
    	String dst = map.getProperty(src,
		request.props.getProperty(propsPrefix + DEFAULT,""));
    	if (dst.equals("")) {
	    request.sendError(404,"Not Authorized - Invalid id :" + src, "");
	    return true;
    	}

    	// If we have a supplier url, change it to the correct supplier, 
    	// based on the id of the user, otherwise handle it as a normal url.

	String url = "/" + dst +
		request.url.substring(request.url.indexOf("/",1));
	request.log(Server.LOG_INFORMATIONAL, propsPrefix + ": mapping " + request.url + 
		" -> " + url);
	request.url = url;
    	return false;
    }
}
