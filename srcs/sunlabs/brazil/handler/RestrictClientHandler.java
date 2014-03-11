/*
 * RestrictClientHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2006 Sun Microsystems, Inc.
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
 * Created by suhler on 00/12/21
 * Last modified by suhler on 06/11/13 15:02:20
 *
 * Version Histories:
 *
 * 2.2 06/11/13-15:02:20 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.1 02/10/01-16:36:38 (suhler)
 *   version change
 *
 * 1.7 02/04/24-12:58:02 (suhler)
 *   adjust log levels
 *
 * 1.6 02/02/05-11:41:58 (suhler)
 *   - chaned "restrict" to "allow"
 *   - added a hostname based "deny"
 *
 * 1.5 01/09/12-16:41:16 (suhler)
 *   change match -> restrict
 *
 * 1.4 01/07/20-11:32:46 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.3 01/07/17-14:15:17 (suhler)
 *   use MatchUrl
 *
 * 1.2 01/02/19-10:44:49 (suhler)
 *   add redirect if not authorized
 *
 * 1.2 00/12/21-11:33:59 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/RestrictClientHandler.java
 *
 * 1.1 00/12/21-11:33:58 (suhler)
 *   date and time created 00/12/21 11:33:58 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.IOException;
import java.net.InetAddress;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.util.regexp.Regexp;

/**
 * Simple access control hander based on source ip addresses.
 * Compare the ip address of the client with a regular expression.
 * Only allow access to the specified url prefix if there is a match.
 * <p>
 * Properties:
 * <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URL that triggers this handler.
 * (See {@link MatchString}).
 * <dt>allow	<dd>The regular expression that matches the
 *		ip addresses of clients (in xxx.xxx.xxx.xxx format)
 *		that are permitted to access url's starting with
 *		<code>prefix</code>.
 * <dt>deny	<dd>The regular expression that matches the set of ip
 *		<b>names</b> that should be denied access.  This is to
 *		make complying with silly EAR requirements easier.  The use
 *		of this option implies a reverse DNS lookup, which could be
 *		expensive, as DNS names (and not ip addresses) are used for
 *		the comparison.  Case insensitive matching is used.
 * <dt>redirect	<dd>Name of the url to re-direct to if permission is denied.
 *		    If not specified, a simple message is sent to the client.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		2.2, 06/11/13
 */

public class RestrictClientHandler implements Handler {
    String propsPrefix;		// our name in the properties file
    MatchString isMine;         // check for matching url
    Regexp allow=null;		// regexp to match allowed ip addresses
    Regexp deny=null;		// regexp to match denies hostnames
    String redirect;		// where to redirect denials to (if any)

    final static String REDIRECT = "redirect";

    public boolean
    init(Server server, String prefix) {
	propsPrefix = prefix;
	isMine = new MatchString(prefix, server.props);
	redirect = server.props.getProperty(prefix + REDIRECT);
	String str = server.props.getProperty(propsPrefix + "allow");
	if (str != null) {
	   try {
	     allow = new Regexp(str);
	   } catch (Exception e) {
	       server.log(Server.LOG_WARNING, prefix,
		   "Invalid regular expression for \"allow\"");
	       return false;
	   }
        }
	str = server.props.getProperty(propsPrefix + "deny");
	if (str != null) {
	   try {
	     deny = new Regexp(str, true);
	   } catch (Exception e) {
	       server.log(Server.LOG_WARNING, prefix,
		   "Invalid regular expression for \"deny\"");
	       return false;
	   }
        }
	if (allow == null && deny == null) {
	   server.log(Server.LOG_WARNING, prefix,
		"nether \"deny\" or \"allow\" is specified");
	   return false;
	}
	return true;
    }

    public boolean
    respond(Request request) throws IOException {
	if (!isMine.match(request.url)) {
	    return false;
        }
	InetAddress inet = request.getSocket().getInetAddress();

	if ((deny != null && deny.match(inet.getHostName()) == null) ||
	        (allow != null && allow.match(inet.getHostAddress()) != null)) {
	    request.log(Server.LOG_LOG, propsPrefix,
		    "Allowing: " + inet.toString());
	    return false;
	} else if (redirect!=null) {
	    request.redirect(redirect,null);
	} else {
	    request.sendError(403, inet.getHostAddress() +
		   " is not authorized to obtain " + request.url);
	}
	return true;
    }
}
