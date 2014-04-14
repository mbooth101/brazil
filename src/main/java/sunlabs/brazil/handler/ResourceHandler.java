/*
 * ResourceHandler.java
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
 * Version:  2.6
 * Created by suhler on 99/07/09
 * Last modified by suhler on 06/11/13 15:04:14
 *
 * Version Histories:
 *
 * 2.6 06/11/13-15:04:14 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.5 06/04/28-16:16:45 (suhler)
 *   compute content length properly
 *
 * 2.4 06/04/25-14:15:49 (suhler)
 *   use consolidated mime type handling in FileHandler
 *
 * 2.3 04/12/30-12:37:55 (suhler)
 *   javadoc fixes
 *
 * 2.2 03/08/01-16:18:18 (suhler)
 *   fixes for javadoc
 *
 * 2.1 02/10/01-16:36:35 (suhler)
 *   version change
 *
 * 1.21 01/07/20-11:31:12 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.20 01/07/17-14:16:36 (suhler)
 *   use MatchUrl
 *
 * 1.19 00/12/11-13:27:31 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.18 00/10/06-10:50:34 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.17 00/10/05-15:51:01 (cstevens)
 *   lint
 *
 * 1.16.1.1 00/08/16-12:11:34 (suhler)
 *   remove unused import
 *
 * 1.16 00/07/10-15:08:44 (cstevens)
 *   ResourceHandler.getResourceStream() didn't work under Windows due to
 *   platform-dependant file names.  Class.getResourceAsStream() requires '/' as
 *   separator while File.getParent() requires '\'.  Added code to turn '/' into
 *   '\' and vice versa as necessary.  ResourceHandler.getResourceStream() now
 *   accepts either '\' or '/' in path.
 *
 * 1.15 00/06/30-10:28:27 (cstevens)
 *
 * 1.14 00/05/31-13:46:34 (suhler)
 *   doc cleanup
 *
 * 1.13 00/05/24-11:25:27 (suhler)
 *   remove leading @ processing
 *
 * 1.12 00/05/22-18:37:07 (suhler)
 *   remove junk, add comments
 *
 * 1.11 00/05/19-11:48:48 (suhler)
 *   doc fixes
 *
 * 1.10 00/04/24-12:58:38 (cstevens)
 *   init stuff, ignore
 *
 * 1.9 00/04/20-11:50:23 (cstevens)
 *   copyright.
 *
 * 1.8 00/04/17-14:24:38 (cstevens)
 *   Get ResourcePath.
 *
 * 1.7 00/04/12-15:54:14 (cstevens)
 *   ResourceHandler exposes public method to load files and get Strings or bytes
 *   from the jar file.
 *
 * 1.6 00/03/29-16:07:40 (cstevens)
 *   ResourceHandler exposes public method to load files from the jar file.
 *
 * 1.5 00/03/10-17:06:51 (cstevens)
 *   Fix wildcard and unneeded imports
 *
 * 1.4 00/03/02-17:46:24 (cstevens)
 *   NullPointerException if file resource was not found in .jar file
 *
 * 1.3 99/10/26-17:11:18 (cstevens)
 *   Get rid of public variables Request.server and Request.sock:
 *   A. In all cases, Request.server was not necessary; it was mainly used for
 *   constructing the absolute URL for a redirect, so Request.redirect() was
 *   rewritten to take an absolute or relative URL and do the right thing.
 *   B. Request.sock was changed to Request.getSock(); it is still rarely used
 *   for diagnostics and logging (e.g., ChainSawHandler).
 *
 * 1.2 99/07/12-09:27:35 (suhler)
 *   lint
 *
 * 1.2 99/07/09-16:05:02 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/ResourceHandler.java
 *
 * 1.1 99/07/09-16:05:01 (suhler)
 *   date and time created 99/07/09 16:05:01 by suhler
 *
 */

package sunlabs.brazil.handler;

import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.util.http.HttpInputStream;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Handler for serving documents out of the jar file.
 * 
 * Look for url's as resources, presumably in the same "jar"
 * file as the class files.
 * This allows an entire web site to be included in the jar file.
 * A typical way to use this handler (with java 1.2+) is as follows:
 *<ul>
 *<li>Add an existing web site to the jar file with:
 *    <pre>jar uf [jar file] [web site]</pre>
 *<li>Create a config file, and add it to the jar file as:
 *    <code>sunlabs/brazil/main/config</code>.  See
 *    {@link sunlabs.brazil.server.Main Main.java} for more info.
 *<li>Create a <i>startup</i> file containing:
 *    <pre>Main-Class: sunlabs.brazil.server.Main</pre>, 
 *    and add it to the manifest with:
 *    <pre>jar ufm [jar file] [startup file]</pre>
 *<li>Start the server with:
 *    <pre>java -jar [jar file] [optional server options....]</pre>
 *</ul>
 *
 *<p>
 * if no suffix is provided, and the "directory" exists, a redirect
 * is issued, to add-on the trailing slash.
 *
 * The following server properties are used:
 * <dl class=props>
 * <dt>root	<dd> The document root path within the jar file
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URL that triggers this handler.
 * (See {@link MatchString}).
 * <dt>default	<dd> The default file name for url references ending in /
 * <dt>mime.xxx <dd> The mime type for suffix xxx.
 *	See {@link sunlabs.brazil.server.FileHandler} for a
 *	description of how to set mime types for url suffixes.
 * </dl>
 *
 * @author Stephen Uhler
 * @version %V% ResourceHandler.java
 */

public class ResourceHandler
    implements Handler
{
    private static final String ROOT = "root"; 
    private static final String DEFAULT = "default"; 
    private static final String MISSING = "default"; 

    String root;		// prepend to all url's
    MatchString isMine;            // check for matching url
    String defaultName;		// default name for directory url's

    public boolean
    init(Server server, String prefix) {
	isMine = new MatchString(prefix, server.props);
	root = server.props.getProperty(prefix + ROOT,
		    server.props.getProperty(ROOT,"/"));
	if (!root.endsWith("/")) {
	    root += "/";
	}
        defaultName = server.props.getProperty(prefix + DEFAULT, "index.html");
	return true;
    }

    public boolean
    respond(Request request)  throws IOException {
	if (!isMine.match(request.url)) {
	    return false;
        }
	String resource = root + request.url.substring(1);
	if (resource.endsWith("/")) {
	    resource += defaultName;
	}
	request.log(Server.LOG_DIAGNOSTIC, "Looking for resource: " + resource);

	// see if this looks like a directory ??

	if (myClass.getResourceAsStream(resource + "/" + defaultName)
		!= null) {
	    request.log(Server.LOG_DIAGNOSTIC, "Redirecting by adding a /");
	    request.redirect(request.url + "/", null);
	    return true;
	}

	String type = FileHandler.getMimeType(resource, request.props,
		isMine.prefix());
	     
        request.log(Server.LOG_DIAGNOSTIC, "Resource type: " + type);
	if (type == null) {
	    request.log(Server.LOG_INFORMATIONAL, request.url +
		    " has an invalid type");
	    return false;
	}
	InputStream in = myClass.getResourceAsStream(resource);
	if (in == null) {
	    request.log(Server.LOG_DIAGNOSTIC, "Resource not found in archive");
	    return false;
	}
	request.sendResponse(in, in.available(), type, 200);
	return true;
    }

    private static final Class myClass = ResourceHandler.class;
    
    static String
    getProperty(Properties props, String prefix, String name, String def)
    {
	return props.getProperty(prefix + name, props.getProperty(name, def));
    }

    static String
    getResourcePath(Properties props, String prefix, String file)
    {
	File f = new File(file);
	if (f.isAbsolute() == false) {
	    String root = getProperty(props, prefix, FileHandler.ROOT, ".");
	    f = new File(root, file);
	}
	return f.getPath();
    }

    /**
     * Look for a file in the filesystem.  If its not there, see if
     * we can find a resource in our jar file.  Relative paths
     * are resolved with respect to the document root.
     *
     * @param props	where to look for server root property
     * @param prefix	"
     * @param file	The pseudo file to find as a resource
     * @return		The input stream (or null)
     */

    public static InputStream
    getResourceStream(Properties props, String prefix, String file)
	throws IOException
    {
	String path = getResourcePath(props, prefix, file);
	File f = new File(path);
	if (f.isFile()) {
	    return new FileInputStream(f);
	}

	/*
	 * Form absolute path and collapse all relative directories;
	 * getResourceAsStream() won't work otherwise.
         *
         * File.getCanonicalPath() would be nice to use, but under Windows
         * it doesn't understand '/' and it prepends a "C:\" to the result.
         * Class.getResourceAsStream requires '/'.
	 */

        path = FileHandler.urlToPath(path.replace('\\', '/'));
	InputStream in = myClass.getResourceAsStream(path.replace('\\', '/'));
	if (in == null) {
	    throw new FileNotFoundException(file);
	}
	return in;
    }

    public static String
    getResourceString(Properties props, String prefix, String file)
	throws IOException
    {
	InputStream in = getResourceStream(props, prefix, file);
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	new HttpInputStream(in).copyTo(out);
        in.close();
	return out.toString();
    }

    /**
     * Find a file blob as a resource in our jar file (experimental).
     * @param props	where to look for server root property
     * @param prefix	"
     * @param file	The pseudo file to find as a resource
     * @return		The data, if available, or raises an exception.
     */

    public static byte[]
    getResourceBytes(Properties props, String prefix, String file)
	throws IOException
    {
	InputStream in = getResourceStream(props, prefix, file);
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	new HttpInputStream(in).copyTo(out);
        in.close();
	return out.toByteArray();
    }
}
