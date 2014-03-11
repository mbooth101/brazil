/*
 * TestHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.
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
 * Version:  2.1
 * Created by cstevens on 99/11/09
 * Last modified by suhler on 02/10/01 16:37:34
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:37:34 (suhler)
 *   version change
 *
 * 1.4 00/04/24-13:04:17 (cstevens)
 *   wildcard imports
 *
 * 1.3 99/11/17-15:37:23 (cstevens)
 *   testing
 *
 * 1.2 99/11/16-14:37:39 (cstevens)
 *   test handler for test scripts.
 *
 * 1.2 99/11/09-20:22:04 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 tests/TestHandler.java
 *
 * 1.1 99/11/09-20:22:03 (cstevens)
 *   CodeManager Uniquification: tests/TestHandler.java
 *   date and time created 99/11/09 20:22:03 by cstevens
 *
 */

package tests;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

public class TestHandler
    implements Handler
{
    public boolean
    init(Server server, String prefix)
    {
	return true;
    }

    public boolean
    respond(Request request)
	throws IOException
    {
	String url = request.url;
	Hashtable query = request.getQueryData();
	
	request.log(Server.LOG_INFORMATIONAL, "TestHandler", request.url);
	if (request.method.equals("PUMPKIN")) {
	    request.log(Server.LOG_INFORMATIONAL, "pumpkin method");
	    request.sendResponse("");
	    return true;
	} else if (url.equals("/index.html")) {
	    request.sendResponse("<title>index.html</title>");
	    return true;
	} else if (url.equals("/IOException")) {
	    throw new IOException((String) query.get("msg"));
	} else if (url.equals("/Exception")) {
	    RuntimeException e;
	    try {
		String name = (String) query.get("type");
		e = (RuntimeException) Class.forName(name).newInstance();
	    } catch (Exception e2) {
		return false;
	    }
	    throw e;
	} else if (query.get("property") != null) {
	    String name = (String) query.get("name");
	    String value = (String) query.get("value");
	    Properties props = request.props;
	    if (name == null) {
		StringBuffer sb = new StringBuffer();
		Enumeration e = props.propertyNames();
		while (e.hasMoreElements()) {
		    String key = (String) e.nextElement();
		    sb.append(key).append("=");
		    sb.append(getProp(props, key));
		    sb.append("\n");
		}
		value = sb.toString();
	    } else if (value == null) {
		value = getProp(props, name);
		if (value == null) {
		    value = "";
		}
		value = name + "=" + value;
	    } else {
		props.put(name, value);
	    }
	    request.sendResponse(value, "text/plain");
	    return true;
	}

	return false;
    }

    String
    getProp(Properties props, String key)
    {
	try {
	    String result = props.getProperty(key);
	    if (result != null) {
		return result;
	    }
	} catch (ClassCastException e) {}

	Object obj = props.get(key);
	if (obj == null) {
	    return null;
	} else if (obj instanceof byte[]) {
	    return new String((byte[]) obj, 0);
	} else {
	    return obj.getClass().getName();
	}
    }
	
}
