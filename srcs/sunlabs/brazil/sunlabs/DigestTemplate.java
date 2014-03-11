/*
 * DigestTemplate.java
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
 * Version:  2.2
 * Created by suhler on 02/03/15
 * Last modified by suhler on 04/11/30 15:19:42
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:42 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:37:32 (suhler)
 *   version change
 *
 * 1.4 02/05/16-11:16:30 (suhler)
 *   doc lint
 *
 * 1.3 02/04/24-11:13:54 (suhler)
 *   editting typo
 *
 * 1.2 02/04/18-11:24:06 (suhler)
 *   lint
 *
 * 1.2 02/03/15-15:49:34 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/DigestTemplate.java
 *
 * 1.1 02/03/15-15:49:33 (suhler)
 *   date and time created 02/03/15 15:49:33 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.util.Base64;

/**
 * Compute the Base64 encoded SHA1 digest of a value
 * (so I don't have to store plain text
 * passwords).  This should probably be added to the Calculator, but
 * this is easier.
 * <br><code>
 * &lt;digest name=nnn value=vvv&gt;
 * </code>
 *
 * @author      Stephen Uhler
 * @version		2.2
 */

public class DigestTemplate extends Template {
    MessageDigest digest = null;

    public DigestTemplate() {
	try {
	   digest = MessageDigest.getInstance("SHA");
	} catch (NoSuchAlgorithmException e) {}
    }

    public void
    tag_digest(RewriteContext hr) {
	String name = hr.get("name");
	String value = hr.get("value");

	debug(hr);
	hr.killToken();
	if (digest != null  && name!=null && value!= null) {
	   hr.request.props.put(name, Base64.encode(digest.digest(
		       value.getBytes())));
	} else {
	   debug(hr,"Invalid parameters or no digest available");
	}
    }
}
