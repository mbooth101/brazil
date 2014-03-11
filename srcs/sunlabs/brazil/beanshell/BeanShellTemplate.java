/*
 * BeanShellTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2002-2004 Sun Microsystems, Inc.
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
 * Version:  1.4
 * Created by suhler on 02/11/14
 * Last modified by suhler on 04/11/30 15:19:38
 *
 * Version Histories:
 *
 * 1.4 04/11/30-15:19:38 (suhler)
 *   fixed sccs version string
 *
 * 1.3 04/04/07-10:23:54 (suhler)
 *   doc fixes
 *
 * 1.2 03/07/15-09:13:23 (suhler)
 *   doc fixes for pdf version of the manual
 *
 * 1.2 02/11/14-14:49:36 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 beanshell/BeanShellTemplate.java
 *
 * 1.1 02/11/14-14:49:35 (suhler)
 *   date and time created 02/11/14 14:49:35 by suhler
 *
 */

package sunlabs.brazil.beanshell;

import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.server.Server;
import bsh.EvalError;
import bsh.Interpreter;
import java.util.Properties;

/**
 * Template to define new templates in beanshell, and use them later on in
 * the current session (or page).
 * This adds to the capability of the template in the following way:
 * <br>The variable "tagMap", a Properties object, may be used to associate
 *      bsh code with a tag.
 * <pre>
 *      tagMap.put("mytag","do_mytag();");
 * </pre>
 * will cause the method <code>do_mytag()</code> to be called in response to:
 * &lt;mytag...&gt;.  The variable "rewriteContext" will be available
 * to access the attributes of the tag.
 *
 * @author      Stephen Uhler
 * @version		1.4
 */

public class BeanShellTemplate extends BeanShellServerTemplate {

    public Properties tagMap;	// where to keep tag to code mappings

    /**
     * When we set up the interp, make th "tagMap" visible
     */

    protected boolean
    setup(RewriteContext hr) {
	boolean first = (bsh == null);
	boolean ok = super.setup(hr);
	if (ok && first) {
	   tagMap = new Properties();
	   try {
	       bsh.set("tagMap", tagMap);
	   } catch (EvalError e) {
		hr.request.log(Server.LOG_ERROR, "initializing BeanShell",
			    e.toString());
		return false;
	   }
	}
	return ok;
    }

    /**
     * if a mapping exists for this tag, run the code
     */

    public void
    defaultTag(RewriteContext hr) {
        if (!setup(hr)) {
	    debug(hr, "Interpreter didn't initialize");
	    return;
        }
	System.out.println("Map: " + tagMap);
        String tag = hr.getTag();
        String script = tagMap.getProperty(tag);
	if (script != null) {
	    try {
	        bsh.set("rewriteContext", hr);
	        bsh.eval(script);
	    } catch (EvalError e) {
	        hr.append("\n<!-- server-side BeanShell: error code " +
		        e.toString() + " -->\n");
	        hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, e.toString());
	    }
	}
    }
}
