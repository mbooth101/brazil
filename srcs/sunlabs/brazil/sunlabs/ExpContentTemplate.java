/*
 * ExpContentTemplate.java
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
 * Created by suhler on 01/05/25
 * Last modified by suhler on 04/11/30 15:19:43
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:43 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:37:31 (suhler)
 *   version change
 *
 * 1.4 02/07/24-10:48:46 (suhler)
 *   doc updates
 *
 * 1.3 02/06/13-17:56:35 (suhler)
 *   init wasn't calling super.init()
 *
 * 1.2 02/05/16-11:16:17 (suhler)
 *   doc lint
 *
 * 1.2 01/05/25-13:04:35 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/ExpContentTemplate.java
 *
 * 1.1 01/05/25-13:04:34 (suhler)
 *   date and time created 01/05/25 13:04:34 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import java.util.Properties;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.template.ContentTemplate;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.util.regexp.Regexp;

/**
 * Allow extracted content to be filtered through regular expressions.
 * Many sites use comment conventions to demarcate the headers and 
 * footer information in their pages.  This template allows
 * regular expressions to be used to process the "content" value
 * extracted by the <code>ContentTemplate</code>.
 * <p>
 * Request properties:
 * <dl class=props>
 * <dt>extract	<dd>A regular expression to match the extracted content
 * <dt>replace	<dd>A regular expression substitution string used to
 *		replace the content, if the expression matched.
 * <dt>urlPrefix<dd>A prefix the url must match to be considered
 *		for rewriting
 * </dl>
 * @author		Stephen Uhler
 * @version		2.2
 */

public class ExpContentTemplate extends ContentTemplate {

    boolean extract;
    boolean init=true;
    Regexp re;
    String rpl = null;
    String urlPrefix = "/";

    public boolean
    init(RewriteContext hr) {
	if (init) {
	    Properties props = hr.request.props;
	    String exp = props.getProperty(hr.prefix + "extract");
	    rpl = props.getProperty(hr.prefix + "replace");
	    urlPrefix = props.getProperty(hr.prefix + "urlPrefix", "/");
	    extract = (exp!=null  && rpl!=null );
	    init = false;
	    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, 
			"extracting content from: " + urlPrefix +
			" with: " + exp);
	    re = new Regexp(exp);
	}
	return super.init(hr);
    }

    /**
     * Run the content through a regexp to do further extraction.
     * If the regexp didn't match, leave the existing content alone.
     */

    public boolean
    done(RewriteContext hr) {
        super.done(hr);
        if (extract && hr.request.url.startsWith(urlPrefix)) {
	    String result =
		    re.sub(hr.request.props.getProperty("content"), rpl);
	    if (result != null) {
	        hr.request.props.put("content", result);
		hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, 
			"Replacing content via rexp");
	    }
	}
        return true;
    }
}
