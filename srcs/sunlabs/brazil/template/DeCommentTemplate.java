/*
 * DeCommentTemplate.java
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
 * Created by suhler on 01/08/28
 * Last modified by suhler on 04/04/05 14:49:51
 *
 * Version Histories:
 *
 * 2.2 04/04/05-14:49:51 (suhler)
 *   fix enable/disable detection
 *
 * 2.1 02/10/01-16:36:53 (suhler)
 *   version change
 *
 * 1.4 02/07/24-10:46:34 (suhler)
 *   doc updates
 *
 * 1.3 02/04/25-13:44:10 (suhler)
 *   doc fixes
 *
 * 1.2 01/09/23-17:36:14 (suhler)
 *   fixed type
 *
 * 1.2 70/01/01-00:00:02 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/DeCommentTemplate.java
 *
 * 1.1 01/08/28-20:57:45 (suhler)
 *   date and time created 01/08/28 20:57:45 by suhler
 *
 */

package sunlabs.brazil.template;

/**
 * Template class for removing comments from html pages.
 * Properties:
 * <dl class=props>
 * <dt>disable	<dd>if true, disable comment removal.  This is checked at
 *     each request.
 * </dl>
 *
 * @author Stephen Uhler
 * @version @(#)DeCommentTemplate.java	2.2
 */

public class DeCommentTemplate extends Template {
    boolean disable;

    public boolean
    init(RewriteContext hr) {
	disable = hr.isTrue("disable");
	return true;
    }

    public void comment(RewriteContext hr) {
        if (!disable) {
	    hr.killToken();
	}
    }
}
