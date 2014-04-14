/*
 * PlainFilter.java
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
 * Contributor(s): suhler.
 *
 * Version:  2.2
 * Created by suhler on 99/07/29
 * Last modified by suhler on 04/11/30 15:19:39
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:39 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:39:00 (suhler)
 *   version change
 *
 * 1.12 02/07/24-10:44:31 (suhler)
 *   doc updates
 *
 * 1.11 01/09/13-09:23:24 (suhler)
 *   remove uneeded import
 *
 * 1.10 01/04/09-12:32:11 (suhler)
 *   use HttpUtil.HtmlEncode instead of built-in private method
 *   to protect html
 *
 * 1.9 00/12/11-13:25:51 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.8 00/10/31-10:17:37 (suhler)
 *   doc fixes
 *
 * 1.7 00/05/24-11:24:56 (suhler)
 *   parse error
 *
 * 1.6 99/10/06-12:29:29 (suhler)
 *   use MimeHeaders
 *
 * 1.5 99/09/01-15:30:35 (suhler)
 *   protect special chars, protect against no content typs
 *   .
 *
 * 1.4 99/08/30-09:37:08 (suhler)
 *   remove wildcarded imports, fix docs
 *
 * 1.3 99/08/30-09:23:33 (suhler)
 *   typo
 *
 * 1.2 99/08/06-08:30:51 (suhler)
 *   added respond() call
 *
 * 1.2 99/07/29-16:17:20 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 filter/PlainFilter.java
 *   Name history : 1 0 tail/PlainFilter.java
 *
 * 1.1 99/07/29-16:17:19 (suhler)
 *   date and time created 99/07/29 16:17:19 by suhler
 *
 */

package sunlabs.brazil.filter;

import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.http.MimeHeaders;
import sunlabs.brazil.util.http.HttpUtil;

/**
 * Filter to turn text/plain into html.  This allows plain text to 
 * be processed by other filters that only deal with html.
 * <p>
 * The following server properties are used:
 * <dl class=props>
 * <dt>template	<dd> The string to use as an html template.  The string
 *		     should contain a single "%", which is replaced by the 
 *		     text/plain content.  The default stuff the content
 *		     between &lt;pre&gt;...&lt;/pre&gt;.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		2.2
 */

public class PlainFilter implements Filter {
    String template1;		// html wrapper template (prefix)
    String template2;		// html wrapper template (postfix)

    public boolean
    init(Server server, String prefix) {
	String template = server.props.getProperty(prefix + "template", 
		"<title>text document</title><body bgcolor=white><pre>" +
		"%</pre></body>");
	int index = template.indexOf("%");
	if (index > 0) {
	    template1 = template.substring(0,index);
	    template2 = template.substring(index+1);
	    return true;
	} else {
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
     * Only filter text/plain documents
     */

    public boolean
    shouldFilter(Request request, MimeHeaders headers) {
	String type = headers.get("content-type");
	return (type != null && type.toLowerCase().startsWith("text/plain"));
    }

    /**
     * Wrap html around text/plain, converting it to html.
     * Change the content-type to text/html.
     */

    public byte[]
    filter(Request request, MimeHeaders headers, byte[] content) {
	String result =  template1 + HttpUtil.htmlEncode(new String(content)) +
		template2;
	headers.put("content-type", "text/html");
	return result.getBytes();
    }
}
