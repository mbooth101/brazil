/*
 * TemplateTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.
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
 * Created by suhler on 01/07/16
 * Last modified by suhler on 02/10/01 16:36:52
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:36:52 (suhler)
 *   version change
 *
 * 1.3 01/09/13-09:24:13 (suhler)
 *   remove uneeded import
 *
 * 1.2 01/08/14-16:39:34 (suhler)
 *   doc lint
 *
 * 1.2 01/07/16-16:54:55 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/TemplateTemplate.java
 *
 * 1.1 01/07/16-16:54:54 (suhler)
 *   date and time created 01/07/16 16:54:54 by suhler
 *
 */

package sunlabs.brazil.template;

import java.util.Hashtable;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.template.TemplateRunner;

/**
 * Template class for processing markup through a sequence of template filters.
 * This class processes:
 * <pre>
 * &lt;template data=... [token=...] [prefix=...] [ignore=true|false]&gt;
 * </pre>
 * <dl class=attributes>
 * <dt>data
 * <dd>contains the markup to process.
 * <dt>token
 * <dd>names the request property that
 * contains the
 * class names or tokens that will define the templates that will process
 * the data (see the {@link sunlabs.brazil.filter.TemplateFilter} discussion
 * regarding the <i>templates</i> property).  It defaults to "templates" with
 * this class's prefix.
 * <dt>prefix
 * <dd>Which prefix to assign the templates, if the "token" list contains
 * class names.   Defaults to our prefix.
 * <dt>ignore
 * <dd>If true, the result of the template processing is ignored.
 * by default it is inserted into the resultant markup.
 * </dl>
 * <p>
 * The templates for processing each token are setup only once, 
 * the first time the token is referenced.
 * 
 * @author		Stephen Uhler
 * @version		@(#)TemplateTemplate.java	2.1
 */

public class TemplateTemplate extends Template {
	
    Hashtable runners = new Hashtable(5);

    public void
    tag_template(RewriteContext hr) {
	String prefix =  hr.get("prefix", hr.prefix);
	String data =  hr.get("data");
	String token =  hr.get("token");
	boolean ignore = hr.isTrue("ignore");

	if (token == null) {
	    token=hr.prefix + "templates";
	}

        debug(hr);
	hr.killToken();

	if (data==null || data.length() == 0) {
	    debug(hr, "missing data");
	    return;
	}

	String templates = hr.request.props.getProperty(token);
	if (templates == null) {
	    debug(hr, "token: " + token + " is not defined");
	    return;
	}

	TemplateRunner runner = (TemplateRunner) runners.get(token);
	if (runner == null) {
	    try {
		runner = new TemplateRunner(hr.server, prefix, templates);
	        runners.put(token, runner);
		hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
			"creating runner for: " + templates);
	    } catch (Exception e) {
		debug(hr, "Can't instantiate " + e.getMessage());
		return;
	    }
	}
	String result = runner.process(hr.request, data, hr.sessionId);
	if (!ignore) {
	    hr.append(result);
	}
    }
}
