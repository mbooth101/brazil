/*
 * SnarfTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2004 Sun Microsystems, Inc.
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
 * Created by suhler on 01/04/04
 * Last modified by suhler on 04/11/30 15:19:43
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:43 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:37:30 (suhler)
 *   version change
 *
 * 1.2 02/07/24-10:48:36 (suhler)
 *   doc updates
 *
 * 1.2 01/04/04-16:57:50 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/SnarfTemplate.java
 *
 * 1.1 01/04/04-16:57:49 (suhler)
 *   date and time created 01/04/04 16:57:49 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;

/**
 * Template class for extracting content out of
 * &lt;snarf property=xxx&gt; ... &lt;/snarf&gt; pairs.
 * <i>xxx</i> is the name of the property to append the <i>snarf</i>ed
 * content to.  Defaults to <code>[prepend].snarf</code>.
 * All snarf'ed content is deleted.
 * <p>
 * Properties:
 * <dl class=props>
 * <dt> <code>prepend</code>
 * <dd> The string to prepend all properties with.  Defaults to
 *	the handler's prefix.
 * <dt> <code>debug</code>
 * <dd> If set, the <i>snarf</i> tags will be replaced by comments.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		2.2
 */

public class SnarfTemplate extends Template {
    String save;	// text before "snarf"
    String property;	// name of the property to store it in
    String prepend;	// What to prepend all properties with
    boolean snarfing;	// to prevent nesting
    boolean debug;

    /**
     * Get the debug flag and reset page.
     */

    public boolean
    init(RewriteContext hr) {
	debug = (hr.request.props.getProperty(hr.prefix + "debug") != null);
	prepend = hr.request.props.getProperty(hr.prefix+"prepend", hr.prefix);
	if (!prepend.endsWith(".") && !prepend.equals("")) {
	    prepend += ".";
	}
	snarfing = false;
	save = "";
	return true;
    }

    /**
     * Mark the current location in the document.
     */

    public void
    tag_snarf(RewriteContext hr) {
    	if (snarfing) {
	    tag_slash_snarf(hr);
	}
	if (debug) {
	    hr.append("<!-- " + hr.getBody() + " -->");
	}
    	save = hr.toString();
    	property = hr.get("property");
    	if (property == null) {
	    property = prepend + "snarf";
	} else {
	   property = prepend + property;
        }
	hr.reset();
	snarfing = true;
    }

    /**
     * Save the content gathered so far.
     */

    public void
    tag_slash_snarf(RewriteContext hr) {
        if (snarfing) {
	    if (debug) {
		hr.append("<!-- " + hr.getBody() + " -->");
	    }
	    String snarf = hr.request.props.getProperty(property,"") +
		hr.toString();
	    hr.request.props.put(property, snarf);
	    hr.reset();
	    hr.append(save);
	    save="";
	    snarfing = false;
	}
    }
}
