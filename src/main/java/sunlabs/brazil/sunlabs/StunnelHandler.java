/*
 * StunnelHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2003 Sun Microsystems, Inc.
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
 * Version:  1.6
 * Created by suhler on 03/05/12
 * Last modified by suhler on 03/10/24 10:18:53
 *
 * Version Histories:
 *
 * 1.6 03/10/24-10:18:53 (suhler)
 *   doc fix
 *
 * 1.5 03/08/01-16:20:15 (suhler)
 *   fixes for javadoc
 *   add "protocol" option to work with arbitrary protocol gateways
 *
 * 1.4 03/07/14-09:58:33 (suhler)
 *   doc fixes
 *
 * 1.3 03/07/07-14:08:33 (suhler)
 *   typos
 *
 * 1.2 03/05/19-14:23:22 (suhler)
 *   deal with the "default" port changing from "80" to "443" when
 *   mapping from "http" to "https".  This allows the proxy to work
 *   on the default ssl port
 *
 * 1.2 03/05/12-16:33:04 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/StunnelHandler.java
 *
 * 1.1 03/05/12-16:33:03 (suhler)
 *   date and time created 03/05/12 16:33:03 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import java.io.IOException;
import java.net.InetAddress;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.regexp.Regexp;

/**
 * Handler to enable proper interaction with a protocol conversion 
 * gateway, by rewriting "redirect" directives properly.
 * For example, this handler may be used with
 * stunnel (see 
 * <a href=http://www.stunnel.org)>stunnel.org</a>), configured as an SSL gateway.
 * enabling Brazil with an external ssl protocol stack.
 * For example, the stunnel configuration
 * <pre>
 *  [https]
 *  accept  = 443
 *  connect = 8080
 * </pre>
 * Will allow "https" connections on the standard port ssl (443) to access a Brazil
 * server on port 8080.
 *
 * When using Brazil in this configuration without this handler, since
 * Brazil talks to the gateway via "http", it will issue redirects to "http",
 * which is the wrong protocol.
 * This template looks at the origin ip address, and if
 * it matches, changes the server protocol for this request, resulting in
 * the client redirecting back through the gateway properly.
 * <p>
 * Properties:
 * <dl class=props>
 * <dt>ssl	<dd>The regexp to match client ip addresses that are coming
 *		from ssl gateways (such as stunnel).
 * <dt>protocol <dd>The protocol to replace "http" with when redirection
 *              via a gateway (defaults to "https").
 * </dl>
 */

public class StunnelHandler implements Handler {
    String prefix;
    Regexp ssl=null;		// regexp to match ssl'd ip addresses
    String protocol;		// the protocol to use, defaults to "https"

    public boolean
    init(Server server, String prefix) {
	String str = server.props.getProperty(prefix + "ssl");
	protocol = server.props.getProperty(prefix + "protocol", "https");
	this.prefix = prefix;
	if (str != null) {
	   try {
	     ssl = new Regexp(str);
	   } catch (Exception e) {
	       server.log(Server.LOG_WARNING, prefix,
		   "Invalid regular expression for \"ssl\" gateway");
	       return false;
	   }
        }
	return true;
    }

    /**
     * If we are coming from the machine which is designated as our ssl 
     * gateway, then we need to change the protocol to "https" and
     * remap the default port.
     */

    public boolean
    respond(Request request) throws IOException {
	InetAddress inet = request.getSocket().getInetAddress();
	System.out.println("GOT " + inet.getHostAddress());
	if (ssl.match(inet.getHostAddress()) != null) {
	    String host = request.headers.get("Host");
	    if (host != null && host.indexOf(":") < 0) {
		request.headers.put("Host", host + ":443");
	    }
	    request.serverProtocol=protocol;
	    request.log(Server.LOG_LOG, prefix,
			"mapping to ssl: " + inet.toString());
	}
	return false;
    }
}
