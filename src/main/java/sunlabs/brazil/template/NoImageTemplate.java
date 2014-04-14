/*
 * NoImageTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2003 Sun Microsystems, Inc.
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
 * Version:  2.2
 * Created by suhler on 99/06/18
 * Last modified by suhler on 03/07/15 09:11:24
 *
 * Version Histories:
 *
 * 2.2 03/07/15-09:11:24 (suhler)
 *   Doc fixes for pdf version of the manual
 *
 * 2.1 02/10/01-16:36:44 (suhler)
 *   version change
 *
 * 1.10 02/05/01-11:28:16 (suhler)
 *   fix sccs version info
 *
 * 1.9 01/07/16-16:47:54 (suhler)
 *   use new Template convenience methods
 *
 * 1.8 01/04/09-18:43:32 (suhler)
 *   weird typo
 *
 * 1.7 00/07/07-17:01:52 (suhler)
 *
 * 1.6 00/05/31-13:49:19 (suhler)
 *   name change
 *
 * 1.5 99/10/26-17:11:48 (cstevens)
 *   Get rid of public variables Request.server and Request.sock:
 *   A. In all cases, Request.server was not necessary; it was mainly used for
 *   constructing the absolute URL for a redirect, so Request.redirect() was
 *   rewritten to take an absolute or relative URL and do the right thing.
 *   B. Request.sock was changed to Request.getSock(); it is still rarely used
 *   for diagnostics and logging (e.g., ChainSawHandler).
 *
 * 1.4 99/09/29-16:05:26 (cstevens)
 *   New HtmlRewriter object, that allows arbitrary rewriting of the HTML (by
 *   templates and others), instead of forcing the templates to return a string
 *   that contained all of the new HTML content in one big string.
 *
 * 1.3 99/09/15-14:40:46 (cstevens)
 *   Rewritign http server to make it easier to proxy requests.
 *
 * 1.2 99/06/28-10:52:14 (suhler)
 *   ??
 *
 * 1.2 99/06/18-08:58:57 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/NoImageTemplate.java
 *
 * 1.1 99/06/18-08:58:56 (suhler)
 *   date and time created 99/06/18 08:58:56 by suhler
 *
 */

package sunlabs.brazil.template;

/**
 * Sample template class for removing all images
 * from a web page, and replacing them with their alt strings.
 * This class is used by the TemplateHandler
 * Each image is replaced by a text string defined by the server property
 * "template", which the first "%" replaced by the contents of the
 * alt attribute.  Defaults to "[&lt;b&gt;%&lt;b&gt;]".
 * <dl class=props>
 * <dt><code>template</code>
 * <dd>The text used to replace the image. The first "%" will contain the image
 * "alt" string, if any.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		NoImageTemplate.java
 */

public class NoImageTemplate
    extends Template
{
    private String template = "[<b>%</b>]";
    private boolean filterOther; // only filter non-local images
    private String mine;
    private int index;

    /**
     * Save a reference to our request properties
     * We'll use it some day to tailor the image display
     */

    public boolean
    init(RewriteContext hr)
    {
	template  = hr.request.props.getProperty(hr.prefix + "template",
		template);
	filterOther  = (null != hr.request.props.getProperty(hr.prefix + "nonlocal", null));
	mine = hr.request.serverUrl();
	index = template.indexOf("%");
	// System.out.println("mine=" + mine + "  filter = " + filterOther);

	return true;
    }

    /**
     * Convert the html tag img into text using the alt string
     * @param h	The name/value pairs
     *  src=
     *  alt=<...>
     */

    public void
    tag_img(RewriteContext hr)
    {

	// Only nuke non-local images - XXX for testing

	if (filterOther) {
	    String src = hr.get("src",null);
	    if (src.indexOf("http://") < 0 || src.indexOf(mine) >= 0) {
		return;
	    }
	    // System.out.println( "Stripping image: " + src);
	}

	String alt = hr.get("alt",null);
	if (alt == null) {
	    alt = "image";
	}
	if (index >= 0) {
	    hr.append(template.substring(0,index) +
		    alt + template.substring(index+1));
	} else {
	    hr.append(template);
	}
    }
}
