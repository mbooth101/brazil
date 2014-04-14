/*
 * TemplateHandler.java
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
 * Contributor(s): cstevens, drach, suhler.
 *
 * Version:  2.8
 * Created by suhler on 98/09/14
 * Last modified by suhler on 07/01/08 15:15:18
 *
 * Version Histories:
 *
 * 2.8 07/01/08-15:15:18 (suhler)
 *   set the last-modified property
 *
 * 2.7 06/11/13-15:00:12 (suhler)
 *   move MatchString from handler -> util
 *
 * 2.6 06/04/25-14:16:39 (suhler)
 *   use consolidated mime type handling in FileHandler
 *
 * 2.5 04/12/30-12:38:25 (suhler)
 *   javadoc fixes
 *
 * 2.4 04/10/24-17:21:47 (suhler)
 *   bug fix.  change "FileName" to "fileName"
 *
 * 2.3 03/08/01-16:19:06 (suhler)
 *   fixes for javadoc
 *
 * 2.2 03/07/14-09:57:42 (suhler)
 *   added tagPrefix option docs
 *
 * 2.1 02/10/01-16:36:47 (suhler)
 *   version change
 *
 * 1.42 02/08/21-15:33:51 (suhler)
 *   add "outputEncoding" support
 *
 * 1.41 02/06/27-18:38:11 (suhler)
 *   add encoding option
 *
 * 1.40 02/06/24-15:40:14 (suhler)
 *   Don't install the template handler is one of the templates fails
 *
 * 1.39 02/05/24-16:58:22 (suhler)
 *   add "modified" option to output last-modified times
 *
 * 1.38 02/04/24-13:36:11 (suhler)
 *   check for text mime type before processing
 *
 * 1.37 02/04/24-12:53:00 (suhler)
 *   check for (and reject) non-text mime types
 *
 * 1.36 01/12/07-14:26:28 (suhler)
 *   'twas quite broken:
 *   - use MatchString instead of old prefix/suffix matching
 *   - fixed redirects and suffix checking, so that this handler may be
 *   - used by itself.
 *
 * 1.35 01/08/14-16:39:09 (suhler)
 *   any status code changes were being inadvertantly reset
 *
 * 1.34 01/05/02-15:32:06 (drach)
 *   Add template tokens.
 *
 * 1.33 01/03/06-09:07:53 (suhler)
 *   make sure specified suffix maps to a valid mime type
 *
 * 1.32 00/12/11-13:30:04 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.31 00/11/28-10:09:34 (suhler)
 *   changed log level
 *
 * 1.30 00/10/25-20:16:45 (suhler)
 *   Session handling was broken: was not finding session id properly
 *   .
 *
 * 1.29 00/10/20-15:42:31 (suhler)
 *   api change due to race condition in TemplateRunner
 *
 * 1.28 00/07/05-14:11:11 (cstevens)
 *   Server object is in RewriteContext used by templates, so templates (such as
 *   the TclServerTemplate) can be initialized with the server and prefix, similar
 *   to how Handlers are initialized.
 *
 * 1.27 00/05/31-13:50:06 (suhler)
 *   use standard "null" session
 *
 * 1.26 00/05/22-14:06:02 (suhler)
 *   add redirects and default handling ala the file handler
 *
 * 1.25 00/05/15-10:13:51 (suhler)
 *   added diags
 *
 * 1.24 00/05/12-10:58:01 (suhler)
 *   - removed all cookie header processing:
 *   use a session handler instead
 *   - Only filte system templates are supported.  Removed uncocumented
 *   "handler" option
 *   - fixed documentation
 *
 * 1.23 00/04/17-14:25:23 (cstevens)
 *   TemplateHandler.java, TemplateFilter.java:
 *   1. configuration property "templateClass" -> "templates"
 *
 * 1.22 00/03/29-16:15:53 (cstevens)
 *   clean TemplateHandler.
 *
 * 1.21 00/02/01-19:01:27 (suhler)
 *   missing close()
 *
 * 1.20 00/01/03-13:37:26 (suhler)
 *   fixes
 *
 * 1.19 00/01/03-13:29:06 (suhler)
 *   redo removing use of file handler
 *
 * 1.18 99/12/09-10:42:36 (suhler)
 *   re-added old fileHandler hack
 *
 * 1.17 99/11/16-19:07:59 (cstevens)
 *   wildcard imports
 *
 * 1.16 99/10/28-17:23:02 (cstevens)
 *   ChangedTemplate
 *
 * 1.15 99/10/25-15:36:41 (cstevens)
 *   propsPrefix to pref
 *
 * 1.14 99/10/21-18:14:10 (cstevens)
 *   TemplateHandler now takes a list of Templates, rather than just one.  When
 *   parsing an HTML file, it will now dispatch to the union of all the tag
 *   methods defined in the list of Templates.  In that way, the user can
 *   compose things like the BSLTemplate to iterate over request properties with
 *   the PropsTemplate to substitute in their values.  Otherwise, it would have
 *   required N separate passes (via N separate TemplateHandlers) over the HTML
 *   file, one for each Template and/or level of recursion in the BSLTemplate.
 *
 * 1.13 99/10/01-11:26:46 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.12 99/09/29-16:05:54 (cstevens)
 *   New HtmlRewriter object, that allows arbitrary rewriting of the HTML (by
 *   templates and others), instead of forcing the templates to return a string
 *   that contained all of the new HTML content in one big string.
 *
 * 1.11 99/06/28-10:50:07 (suhler)
 *   ???
 *
 * 1.10 99/05/24-18:24:46 (suhler)
 *   fixed wildcard imports
 *
 * 1.9 99/05/24-17:38:07 (suhler)
 *   rewritten to use new template class
 *   (the timing is unfortunate)
 *
 * 1.8 99/05/13-09:22:28 (suhler)
 *   modified to use new no-dilimeter option of HtmlMunge
 *
 * 1.7 99/03/30-09:30:59 (suhler)
 *   documentation update
 *
 * 1.6 99/02/19-15:44:47 (suhler)
 *   If the cookie name "none" is supplied as a property, then no cookies are
 *   used, and every client gets the same object.
 *
 * 1.5 98/11/30-15:47:07 (suhler)
 *   Rearranged code to permit subclassing for server-side xml processing
 *
 * 1.4 98/10/13-08:09:41 (suhler)
 *   don't import self
 *
 * 1.3 98/09/21-14:55:04 (suhler)
 *   changed the package names
 *
 * 1.2 98/09/20-15:38:14 (suhler)
 *   removed *'s imports
 *
 * 1.2 98/09/14-18:03:12 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 3 2 handlers/templates/TemplateHandler.java
 *   Name history : 2 1 handlers/TemplateHandler.java
 *   Name history : 1 0 TemplateHandler.java
 *
 * 1.1 98/09/14-18:03:11 (suhler)
 *   date and time created 98/09/14 18:03:11 by suhler
 *
 */

package sunlabs.brazil.template;

import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.http.HttpUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The <code>TemplateHandler</code> reads a template file from
 * the document root, based on the URL, and passes the content through
 * one or more template filters.
 * <p>
 * The following configuration parameters are used to initialize this
 * <code>Handler</code>: <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URL that triggers this handler.
 *	By default, all URL's are considered.
 *	Only documents that are mime sub-types of <code>text</code>
 *	are processed. See {@link sunlabs.brazil.server.FileHandler} for a
 *	description of how to set mime types for url suffixes.
 * (See {@link MatchString}).
 * <dt> <code>templates</code>
 * <dd> A list of template names.  For each name in the list, the property
 *      <code><i>name</i>.class</code> is examined to determine which class to
 *      use for each template.  Then <code>name</code> is used as the prefix
 *      for other template specific properties if any.  If
 *      <code><i>name</i>.class</code> does not exist, then <code>name</code>
 *      is assumed to be the class name, and there are no template specific
 *      properties for the template.  Methods in the template classes will be
 *      invoked to process the XML/HTML tags present in the content.
 * <dt> <code>session</code>
 * <dd> The name of the request property that the Session ID will be found
 *	in, used to identify the proper template instance.
 *	The default value is "SessionID".  Typically, a sessionHandler, 
 *	such as {@link sunlabs.brazil.handler.CookieSessionHandler} is used
 *	upstream to create the sessionID.  If no id is found, then the
 *	session named "common" is used instead.  Exactly one instance of
 *	each template class is created for each session.
 * <dt> <code>default</code>
 * <dd> The default file in the directory to use as a template if
 *	a directory name is specified.  Defaults to index[suffix], 
 *	or "index.html" if no suffix is provided.
 * <dt> <code>encoding</code>
 * <dd> The character encoding to use to interpret the template.
 *	If no encoding is specified, the default encoding is used.
 *      The template is read from the filesystem, and converted into a 
 *	String using this encoding.  All template processing is done
 *	using the String representation.
 * <dt> <code>outputEncoding</code>
 * <dd> The character encoding to use to interpret the template results.
 *	If no "outputEncoding" is specified, then "encoding" is used.
 *      Once template processing is complete, the results are converted
 *      into a byte stream for transmission to the client using the
 *	"outputEncoding", if specified.  If not specified then the HTTP
 *	default (8-bit ASCII) encoding is used.
 * <dt> <code>modified</code>
 * <dd> if present (e.g. set to any value) an HTTP <code>
 *	last-modified</code> header is added to the response with the
 *	current time.
 * <dt> <code>debug</code>
 *	if set to "true", template debugging is enabled: templates will emit
 *	their pre-processed markup as an HTML comment.  This parameter only takes effect
 *	if the <code>debug</code> option is not specified for an individual template.
 * <dd>
 * <dt> <code>tagPrefix</code>
 * <dd> If specified, all tag names defined for each template class
 *      are prefixed with <i>tagPrefix</i>.
 *	This parameter only takes effect
 *	if the <code>tagPrefix</code> option is not specified for an individual template.
 * <dd>
 * </dl>
 * <p>The request properties <code>DirectoryName</code>,
 *  <code>fileName</code> and <code>lastModified</code> may be
 *  set as a convenience for downstream handlers.
 * <p>
 * This handler duplicates some of the functionality of the
 * {@link  sunlabs.brazil.handler.DefaultFileHandler template filter}, 
 * so that it may be used by itself in simple configurations.  As such, 
 * if issues re-directs if directories are given without a trailing "/", 
 * and uses an "index" file (see <code>default</code> above) if a directory
 * name is specified.
 * <p>
 * To filter content other than from the file system, use the
 * {@link  sunlabs.brazil.filter.TemplateFilter template filter} instead.
 *
 * @author	Stephen Uhler (stephen.uhler@sun.com)
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version	 2.8 07/01/08
 */

public class TemplateHandler implements Handler {
    static final String HANDLER = "handler";
    static final String TEMPLATES = "templates";      // the class to process these templates
    static final String SESSION = "session";  
    static final String ENCODING = "encoding";  
    static final String DEFAULT = "default";    // property for default document, given directory

    String propsPrefix;		// our perfix in the config file
    String sessionProperty;	// where to find the session name
    MatchString isMine;         // check for matching url
    boolean modified;		// if set, emit last-modified header
    String encoding;		// the page character encoding

    Handler handler;
    TemplateRunner runner;		// The template object for our class

    public boolean
    init(Server server, String propsPrefix) {

	this.propsPrefix = propsPrefix;
	isMine = new MatchString(propsPrefix, server.props);

	Properties props = server.props;
        sessionProperty = props.getProperty(propsPrefix + SESSION, "SessionID");
        encoding = props.getProperty(propsPrefix + ENCODING);
        modified = (props.getProperty(propsPrefix + "modified") != null);

	/*
	 * Gather the templates.
	 */

	String str;
	str = server.props.getProperty(propsPrefix + TEMPLATES);
	if (str == null) {
	    server.log(Server.LOG_ERROR, propsPrefix,
		    "no " + TEMPLATES + " property specified");
	    return false;
	}

	try {
	    runner = new TemplateRunner(server, propsPrefix, str);
	} catch (ClassCastException e) {
	    server.log(Server.LOG_ERROR, e.getMessage(), "not a Template");
	    return false;
	} catch (ClassNotFoundException e) {
	    server.log(Server.LOG_ERROR, e.getMessage(), "unknown class");
	    return false;
	}
	return true;
    }

    /**
     * Process an html template file, using the supplied template
     * processing classes.
     */

    public boolean
    respond(Request request) throws IOException {
	if (!isMine.match(request.url)) {
	    return false;
        }

	Properties props = request.props;
	String root = props.getProperty(propsPrefix + FileHandler.ROOT,
		props.getProperty(FileHandler.ROOT, "."));
	String name = FileHandler.urlToPath(request.url);
	File file = new File(root + name);
	String path = file.getPath();
	String outputEncoding = props.getProperty(propsPrefix +
		"outputEncoding", encoding);

	/*
	 * XXX This doesn't belong here.  The DefaultFileHandler should
	 * be used instead.  This allows "simple" configurations using only
	 * this handler.
	 */

	if (file.isDirectory()) {
	    if (request.url.endsWith("/") == false) {
		request.redirect(request.url + "/", null);
		return true;
	    }
	    props.put("DirectoryName", path);
	    String index = props.getProperty(propsPrefix+DEFAULT, "index.html");
	    file = new File(file, index);
	    path = file.getPath();
	}
	props.put("fileName", path);
	request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
		"Looking for template file: " + path);

	int index = path.lastIndexOf('.');
	if (index < 0) {
	    request.log(Server.LOG_INFORMATIONAL, propsPrefix,
		    "no file suffix for: " + path);
	    return false;
	}

	String type = FileHandler.getMimeType(path, props, propsPrefix);
	if (type == null) {
	    request.log(Server.LOG_INFORMATIONAL, propsPrefix,
		    "unknown file suffix for: " + path);
	    return false;
	}

	// make sure this is a text file!

	if (!type.toLowerCase().startsWith("text/")) {
	    request.log(Server.LOG_INFORMATIONAL, propsPrefix,
		    "Not a text file: " + type);
	    return false;
	}

	// OK, now we can read in the file!

	String content;
	try {
	    content =  getContent(request, file, encoding);
	} catch (IOException e) {
	    request.log(Server.LOG_WARNING, propsPrefix, e.getMessage());
	    return false;
	}

        String session = props.getProperty(sessionProperty, "common");
	request.log(Server.LOG_DIAGNOSTIC, propsPrefix, 
		"Using session (" + session + ")");
	String result = runner.process(request,	content, session);
	if (result != null) {
	    if (modified) {
	       request.addHeader("Last-Modified", HttpUtil.formatTime());
	    }
	    FileHandler.setModified(request.props,file.lastModified());
	    if (outputEncoding == null) {
	        request.sendResponse(result, type);
	    } else {
	        request.sendResponse(result.getBytes(outputEncoding),
			  type + "; charset=" + outputEncoding);
	    }
	    return true;
	} else {
	    request.log(Server.LOG_INFORMATIONAL,propsPrefix,runner.getError());
	    return false;
	}
    }

    /**
     * get the content associated with this template.
     * This version reads it from a file.
     *
     * @param request	The standard request object
     * @param file	The file object to get the template from
     * @return		The content of the template to be processed
     */

    public String
    getContent(Request request, File file, String encoding) throws IOException {
	FileInputStream in = new FileInputStream(file);
	byte[] buf = new byte[in.available()];
	in.read(buf);
	in.close();
	String result;
	if (encoding != null) {
	    result = new String(buf, encoding);
	} else {
	    result = new String(buf);
	}
	return result;
    }
}
