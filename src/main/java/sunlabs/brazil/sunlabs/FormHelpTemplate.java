/*
 * FormHelpTemplate.java
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
 * Version:  1.1
 * Created by suhler on 06/08/13
 * Last modified by suhler on 06/08/13 10:17:25
 *
 * Version Histories:
 *
 * 1.2 06/08/13-10:17:26 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/FormHelpTemplate.java
 *
 * 1.1 06/08/13-10:17:25 (suhler)
 *   date and time created 06/08/13 10:17:25 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import sunlabs.brazil.server.Server;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.template.FormTemplate;

/**
 * Template class for adding field help to the FormTemplate.
 * The "help" attribute of the &lt;form&gt; element specifies a javascript 
 * template used to generate an event handler for each form element.
 * The "help" attribute on each form element is passed to the javascript
 * template, allowing a (user supplied) javascript function to present
 * field specific help.  The help text for each feld may be supplied
 * as a server config file.
 * 
 * <p>"help" feature:<br>
 * This template may be used to provide field specific help with a little
 * external javascript "glue". For example, the markup:
 * <pre>
 * &lt;form event=onfocus help="do_help(%)"&gt;
 * ...
 *   &lt;input name=foo ... help="help for this input field"&gt;
 * ...
 * &lt;/form&gt;
 * </pre>
 * will generate:
 * <pre>
 * &lt;form&gt;
 * ...
 *   &lt;input ... onfocus='do_help("help for this input field")'&gt;
 * ...
 * &lt;/form&gt;
 * </pre>
 * This works with the form elements &lt;input&gt; &lt;select&gt;, and &lt;textarea&gt;.
 * The "event" defaults to "onfocus".
 * If <code>foo.help</code> is a defined property then it is used as
 * the help text.
 *
 * @author		Stephen Uhler
 * @version		@(#)FormHelpTemplate.java	1.1
 */
public class FormHelpTemplate extends FormTemplate {
    String helpAction = null; 	// the javascript template
    String helpEvent = null;	// the event (onfocus)

    public boolean init(RewriteContext hr) {
	return super.init(hr);
    }

    /**
     * Look for a "help" attribute, remember its value, then remove it .
     */

    public void tag_form(RewriteContext hr) {
	super.tag_form(hr);
	helpAction = hr.get("help");
	helpEvent = hr.get("event", "onfocus");
	hr.remove("help");
    }

    /**
     * Forget about the "help" action.
     */

    public void tag_slash_form(RewriteContext hr) {
	super.tag_slash_form(hr);
	helpAction=null;
	helpEvent=null;
    }

    /**
     * Look for a "help" attribute, remember its value, then remove it .
     */

    public void tag_input(RewriteContext hr) {
	super.tag_input(hr);
	do_help(hr);
    }

    /**
     * Look for a "help" attribute, remember its value, then remove it .
     */

    public void tag_select(RewriteContext hr) {
	super.tag_select(hr);
	do_help(hr);
    }

    /**
     * Needed to retain parent functionality.
     */

    public void tag_slash_select(RewriteContext hr) {
        super.tag_slash_select(hr);
    }

    /**
     * Needed to retain parent functionality.
     */

    public void tag_option(RewriteContext hr) {
	super.tag_option(hr);
    }

    /**
     * Look for a "help" attribute, remember its value, then remove it .
     */

    public void tag_textarea(RewriteContext hr) {
	do_help(hr);
    }

    /**
     * Look for help text, and put it into an event call.
     * First, see if the attribute "helpname" is specified, and use
     * it to name the property containing the help text.  Otherwise look
     * in the "name".help property, and lastly the "help" attribute.
     */

    protected void do_help(RewriteContext hr) {
	String help = hr.get("help");
	String name = hr.get("name");
	if (help != null) {
	    hr.remove("help");
	}
	String helpName = hr.get("helpname");
	if (helpName != null) {
	    hr.remove("helpname");
	} else if (name != null) {
	    helpName = name + ".help";
	}
	if (helpName != null) {
	    help = hr.request.props.getProperty(helpName, help);
	}
	if (help != null && helpAction != null) {
	    hr.put(helpEvent, format(helpAction, help));
	}
    }

    /**
     * Substitute "arg" into "base" at "%"
     */

    protected String format(String base, String arg) {
	String delim = "\"";
	int index;
	if (arg.indexOf("\"") != -1) {
	     delim="'";
	}
	if ((index = base.indexOf("%")) >= 0) {
	    return base.substring(0,index) + delim + arg + delim +
		base.substring(index+1);
	} else {
	   return base;
	}
    }

    public boolean done(RewriteContext hr) {
	return(super.done(hr));
    }
}
