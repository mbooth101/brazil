/*
 * RemoteHostTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2004 Sun Microsystems, Inc.
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
 * Version:  1.2
 * Created by suhler on 04/03/26
 * Last modified by suhler on 04/11/30 15:19:43
 *
 * Version Histories:
 *
 * 1.2 04/11/30-15:19:43 (suhler)
 *   fixed sccs version string
 *
 * 1.2 04/03/26-10:47:24 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/RemoteHostTemplate.java
 *
 * 1.1 04/03/26-10:47:23 (suhler)
 *   date and time created 04/03/26 10:47:23 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import java.net.InetAddress;

/**
 * Return the remote host name associated with this request.
 * Note: This involves a reverse dns lookup, which can be expensive.
 * <dl class=attrib>
 * <dt>name<dd>where to put the result, defaults to [prefix].host
 * </dl>
 */

public class RemoteHostTemplate extends Template {
    public void
    tag_remotehost(RewriteContext hr) {
	debug(hr);
	String name = hr.get("name", hr.prefix + "host");
	InetAddress inet = hr.request.getSocket().getInetAddress();
	String host = inet.getHostName();
	if (host != null) {
	    debug(hr, name + "=" + host);
	    hr.request.props.put(name, host);
	} else {
	    debug(hr, "host unknown");
	}
    }
}
