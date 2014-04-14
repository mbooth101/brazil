/*
 * ModifiedTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.
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
 * Version:  2.1
 * Created by suhler on 00/11/19
 * Last modified by suhler on 02/10/01 16:36:50
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:36:50 (suhler)
 *   version change
 *
 * 1.4 01/05/11-14:50:46 (suhler)
 *   use template.debug()
 *
 * 1.3 00/12/11-13:31:16 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.2 00/11/20-13:25:14 (suhler)
 *   doc fixes
 *
 * 1.2 00/11/19-10:31:38 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/ModifiedTemplate.java
 *
 * 1.1 00/11/19-10:31:37 (suhler)
 *   date and time created 00/11/19 10:31:37 by suhler
 *
 */

package sunlabs.brazil.template;

import sunlabs.brazil.util.http.HttpUtil;
import sunlabs.brazil.server.Server;
import java.io.Serializable;

/**
 * Template class for computing <code>last-modified</code> times
 * for content that is processed through templates.
 * <p>
 * For traditional web content that is stored in a file, it is easy to
 * keep track of the last time the content changed, simply by looking at the
 * modify-time attribute of the file.  Many browsers (and caches) use
 * this information, 
 * obtained from the <code>last-modified</code> http header to determine 
 * whether to use an existing copy of the document, or a cached copy.
 * <p>
 * When the content is dynamically transformed, however, the last modified
 * time is more complex: a combination of the original file modification
 * time combined with the last change made to the transformation 
 * parameters.
 * <p>
 * A new HTML tag,  
 * <code>&lt;modified&gt;</code> is defined, and is intended to be
 * used in conjunction with the BSLTemplate.  When present in a 
 * page, the <code>last-modified</code> time of the transformation
 * is set to the server's current time.  When the content is delivered
 * to the client, the <code>last-modified</code> header is set to the
 * more recent of the origin last-modified time and the transformation
 * modified time.
 * <p>
 * Properties:
 * <dl class=props>
 * <dt> debug
 * <dd> If this configuration parameter is present, <code>modified</code>
 * tag is replaced by a comment.  Otherwise it is removed from the document.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		%V% 07/01/08
 */

public class ModifiedTemplate extends Template implements Serializable {

    long modified = 0;	// last modified time of session transformation

    /**
     * Set the content transformation modifiy time to NOW
     */

    public void
    tag_modified(RewriteContext hr) {
	modified = hr.request.startMillis;
	hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix +
		"setting transform modified time: " + modified);
	hr.killToken();
	debug(hr);
    }

    /**
     * Compute the http last modified value by comparing the origin
     * last-modified value (if any) with the transform value
     */

    public boolean
    done(RewriteContext hr) {
	if (modified == 0) {
	    modified = hr.request.startMillis;
	}
	/*

	 * Find the last-mod time from the origin document.  Look
	 * in the request props first.
	 */

	long originTime;
	String propsTime = hr.request.props.getProperty("lastModified");
	if (propsTime == null) {
	    originTime = HttpUtil.parseTime(hr.request.responseHeaders.get(
			"last-modified"));
	} else {
	    originTime = Long.parseLong(propsTime);
	}

	/*
	 * Muck with the last-modified header, only if we need to
	 */

	if (originTime < modified) {
	    hr.request.responseHeaders.remove("last-modified");
	    hr.request.addHeader("Last-Modified",HttpUtil.formatTime(modified));
	    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix +
		    "Adjusting mod time: " + originTime + " -> " + modified);
	}

    return true;
    }
}
