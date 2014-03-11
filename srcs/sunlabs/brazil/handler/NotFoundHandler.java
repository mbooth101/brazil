/*
 * NotFoundHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2006 Sun Microsystems, Inc.
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
 * Created by suhler on 99/03/29
 * Last modified by suhler on 06/11/13 15:03:49
 *
 * Version Histories:
 *
 * 2.3 06/11/13-15:03:49 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.2 03/08/01-16:18:23 (suhler)
 *   fixes for javadoc
 *
 * 2.1 02/10/01-16:36:32 (suhler)
 *   version change
 *
 * 1.13 02/07/24-10:44:50 (suhler)
 *   doc updates
 *
 * 1.12 02/04/28-20:09:13 (suhler)
 *   doc update
 *
 * 1.11 01/07/20-11:31:44 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.10 01/07/17-14:14:58 (suhler)
 *   use MatchUrl
 *
 * 1.9 00/12/11-13:27:46 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.8 00/05/15-10:07:51 (suhler)
 *   removed unused bufSize parameter
 *
 * 1.7 00/04/20-11:48:59 (cstevens)
 *   copyright.
 *
 * 1.6 00/02/27-19:51:01 (suhler)
 *   add "type" parameter to send type of not-found file
 *
 * 1.5 99/10/01-11:26:06 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.4 99/09/15-14:39:24 (cstevens)
 *   Rewritign http server to make it easier to proxy requests.
 *
 * 1.3 99/06/28-10:50:19 (suhler)
 *   no change
 *
 * 1.2 99/03/30-09:32:49 (suhler)
 *   documentation update
 *
 * 1.2 99/03/29-15:33:30 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/NotFoundHandler.java
 *
 * 1.1 99/03/29-15:33:29 (suhler)
 *   date and time created 99/03/29 15:33:29 by suhler
 *
 */

package sunlabs.brazil.handler;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.util.MatchString;
import java.io.File;
import java.io.IOException;

/**
 * Handler for returning "file not found" errors back to the client.
 * Look for the file "NotFound.html" in the current directory, and return it
 * if it exists.  Otherwise, return the "NotFound.html" file in the document
 * root directory.  If neither can be found, then punt, and let someone else
 * deal with it.
 * <p>
 * If more sophisticated processing is desired, then the
 * {@link UrlMapperHandler}
 * may be used in combination with the
 * {@link sunlabs.brazil.template.TemplateHandler}.
 *
 * <p>
 * Configuration parameters understood by this handler
 * <dl class=props>
 * <dt>root	<dd>The location of the document root for locating the
 *		default "not found" file (also looks using prefix of "").
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URL that triggers this handler.
 * (See {@link MatchString}).
 * <dt>fileName	<dd>The name of the file to send for missing files.
 *		Defaults to "notfound.html"
 * <dt>type	<dd>The file type, defaults to text/html
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		2.3, 06/11/13
 */

public class NotFoundHandler implements Handler {
    static final String NAME =   "fileName";  // name of the not-found file
    static final String TYPE =   "type";  // type of the not-found file (should be looked up from the suffix)

    MatchString isMine;            // check for matching url
    String fileName;		// name of "not found" file
    String type;		// file type
    File rootFile;		// root not-found file

    /**
     * Extract the handler properties.
     * Get the URL prefix and default "missing" file name.
     */

    public boolean
    init(Server server, String prefix) {
	isMine = new MatchString(prefix, server.props);
	fileName = server.props.getProperty(prefix + NAME, "notfound.html");
	type = server.props.getProperty(prefix + TYPE, "text/html");
	String root = server.props.getProperty(prefix + FileHandler.ROOT,
		server.props.getProperty(FileHandler.ROOT, "."));
	rootFile = new File(root, fileName);
	server.log(Server.LOG_DIAGNOSTIC, prefix,
	    "looking for: " + rootFile);
	if (!rootFile.isFile()) {
	    server.log(Server.LOG_WARNING, prefix, "Can't find file: " + rootFile);
	}
	return true;
    }

    /**
     * Look for and deliver the "not found" file
     * Look in the current directory first, then in the doc root.
     * Only files whose suffixes have valid mime types are delivered.
     */

    public boolean
    respond(Request request) throws IOException {
	if (!isMine.match(request.url)) {
	    return false;
	}
	String missing = request.props.getProperty("fileName");
	if (missing == null) {
	    request.log(Server.LOG_DIAGNOSTIC, "No missing file found!!");
	    return false;
	}
	File name = new File((new File(missing)).getParent(), fileName);
	if (name.canRead() && name.isFile()) {
	    FileHandler.sendFile(request, name, 404, type);
	    request.log(Server.LOG_DIAGNOSTIC, "sending not-found file");
	} else if (rootFile.canRead() && rootFile.isFile())  {
	    FileHandler.sendFile(request, rootFile, 404, type);
	    request.log(Server.LOG_DIAGNOSTIC, "sending Root not-found file");
	} else {
	    return false;
	}
	return true;
    }
}
