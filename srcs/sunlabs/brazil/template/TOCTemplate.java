/*
 * TOCTemplate.java
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
 * Created by suhler on 99/09/01
 * Last modified by suhler on 03/07/07 14:02:52
 *
 * Version Histories:
 *
 * 2.2 03/07/07-14:02:52 (suhler)
 *   use addClosingTag() convenience method
 *
 * 2.1 02/10/01-16:36:45 (suhler)
 *   version change
 *
 * 1.8 02/05/01-11:27:51 (suhler)
 *   fix sccs version info
 *
 * 1.7 00/05/31-13:49:33 (suhler)
 *   name change
 *
 * 1.6 00/05/24-11:36:26 (suhler)
 *   oops
 *
 * 1.5 00/05/24-11:26:28 (suhler)
 *   redo properties extraction
 *
 * 1.4 00/05/22-14:05:42 (suhler)
 *   set properties for BSL instead of a fixed string
 *
 * 1.3 99/10/06-12:18:03 (suhler)
 *   bug fix
 *
 * 1.2 99/09/29-16:05:35 (cstevens)
 *   New HtmlRewriter object, that allows arbitrary rewriting of the HTML (by
 *   templates and others), instead of forcing the templates to return a string
 *   that contained all of the new HTML content in one big string.
 *
 * 1.2 99/09/01-15:38:27 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/TOCTemplate.java
 *
 * 1.1 99/09/01-15:38:26 (suhler)
 *   date and time created 99/09/01 15:38:26 by suhler
 *
 */

package sunlabs.brazil.template;

/**
 * Template class for extracting table of contents information
 * out of an html page by examining the "H1" tags, and setting
 * request properties that can be used to build a table of contents.
 * This class is used by the TemplateHandler.
 *
 * @author		Stephen Uhler
 * @version		@(#)TOCTemplate.java	2.2
 */

public class TOCTemplate
    extends Template
{
    StringBuffer extract;	// the extracted TOC
    int count = 0;

    public boolean
    init(RewriteContext hr)
    {
	extract = new StringBuffer();
	count = 0;
	return true;
    }

    /**
     * Add a name anchor to the H1 tag, so we can go there, 
     * and set the request properties:<code>TOC.[anchor]</code>
     * to the text of the <code>H1</code> tag.
     */

    public void
    tag_h1(RewriteContext hr)
    {
	String propName = "h1_toc_" +  ++count;
	hr.append("<a name=" + propName +"></a>");
	hr.appendToken();

	while (hr.nextToken()) {
	    String tag = hr.getTag();
	    if (hr.isClosingFor("h1")) {
		break;
	    }
	    extract.append(hr.getToken());
	}
	hr.request.props.put("TOC." + propName, extract.toString());
	extract.setLength(0);
    }
}
