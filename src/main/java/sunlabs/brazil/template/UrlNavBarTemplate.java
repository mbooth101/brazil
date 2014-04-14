/*
 * UrlNavBarTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.
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
 * Version:  2.1
 * Created by suhler on 00/05/22
 * Last modified by suhler on 02/10/01 16:36:39
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:36:39 (suhler)
 *   version change
 *
 * 1.8 02/07/24-10:46:46 (suhler)
 *   doc updates
 *
 * 1.7 02/05/01-11:27:34 (suhler)
 *   fix sccs version info
 *
 * 1.6 00/12/11-13:30:40 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.5 00/10/09-10:53:13 (suhler)
 *   added IncludeDir flag to include nav bar entries even if the url ends with /
 *
 * 1.4 00/07/07-15:32:43 (suhler)
 *   do processing in "init" instead of "done" (duh!), add prepend
 *   option to modify properties
 *
 * 1.3 00/05/31-13:50:38 (suhler)
 *   name change
 *
 * 1.2 00/05/24-11:27:01 (suhler)
 *   redo properties extraction
 *
 * 1.2 00/05/22-14:44:27 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/UrlNavBarTemplate.java
 *
 * 1.1 00/05/22-14:44:26 (suhler)
 *   date and time created 00/05/22 14:44:26 by suhler
 *
 */

package sunlabs.brazil.template;

import java.util.StringTokenizer;
import sunlabs.brazil.server.Server;

/**
 * Template class for dynamically generating a navigation bar
 * by looking at portions of the url.
 * Given url:
 *  <code>/main/next/last/foo.html</code>
 * generate the request properties for the directories:
 *   <code>main</code>, <code>next</code>,  and <code>last</code>.
 * The properties will be:
 * <pre>
 * NAV.main=/main/
 * NAV.next=/main/next/ ....
 * NAV.=main/next/....
 * </pre>
 * These properties may be incorporated into web pages using the
 * BSLTemplate's &lt;foreach&gt; tag, using a delimeter of "/" to
 * iterate over the listings.
 * <p>
 * The follow request properties are consulted:
 * <dl class=props>
 * <dt>prepend	<dd>Use as a prefix on the property name, instead
 *		of "NAV.".
 * <dt>includeDir<dd> Normally, if the URL refers to the directory (
 *		e.g. it ends with a /), no nav bar entry is generated.
 *		If this property is set, the entry is generated.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		@(#)UrlNavBarTemplate.java	2.1
 */

public class UrlNavBarTemplate
    extends Template
{
    StringBuffer navbar;	// the extracted NavBar
    StringBuffer url;		// the url to date

    /**
     * Compute a set of properties based on the URL
     */

    public boolean
    init(RewriteContext hr) {
	url = new StringBuffer("/");
	String prepend = hr.request.props.getProperty(hr.prefix + "prepend",
		"NAV.");
	String includeDir = (hr.request.props.getProperty(hr.prefix +
		"includeDir") == null) ? "" : "x";
	StringTokenizer st = new StringTokenizer(hr.request.url + includeDir,
		"/");
	int count = 0;
	String path = null;
	while (st.hasMoreTokens()) {
	    String dir = st.nextToken();
	    if (!st.hasMoreTokens()) {
		break;
	    }
	    url.append(dir + "/");
	    if (url.equals(hr.request.url)) {
		break;
	    }
	    count++;
	    path = url.toString();
	    hr.request.props.put(prepend + dir,  path);
	}
	if (count > 0) {
	    hr.request.props.put(prepend, path.substring(1,path.length()-1));
	}
	hr.request.log(Server.LOG_DIAGNOSTIC,hr.prefix,
		"Computing nav-bar entries: " + count);

	return true;
    }
}
