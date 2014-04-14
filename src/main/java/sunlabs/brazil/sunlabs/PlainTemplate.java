/*
 * PlainTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2003-2005 Sun Microsystems, Inc.
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
 * Created by suhler on 03/05/12
 * Last modified by suhler on 05/12/22 16:17:44
 *
 * Version Histories:
 *
 * 1.3 05/12/22-16:17:44 (suhler)
 *   doc tweaks
 *
 * 1.2 03/07/07-14:08:39 (suhler)
 *   use addClosingTag() convenience method
 *
 * 1.2 03/05/12-16:35:20 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/PlainTemplate.java
 *
 * 1.1 03/05/12-16:35:19 (suhler)
 *   date and time created 03/05/12 16:35:19 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import sunlabs.brazil.util.http.HttpUtil;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;

/**
 * Template to turn all markup between &lt;plain&gt; and &lt;/plain&gt; into
 * ordinary text, by escaping all HTML markup.
 */

public class PlainTemplate extends Template  {

    public boolean
    init(RewriteContext hr) {
	hr.addClosingTag("plain");
	return true;
    }

    public void
    tag_plain(RewriteContext hr) {
	hr.accumulate(false);
	hr.nextToken();
	String plain = hr.getBody();
	hr.accumulate(true);
	hr.nextToken(); // eat the /plain
	hr.append(HttpUtil.htmlEncode(plain)); 
    }
}
