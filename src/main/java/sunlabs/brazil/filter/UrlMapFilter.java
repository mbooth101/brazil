/*
 * UrlMapFilter.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2004 Sun Microsystems, Inc.
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
 * Version:  2.2
 * Created by suhler on 01/05/25
 * Last modified by suhler on 04/11/30 15:19:44
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:44 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:37:26 (suhler)
 *   version change
 *
 * 1.2 02/07/24-10:48:30 (suhler)
 *   doc updates
 *
 * 1.2 01/05/25-13:04:34 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/UrlMapFilter.java
 *
 * 1.1 01/05/25-13:04:33 (suhler)
 *   date and time created 01/05/25 13:04:33 by suhler
 *
 */

package sunlabs.brazil.filter;

import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.handler.MapPage;
import sunlabs.brazil.handler.MultiProxyHandler;
import sunlabs.brazil.util.http.MimeHeaders;

/**
 * Filter to Map url's from any proxied content.
 * The {@link MultiProxyHandler} is used to virtually mount other web sites
 * into the local document root.  In the process, it rewrites all the
 * local url's found in the proxied content to point to the locally mounted
 * version.
 * <p>
 * This filter examimes content derived from non-proxied sources for
 * absolute url's to proxied sites, and rewrites them to point to
 * the virtual mount point on the local machine instead of directly to 
 * the mounted site.
 *
 * @author		Stephen Uhler
 * @version		2.2
 */

public class UrlMapFilter implements Filter {
    MapPage mapper = null;
    String prefix;

    public boolean
    init(Server server, String prefix) {
	this.prefix = prefix;
	if (MultiProxyHandler.proxies != null) {
	    mapper = new MapPage(null); // don't map urls starting with '/'
	    mapper.setMap(MultiProxyHandler.proxies);
	    return true;
	} else {
	   return false;
       }
    }

    /**
     * This is the request object before the content was fetched
     */

    public boolean respond(Request request) {
	return false;
    }

    /**
     * Only filter text documents if the MultiProxyHandler was called
     */

    public boolean
    shouldFilter(Request request, MimeHeaders headers) {
	String type = headers.get("Content-Type");
	boolean filter= ((type != null) &&
		type.toLowerCase().startsWith("text/"));
	request.log(Server.LOG_DIAGNOSTIC, prefix + "type: " + type +
		    " filter?: " + filter);
	return filter;
    }

    /**
     * Rewrite all absolute links, if there are any left
     */

    public byte[]
    filter(Request request, MimeHeaders headers, byte[] content) {
	if (headers.get("Content-Type").toLowerCase().startsWith("text/html")
		    == false) {
	    request.log(Server.LOG_DIAGNOSTIC, prefix +
		    " Not text/html, skipping");
	} else {
	    content =  mapper.convertHtml(new String(content)).getBytes();
	}
	return content;
    }
}
