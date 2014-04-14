/*
 * AclSwitchHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1998-2006 Sun Microsystems, Inc.
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
 * Created by suhler on 98/09/21
 * Last modified by suhler on 06/11/13 15:01:51
 *
 * Version Histories:
 *
 * 2.2 06/11/13-15:01:51 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.1 02/10/01-16:36:21 (suhler)
 *   version change
 *
 * 1.13 01/10/19-11:12:53 (suhler)
 *   using wrong prefix!
 *
 * 1.12 01/07/20-11:31:04 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.11 01/07/17-14:15:32 (suhler)
 *   use MatchUrl
 *
 * 1.10 01/01/05-11:54:01 (suhler)
 *   doc typo
 *
 * 1.9 00/12/11-13:26:38 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.8 00/10/05-11:05:22 (suhler)
 *   ACL list can be either url prefixes -or- regular expression patterns
 *   .
 *
 * 1.7 00/05/31-13:45:20 (suhler)
 *   doc cleanup
 *
 * 1.6 00/05/22-14:03:18 (suhler)
 *   doc updates
 *
 * 1.5 00/05/19-11:47:43 (suhler)
 *   - Removed options to add http headers, use a session handler instead
 *   - fixed password matching problems (not well tested)
 *   - changed the way roles prefixes are handled
 *
 * 1.4 00/04/20-11:48:00 (cstevens)
 *   copyright.
 *
 * 1.3 00/03/10-17:01:51 (cstevens)
 *   Removing unused member variables.
 *
 * 1.2 99/03/30-09:28:22 (suhler)
 *   documentation update
 *
 * 1.2 98/09/21-14:07:48 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/AclSwitchHandler.java
 *
 * 1.1 98/09/21-14:07:47 (suhler)
 *   date and time created 98/09/21 14:07:47 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.util.regexp.Regexp;

/**
 * Simple access control hander based on url prefixes or regexps.
 * Looks up list of valid prefixes or regular expressions in
 *  {@link sunlabs.brazil.server.Request#props}
 * , and allows/denies
 * access based on those prefixes. 
 * This is expected to work in conjunction with an upstream handler, 
 * such as
 * {@link sunlabs.brazil.handler.RolesHandler}
 * or
 * {@link sunlabs.brazil.handler.BasicAuthHandler}
 * that examines the request, and place credentials into the
 * request object.  The credentials consist of url prefixes
 * or regular expressions that match classes of url's.  Documents
 * whose URL prefix don't  match a credential are rejected.
 * If a credential does not begin with a slash (/), the {@link #init} prefix
 * for this handler is prepended.
 * <p>
 * Properties:
 * <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Sepcify the URL that triggers this handler.
 * (See {@link MatchString}).
 * <dt>authName	<dd>The name of the request.props entry to find a
 *		    white-space delimited list of url prefixes or
 *		    regular expression patterns. (defaults to "roles").
 *		    If the items in the list don't start with "/", then
 *		    the url prefix is prepended (only for prefix matching).
 * <dt>redirect	<dd>Name of the url to re-direct to if permission is denied.
 *		    If not specified, a simple message is sent to the client.
 * <dt>useRegexp<dd>If provided, the list of credentials is interpreted as
 *		    regular expressions, otherwise url prefixes are used.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		2.2, 06/11/13
 */

public class AclSwitchHandler implements Handler {
    String propsPrefix;		// our name in the properties file
    String aclName;		// name of acl property in request obj
    String urlPrefix;		// our ul prefix.
    String redirect;		// where to redirect denials to (if any)
    boolean useRegexp;		// interpret strings as exp's
    MatchString isMine;		// check for matching url

    /**
     * Handler configuration property <b>authName</b>.
     * The name of the request property to fine a white space
     * delimited list of credentials - URL prefixes - for this
     * request.  Defaults to <b>roles</b>.
     */
    static final String ACLNAME = "authName"; // authorization template

    /**
     * Name of the url to re-direct to if permission is denied.
     * If not specified, a simple error message is sent to the client
     */

    final static String REDIRECT = "redirect";

    public boolean
    init(Server server, String prefix) {
	propsPrefix = prefix;
	aclName = server.props.getProperty(propsPrefix + ACLNAME, "roles");
	redirect = server.props.getProperty(prefix + REDIRECT);
	urlPrefix = server.props.getProperty(prefix + "prefix", "/");
	useRegexp = (server.props.getProperty(prefix + "useRegexp") != null);
	isMine = new MatchString(prefix, server.props);
	return true;
    }

    public boolean
    respond(Request request) throws IOException {
	if (!isMine.match(request.url)) {
	    return false;
	}
	String aclList = request.props.getProperty(aclName,null);
	request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
		"ACL list found in: " + aclName + ": " + aclList);
	if (aclList == null) {
	    request.sendError(400 ,"File Not Found", "No Acl found");
	    return true;
	}

	/*
	 * see if our url prefix (or exp) matches anything on the list
	 */

	StringTokenizer st = new StringTokenizer(aclList);
	while(st.hasMoreTokens()) {
	    String prefix = st.nextToken();
	    if (!useRegexp && !prefix.startsWith("/")) {
		prefix = urlPrefix + prefix;
	    }
	    request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
		    "Checking url against: " + prefix);
	    if ((useRegexp && 
	    	    (new Regexp(prefix)).match(request.url,(String[])null)) ||
		    request.url.startsWith(prefix)) {
	    	return false;
	    }
	}

	/*
	 * Either redirect to "permission denied" page, or send a simple
	 * error response
	 */

	if (redirect!=null) {
	    request.redirect(redirect,null);
	} else {
	    request.sendError(400 ,"File Not Found", "url doesn't match: "
		+ aclList);
	}
	return true;
    }
}
