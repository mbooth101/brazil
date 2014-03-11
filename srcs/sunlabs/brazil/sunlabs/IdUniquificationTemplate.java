/*
 * IdUniquificationTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2006-2007 Sun Microsystems, Inc.
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
 * Version:  1.3
 * Created by suhler on 06/11/13
 * Last modified by suhler on 07/01/08 15:20:28
 *
 * Version Histories:
 *
 * 1.3 07/01/08-15:20:28 (suhler)
 *   doc fixes
 *
 * 1.2 06/11/13-11:37:35 (suhler)
 *   add std header
 *   .
 *
 * 1.2 06/11/13-11:34:17 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/IdUniquificationTemplate.java
 *
 * 1.1 06/11/13-11:34:16 (suhler)
 *   date and time created 06/11/13 11:34:16 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import sunlabs.brazil.server.Server;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.template.FormTemplate;

import java.util.Enumeration;

/**
 * Template to assign div and span id's that are unique for each
 * browser window.  Html Tag id's are supposed to be unique.  However, if there
 * are multiple browser windows, the server often ends of generating the same
 * id for each window that is displaying the same url.  This template fixes this
 * by automatically incorporating a unique identifier into every "id"
 * attribute based on the value of the "subst" request property. The first
 * "%" character in the value of "subst" is replaced by the specified "id" value.
 * <p>
 * This template looks at all "span", "div", and "section" tags and
 * rewrites all id attributes. ("section" is a non-standard tag that may be
 * used by the server for automatic "id" uniquification.)
 * <p>
 * It is up to the developer to make sure there is a request property that
 * is unique for each window that is part us the "subst" value (e.g.
 * <pre>subst=%_${window_id}</pre> to append the window id onto the id).
 * <p>
 * If the boolean attribute "norewrite" is specified, the id will not be rewritten.
 * <p>
 * If the request property "title.<id>" exists, it is added as the title
 * attribute for this section or div, which will make it show-up on the
 * "balloon help"
 * <p>
 * NOTE:<br>This template may be useful primarily in debugging multi browser
 * window AJAC applications, and shouldn't be needed in "production".
 */

public class IdUniquificationTemplate extends Template {
    public boolean init(RewriteContext hr) {
	return super.init(hr);
    }
    public void tag_span(RewriteContext hr) {
	do_id(hr);
    }
    public void tag_div(RewriteContext hr) {
	do_id(hr);
    }
    public void tag_table(RewriteContext hr) {
	do_id(hr);
    }

    /* This is my made-up pseudo tag */

    public void tag_section(RewriteContext hr) {
	do_id(hr);
    }

    /**
     * look for id=xxx tags, and modify them to add a window
     * specific identifier. Do all ${...} substitutions first.
     */

    void do_id(RewriteContext hr) {
	debug(hr);

	String id = hr.get("id"); // the id to rewrite
	String subst = hr.request.props.getProperty(hr.prefix + "subst");
	boolean noRewrite = hr.isTrue("norewrite");
	if (noRewrite) {
	    hr.remove("norewrite");
	} else if (subst != null && id != null) {
	    hr.put("id", format(subst, id));
	}

	// look up a title from the properties file based on the original id
	
	if (!hr.getTag().equals("section")) {
	    String title = hr.request.props.getProperty("title." + id);
	    if (title != null) {
	        hr.put("title", title);
            }
        }
        hr.substAttributeValues();
	hr.appendToken();
    }

    /**
     *  replace % in base with arg.
     */

    String format(String base, String arg) {
	int index;
	if ((index = base.indexOf("%")) >= 0) {
	    return base.substring(0,index) + arg + base.substring(index+1);
	} else {
	    return base;
	}
    }
}
