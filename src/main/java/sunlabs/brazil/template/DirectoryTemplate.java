/*
 * DirectoryTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2006 Sun Microsystems, Inc.
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
 * Version:  2.8
 * Created by suhler on 00/07/05
 * Last modified by suhler on 06/04/25 14:16:14
 *
 * Version Histories:
 *
 * 2.8 06/04/25-14:16:14 (suhler)
 *   use consolidated mime type handling in FileHandler
 *
 * 2.7 05/12/21-10:29:56 (suhler)
 *   look for "delimiter" attribute in <filelist> tag
 *
 * 2.6 04/10/28-12:48:20 (suhler)
 *   doc fixes
 *
 * 2.5 04/08/13-09:17:37 (suhler)
 *   doc fixes
 *
 * 2.4 04/04/19-16:50:59 (suhler)
 *   - removed one-time-only check: doesn'
 *   make sense now that we have a "directory" attribute
 *   - added a "select=<glob>" attribute to restrict file names
 *
 * 2.3 04/04/19-14:36:05 (suhler)
 *   Resolve directory attibute properly
 *
 * 2.2 03/07/28-09:16:48 (suhler)
 *   add attribute for specifying required directory  (This class needs a redo)
 *
 * 2.1 02/10/01-16:36:49 (suhler)
 *   version change
 *
 * 1.11 02/07/24-10:46:18 (suhler)
 *   doc updates
 *
 * 1.10 02/04/25-13:43:28 (suhler)
 *   doc fixes
 *
 * 1.9 01/05/11-14:51:35 (suhler)
 *   use template.debug()
 *
 * 1.8 01/04/03-12:15:12 (suhler)
 *   change timebase back to seconds
 *
 * 1.7 01/02/27-10:53:30 (suhler)
 *   added option to extract last modified times and file sizes for each file
 *
 * 1.6 00/12/11-20:24:31 (suhler)
 *   doc typo
 *
 * 1.5 00/12/11-13:30:08 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.4 00/07/07-15:32:06 (suhler)
 *   added a server log entry
 *
 * 1.3 00/07/06-18:00:21 (suhler)
 *   bug fx - only worked 1st time
 *   - also can be used as a handler
 *
 * 1.2 00/07/06-15:49:12 (suhler)
 *   doc update
 *
 * 1.2 00/07/05-16:22:07 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/DirectoryTemplate.java
 *
 * 1.1 00/07/05-16:22:06 (suhler)
 *   date and time created 00/07/05 16:22:06 by suhler
 *
 */

package sunlabs.brazil.template;

import java.io.File;
import java.util.Properties;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Glob;

/**
 * Put current directory information (based on the URL) into the
 * request properties.
 * The <code>&lt;filelist&gt;</code> tag, if present in the document, 
 * triggers the generation of a directory and file listing, based on the
 * current URL.
 * <p>
 * Template Properties:
 * <dl class=props>
 * <dt>prepend	<dd>String to prepend to the properties "Directories" and
 *		"Files" That contain the directory and file lists respectively.
 *		Defaults to the Templates properties prefix.
 * <dt>delimiter<dd>Delimiter character to separate entries, defaults to " ".
 * <dt>directory<dd>The directory to use instead of the one implied by the URL.
 *		If it starts with "/", then it is resolved relative to the
 *		document root, otherwise it is resolved relative to the
 *		directory implied by the URL.  The document root is
 *		found in the porperty <code>"[prefix].root"</code>, or in
 *		<code>"root"</code>, or (if neither exists), the current
 *		directory of the server.
 * <dt>DirectoryName<dd>If set (usually by an upstream handler,
 *		such as the FileHandler, or TemplateHandler),
 *		this is used as the directory name instead of
 *		deriving it from the URL.
 * <dt>debug	<dd>if set, a comment is emitted indicating where the
 *		file-list entitiy was encountered.
 * <dt>[prepend].Directories<dd>List of sub-directories in current directory
 *		is set by this template.
 * <dt>[prepend].Files	<dd>List of files with valid suffixes in
 *		current directory. is set by this template.
 * <dt>mime.xxx	<dd>An indication that suffix "xxx" is valid.
 *		Only valid file names are returned.
 *		See {@link sunlabs.brazil.server.FileHandler} for a
 *		description of how to set mime types for url suffixes.
 * <dt>select	<dd>Specifies a "glob" pattern to restrict the names of
 *		files and directories returned. The form "![pattern]" selects
 *		the inverse of the glob pattern.
 * <dt>stats	<dd>If specified, then for each file, the properties:
 *		<b>[prepend].[file].mod</b> and <b>[prepend].[file].size</b>
 *		are set, containing the file last modified time
 *		(seconds since epoch) and size (bytes) respectively.
 * </dl>
 * This class may also be used as a handler, in which case the
 * property <code>prefix</code> is used to match the leading
 * portion of a URL.
 * <p>
 * The tag takes the optional parameters: <code>prepend</code>
 * and <code>stats</code> that override the corrosponding request
 * properties (above).  In addition, the attribute <code>dir</code>
 * may be used to select an alternate directory for files.
 *
 * @author	Stephen Uhler
 * @version	%V% DirectoryTemplate.java
 */

public class DirectoryTemplate extends Template implements Handler {
    private String propsPrefix;

    public boolean
    init(Server server, String prefix) {
	propsPrefix = prefix;
	return true;
    }

    /**
     * Compute the directory info, and add it to the request properties.
     */

    public boolean
    respond(Request request) {
	String prefix = request.props.getProperty(propsPrefix + "prefix", "/");
	String glob = request.props.getProperty(propsPrefix + "select");

	if (request.url.startsWith(prefix)) {
	    getFiles(null, glob, request, propsPrefix);
	}
	return false;
    }

    /**
     * Reset at each page
     */

    public boolean
    init(RewriteContext hr) {
	return super.init(hr);
    }

    /**
     * Turn on the directory calculator.  The presense of this tag
     * causes the files and subdirectories in the current directory
     * to be added to the request properties.
     * <p>
     * The attribute "stats" may be specified to enable
     * additional statistics, overriding the request properties.
     */

    public void
    tag_filelist(RewriteContext hr) {
	hr.killToken();
	debug(hr);
	String stats = hr.get("stats",null);
	if (stats != null) {
	    hr.request.props.put(hr.prefix + "stats", stats);
	}
	String delim = hr.get("delimiter",null);
	if (delim != null) {
	    hr.request.props.put(hr.prefix + "delimiter", delim);
	}
	String prepend = hr.get("prepend",null);
	if (prepend != null) {
	    hr.request.props.put(hr.prefix + "prepend", prepend);
	}
	getFiles(hr.get("directory"), hr.get("select"), hr.request, hr.prefix);
    }

    /**
     * Generate properties containing the files and directories in
     * the "current" directory.  The current directory is taken from
     * the "DirectoryName" request property, or derived from the URL.
     * This functionality was culled from the FileHandler and
     * the Directory Handler.
     * <p>
     * If "directory" is specified (e.g. not null), then it is used as the
     * directory instead. If "directory" starts with "/" then the directory is
     * resolved relative to the document root, otherwise it is
     * resolved relative to the current directory.
     * If "select" is specified, then only files or directories matching
     * the supplied <b>glob</b> pattern are selected.  If the first
     * character of "select" is "!", then the sense of the glob pattern
     * is inverted.
     */

    public static void
    getFiles(String dir, String glob, Request request, String prefix) {
	Properties props = request.props;
	String prepend = props.getProperty(prefix + "prepend", prefix);
	String delim = props.getProperty(prefix + "delimiter", " ");
	String stats = props.getProperty(prefix + "stats");
	File file = null;
	boolean invert = false;

	// experimental inverse globbing

	if (glob != null && glob.startsWith("!")) {
	    glob=glob.substring(1);
	    invert=true;
	}

	if (!prepend.equals("") && !prepend.endsWith(".")) {
	    prepend += ".";
	}

	if (dir == null) {
	    dir ="";
	}

	if (dir.startsWith("/")) { 
	    String root = props.getProperty(prefix + FileHandler.ROOT,
		    props.getProperty(FileHandler.ROOT, "."));
	    file = new File(root, dir.substring(1));
	} else {
	    String current = props.getProperty("DirectoryName");
	    if (current != null) {
	        file = new File(current, dir);
	    } else {
		String name = FileHandler.urlToPath(request.url);
		String root = props.getProperty(prefix + FileHandler.ROOT,
			props.getProperty(FileHandler.ROOT, "."));
		file = new File(root + name);
		if (!file.isDirectory()) {
		    file = new File(file.getParent());
		}
		props.put("DirectoryName", file.getPath());
		file = new File(file, dir);
	    }
	}

	request.log(Server.LOG_DIAGNOSTIC, prefix,
		"using directory: " + file);

	String[] list = file.list();
	StringBuffer dirs = new StringBuffer();
	StringBuffer files = new StringBuffer();
	if (list != null) {
	    String dsep = "";
	    String fsep = "";
	    for(int i = 0; i < list.length; i++) {
		String name = list[i];
		if (glob != null && !(invert ^ Glob.match(glob,name))) {
		    continue;
		}
		if ((new File(file, name)).isDirectory()) {
		    dirs.append(dsep).append(name);
		    dsep = delim;
		} else if (FileHandler.getMimeType(name, props, prefix)!=null) {
		    files.append(fsep).append(name);
		    fsep = delim;
		    if (stats != null) {
			File stat = new File(file,name);
			props.put(prepend + name + ".size",
				"" + stat.length());
			props.put(prepend + name + ".mod",
				"" + stat.lastModified()/1000);
		    }
		}
	    }
	    props.put(prepend + "Directories", dirs.toString());
	    props.put(prepend + "Files", files.toString());
	    // System.out.println("Dirs=" + dirs +  "\nFiles=" + files);
	    request.log(Server.LOG_DIAGNOSTIC, prefix,
		"Generating file and directory properties: " + list.length);
	}
    }
}
