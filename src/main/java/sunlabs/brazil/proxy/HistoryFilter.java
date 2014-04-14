/*
 * HistoryFilter.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2004 Sun Microsystems, Inc.
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
 * The Initial Developer of the Original Code is: cstevens.
 * Portions created by cstevens are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): cstevens, suhler.
 *
 * Version:  2.2
 * Created by cstevens on 00/04/12
 * Last modified by suhler on 04/11/30 15:19:39
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:39 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:37:12 (suhler)
 *   version change
 *
 * 1.6 00/12/11-13:31:35 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.5 00/05/22-14:06:42 (suhler)
 *   doc updates
 *
 * 1.4 00/05/01-14:54:03 (suhler)
 *   fix doc bug
 *
 * 1.3 00/04/24-13:02:50 (cstevens)
 *   doc
 *
 * 1.2 00/04/12-15:58:52 (cstevens)
 *   imports
 *
 * 1.2 00/04/12-11:26:13 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 proxy/HistoryFilter.java
 *
 * 1.1 00/04/12-11:26:12 (cstevens)
 *   date and time created 00/04/12 11:26:12 by cstevens
 *
 */

package sunlabs.brazil.proxy;

import sunlabs.brazil.filter.Filter;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.util.http.HttpUtil;
import sunlabs.brazil.util.http.MimeHeaders;
import sunlabs.brazil.util.regexp.Regexp;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

/**
 * The <code>HistoryFilter</code> is both a <code>Handler</code> and a
 * <code>Filter</code> that keeps a record of all pages visited by a
 * given session.  
 * <p>
 * The <code>HistoryFilter</code> can be used to
 * make a user's session "mobile" as follows:  A user's history
 * is normally stored with the browser being used, on the user's machine.
 * If the user runs a different browser or goes to a different machine, the
 * user's history will not be there.  Instead, the user can access the web
 * via a proxy that keeps track of their history.  No matter which browser
 * the user chooses or machine the user is at, a server running with the
 * <code>HistoryFilter</code> will automatically remember and be able to
 * present the user's history.  
 * <p>
 * The history is kept with respect to a Session ID.
 * <p>
 * This filter uses the following configuration properties: <dl class=props>
 * 
 * <dt> <code>prefix</code>
 * <dd>	This handler will only process URLs beginning with this string.
 *	The default value is "", which matches all URLs.
 *
 * <dt> <code>session</code>
 * <dd>	The name of the request property that holds the Session ID.  The
 *	default value is "SessionID".
 *
 * <dt> <code>nosession</code>
 * <dd>	The Session ID to use if the Session ID was not specified.  The
 *	default value is "common".
 *
 * <dt> <code>admin</code>
 * <dd> URLs beginning with this prefix cause the <code>HistoryFilter</code>
 *	to store the history information for the current Session in the
 *	request properties
 *	
 * <dt> <code>filter</code>
 * <dd>	If specified, then this is a <code>Regexp</code> pattern to match
 *	against the "Content-Type" of the result.  Setting this also implies
 *	that the <code>HistoryFilter</code> will be invoked as a
 *	<code>Filter</code> and not a <code>Handler</code>.  The default
 *	value is "", which indicates that the "Content-Type" is <b>not</b>
 *	examined and that this <code>HistoryFilter</code> will be invoked
 *	as a <code>Handler</code>.
 * </dl>
 *
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version		2.2
 */

public class HistoryFilter
    implements Filter
{
    private static final String URL_PREFIX = "prefix";
    private static final String SESSION = "session";
    private static final String NOSESSION = "nosession";
    private static final String ADMIN = "admin";
    private static final String FILTER = "filter";

    public String urlPrefix = "";
    public String session = "SessionID";
    public String nosession = "common";
    public String admin = "";
    public Regexp filter = null;

    String prefix;

    /**
     * Initializes this filter by reading all its configuration properties.
     * <p>
     * It is an error if the <code>filter</code> is specified but malformed.
     *
     * @param	server
     *		The HTTP server.
     *
     * @param	prefix
     *		The configuration property prefix.
     *
     * @return	<code>true</code> if this filter initialized successfully,
     *		<code>false</code> otherwise.  
     */
    public boolean
    init(Server server, String prefix)
    {
	this.prefix = prefix;

	Properties props = server.props;
	urlPrefix = props.getProperty(prefix + URL_PREFIX, urlPrefix);
	session = props.getProperty(prefix + SESSION, session);
	nosession = props.getProperty(prefix + NOSESSION, nosession);
	admin = props.getProperty(prefix + ADMIN, admin);
	if (admin.length() == 0) {
	    admin = null;
	}
	try {
	    String tmp = props.getProperty(prefix + FILTER, "");
	    if (tmp.length() > 0) {
		filter = new Regexp(tmp);
	    }
	} catch (IllegalArgumentException e) {
	    server.log(Server.LOG_DIAGNOSTIC, prefix, e.getMessage());
	    return false;
	}
	
	return true;
    }

    /**
     * If the <code>admin</code> prefix is seen, store the history
     * information associated with the session in the request properties.
     * <p>
     * If invoked as a <code>Handler</code> and the URL matches the
     * <code>prefix</code>, records this page's address in the history.
     *
     * @param	request
     *		The <code>Request</code> object that represents the HTTP
     *		request.
     *
     * @return	<code>false</code>, indicating that this <code>respond</code>
     *		method ran purely for its side effects.
     */
    public boolean
    respond(Request request)
    {
	if ((admin != null) && request.url.startsWith(admin)) {
	    StringBuffer sb = new StringBuffer();
	    Properties props = request.props;

	    Hashtable pages = getPages(request);
	    Enumeration keys = pages.keys();
	    for (int i = 0; keys.hasMoreElements(); i++) {
		String url = (String) keys.nextElement();
		PageInfo pi = (PageInfo) pages.get(url);
		sb.append(i).append(' ');

		String pfx = prefix + i;
		props.put(pfx + ".url", pi.url);
		props.put(pfx + ".first", HttpUtil.formatTime(pi.first));
		props.put(pfx + ".last", HttpUtil.formatTime(pi.last));
		props.put(pfx + ".count", Integer.toString(pi.count));
	    }
	    props.put(prefix + "pages", sb.toString());
	} else if ((filter == null) && request.url.startsWith(urlPrefix)) {
	    /*
	     * If no Content-Type filter is present, it means that the
	     * HistoryFilter is being used as a Handler.
	     */
	    recordUrl(request);
	}
	    
	return false;
    }

    /**
     * Called when invoked as a <code>Filter</code>.  If the URL matches the
     * <code>prefix</code> and the returned "Content-Type" matches the
     * <code>filter</code>, records this page's address in the history.
     *
     * @param	request
     *		The in-progress HTTP request.
     *
     * @param	headers
     *		The MIME headers from the result.
     *
     * @return	<code>false</code> indicating that this <code>Filter</code>
     *		does not want to modify the content.
     */
    public boolean
    shouldFilter(Request request, MimeHeaders headers)
    {
	try {
	    String type = headers.get("Content-Type");
	    if (request.url.startsWith(urlPrefix)
		    && (filter.match(type) != null)) {
		recordUrl(request);
	    }
	} catch (Exception e) {
	    /* Ignore:
	     * No content-type
	     * Missing/malformed filter.
	     */
	}
	return false;
    }

    /**
     * Returns the original content, since this filter does not change
     * content.  Won't actually be invoked.
     */
    public byte[]
    filter(Request request, MimeHeaders headers, byte[] content)
    {
	return content;
    }


    private void
    recordUrl(Request request)
    {
	Hashtable pages = getPages(request);
	PageInfo pi = (PageInfo) pages.get(request.url);
	if (pi == null) {
	    pi = new PageInfo(request.url);
	    pages.put(request.url, pi);
	}
	pi.count++;
	pi.last = System.currentTimeMillis();
    }

    private Hashtable
    getPages(Request request)
    {
	String id = request.props.getProperty(session, nosession);
	return (Hashtable) SessionManager.getSession(id,
		HistoryFilter.class, Hashtable.class);
    }

    /**
     * Keep information about a visited URL
     */

    static class PageInfo
	implements Serializable
    {
	String url;
	int count;
	long first;
	long last;
	
	public
	PageInfo(String url)
	{
	    this.url = url;
	    
	    first = System.currentTimeMillis();
	}
    }
}
