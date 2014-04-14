/*
 * ExecFilter.java
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
 * Created by suhler on 01/11/16
 * Last modified by suhler on 04/11/30 15:19:38
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:38 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:39:06 (suhler)
 *   version change
 *
 * 1.3 01/12/10-13:55:35 (suhler)
 *   addrd "newType" property to convert mime types
 *
 * 1.2 01/11/16-13:48:32 (suhler)
 *   add example
 *
 * 1.2 01/11/16-13:43:38 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 filter/ExecFilter.java
 *
 * 1.1 01/11/16-13:43:37 (suhler)
 *   date and time created 01/11/16 13:43:37 by suhler
 *
 */

package sunlabs.brazil.filter;

import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.http.MimeHeaders;
import sunlabs.brazil.util.http.HttpUtil;
import sunlabs.brazil.util.http.HttpInputStream;
import sunlabs.brazil.util.regexp.Regexp;
import sunlabs.brazil.util.Format;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Filter to Run all content through an external process filter.
 * The content is provided as the standard input to a command, that is
 * expected to return the new content.
 * <p>
 * The following server properties are used:
 * <dl class=props>
 * <dt>command
 * <dd>The command to exec.  The content is supplied as stdin, and
 * the filtered output is expected on stdout.  ${...} substitutions
 * Are done at each filter invocation.
 * <dt>types
 * <dd>A regular expression that matches the content types for the
 * content we wish to filter
 * <dt>type
 * <dd>This property is set to the content type of the content just
 * before <i>command</i> is evaluated.
 * <dt>newType
 * <dd>This property, if set, is used as the new content type.
 * It is evaluated for ${...} at each conversion.
 * <dt>error
 * <dd>If the command failed, this property will contain the error
 * message.  If the command generated output on stderr, this property
 * will contain the output.
 * </dl>
 * The following configuration will halve the size of all jpeg and
 * gif images, using the <i>convert</i> program from ImageMagic.
 * <pre>
 * prefix.types=image/(jpeg|gif)
 * prefix.command= convert -sample 50% \ 
 *     ${map.${prefix.type}}:- ${map.${prefix.type}}:-
 * map.image/gif=GIF
 * map.image/jpeg=JPEG
 * </pre>
 *
 * @author		Stephen Uhler
 * @version		2.2
 */

public class ExecFilter implements Filter {
    String command;	// image manipulation command to run
    Regexp types;	// the regular expression to the types
    String prefix;	// our properties prefix

    public boolean
    init(Server server, String prefix) {
	this.prefix = prefix;
	command = server.props.getProperty(prefix + "command");
        String reString = server.props.getProperty(prefix + "types");
        try {
            types = new Regexp(reString, true);
	} catch (IllegalArgumentException e) {
	    server.log(Server.LOG_WARNING, prefix, "Invalid regexp");
	    return false;
	}
	return (command != null);
    }

    /**
     * This is the request object before the content was fetched
     */

    public boolean respond(Request request) {
	return false;
    }

    /**
     * Only filter content types that match
     */

    public boolean
    shouldFilter(Request request, MimeHeaders headers) {
	String type = headers.get("content-type");
	boolean ok = (type != null && types.match(type) != null);
	request.log(Server.LOG_DIAGNOSTIC, prefix + 
		"Will filter: " + type + " " + ok);
	return ok;
    }

    /**
     * Run content through filter.  Process ${...}
     */

    public byte[]
    filter(Request request, MimeHeaders headers, byte[] content) {
	String type = headers.get("content-type");
	request.props.put(prefix + "type", type);
	String formatted = Format.subst(request.props, command);
	request.log(Server.LOG_DIAGNOSTIC, prefix + 
	    "Running:  [" + formatted + "] (" + type + ")");
	try {
	    Process process = Runtime.getRuntime().exec(formatted);

	    OutputStream out = process.getOutputStream();
	    out.write(content);
	    out.close();

	    HttpInputStream in = new HttpInputStream(process.getInputStream());
	    HttpInputStream err = new HttpInputStream(process.getErrorStream());

	    ByteArrayOutputStream inB = new ByteArrayOutputStream();

	    in.copyTo(inB);
	    if (inB.size() > 0) {
	        content=inB.toByteArray();
	        String newType = request.props.getProperty(prefix + "newType");
		if (newType != null) {
		    newType = Format.subst(request.props, newType);
		    request.log(Server.LOG_DIAGNOSTIC, prefix + 
			    "Changing content type to: " + newType);
		    headers.put("content-type", newType);
		}
	    } else {
		request.log(Server.LOG_DIAGNOSTIC, prefix + 
			"No content from filter");
	    }
	    in.close();
	    inB.close();

	    ByteArrayOutputStream errB = new ByteArrayOutputStream();
	    err.copyTo(errB);
	    if (errB.size() > 0) {
		request.log(Server.LOG_DIAGNOSTIC, prefix + 
		   "Error result: " + errB);
		request.props.put(prefix + "error", errB.toString());
	    }
	    err.close();
	    errB.close();

	    process.waitFor();
	} catch (Exception e) {
	    request.log(Server.LOG_DIAGNOSTIC, prefix + 
	       "Error result: " + e.getMessage());
	    request.props.put(prefix + "error", e.getMessage());
	}
	return content;
    }
}
