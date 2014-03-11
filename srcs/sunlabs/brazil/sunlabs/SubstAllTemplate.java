/*
 * SubstAllTemplate.java
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
 * Version:  2.3
 * Created by suhler on 01/05/07
 * Last modified by suhler on 04/11/30 15:19:43
 *
 * Version Histories:
 *
 * 2.3 04/11/30-15:19:43 (suhler)
 *   fixed sccs version string
 *
 * 2.2 03/07/15-09:12:13 (suhler)
 *   doc fixes for pdf version of the manual
 *
 * 2.1 02/10/01-16:37:30 (suhler)
 *   version change
 *
 * 1.4 01/07/16-16:52:11 (suhler)
 *   use new Template convenience methods
 *
 * 1.3 01/05/21-16:44:50 (suhler)
 *   docs
 *
 * 1.2 01/05/15-10:08:38 (suhler)
 *   added docs
 *
 * 1.2 01/05/07-12:01:14 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/SubstAllTemplate.java
 *
 * 1.1 01/05/07-12:01:13 (suhler)
 *   date and time created 01/05/07 12:01:13 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import java.util.Enumeration;

/**
 * Template to substitute ${...} for the value of name/value attribute
 * pairs of all html tags that aren't otherwise accounted for by other
 * templates.  This enables the elimination of <code>
 * &lt;tag&gt;...&lt;/tag&gt;
 * </code> constructs in most cases.
 * @see sunlabs.brazil.template.MacroTemplate
 *
 * @author      Stephen Uhler
 * @version		2.3
 */

public class SubstAllTemplate extends Template {
    public void
    defaultTag(RewriteContext hr) {
        Enumeration e = hr.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            hr.put(key, hr.get(key));
	}
    }
}
