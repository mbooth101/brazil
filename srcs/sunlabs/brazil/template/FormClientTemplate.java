/*
 * FormClientTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1998-2002 Sun Microsystems, Inc.
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
 * Contributor(s): cstevens, rinaldo, suhler.
 *
 * Version:  2.1
 * Created by suhler on 98/10/27
 * Last modified by suhler on 02/10/01 16:36:43
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:36:43 (suhler)
 *   version change
 *
 * 1.14 02/07/24-10:46:53 (suhler)
 *   doc updates
 *
 * 1.13 02/05/01-11:27:24 (suhler)
 *   fix sccs version info
 *
 * 1.12 02/04/25-13:43:04 (suhler)
 *   doc fixes
 *
 * 1.11 00/12/11-20:25:48 (suhler)
 *   doc changes
 *
 * 1.10 00/12/11-13:29:49 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.9 00/05/31-13:49:04 (suhler)
 *   name change
 *
 * 1.8 00/04/26-16:08:18 (suhler)
 *   added documentation
 *
 * 1.7 99/10/21-18:08:41 (cstevens)
 *   Added ability to change a tag into a comment.  Used by BSL and Tcl templates,
 *   to keep track of where the substitution occurred when examining the resultant
 *   HTML document.
 *
 * 1.6 99/09/29-16:05:04 (cstevens)
 *   New HtmlRewriter object, that allows arbitrary rewriting of the HTML (by
 *   templates and others), instead of forcing the templates to return a string
 *   that contained all of the new HTML content in one big string.
 *
 * 1.5 99/05/24-10:14:07 (suhler)
 *   - better documentation
 *   - added <tag> and </tag> processing
 *
 * 1.4 99/03/30-09:34:10 (suhler)
 *   documentation update
 *
 * 1.3 98/11/10-13:46:38 (suhler)
 *   Merged changes between child workspace "/home/suhler/naws" and
 *   parent workspace "/net/smartcard.eng/export/ws/brazil/naws".
 *
 * 1.1.1.2 98/11/10-13:33:54 (suhler)
 *   added an option to <subst> to provied default values
 *
 * 1.2 98/11/10-13:21:30 (rinaldo)
 *
 * 1.1.1.1 98/11/08-11:36:40 (suhler)
 *   added <subst> processing
 *
 * 1.2 98/10/27-14:32:47 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 handlers/templates/FormClientTemplate.java
 *   Name history : 1 0 templates/FormClientTemplate.java
 *
 * 1.1 98/10/27-14:32:46 (suhler)
 *   date and time created 98/10/27 14:32:46 by suhler
 *
 */

package sunlabs.brazil.template;

import java.util.Hashtable;
import sunlabs.brazil.server.Server;

/**
 * [<i>Deprecated, use the FormTemplate and SetTemplate instead</i>]<br>
 * SAMPLE Template class for substituting Default values into html forms
 * The data is retrieved from the client, and sent back to
 * the client later on.  This will be used for e-business cards
 * stored on java rings/cards.  This template also incorporates the functionallity
 * of the PropsTemplate, as the current scheme doesn't allow composition of
 * template handler classes (at least for now).
 *
 * If a URL contains query data, and the value of the server
 * property "uploadContains" occurs in the URL, then all of the query data
 * is saved in the server on behalf of the client.  IF no "uploadContains"
 * string is set, all query data is saved on the server.
 *
 * The following Html entities processed by this class:
 * <dl class=tags>
 * <dt> input
 * <dd> if the "name" attribute has saved data, the value attribute is
 *	replaced by the caved value.  This allows default form values to be
 *	replaced by previous data submitted to the server by the client.
 * <dt> property
 * <dd> This tag is replaced by the value of a server property.
 * <dt> subst
 * <dd> This tag is replaced by the value previously uploaded to the the
 *	server from the client for this value.
 * <dt> tag, /tag
 * <dd> Inserts a "<" or ">" respectively, allowing parameters to
 *	be substituted inside of other entities.  For example, suppose
 *	the client uploaded the value <b>HOME</b> as
 *	<code>http://my.home.com/</code>, and the server withes to create a
 *	link to that page.  The template fragment:
 *	"&lt;a href=&lt;subst HOME&gt;gt;
 *	won't work, as nested entities are not allowed.  Instead, try:
 *	"&lt;tag&gt;a href=&lt;subst HOME&gt;lt;tag&gt;
 * </dl>
 *
 * @author		Stephen Uhler
 * @version           @(#)FormClientTemplate.java	2.1
 */

public class FormClientTemplate
    extends Template
{
    Hashtable data;	// data sent from client, to be stored on server
    String uploadUrl;	// url's to upload data must contain this string
    static final String UPLOAD = "uploadContains";

    public
    FormClientTemplate()
    {
	data = new Hashtable();
    }
    
    /**
     * Save a reference to our request properties.
     * If the URL contains the upload string, save all of the query
     * parameters on behalf of the user.
     */

    public boolean
    init(RewriteContext hr)
    {
	uploadUrl = hr.request.props.getProperty(hr.prefix + UPLOAD, "");
	log(hr, "Upload url: " + uploadUrl);

	/*
	 * If this is an "upload" url, save the data, and ignore the rest
	 * of the request. I'm not sure how to identify upload url's yet.
	 * For now, anything that has query data??
	 */

	if (hr.request.url.indexOf(uploadUrl) != -1) {
	    data = hr.request.getQueryData(data);
	    log(hr, "saving data for: " + hr.request.url);
	}
	log(hr, "data for: " + hr.request.url + ": " + data);
	return true;
    }

    /**
     * Look for <input name=[x] value=[v]>, and replace the
     * value with the entry in the previously saved client data.
     * @param h		The attribute/value pairs for this entity.
     */

    public void
    tag_input(RewriteContext hr)
    {
	String name = hr.get("name");
	if (name == null) {
	    return;
	}

	String value = (String) data.get(name);
	if (value == null) {
	    return;
	}

	hr.put("value", value);
    }

    /**
     * Convert the html tag "property" in to the request's property.
     * @param h		Hashtable containing tag parameters
     *			"name"	The property name
     *			"default" a default value, if no name available
     */

    public void
    tag_property(RewriteContext hr)
    {
	String name = hr.getArgs();
	String value = null;

	if (name.indexOf('=') >= 0) {
	    name = hr.get("name");
	    value = hr.get("default");
	}
	hr.append(hr.request.props.getProperty(name, value));
    }

    /**
     * Tag to do substitution of previously uploaded data.
     * @param name	The name of the token to replace with client data.
     */

    public void
    tag_subst(RewriteContext hr)
    {
	String name = hr.getArgs();
	String value = null;
	if (name.indexOf('=') >= 0) {
	    name = hr.get("name");
	    value = hr.get("default");
	}
	String result = (String) data.get(name);
	if (result == null) {
	    result = value;
	}
	hr.append(result);
    }

    /**
     * Using the current scheme, there is no easy way to substitute into
     * a tag parameter.  So we'll invent a "magic" tag (called tag)
     * that will allow us to create entities dynamically
     */

    public void
    tag_tag(RewriteContext hr)
    {
	hr.append("<");
    }

    public void
    tag_slash_tag(RewriteContext hr)
    {
	hr.append(">");
    }

    /**
     * simple interface to server logging
     */

    private void
    log(RewriteContext hr, String msg) {
	hr.request.log(Server.LOG_DIAGNOSTIC,
		hr.prefix + "formClientTemplate: " + msg);
    }
}
