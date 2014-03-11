/*
 * PutHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2006 Sun Microsystems, Inc.
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
 * Created by suhler on 06/11/20
 * Last modified by suhler on 06/12/12 16:36:04
 *
 * Version Histories:
 *
 * 1.3 06/12/12-16:36:04 (suhler)
 *   doc fixes
 *
 * 1.2 06/11/20-17:22:40 (suhler)
 *   use proper MatchString
 *
 * 1.2 06/11/20-17:07:02 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/PutHandler.java
 *
 * 1.1 06/11/20-17:07:01 (suhler)
 *   date and time created 06/11/20 17:07:01 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.util.MatchString;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Simple PUT and DELETE method handler.
 * Create, update, or delete files implied by the URL.
 * returns: 
 *   (for PUT)
 *   <ul>
 *   <li>201 Created		
 *   <li>204 No Content - it worked
 *   <li>415 Invalid file suffix (no mime type found)
 *   <li>403 bad file permissions
 *   <li>409 conflict suffix does not match mime type
 *   <li>500 server error: can't complete file write
 *   <li>501 invalid content-range
 *   </ul>
 *   (for DELETE)
 *   <ul>
 *   <li>204 No Content - delete succeeded
 *   <li>403 forbidden - no delete permissions
 *   <li>404 not found - no file to delete
 *   </ul>
 *
 * To be uploaded, the file suffix must match the content type
 * defined on the server and the content type of the request.
 *
 * <dl class=props>
 * <dt>root</td>
 * <dd>The document root.  Can be used to override the default document root.
 * <dt>prefix, suffix, glob, match
 * <dd>Specifies which URL's trigger this handler. (See MatchString).
 * </dl>
 * <p>
 * TODO:
 * <ul>
 * <li> allow the deletion of empty directories
 * <li> support byte-ranges for updating
 * </ul>
 * NOTES:<br>
 * This handler mostly overlaps the functionallity of the
 * PublishHandler, and they should be combined.
 */

public class PutHandler implements Handler {
    MatchString isMine;         // check for matching url

    public boolean
    init(Server server, String prefix) {
	isMine = new MatchString(prefix, server.props);
	return true;
    }

    public boolean
    respond(Request request) throws IOException {
	if (!isMine.match(request.url)) {
	    return false;
        }

	// We'll add content-ranges later.

	if (request.headers.get("Content-range") != null) {
	    request.sendError(501, "No content ranges, sorry");
	    return true;
	}

	String root = request.props.getProperty(isMine.prefix() + "root",
		request.props.getProperty("root","."));
	String name = FileHandler.urlToPath(request.url);

	// only allow creation/deletion of valid mime types:

	String impliedType = FileHandler.getMimeType(name, request.props,
		isMine.prefix());

	if (impliedType == null) {
	    request.sendError(415, "no mime type for supplied file suffix");
	    return true;
	}

	String type = request.headers.get("Content-type");
	if (type != null && !type.equalsIgnoreCase(impliedType)) {
	    request.sendError(409, "file suffix implies media type " +
		impliedType + ", but media type is specified as: " +
		type);
	    return true;
	}

	// At this point we have a file with a valid suffix and media type

	File file = new File(root, name);
	request.log(Server.LOG_DIAGNOSTIC, isMine.prefix(),
		"Processing: " + file);

	/*
	 * - if file doesn't exist, try to create it
	 * - if file exists, try to write it
	 */

	if (request.method.equals("PUT")) {
	    if (file.exists() && file.canWrite()) {
		boolean ok = writeFile(file, request.postData);
		if (ok) {
		    request.sendResponse("File replaced", "text/plain", 204);
		} else {
		    request.sendError(500, "upload failed");
		}
	    } else if (file.exists()) {
	        request.sendError(403, "Permission to write file denied");
	    } else {
		(new File(file.getParent())).mkdirs();
		if (writeFile(file, request.postData)) {
		    request.sendResponse("File created", "text/plain", 201);
		} else {
		    request.sendError(403, "Permission to create file denied");
		}
	    }
	} else if (request.method.equals("DELETE")) {
	    if (!file.exists()) {
                request.sendError(404, "No file to delete");
	    } else if (!file.delete()) {
                request.sendError(403, "Delete prohibited");
	    } else {
		request.sendResponse("delete succeeded", "text/plain", 204);
	    }
        } else {
	    return false;
	}
	return true;
    }

    /**
     * write a file, return success or failure.
     */

    boolean
    writeFile(File file, byte[] data) {
	try {
	    FileOutputStream out = new FileOutputStream(file);
	    out.write(data);
	    out.close();
	} catch (IOException e) {
	    return false;
	}
	return true;
    }
}
