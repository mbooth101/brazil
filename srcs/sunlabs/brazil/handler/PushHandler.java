/*
 * PushHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2006 Sun Microsystems, Inc.
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
 * Contributor(s): cstevens, suhler.
 *
 * Version:  2.2
 * Created by suhler on 99/08/04
 * Last modified by suhler on 06/11/13 15:04:09
 *
 * Version Histories:
 *
 * 2.2 06/11/13-15:04:09 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.1 02/10/01-16:36:25 (suhler)
 *   version change
 *
 * 1.12 02/07/24-10:45:12 (suhler)
 *   doc updates
 *
 * 1.11 01/07/20-11:32:10 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.10 01/07/17-14:15:52 (suhler)
 *   use MatchUrl
 *
 * 1.9 00/12/11-13:28:16 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.8 00/05/31-13:47:07 (suhler)
 *   doc cleanup
 *
 * 1.7 00/05/24-11:25:37 (suhler)
 *   added docs
 *
 * 1.6 00/04/20-11:48:11 (cstevens)
 *   copyright.
 *
 * 1.5 00/03/10-17:02:14 (cstevens)
 *   Removing unused member variables
 *
 * 1.4 99/10/27-13:19:49 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.3 99/10/26-18:52:30 (cstevens)
 *   case
 *
 * 1.2.1.2 99/10/20-14:42:08 (suhler)
 *   make methods public
 *
 * 1.2.1.1 99/10/18-10:25:26 (suhler)
 *   rewrote to use binary data
 *
 * 1.2 99/09/15-14:40:36 (cstevens)
 *   Rewritign http server to make it easier to proxy requests.
 *
 * 1.2 99/08/04-18:43:18 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/PushHandler.java
 *
 * 1.1 99/08/04-18:43:17 (suhler)
 *   date and time created 99/08/04 18:43:17 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.IOException;
import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

/**
 * Skeleton Handler for uploading files using multipart/form-data.
 * Application specific functionality is added by overriding
 * {@link #processData}.
 * <br>NOTE: Most applications will want to use the
 * {@link sunlabs.brazil.template.MultipartSetTemplate} to deal with
 * multipart/form data.
 * <p>
 * Properties:
 * <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URL that triggers this handler.
 * (See {@link MatchString}).
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		2.2, 06/11/13
 */

public class PushHandler implements Handler {
    MatchString isMine;            // check for matching url

    public boolean
    init(Server server, String prefix) {
	isMine = new MatchString(prefix, server.props);
	return true;
    }

    /**
     * Make sure this is one of our requests.
     * IF OK, save file to proper spot.
     */

    public boolean
    respond(Request request) throws IOException {

	String type = (String) request.headers.get("content-type");
    	if (!isMine.match(request.url) ||
		!type.startsWith("multipart/form-data")) {
	    return false;
	}	

	/*
	 * screen out bad requests
	 */

	if (request.postData == null) {
	    request.sendError(400, "No content to put");
	    return true;
	}

	if (request.headers.get("Content-Range") != null) {
	    request.sendError(501, "Can't handle partial puts");
	    return true;
	}
	return processData(request);
    }

    /**
     * process the data - this doesn't currently do anything useful.
     */

    public boolean processData(Request request) {
	Split s = new Split(request.postData);
	while (s.nextPart()) {
	    System.out.println(s.name() + ": {" + s.content() + "}");
	}
	return false;
    }

    /**
     * Split multipart data into its constituent pieces.  Use byte[] so we can
     * handle (potentially) large amounts of binary data.
     * This acts as an iterator, stepping through the parts, extracting the appropriate
     * info for eacxh part.
     */

    public static class Split {
	byte[] bytes;
	int bndryEnd;   // end of the initial boundary line
	int partStart;	// start index of this part
	int partEnd;	// index to the end of this part
	int contentStart;	// start of the content

 	/**
	 * create a new multipart form thingy
	 */

	public Split(byte[] bytes) {
	    partEnd = 0;
	    this.bytes = bytes;
	    bndryEnd = indexOf(bytes, 0, bytes.length, "\r\n");
	    partStart=0;
	    contentStart=0;
	}

	/**
	 * Return true if there is a next part
	 */

	public boolean
	nextPart() {
	    partStart = partEnd + bndryEnd+2;
	    if (partStart >= bytes.length) {
		return false;
	    }
	    partEnd = indexOf(bytes, partStart, bytes.length, bytes, 0, bndryEnd);
	    if (partEnd < 0) {
		return false;
	    }
	    partEnd -=2; // back over \r\n
	    contentStart = indexOf(bytes, partStart, bytes.length, "\r\n\r\n") + 4;
	    return true;
	}

	/**
	 * Get the content as a string
	 */

	public String
	content() {
	    return new String(bytes, contentStart, partEnd-contentStart);
	}

	/**
	 * Return the content length
	 */

	public int
	length() {
	    return partEnd - contentStart;
	}

	/**
	 * return the index into the start of the data for this part
	 */

	public int
	start() {
	    return contentStart;
	}

	/**
	 * Return the header as a string
	 */

	public String header() {
	    return (new String(bytes, partStart, contentStart-partStart));
	}

	/**
	 * get the part name
	 */
	
	public String name() {
	    int start = indexOf(bytes, partStart, contentStart, "name=\"") + 6;
	    int end = indexOf(bytes, start, contentStart, "\"");
	    if (start>=6 && end >=0) {
		return (new String(bytes, start, end-start));
	    } else {
	        return null;
	    }
	}
    }

    /**
     * Find the index of dst in src or -1 if not found >
     * This is the byte array equivalent to string.indexOf()
     */
     
    public static int
    indexOf(byte[] src, int srcStart, int srcEnd, byte[] dst, int dstStart, int dstEnd) {
        int len = dstEnd - dstStart;    // len of to look for
	srcEnd -= len;
        for (;srcStart < srcEnd; srcStart++) {
	    boolean ok=true;
	    for(int i=0; ok && i<len;i++) {
		if (dst[i+dstStart] != src[srcStart + i]) {
		    ok=false;
		}
	    }
	    if (ok) {
		return srcStart;
	    }
	}
	return -1;
    }

    public static int
    indexOf(byte[] src, int srcStart, int srcEnd, String dst) {
        return (indexOf(src, srcStart, srcEnd, dst.getBytes(), 0, dst.length()));
    }
}
