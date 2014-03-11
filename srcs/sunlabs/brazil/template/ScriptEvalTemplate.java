/*
 * ScriptEvalTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2006 Sun Microsystems, Inc.
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
 * Version:  2.3
 * Created by suhler on 01/07/17
 * Last modified by suhler on 06/11/13 12:02:24
 *
 * Version Histories:
 *
 * 2.3 06/11/13-12:02:24 (suhler)
 *   do ${...} processing on all attributes
 *
 * 2.2 04/05/28-10:34:01 (suhler)
 *   do substitutions for <script> and <style>, and allow \n substutution
 *   to be selectable
 *
 * 2.1 02/10/01-16:36:52 (suhler)
 *   version change
 *
 * 1.2 01/07/18-10:39:14 (suhler)
 *   protect \'s
 *
 * 1.2 01/07/17-18:20:47 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/ScriptEvalTemplate.java
 *
 * 1.1 01/07/17-18:20:46 (suhler)
 *   date and time created 01/07/17 18:20:46 by suhler
 *
 */

package sunlabs.brazil.template;

import sunlabs.brazil.util.Format;

/**
 * Template class for performing ${...} substitutions inside
 * javascript and style tags.
 * This class is used by the TemplateHandler
 * <p>
 * A new attribute <code>eval</code> is defined for the <code>script</code>
 * and <code>style</code> tags.
 * If <code>eval</code> is present, any ${...} constructs are evaluated in the
 * body of the "script" or "style".
 * <p>
 * If the attribute <code>esc</code> is true, then strings of the form
 * "\X" are replaced as per {@link sunlabs.brazil.util.Format}.  Otherwise
 * "\X" is treated specially only for X = $, to escape variable
 * substitution.
 * <p>
 * Both "eval" and "esc" attributes are removed from the "script" or "style"
 * tags.
 *
 * @author		Stephen Uhler
 * @version		@(#)ScriptEvalTemplate.java	2.3
 */

public class ScriptEvalTemplate extends Template {

    public void
    tag_script(RewriteContext hr) {
	if (hr.isTrue("eval")) {
	    boolean noesc = !hr.isTrue("esc");
	    hr.remove("eval");
	    hr.remove("esc");
	    hr.substAttributeValues();
	    hr.nextToken();
	    hr.append(Format.subst(hr.request.props, hr.getBody(), noesc));
	} else {
	    hr.substAttributeValues();
	}
    }

    public void
    tag_style(RewriteContext hr) {
	tag_script(hr);
    }
}
