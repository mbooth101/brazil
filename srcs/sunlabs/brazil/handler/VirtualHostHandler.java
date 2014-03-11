/*
 * VirtualHostHandler.java
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
 * Version:  2.6
 * Created by suhler on 99/06/28
 * Last modified by suhler on 05/12/05 14:01:25
 *
 * Version Histories:
 *
 * 2.6 05/12/05-14:01:25 (suhler)
 *   added "addlevel" to integrate virtual hosting with host wildcards without
 *   needing the UrlMapperHandler
 *
 * 2.5 05/12/05-13:03:06 (suhler)
 *   add "levels" attribute to support wild-carded virtual hosts, diag cleanup
 *
 * 2.4 04/11/03-08:30:07 (suhler)
 *   url.orig is now set in Request.java
 *
 * 2.3 04/10/24-17:21:06 (suhler)
 *   set "url.orig" property
 *   .
 *
 * 2.2 04/02/18-09:29:29 (suhler)
 *   Make the Virtual Host Handler more usefull (cause the MultiHostHandler
 *   is complex, and hard to use).
 *   - virtual site switching may be accomplished by changing either the
 *   URL or document root
 *   - Per host mappings may be specified, permitting greater flexibility in
 *   naming virtual host roots
 *   - all non-configured hosts can be sent to a common location
 *
 * 2.1 02/10/01-16:36:33 (suhler)
 *   version change
 *
 * 1.5 02/07/24-10:44:37 (suhler)
 *   doc updates
 *
 * 1.4 00/04/20-11:50:19 (cstevens)
 *   copyright.
 *
 * 1.3 99/10/26-18:52:24 (cstevens)
 *   case
 *
 * 1.2 99/10/01-11:26:40 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.2 99/06/28-11:07:18 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/VirtualHostHandler.java
 *
 * 1.1 99/06/28-11:07:17 (suhler)
 *   date and time created 99/06/28 11:07:17 by suhler
 *
 */

package sunlabs.brazil.handler;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Format;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Handler for managing virtual hosts using the same server configuration.
 * This prefixes the host name (from the http "host" header, with the
 * port portion removed) onto the
 * url and passes the request along.
 * If no host is provided, the host "default" is used instead.
 * <br>
 * If hosts require their own server configurations,
 * use the {@link MultiHostHandler} instead.
 * <p>
 * Configuration parameters:
 * <dl class=props>
 * <dt> maproot
 * <dd> If set upon server startup, this handler changes the "root" property
 *      instead of the "url" property, by appending the "host" onto the
 *	document root, instead of prepending the "host" to the url.
 * <dt> [prefix].[host].
 * <dd> If the "mapping" property exists that matches the incoming
 *      "host" name, then
 *	that value is used instead of [host] to rewrite the "url" or "root".
 * <dt> default
 * <dd> If set, then all hosts for which no mappings are defined are mapped
 *      to the value of this property.
 * <dt> levels
 * <dd> If defined, then for the purpose of host matching, only "levels"
 *      of hostnames are considered. If levels=3, then for host:
 *      a.b.c.d.e, the host is considered to be "c.d.e".  This enables
 *	support for wildcard-host matching within a virtual domain.
 * <dt> addlevel=true|false
 * <dd> If "true",  "levels" is specified, and the number of tokens (levels)
 *      in the hostname exceeds "levels", then all the extra tokens in the
 *	hostname are prepended to the URL as initial directories: If
 *	"levels" is 3, and "addlevel=true" then:
 *      host <code>http://a.b.c.d.e/foo.html</code> will be mapped to
 *      <code>http://c.d.e/b/a/foo.html</code>, and the file "foo.html"
 *	should be at [docroot]/c.d.e/b/a/foo.html].
 *      <p>If "addlevel=false", then <code>http://a.b.c.d.e/foo.html</code>
 *	will be mapped to <code>http://c.d.e/foo.html</code>, and the file 
 *      "foo.html" should be at [docroot]/c.d.e/foo.html.  In this case, 
 *	the "a.b" part of the host is available as part of the host property, 
 *	which retains its original value.
 *
 * </dl>
 * With no configuration options, each virtual host document root is in
 * a subdirectory whose name matches the host (e.g. www.foo.com).  The
 * "maproot" property changes how virtual roots are
 * distingished: by URL or by document root.  The "mapping" properties are 
 * used to choose a name for the subdirectory that differs from the
 * virtual hostname.  Finally, if "default" is set, then virtual hosts
 * with no subdirectory are all shunted into the subdirectory specified.
 * 
 * @author		Stephen Uhler
 * @version		2.1, 02/10/01
 */

public class VirtualHostHandler implements Handler {
    boolean mapRoot = false;	// map the root, don't rewrite the url
    String prefix;

    public boolean
    init(Server server, String prefix) {
        this.prefix = prefix;
	if (server.props.getProperty(prefix + "maproot") != null) {
	    mapRoot=true;
	}
	server.log(Server.LOG_DIAGNOSTIC, prefix,
	    "Starting virtual host handler (maproot=" + mapRoot + ")");
	return true;
    }

    /**
     * Either look for host header, tack on front of url,
     * or modify the "root" property
     */

    public boolean
    respond(Request request) throws IOException {
    	String host = (String) request.headers.get("Host");
    	if (host == null) {
    	    host = "default";
	} else {
            host = host.toLowerCase();
	}
	int index = host.indexOf(":");
	if (index >= 0) {
	    host = host.substring(0,host.indexOf(":"));
	}

	// strip off proper levels

	String levels = request.props.getProperty(prefix + "levels");
	int level = 0;
	String prepend = "";
	if (levels != null) {
	    try {
	        level = Integer.decode(levels).intValue();
		StringTokenizer st = new StringTokenizer(host, ".");
		int tokens = st.countTokens();
		boolean shouldAdd = Format.isTrue(request.props.getProperty(
			prefix + "addlevel"));
		while (level > 0 && tokens > level) {
		   if (shouldAdd) {
		       prepend = "/" + st.nextToken() + prepend;
		   } else {
		       st.nextToken();
		   }
		   tokens--;
		}
		String old = host;
		host = st.nextToken("\n").substring(1);
		request.log(Server.LOG_DIAGNOSTIC, prefix,
			"Stripping host: " + old + " => " + host);
	    } catch (NumberFormatException e) {}
	}

	String map = request.props.getProperty(prefix + host);
	if (map != null) {
	    request.log(Server.LOG_DIAGNOSTIC, prefix, "mapping host: " + host +
		    " =>" + map);
	    host = map;
	} else if ((map=request.props.getProperty(prefix + "default")) != null) {
	    request.log(Server.LOG_DIAGNOSTIC, prefix, "default: " + host +
		" to " + map);
            host = map;
	}

	String root = request.props.getProperty("root",".") + "/" + host +
		prepend;
	if (mapRoot) {
	    request.log(Server.LOG_DIAGNOSTIC, prefix, 
		    "root changed: " + request.props.getProperty("root","?") + 
		    " => " + root);
	    request.props.put("root", root);
	} else {
	    String old = request.url;
	    request.url = "/" + host.toLowerCase() + prepend + request.url;
	    request.log(Server.LOG_DIAGNOSTIC, prefix, 
		    " Mapping url: " + old + " => " + request.url);
	}
	return false;
    }
}
