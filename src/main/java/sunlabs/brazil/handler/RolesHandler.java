/*
 * RolesHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1998-2006 Sun Microsystems, Inc.
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
 * Created by suhler on 98/09/30
 * Last modified by suhler on 06/11/13 15:04:24
 *
 * Version Histories:
 *
 * 2.3 06/11/13-15:04:24 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.2 03/07/15-09:11:13 (suhler)
 *   Doc fixes for pdf version of the manual
 *
 * 2.1 02/10/01-16:36:27 (suhler)
 *   version change
 *
 * 1.14 02/07/24-10:44:55 (suhler)
 *   doc updates
 *
 * 1.13 01/07/20-11:31:25 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.12 01/07/17-14:15:27 (suhler)
 *   use MatchUrl
 *
 * 1.11 00/07/07-17:01:22 (suhler)
 *   remove System.out.println(s)
 *
 * 1.10 00/05/19-11:48:15 (suhler)
 *   changed default request.props names for consistency with other handlers
 *
 * 1.9 00/04/20-11:49:07 (cstevens)
 *   copyright.
 *
 * 1.8 99/10/01-11:26:12 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.7 99/09/15-14:39:29 (cstevens)
 *   Rewritign http server to make it easier to proxy requests.
 *
 * 1.6 99/03/30-09:29:33 (suhler)
 *
 * 1.5 99/01/05-10:57:26 (suhler)
 *   removed wildcarded imports
 *
 * 1.4 98/10/15-09:26:07 (suhler)
 *   fixed syntax error
 *
 * 1.3 98/10/15-08:55:45 (suhler)
 *   checkpoint - adding "check" flag - in progress
 *   .
 *
 * 1.2 98/10/13-08:10:37 (suhler)
 *   lots of stuff
 *
 * 1.2 98/09/30-09:46:04 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/RolesHandler.java
 *
 * 1.1 98/09/30-09:46:03 (suhler)
 *   date and time created 98/09/30 09:46:03 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

/**
 * Handler for associating roles with an id.  This is a placeholder
 * until the SunEconomy gets integrated in.  It looks for an "id" in the 
 * request, looks it up in a property file, then adds the value of the
 * id into the request.  It may be used in conjunction with
 * {@link AclSwitchHandler} to provide role based web access.
 * Properties:
 * <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URL that triggers this handler.
 * (See {@link MatchString}).
 * <dt>SessionID
 * <dd>The property to use to look up the id.  Defaults to "SessionID".
 * <dt>roleName
 * <dd>The property to place the result of the id lookup into. 
 *     Defaults to "roleName";
 * <dt>mapFile
 * <dd>The absolute path to the java properties file containing the it to role mapping.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		 @(#) RolesHandler.java 2.3 06/11/13 15:04:24
 */

public class RolesHandler implements Handler {
    Properties map;		// The authorization mapping table
    String propsPrefix;		// My prefix in the global properties file
    String urlPrefix;		// The prefix to look for to map
    String idKey;	// The property name for the token id
    String roleKey;	// The property name for the roles
    boolean check = true;	// only put results in request, don't return

    /**
     * Handler configuration property <b>SessionID</b>.
     * The request property name to find the id string.
     * Defaults to id.
     */

    public static final String ID_KEY = "SessionID";	// property key for token id

    /**
     * Handler configuration property <b>roleName</b>.
     * The request property name to place the roles into.
     * Defaults to roles.
     */

    public static final String ROLE_KEY = "roleName";	// property key for token roles

    /**
     * Handler configuration property <b>mapFile</b>.
     * The path to the java properties file containing the id
     * to roles mapping.  The roles are a whitespace delimited list
     * of ascii role names.
     */

    public static final String MAP = "mapFile";	// properties file

    /**
     * Handler configuration property <b>check</b>.
     * If true, the results are out into the request object.
     * Otherwise, they are returned in a text/plain java properties
     * formatted document, which can be used with the
     * @see RemoteStsHandler.
     */

    static final String CHECK = "check"; // how results are returned
    MatchString isMine;            // check for matching url

    public boolean
    init(Server server, String prefix) {
	propsPrefix = prefix;
	isMine = new MatchString(prefix, server.props);
	String mapFile = server.props.getProperty(propsPrefix + MAP,"");
	map = new Properties();
	try {
	    FileInputStream in = new FileInputStream(mapFile);
	    map.load(in);
	    in.close();
	} catch (Exception e) {
	    server.log(Server.LOG_ERROR, prefix, propsPrefix + MAP + ": ("
			+ mapFile + ") " + e.toString());
	    return false;
	}
	idKey = server.props.getProperty(propsPrefix + ID_KEY, "id");
	roleKey = server.props.getProperty(propsPrefix + ROLE_KEY, "roles");
	check = server.props.getProperty(
		propsPrefix + CHECK, "y").startsWith("y");
	return true;
    }

    /** 
     * Dispatch and handle the request.
     * This version looks at the request for the id, looks it up in the
     * table, and adds the value, if available
     */

    public boolean
    respond(Request request) throws IOException {
	if (!isMine.match(request.url)) {
	    return false;
        }
	String id = (String) request.props.get(idKey);
	// System.out.println("ids: " + map);
	// System.out.println("props: " + request.props);
	if (id != null && map.containsKey(id)) {
	    String value = (String) map.get(id);
	    request.log(Server.LOG_DIAGNOSTIC, "Mapping: " + id + "->" + value);
	    request.props.put(roleKey,value);
	}
	if (!check) {
	    StringBuffer result = new StringBuffer("");
	    Enumeration keys = request.props.keys();
	    while(keys.hasMoreElements()) {
		String key = (String) keys.nextElement();
		result.append(key + "=" + request.props.get(key) + "\n");
	    }
	    request.sendResponse(result.toString(),"text/plain");
	    return true;
	} else {
	    return false;
	}
    }
}
