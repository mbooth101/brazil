/*
 * PropertiesHandler.java
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
 * Version:  2.3
 * Created by suhler on 00/08/28
 * Last modified by suhler on 06/11/13 15:03:56
 *
 * Version Histories:
 *
 * 2.3 06/11/13-15:03:56 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.2 04/08/30-09:02:24 (suhler)
 *   "enum" became a reserved word, change to "enumer".
 *
 * 2.1 02/10/01-16:36:34 (suhler)
 *   version change
 *
 * 1.7 02/07/24-10:45:36 (suhler)
 *   doc updates
 *
 * 1.6 02/05/01-11:20:31 (suhler)
 *   add version info
 *
 * 1.5 01/07/20-11:32:37 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.4 01/07/17-14:14:35 (suhler)
 *   use MatchUrl
 *
 * 1.3 00/12/11-13:29:05 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.2 00/10/31-10:18:31 (suhler)
 *   doc fixes
 *
 * 1.2 00/08/28-08:35:11 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/PropertiesHandler.java
 *
 * 1.1 00/08/28-08:35:10 (suhler)
 *   date and time created 00/08/28 08:35:10 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Glob;
import sunlabs.brazil.util.MatchString;

/**
 * Handler for returning selected request properties as a text/plain document
 * in java properties format. 
 * A server using this handler may be called
 * by a server using the {@link ProxyPropertiesHandler}
 * to communicate per-request properties between the two servers.
 * <p>
 * Properties:
 * <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URL that triggers this handler.
 * (See {@link MatchString}).
 * <dt>select	<dd>Glob pattern to match properties selected
 *		(Defaults to *).  This is re-examined at every request.
 * <dt>type	<dd>Type of output to generate (defaults to text/plain).
 * <dt>comment	<dd>Comment to put on output (defaults to <i>select</i>).
 * </dl>
 *
 * @author      Stephen Uhler
 * @version	@(#)PropertiesHandler.java	2.3
 */

public class PropertiesHandler implements Handler {
    String prefix;		// our handler name
    String select;		// glob pattern to select props
    String type;
    MatchString isMine;            // check for matching url

    public boolean
    init(Server server, String prefix) {
	this.prefix = prefix;
	isMine = new MatchString(prefix, server.props);
	type = server.props.getProperty(prefix + "type", "text/plain");
	return true;
    }

    /**
     * If this is one of our URL's, look through each
     * request property, and selct those that match the <i>Select</i>
     * property.  Then emit them all as text/plain.
     */

    public boolean
    respond(Request request) throws IOException {
	if (!isMine.match(request.url)) {
	    return false;
	}
	String select = request.props.getProperty(prefix + "select", "*");

	Properties p = new Properties();
	Enumeration enumer = request.props.propertyNames();
	while(enumer.hasMoreElements()) {
	    String key = (String) enumer.nextElement();
	    if (Glob.match(select, key)) {
		p.put(key, request.props.getProperty(key));
	    }
	}
	String comment = request.props.getProperty(prefix + "comment");
	if (comment == null) {
	    comment = prefix + this.getClass() + " selecting: " + select;
	}
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	p.save(out,comment);
	request.sendResponse(out.toByteArray(), type);
	return true;
    }
}
