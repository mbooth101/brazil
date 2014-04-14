/*
 * JunkBusterHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2006 Sun Microsystems, Inc.
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
 * The Initial Developer of the Original Code is: cstevens.
 * Portions created by cstevens are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): cstevens, suhler.
 *
 * Version:  2.2
 * Created by cstevens on 00/04/03
 * Last modified by suhler on 06/04/25 14:16:57
 *
 * Version Histories:
 *
 * 2.2 06/04/25-14:16:57 (suhler)
 *   use consolidated mime type handling in FileHandler
 *
 * 2.1 02/10/01-16:37:12 (suhler)
 *   version change
 *
 * 1.6 00/12/11-13:31:40 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.5 00/05/31-13:50:50 (suhler)
 *   docs
 *
 * 1.4 00/05/24-11:27:38 (suhler)
 *   add docs, change '@' processing
 *
 * 1.3 00/05/22-14:06:50 (suhler)
 *   doc updates
 *
 * 1.2 00/04/12-15:58:42 (cstevens)
 *   Get bytes for replacement image from resource file.
 *
 * 1.2 00/04/03-16:58:05 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 proxy/JunkBusterHandler.java
 *
 * 1.1 00/04/03-16:58:04 (cstevens)
 *   date and time created 00/04/03 16:58:04 by cstevens
 *
 */

package sunlabs.brazil.proxy;

import sunlabs.brazil.handler.ResourceHandler;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.http.HttpInputStream;
import sunlabs.brazil.util.regexp.Regexp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Remove <i>junk</i> images from web pages.
 * This approach is to take all requests for images that look like ads and
 * instead return a dummy bitmap.
 * <p>
 * Other approaches to removing ads are to filter the HTML returned and
 * (1) remove the ads altogether or (2) change the href in the ads to point
 * to a different bitmap.  The advantage of option (2) is that all ads can be
 * changed to point to the same bitmap, increasing the caching performance
 * of the browser.
 * <p>
 * Properties:
 * <dl class=props>
 * <dt>image	<dd>The file to contain the replacement image.
 * <dt>host	<dd>The regular expression  matching url's to reject.
 *		 If the	expression starts with a '@', it interpreted as a
 *		 file name (minus the @) that contains a new-line separated
 * 		 list of regular exporessions. See
 *		 {@link sunlabs.brazil.util.regexp.Regexp} for more information on
 *		 regular expressions.
 * </dl>
 */
public class JunkBusterHandler
    implements Handler
{
    private static final String IMAGE = "image";
    private static final String HOSTS = "hosts";

    byte[] image;
    String type;

    String prefix;

    String urlPattern;
    Regexp hostPattern;


    public boolean
    init(Server server, String prefix)
    {
	this.prefix = prefix;

	Properties props = server.props;
	
	String name = props.getProperty(prefix + IMAGE, "");
	type = FileHandler.getMimeType(name, props, prefix);
	if (type == null) {
	    server.log(Server.LOG_DIAGNOSTIC, prefix,
		    "unknown image file MIME type for: " + name);
	    return false;
	}
	try {
	    image = ResourceHandler.getResourceBytes(props, prefix, name);
	} catch (Exception e) {
	    server.log(Server.LOG_DIAGNOSTIC, prefix, "error reading image file");
	    return false;
	}

	try {
	    String hosts = props.getProperty(prefix + HOSTS);
	    if (hosts.charAt(0) == '@') {
		hostPattern = loadUrls(props, prefix, hosts.substring(1));
	    } else {
		hostPattern = new Regexp(hosts);
	    }
	} catch (Exception e) {
	    server.log(Server.LOG_DIAGNOSTIC, prefix,
		    "error in hosts: " + e.getMessage());
	}
	return true;
    }

    public boolean
    respond(Request request)
	throws IOException
    {
	if ((hostPattern != null) &&
		hostPattern.match(request.url, (int[]) null)) {
	    request.log(Server.LOG_DIAGNOSTIC, prefix, request.url);
	    return sendReplacementImage(request);
	}
	return false;
    }

    public Regexp
    loadUrls(Properties props, String prefix, String file)
    {
	StringBuffer sb = new StringBuffer();
	try {
	    HttpInputStream in = new HttpInputStream(
		    ResourceHandler.getResourceStream(props, prefix, file));

	    String line;
	    while ((line = in.readLine()) != null) {
		line = line.trim();
		if ((line.length() == 0) || (line.charAt(0) == '#')) {
		    continue;
		}
		sb.append(line).append('|');
	    }
	    sb.setLength(sb.length() - 1);
	    return new Regexp(sb.toString());
	} catch (Exception e) {
	    return null;
	}
    }

    public boolean
    sendReplacementImage(Request request)
	throws IOException
    {
	request.sendResponse(image, type);
	return true;
    }
}
