/*
 * PropsTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1998-2005 Sun Microsystems, Inc.
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
 * Version:  2.4
 * Created by suhler on 98/09/14
 * Last modified by suhler on 05/11/27 18:41:50
 *
 * Version Histories:
 *
 * 2.4 05/11/27-18:41:50 (suhler)
 *   assume "url.orig" is always defined
 *
 * 2.3 04/11/03-08:35:55 (suhler)
 *   url.orig is now set in request
 *
 * 2.2 04/04/28-14:51:17 (suhler)
 *   doc format fix
 *
 * 2.1 02/10/01-16:36:42 (suhler)
 *   version change
 *
 * 1.38 02/04/25-13:44:02 (suhler)
 *   doc fixes
 *
 * 1.37 02/01/29-14:37:09 (suhler)
 *   doc lint
 *
 * 1.36 01/08/14-16:39:25 (suhler)
 *   doc list
 *
 * 1.35 01/07/16-16:49:09 (suhler)
 *   use new Template convenience methods
 *
 * 1.34 01/05/24-10:44:20 (suhler)
 *   fixed over-exuberant debugging generation
 *
 * 1.33 01/05/11-14:52:37 (suhler)
 *   use template.debug()
 *
 * 1.32 01/03/26-09:57:52 (suhler)
 *   added convert=[url|html]
 *
 * 1.31 01/03/16-14:45:43 (cstevens)
 *   Merged changes between child workspace "/home/cstevens/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.29.1.1 01/03/16-14:36:52 (cstevens)
 *   <property name=foo default=${bar}> was inserting the literal string "${bar}"
 *   rather than the value of the variable "bar" if the variable "foo" was
 *   undefined.
 *
 * 1.30 01/03/15-10:43:38 (suhler)
 *   use "url.orig" if set instead of reuqest.url
 *
 * 1.29 00/12/11-13:29:09 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.28 00/11/28-10:09:23 (suhler)
 *   add ip address to header properties
 *
 * 1.27 00/10/26-15:59:37 (suhler)
 *   implement serializable
 *
 * 1.26 00/10/05-15:51:41 (cstevens)
 *   PropsTemplate.subst() and PropsTemplate.getProperty() moved to the Format
 *   class.
 *
 * 1.25 00/10/05-15:29:50 (cstevens)
 *   Consolidate code for getProperty and subst, in preparation for moving it to
 *   a util class.
 *
 * 1.24 00/07/06-11:35:16 (cstevens)
 *   Replace "[xxx]" syntax for embedded property names with "${xxx}" syntax,
 *   since "[" and "]" were already used by "glob=" and "match=" in BSL.  Escaping
 *   the "[" characters was uglier than expected, and "${}" syntax matches how
 *   the csh, sh, Tcl, and make parsers reference variables.
 *
 * 1.23 00/07/05-14:10:01 (cstevens)
 *   PropsTemplate and BSLTemplate allow '[' and ']' to specify a variable
 *   substitution wherever a constant was used before.  For example, as in
 *   '<if name=stock value="[favorite]">' to compare the value of property "stock"
 *   not to the constant "[favorite]", but to the value of the property "favorite".
 *   In order to include a literal "[" in an option, use "\[" -- a backslash
 *   escapes the following character, whatever it is.
 *
 * 1.22 00/06/28-16:23:02 (cstevens)
 *   If "name=" was misspelled in <property> tag, NullPointerException.
 *
 * 1.21 00/06/09-15:03:56 (suhler)
 *   add more stuff to headers option
 *
 * 1.20 00/05/31-13:48:40 (suhler)
 *   name change
 *
 * 1.19 00/05/22-14:04:33 (suhler)
 *   doc updates
 *
 * 1.18 00/05/10-13:36:36 (suhler)
 *   added documentation
 *
 * 1.17 00/04/27-16:05:03 (cstevens)
 *   Merged changes between child workspace "/home/cstevens/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.15.1.1 00/04/27-16:01:55 (cstevens)
 *   subst() method, used by BSL for sorting properties.
 *
 * 1.16 00/04/26-16:08:06 (suhler)
 *   added documentation
 *
 * 1.15 00/04/12-15:55:16 (cstevens)
 *   Don't put error comment in result unless debug flag is turned on.
 *
 * 1.14 00/03/29-16:13:49 (cstevens)
 *   PropsTemplate exposes public method to parse and dereference composed property
 *   names (using the '[' and ']' characters).  Method is used by BSLTemplate.
 *
 * 1.13 00/03/02-17:54:08 (cstevens)
 *   Use pattern of the form [XXX] to substitute in the value of a property in
 *   the middle of another property in the <property> tag.
 *
 * 1.12 00/02/02-09:34:55 (suhler)
 *   claim to be serializable
 *
 * 1.11 99/11/08-16:20:19 (suhler)
 *   add options to place the query data and mime headers into the request
 *   properties
 *
 * 1.10 99/10/21-18:08:36 (cstevens)
 *   Added ability to change a tag into a comment.  Used by BSL and Tcl templates,
 *   to keep track of where the substitution occurred when examining the resultant
 *   HTML document.
 *
 * 1.9 99/10/06-12:17:48 (suhler)
 *   bug fix
 *
 * 1.8 99/09/29-16:04:24 (cstevens)
 *   New HtmlRewriter object, that allows arbitrary rewriting of the HTML (by
 *   templates and others), instead of forcing the templates to return a string
 *   that contained all of the new HTML content in one big string.
 *
 * 1.7 99/09/01-12:15:51 (suhler)
 *   make "url" a request property
 *
 * 1.6 99/06/28-10:51:30 (suhler)
 *   added debugging
 *
 * 1.5 99/05/13-09:23:42 (suhler)
 *   Added a "tag" method to allow property substitution within
 *   html entities.
 *
 * 1.4 99/03/30-09:33:49 (suhler)
 *   documentation update
 *
 * 1.3 99/03/09-11:18:20 (suhler)
 *   addede additional signiture to allow for default value
 *
 * 1.2 98/09/21-15:04:08 (suhler)
 *   changed the package names
 *
 * 1.2 98/09/14-18:05:20 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 handlers/templates/PropsTemplate.java
 *   Name history : 1 0 templates/PropsTemplate.java
 *
 * 1.1 98/09/14-18:05:19 (suhler)
 *   date and time created 98/09/14 18:05:19 by suhler
 *
 */

package sunlabs.brazil.template;

import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.http.HttpUtil;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Dictionary;
import java.util.Properties;
import java.io.Serializable;

/**
 * [<i>Deprecated, use the the SetTemplate</i>.]<br>
 * Template class for substituting request properties into an HTML page.
 * This class is used by the TemplateHandler
 * The following request properties are used:
 * <dl class=props>
 * <dt>query	<dd> The query parameters are placed into the request object,
 *	        prefixed by the value assigned to "query".
 * <dt>headers	<dd> The mime headers are placed into the request object,
 *		prefixed by the value assigned to "headers".  The values:
 *		url, query, method, and version are copied from the request
 *		object into the properties. The clients IP address is
 *		saved in the "address" property.
 * <dt>url.orig	<dd> If set and "headers" are requested, this value is used as
 *		the <code>url</code> instead of the one in request.url. 
 * </dl>
 * <p>
 * A new HTML tag,  
 * <code>&lt;property&gt;</code> is defined.  It takes the following 
 * tag attributes:
 * <dl>
 * <dt>name	<dd> The name of the property in
 *                   {@link sunlabs.brazil.server.Request#props props}
 *		     to replace the <code>property</code> tag with.
 * <dt>default	<dd> The value to use if the property is not defined.
 *		     If no <code>default</code> is specified, the empty
 *		     string is used instead.
 * <dt>convert	<dd> The value is converted before substitution into
 *		     the content.  Convert accepts either:
 *		     <ul>
 *		     <li><b>convert=html</b>Protect plain text by escaping the
 *			characters &quot; &lt; &gt; and &amp;.
 *		     <li><b>convert=url</b>Make sure the string is suitable
 *			for use in a url or query string.
 *		     </ul>
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		%V% PropsTemplate.java 2.4
 */

public class PropsTemplate
    extends Template implements Serializable
{
    /**
     * This gets called at every page, at the beginning.  See if we should add
     * the mime headers and query parameters into the request object
     */

    public boolean
    init(RewriteContext hr)
    {
	Properties props = hr.request.props;
	String query = props.getProperty(hr.prefix + "query");
	if (query != null) {
	    Dictionary h = hr.request.getQueryData(null);
	    Enumeration keys = h.keys();
	    while(keys.hasMoreElements()) {
		String key = (String) keys.nextElement();
		props.put(query + key, h.get(key));
	    }
	}

	String headers = props.getProperty(hr.prefix + "headers");
	if (headers != null) {
	    Enumeration keys = hr.request.headers.keys();
	    while(keys.hasMoreElements()) {
		String key = (String) keys.nextElement();
		props.put(headers + key.toLowerCase(),
			hr.request.headers.get(key));
	    }
	    props.put(headers + "method", hr.request.method);
	    props.put(headers + "url", props.getProperty("url.orig"));
	    props.put(headers + "query", hr.request.query);
	    props.put(headers + "protocol", hr.request.protocol);
    	    props.put(headers + "address",
		"" + hr.request.getSocket().getInetAddress().getHostAddress());
	}
	return super.init(hr);
    }

    /**
     * Convert the html tag "property" in to the request's property
     * @param key	The name of the property to substitute.  Variable
     *			substitution using the style described in 
     *		 	{@link Format#getProperty} is permitted, e.g.:
     *			<code>employee.${id}.last</code>
     */

    public void
    tag_property(RewriteContext hr) {
	String name = hr.getArgs();
	String result = null;
	if (name.indexOf('=') >= 0) {
	    name = hr.get("name");
	    result = hr.get("default");
	} else {
	    name = Format.subst(hr.request.props, name);
	}

	if (name == null) {
	    debug(hr, "property: no name");
	    return;
	}
	result = hr.request.props.getProperty(name,result);

	if (result != null) {
	    String convert = hr.get("convert");
	    if ("html".equals(convert)) {
		result = HttpUtil.htmlEncode(result);
	    } else if ("url".equals(convert)) {
		result = HttpUtil.urlEncode(result);
	    }
	    hr.append(result);
	} else {
	    debug(hr, "property: no value for " + name);
	}
	hr.killToken();
    }

    /**
     * Insert a literal "&lt;".
     * Using the current scheme, there is no easy way to substitute into
     * a tag parameter.  So we'll invent a "magic" tag (called tag)
     * that will allow us to create entities dynamically.  Thus values
     * can be substituted into entities by escaping the entity as in:
     * <pre>
     * &lt;tag&gt;a href=&lt;property href&gt;&lt;/tag&gt;
     * </pre>
     */

    public void
    tag_tag(RewriteContext hr)
    {
	hr.append("<");
    }

    /**
     * Insert a literal "&gt;"
     */

    public void
    tag_slash_tag(RewriteContext hr)
    {
	hr.append(">");
    }
}
