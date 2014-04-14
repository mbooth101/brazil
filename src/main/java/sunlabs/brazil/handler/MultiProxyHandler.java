/*
 * MultiProxyHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1998-2007 Sun Microsystems, Inc.
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
 * Created by suhler on 98/09/21
 * Last modified by suhler on 07/03/21 15:54:00
 *
 * Version Histories:
 *
 * 2.3 07/03/21-15:54:00 (suhler)
 *   use new convenience method
 *
 * 2.2 07/01/29-15:35:53 (suhler)
 *   rewrite to conform with the new GenericProxyHandler url mapping semantics
 *
 * 2.1 02/10/01-16:36:24 (suhler)
 *   version change
 *
 * 1.13 02/07/24-10:44:44 (suhler)
 *   doc updates
 *
 * 1.12 00/12/23-11:37:10 (suhler)
 *   make the static map table public (why not?)
 *
 * 1.11 00/04/20-11:48:24 (cstevens)
 *   copyright.
 *
 * 1.10 00/02/03-14:23:23 (suhler)
 *   Still trying to get /'s right
 *
 * 1.9 00/02/02-14:48:07 (suhler)
 *   url's with an implied port of 80 didn't work
 *
 * 1.8 99/10/26-17:10:36 (cstevens)
 *   Get rid of public variables Request.server and Request.sock:
 *   A. In all cases, Request.server was not necessary; it was mainly used for
 *   constructing the absolute URL for a redirect, so Request.redirect() was
 *   rewritten to take an absolute or relative URL and do the right thing.
 *   B. Request.sock was changed to Request.getSock(); it is still rarely used
 *   for diagnostics and logging (e.g., ChainSawHandler).
 *
 * 1.7 99/10/14-14:57:07 (cstevens)
 *   resolve wilcard imports.
 *
 * 1.6 99/10/06-12:21:31 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.4.1.1 99/10/06-12:17:14 (suhler)
 *   new api's
 *
 * 1.5 99/10/01-11:25:56 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.4 99/09/01-15:33:49 (suhler)
 *   fixed nasty typo
 *
 * 1.3 99/06/28-10:49:56 (suhler)
 *   rewritten to use new handler
 *
 * 1.2 99/03/30-09:30:34 (suhler)
 *   documentation update
 *
 * 1.2 98/09/21-14:07:49 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/MultiProxyHandler.java
 *
 * 1.1 98/09/21-14:07:48 (suhler)
 *   date and time created 98/09/21 14:07:48 by suhler
 *
 */

package sunlabs.brazil.handler;

import sunlabs.brazil.server.Server;
import sunlabs.brazil.server.Request;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Handler for permitting multiple cross connected virtual web sites.
 * Each handler instance adds its prefix and destination to a
 * static hashtable so the URL rewrite rules rewrite all of them correctly.
 * <p>
 * The
 * {@link GenericProxyHandler}
 * rewrites all of the links in each wepage to point back to the local.
 * machine.  Using this handler, if multiple virtual websites are configured, 
 * then links in one site that point to other virtual web sites are
 * rewritten to point locally as well.
 * <p>
 * For example, suppose we have 2  MultiProxyhandlers A and B configured
 * into the server as:
 * <ul>
 * <br><code>A.prefix=/foo/</code>
 * <br><code>A.host=www.foo.com</code>
 * <br><code>B.prefix=/bar/</code>
 * <br><code>B.host=www.bar.com</code>.
 * </ul>
 * A local request for <code>/foo/bar/test.html</code> will fetch
 * <code>http://www.foo.com/bar/test.html</code>.  If the result is
 * an html page, then any links in that page that resolve to
 * <code>www.foo.com/...</code> will be rewritten to the local server
 * as <code>/foo/...</code>, and any links that points to
 * <code>www.bar.com/...</code> will be rewritten to
 * <code>/bar/...</code>.
 *
 * @author      Stephen Uhler
 * @version	2.3, 07/03/21
 */

public class MultiProxyHandler extends GenericProxyHandler {

    /* XXX This shouldn't be global; instead there should be a 
     *	   per-server hashtable (sau)
     */

    /**
     * Holds all proxy -> prefix mappings for this server.
     */
    public static Hashtable proxies = null;
    boolean init=true;

    /**
     * Initialize this handler.
     * Add rewrite mapping into the global table.
     * If any "virtual" web sites reference other "virtual" web sites, then
     * rewrite the links accordingly.
     */

    public boolean
    init(Server server, String prefix) {
    	if (!super.init(server,prefix)) {
	    return false;
	}
	if (proxies == null) {
	    proxies = new Hashtable(17);
	}
	String map = "http://" + host + ":" + port + "/";

	/*
	 * No good reason not to allow this, other then to prevent confusion
	 */

	if (proxies.containsKey(map)) {
	    server.log(Server.LOG_WARNING, prefix,
		    "Prefix already in use: " + map + " -> " +
		    proxies.get(map) + " (ignoring)");
	    // return false;	// use this to barf!
	    return true;
	}
	proxies.put(map, urlPrefix);
	if (map.endsWith(":80/")) {
	    proxies.put(map.substring(0,map.length()-4) + "/", urlPrefix);
	}

	server.log(Server.LOG_INFORMATIONAL, prefix, "proxies: " + proxies);
	return true;
    }

    public boolean
    respond(Request request) throws IOException {

	/* Install the mapping table at the first request */

	if (init) {
	    init=false;
	    getMapper().setMap(proxies);
	}
	return super.respond(request);
    }
}
