/*
 * SunNetAuthHandler.java
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
 * Contributor(s): cstevens, rinaldo, suhler.
 *
 * Version:  2.1
 * Created by suhler on 98/09/30
 * Last modified by suhler on 02/10/01 16:36:29
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:36:29 (suhler)
 *   version change
 *
 * 1.31 02/05/10-15:18:30 (suhler)
 *   use util.Guid for cookie sessionhandle generation
 *
 * 1.30 01/07/16-16:45:10 (suhler)
 *   add Token.java
 *
 * 1.29 00/12/11-20:23:22 (suhler)
 *   doc typo
 *
 * 1.28 00/12/11-13:27:56 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.27 00/12/08-16:45:47 (suhler)
 *   doc fixes
 *
 * 1.26 00/03/29-14:35:29 (cstevens)
 *   pedantic use of request.getQueryData(()
 *
 * 1.25 00/03/10-17:02:25 (cstevens)
 *   Removing unused member variables
 *
 * 1.24 99/10/26-18:50:52 (cstevens)
 *   case
 *
 * 1.22.1.1 99/10/23-19:48:21 (rinaldo)
 *
 * 1.23 99/10/01-11:26:17 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.22 99/09/29-15:41:13 (cstevens)
 *   Replace HtmlMunge with LexML
 *
 * 1.21 99/09/15-14:39:35 (cstevens)
 *   Rewritign http server to make it easier to proxy requests.
 *
 * 1.20 99/05/25-09:21:06 (suhler)
 *   modified to use new HtmlMunge code
 *
 * 1.19 99/05/24-18:53:01 (suhler)
 *   Updated to use new HtmlMunge in no-delim mode
 *   .
 *
 * 1.18 99/04/19-10:53:32 (suhler)
 *   fixed docs
 *
 * 1.17 99/03/30-09:31:29 (suhler)
 *   documentation update
 *
 * 1.16 99/01/05-10:58:15 (suhler)
 *   fixed version string
 *
 * 1.15 98/12/09-13:48:47 (rinaldo)
 *   Merged changes between child workspace "/export/home/rinaldo/ws/brazil/naws" and
 *   parent workspace "/net/smartcard.eng/export/ws/brazil/naws".
 *
 * 1.13.1.1 98/12/09-13:48:25 (rinaldo)
 *   testing
 *
 * 1.14 98/12/08-13:15:18 (suhler)
 *   Changed exit criterion: a session terminates as then a URL contains the
 *   exit string (by default "exit").  The prefix matching was too restrictive
 *   .
 *
 * 1.13 98/11/18-14:00:12 (suhler)
 *   fixed toHexString generation of challenge
 *   changed the way validation properties are handled
 *
 * 1.12 98/11/17-10:06:11 (suhler)
 *   Merged changes between child workspace "/home/suhler/naws" and
 *   parent workspace "/net/smartcard.eng/export/ws/brazil/naws".
 *
 * 1.11 98/11/16-14:47:28 (rinaldo)
 *
 * 1.8.1.2 98/11/15-20:45:32 (suhler)
 *   fixed challenge generation when random produces < 16 digits
 *
 * 1.10 98/11/15-15:30:53 (rinaldo)
 *   Create new Distribution
 *
 * 1.8.1.1 98/11/13-15:51:05 (suhler)
 *   Typo in configuration code.  Max uses wasn't pares properly
 *   .
 *
 * 1.9 98/11/10-13:21:25 (rinaldo)
 *
 * 1.8 98/11/02-17:20:13 (suhler)
 *   better diagnostics, mostly
 *
 * 1.7 98/10/15-10:16:55 (suhler)
 *   lint
 *
 * 1.6 98/10/15-08:56:00 (suhler)
 *   better docs
 *
 * 1.5 98/10/14-18:10:58 (suhler)
 *   Working version
 *
 * 1.4 98/10/13-13:49:53 (suhler)
 *   next checkpoint
 *
 * 1.3 98/10/13-12:14:03 (suhler)
 *   checkpoint, just for a putback
 *
 * 1.2 98/10/13-08:10:52 (suhler)
 *   Still in process
 *
 * 1.2 98/09/30-14:24:31 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/SunNetAuthHandler.java
 *
 * 1.1 98/09/30-14:24:30 (suhler)
 *   date and time created 98/09/30 14:24:30 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Guid;

/**
 * All-in-one Handler for doing supplier.net style authentication.
 * <p>
 * The purpose of this handler is to provide an authenticated "front end"
 * to one or more web sites, using (hopefully) arbitrary challenge-
 * response based authentication via a plug-in authentication interface.
 * It can bridge disparate DNS domains by selectively mapping servers
 * on one domain into another, based on the supplied credentials, by using
 * the {@link MultiProxyHandler}.
 * <p>
 * The authentication step is expected to yield a list of roles, each of
 * which represents permission to access a specific foreign site.
 * Once authentication is complete, and the roles are obtained,
 * the handler keeps a set of credentials (a lease)
 * on behalf of the user, which can be tuned at setup time for a variety
 * of expiration conditions. Once a lease expires, re-authentication is required.
 * <p>
 * This handler starts two sets of handlers of its own, an authentication handler -
 * responsible for doing the authentication, and one of more virtual
 * proxy handlers -  one for each  possible role.  In the current
 * implementation, the authentication handler is specified and a configuration
 * property, and the proxy handlers are all instances of
 * {@link MultiProxyHandler}, one per role.
 * <p>
 * Operation of the handler proceeds in the following steps:
 * <ol>
 * <li> When the server starts, the handler is initialized.
 *  <ul>
 *  <li>The template file is located and read.
 *  <li>One {@link MultiProxyHandler} is started for each possible role
 *  <li>The Authentication handler is started.  Its operation is defined below.
 *  </ul>
 * <li> Browser cookies are used as a reference to the user's credentials.  If
 *      the cookie returned by the browser refers to a valid credential, 
 *      the requested url is compared to the user's roles.  If the requested
 *      URL is permitted, by matching one of the users's roles, the URL
 *      is forwarded to the proper virtual web site for delivery.  Otherwise
 *      the URL is considered "not found".
 * <li> If the credentials are not valid, either because thay had expired, 
 *      were removed, or there is no browser cookie, the authentication
 *      sequence is started, for the purpose of obtaining valid credentials.
 *  <ul>
 *  <li> A browser cookie is chosen at random, and a "set-cookie" request
 *       is sent to the client (in lieu of the URL requested) along with
 *       the login template.  An additional random value is created, retained
 *       by the handler on behalf of this client, and made available as a
 *       parameter to the login template.
 *  <li> The next response from the client is expected to contain the
 *       information required to authenticate the client.  This is normally
 *       accomplished by having the user fill out the form that is contained
 *       on the login template, and clicking the submit button.
 *  <li> The client's response (e.g. query data), along with the random number
 *       generated in the previous step, are forwarded to the authentication
 *       handler.
 *  <li> The authentication handler is expected to place a user id and a list
 *       of roles in the resulting request object if authentication is successful
 *       or an error message otherwise.  If the authentication suceeds, the
 *       roles are entered into the lease, and the original URL processing
 *       is resumed.  If instead an error is returned, the authentication 
 *       sequence is repeated.  The error message is may be displayed
 *       to the user if it is included as a parameter on the login template.
 *  </ul>
 * </ol>
 * <p>
 * The login template is ordinary HTML, except contructs of the form:
 * <pre>
 *   &lt;insert property=xx default=yy&gt;
 * </pre>
 * may be used to substitute
 * {@link sunlabs.brazil.server.Request#props}
 * into the template.  The properties <code>challenge</code> and
 * <code>Message</code> are automatically set to indicate the random
 * challange and error message (if any) from a previous attempt, respectively.
 * <p>
 * The following configuration parameters are recgnized:
 *
 * <dl class=props>
 * <dt>prefix	<dd> URL prefix for proxy
 * <dt>authenticate	<dd> URL for authentication page
 * <dt>cookie	<dd> name of the cookie
 * <dt>roles	<dd> list of roles
 * <dt>proxy	<dd> prefix for proxy handler
 * <dt>idName	<dd> property key for token id
 * <dt>roleName	<dd> property key for token roles
 * <dt>maxIdle	<dd> maximum idle time for token (seconds)
 * <dt>maxAge	<dd> maximum total age for token (seconds)
 * <dt>maxUses	<dd> maximum total uses for token
 * <dt>exit	<dd> prefix to exit a session
 * <dt>all	<dd> "free" directory suffixes
 * <dt>template	<dd> login template
 *</dl>
 *
 * Currently, the "sunlabs.brazil.handler.MultiProxyHandler" class
 * is called to do the actual proxying.
 * <i>(There should be a link to a sample config file for this one)</i>
 * <p>
 * NOTE: This handler is included for historical purposes.  It should be
 * upated to take advantage of features not available when it was first written.
 *
 * @author		Stephen Uhler
 * @version            2.1, 02/10/01
 */

public class SunNetAuthHandler implements Handler {
    Hashtable proxies;	// where to keep proxy handlers
    Handler tokenHandler;	// the handler to verify the credentials
    String propsPrefix;	// my prefix into the properties file
    String UrlPrefix;	// The url prefix that triggers this handler
    String cookieName;	// how to name our session cookie
    String authUrl;	// the url for authentication
    String idKey;	// The property name for the token id
    String roleKey;	// The property name for the roles
    String template;	// The file name of the login template
    String exitString;	// Any url containing this string ends a session
    int maxAge;		// max allowable age for token (in seconds)
    int maxIdle;	// length of idle time (in seconds)
    int maxUses;	// max usages for token
    Vector free;	// no authorization required for these

    static final String PREFIX = "prefix";	// URL prefix for proxy
    static final String AUTH = "authenticate";	// URL for authentication page
    static final String COOKIE = "cookie";	// name of the cookie
    static final String ROLES = "roles";	// list of roles
    static final String PROXY = "proxy";	// prefix for proxy
    static final String ID_KEY = "idName";	// property key for token id
    static final String ROLE_KEY = "roleName";	// property key for token roles
    static final String MAX_IDLE = "maxIdle";	// maximum idle time for token
    static final String MAX_AGE = "maxAge";	// maximum total age for token
    static final String MAX_USES = "maxUses";	// maximum total uses for token
    static final String LOGOUT = "exit";	// prefix to exit a session
    static final String ALL = "all";		// "free" directory suffixes
    static final String PROXY_CLASS = "sunlabs.brazil.handler.MultiProxyHandler";
    static final String TEMPLATE = "template";	// login template

    /**
     *  Create a "bag" for the handler hashtable
     */

    static class RoleData {
    	public String role;		// name of the role
    	public Handler handler;		// handler reference

	RoleData(String role, Handler handler) {
	    this.role = role;
	    this.handler = handler;
	}

	public String toString() {
	    return role + ": " + 
		    (handler==null ? "(null)" : handler.getClass().getName());
	}
    }

    /**
     * Set up all of the handlers
     * - Secure Token Services for authentication
     * - MultiProxyHandler for dispatching to hosts
     */

    public boolean
    init(Server server, String prefix) {
	propsPrefix = prefix;

	/*
	 * Extract the configuration properties
	 * idKey	The name of the request property to put the
	 *		token id into. [id]
	 * roleKey	The name of the request property to put the
	 *		list of valid roles for this card into [roles]
	 * urlPrefix	The global prefix for this handler [/]
	 * TEMPLATE	The name of the login template page.  It must have
	 *		tags of the form:
	 *			<insert property=xx default=yy>
	 *		to substitute request properties.  Useful
	 *		values of xx include "random" and "Message" [login.html]
	 * authUrl	The url to send to the token handler.  The
	 *		query info from the client will be added automatically
	 * cookieName	The name to use for the browser cookie [brazil]
	 * roles	The list of valid roles for this url []
	 * maxUses	The total number of times the browser cookie
	 *		may be used in this session
	 * maxIdle	The longest interval allowed between uses of this
	 *		cookie (seconds)
	 * maxAge	Maximum lifetime allowed for this session (seconds)
	 * exitString	any url containing this string teminates a session
	 * all		Names of sub-directories anyone can access with
	 * 		no authentication
	 */

	idKey = server.props.getProperty(propsPrefix + ID_KEY, "id");
	roleKey = server.props.getProperty(propsPrefix + ROLE_KEY, "roles");
	UrlPrefix = server.props.getProperty(propsPrefix + PREFIX, "/");
	authUrl = server.props.getProperty(propsPrefix + AUTH,
		"/SecureTokenServices/VerifyPin");
	cookieName = server.props.getProperty(propsPrefix + COOKIE, "brazil");
	exitString = server.props.getProperty(propsPrefix + LOGOUT, "exit");
	StringTokenizer roles = new StringTokenizer(
		server.props.getProperty(propsPrefix + ROLES, ""));

	StringTokenizer st = new StringTokenizer(
		server.props.getProperty(propsPrefix + ALL, ""));
    	free = new Vector(st.countTokens());
    	for (int i=0; i < free.capacity(); i++) {
	    free.addElement(UrlPrefix + st.nextToken());
    	}

	maxUses = Integer.decode(server.props.getProperty(propsPrefix +
		MAX_USES, "1000")).intValue();
	maxIdle = Integer.decode(server.props.getProperty(propsPrefix +
		MAX_IDLE, "1200")).intValue();
	maxAge = Integer.decode(server.props.getProperty(propsPrefix +
		MAX_AGE, "86400")).intValue();

	server.log(Server.LOG_DIAGNOSTIC, prefix, "Config parameters:\n" +
		"  url prefix: " + UrlPrefix + "\n" +
		"  token URL: " + authUrl + "\n" +
		"  browser cookie: " + cookieName + "\n" +
		"  exit string: " + exitString + "\n" +
		"  roles: " + roles + "\n" +
		"  max uses: " + maxUses + "\n" +
		"  max idle: " + maxIdle + "\n" +
		"  max age: " + maxAge + "\n" +
		"  free dirs: " + free + "\n" +
		"---");

	/*
	 * Read in the template file.  If it doesn't start with /,
	 * make it relative to the document root
	 */

	String templateFile = server.props.getProperty(propsPrefix + TEMPLATE,
		"login.html");
	try {
	    FileInputStream in;

	    if (!templateFile.startsWith(File.separator)) {
	    	templateFile = server.props.getProperty(FileHandler.ROOT) +
	    		File.separator + templateFile;
	    }
	    server.log(Server.LOG_DIAGNOSTIC, prefix, "Template: " + templateFile);
	    in = new FileInputStream(templateFile);
	    byte[] contents = new byte[in.available()];
	    in.read(contents);
	    template = new String(contents);
	    in.close();
	} catch (Exception e) {
	    server.log(Server.LOG_ERROR, prefix, "Can't read template: " + e);
	    return false;
	}


	/*
	 * Start the token handler.  Set the properties appropriately
	 */

	try {
	    String tokenName = server.props.getProperty(propsPrefix +
		    "token.class", "");
	    server.log(Server.LOG_DIAGNOSTIC, prefix, "tokenHndlr: " + tokenName);
	    Class tokenClass = Class.forName(tokenName);
	    tokenHandler = (Handler) tokenClass.newInstance();
	} catch (Exception e) {
	    server.log(Server.LOG_WARNING, prefix,
		    "Error creating token handler: " + e);
	    return false;
	}
	if (!tokenHandler.init(server, propsPrefix + "token.")) {
	    server.log(Server.LOG_ERROR, prefix, "token handler won't start");
	    return false;
	}
	
	/*
	 * Kick off all the proxy handlers.  Make up a default
	 * prefix for those prefixes that aren't specified.
	 * If there is no handler, then  fall through (how XXX)
	 * New strategy:  Look for a "host" property, only start the proxy
	 * if "host" is specified, otherwise, skip it!
	 */

	proxies = new Hashtable(roles.countTokens());
	while (roles.hasMoreTokens()) {
	    String role = roles.nextToken();
	    System.out.println ("Working on role: " + role);
	    String proxyPrefix = propsPrefix + role + ".";

	    if (!server.props.containsKey(proxyPrefix + PREFIX)) {
		server.props.put(proxyPrefix + PREFIX, UrlPrefix +
			(UrlPrefix.endsWith("/") ? "" : "/") + role + "/");
	    }
	    server.log(Server.LOG_DIAGNOSTIC, prefix,
		    "role " + role + " prefix: " +
		    server.props.get(proxyPrefix + PREFIX));
	    try {
	        Class proxyClass = Class.forName(PROXY_CLASS);
		Handler h = (Handler) proxyClass.newInstance();
		if (h.init(server, proxyPrefix)) {
		    proxies.put(server.props.get(proxyPrefix + PREFIX),
			    new RoleData(role, h));
		} else {
		    server.log(Server.LOG_WARNING, prefix,
			    "No proxy specified" +
			    " for role: " + role + ", using local directory");
		    proxies.put(server.props.get(proxyPrefix + PREFIX),
			    new RoleData(role, null));
		}
	    } catch (Exception e) {
		server.log(Server.LOG_WARNING, prefix,
			"Error creating handler: " + e);
		e.printStackTrace();
	    }
	}
	if (proxies.size() < 1) {
	    server.log(Server.LOG_ERROR, prefix, "Can't start any proxies");
	    server.log(Server.LOG_ERROR, prefix, "  SECURITY ALERT!");
	    return false;
	}
	server.log(Server.LOG_DIAGNOSTIC, prefix, "roles: " + proxies);
	return true;
    }

    /**
     * Act like a "gatekeeper".  If we have a valid browser cookie, 
     * Then dispatch to one of the proxies.  If not, try to authenticate
     * by returning the login "template", fetching the credentials, and
     * establising a session.
     */

    public boolean
    respond(Request request) throws IOException {

    	if (!request.url.startsWith(UrlPrefix)) {
	    request.log(Server.LOG_DIAGNOSTIC,"Handler " +
		    propsPrefix + " ignoring: " + request.url);
	    return false;
	}	

	/*
	 * Extract the browser cookie - if any
	 */

        String cookieValue = null;
        try {
	    String header = (String) request.headers.get("Cookie");
		System.out.println("HEADERS.GET.COOKIE: " + header );
	    int index = header.indexOf(cookieName + "=");
	    StringTokenizer st = new StringTokenizer(
		    header.substring(index).substring(cookieName.length() + 1));
	    cookieValue = st.nextToken();
		// Strip off the semicolon if there is one
		if ( cookieValue.endsWith(";") ) {
			cookieValue = cookieValue.substring(0,cookieValue.length() - 1 );
		}
	    request.log(Server.LOG_DIAGNOSTIC,"  Got cookie: " + cookieValue);
	} catch (Exception e) {}

	/**
	 * Look for session termination.
	 */

    	if (request.url.indexOf(exitString) != -1) {
	    Token.removeToken(cookieValue);
	    request.log(Server.LOG_DIAGNOSTIC,"  Session teminated: " +
		    request.url);
	    return false;
	}	

	/*
	 * See if its a free-be
	 */

	Enumeration e = free.elements();
	while(e.hasMoreElements()) {
	    if (request.url.startsWith((String) e.nextElement())) {
		request.log(Server.LOG_DIAGNOSTIC,"  Unprotected url: " +
			request.url);
		return false;
	    }
	}

	/*
	 * See if this prefix matches one of the roles
	 */

	Handler handler = null;		// the handler for this url
	String role = null;		// the role for this url
	Enumeration keys = proxies.keys();
	while(keys.hasMoreElements()) {
	    String key = (String) keys.nextElement();
	    if (request.url.startsWith(key)) {
		RoleData data = (RoleData) proxies.get(key);
		handler = data.handler;
		role = data.role;
		break;
	    }
	}

	if (role == null) {
	    request.log(Server.LOG_DIAGNOSTIC,"  No prefix match for: " +
		    request.url);
	    request.sendError(400, "Not found", "No matching role");
	    return true;
	}

	/*
	 * Set a browser cookie, if it doesn't exist
	 */

	if (cookieValue == null) {
	    do {
		cookieValue = Guid.getString();
	    } while (cookieValue.length() < 14);
	    cookieValue = cookieValue.substring(0,14);
	    request.log(Server.LOG_DIAGNOSTIC,"  New cookie: " + cookieValue);
	    request.addHeader("Set-Cookie", cookieName + "=" + cookieValue
		    + "; path=" + UrlPrefix);
	}

	request.props.put("challenge", cookieValue);

	/*
	 * No token, Send client the login page.  Then the request should
	 * be re-issued by the client with the credentials in the query data.
	 */

	if (!Token.haveToken(cookieValue)) {
	    Token.getToken(cookieValue);	// create a blank token
	    returnLogin(request, "");
	    return true;
	}
	Token token = Token.getToken(cookieValue);

	/*
	 * Have an empty token, Call the STS handler to get the
	 * proper credentials.  The client card data should be
	 * in the query data.   Make sure we add the challenge to the
	 * query data.
	 *
	 * XXX Technicaly this is incorrect.  We need to generate our own request object, instead of
	 * trying to pervert the original one.
	 */

	if (token.getId() == null) {
	    String save = request.url;
	    request.url = authUrl;

	    if (request.query.length() > 0) {
	    	request.query += "&random=" + cookieValue;
	    } else {
	    	request.query = "random=" + cookieValue;
	    }

	    request.log(Server.LOG_DIAGNOSTIC,
		    "  About to call token handler: " + authUrl
		    + " query: " + request.query
		    + " params: " + request.getQueryData(null)
		    + " post: " + request.postData
		    + " headers: " + request.headers
		    + " request.method:" +  request.method );

	    boolean ok = tokenHandler.respond(request);
	    request.log(Server.LOG_DIAGNOSTIC, "  result " + ok + " (" +
		    request.props + ")");
	    request.url = save;

	    /*
	     * at this point we should have the credentials in the request.
	     * If not - return to the login page.
	     * If so, remember the credentials in our token object.
	     */

	    String id = (String) request.props.get(idKey);
	    String error = request.props.getProperty("error", "unknown");
	    if (id == null) {
		request.log(Server.LOG_DIAGNOSTIC, "   Can't find: " +
			idKey + " in request data");
		returnLogin(request, "No token id found in request data: " +
			error.substring(error.lastIndexOf(":")+1));
		return true;
	    }
	    String roles = (String) request.props.get(roleKey);
	    if (roles == null) {
		request.log(Server.LOG_DIAGNOSTIC, "   Can't find: " +
			roleKey + " in request data");
		returnLogin(request, "No roles available for id " + id);
		return true;
	    }
	    token.setToken(id, roles);

	    /*
	     * Strip off the query data used for token validation.
	     * This should restore the query info that was presented as
	     * part of the original request.
	     */

	     // request.query="";
	}

	/*
	 * Have a token, make sure its still valid.  If so, call the
	 * proper handler, otherwise redirect to the login page with
	 * the appropriate error message.  We should remember the URL, 
	 * so we can redirect back here when reauthentication is complete.
	 */

	if (token.getAge() > maxAge || token.getIdle() > maxIdle ||
		    token.getUses() > maxUses) {
	    String message;
	    if (token.getAge() > maxAge) {
	    	message = "Session is too old";
	    } else if (token.getIdle() > maxIdle) {
		message = "Session was idle too long";
	    } else {
		message = "Session was used up";
	    }
	    Token.removeToken(cookieValue);
	    returnLogin(request, message);
	    return true;
	}
	request.log(Server.LOG_DIAGNOSTIC, "Credentials check: " +
		" age=" + token.getAge() +
		" idle=" + token.getIdle() +
		" uses=" + token.getUses());

	/*
	 * Now check the url against the list of allowed roles
	 */
	
	Vector valid = token.getRoles();
	if (valid.contains(role)) {
	    if (handler != null) {
		request.log(Server.LOG_DIAGNOSTIC, "  dispatching to proxy " + role);
		return handler.respond(request);
	    } else {
		request.log(Server.LOG_DIAGNOSTIC, "  dispatching next handler");
		return false;
	    }
	} else {
	    request.sendError(400, "Not found", "Invalid role");
	    return true;
	}
    }

    /**
     * return the login page with the appropriate message substituted in
     */

    public void returnLogin(Request request, String message) {
    	request.props.put("Message", message);
	request.log(Server.LOG_DIAGNOSTIC, "sending login page: " + message);
	String result = processTemplate(template, request.props);
	try {
	    request.sendResponse(result);
	} catch (IOException e) {
	    request.log(Server.LOG_ERROR, e.toString());
	}
    }

    /**
     * Process a template page, and send to the client.
     * This should be re-done to use the template handler.
     * Look for html tags of the form:
     *   <insert property=[name] default=[default]>
     * Also look for:
     *   <param name=[name] value=[value]>
     * and replace the tag with the value of the request property.
     * @param template		The template to process
     * @param data		The hashtable containing the data to subst
     */

    public static String processTemplate(String template, Hashtable data) {
	// System.out.println(" munge: " + data);
	HtmlRewriter hr = new HtmlRewriter(template);
	while (hr.nextTag()) {
	    String tag = hr.getTag();
	    if (tag.equals("insert")) {
		String property = hr.get("property");
		String def;
		if ((property != null) && data.containsKey(property)) {
		    hr.append((String) data.get(property));
		} else if ((def = hr.get("default")) != null) {
		    hr.append(def);
		}
	    } else if (tag.equals("param")) {
		// System.out.println(" param: " + h);
		String property = hr.get("name");
		if (data.containsKey(property)) {
		    hr.put("value", (String) data.get(property));
		}
	    }
	}
	return hr.toString();
    }
}

/**
 * Manage supplier.net session tokens.
 * Each token represents a "credentials lease" for a sun.net session.
 * Used by {@link SunNetAuthHandler}
 */

class Token {
    static Hashtable tokens;	// where to store the tokens
    long ctime;		// token creation time
    long atime;		// time of last use
    long now;		// The current time
    int uses;		// number of times token is used
    String id;		// ID for this token (e.g. the smart card serial #)
    Vector roles;	// The roles affiliated with this token
    Object arg;		// client data pointer

    /**
     * Initialize the hash table to hold all of the tokens
     */

    static {
    	tokens = new Hashtable();
    }

    /**
     * Create a new token, put it in the hash table .
     * @param cookie	The reference for this token (e.g. web cookie)
     * This is called by getToken.
     */

    private Token (String cookie) {
    	now = System.currentTimeMillis();
    	ctime = now;
	id = null;
	roles = null;
	uses = 0;
	arg=null;
    	tokens.put(cookie,this);
    }

    /**
     * Find the token assosiated with this cookie.
     * Create a new one if it doesn't exist
     */

    public static Token getToken(String cookie) {
    	Token result = (Token) tokens.get(cookie);
    	if (result == null) {
	    result = new Token(cookie);
	}
	result.atime = result.now;
	result.now = System.currentTimeMillis();
	result.uses++;
    	return result;
    }

    /**
     * Check to see if a token exists
     * @return true if there was a token
     */

    public static boolean haveToken(String cookie) {
    	return tokens.containsKey(cookie);
    }

    /**
     * Remove the token associated with this cookie
     * @return true if there was a token to remove
     */

    public static boolean removeToken(String cookie) {
    	return (cookie!= null && tokens.remove(cookie) != null);
    }

    /**
     * Set the id and roles associated with this token
     * @param id	The unique identifier for this token
     * @param roles     The white space delimited set of roles
     */

    public void setToken(String id, String roles) {
    	this.id = id;
    	StringTokenizer st = new StringTokenizer(roles);
    	this.roles = new Vector(st.countTokens());
    	for (int i=0; i < this.roles.capacity(); i++) {
	    this.roles.addElement( st.nextToken());
    	}
    }

    /**
     * Get the unique id associated with this token.
     */

    public String getId() {
        return this.id;
    }

    /**
     * Get the roles associated with this token
     */

    public Vector getRoles() {
        return this.roles;
    }

    /**
     * Get the number of uses of this token
     */

    public int getUses() {
        return this.uses;
    }

    /**
     * Get the total age of this token in seconds
     */

    public int getAge() {
        return (int) ((this.now - this.ctime)/1000);
    }

    /**
     * Get the idle time of this token in seconds
     */

    public int getIdle() {
        return (int) ((this.now - this.atime)/1000);
    }

    /**
     * Set the client data
     */

    public void setArg(Object arg) {
        this.arg = arg;
    }

    /**
     * Get the client data
     */

    public Object getArg() {
        return this.arg;
    }

    /**
     * Print a prettier version of this instance
     */

    public String toString() {
        String list = "";
    	return "times: " + ctime/1000 + " - " + atime/1000 + " - " + now/1000 +
    		" total tokens: " + tokens.size() + 
    		" uses: " + uses + " id: " + id + " roles:" + roles;
    }
}
