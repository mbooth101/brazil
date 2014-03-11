/*
 * DefaultFileHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2005 Sun Microsystems, Inc.
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
 * Created by suhler on 00/08/16
 * Last modified by suhler on 05/06/28 10:16:54
 *
 * Version Histories:
 *
 * 2.3 05/06/28-10:16:54 (suhler)
 *   nit
 *
 * 2.2 03/08/01-16:18:28 (suhler)
 *   fixes for javadoc
 *
 * 2.1 02/10/01-16:36:37 (suhler)
 *   version change
 *
 * 1.3 01/08/07-11:00:15 (suhler)
 *   add (optional) code for dealing with ordinary files that have a
 *   trailing '/' by mistake.
 *
 * 1.2 00/12/11-13:29:00 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.2 00/08/16-12:09:33 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/DefaultFileHandler.java
 *
 * 1.1 00/08/16-12:09:32 (suhler)
 *   date and time created 00/08/16 12:09:32 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

/**
 * Handler for appending a url ending with '/' into the appropriate
 * url based on a default file in the file system.
 * <p>
 * The following request properties are used:
 * <dl class=props>
 * <dt>defaults<dd>The names of the default files to search for in
 *                 the directory implied by the URL.
 *		   The first one that exists will
 *		   cause its name to be appended to the URL.  Defaults
 *		   to "index.html".
 * <dt>root<dd>The document root to look for files.  If none is found with our
 *			prefix, then "root" is examined.  Defaults to ".".
 * <dt>DirectoryName<dd>This property is set if the URL represents a valid
 *			directory in the document root.
 * <dt>fileName<dd>This property is set to the name of the default file, 
 *			if one was found.
 * </dl>
 * If a url ends with "/", but is a readable plain file, the "/" is
 * removed
 *
 * @author		Stephen Uhler
 * @version		%V% 05/06/28
 */

public class DefaultFileHandler implements Handler {
    static final String DEFAULTS = "defaults"; // list of default files
    static final String ROOT = "root";    // document root
    String prefix;			  // our properties prefix

    /**
     * Remember our prefix in the properties table.
     */

    public boolean
    init(Server server, String prefix) {
	this.prefix = prefix;
	return true;
    }

    /**
     * If the url ends with a "/" look around in the corrosponding directory
     * to find a suitable default file, and then change the url.
     *
     * @return		Always returns false.
     */

    public boolean
    respond(Request request) throws IOException {
	if (!request.url.startsWith("/") || !request.url.endsWith("/")) {
	    return false;
	}
	String root = request.props.getProperty(prefix + ROOT,
		request.props.getProperty(ROOT, "."));
	String dir = root + FileHandler.urlToPath(request.url);
	request.log(Server.LOG_DIAGNOSTIC, prefix, "Checking directory: "+ dir);
	File base = new File(dir);

	if (!base.isDirectory()) {
	    request.log(Server.LOG_DIAGNOSTIC, prefix, "  Not a directory");

/*

	    // See if its a "real file" with a redundant "/"
	    // If so, remove the / and redirect

	    File check = new File(dir.substring(0, dir.length() -1));
	    if (check.isFile() && check.canRead()) {
		request.redirect(request.url.substring(0,
			request.url.length()-1), null);
		request.log(Server.LOG_DIAGNOSTIC, prefix,
		    "Plain file; redirect with superfluous / removed");
		return true;
	    }
*/
	    return false;
	}
	request.props.put("DirectoryName", base.getPath());

	StringTokenizer st = new StringTokenizer(request.props.getProperty(
		prefix + DEFAULTS, "index.html"));
	while (st.hasMoreTokens()) {
	    String token = st.nextToken();
	    File name = new File(base, token);
	    request.log(Server.LOG_DIAGNOSTIC, prefix, name + " ?");
	    if (name.isFile()) {
	    	request.url += token;
		request.props.put("fileName", name.getPath());
		request.log(Server.LOG_DIAGNOSTIC, prefix, 
			"new url: " + request.url);
	    	break;
	    }
	}
	return false;
    }
}
