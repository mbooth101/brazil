/*
 * FileHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1998-2007 Sun Microsystems, Inc.
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
 * Version:  2.8
 * Created by suhler on 98/09/14
 * Last modified by suhler on 07/01/08 15:15:47
 *
 * Version Histories:
 *
 * 2.8 07/01/08-15:15:47 (suhler)
 *   add a lastmodified method to deal with the "lastModified" property
 *
 * 2.7 06/04/25-14:22:19 (suhler)
 *   use consolidated mime type handling in FileHandler
 *
 * 2.6 05/12/09-14:50:44 (suhler)
 *   Document the partial-content feature (using the "range") header, fix
 *   an off-by-one error when the range "end" is trucated, and add the proper
 *   "content-range" header
 *
 * 2.5 05/02/04-11:38:28 (suhler)
 *   redo glob-style mime type matching to look at explicit list of
 *   patterns, so we don't search all the properties (twice)
 *
 * 2.4 05/02/04-11:01:53 (suhler)
 *   added globbing to mime typ matching (using search through properties)
 *
 * 2.3 04/11/30-15:19:41 (suhler)
 *   fixed sccs version string
 *
 * 2.2 03/08/01-16:17:53 (suhler)
 *   fixes for javadoc
 *
 * 2.1 02/10/01-16:34:50 (suhler)
 *   version change
 *
 * 1.34 02/07/24-10:47:55 (suhler)
 *   doc updates
 *
 * 1.33 02/07/11-17:44:33 (suhler)
 *   doc fixes
 *
 * 1.32 01/07/23-14:48:25 (suhler)
 *   changed "allow" to "getOnly" and reversed the default: all methods are
 *   accepted unless "getOnly" is specified
 *   .
 *
 * 1.31 01/03/12-17:27:16 (cstevens)
 *   spelling error in log message
 *
 * 1.30 01/01/07-22:35:43 (suhler)
 *   added byte range support
 *
 * 1.29 00/12/11-13:32:13 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.28 00/11/19-10:32:45 (suhler)
 *   Add "lastModified" into request properties
 *
 * 1.27 00/10/17-09:30:45 (suhler)
 *   added a "prefix" property.
 *   .
 *
 * 1.26 00/10/05-09:18:11 (suhler)
 *   return "403 Forbidden" for existing but not readable files
 *   .
 *
 * 1.25 00/05/15-10:09:12 (suhler)
 *   removed "deny" option  - use the UrlMapperHandler instead
 *   removed the "result" option - use the FilterHandler instead
 *   added more diagnostics
 *
 * 1.24 00/04/24-14:06:12 (cstevens)
 *   doc
 *
 * 1.23 00/01/30-17:45:14 (suhler)
 *   Removed platform dependency in redirect check
 *
 * 1.22 99/12/09-10:43:11 (suhler)
 *   add "allow" configuration parameter to allow handling other than
 *   GET requests
 *
 * 1.21 99/11/17-15:36:49 (cstevens)
 *   broke DirectoryHandler.
 *
 * 1.20 99/11/16-19:09:01 (cstevens)
 *   wildcard imports
 *
 * 1.19 99/11/16-14:18:14 (cstevens)
 *   FileHandler.java:
 *   1. documentation.
 *   2. "blockSize" property was retrieved from Properties but never used, since
 *   the copy operation uses the Server.bufsize.  "blockSize" removed.
 *   3. "deny" property to deny access to files that matched a glob pattern was
 *   broken.  Arguments to Glob were reversed, so the code was treating the
 *   URL as the pattern and the deny pattern as the filename.  No matches.
 *   4. URL was never "URL decoded", so something like "/in%64ex%2ehtml"
 *   was not converted to "/index.html".  Code would therefore look for the
 *   existence of a file called "in%64ex%2ehtml".
 *   5. java.net.URL was used to convert a URL to the local file name.  However,
 *   java.net.URL does not collapse a trailing "/.." (or "/.") in the URL,
 *   leading to the possibility of escaping from the document root.
 *   6. In jdk-1.2, java.io.File collapses out multiple consecutive "/" characters
 *   in a file name, while in jdk-1.1, this doesn't happen.  It is necessary
 *   to take them out to make it easier to compare if two files are the
 *   same.
 *   7. Request property "fileName" is set to the name of the file being
 *   fetched.  But if the user requested a directory name, and the
 *   FileHandler automatically filled in the name of the index file (e.g.,
 *   index.html), "fileName" should have been set to the that file, not
 *   just the name of the directory.
 *   8. After appending the name of the index file onto the name of the
 *   directory requested in an URL, the FileHandler didn't ensure that that
 *   file existed (and was a regular file).  This would cause a
 *   FileNotFoundException, and the server would either send a "500 Server
 *   Error" or just close the connection back to the client.  Instead, it
 *   should return a "404 Not Found" message.
 *
 * 1.18 99/10/28-17:23:27 (cstevens)
 *   ChangedTemplate
 *
 * 1.17 99/10/26-18:13:19 (cstevens)
 *   Get rid of public variables Request.server and Request.sock:
 *   A. In all cases, Request.server was not necessary; it was mainly used for
 *   constructing the absolute URL for a redirect, so Request.redirect() was
 *   rewritten to take an absolute or relative URL and do the right thing.
 *   B. Request.sock was changed to Request.getSock(); it is still rarely used
 *   for diagnostics and logging (e.g., ChainSawHandler).
 *
 * 1.16 99/10/25-14:29:49 (suhler)
 *   file handler wasn't sending content type
 *
 * 1.15 99/10/14-14:57:34 (cstevens)
 *   resolve wilcard imports.
 *
 * 1.14 99/10/14-12:52:04 (cstevens)
 *   FileHandler.sendFile uses new Request.sendResponse(InputStream) method.
 *
 * 1.13 99/09/22-16:02:29 (suhler)
 *   The file handler only responds to GET requests
 *
 * 1.12 99/09/17-14:02:55 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.11 99/09/15-14:41:02 (cstevens)
 *   Rewritign http server to make it easier to proxy requests.
 *
 * 1.10.1.1 99/08/18-08:40:30 (suhler)
 *   doc change
 *
 * 1.10 99/08/06-12:05:33 (suhler)
 *   moved glob to util directoy
 *
 * 1.9 99/06/29-14:33:31 (suhler)
 *   Fixed redirects on directory names with no trailing '/'
 *   Uses "host:" header if available
 *   .
 *
 * 1.8 99/06/28-10:53:16 (suhler)
 *   allow results to be placed in properties (temporary)
 *
 * 1.7 99/06/04-13:54:41 (suhler)
 *   added "request" option to place result into request properties
 *   and requrn false.
 *   This allows other handlers to process files more
 *
 * 1.6 99/04/27-14:45:09 (suhler)
 *   - added glob style pattern matcher as a utility method
 *   - added "deny" parameter to fall through if url matches a pattern
 *
 * 1.5 99/03/30-09:24:31 (suhler)
 *   - documentation updates
 *   - made SendFile public
 *   - set "Directory" and "FileName" properties upon failure
 *   - don't force result code to be 200
 *
 * 1.4 99/02/04-20:38:21 (cstevens)
 *   lint: changed wildcard import statements to explicit imports.
 *
 * 1.3 98/10/13-08:05:34 (suhler)
 *   added informational messages
 *
 * 1.2 98/09/21-14:50:54 (suhler)
 *   changed the package names
 *
 * 1.2 98/09/14-18:03:05 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 server/FileHandler.java
 *   Name history : 1 0 FileHandler.java
 *
 * 1.1 98/09/14-18:03:04 (suhler)
 *   date and time created 98/09/14 18:03:04 by suhler
 *
 */

package sunlabs.brazil.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

import sunlabs.brazil.util.Glob;
import sunlabs.brazil.util.http.HttpUtil;

/**
 * Standard handler for fetching static files.
 * This handler does URL to file conversion, file suffix to mime type
 * lookup, delivery of <i>index</i> files where providing directory
 * references, and redirection for missing slashes (/) at the end of
 * directory requests.
 * <p>
 * The following configuration parameters are used:
 * <dl class=props>
 * <dt>root	<dd> property for document root (.)
 *		Since the document root is common to many handlers, if no
 *		root property is found with the supplied prefix, then the
 *		root property with the empty prefix ("") is used instead.
 *		This allows many handlers to share the common property.
 * <dt>default	<dd> The document to deliver 
 *		if the URL ends in "/". (defaults to index.html.)
 * <dt>prefix	<dd> Only url's that start with this are allowed.
 *		defaults to "".  The prefix is removed from the
 *		url before looking it up in the file system.
 *		So, if prefix is <code>/foo</code> then the the file
 *		<code>[root]/foo/bar.html</code> will be delivered
 *		in response to the url <code>/bar.html</code>.
 * <dt>mime	<dd> property for mime type
 *		For each file suffix .XX, the property mime.XX is used to 
 *		determine the mime type.  If no property exists (or its
 *		value is "unknown", the document
 *		will not be delivered.
 * <dt>mimePatterns
 *		<dd> List of glob patterns that match file name suffixes
 *		for matching mime types.  For example:
 *		<pre>
 *		mimePatterns=.x* .a?
 *		mime.x*=text/xml
 *		mime.a?=application/octet-stream
 *		</pre>
 *		The types corrosponding to mime patterns are searched for
 *		in mimePattern order, first looking for
 *		<code>prefix.mime.pattern</code> then
 *		<code>mime.pattern</code>.  If neither property exists, 
 *		then the type is invalid.
 * <dt>getOnly  <dd>If defined, only "GET" requests will be processed.  By
 *		default, all request types are handled. (Note: this is the
 *		inverse of the previous policy, defined by the undocumented
 *		"allow" parameter).
 * </dl>
 * <p>
 * The FileHandler sets the following entries in the request properties
 * as a side-effect:
 * <dl>
 * <dt>fileName		<dd>The absolute path of the file
 *			that couldn't be found.
 * <dt>DirectoryName	<dd>If the URL specified is a directory name, 
 *			its absolute path is placed here.
 * <dt>lastModified	<dd>The Time stamp of the last modified time
 * </dl>
 * <p>
 * This handler supports a subset of the http <code>range</code>
 *  header of the form
 * <code>range bytes=[start]-[end]</code>, where <i>start</i> and <i>end</i>
 * are byte positions in the file, starting at zero.  A large or
 * missing <i>end</i> value is treated as the end of the file.  If a valid
 * <code>range</code>header is found, the appropriate 
 * <code>content-range</code> header is returned, along with the partial
 * contents of the file.
 *
 * @author      Stephen Uhler
 * @version		2.8
 */

public class FileHandler implements Handler {
    private static final String PREFIX = "prefix";  // our prefix
    private static final String DEFAULT = "default";    // property for default document, given directory
    private static final String GETONLY = "getOnly";  // allow only GETs

    public static final String MIME = "mime";	// property for mime type
    public static final String UNKNOWN = "unknown";// make this suffix unknown
    public static final String ROOT = "root";   // property for document root

    public String urlPrefix = "/";

    String prefix;


    /**
     * Initialize the file handler.
     *
     * @return	The file handler always returns true.
     */

    public boolean
    init(Server server, String prefix)
    {
	this.prefix = prefix;
	urlPrefix = server.props.getProperty(prefix + PREFIX, urlPrefix);
	return true;
    }

    /** 
     * Find, read, and deliver via http the requested file.
     * The server property <code>root</code> is used as the document root.
     * The document root is recalculated for each request, so an upstream
     * handler may change it for that request.
     * For URL's ending with "/", the server property <code>default</code>
     * (normally index.html) is automatically appended.
     *
     * If the file suffix is not found as a server property <code>
     * mime.<i>suffix</i></code>, the file is not delivered.
     */

    public boolean
    respond(Request request)
	throws IOException
    {
	if (!request.url.startsWith(urlPrefix)) {
	    return false;
	}
	if ((request.props.getProperty(prefix + GETONLY)!=null) &&
		(!request.method.equals("GET"))) {
	    request.log(Server.LOG_INFORMATIONAL, prefix, 
		"Skipping request, only GET's allowed");
	    return false;
	}	

	String url = request.url.substring(urlPrefix.length());
	Properties props = request.props;
	String root = props.getProperty(prefix + ROOT,
		props.getProperty(ROOT, "."));
	String name = urlToPath(url);
	request.log(Server.LOG_DIAGNOSTIC, prefix, "Looking for file: (" +
		root + ")(" + name + ")");
	File file = new File(root + name);
	String path = file.getPath();

	if (file.isDirectory()) {
	    /*
	     * Must check if the original <code>name</code> ends with "/",
	     * not <code>File.getPath</code> because in jdk-1.2,
	     * <code>File.getPath</code> truncates the terminating "/".
	     */

	    if (request.url.endsWith("/") == false) {
		request.redirect(request.url + "/", null);
		return true;
	    }
	    props.put("DirectoryName", path);

	    String index = props.getProperty(prefix + DEFAULT, "index.html");

	    file = new File(file, index);
	    path = file.getPath();
	}

	/*
	 * Put the name of the file in the request object.  This
	 * may be of some use for down stream handlers.  
	 */

	props.put("fileName", path);

	if (file.exists() == false) {
	    request.log(Server.LOG_INFORMATIONAL, prefix, 
		     "no such file: " + path);
	    return false;
	}

	String basename = file.getName();
	String type = getMimeType(basename, props, prefix);
	if (type == null) {
	    request.log(Server.LOG_INFORMATIONAL, prefix,
		    "unknown file suffix for " + basename);
	    return false;
	}
	sendFile(request, file, 200, type);
	return true;
    }

    /**
     * Get the mime type based on the suffix of a String.  The
     * suffix (e.g. text after the last ".") is used to lookup
     * the entry "prefix.mime.[suffix]" then "mime.[suffix]" in 
     * <code>props</code>.  If neither entry is found, then mime
     * glob pattern are used, if available.  If there is no suffix, 
     * then the empty string is used.
     * <p>
     * If the mime type is set to the special string "unknown", then
     * the type is unknown.  This allows specific types to be undefined
     * when glob patterns are used.
     * <p>
     * If the property "prefix.mimePatterns" (or "mimePatterns") exists, 
     * then it specifies a white-space delimited set of glob style patterns
     * for matching file suffixes to types.  If a match for a specific file
     * suffix fails, then the property "mime.[pattern]" is used for type
     * comparisons.
     * <p>
     * The entries:
     * <pre>
     * mimePatterns=*ml
     * mime.*ml=text/xml
     * </pre>
     * would associate the type "text/xml" with the file foo.html, foo.xml
     * and foo.dhtml.
     * The entries:
     * <pre>
     * mimePatterns=*
     * mime*=application/octet-stream
     * mime.config=unknown
     * </pre>
     * Would set the types for all file types not otherwise defined to be
     * "application/octet-stream", except that files ending in ".config"
     * would have no type (e.g. they would generate a file not found error).
     *
     * @param name	The string to compute the mime type for
     * @param props	The properties to look up the mime types in.
     * @props prefix	The properties prefix for the name lookup
     * @return		The type (or null if not found).
     */

    public static String
    getMimeType(String name, Properties props, String prefix) {
	String type = null;
	int index = name.lastIndexOf('.');
	if (index > 0) {
	    String suffix = name.substring(index);
	    type = props.getProperty(prefix + MIME + suffix,
		    props.getProperty(MIME + suffix));

	    // check for glob patterns
	    if (type == null) {
		type = getGlobType(suffix, props, prefix);
	    } else if (type.equals(UNKNOWN)) {
		return null;
	    }
	} else if (index < 0) {
	    type = getGlobType("", props, prefix);
	}
	return type;
    }

    /** 
     * Helper to find mime types for suffixes with glob pattern.
     * Used only if a static mime type is not found.
     * @param suffix		the file suffix to look for
     * @param dict		where to look
     * @param prefix		prefix in properties table 
     */

    static String
    getGlobType(String suffix, Properties props, String pre) {
	String list = props.getProperty(pre + "mimePatterns", 
			props.getProperty("mimePatterns"));
	if (list == null) {
	    return null;
	}
	StringTokenizer st = new StringTokenizer(list);
	while(st.hasMoreTokens()) {
	    String key = st.nextToken();
	    if (Glob.match(key, suffix)) {
		String type = props.getProperty(pre + "mime" + key,
			props.getProperty("mime" + key));
		if (UNKNOWN.equals(type)) {
		    break;
		} else {
		    return type;
		}
	    }
	}
	return null;
    }

    /**
     * Helper function to convert an url into a pathname. <ul>
     * <li> Collapse all %XX sequences.
     * <li> Ignore missing initial "/".
     * <li> Collapse all "/..", "/.", and "//" sequences.
     * </ul>
     * <code>URL(String)</code> collapses all "/.." (and "/.") sequences,
     * except for a trailing "/.." (or "/."), which would lead to the
     * possibility of escaping from the document root.
     * <p>
     * <code>File.getPath</code> in jdk-1.1 leaves all the "//" constructs
     * in, but it collapses them in jdk-1.2, so we have to always take it
     * out ourselves, just to be sure.
     *
     * @param	url
     *		The file path from the URL (that is, minus the "http://host"
     *		part).  May be <code>null</code>.
     *
     * @return	The path that corresponds to the URL.  The returned value
     *		begins with "/".  The caller can concatenate this path
     *		onto the end of some document root.
     */
    public static String
    urlToPath(String url)
    {
	String name = HttpUtil.urlDecode(url);

	StringBuffer sb = new StringBuffer();
	StringTokenizer st = new StringTokenizer(name, "/");
	while (st.hasMoreTokens()) {
	    String part = st.nextToken();
	    if (part.equals(".")) {
		continue;
	    } else if (part.equals("..")) {
		for (int i = sb.length(); --i >= 0; ) {
		    if (sb.charAt(i) == '/') {
			sb.setLength(i);
			break;
		    }
		}
	    } else {
		sb.append(File.separatorChar).append(part);
	    }
	}
	if ((sb.length() == 0) || name.endsWith("/")) {
	    sb.append(File.separatorChar);
	}
	return sb.toString();
    }

    /**
     * Send a file as a response.
     * @param request       The request object
     * @param fileHandle    The file to output
     * @param type          The mime type of the file
     */

    static public void
    sendFile(Request request, File file, int code, String type)
	throws IOException
    {
	if (file.isFile() == false) {
	    request.sendError(404, null, "not a normal file");
	    return;
	}
	if (file.canRead() == false) {
	    request.sendError(403, null, "Permission Denied");
	    return;
	}

	FileInputStream in = null;
	try {
	    in = new FileInputStream(file);

	    request.addHeader("Last-Modified", 
		    HttpUtil.formatTime(file.lastModified()));
	    setModified(request.props, file.lastModified());

	    int size = (int) file.length();
	    request.setStatus(code);
	    size = range(request, in, size);
	    request.sendResponse(in, size, type, -1);
	} finally {
	    if (in != null) {
		in.close();
	    }
	}
    }

    /**
     * Set the "lastModified" request property.  If already set,
     * use the most recent value.
     * @param props	Where to find the "lastModified" property
     * @param mod	The modidied time, in ms since the epoch
     */

     public static void setModified(Properties props, long mod) {
	if (mod <= 0) {
	    return;
	}
	String current = props.getProperty("lastModified");
	if (current == null) {
	   props.put("lastModified", "" + mod);
	} else {
	    try {
	        long l = Long.parseLong(current);
		if (l > mod) {
		   props.put("lastModified", "" + l);
	        }
	    } catch (NumberFormatException e) {}
	}
     }

    /**
     * Compute simple byte ranges. (for gnutella support).  Only simple
     * ranges (e.g. xxx-yyy) are supported.  This skips over the proper
     * number of bytes in the input stream, changes the request status to
     * 26, and adds a content-range header.
     * @return		The (potential partial) size.
     *			
     */

    private static int
    range(Request request, FileInputStream in, int size) throws IOException {
	String range=request.getRequestHeader("range");
	int sep = 0;
	if (range != null && request.getStatus()==200 &&
		range.indexOf("bytes=") == 0 &&
		(sep = range.indexOf("-")) > 0 &&
		range.indexOf(",")<0) {
	    int start = -1;
	    int end = -1;
	    try {
		start = Integer.parseInt(range.substring(6,sep));
	    } catch (NumberFormatException e) {}
	    try {
		end = Integer.parseInt(range.substring(sep+1));
	    } catch (NumberFormatException e) {}
	    if (end == -1) {
		end = size - 1;
	    } else if (end >= size) {
		end = size - 1;
	    }
	    if (start == -1) {
		start = size - end + 1;
		end = size;
	    }
	    if (end >= start) {
		in.skip(start);
	        request.addHeader("Content-Range", 
			"bytes " + start + "-" + end + "/" + size);
		size = end - start + 1;
		request.setStatus(206);
	    }
	}
	return size;
    }
}
