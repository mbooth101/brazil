/*
 * CopyContentFilter.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2004 Sun Microsystems, Inc.
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
 * Created by suhler on 00/02/16
 * Last modified by suhler on 04/11/30 15:19:38
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:38 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:39:04 (suhler)
 *   version change
 *
 * 1.5 00/12/11-13:26:15 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.4 00/05/31-13:44:47 (suhler)
 *   doc cleanup
 *
 * 1.3 00/05/22-14:03:00 (suhler)
 *
 * 1.2 00/03/10-16:58:39 (cstevens)
 *   wildcard imports
 *
 * 1.2 00/02/16-11:50:57 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 filter/CopyContentFilter.java
 *   Name history : 1 0 tail/CopyContentFilter.java
 *
 * 1.1 00/02/16-11:50:56 (suhler)
 *   date and time created 00/02/16 11:50:56 by suhler
 *
 */

package sunlabs.brazil.filter;

import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.http.MimeHeaders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Filter to save content (of an entire site) to a disk file.
 * This is used to "steal" other sites.  It is expected to be used
 * in conjunction with  a {@link sunlabs.brazil.handler.GenericProxyHandler}.
 *
 * Only files that don't already exist on the local file system are
 * saved.  
 *
 * Properties:
 *<dl class=props>
 *<dt>directoryName  <dd>The root in the file system to save the content in
 *</dl>
 *
 * @author		Stephen Uhler
 * @version		2.2
 */

public class CopyContentFilter implements Filter {
    static final String DIR = "directoryName";
    String prefix;	// the properties prefix
    File dir;		// root directory to steal stuff in

    public boolean
    init(Server server, String prefix) {
	String name = server.props.getProperty(prefix + DIR);
	this.prefix = prefix;
	if (name.startsWith("/")) {
	    dir = new File(name);
	} else {
	    String root = server.props.getProperty("root",".");
	    dir = new File(root, name);
	}
	if (dir.mkdir() || dir.isDirectory()) {
	    return true;
	} else {
	    server.log(Server.LOG_WARNING, prefix, 
			"Can't write into: " + dir);
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
     * Watch every document that passes by.  If the HTTP rerun code is
     * "200", plan to save the content on the local file system.
     */

    public boolean
    shouldFilter(Request request, MimeHeaders headers) {
	return (headers.get("status").indexOf("200") >= 0); 
    }

    /**
     * Grab the contents, and save as a file (if file doesn't already exist).
     * The URL is mapped into a pathname starting from <code>directoryName
     * </code>.
     */

    public byte[]
    filter(Request request, MimeHeaders headers, byte[] content) {
	File file = new File(dir,FileHandler.urlToPath(request.url));
	if (!file.exists()) {
	    try {
		(new File(file.getParent())).mkdirs();
		FileOutputStream out = new FileOutputStream(file);
		out.write(content);
		out.close();
		request.log(Server.LOG_DIAGNOSTIC, prefix,
			"Saved " + content.length + " bytes to: " + file);
	    } catch (IOException e) {
		request.log(Server.LOG_WARNING, prefix, " can't write " +
			file + ": " + e);
	    }
	}
	return content;
    }
}
