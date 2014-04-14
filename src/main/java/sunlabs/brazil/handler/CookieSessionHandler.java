/*
 * CookieSessionHandler.java
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
 * Contributor(s): cstevens, drach, suhler.
 *
 * Version:  2.2
 * Created by suhler on 99/06/28
 * Last modified by suhler on 06/11/13 15:02:01
 *
 * Version Histories:
 *
 * 2.2 06/11/13-15:02:01 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.1 02/10/01-16:36:38 (suhler)
 *   version change
 *
 * 1.19 02/05/10-15:14:14 (suhler)
 *   use util.Guid for cookie name generation
 *
 * 1.18 02/05/09-09:53:38 (suhler)
 *   Make invented session-i's more unique
 *
 * 1.17 01/08/01-10:16:29 (suhler)
 *   documented "prefix" -> cookie PATH behavior
 *
 * 1.16 01/07/31-17:49:47 (drach)
 *   Add a url prefix property
 *
 * 1.15 01/07/20-11:30:54 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.14 01/07/20-10:38:10 (suhler)
 *   don't set session if it already exists
 *
 * 1.13 01/07/17-14:14:51 (suhler)
 *   use MatchUrl
 *
 * 1.12 00/12/11-13:28:46 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.11 00/12/08-16:45:58 (suhler)
 *   doc fixes
 *
 * 1.10 00/05/31-13:47:42 (suhler)
 *   doc cleanup
 *
 * 1.9 00/04/28-09:22:37 (suhler)
 *   restore "gotCookie" semantics
 *
 * 1.8 00/04/24-14:05:57 (cstevens)
 *   Take out hard-coded "SessionID".
 *
 * 1.7 00/04/24-12:58:46 (cstevens)
 *   jImport
 *
 * 1.6 00/04/20-11:51:15 (cstevens)
 *   Rename SessionHandler to CookieSessionHandler.
 *
 * 1.5 00/02/01-19:00:55 (suhler)
 *   strip trailing ";" from cookie name
 *
 * 1.4 99/11/30-09:47:01 (suhler)
 *   SessionHandler puts "gotCookie" property in request if a cookie was
 *   retrieved from the browser
 *
 * 1.3 99/10/26-18:52:20 (cstevens)
 *   case
 *
 * 1.2 99/09/15-14:40:29 (cstevens)
 *   Rewritign http server to make it easier to proxy requests.
 *
 * 1.2 99/06/28-11:06:38 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 handlers/CookieSessionHandler.java
 *   Name history : 1 0 handlers/SessionHandler.java
 *
 * 1.1 99/06/28-11:06:37 (suhler)
 *   date and time created 99/06/28 11:06:37 by suhler
 *
 */

package sunlabs.brazil.handler;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.util.http.MimeHeaders;
import sunlabs.brazil.util.regexp.Regexp;
import sunlabs.brazil.util.Guid;
import sunlabs.brazil.util.MatchString;

import java.util.Hashtable;
import java.util.Properties;
import java.io.IOException;


/**
 * Handler for creating browser sessions using cookies.
 * This handler provides a single cookie-id that may be used by
 * other handlers.
 *
 * The intent is to require only one cookie per server.
 * (See also {@link sunlabs.brazil.filter.SessionFilter}, which
 * manages sessions with or without cookies).
 *
 * The following server properties are used:
 * <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URL that triggers this handler
 * (See {@link MatchString}).
 * <p>
 * If <code>prefix</code> is specified, it is also used to
 * instruct the client to limit the scope of the 
 * browser cookie. to that prefix.
 *
 * <dt> cookie
 * <dd> the name of the cookie to use (defaults to "cookie").
 *
 * <dt> <code>map</code>
 * <dd> If specified, the <code>ident</code> argument to
 *	{@link SessionManager#getSession} to get the table of valid
 *	cookies, used to map the cookie value to a Session ID.  By default,
 *	the Session ID stored in the request is the cookie value itself.

 * <dt> <code>exist</code>
 * <dd> If specified, this means that the Session ID corresponding to the
 *	cookie value must already exist in the <code>SessionManager</code>.
 *	Normally, if the cookie was not present, a new cookie is
 *	automatically created.  
 *
 * <dt>persist <dd> If set, cookies persist across browser sessions
 *
 * <dt> <code>session</code>
 * <dd> The name of the request property that the Session ID will be stored
 *	in, to be passed to downstream handler.  The default value is
 *	"SessionID".  If the property already exists, and is not empty, 
 *      no action will be taken.
 * </dl>
 * If a cookie was returned from the browser, the property:
 * <code>gotCookie</code> is set to the cookie name.  Otherwise it is left unset.
 *
 * @author		Stephen Uhler
 * @version		2.2, 06/11/13
 */

public class CookieSessionHandler
    implements Handler
{
    private static final String COOKIE = "cookie";
    private static final String MAP = "map";
    private static final String EXIST = "exist";
    private static final String PERSIST = "persist"; 
    private static final String PROPERTY = "session";
    private static final String URLPREFIX = "prefix";

    public String cookieName = "cookie";
    public String ident = "";
    public boolean mustExist = false;
    public boolean persist = false;
    public String session = "SessionID";
    MatchString isMine;            // check for matching url

    String urlPrefix;
    String propsPrefix;
    Regexp cookie;
    
    public boolean
    init(Server server, String propsPrefix)
    {
	this.propsPrefix = propsPrefix;
	
	Properties props = server.props;

	isMine = new MatchString(propsPrefix, server.props);
	cookieName = props.getProperty(propsPrefix + COOKIE, cookieName);
	ident = props.getProperty(propsPrefix + MAP, ident);
	mustExist = props.getProperty(propsPrefix + EXIST) != null;
	persist = props.getProperty(propsPrefix + PERSIST) != null;
	session = props.getProperty(propsPrefix + PROPERTY, session);
	urlPrefix = props.getProperty(propsPrefix + URLPREFIX, "/");

	cookie = new Regexp("(^|; *)" + cookieName + "=([^;]*)");
        return true;
    }

    public boolean
    respond(Request request) throws IOException {
	if (!isMine.match(request.url)) {
	    return false;
        }
	String current = request.props.getProperty(session);
	if (current != null && !current.equals("")) {
	    request.log(Server.LOG_INFORMATIONAL, propsPrefix,
		    session + " already exists, skipping");
	    return false;
        }

	/*
	 * Get the cookie out of the http header.  Cookies can be on separate
	 * lines or all combined on one line.  When multiple cookies are on
	 * one line, they are separated by ';' characters.
	 */

	String value = null;
	String[] subs = new String[3];
	MimeHeaders headers = request.headers;
	for (int i = headers.size(); --i >= 0; ) {
	    if (headers.getKey(i).equalsIgnoreCase("Cookie") == false) {
		continue;
	    }
	    if (cookie.match(headers.get(i), subs)) {
		value = subs[2];
		if (value.length() == 0) {
		    value = null;
		}
		break;
	    }
	}

	/* existing session, client can handle sessions */

	if (value != null) {
	    request.props.put("gotCookie", "true");
	}

	/*
	 * If present, map cookie to Session ID.  Otherwise, Session ID is
	 * cookie.
	 */
	String id = value;
	if (id != null) {
	    Hashtable h = (Hashtable) SessionManager.getSession(null, ident,
		    Hashtable.class);
	    id = (String) h.get(id);
	    if (id == null) {
		id = value;
	    }
	}


	/*
	 * Check if Session ID corresponds to an existing session.
	 */

	if ((id != null) && mustExist) {
	    if (SessionManager.getSession(id, null, null) == null) {
		id = null;
	    }
	}

	/*
	 * If Session ID was missing or wasn't known, make a new cookie.
	 */
    
        // System.out.println("must exist " + mustExist);

	if (id != null) {
	    request.log(Server.LOG_INFORMATIONAL, propsPrefix,
		    "Using cookie: " + value);
	} else if (mustExist == false) {
	    String expire = "";
	    if (persist) {
		expire="; EXPIRES=Fri, 01-Jan-10 00:00:00 GMT";
	    }
	    if (value == null) {
		value =  Guid.getString();
		request.log(Server.LOG_INFORMATIONAL, propsPrefix,
			"Creating new cookie: " + value);
		request.addHeader("Set-Cookie", cookieName + "=" + value +
			"; PATH=" + urlPrefix + expire);
	    }
	    id = value;
	}

	if (id != null) {
	    request.props.put(session, id);
	}
	return false;
    }
}
