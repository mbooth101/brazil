/*
 * MD5Filter.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2002-2006 Sun Microsystems, Inc.
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
 * Created by suhler on 02/02/07
 * Last modified by suhler on 06/11/13 15:07:20
 *
 * Version Histories:
 *
 * 2.3 06/11/13-15:07:20 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.2 04/11/30-15:19:38 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:39:06 (suhler)
 *   version change
 *
 * 1.2 02/02/07-16:26:32 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 filter/MD5Filter.java
 *
 * 1.1 02/02/07-16:26:31 (suhler)
 *   date and time created 02/02/07 16:26:31 by suhler
 *
 */

package sunlabs.brazil.filter;

import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Base64;
import sunlabs.brazil.util.http.MimeHeaders;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import sunlabs.brazil.util.MatchString;

/**
 * Filter to compute the MD5 checksum of the content, and
 * generate the appropriate "Content-MD5" http header.
 * As md5 checksum generation can be expensive, care should be
 * taken as to which types of content are digested.
 * <p>
 * The following server properties are used:
 * <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URLs that trigger this filter
 * (See {@link sunlabs.brazil.handler.MatchString}).
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		2.3
 */

public class
MD5Filter implements Filter {
    MatchString isMine;            // check for matching urls

    /**
     * Make sure MD5 is available in this VM, or don't start.
     */

    public boolean
    init(Server server, String prefix) {
	isMine = new MatchString(prefix, server.props);
	try {
	    MessageDigest.getInstance("MD5");
	} catch (NoSuchAlgorithmException e) {
	    server.log(Server.LOG_WARNING, prefix,
		    "Can't find MD5 implementation");
	    return false;
        }
	return true;
    }

    /**
     * This is the request object before the content was fetched.
     */

    public boolean respond(Request request) {
	return false;
    }

    /**
     * Only filter url's that match.
     */

    public boolean
    shouldFilter(Request request, MimeHeaders headers) {
	return (isMine.match(request.url));
    }

    /**
     * Compute digest, add to header.
     */

    public byte[]
    filter(Request request, MimeHeaders headers, byte[] content) {
	MessageDigest digest;
	try {
	    digest = MessageDigest.getInstance("MD5");
	    String md5 = Base64.encode(digest.digest(content));
	    request.log(Server.LOG_DIAGNOSTIC, isMine.prefix(),
		"Digest for " + request.url + " " + md5);
	    request.addHeader("Content-MD5", md5);
	} catch (NoSuchAlgorithmException e) {}
	return content;
    }
}
