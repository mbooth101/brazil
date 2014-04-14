/*
 * ReplaceFilter.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2004 Sun Microsystems, Inc.
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
 * Created by suhler on 99/09/01
 * Last modified by suhler on 04/11/30 15:19:39
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:39 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:39:02 (suhler)
 *   version change
 *
 * 1.8 02/07/24-10:44:03 (suhler)
 *   doc updates
 *
 * 1.7 02/04/25-14:10:19 (suhler)
 *   look for file in resource if not found in filesystem
 *
 * 1.6 01/02/19-10:46:37 (suhler)
 *   fix bug in "root" computation
 *
 * 1.5 00/12/11-13:26:06 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.4 00/11/07-08:55:20 (suhler)
 *   add debug flag to prevent template cacheing
 *
 * 1.3 00/05/31-13:44:37 (suhler)
 *   docs
 *
 * 1.2 99/10/06-12:30:51 (suhler)
 *   use mimeHeaders
 *
 * 1.2 99/09/01-15:35:34 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 filter/ReplaceFilter.java
 *   Name history : 1 0 tail/ReplaceFilter.java
 *
 * 1.1 99/09/01-15:35:33 (suhler)
 *   date and time created 99/09/01 15:35:33 by suhler
 *
 */

package sunlabs.brazil.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import sunlabs.brazil.handler.ResourceHandler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.http.MimeHeaders;

/**
 * Filter to replace current content with a static form, or template.
 * This should be called
 * the TemplateFilter, but that name's already taken.  The content is
 * replaced by the template lock-stock-and-barrel.  Typically, an upstream 
 * filter has extracted the relevent parts of the content, and a down-stream
 * filter will combine it with the template.
 * The filename to use for the template
 * is computed at each request, so it may be modified dynamically.
 *
 * The following server properties are used:
 * <dl class=props>
 * <dt>type	<dd>    Text subtype of content to filter.  Defaults to "html"
 * <dt>debug	<dd>    If set, the template is re-read each time.  Otherwise
 *			a cached copy is used.
 * <dt>fileName	<dd>    Name of the file to use as the form or template.
 *			The file is searched for as a <i>Resource</i> if not
 *			found in the filesystem.
 * <dt>root	<dd>	The document root used to find the template file.
 *			If not found, "root" with no prefix is used instead.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		2.2
 */

public class ReplaceFilter implements Filter {
    static final String FILE = "fileName";
    String type;	// the text sub-type to replace (defaults to html)
    String prefix;	// the properties prefix
    byte[] data;	// cache of the last template
    String path;	// cache of last file name;
    boolean debug;	// don't cache file

    public boolean
    init(Server server, String prefix) {
	type = server.props.getProperty(prefix + "type", "html");
	debug = server.props.getProperty(prefix + "debug") != null;
	this.prefix = prefix;
	path = null;
	return true;
    }

    /**
     * This is the request object before the content was fetched
     */

    public boolean respond(Request request) {
	return false;
    }

    /**
     * Only replace text documents
     */

    public boolean
    shouldFilter(Request request, MimeHeaders headers) {
	String type = headers.get("content-type");
	return (type != null && type.startsWith("text/"));
    }

    /**
     * Grab the template file name, Read in the file, and
     * deliver it as content.
     */

    public byte[]
    filter(Request request, MimeHeaders headers, byte[] content) {
	if (!(headers.get("content-type")).startsWith("text/" + type)) {
	    return content;
	}
	String name = request.props.getProperty(prefix + "fileName");
	if (name == null) {
	    request.log(Server.LOG_WARNING,"No fileName property found for " +
		prefix);
	    return content;
	}

	if (!debug && path != null && name.equals(path)) {
	    request.log(Server.LOG_DIAGNOSTIC, "using cached content for: "
		    + name);
	    return data;
	}

	try {
	    data =  ResourceHandler.getResourceBytes(request.props,
		prefix, name);
	    return data;
	} catch (IOException e) {
	    request.log(Server.LOG_WARNING, prefix + " can't get " + name +
		    ": " + e);
	    return content;
	}
    }
}
