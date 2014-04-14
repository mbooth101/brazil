/*
 * TitleTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2006 Sun Microsystems, Inc.
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
 * Last modified by suhler on 06/11/20 17:22:22
 *
 * Version Histories:
 *
 * 1.3 06/11/20-17:22:22 (suhler)
 *   Now uses session manager to store title properties
 *
 * 1.2 06/11/17-15:47:47 (suhler)
 *   fetch title.XXX entries from a separate config file
 *
 * 1.2 06/11/13-15:23:17 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/TitleTemplate.java
 *
 * 1.1 06/11/13-15:23:16 (suhler)
 *   date and time created 06/11/13 15:23:16 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.handler.ResourceHandler;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.server.Server;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Template to look up "title" attributes in a database, and rewrite them.
 * Looks for "title.<content>" in the properties, where "content" is
 * the current value of the title.  Firefox displays "balloon help" when
 * it sees a "title" attribute.  This allows us to put the titles in a table,
 * and substitute them into the document at run time.
 * <p>
 * <dl class=props>
 * <dt>config
 * <dd>This specifies the name of a properties file that contains the
 * help table.  This is consulted if the help test is not present
 * in request.props. The file is interpreted relative to the document
 * root, and may exist as a resource.
 * </dl>
 * <p>
 * Any ${..} substitutions are done when the text is mapped.
 * Make sure no "'s or '>'s
 * occur in the text.
 * <p>ToDo:
 * If the title isn't there, we could look for "id" and/or "name", but we don't.
 */

public class TitleTemplate extends Template {

    Properties props = null;

    public boolean
    init(RewriteContext hr) {
	setup(hr);
	return super.init(hr);
    }

    /**
     * Read in a special properties file of titles.  These are used
     * if no title is found in the request properties.
     */

    void
    setup(RewriteContext hr) {
	if (props != null) {
	    return;
	}
	String config = hr.request.props.getProperty(hr.prefix + "config");
	if (config == null) {
	    return;
	}
	props = (Properties) SessionManager.getSession(hr.prefix, "TITLE",
		Properties.class);
	if (props.isEmpty()) {
	    try {
		InputStream in = ResourceHandler.getResourceStream(
			hr.request.props, hr.prefix, config);
		props.load(in);
		in.close();
	    } catch (IOException e) {
		hr.request.log(Server.LOG_WARNING, hr.prefix, 
			"error reading title mapping file: " + e);
		props.put("error", e.getMessage());	// sentinal
	    }
	}
    }

    public void
    defaultTag(RewriteContext hr) {
	String title = hr.get("title");
	if (title != null) {
	    String replace = hr.request.props.getProperty("title." + title);
	    if (replace == null && props != null) {
		replace=props.getProperty("title." + title);
	    }
	    if (replace != null) {
		debug(hr,"Title: (" + title + ") => (" + replace + ")");
	        hr.put("title", Format.subst(hr.request.props, replace));
	    }
	}
    }
}
