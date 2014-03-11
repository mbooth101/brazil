/*
 * SessionFilter.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2005 Sun Microsystems, Inc.
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
 * Contributor(s): suhler.
 *
 * Version:  2.4
 * Created by suhler on 00/11/04
 * Last modified by suhler on 05/07/12 10:36:42
 *
 * Version Histories:
 *
 * 2.4 05/07/12-10:36:42 (suhler)
 *   add "https" support
 *
 * 2.3 04/11/30-15:19:39 (suhler)
 *   fixed sccs version string
 *
 * 2.2 02/11/05-10:43:59 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.17.1.1 02/11/05-10:42:33 (suhler)
 *   remove purious diagnostic output
 *
 * 2.1 02/10/01-16:39:05 (suhler)
 *   version change
 *
 * 1.17 02/07/24-10:44:14 (suhler)
 *   doc updates
 *
 * 1.16 02/06/24-15:51:31 (suhler)
 *   change prefix to cookiePrefix
 *
 * 1.15 02/06/23-20:57:02 (suhler)
 *   add cookie prefix
 *
 * 1.14 02/05/10-15:14:32 (suhler)
 *   use util.Guid for cookie name generation
 *
 * 1.13 02/05/09-09:53:16 (suhler)
 *   Make invented session-i's more unique
 *
 * 1.12 02/01/29-14:39:40 (suhler)
 *   doc lint
 *
 * 1.11 01/12/10-13:54:54 (suhler)
 *   lint
 *
 * 1.10 01/07/20-10:38:30 (suhler)
 *   don't ses session if it already exists
 *
 * 1.9 01/01/14-14:52:53 (suhler)
 *   lint
 *
 * 1.8 00/12/11-13:26:30 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.7 00/11/27-22:36:47 (suhler)
 *   make another attempt at suffix matching.
 *
 * 1.6 00/11/20-13:21:16 (suhler)
 *   doc fixes
 *
 * 1.5 00/11/17-09:32:40 (suhler)
 *   remove diagnostics
 *
 * 1.4 00/11/15-09:47:24 (suhler)
 *   temporary fix for suffix matching
 *
 * 1.3 00/11/05-20:34:12 (suhler)
 *   Reorganized the code.  Handler redirects better, so URL's look better
 *
 * 1.2 00/11/05-17:00:34 (suhler)
 *   checkpoint
 *
 * 1.2 00/11/04-19:28:40 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 filter/SessionFilter.java
 *
 * 1.1 00/11/04-19:28:39 (suhler)
 *   date and time created 00/11/04 19:28:39 by suhler
 *
 */

package sunlabs.brazil.filter;

import java.io.IOException;
import java.util.Properties;
import sunlabs.brazil.handler.MapPage;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Guid;
import sunlabs.brazil.util.http.MimeHeaders;
import sunlabs.brazil.util.regexp.Regexp;

/**
 * Filter to manage browser sessions using browser cookies or URL
 * rewriting as needed.
 * This should be used as the last filter in the filter chain.
 * It attempts to use browser cookies.  If they don't work, 
 * it rewrites the URL's instead, tacking the session info onto
 * the end of the URL.
 * <p>
 * This Filter works by first examining the request as a <i>handler</i>.
 * If the request contains an ID, either in the "browser cookie" or 
 * written into the URL, the session ID is extracted.
 * In the id-in-the-url case, the ID is removed from the URL.  When called
 * later as a filter, the SessionFilter rewrites all relevent URL's in the page
 * to incorporate the ID.
 * <p>
 * If an ID can't be found either in the <i>cookie</i> or URL, a couple
 * the session creation sequence starts.
 * First, the browser is send a "set-cookie" request along with a redirect
 * that contains the cookie value encoded into the redirected URL.
 * When the browser follows the redirect, the request is examined to se
 * if the cookie value was sent.  If so, the browser is redirected back
 * to the original URL, and normal "cookie" processing takes place.
 * If no cookie is found, the browser is redirected back to the
 * original URL, modified to embed the ID into it, and normal URL
 * session rewriting takes place.
 * <p>
 * The following server properties are used:
 * <dl class=props>
 * <dt> cookie
 * <dd> The name of the cookie to use (defaults to "cookie").
 *	If the name is "none", then no cookies are used.  Instead, 
 *	session rewriting will occur for every session.
 * <dt> session
 * <dd> The name of the request property that the Session ID will be stored
 *	in, to be passed to downstream handler.  The default value is
 *	"SessionID".   If the session property is set, and not empty,
 *	then no processing is done.
 * <dt>persist <dd> If set, cookies persist across browser sessions.
 *	If cookies are disabled, no persistence is available.
 * <dt>cookiePrefix<dd> The URL prefix for which the cookie applies.  
 *	     Defaults to "/".
 * <dt>suffix <dd> A regular expression that matches url suffix we process.
 *	Defaults to <code>html|xml|txt</code>.
 * </dl>
 * The Following request properties are set:
 * <dl>
 * <dt>gotCookie
 * <dd>An id was retrieved out of a cookie header
 * <dt>UrlID
 * <dd>Set to the string tacked onto the end of each URL, if
 * session ID's are managed by URL rewriting.  If cookies are used, this
 * is set to the empty string.
 * </dl>
 * @author		Stephen Uhler
 * @version		2.4
 */

public class SessionFilter implements Filter {
    private static final String SESSION = "session";
    private static final String COOKIE = "cookie";
    private static final String PERSIST = "persist"; 
    private static final String SUFFIX = "suffix"; 
    private static final String URLPREFIX = "cookiePrefix"; 

    public String session = "SessionID";
    public String cookieName = "cookie";
    public String urlSep = ",id=";	// delimeter between url and ID
    public String redirectToken;	// magic to put in url to redirect
    public String encoding = "UrlID";   // set if url encoding is in effect
    public boolean persist;		// if true, make cookies last.

    private String propsPrefix;		// properties prefix
    private boolean haveRedirect;	// we have a redirect response

    Regexp cookieExp;	// exp to match cookie in mime header
    Regexp suffixExp;	// exp to match suffix
    String urlPrefix;	// url prefix for cookie to match on

	
    public boolean
    init(Server server, String propsPrefix) {
	this.propsPrefix = propsPrefix;
	Properties props = server.props;
	session = props.getProperty(propsPrefix + SESSION, session);
	cookieName = props.getProperty(propsPrefix + COOKIE, cookieName);
	persist = props.getProperty(propsPrefix + PERSIST) != null;
	urlPrefix = props.getProperty(propsPrefix + URLPREFIX, "/");

	/* XXX
	 * The code should be re-arranged so the url id is stripped
	 * off before we do the suffix check.  Then this won't look
	 * quite so ugly.
	 */

	String suffix = props.getProperty(propsPrefix + SUFFIX,"html|txt|xml");
	cookieExp = new Regexp("(^|; *)" + cookieName + "=([^;]*)");
	suffixExp = new Regexp("(\\.(" + suffix + ")|/)$");
	redirectToken = cookieName + ",";
	server.log(Server.LOG_DIAGNOSTIC, propsPrefix, 
		"suffix:" + suffix);
	return true;
    }

    /**
     * This is called by the filterHandler before the content generation
     * step.  It is responsible for extracting the session information, 
     * then (if required) restoring the URL's to their original form.
     * It tries relatively hard to use cookies if they are available
     * through a series or redirects.
     */

    public boolean
    respond(Request request) throws IOException {

	String current = request.props.getProperty(session);
	if (current != null && !current.equals("")) {
	    request.log(Server.LOG_INFORMATIONAL, propsPrefix,
		    session + " already exists, skipping");
	    return false;
        }

	/*
	 * See if the query contains our redirect token. if so, strip it 
	 * off, and set the "redirected" flag.
	 */
	
	boolean haveRedirect = request.query.startsWith(redirectToken);
	if (haveRedirect) {
	    request.query = request.query.substring(redirectToken.length());
	    request.log(Server.LOG_DIAGNOSTIC, propsPrefix, 
		"Found and removing Redirect token");
	}

	String id = fetchCookie(request);

	/*
	 * If we got the cookie during the redirect, redirect back
	 * to clean up the url.
	 */

	if (haveRedirect && id != null) {
	    redirect(request);
	    return true;
	}

	/*
	 * If we have the cookie, set the session id and return.
	 */

	if (id != null) {
	    request.props.put("gotCookie", "true");
	    request.props.put(session, id);
	    request.log(Server.LOG_DIAGNOSTIC, propsPrefix, "Found cookie (" +
		    cookieName + ") = " + id);
	    return false;
	}

	/*
	 * No cookie, see if the id is encoded into the URL.
	 * For now, we'll encode the session info into the URL so
	 * that /foo/bar.html becomes /foo/bar.html,id=nnn.
	 * If we found the id in the url, and the redirect token, redirect
	 * again to remove the redirect token, but leave in the session info.
	 * We could try to prevent spoofing; Later.
	 */

	int sepIndex = request.url.indexOf(urlSep);
	if (sepIndex > 0) {
	    id = request.url.substring(sepIndex + urlSep.length());
	    request.url = request.url.substring(0, sepIndex);
	    request.props.put(encoding, urlSep + id);
	    request.props.put(session, id);
	    request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
		    "Found id in url=" + id);
	    return false;
	}
	
	/*
	 * If this isn't a suffix that needs an ID, then don't bother
	 */

	if (suffixExp.match(request.url) == null) {
	    request.log(Server.LOG_DIAGNOSTIC, propsPrefix, 
		"Invalid suffix for " + request.url);
	    request.props.remove(encoding);
	    return false;
	}

	/*
	 * No id in either cookie or Url, so we need to make one up.
	 * If this is the first use, try to set a cookie and redirect
	 * back here.  We'll add some magic onto the end of the Url
	 * as part of the query string so we know we tried to set a cookie.
	 * If we already tried that, then tack the session id onto the
	 * url, and redirect again. 
	 */

	id = Guid.getString();

	if (haveRedirect) {
	    request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
		    "refusing cookies, set url encoding=" + id);
	    request.url += urlSep + id;
	} else {
	    String expire = "";
	    if (persist) {
		expire="; EXPIRES=Fri, 01-Jan-10 00:00:00 GMT";
	    }
	    if (!cookieName.equals("none")) {
		request.addHeader("Set-Cookie", cookieName + "=" + id +
			"; PATH=" + urlPrefix + expire);
	    }
	    if (request.query.equals("")) {
		request.query = redirectToken;
	    } else {
		request.query = redirectToken + request.query;
	    }
	}
	redirect(request);
	return true;
    }

    /**
     * Do a redirect back to ourselves.
     */

    private void
    redirect(Request request) throws IOException {
	String target;
	if (request.method.equals("GET") && !request.query.equals("")) {
	    target = request.url + "?" + request.query;
	} else {
	    target = request.url;
	}
	request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
	       "Redirect to: " + target);
	request.redirect(target, null);
    }

    /**
     * Try to Get the cookie out of the http header.  Cookies can be
     * on separate lines or all combined on one line. 
     * When multiple cookies are on one line, they are separated
     * by ';' characters.
     *
     * @return		The cookie value, or null if not found.
     */

    String
    fetchCookie(Request request) {
	String id = null;
	String[] subs = new String[3];
	MimeHeaders headers = request.headers;
	for (int i = headers.size(); --i >= 0; ) {
	    if (headers.getKey(i).equalsIgnoreCase("Cookie") == false) {
		continue;
	    }
	    request.log(Server.LOG_DIAGNOSTIC, propsPrefix, 
		    "Examining cookie header: " + headers.get(i));
	    if (cookieExp.match(headers.get(i), subs)) {
		id = subs[2];
		if (id.length() == 0) {
		    id = null;
		}
		request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
		    "Got cookie id: " + id);
		break;
	    }
	}
	return id;
    }

    /**
     * We have the results, only filter if html and we're rewriting
     */

    public boolean
    shouldFilter(Request request, MimeHeaders headers) {
	String type = headers.get("content-type");
	boolean shouldEncode = (request.props.getProperty(encoding) != null);
	return (shouldEncode && type != null &&
		type.toLowerCase().startsWith("text/"));
    }

    /**
     * Rewrite all the url's, adding the session id to the end
     */
	
    public byte[]
    filter(Request request, MimeHeaders headers, byte[] content) {
	String id = request.props.getProperty(session);
	if (id == null) {
	    return content;
	}
	String type = headers.get("content-type");
	if (!type.toLowerCase().startsWith("text/html")) {
	    return content;
	}
	Map map = new Map(urlSep + id, suffixExp);
	return map.convertHtml(new String(content)).getBytes();
    }

    /**
     * The mapPage class was designed for virtual web page re-mapping, 
     * but it knows which entity attributes are URL's.
     * We use it for session attachment instead.
     */

    private static class Map extends MapPage {
	public static boolean debug = false;  // turn on for diagnostics
        Regexp re;
	Map(String s, Regexp re) {
	    super(s);
	    this.re = re;
	}

	/**
	 * If the url doesn't start with http://, tack the session info
	 * on to the end of the URL.  This can fail if the absolute
	 * URL resolves back to us XXX.
	 */

	public String
	convertString(String fix) {
	    if (fix.startsWith("http://") || fix.startsWith("https://")) {
		return null;
	    }
	    int index = fix.indexOf('#');
	    if (index < 0) {
		index = fix.indexOf('?');
	    }
	    if (index == 0) {
		return null;
	    } else if (index < 0) {
		index = fix.length();
	    }
	    String urlPart = fix.substring(0, index);
	    if (re.match(urlPart) != null) {
		String result = urlPart + prefix + fix.substring(index);
		debug(" converting: " + fix + " => " + result);
		return result;
	    } else {
	        debug("  Not converting: " + urlPart);
	        return null;
	    }
	}

	private void debug(String s) {
	    if (debug) {
		System.err.println(s);
	    }
	}
    }
}
