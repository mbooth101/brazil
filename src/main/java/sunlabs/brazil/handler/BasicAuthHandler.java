/*
 * BasicAuthHandler.java
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
 * Version:  2.3
 * Created by suhler on 98/09/14
 * Last modified by suhler on 06/11/13 15:01:57
 *
 * Version Histories:
 *
 * 2.3 06/11/13-15:01:57 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.2 03/08/01-16:18:40 (suhler)
 *   fixes for javadoc
 *
 * 2.1 02/10/01-16:36:33 (suhler)
 *   version change
 *
 * 1.33 02/07/24-10:45:49 (suhler)
 *   doc updates
 *
 * 1.32 02/01/17-19:12:33 (suhler)
 *   - static table is kept in SessionManager, so "Static" and "dynamic" modes
 *   may interoperate.
 *   - if the valid credentials are "", then no SessionId property is set
 *   .
 *
 * 1.31 01/08/14-16:37:36 (suhler)
 *   small changes to allow dynamic authorization tables to interoperate
 *   with the SetTemplate
 *
 * 1.30 01/07/20-11:30:33 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.29 01/07/17-14:15:45 (suhler)
 *   use MatchUrl
 *
 * 1.28 01/03/13-15:12:17 (cstevens)
 *   Bug loading message resource.
 *
 * 1.27 01/01/12-08:25:13 (suhler)
 *   make map table public, so it may be manipulated from server-side scripts
 *
 * 1.26 00/12/28-12:13:30 (suhler)
 *   doc fixes
 *
 * 1.25 00/12/11-13:27:18 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.24 00/10/06-11:07:22 (suhler)
 *   "subst" moved from PRopsTemplate to Format
 *
 * 1.23 00/10/05-10:43:39 (suhler)
 *
 * 1.22 00/08/16-12:11:07 (suhler)
 *   replaced Format with PRopsTemplate.getPRoperty
 *
 * 1.21 00/07/05-13:45:24 (cstevens)
 *   BasicAuthHandler was giving misleading error message indicating that it
 *   couldn't load the mapfile even if no mapfile was specified.
 *
 * 1.20 00/06/05-11:03:14 (suhler)
 *   doc fixes
 *
 * 1.19 00/05/31-13:47:53 (suhler)
 *   name change
 *
 * 1.18 00/05/22-14:04:04 (suhler)
 *   remove ErrorMsg
 *
 * 1.17 00/05/19-11:49:39 (suhler)
 *   doc cleanups, better diagnostice, fixed broken http header processing
 *
 * 1.16 00/04/28-09:22:12 (suhler)
 *   always set "gotCookie" property
 *
 * 1.15 00/04/20-11:48:48 (cstevens)
 *   copyright.
 *   rename.
 *
 * 1.14 00/04/17-14:22:49 (cstevens)
 *   Doc
 *
 * 1.13 00/04/12-15:52:25 (cstevens)
 *   documentation
 *
 * 1.12 00/03/29-14:34:00 (cstevens)
 *   AuthHandler handles authentication via 401 or 407 (Proxy-Authentication).
 *
 * 1.11 00/03/10-17:02:00 (cstevens)
 *   Removing unused member variables
 *
 * 1.10 99/11/16-19:07:46 (cstevens)
 *   wildcard imports.
 *
 * 1.9 99/10/26-18:50:21 (cstevens)
 *   case
 *
 * 1.8 99/10/26-17:03:40 (cstevens)
 *   Get rid of public variables Request.server and Request.sock
 *   In all cases, Request.server was not necessary in the Handler.
 *   Request.sock was changed to Request.getSock(); it is still rarely used for
 *   diagnostics and loggin (e.g., ChainSawHandler).
 *
 * 1.7 99/10/01-11:25:42 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.6 99/09/15-14:38:24 (cstevens)
 *   Rewriting http server.
 *
 * 1.5 99/07/12-09:25:56 (suhler)
 *   renamed "oops" and made available to package
 *
 * 1.4 99/04/01-09:03:47 (suhler)
 *   credentials file can now be resolved relative to the document root
 *
 * 1.3 99/03/30-09:28:39 (suhler)
 *   documentation update
 *
 * 1.2 98/09/21-14:53:28 (suhler)
 *   changed the package names
 *
 * 1.2 98/09/14-18:03:03 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 4 3 handlers/BasicAuthHandler.java
 *   Name history : 3 2 handlers/BasicSessionHandler.java
 *   Name history : 2 1 handlers/AuthHandler.java
 *   Name history : 1 0 AuthHandler.java
 *
 * 1.1 98/09/14-18:03:02 (suhler)
 *   date and time created 98/09/14 18:03:02 by suhler
 *
 */

package sunlabs.brazil.handler;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.MatchString;

import java.io.InputStream;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.io.IOException;

/**
 * The <code>BasicAuthHandler</code> obtains a Session ID by performing
 * "basic" authentication, using either the "Authorization" or the
 * "Proxy-Authorization" headers.  This handler prevents
 * subsequent downstream handlers from being accessed unless the proper
 * authentication was seen in the request.  The Session ID obtained by this
 * handler is meant to be used by those downsteams handlers to access
 * whatever session-dependent information they need.
 * <p>
 * If the request does not contain the authentication headers or the
 * authentication information is not valid, this handler sends an HTTP
 * error message along with the "WWW-Authenticate" or "Proxy-Authenticate"
 * header, as appropriate.  See
 * <code><a href=#config.code>code</a></code>,
 * <code><a href=#config.authorization>authorization</a></code>,
 * <code><a href=#config.authenticate>authenticate</a></code>
 * <p>
 * If the request does contain valid authentication information, the
 * Session ID associated with the authentication information is inserted
 * into the request properties, for use by downstream handlers.  After
 * inserting the Session ID, this handler returns <code>false</code> to
 * allow the downstream handlers to run.  
 * IF the Session ID in empty (e.g. ""), then, although authenticateion 
 * succeeds, no Session Id property is set.
 * <p>
 * The set of valid Session IDs is contained 
 * in a globally accessible table managed by the <code>SessionManager</code>,
 * which may be initialized with a static table 
 * (see <code><a href=#config.code>mapFile</a></code>).
 * <p>
 * The format of the initialization table (if any) described above is a
 * Java properties file where keys are the Base64 encoded strings obtained
 * from the Authentication header and the values are the associated Session
 * IDs.  Base64 strings can contain the '=' character, but the keys in a
 * Java properties file cannot contain an '=' character, so all '=' characters
 * in the Base64 strings must be converted to '!' in the properties file,
 * as shown in the following sample properties file:
 * <pre>
 * bXIuIGhhdGU6a2ZqYw!! = radion
 * Zm9vOmJhcg!! = foo
 * </pre>
 * The data in the SessionManager table doesn't use the '!'s, only ='s.
 * <hr>
 * There are several different types of authentication possible.  All
 * authentication handlers should follow these basic principles: <ul>
 * <li> The authentication handler examines some aspect of the request
 *	to decide if the appropriate authentication is present.
 * <li> If the request is acceptable, the authentication handler should
 *	insert the extracted Session ID into a request property
 *	and then return <code>false</code>, to allow subsequent handlers
 *	to run and perhaps use the Session ID.
 * <li> If the request is not acceptable, the authentication handler can
 *	return an error message or do some other thing to try to obtain a
 *	valid authentication.
 * <li> Handlers wishing to be protected by authentication should not
 *	subclass an authentication handler.  Instead, such handler should
 *	be written to assume that authentication has already been performed
 *	and then just examine the Session ID present.  
 *	The web developer is then responsible for choosing which one (of
 *	possibly many) forms of authentication to use and installing those
 *	authentication handlers before the "sensitive" handler.
 * <li> Handlers that are protected by an authentication handler can
 *	use the Session ID stored in the request properties regardless of
 *	the specifics of the authentication handler.
 * </ul>
 * <pre>
 * handlers=auth history file
 *
 * auth.class=BasicAuthHandler
 * auth.session=account
 * auth.message=Go away, you're not allowed here!
 *
 * history.class=HistoryHandler
 * history.session=account
 *
 * file.class=FileHandler
 * file.root=htdocs
 * </pre>
 * In the sample pseudo-configuation file specified above, the
 * <code>BasicAuthHandler</code> is first invoked to see if the HTTP "basic"
 * authentication header is present in the request.  If it isn't, a nasty
 * message is sent back.  If the "basic" authentication header is present
 * and corresponds to a user that the <code>BasicAuthHandler</code> knows
 * about, the Session ID associated with that user is stored in the specified
 * property named "account".
 * <p>
 * Subsequently, the <code>HistoryHandler</code> examines its specified
 * property (also "account") for the Session ID and uses that to keep
 * track of which session is issuing the HTTP request.
 * <p>
 * Each handler that needs a Session ID should have a
 * configuration parameter that allows the web developer to specify the
 * name of the request property that holds the Session ID.
 * Multiple handlers can all use the same request property as each other,
 * all protected by the same authentication handler.
 * <hr>
 * This handler uses the following configuration properties:
 * <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Sepcify the URL that triggers this handler.
 * <a name=config.code> </a>
 * <dt> <code>code</code>
 * <dd> The type of authentication to perform.  The default value is 401.
 *	<p>
 *	The value 401 corresponds to standard "basic" authentication.
 *	The "Authorization" request header is supposed to contain the
 *	authentication string.  If the request was not authenticated, the
 *	"WWW-Authenticate" header is sent in the HTTP error response
 *	to cause the browser to prompt the client to authenticate.
 *	<p>
 *	The value 407 corresponds to "basic" proxy/firewall authentication.
 *	The "Proxy-Authorization" request header is supposed to contain the
 *	authentication string.  If the request was not authenticated, the
 *	"Proxy-Authenticate" header is sent in the HTTP error response
 *	to cause the browser to prompt the client to authenticate.
 *	<p>
 *	Any other value may also be specified.  Whatever the value, it will
 *	be returned as the HTTP result code of the error message.
 *
 * <a name=config.authorization> </a>
 * <dt> <code>authorization</code>
 * <dd> If specified, this is the request header that will contain the
 *	"basic" authentication string, instead of the "Authorization"
 *	or "Proxy-Authorization" header implied by <code>code</code>.
 *
 * <a name=config.authenticate> </a>
 * <dt> <code>authenticate</code>
 * <dd> If specified, this is the response header that will be sent in the
 *	HTTP error response if the user is not authenticated.
 *	<p>
 *	If this string is "", then this handler will
 *	authenticate the request if the authorization header is present,
 *	but <b>will not</b> send an HTTP error message if the request could
 *	not be authenticated.  This is useful if the web developer wants to
 *	do something more complex (such as invoking an arbitrary set of
 *	handlers) instead of just sending a simple error message if the
 *	request was not authenticated.  In this case, the web developer can
 *	determine that the request was not authenticated because <b>no</b>
 *	Session ID will be present in the request properties.
 *
 * <dt> <code>realm</code>
 * <dd> The "realm" of the HTTP authentication error message.  This is a
 *	string that the browser is supposed to present to the client when
 *	asking the client the authenticate.  It provides a human-friendly
 *	name describing who wants the authentication.
 *
 * <dt> <code>message</code>
 * <dd> The body of the HTTP authentication error message.  This will be
 *	displayed by the browser if the client chooses not to authenticate.
 *	The default value is "".  Patterns of the form <i>${xxx}</i> are
 *	replaced with the value of the <i>xxx</i>
 *	entry of <code>request.props</code>.
 *
 * <dt> <code>mapFile</code>
 * <dd> If specified, this is the initial Session ID file.
 *      This is expected to be
 *	a java properties file, whose keys are the authentication tokens,
 *	and whose values are the Session IDs that are inserted into the
 *	request properties.
 *	<p>
 *	The keys in the file are basic authentication (base64) tokens with
 *	any trailing <code>"="</code> characters changed to <code>"!"</code>.
 *
 * <dt> <code>session</code>
 * <dd> The name of the request property that the Session ID will be stored
 *	in, to be passed to downstream handlers.  The default value is
 *	"SessionID". 
 *
 * <dt>	<code>ident</code>
 * <dd> The <code>ident</code> argument to {@link SessionManager#getSession}
 *	to get the table of valid sessions.  The default value is
 *	"authorized".  If <code>ident</code> is of the form
 *      <code>ident:session</code>, then the <code>session</code>
 *      portion is used as the <code>session</code> argument to
 *      SessionManager.get().  Otherwise the <code>session</code>
 *      argument is NULL.  This table may be manipulated with the SetTemplate, 
 *      using the "ident" namespace and "session" for the 
 *	SetTemplate "sessionTable" parameter.
 * </dl>
 *
 * @author	Stephen Uhler (stephen.uhler@sun.com)
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version	2.3, 06/11/13
 */
public class BasicAuthHandler implements Handler {
    private static final String CODE = "code";
    private static final String AUTHORIZATION = "authorization";
    private static final String AUTHENTICATE = "authenticate";
    private static final String REALM = "realm";
    private static final String MESSAGE = "message";
    private static final String MAP_FILE = "mapFile";
    private static final String SESSION = "session";
    private static final String IDENT = "ident";

    MatchString isMine;            // check for matching url
    public int code = 401;
    public String authorization = "Authorization";
    public String authenticate = "WWW-Authenticate";
    public String realm = "realm";
    public String message = "Invalid credentials supplied";
    public String mapFile = null;
    public String session = "SessionID";
    public String ident = "authorized";
    public String sessionTable = null;

    String propsPrefix;

    /**
     * Initializes this handler.  It is an error if the <code>mapFile</code>
     * parameter is specified but that file cannot be loaded.
     *
     * @param	server
     *		The HTTP server that created this handler.
     *
     * @param	prefix
     *		A prefix to prepend to all of the keys that this
     *		handler uses to extract configuration information.
     *
     * @return	<code>true</code> if this <code>Handler</code> initialized
     *		successfully, <code>false</code> otherwise.
     */
    public boolean
    init(Server server, String propsPrefix)
    {
	Properties props = server.props;

	this.propsPrefix = propsPrefix;
	isMine = new MatchString(propsPrefix, server.props);
	try {
	    String str = props.getProperty(propsPrefix + CODE);
	    code = Integer.decode(str).intValue();
	} catch (Exception e) {}

	if (code == 407) {
	    authorization = "Proxy-Authorization";
	    authenticate = "Proxy-Authenticate";
	}
	authorization = props.getProperty(propsPrefix + AUTHORIZATION, authorization);
	authenticate = props.getProperty(propsPrefix + AUTHENTICATE, authenticate);
	realm = props.getProperty(propsPrefix + REALM, realm);
	message = props.getProperty(propsPrefix + MESSAGE, message);
	mapFile = props.getProperty(propsPrefix + MAP_FILE, mapFile);
	session = props.getProperty(propsPrefix + SESSION, session);
	ident = props.getProperty(propsPrefix + IDENT, ident);

	int i = ident.indexOf(":");
	if (i>0) {
	    sessionTable=ident.substring(i+1);
	    ident = ident.substring(0,i);
	}

	try {
	    if (message.startsWith("@")) {
		message = ResourceHandler.getResourceString(server.props,
			propsPrefix, message.substring(1));
	    }
	} catch (IOException e) {
	    server.log(Server.LOG_ERROR, propsPrefix,
		    "Can't get \"denied\" message");
	} 

	if (mapFile != null) try {
	    server.log(Server.LOG_DIAGNOSTIC, propsPrefix,
		    "Loading credentials file " + mapFile);
	    InputStream in = ResourceHandler.getResourceStream(server.props,
		    propsPrefix, mapFile);
	    Properties p = new Properties();
	    p.load(in);
	    in.close();

	    Properties map = (Properties) SessionManager.getSession(ident,
			sessionTable, Properties.class);
	    Enumeration keys = p.keys();
	    while (keys.hasMoreElements()) {
		String key = (String) keys.nextElement();
		String value = (String) p.get(key);
		map.put(key.replace('!', '='), value);
	    }
	} catch (Exception e) {
	    server.log(Server.LOG_DIAGNOSTIC, propsPrefix,
		"Credentials file (" + mapFile + ") not available: " + e);
	}
	mapFile = null;
	return true;
    }

    /**
     * Looks up the credentials for this request, and insert them into the
     * request stream.  If no credentials are found, prompt the user for them.
     */

    public boolean
    respond(Request request) throws IOException {
	if (!isMine.match(request.url)) {
	    return false;
	}

	String auth = request.headers.get(authorization);
	if (auth == null) {
	    return complain(request,"Missing http header: " + authorization);
	}

	try {
	    /*
	     * Strip off "basic" at beginning of value.
	     */

	    StringTokenizer st = new StringTokenizer(auth);
	    if ("basic".equalsIgnoreCase(st.nextToken()) == false) {
		return complain(request, "Non-basic realm: " + auth);
	    }

	    auth = st.nextToken();

	    String id;
	    request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
		   "Session manager: " + ident + ":" + sessionTable);
	    Properties h = (Properties) SessionManager.getSession(ident,
		sessionTable, Properties.class);
	    id = (String) h.getProperty(auth);
	    if (id == null) {
		return complain(request, "no id matching: " + auth);
	    }
		
	    /*
	     * Found registered user, so add user to request properties.
	     */

	    if (!id.equals("")) {
	       request.props.put(session, id);
	       request.props.put("gotCookie", "true");
	       request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
		   "Setting " + session + " to: " + id);
	    } else {
	       request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
		   "Authorization accepted, no session provided");
	    }
	    return false;
	} catch (Exception e) {
	    /* Malformed authorization. */
	    return complain(request, e.toString());
	}
    }

    /**
     * Authentication failed.
     * Send the appropriate authentication required header as a response.
     * @param request	The request to respond to
     * @param reason	The reason for failure (for diagnostics)
     * @return		True
     */
    
    public boolean
    complain(Request request, String reason)
	throws IOException
    {
	if (authenticate.length() == 0) {
	    request.log(Server.LOG_DIAGNOSTIC, propsPrefix, "no authenticate?");
	    return false;
	}
	request.addHeader(authenticate, "basic realm=\"" + realm + "\"");
	request.sendResponse(Format.subst(request.props, message),
		"text/html", code);
	request.log(Server.LOG_DIAGNOSTIC, propsPrefix, reason);
	return true;
    }
}
