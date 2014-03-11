/*
 * HomeDirHandler.java
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
 * Created by suhler on 98/09/14
 * Last modified by suhler on 04/11/03 08:29:39
 *
 * Version Histories:
 *
 * 2.2 04/11/03-08:29:39 (suhler)
 *   url.orig is now set in Request.java
 *
 * 2.1 02/10/01-16:36:23 (suhler)
 *   version change
 *
 * 1.13 01/07/17-14:16:31 (suhler)
 *   lint
 *
 * 1.12 01/06/13-09:29:45 (suhler)
 *   added option to alter "/~" for user directory detection
 *
 * 1.11 01/03/15-10:42:30 (suhler)
 *   set the request property "url.orig" if reuqest.url is changed
 *
 * 1.10 01/02/19-10:45:41 (suhler)
 *   add name of "root" property to set.
 *
 * 1.9 00/12/11-13:26:57 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.8 00/05/31-13:45:43 (suhler)
 *   doc cleanup
 *
 * 1.7 00/04/20-11:48:15 (cstevens)
 *   copyright.
 *
 * 1.6 00/03/10-17:02:20 (cstevens)
 *   Removing unused member variables
 *
 * 1.5 99/10/26-17:10:08 (cstevens)
 *   Get rid of public variables Request.server and Request.sock:
 *   A. In all cases, Request.server was not necessary; it was mainly used for
 *   constructing the absolute URL for a redirect, so Request.redirect() was
 *   rewritten to take an absolute or relative URL and do the right thing.
 *   B. Request.sock was changed to Request.getSock(); it is still rarely used
 *   for diagnostics and logging (e.g., ChainSawHandler).
 *
 * 1.4 99/06/29-14:38:11 (suhler)
 *   use new redirector
 *
 * 1.3 99/03/30-09:29:57 (suhler)
 *   documentation update
 *
 * 1.2 98/09/21-14:53:58 (suhler)
 *   changed the package names
 *
 * 1.2 98/09/14-18:03:07 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 handlers/HomeDirHandler.java
 *   Name history : 1 0 HomeDirHandler.java
 *
 * 1.1 98/09/14-18:03:06 (suhler)
 *   date and time created 98/09/14 18:03:06 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.File;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import java.io.IOException;

/**
 * Handler for converting ~username queries.
 * When invoked upstream of the
 * {@link FileHandler}
 * This provides Unix user's with individual home pages.
 *<p>
 *Properties:
 * <dl class=props>
 * <dt>subdir	<dd>Name of the directory in the user's home directory
 *		that represents the user's "doc root"
 * <dt>home	<dd>The mount-point for home directories, 
 *		defaults to "/home/".
 * <dt>root	<dd>The name of the root property to set.
 *		Defaults to "root".
 * <dt>prefix	<dd>The url prefix used to identify home directory queries.
 *		Defaults to "/~".
 * </dl>
 * Url's of the form:
 * <pre>/~[user]/stuff...</pre>
 * are transformed into
 * [home][user]/[subdir]/stuff....
 * <p>
 * Note:  This functionallity has been mostly subsumed by the
 * {@link UrlMapperHandler}.
 *
 * @author		Stephen Uhler
 * @version		2.2, 04/11/03
 */

public class HomeDirHandler implements Handler {
    private String urlPrefix;
    private String propsPrefix;
    private String docDir;
    private String home;
    private String rootProperty;

    /**
     * Handler configuration property <b>subdir</b>.
     * The name of the directory in the user's home directory to use as the
     * document root for this request.
     * The default is <code>public_html</code>.
     */
    static final String DIR = "subdir";      // subdirectory for finding pages
    /**
     * Handler configuration property <b>home</b>.
     * The directory that user accounts live in.
     * The current implementation doesn't consult the password file, but
     * uses a static mapping from the userid to a directory.
     * The default is /home.
     * using the default settings, a url of the form:
     * <code>/~user/foo.html</code> gets translated into the file:
     * <code>/home/user/public_html/foo.html</code>.
     */
    static final String HOME = "home";      // where user's live - need both slashes

    /**
     * Get and set the configuration parameters.
     */

    public boolean
    init(Server server, String prefix) {
	propsPrefix = prefix;
	docDir = server.props.getProperty(propsPrefix + DIR, "public_html");
	home = server.props.getProperty(propsPrefix + HOME, "/home/");
	urlPrefix = server.props.getProperty(propsPrefix + "prefix", "/~");
	rootProperty = server.props.getProperty(propsPrefix + FileHandler.ROOT, 
		FileHandler.ROOT);
	return true;
    }

    /**
     * If this is a ~user request, modify the <code>root</code> and
     * <code>url</code> properties of the request object.
     */

    public boolean
    respond(Request request) throws IOException {
	String user;
	String url = "/";
	if (request.url.startsWith(urlPrefix)) {
	    int len = urlPrefix.length();
	    int index =  request.url.indexOf("/",len);
	    if (index < 0) {
	    	request.redirect(request.url + "/", null);
		return true;
	    } else {
		user = request.url.substring(len,index);
		url = request.url.substring(index);
	    }

	    String root = home + user + "/" + docDir;
	    File file = new File(root);
	    if (file.isDirectory()) {
		request.props.put(rootProperty, root);
		request.url = url;
		request.log(Server.LOG_INFORMATIONAL, propsPrefix + ".. mapping " + user + " to " + request.props.get(FileHandler.ROOT) + " url: " + request.url);
	    }
	}
    return false;
    }
}
