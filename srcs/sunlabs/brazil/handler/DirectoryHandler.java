/*
 * DirectoryHandler.java
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
 * Last modified by suhler on 06/11/13 15:02:14
 *
 * Version Histories:
 *
 * 2.3 06/11/13-15:02:14 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.2 03/07/07-14:43:12 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 2.1 02/10/01-16:36:32 (suhler)
 *   version change
 *
 * 1.17.1.1 02/09/04-11:05:49 (suhler)
 *   formatting
 *
 * 1.17 02/07/24-10:45:02 (suhler)
 *   doc updates
 *
 * 1.16 02/04/25-14:08:10 (suhler)
 *   doc update
 *
 * 1.15 01/07/20-11:31:56 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.14 01/07/17-14:15:38 (suhler)
 *   use MatchUrl
 *
 * 1.13 00/12/11-20:23:36 (suhler)
 *   doc typs
 *
 * 1.12 00/12/11-13:28:07 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.11 00/10/31-10:17:58 (suhler)
 *   doc fixes
 *
 * 1.10 00/07/06-15:48:45 (suhler)
 *   doc update
 *
 * 1.9 00/05/31-13:47:00 (suhler)
 *   doc cleanup
 *
 * 1.8 00/05/10-16:30:04 (suhler)
 *   Added option to return directory info in request.props using
 *   the "setProps" option
 *
 * 1.7 00/04/20-11:49:46 (cstevens)
 *   copyright.
 *
 * 1.6 00/03/02-17:45:27 (cstevens)
 *   NullPointerException if directory had no files in it or was not readable.
 *
 * 1.5 99/09/15-14:40:00 (cstevens)
 *   Rewritign http server to make it easier to proxy requests.
 *
 * 1.4 99/08/06-12:04:43 (suhler)
 *   use sort in util directory
 *
 * 1.3 99/03/31-14:42:09 (suhler)
 *   fixed comment typo
 *
 * 1.2 99/03/30-09:33:00 (suhler)
 *   documentation update
 *
 * 1.2 99/03/29-15:33:30 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/DirectoryHandler.java
 *
 * 1.1 99/03/29-15:33:29 (suhler)
 *   date and time created 99/03/29 15:33:29 by suhler
 *
 */

package sunlabs.brazil.handler;

import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.util.http.HttpUtil;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.server.FileHandler;
import java.io.IOException;
import java.io.File;
import java.util.Vector;
import sunlabs.brazil.util.Sort;

/**
 * This is a bare-bones handler for providing directory listings
 * for web servers.
 * It is designed to be placed after the
 * {@link sunlabs.brazil.server.FileHandler}.  If no index file is found, 
 * Then a simple directory listing will be produced.  Only files whose
 * extensions are in the mime properties will be listed.
 * <p>
 * NOTE: This handler is obsolete, as it provides no control over the
 * format of the directory listing, 
 * Use the {@link sunlabs.brazil.template.DirectoryTemplate} instead.
 * <p>
 * Configuration properties used:
 * <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URL that triggers this handler.
 * (See {@link MatchString}).
 * <dt>DirectoryName	<dd>This property is set by the
 *			{@link sunlabs.brazil.server.FileHandler} if the
 *			URL it was passed resolves to a directory, but no
 *			index file (e.g. index.html) was found.
 * <dt>setProps		<dd>If present, no content is returned.  Instead, 
 *			The properties "Directories" and "Files" are
 *			set in the request properties, so the format of
 *			the output may be generated dynamically.
 *			[Note: This feature is deprecated, use the
 *			{@link sunlabs.brazil.template.DirectoryTemplate}
 *			instead].
 * <dt>delim		<dd>The delimeter separating the file names. 
 *			Defaults to a single space.
 * <dt>mime.xxx		<dd>Only documents ending in ".xxx" are considered.
 *			more than on mime.xxx parameters may be specified.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		2.3, 06/11/13
 */

public class DirectoryHandler implements Handler {
    private Server server;
    private String propsPrefix;
    private static final String PREFIX = "prefix";      // url prefix
    MatchString isMine;            // check for matching url

    /**
     * Get the url prefix for this handler.
     */

    public boolean
    init(Server server, String prefix) {
    	this.server = server;
	propsPrefix = prefix;
	isMine = new MatchString(prefix, server.props);
	return true;
    }

    /**
     * Display files in a directory, after being rejected by the 
     * FileHandler.   The output is very simple.
     */

    public boolean
    respond(Request request) throws IOException {
    	String directory = request.props.getProperty("DirectoryName");
	if (directory == null || !isMine.match(request.url)) {
	    return false;
	}

	String[] list = (new File(directory)).list();
	Vector files = new Vector();
	Vector dirs = new Vector();

	if (list != null) {
	    for(int i = 0; i < list.length; i++) {
		String name = list[i];
		int index = name.lastIndexOf(".");
		if ((new File(directory, name)).isDirectory()) {
		    dirs.addElement(name);
		} else if (index > 0 &&
			server.props.containsKey(FileHandler.MIME +
			name.substring(index))) {
		    files.addElement(name);
		}
	    }
	}

	/*
	 * Just set properties.  We should be more flexible here.  This
	 * doesn't work with embedded spaces for now!
	 */

	if (request.props.getProperty(propsPrefix + "setProps") != null) {
	    String delim=request.props.getProperty(propsPrefix + "delim", " ");
	    StringBuffer dir = new StringBuffer();
	    for (int i = 0; i < dirs.size(); i++) {
		if (i>0) {
		    dir.append(delim);
		}
		dir.append((String) dirs.elementAt(i));
	    }
	    StringBuffer file = new StringBuffer();
	    for (int i = 0; i < files.size(); i++) {
		if (i>0) {
		    file.append(delim);
		}
		file.append((String) files.elementAt(i));
	    }
	    request.props.put("Directories", dir.toString());
	    request.props.put("Files", file.toString());
	    return false;
	}

	/*
	 * simple output for now
	 */

	StringBuffer result = new StringBuffer();
	result.append("<title>Directory Listing</title>\n");
	result.append("<h1>Directory Listing</h1>\n");
	result.append("<a href=..><b>parent directory</b></a>\n");
	if (dirs.size() > 0) {
	    Sort.qsort(dirs);
	    result.append("<h2>Directories</h2>\n");
	    list(dirs, result);
	}
	if (files.size() > 0) {
	    Sort.qsort(files);
	    result.append("<h2>Files</h2>\n");
	    list(files, result);
	}
	request.sendResponse(result.toString());
	return true;
    }

    /*
     * List the elements of the directory in a row
     */

    private void list(Vector v, StringBuffer result) {
	for (int i = 0; i < v.size(); i++) {
	    String name = (String) v.elementAt(i);
	    result.append("<a href=\"" + name  + " \">" +
		    HttpUtil.htmlEncode(name) + "</a><br>\n");
	}
    }
}
