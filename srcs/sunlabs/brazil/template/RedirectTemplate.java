/*
 * RedirectTemplate.java
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
 * Contributor(s): cstevens, suhler.
 *
 * Version:  2.1
 * Created by suhler on 98/09/14
 * Last modified by suhler on 02/10/01 16:36:41
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:36:41 (suhler)
 *   version change
 *
 * 1.10 02/05/01-11:28:24 (suhler)
 *   fix sccs version info
 *
 * 1.9 02/04/25-13:43:43 (suhler)
 *   doc fixes
 *
 * 1.8 00/05/31-13:48:34 (suhler)
 *   name change
 *
 * 1.7 00/05/22-14:04:21 (suhler)
 *   doc updates
 *
 * 1.6 99/10/21-18:08:28 (cstevens)
 *   Added ability to change a tag into a comment.  Used by BSL and Tcl templates,
 *   to keep track of where the substitution occurred when examining the resultant
 *   HTML document.
 *
 * 1.5 99/09/29-16:04:17 (cstevens)
 *   New HtmlRewriter object, that allows arbitrary rewriting of the HTML (by
 *   templates and others), instead of forcing the templates to return a string
 *   that contained all of the new HTML content in one big string.
 *
 * 1.4 99/09/15-14:40:41 (cstevens)
 *   Rewritign http server to make it easier to proxy requests.
 *
 * 1.3 99/03/30-09:33:40 (suhler)
 *   documentation update
 *
 * 1.2 98/09/21-14:56:23 (suhler)
 *   changed the package names
 *
 * 1.2 98/09/14-18:05:21 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 handlers/templates/RedirectTemplate.java
 *   Name history : 1 0 templates/RedirectTemplate.java
 *
 * 1.1 98/09/14-18:05:20 (suhler)
 *   date and time created 98/09/14 18:05:20 by suhler
 *
 */

package sunlabs.brazil.template;

/**
 * [<i>Deprecated, use the AddHeaderTemplate instead</i>.]<br>
 * Template class for redirecting an html page
 * This class is used by the TemplateHandler
 * @author		Stephen Uhler
 * @version		@(#)RedirectTemplate.java	2.1
 */

public class RedirectTemplate
    extends Template
{
    String redirect;
    
    /**
     * Look for a redirect tag, change it to an HREF, and remember where
     * to redirect to (e.g. <code>&lt;redirect http://some.where.com&gt;)
     * </code>
     */

    public void
    redirect(RewriteContext hr)
    {
	redirect = hr.getArgs();

	hr.append("<A HREF=" + redirect + ">" + redirect + "</A>");
    }

    /**
     * adjust the response headers to reflect redirection, if supplied.
     * Otherwise, ignore this request.
     */

    public boolean
    done(RewriteContext hr)
    {
	if (redirect != null) {
	    hr.request.addHeader("location", redirect);
	    hr.request.setStatus(302);
	    return true;
    	} else {
	    return false;
	}
    }
}
