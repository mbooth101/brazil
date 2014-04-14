/*
 * ProxyPropertiesHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1998-2004 Sun Microsystems, Inc.
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
 * Version:  2.2
 * Created by suhler on 98/09/18
 * Last modified by suhler on 04/08/30 09:02:32
 *
 * Version Histories:
 *
 * 2.2 04/08/30-09:02:32 (suhler)
 *   "enum" became a reserved word, change to "enumer".
 *
 * 2.1 02/10/01-16:36:26 (suhler)
 *   version change
 *
 * 1.18 02/08/12-13:30:31 (suhler)
 *   process ${...} constructs in the URL on a per-request basis
 *
 * 1.17 00/12/11-13:28:37 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.16 00/11/14-14:56:59 (suhler)
 *   added primitive URL mapping capability
 *
 * 1.15 00/10/31-10:18:20 (suhler)
 *   doc fixes
 *
 * 1.14 00/05/10-10:54:48 (suhler)
 *   Allow a string prefix to be prepended to all properties that are retrieved.
 *
 * 1.13 00/05/01-14:53:52 (suhler)
 *   fix doc bug
 *
 * 1.12 00/04/20-11:48:34 (cstevens)
 *   copyright.
 *
 * 1.11 00/02/02-14:49:53 (suhler)
 *   specify alternate mime types to process
 *
 * 1.10 00/01/11-13:28:19 (suhler)
 *   rename class
 *
 * 1.9 99/10/26-18:50:47 (cstevens)
 *   case
 *
 * 1.8 99/10/26-17:10:23 (cstevens)
 *   spelling
 *
 * 1.7 99/10/06-12:17:00 (suhler)
 *   new proxy api's
 *
 * 1.6 99/03/30-09:30:18 (suhler)
 *   documentation update
 *
 * 1.5 98/11/18-14:53:57 (suhler)
 *   Changed to reflect new genericProxy api's
 *
 * 1.4 98/09/21-14:54:20 (suhler)
 *   changed the package names
 *
 * 1.3 98/09/20-15:39:03 (suhler)
 *   removed *'d imports
 *
 * 1.2 98/09/18-15:05:33 (suhler)
 *   lint
 *
 * 1.2 98/09/18-10:08:55 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 3 2 handlers/ProxyPropertiesHandler.java
 *   Name history : 2 1 handlers/RemoteStsHandler.java
 *   Name history : 1 0 RemoteStsHandler.java
 *
 * 1.1 98/09/18-10:08:54 (suhler)
 *   date and time created 98/09/18 10:08:54 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.http.MimeHeaders;
import java.util.Enumeration;
import java.util.Properties;
import sunlabs.brazil.util.Format;

/**
 * Obtain properties format content from remote websites, and
 * add it to the current request properties.
 * Many of the handlers 
 * are designed to produce side effects, by inserting values into the
 * request properties (see {@link PropertiesHandler}).
 * If they are instead configured to produce the properties
 * in java properties format, then this handler
 * will read their output, and place the result in the request object on
 * their behalf.  This capability allows certain handlers to be run on
 * other web sites, yet behave as if they are in the handler chain.
 *
 * The following request properties are used:
 * <dl class=props>
 * <dt>type	<dd> The document type for files to process as
 *		     java properties (defaults to text/plain)
 * <dt>prepend	<dd> The prefix that should be prepended to each property
 *		     before it is inserted into the request properties
 * <dt>url	<dd> The url that should be used to fetch the remote content.
 *		     If not specified, the curent url is used instead. 
 *		     Any ${...} constructs in the url are evaluated at each
 *		     request.
 * </dl>
 * NOTE: This capability should be generalized.
 *
 * @author      Stephen Uhler
 * @version	2.2, 04/08/30
 */

public class ProxyPropertiesHandler extends GenericProxyHandler implements Handler {

    String type;	// document type for filtering
    String prepend = null;	// prepend all properties with this
    String mapUrl;

    public boolean
    init(Server server, String prefix) {
	type = server.props.getProperty(prefix + "type", "text/plain");
	server.log(Server.LOG_DIAGNOSTIC, prefix, "processing type: " + type);
	return super.init(server, prefix);
    }

    public boolean
    respond(Request request)
    throws IOException {
	request.log(Server.LOG_DIAGNOSTIC, prefix,
		"Calling ProxyProps handler respond");
	prepend = request.props.getProperty(prefix + "prepend");
	mapUrl = request.props.getProperty(prefix + "url");
	boolean result;
	if (mapUrl != null) {
	    String save = request.url;
	    request.url = Format.subst(request.props, mapUrl);
	    request.log(Server.LOG_DIAGNOSTIC, prefix + 
	    	"Mapping " + save + " -> " + mapUrl);
	    result = super.respond(request);
	    request.url = save;
	} else {
	    result = super.respond(request);
	}
	request.log(Server.LOG_DIAGNOSTIC, prefix, "PRoxy done: " + result);
	return result;
    }

    /**
     * See if the content needs to be filtered
     * Return "true" if "modifyContent" should be called
     * @param headers	mime headers for data to proxy
     */

    protected boolean shouldFilter(MimeHeaders headers) {
	String header = headers.get("Content-Type");
     	return (header != null && header.equals(type));
    }

    public byte[] modifyContent(Request request, byte[] content) {

	/*
 	 * Get snarf into byte input array, then do a url.props.load on it.
	 * If its not a properties object, just return it.  
	 * How do we know??
	 */

	ByteArrayInputStream in = new ByteArrayInputStream(content);
	Properties p;
	if (prepend != null) {
	    p = new Properties();
	} else {
	    p = request.props;
	}
	try {
	    p.load(in);
	    request.log(Server.LOG_INFORMATIONAL, prefix + 
		    "..  Got remote properties");
	} catch (java.io.IOException e) {
	    request.props.put("Error",e.toString());
	}

	/* add the prefix, if any (yuk!) */

	if (prepend != null) {
	    Properties to = request.props;
	    request.log(Server.LOG_INFORMATIONAL, prefix + 
		    "..  prepending: " + prepend);
	    Enumeration enumer = p.propertyNames();
	    while(enumer.hasMoreElements()) {
		String key = (String) enumer.nextElement();
		to.put(prepend + key, p.getProperty(key));
	    }
	    p = null;
	}
	try {
	    in.close();
	} catch (java.io.IOException e) {
	    request.log(Server.LOG_WARNING, prefix + 
		    "..  Close failed!! reading props: " + e);
	}
	return null;
    }
}
