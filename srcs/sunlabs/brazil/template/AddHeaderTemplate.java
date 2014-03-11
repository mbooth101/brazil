/*
 * AddHeaderTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2007 Sun Microsystems, Inc.
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
 * Version:  2.8
 * Created by suhler on 99/03/09
 * Last modified by suhler on 07/01/08 15:14:17
 *
 * Version Histories:
 *
 * 2.8 07/01/08-15:14:17 (suhler)
 *   allow "last-modified" to take a time, in ms since the epoch
 *   .
 *
 * 2.7 04/11/30-10:45:50 (suhler)
 *   oops2
 *
 * 2.6 04/11/30-10:44:19 (suhler)
 *   oops
 *
 * 2.5 04/11/30-10:43:45 (suhler)
 *   version fix
 *
 * 2.4 04/05/03-11:58:12 (suhler)
 *   doc updates
 *
 * 2.3 04/05/03-11:48:23 (suhler)
 *   fix "location" directive; non-absolute url's are now handled rationally
 *
 * 2.2 04/04/28-14:51:08 (suhler)
 *   Comment a "Bug" (not yet fixed)
 *
 * 2.1 02/10/01-16:36:43 (suhler)
 *   version change
 *
 * 1.13 01/09/13-09:24:06 (suhler)
 *   remove uneeded import
 *
 * 1.12 01/08/14-16:39:19 (suhler)
 *   allow setting status headers
 *
 * 1.11 01/07/16-16:48:40 (suhler)
 *   use new Template convenience methods
 *
 * 1.10 01/06/18-16:08:17 (suhler)
 *   the attribute "nocontent" causes a 204 status to be generated
 *
 * 1.9 00/11/21-13:24:11 (suhler)
 *   handle location headers properly
 *
 * 1.8 00/11/20-13:21:42 (suhler)
 *   doc fixes
 *
 * 1.7 00/11/17-09:35:50 (suhler)
 *   better diagnostics, ${} substitutions, header removal as well.
 *
 * 1.6 00/10/31-10:18:53 (suhler)
 *   doc fixes
 *
 * 1.5 00/05/31-13:48:56 (suhler)
 *   name change
 *
 * 1.4 00/05/22-14:04:49 (suhler)
 *   doc updates
 *
 * 1.3 99/09/29-16:05:09 (cstevens)
 *   New HtmlRewriter object, that allows arbitrary rewriting of the HTML (by
 *   templates and others), instead of forcing the templates to return a string
 *   that contained all of the new HTML content in one big string.
 *
 * 1.2 99/03/30-09:34:04 (suhler)
 *   documentation update
 *
 * 1.2 99/03/09-11:18:35 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/AddHeaderTemplate.java
 *
 * 1.1 99/03/09-11:18:34 (suhler)
 *   date and time created 99/03/09 11:18:34 by suhler
 *
 */

package sunlabs.brazil.template;

import java.util.Enumeration;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.util.http.HttpUtil;

/**
 * Template class for adding arbitrary mime headers to a reply.
 * Add a header onto the response, removing the tag from the HTML.
 * <code>&lt;addheader name1=value1 name2=value2 ...&gt;</code>
 * where <code>name</code> is the name of an HTTP header, and
 * <code>value</code> is its value.  If no value is provided, then
 * the header is removed.  If multiple name/value pairs are provided, 
 * they are processed in arbitrary order.
 * <p>
 * Special headers.<br>
 * If a "location" header is added, the status code is automatically
 * set to "302".  If the value doesn't start with "http://" or
 * "https://", then the
 * value is turned into an absolute URL
 * by prepending the the hostname (and the path from the document
 * root if the value doesn't start with '/'.<br>
 * Removing location headers is ill-advised.
 * <p>
 * The special header <b>status</b> is used to set the status code.
 * <p>
 * If the attribute <code>nocontent</code> is present, the http header
 * status is set to <b>204 no content</b>.  This causes the browser to
 * ignore the contents of the page.
 * <p>
 * If a <code>last-modified</code> header is present and the value is an
 * integer, it is taken to be the time (in ms since the epoch), and
 * converted to the proper format.
 * <p>
 * The values are subject to ${...} substitutions.
 * <br>Note:  Setting invalid headers will lead to unpredictable results,
 *
 * @author		Stephen Uhler
 * @version		@(#) AddHeaderTemplate.java 2.8@(#)
 */

public class AddHeaderTemplate extends Template {

    /**
     * Process the special <code>addheader</code> tag.
     */
    public void
    tag_addheader(RewriteContext hr) {
	Enumeration e = hr.keys();
	while (e.hasMoreElements()) {
	    String key = (String) e.nextElement();
	    String value = hr.get(key);
	    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix +
	    	"addheader: " + key + ": " + value);
	    if (key.equals("nocontent")) {
		hr.request.setStatus(204);
	    } else if (key.equals("last-modified")) {
		try {
		    int tm = Integer.decode(value).intValue();
		    value = HttpUtil.formatTime(tm);
		} catch (Exception ex) {}
		hr.request.addHeader(key,value);
	    } else if (key.equals("status")) {
		try {
		    hr.request.setStatus(Integer.decode(value).intValue());
		} catch (Exception ex) {
		   debug(hr,"Invalid status code" + ex.getMessage()); 
		}
	    } else if (value.equals("")) {
	        hr.request.responseHeaders.remove(key);
	    } else {
		if (key.equals("location")) {
		    hr.request.setStatus(302);
		    if (!(value.startsWith("http://") ||
			    value.startsWith("https://"))) {
			if (!value.startsWith("/")) {
			    // relative to currentl url
			    value = hr.request.url.substring(0,
				    hr.request.url.lastIndexOf("/")) + "/" +
				    value;
			}
			value =hr.request.serverUrl() + FileHandler.urlToPath(
				value);
		    }
	            hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix +
			"redirecting to: " + value);
		}
		hr.request.addHeader(key,value);
	    }
	}
	hr.killToken();
    }
}
