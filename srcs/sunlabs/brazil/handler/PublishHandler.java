/*
 * PublishHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2006 Sun Microsystems, Inc.
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
 * Version:  2.2
 * Created by suhler on 99/03/31
 * Last modified by suhler on 06/11/13 15:04:03
 *
 * Version Histories:
 *
 * 2.2 06/11/13-15:04:03 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.1 02/10/01-16:36:27 (suhler)
 *   version change
 *
 * 1.15 01/07/20-11:32:03 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.14 01/07/17-14:16:10 (suhler)
 *   use MatchUrl
 *
 * 1.13 00/12/11-13:27:09 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.3.1.1 00/05/31-13:46:12 (suhler)
 *   revert to old version, and clean up
 *
 * 1.12 00/04/20-11:50:04 (cstevens)
 *   Uses existing session
 *
 * 1.11 00/04/12-15:53:07 (cstevens)
 *   pull AuthHandler out of PublishHandler.
 *
 * 1.10 00/03/10-17:02:38 (cstevens)
 *   Removing unused member variables
 *
 * 1.9 99/10/26-18:51:01 (cstevens)
 *   case
 *
 * 1.8 99/10/14-14:57:15 (cstevens)
 *   resolve wilcard imports.
 *
 * 1.7 99/10/01-11:26:26 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.6 99/09/15-14:40:09 (cstevens)
 *   Rewritign http server to make it easier to proxy requests.
 *
 * 1.5 99/07/12-09:26:33 (suhler)
 *   publish handler now uses credentials to match url prefixes
 *
 * 1.4 99/04/05-12:06:57 (suhler)
 *   fils.canWrite() has weird semantics - don't use it.
 *
 * 1.3 99/04/01-09:05:25 (suhler)
 *   re-write:
 *   - removed built-in basic authentication
 *   - starts separate authentication handler, allowing for
 *   plug-in configuration policies
 *   - default auth handler is AuthHandler (e.g. basic authentication
 *
 * 1.2 99/03/31-21:31:07 (suhler)
 *   added alternate authentication scheme - still broken though
 *
 * 1.2 99/03/31-17:07:18 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/PublishHandler.java
 *
 * 1.1 99/03/31-17:07:17 (suhler)
 *   date and time created 99/03/31 17:07:17 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

/**
 * Handler for supporting publishing from Communicator.
 * Launches an authentication handler to protect the content
 * from malicious users.
 * <p>
 * Looks for <code>PUT</code> requests, and creates or modifies the
 * content as indicated.
 * <p>
 * The following request properties are used:
 * <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URL that triggers this handler.
 * (See {@link MatchString}).
 * <dt>session	<dd> The the name of request property holding the session
 *		information to provide the credentials for posting.  The
 *		default is "SessionID".
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		2.2, 06/11/13
 */

public class PublishHandler implements Handler {
    private static final String SESSION = "session";

    MatchString isMine;            // check for matching url
    public String session = "SessionID";

    public String propsPrefix;    // my prefix into the properties file

    /**
     * Start up the authentication handler.
     */

    public boolean
    init(Server server, String prefix) {
	this.propsPrefix = prefix;

	Properties props = server.props;
	isMine = new MatchString(prefix, server.props);
	session = props.getProperty(prefix + SESSION, session);

	return true;
    }

    /**
     * Make sure this is one of our "PUT" requests.
     * Look up the credentials for this request.
     * If no credentials are found, prompt the user for them.
     * IF OK, save file to proper spot.
     */

    public boolean
    respond(Request request)
	throws IOException
    {
    	if (!isMine.match(request.url) || !request.method.equals("PUT")) {
	    return false;
	}	

	/*
	 * screen out bad requests
	 */

	if (request.postData == null) {
	    request.sendError(400, "No content to put");
	    return true;
	}

	if (request.headers.get("Content-Range") != null) {
	    request.sendError(501, "Can't handle partial puts");
	    return true;
	}

	/*
	 * Get the credentials left by the auth handler.
	 */

	String credentials = request.props.getProperty(session, "");

	/*
 	 * Make sure the credentials are valid for this prefix
	 */

	StringTokenizer st = new StringTokenizer(credentials);
	boolean ok = false;
	while (st.hasMoreTokens()) {
	    String prefix = st.nextToken();
	    if (request.url.startsWith(prefix)) {
		ok = true;
		request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
			"file: " + request.url + " matches: " + prefix);
		break;
	    }
	}

    	/*
    	 * OK, now try to save the file.
    	 */

	String root = request.props.getProperty(FileHandler.ROOT, ".");
	File file = new File(root + FileHandler.urlToPath(request.url));

	request.log(Server.LOG_INFORMATIONAL, propsPrefix + "root: " + root);

	int code = (file.exists()) ? 204 : 201;

	FileOutputStream out;
	try {
	    out = new FileOutputStream(file);
	} catch (IOException e) {
	    request.sendError(403, "Permission denied", file.getAbsolutePath());
	    return true;
	}
	out.write(request.postData);
	out.close();
	request.sendResponse("Update of " + request.url + " succeeded",
		"text/html", code);
	return true;
    }
}
