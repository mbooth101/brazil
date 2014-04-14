/*
 * UrlSessionFilter.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2005 Sun Microsystems, Inc.
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
 * Created by suhler on 99/08/06
 * Last modified by suhler on 05/07/12 10:36:50
 *
 * Version Histories:
 *
 * 2.3 05/07/12-10:36:50 (suhler)
 *   add "https" support
 *
 * 2.2 04/11/30-15:19:39 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:39:05 (suhler)
 *   version change
 *
 * 1.10 02/05/10-15:18:45 (suhler)
 *   use util.Guid for cookie sessionhandle generation
 *
 * 1.9 00/12/08-16:47:39 (suhler)
 *   doc fixes
 *
 * 1.8 00/10/05-15:50:56 (cstevens)
 *   lint.
 *
 * 1.7 00/07/07-17:01:15 (suhler)
 *   remove System.out.println(s)
 *
 * 1.6 00/05/31-13:45:09 (suhler)
 *   doc cleanup
 *
 * 1.5 00/04/24-14:05:39 (cstevens)
 *   handles returning URLs with # in them.
 *
 * 1.4 99/10/06-12:30:38 (suhler)
 *   use mimeHeaders
 *
 * 1.3 99/09/01-15:31:44 (suhler)
 *   lint
 *
 * 1.2 99/08/30-09:38:18 (suhler)
 *   fiz imports
 *
 * 1.2 99/08/06-08:28:49 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 3 2 filter/UrlSessionFilter.java
 *   Name history : 2 1 filter/SessionFilter.java
 *   Name history : 1 0 tail/SessionFilter.java
 *
 * 1.1 99/08/06-08:28:48 (suhler)
 *   date and time created 99/08/06 08:28:48 by suhler
 *
 */

package sunlabs.brazil.filter;

import sunlabs.brazil.handler.MapPage;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Guid;
import sunlabs.brazil.util.http.MimeHeaders;

import java.util.Properties;

/**
 * Sample filter to use url's instead of cookies for sessions.
 * When html files are delivered, all URL's back to this host are
 * changed to add in the session information.
 *
 * When requests are made, the session info is stripped off the URL,
 * which is passed to the rest of the handlers.
 * <p>
 * Note:  This fiter has been superceded by the
 * {@link SessionFilter}.  It is
 * included for illustrative purposes only.
 *
 * @author		Stephen Uhler
 * @version		2.3
 */

public class UrlSessionFilter
    implements Filter
{
    private static final String SESSION = "session";

    public String session = "SessionID";

    public boolean
    init(Server server, String propsPrefix)
    {
	Properties props = server.props;

	session = props.getProperty(propsPrefix + SESSION, session);

	return true;
    }

    /**
     * Extract the cookie out of the URL, rewriting the url as needed.
     * Add the session info at the end of the url:
     *  /a/b.html -> /a/b.html$xxxx where xxx is the session
     * This gets called before the original request is made.
     */

    public boolean
    respond(Request request)
    {
	String id;
	
	int dollar = request.url.indexOf('$');
	if (dollar > 0) {
	    id = request.url.substring(dollar + 1);
	    request.url = request.url.substring(0, dollar);
	} else {
	    id = Guid.getString();
	}
	request.props.put(session, id);
	return false;
    }

    /**
     * We have the results, only filter if html
     */

    public boolean
    shouldFilter(Request request, MimeHeaders headers) {
	String type = headers.get("content-type");
	return (type != null && type.equals("text/html"));
    }

    /**
     * Rewrite all the url's, adding the session id to the end
     */
	
    public byte[]
    filter(Request request, MimeHeaders headers, byte[] content)
    {
	String id = request.props.getProperty(session);
	if (id == null) {
	    return content;
	}
	Map map = new Map(id);
	return map.convertHtml(new String(content)).getBytes();
    }

    /**
     * The mapPage class was designed for virtual web page re-mapping
     * We use it for session attachment instead.
     */

    static class Map extends MapPage {
	Map(String s) {
	    super(s);
	}

	/**
	 * If the url doesn't start with http://, tack the session info
	 * on to the end of the URL
	 */

	public String convertString(String fix) {
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
	    String result = fix.substring(0, index) + "$" + prefix
		    + fix.substring(index);

	    return result;
	}
    }
}
