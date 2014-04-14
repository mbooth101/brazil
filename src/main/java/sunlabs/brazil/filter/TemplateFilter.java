/*
 * TemplateFilter.java
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
 * Contributor(s): cstevens, drach, suhler.
 *
 * Version:  2.4
 * Created by suhler on 99/07/29
 * Last modified by suhler on 04/12/30 12:42:21
 *
 * Version Histories:
 *
 * 2.4 04/12/30-12:42:21 (suhler)
 *   javadoc fixes
 *   .
 *
 * 2.3 04/11/30-15:11:26 (suhler)
 *   fixed sccs version string
 *
 * 2.2 03/07/14-09:57:30 (suhler)
 *   added tagPrefix option docs
 *
 * 2.1 02/10/01-16:39:01 (suhler)
 *   version change
 *
 * 1.27 02/08/21-15:36:01 (suhler)
 *   add "outputEncoding"
 *
 * 1.26 02/07/08-15:19:32 (suhler)
 *   add "encoding" option
 *
 * 1.25 02/04/24-13:41:00 (suhler)
 *   change the defualt mime type to "text/" from "text/html"
 *
 * 1.24 01/07/16-16:43:35 (suhler)
 *   update docs
 *
 * 1.23 01/05/02-15:23:41 (drach)
 *   Add template tokens.
 *
 * 1.22 00/12/11-13:25:57 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.21 00/12/08-16:47:15 (suhler)
 *   doc fixes
 *
 * 1.20 00/11/28-10:09:55 (suhler)
 *   change log level
 *
 * 1.19 00/10/20-15:42:23 (suhler)
 *   api change due to race condition in TemplateRunner
 *
 * 1.18 00/10/05-14:47:48 (suhler)
 *   added "subtype" parameter to allow other mime subtypes other than html to
 *   be processed (still needs to be text/?? though).
 *   .
 *
 * 1.17 00/07/05-13:44:22 (cstevens)
 *   Server object is in RewriteContext used by templates, so templates (such as
 *   the TclServerTemplate) can be initialized with the server and prefix, similar
 *   to how Handlers are initialized.
 *
 * 1.16 00/05/31-13:43:32 (suhler)
 *   fix docs
 *
 * 1.15 00/05/24-11:25:06 (suhler)
 *   add diagnostics
 *
 * 1.14 00/05/03-11:36:19 (cstevens)
 *   bugs when converting from using "Cookie" header to using Session ID.
 *
 * 1.13 00/04/27-16:04:09 (cstevens)
 *   Merged changes between child workspace "/home/cstevens/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.11.1.1 00/04/27-16:01:27 (cstevens)
 *   doc
 *
 * 1.12 00/04/27-13:23:52 (suhler)
 *   fixed docs
 *
 * 1.11 00/04/24-14:04:48 (cstevens)
 *   error message
 *
 * 1.10 00/04/17-14:16:55 (cstevens)
 *   TemplateHandler.java, TemplateFilter.java:
 *   1. configuration property "templateClass" -> "templates"
 *
 * 1.9 00/03/10-16:57:46 (cstevens)
 *   API documentation and documenting pending issues
 *
 * 1.8 00/02/11-08:57:31 (suhler)
 *   better diagnostics
 *
 * 1.7 99/10/06-12:38:48 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.5.1.1 99/10/06-12:29:56 (suhler)
 *   use mime headers
 *
 * 1.6 99/10/01-11:27:25 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.5 99/09/01-15:31:29 (suhler)
 *   small stuff
 *
 * 1.4 99/08/30-09:38:01 (suhler)
 *   fix imports
 *
 * 1.3 99/08/06-08:31:23 (suhler)
 *   added call to respond method
 *
 * 1.2 99/07/30-10:48:58 (suhler)
 *   lint
 *
 * 1.2 99/07/29-16:17:21 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 filter/TemplateFilter.java
 *   Name history : 1 0 tail/TemplateFilter.java
 *
 * 1.1 99/07/29-16:17:20 (suhler)
 *   date and time created 99/07/29 16:17:20 by suhler
 *
 */

package sunlabs.brazil.filter;

import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.http.MimeHeaders;
import sunlabs.brazil.template.TemplateRunner;
import java.io.UnsupportedEncodingException;

import java.util.Properties;

/**
 * The <code>TemplateFilter</code> sends HTML content through an
 * Html/XML parser to a set of <code>Template</code>s.  Each Html/XML tag may
 * dynamically invoke a Java method present in the <code>Template</code>s.
 * The dynamically-generated content from evaluating the Html/XML tags is
 * returned to the caller.
 * <p>
 * The following configuration parameters are used to initialize this
 * <code>Filter</code>.
 * <dl class=props>
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
 * <dd> The request property that contains the session ID.  If no
 *	"session" property is found with the supplied prefix, then
 *	the global "session" property is used instead.  The default
 *	value is "SessionID".
 * <dt> <code>subtype</code>
 * <dd> Restrict this template to only handle specified text sub-types.
 *	defaults to the empty string, which implies any text sub-type.
 * <dt> <code>encoding</code>
 * <dd> The charset encoding to use to represent the content as text.
 *	If none is specified, the default encoding is used.
 * <dt> <code>outputEncoding</code>
 * <dd> The character encoding to use to interpret the template results.
 *	If no "outputEncoding" is specified, then "encoding" is used.
 *      Once template processing is complete, the results are converted
 *      into a byte stream for transmission to the next filter, using
 *	"outputEncoding", if specified.  If not specified then the
 *	default encoding is used.
 * <dt> <code>tagPrefix</code>
 * <dd> If specified, all tag names defined for each template class are prefixed with
 *      <i>tagPrefix</i>.
 *      This parameter only takes effect
 *      if the <code>tagPrefix</code> option is not specified for an individual template.
 * </dl>
 * The <code>TemplateHandler</code> class is similar, but not identical to
 * running a <code>FilterHandler</code> with the <code>FileHandler</code> and 
 * the <code>TemplateFilter</code>.  The differences between the two should
 * be resolved.
 * <p>
 * Note: The <code>templates</code> property accepts a list of
 * class names or <i>tokens</i> that could be used to represent
 * class names.  If class <i>names</i> are used, all
 * template classes share the TemplateHandler's properties prefix.
 *
 * @author	Stephen Uhler (stephen.uhler@sun.com)
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version	2.4
 */

public class TemplateFilter implements Filter {
    private static final String TEMPLATES = "templates"; 
    private static final String SESSION = "session";

    Server server;
    String prefix;    
    String subtype;		// media subtype to filter (must be text)

    String session = "SessionID";	// our session ID property name

    private TemplateRunner runner;	// The template object for our class

    public boolean
    init(Server server, String prefix)
    {
	Properties props = server.props;

	this.server = server;
	this.prefix = prefix;

	String name = props.getProperty(prefix + TEMPLATES, "");
	subtype = "text/" + props.getProperty(prefix + "subtype", "");
	try {
	    runner = new TemplateRunner(server, prefix, name);
	} catch (Exception e) {
	    server.log(Server.LOG_ERROR, prefix,
		    "Can't instantiate " + e.getMessage());
	    return false;
	}

	session = props.getProperty(prefix + SESSION, session);

	return true;
    }

    /**
     * No action before request is made
     */

    public boolean
    respond(Request request)
    {
	return false;
    }

    /**
     * Filters all HTML files, or files that are likely to be html files,
     * specifically, those whose "Content-Type" starts with "text/".
     */
    public boolean
    shouldFilter(Request request, MimeHeaders headers)
    {
	String type = headers.get("Content-Type");
	boolean filter= ((type != null) &&
		type.toLowerCase().startsWith("text/"));
	request.log(Server.LOG_DIAGNOSTIC, prefix + "type: " + type +
		    " filter?: " + filter);
	return filter;
    }

    /**
     * Evaluates the content as html/XML tags, if the file is (or has now been
     * converted to) "text/html".
     */
    public byte[]
    filter(Request request, MimeHeaders headers, byte[] content)
    {
	if (headers.get("Content-Type").toLowerCase().startsWith(subtype)
		    == false) {
	    request.log(Server.LOG_DIAGNOSTIC, prefix +
		    " Not " + subtype + ", skipping");
	    return content;
	}

	/*
	 * Get the cookie out of the request object (presumably from the
	 * sessionhandler).  If not there,  don't use cookies.
	 */

        String sessionId = request.props.getProperty(session);
	String encoding = request.props.getProperty(prefix + "encoding",
		request.props.getProperty("encoding"));
	String outputEncoding = request.props.getProperty(prefix +
		"outputEncoding", encoding);
	if (sessionId == null) {
	    sessionId = "noCookie";
	    request.log(Server.LOG_WARNING, "template not using cookies");
	}

	/*
	 * Process the content as a template.  If there is an error, 
	 * leave the content unchanged.
	 */

        String enc = null;
	if (encoding != null) {
	    try {
	       enc = new String(content, encoding);
	    } catch(UnsupportedEncodingException e) {
		request.log(Server.LOG_WARNING, "template Error: " +
		    e.getMessage());
	    }
	}
	if (enc == null) {
	    enc = new String(content);
	}

	String result = runner.process(request, enc, sessionId);
	if (result != null) {
	    if (outputEncoding != null) {
		try {
	            return result.getBytes(outputEncoding);
		} catch (UnsupportedEncodingException e) {
		    request.log(Server.LOG_WARNING, e.getMessage());
		}
	    }
	    return result.getBytes();
	} else {
	    request.log(Server.LOG_INFORMATIONAL, "template Error: " +
		    runner.getError());
	    return content;
	}
    }
}
