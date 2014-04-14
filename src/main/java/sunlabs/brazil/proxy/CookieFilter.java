/*
 * CookieFilter.java
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
 * Version:  2.5
 * Created by suhler on 99/01/27
 * Last modified by suhler on 06/01/17 09:48:38
 *
 * Version Histories:
 *
 * 2.5 06/01/17-09:48:38 (suhler)
 *   typo
 *
 * 2.4 05/07/12-10:37:18 (suhler)
 *   add "https" support
 *
 * 2.3 04/11/30-15:11:26 (suhler)
 *   fixed sccs version string
 *
 * 2.2 03/08/01-16:19:22 (suhler)
 *   fixes for javadoc
 *
 * 2.1 02/10/01-16:37:11 (suhler)
 *   version change
 *
 * 1.22 00/12/11-13:31:29 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.21 00/10/06-15:13:53 (suhler)
 *   remove dead code
 *
 * 1.20 00/07/12-15:58:03 (cstevens)
 *   doc
 *
 * 1.19 00/07/12-14:15:42 (cstevens)
 *   Blank characters at end of "Cookie" HTTP header were causing some HTTP servers
 *   to be confused and generate an error return.  Specifically, this seemed to
 *   happen to Microsoft-IIS HTTP servers when issuing a POST to an .asp file.
 *
 * 1.18 00/07/07-17:02:08 (suhler)
 *   remove System.out.println(s)
 *
 * 1.17 00/05/24-11:27:10 (suhler)
 *   add docs
 *
 * 1.16 00/05/22-14:06:31 (suhler)
 *   doc updates
 *
 * 1.15 00/04/24-13:02:16 (cstevens)
 *   CookieFilter: A separate "Cookie" header was sent for each cookie being sent
 *   to the target.  Yahoo Mail requires all cookies sent in a request to
 *   concatenated together in a single "Cookie" header.
 *
 * 1.14 00/04/12-15:57:44 (cstevens)
 *   CookieFilter uses SessionManager to hold cookies instead of accessing PJama
 *   directly.
 *
 * 1.13 00/03/29-16:21:27 (cstevens)
 *   CookieMonster converted to a Filter; as a Filter, the FilterHandler can use an
 *   arbitrary authentication scheme and combine any number of other content
 *   filters.
 *
 * 1.12 99/09/29-15:26:03 (cstevens)
 *   experimental proxy updated for latest changes to brazil server.
 *
 * 1.11 99/06/28-12:00:23 (suhler)
 *   allow cookies to be turned off
 *
 * 1.10 99/03/09-11:06:35 (suhler)
 *   fixed implicit domain completion
 *   fixed cookie matching
 *   - not excactly the same as Netscape
 *   - needs rewriting
 *
 * 1.9 99/02/22-10:41:43 (suhler)
 *   compute busy interval
 *
 * 1.8 99/02/19-14:28:32 (suhler)
 *   many changes.
 *   - rewrote cookie domain processing (its still broken)
 *   - Assume the use of "templates" downstream for handling exception conditions
 *   This eliminates the need to genreate html in the code, bud prevents
 *   the proxy from being run stand-alone
 *   - added info pages
 *   - redid the basic login flow.  Its now inconsistent with its parent.
 *
 * 1.7 99/02/12-16:54:11 (suhler)
 *   checkpoint
 *
 * 1.6 99/02/10-11:42:36 (suhler)
 *   changed package
 *
 * 1.5 99/02/10-10:28:06 (suhler)
 *   re-do set-cookie processing to use un-collapsed mime headers
 *
 * 1.4 99/02/04-14:16:01 (suhler)
 *   updated for new interface
 *
 * 1.3 99/02/03-14:10:33 (suhler)
 *   updated to use new generic proxy interface.  Still needs work
 *
 * 1.2 99/01/29-11:48:20 (suhler)
 *   new proxy interface
 *
 * 1.2 99/01/27-12:05:19 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 proxy/CookieFilter.java
 *   Name history : 1 0 proxy/CookieMonster.java
 *
 * 1.1 99/01/27-12:05:18 (suhler)
 *   date and time created 99/01/27 12:05:18 by suhler
 *
 */

package sunlabs.brazil.proxy;

import sunlabs.brazil.filter.Filter;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.util.http.MimeHeaders;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * The <code>CookieFilter</code> keeps a
 * record of all the browser <i>cookies</i> associated with a given session.
 * This can be
 * used to make the user's cookies "mobile" as follows.  A user's cookies
 * are normally stored with the browser being used, on the user's machine.
 * If the user runs a different browser or goes to a different machine, the
 * user's cookies will not be there.  Instead, the user can access the web
 * via a proxy that keeps all their cookies.  No matter which browser the
 * user chooses or machine the user is at, a proxy running with the
 * <code>CookieFilter</code> will automatically remember and add in their
 * appropriate cookies.  The <code>CookieFilter</code> also supports
 * multiple, concurrent users, keeping each user's cookies separate.
 * <ul>
 * <li> All "Set-Cookie" HTTP response headers are filtered out and saved in
 * the local storage.  "Set-Cookie" headers are not transmitted back to the
 * client.
 *
 * <li> Requests from the client have the appropriate "Cookie" headers added.
 *
 * <li> Users can retrieve, edit, and delete their own cookies.
 *
 * <li> JavaScript code that sets cookies is <b>not</b> handled by this
 * filter, since the code only runs on the client computer, not on the proxy.
 * For instance: <code>document.cookie = "userid=778287312"</code>.  Any and
 * all Javascript is passed unchanged by this filter.
 *
 * <li> This filter works in both a session-based and a non-session-based
 * fashion.  If sessions are used, cookies are kept with respect to the
 * session associated with a user.  If sessions are not used, all cookies
 * are kept in one pile for all users.  The latter case is valid if, say,
 * only one user is using the proxy running the <code>CookieFilter</code>.
 * </ul>
 * Properties:
 * <dl class=props>
 * <dt>session		<dd>The request property to find the session id.
 *			Defaults to "SessionID"
 * <dt>nosession	<dd>The name of the session to use if no session
 *			id is found.  defaults to "common".
 * <dt>admin		<dd>A URL prefix that causes status information to
 *			be placed in the request properties.
 * </dl>
 *
 * @author	Stephen Uhler (stephen.uhler@sun.com)
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version	2.5
 */

public class CookieFilter
    implements Filter
{
    private static final String SESSION = "session";
    private static final String NOSESSION = "nosession";
    private static final String ADMIN = "admin";

    public String session = "SessionID";
    public String nosession = "common";
    public String admin = "";

    String prefix;

    public boolean
    init(Server server, String prefix)
    {
	this.prefix = prefix;

	Properties props = server.props;
	session = props.getProperty(prefix + SESSION, session);
	nosession = props.getProperty(prefix + NOSESSION, nosession);
	admin = props.getProperty(prefix + ADMIN, admin);
	
	return true;
    }

    public boolean
    respond(Request request)
	throws IOException
    {
	if ((admin != null) && request.url.startsWith(admin)) {
	    return getInfo(request);
	}

	if (request.url.startsWith("http://") ||
		request.url.startsWith("https://")) {
	    MimeHeaders headers = request.headers;
	    for (int i = headers.size(); --i >= 0; ) {
		if ("Cookie".equalsIgnoreCase(headers.getKey(i))) {
		    headers.remove(i);
		}
	    }

	    insertCookies(request);
	}
	return false;
    }

    /**
     * Saves all "Set-Cookie" headers from the target in the client's local
     * storage, then removes those headers before allowing the response to
     * go back to the client.  The client never sees cookies on their local
     * machine.
     */
    public boolean
    shouldFilter(Request request, MimeHeaders headers)
    {
	for (int i = headers.size(); --i >= 0; ) {
	    String key = headers.getKey(i);
	    if (key.equalsIgnoreCase("Set-Cookie")) {
		rememberCookie(request, headers.get(i));
		headers.remove(i);
	    }
	}

	return false;
    }

    /**
     * Returns the original content, since this filter does not change
     * content; it changes the headers.
     */
    public byte[]
    filter(Request request, MimeHeaders headers, byte[] content)
    {
	return content;
    }

    boolean
    getInfo(Request request)
    {
	Hashtable h = request.getQueryData();
	
	Hashtable cookies = getSessionCookies(request);
	Enumeration e = cookies.keys();

	Hashtable sites = new Hashtable();

	Properties props = request.props;
	String pfx = prefix + "domains.";
	
	while (e.hasMoreElements()) {
	    Vector list = (Vector) cookies.get(e.nextElement());
	    for (int i = 0; i < list.size(); i++) {
		CookieInfo cookie = (CookieInfo) list.elementAt(i);
		String site = cookie.domain + cookie.path;

		int index = cookie.cookie.indexOf('=');

		String name, value;
		if (index < 0) {
		    name = cookie.cookie;
		    value = "";
		} else {
		    name = cookie.cookie.substring(0, index);
		    value = cookie.cookie.substring(index + 1);
		}

		sites.put(site, site);

		site = pfx + site;
		props.put(site, props.getProperty(site, "") + name + " ");
		props.put(site + "." + name, value);
	    }
	}

	e = sites.keys();
	StringBuffer sb = new StringBuffer();
	for (int i = 0; e.hasMoreElements(); i++) {
	    sb.append(e.nextElement()).append(' ');
	}
	props.put(prefix + "domains", sb.toString());
	return false;
    }

    /*
     * Remembers a set-cookie request from the content server
     *
     * look for set-cookie requests from origin server, and remember them.
     * They will be of the form:
     *   Set-cookie: <cookie>; expires=<date>; path=<path>; domain=<domain>
     *    - ';'s are delimeters
     *    - all but <cookie> are optional
     *    - Cookies have at least on embedded '=' used for id matching
     *    - paths a prefix matches on urls (default is /)
     *    - domain is a suffix match on hostname field (default origin host)
     * Cookies are hashed via domains, eith all cookies for a domain
     * stored in a vector, in order of decreasing path size
     *
     * @param request	The request object from the browser
     * @param line	the http header line containing "set-cookie".
     */

    void
    rememberCookie(Request request, String line)
    {
	Hashtable cookies = getSessionCookies(request);

	Url url = new Url(request.url);
	String domain = url.domain();
	if (domain.equals("")) {
	    domain = url.host();
	}

	CookieInfo cookie = new CookieInfo(url, line);
	Vector list = (Vector) cookies.get(domain);
	if (list == null) {
	    list = new Vector();
	    cookies.put(domain, list);
	    request.log(Server.LOG_DIAGNOSTIC,
		    "Creating vector for domain: " + domain);
	}

	int myLength = cookie.path.length();

	int i;	// cookie index;
	for (i = 0; i < list.size(); i++) {
	    CookieInfo match = (CookieInfo) list.elementAt(i);
	    if (myLength < match.path.length()) {
		break;
	    }
	    if (cookie.isEquals(match)) {
		list.removeElementAt(i);
		request.log(Server.LOG_DIAGNOSTIC, "Replacing Cookie: " + match);
		break;
	    }
	}
	request.log(Server.LOG_DIAGNOSTIC, "Inserting cookie: " + cookie);
	list.insertElementAt(cookie,i);
	// System.out.println("Inserting cookie (" + cookie + ") into: " + cookieList);
    }

    void
    insertCookies(Request request)
    {
	Url url = new Url(request.url);
	String domain = url.domain();

	Hashtable cookies = getSessionCookies(request);
	Vector list = (Vector) cookies.get(domain);
	if (list == null) {
	    return;
	}

	/*
	 * Theory: Multiple cookies that need to be sent to the same site
	 * can be send as multiple "Cookie" lines.  Makes sense, since
	 * multiple "Set-Cookie" headers are sent to the client as separate
	 * lines.
	 *
	 * Practice: Some HTTP servers instead require that all the cookies
	 * are sent in a single "Cookie" line, with each cookie separated
	 * by "; ".  Otherwise they don't see anything but the first or last
	 * "Cookie" line.
	 */
	StringBuffer sb = new StringBuffer();

	Enumeration e = list.elements();
	while (e.hasMoreElements()) {
	    CookieInfo match = (CookieInfo) e.nextElement();
	    if (match.isMatch(url)) {
		sb.append(match.cookie);
		sb.append("; ");
		match.update();
	    }
	}
	if (sb.length() > 0) {
	    /*
	     * Remove final "; " at end of "Cookie" HTTP header.  If present,
	     * it causes some HTTP servers get confused and generate an error
	     * response.  Specifically, this seemed to happen to Microsoft-IIS
	     * HTTP servers when issuing a POST to an .asp file.
	     */

	    sb.setLength(sb.length() - 2);

	    request.headers.add("Cookie", sb.toString());
	}
    }

    /**
     * Get the session's cookie table
     */

    Hashtable
    getSessionCookies(Request request)
    {
	String id = request.props.getProperty(session, nosession);
	return (Hashtable) SessionManager.getSession(id, CookieFilter.class,
		Hashtable.class);
    }

    /**
     * Store information about a cookie
     */

    static class CookieInfo implements Serializable
    {
        long ctime;	// cookie creation time
        long mtime;	// time of last cookie use
        long exptime;	// cookie expiration time
        int uses;	// number of uses
        String path;	// the path prefix for this cookie
        String domain;	// the domain of the cookie (including port)
        String cookie;	// the value of the cookie
        String host;    // Host setting this cookie (including port)

        /**
         * Create a cookie object.
         * @param url: The url for this request, to fill out default stuff
         * @param line The http line containing the cookie request
         */

	public CookieInfo(Url url, String line) {
	    ctime = mtime = System.currentTimeMillis();
	    exptime = 0;
	    uses=0;
	    path="/";
	    domain=null;
	    host=null;
	    cookie=null;

	    // Convert the cookie data into a hash table

	    StringTokenizer st =
		    new StringTokenizer(line, ";");
	    Hashtable cookieData = new Hashtable(5);
	    cookieData.put("value", st.nextToken().trim());
	    while (st.hasMoreTokens()) {
		String param = st.nextToken().trim();
		int index = param.indexOf('=');
		if (index > 0) {
		    cookieData.put(param.substring(0,index).toLowerCase(), 
			    param.substring(index+1));
		} else {
		    cookieData.put(param,""); 
		}
	    }

	    // validate data

	    if (cookieData.containsKey("expires")) {
		exptime = expTime((String) cookieData.get("expires"));
	    }

	    String path =  (String) cookieData.get("path");
	    if (path != null && !path.startsWith("/")) {
	    	path = "/" + path;
	    }
	    if (path != null) {
		this.path = path;
	    }

	    // compute domain

	    host = url.host();
	    String domain =  (String) cookieData.get("domain");
	    if (domain != null) {
		if (domain.indexOf(":") < 0) {
		    domain += host.substring(host.indexOf(":"));
		}
		if (url.domain().indexOf(domain) >= 0) {
		    this.domain = domain;
		} else {
		    // System.out.println("Domain error, using host: " + domain + " !~ " + url.domain());
		    this.domain = host;
		}
	    } else {
	    	this.domain = host;
	    }
	    cookie = (String) cookieData.get("value");
	}

	/**
	 * Generate the cookie info from the string representation
	 * This is called with the output of toString(). No error checking
	 */

	public CookieInfo(String value) {
	    try {
		StringTokenizer st = new StringTokenizer(cookie);
		host = st.nextToken();
		domain = st.nextToken();
		path = st.nextToken();
		cookie = st.nextToken();
		host = st.nextToken();
		ctime = Long.parseLong(st.nextToken());
		mtime = Long.parseLong(st.nextToken());
		exptime = Long.parseLong(st.nextToken());
		uses = Integer.parseInt(st.nextToken());
	    } catch (NumberFormatException e) {
	    	throw new RuntimeException(e.toString());
	    } catch (NoSuchElementException e) {
	    	throw new RuntimeException(e.toString());
	    }
	}

	/**
	 * Generate string representation - convertable back into a cookie
	 */

        public String toString() {
	    return "<" + cookie + "> " + uses + " " + domain + path +
		    "[from " + host + "]";
	    /*
	    return host + "" + domain + " " + path + " " + cookie + " " +
		    host + " " + ctime + " " + mtime + " " + exptime + " " +
		    uses;
	    */
        }

	/**
	 * See if url matches this cookie
	 */

	public boolean
	isMatch(Url url) {
	    return url.path().startsWith(path) &&
	    		url.host().endsWith(domain);
	}

	public void update() {
	    mtime = System.currentTimeMillis();
	    uses++;
	}

	/**
	 * See if cookies match
	 */

	public boolean
	isEquals(CookieInfo cookie) {
	    if (!cookie.domain.equals(domain) ||
		    !cookie.path.equals(path)) {
	    	return false;
	    }
	    int index = cookie.cookie.indexOf("=");
	    if (index >=0 && index == this.cookie.indexOf("=") &&
		    cookie.cookie.substring(0,index).equals(
		    this.cookie.substring(0,index))) {
		return true;
	    } else {
		return false;
	    }
	}

	/**
	 * Convert cookie dates into time-stamps
	 */

	static SimpleDateFormat df =
		new SimpleDateFormat("EEE, dd-MMM-yy HH:mm:ss z");
	public static long expTime(String dateString) {
	    try {
		return (df.parse(dateString)).getTime();
	    } catch (ParseException e) {
		return 0;
	    }
	}
    }

    /**
     * This does special stuff, base on our needs
     * host: the host + port
     * path: the path (starting with /)
     * domain: The minimum legal cookie domain for this host
     */

    static class Url {
    	private String host=null;	// the host part (excluding port)
    	private String domain=null;	// minumum domain for cookies
    	private String path=null;	// path part
    	private String port;		// the port #

	/**
	 * Create a url object from a url string
	 * @param url	The http url string
	 *			("" if none)
	 *
	 */

	public Url(String url) {
	    String host;
	    if (url.startsWith("https://")) {
		host = url.substring("https://".length());
	    } else {
		host = url.substring("http://".length());
	    }
	    int index = host.indexOf("/");
	    if (index >= 0) {
	    	this.path = host.substring(index);
	    	this.host = host.substring(0,index).toLowerCase();
	    } else {
	    	this.path = "/";
	    	this.host = host.toLowerCase();
	    }
	    index = host.indexOf(":");
	    if (index > 0) {
		this.host = host.substring(0,index);
		port = host.substring(index+1);
	    } else {
	    	port = "80";
	    }
	}

	public String host() {
	    return host + ":" + port;
	}

	public String path() {
	    return path;
	}

	/**
	 * Retrieve the minimum domain spec for a host.  Must have port info
	 * @param host	The host name (e.g. foo.x.y.com:80)
	 * @return		min. cookie domain (e.g. y.com:80)
	 */

	public static Vector specialDomains = new Vector();
	static {
	    specialDomains.addElement("com");
	    specialDomains.addElement("edu");
	    specialDomains.addElement("net");
	    specialDomains.addElement("org");
	    specialDomains.addElement("gov");
	    specialDomains.addElement("mil");
	    specialDomains.addElement("int");
	}

	/**
	 * Compute the Minimum cookie domain.
	 * @param default	The string to tack onto the host name, 
	 *			if it only has 1 dot
	 */

	public String
	domain() {
	    if (domain != null) {
	    	return domain;
	    }
	    StringTokenizer st = new StringTokenizer(host,".");
	    int dots = st.countTokens() - 1;
	    // System.out.println("COMPUTING DOMAIN FOR: " + host + " (" + dots + ") dots");
	    switch (dots) {
	    	case 0:		// no dots - invalid domain
	    	case 1:		// 1 dot - invalid domain
		    domain = "";
		    System.out.println("Domain error: " + host);
		    break;
	    	case 2:		// OK if special suffix
		    st.nextToken();
		    String middle = st.nextToken();
		    String right = st.nextToken();
		    if (specialDomains.contains(right)) {
		    	domain="." + middle + "." + right + ":" + port;
		    } else {
			System.out.println("Domain error: " + host);
			domain = "";
		    }
		    break;
	    	default: // 3 or more dots, always ok, leave .xxx.yyy.zzz
		    while (dots-- > 2) {
		    	st.nextToken();
		    }
		    domain = "";
		    String maybe = st.nextToken();
		    String next = "";
		    while (st.hasMoreTokens()) {
			next =  st.nextToken();
			domain += "." + next;
		    }
		    domain += ":" + port;
		    if (!specialDomains.contains(next)) {
		    	domain = "." + maybe + domain;
		    }
	    }
	    return domain;
	}

	public String toString() {
	    return host+path + " ("  + domain + ")";
	}
    }
}

/*

Initial sign-on stragegy
1) have proxy-authentication
   a) have hashtable entry - > ok
   b) don't have hash-table entry ->ask for re-authentication
2) go to web-server sign-on page
   - fill out form userid/password
    a) unused ID - make entry in cookie table, explain how to do proxy
    b) used ID - make-em use another one

Misc
   - keep used id's in a separate hash table

Investigate: application/x-ns-proxy-autoconfig for setting
proxy automatically

function FindProxyForURL(url, host) {
    return "PROXY ???.Sun.COM:8080;"
}
*/
