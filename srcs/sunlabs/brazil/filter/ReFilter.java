/*
 * ReFilter.java
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
 * Version:  2.4
 * Created by suhler on 01/08/27
 * Last modified by suhler on 04/11/30 15:12:11
 *
 * Version Histories:
 *
 * 2.4 04/11/30-15:12:11 (suhler)
 *   fixed sccs version string
 *
 * 2.3 04/11/30-15:11:43 (suhler)
 *   fixed sccs version string
 *
 * 2.2 04/11/30-15:11:26 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:39:05 (suhler)
 *   version change
 *
 * 1.2 01/10/16-16:00:39 (suhler)
 *   doc fix
 *
 * 1.2 70/01/01-00:00:02 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 filter/ReFilter.java
 *
 * 1.1 01/08/27-12:14:52 (suhler)
 *   date and time created 01/08/27 12:14:52 by suhler
 *
 */

package sunlabs.brazil.filter;

import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.regexp.Regexp;
import sunlabs.brazil.util.http.MimeHeaders;

/**
 * Filter to replace text content via a reqular expression substitution.
 * See {@link sunlabs.brazil.util.regexp.Regexp#sub} and
 * {@link sunlabs.brazil.util.regexp.Regexp#subAll}.
 * <p>
 * Note:  The regular expression processing should be consolodated with the RePollHandler, 
 * and the tag_extract... processing.
 * <p>
 * The following server properties are used:
 * <dl class=props>
 * <dt>re	<dd> The regular expression to match the content
 * <dt>sub	<dd> The replacement expression.  If not specified, the matched
 *		content is deleted.
 * <dt>oneOnly	<dd> If set, only replace the first match.
 *		     by default, all matches are replaced.
 * <dt>noCase	<dd> If set, case-insensitive matchins is performed.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version	2.4
 */

public class ReFilter implements Filter {
    String sub;		// the substitution string
    Regexp re;		// the regular expression to match
    boolean oneOnly;	// only replace the first match

    public boolean
    init(Server server, String prefix) {
        sub = server.props.getProperty(prefix + "sub", "");
        oneOnly = (server.props.getProperty(prefix + "oneOnly") != null);

        boolean noCase = (server.props.getProperty(prefix + "noCase") != null);
        String reString = server.props.getProperty(prefix + "re");

        try {
            re = new Regexp(reString, noCase);
	} catch (IllegalArgumentException e) {
	    server.log(Server.LOG_WARNING, prefix, "Invalid regexp");
	    return false;
	}
	return true;
    }

    /**
     * This is the request object before the content was fetched
     */

    public boolean respond(Request request) {
	return false;
    }

    /**
     * Only filter text documents
     */

    public boolean
    shouldFilter(Request request, MimeHeaders headers) {
	String type = headers.get("content-type");
	return (type != null && type.toLowerCase().startsWith("text/"));
    }

    /**
     * If the content matches the regular expression, do the substitution.
     * Otherwise, return the original content un-changed.
     */

    public byte[]
    filter(Request request, MimeHeaders headers, byte[] content) {
        String src = new String(content);
        String result;

        if (oneOnly) {
            result = re.sub(src, sub);
            if (result != null) {
                content = result.getBytes();
	    }
	} else {
	    result = re.subAll(src, sub);
	    if (result != src) {
                content = result.getBytes();
	    }
	        
	}
	return content;
    }
}
