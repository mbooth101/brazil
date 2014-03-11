/*
 * FormTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1998-2006 Sun Microsystems, Inc.
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
 * Contributor(s): cstevens, rinaldo, suhler.
 *
 * Version:  2.4
 * Created by suhler on 98/10/26
 * Last modified by suhler on 06/11/13 15:05:11
 *
 * Version Histories:
 *
 * 2.4 06/11/13-15:05:11 (suhler)
 *   substitute all unknown attributes
 *
 * 2.3 05/11/28-09:15:16 (suhler)
 *   add <form prepend="xxx"> to allow the value derived from the request
 *   properties to be looked-up with the desired prefix
 *
 * 2.2 02/12/19-11:36:50 (suhler)
 *   run size= through format.subst
 *
 * 2.1 02/10/01-16:36:42 (suhler)
 *   version change
 *
 * 1.23 02/07/24-10:46:09 (suhler)
 *   doc updates
 *
 * 1.22 02/05/01-11:28:41 (suhler)
 *   fix sccs version info
 *
 * 1.21 02/04/25-13:43:56 (suhler)
 *   doc fixes
 *
 * 1.20 01/09/10-14:02:51 (suhler)
 *   bug in value=${..} substitutions
 *
 * 1.19 01/09/03-20:21:30 (suhler)
 *   fixed bug in ${...} processing
 *
 * 1.18 01/08/28-20:55:38 (suhler)
 *   input fields to ${...} substitutions on value= if corrosponding
 *   property isn't set
 *
 * 1.17 01/08/07-11:00:38 (suhler)
 *   do ${...} substitutions in value attribute of <option>
 *
 * 1.16 01/07/16-16:48:49 (suhler)
 *   use new Template convenience methods
 *
 * 1.15 01/06/18-16:08:30 (suhler)
 *   works for checkboxes too
 *
 * 1.14 00/12/11-13:29:42 (suhler)
 *   typo
 *
 * 1.13 00/10/31-10:18:44 (suhler)
 *   doc fixes
 *
 * 1.12 00/05/31-13:48:48 (suhler)
 *   name change
 *
 * 1.11 00/05/22-14:04:40 (suhler)
 *   doc updates
 *
 * 1.10 00/05/19-11:55:21 (suhler)
 *   doc fixes
 *
 * 1.9 00/04/28-09:23:02 (suhler)
 *   fix handling of radio buttons
 *
 * 1.8 00/02/02-09:35:03 (suhler)
 *   claim to be serializable
 *
 * 1.7 99/11/14-10:35:19 (rinaldo)
 *   Fix errors with Mime types
 *
 * 1.6 99/09/29-16:04:33 (cstevens)
 *   New HtmlRewriter object, that allows arbitrary rewriting of the HTML (by
 *   templates and others), instead of forcing the templates to return a string
 *   that contained all of the new HTML content in one big string.
 *
 * 1.5 99/06/30-12:23:07 (suhler)
 *   added SLECTED substitutions into <option> tags.
 *   The "value" parameter must be specified in order for selection to occurr
 *
 * 1.4 99/05/24-10:13:36 (suhler)
 *   - better documentation
 *   - added <tag>, </tag>
 *
 * 1.3 99/03/30-09:33:56 (suhler)
 *   documentation update
 *
 * 1.2 98/10/26-11:09:36 (suhler)
 *   Fixed diagnostic output
 *
 * 1.2 98/10/26-09:53:53 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 handlers/templates/FormTemplate.java
 *   Name history : 1 0 templates/FormTemplate.java
 *
 * 1.1 98/10/26-09:53:52 (suhler)
 *   date and time created 98/10/26 09:53:52 by suhler
 *
 */

package sunlabs.brazil.template;

import sunlabs.brazil.server.Server;
import java.util.Enumeration;

/**
 * Template class for substituting default values into html forms.
 * This class is used by the TemplateHandler.
 * 
 * The default values in form elements are replaced by the request property that
 * matches the field name. The following field elements are processed:
 * <ul>
 * <li> &lt;input name=<i>x</i> value=<i>y</i>&gt;
 * <li> &lt;input type=input name=<i>x</i> value=<i>y</i>&gt;
 * <li> &lt;input type=radio name=<i>x</i> value=<i>y</i>&gt;
 * <li> &lt;option value=<i>x</i>&gt;
 * </ul>
 * In all cases, the <code>value</code> attribute must be present.
 * additional information is provided below.
 * <p>
 * If the enclosing &lt;form&gt; tag has the attribute "prepend", then 
 * "prepend" is tacked on the front of each variable name before its
 * value is looked-up.  The "prepend" attribute is then removed from the
 * form tag.
 * 
 * @author		Stephen Uhler
 * @version		@(#)FormTemplate.java	2.4
 */
public class FormTemplate extends Template {
    String selectName;      // The name of the current select variable
    String selectValue;     // The name of the current select variable value
    String prepend = "";    // What to prepend the values with
    int total;              // total for elements examined
    int changed;            // elements whose initial values have been changed

    /**
     * Save a reference to our request properties.
     */

    public boolean init(RewriteContext hr) {
        selectName = null;
        total = 0;
        changed = 0;
        return true;
    }

    /**
     * Look for a "prepend" attrubute, remember its value, then remove it 
     * from the tag.
     */

    public void tag_form(RewriteContext hr) {
	String p = hr.get("prepend");
	if (p != null) {
	    hr.remove("prepend");
	    if (p.endsWith(".")) {
		prepend = p;
	    } else {
		prepend = p + ".";
	    }
	} else {
	    prepend = "";
	}
	hr.substAttributeValues();
    }

    /**
     * Forget about the "prepend" value
     */

    public void tag_slash_form(RewriteContext hr) {
	prepend = "";
    }

    /**
     * Look for &lt;input name=[x] value=[v]&gt; and replace the
     * value with the entry in the request properties. If no value is supplied,
     * no substitution is done.
     * If <code>value</code> contains any ${..} constructs, the substituted
     * value is used instead of the value in the corrosponding request property.
     */
    public void tag_input(RewriteContext hr) {
        total++;

	String origValue = hr.get("value");
	if (origValue != null) {
	    hr.put("value", origValue);
	}

        String name = hr.get("name");
        if (name == null) {
            return;
        } else {
	    hr.put("name", name);
	}

        String value = hr.request.props.getProperty(prepend + name);
        if (value == null) {
            return;
        }

        changed++;
        String type = hr.get("type", false);
	if ("radio".equals(type) || "checkbox".equals(type)) {
	    log(hr,type + " " + value + " ? " + hr.get("value", false));
	    if (value.equals(origValue)) {
		hr.put("checked", "");
	    } else {
		hr.remove("checked");
	    }
	} else {
	    hr.put("value", value);
	    String size = hr.get("size");
            if (size != null) {
	       hr.put("size",size);
	    }
	}
    }

    /**
     * Remember the variable name for the next group of option tags.
     */
    public void tag_select(RewriteContext hr) {
        selectName = hr.get("name");

        if (selectName != null) {
            selectValue = hr.request.props.getProperty(prepend + selectName);
	    hr.put("name", selectName);

            log(hr, 
                "For selection (" + selectName + ") have value :" 
                + selectValue);
        }
    }

    /**
     * Forget the variable name for the next group of option tags
     */
    public void tag_slash_select(RewriteContext hr) {
        selectName = null;
    }

    /**
     * Look at the option tag, set the "selected" attribute as needed.
     * In order for this to work, the VALUE tag *must* be used
     * Do ${...} substitutions on the value.
     */
    public void tag_option(RewriteContext hr) {
        String value = hr.get("value");

        if (value != null) {
	    hr.put("value", value);
        }

        if (selectName == null || selectValue == null || value == null) {
            return;
        }
        if (value.equals(selectValue)) {
            hr.put("selected", "");
        } else {
            hr.remove("selected");
        }
    }

    /**
     * This is for debugging only !!
     */

    public boolean done(RewriteContext hr) {
        log(hr, hr.request.url + " (form template) " + changed + "/" + total + " changed");

        return true;
    }

    /**
     * simple interface to server logging
     */

    private void log(RewriteContext hr, String msg) {
        hr.request.log(Server.LOG_DIAGNOSTIC, 
                       hr.prefix + "formTemplate: " + msg);
    }
}
