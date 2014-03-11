/*
 * MultipartSetTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2006 Sun Microsystems, Inc.
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
 * Version:  2.5
 * Created by suhler on 01/08/20
 * Last modified by suhler on 06/04/25 14:16:24
 *
 * Version Histories:
 *
 * 2.5 06/04/25-14:16:24 (suhler)
 *   use consolidated mime type handling in FileHandler
 *
 * 2.4 04/12/15-13:10:27 (suhler)
 *   redo automatic file saving.  Specify a pattern to match file uploads
 *   to be saved, and a name (containing ${...}) to specify the name to
 *   save the file as
 *
 * 2.3 04/11/30-10:59:44 (suhler)
 *   add direct file saves (not quite right)
 *
 * 2.2 03/08/01-16:18:45 (suhler)
 *   fixes for javadoc
 *
 * 2.1 02/10/01-16:36:53 (suhler)
 *   version change
 *
 * 1.5 02/05/01-11:28:58 (suhler)
 *   fix sccs version info
 *
 * 1.4 02/01/29-14:33:12 (suhler)
 *   doc lint
 *
 * 1.3 01/09/13-09:24:22 (suhler)
 *   remove uneeded import
 *
 * 1.2 01/08/21-11:00:46 (suhler)
 *   small bug in content-type guessing
 *
 * 1.2 01/08/20-20:38:33 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/MultipartSetTemplate.java
 *
 * 1.1 01/08/20-20:38:32 (suhler)
 *   date and time created 01/08/20 20:38:32 by suhler
 *
 */

package sunlabs.brazil.template;

import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.Base64;
import sunlabs.brazil.util.Glob;
import sunlabs.brazil.util.http.HttpInputStream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;

/**
 * Version of the SetTemplate that reflects form/multipart data
 * in Request.props.
 * For ordinary forms, the values placed into <code>request.props</code>
 * are the same as for forms of type <code>www-url-encoded</code>, or
 * <code>method=get</code>, as long as the <code>query</code> option
 * is set.
 * <p>
 * For file input fields (e.g. <code>&lt;input type=file...&gt;</code>), 
 * the file content is associated with the field <i>name</i>, and the
 * properties
 * <i>name</i>.<code>filename</code>,
 * <i>name</i>.<code>type</code>,
 * and
 * <i>name</i>.<code>encoding</code>
 * are set to to the name of the file uploaded, its type, and
 * (unless <code>noEncode</code> is set), the encoding, which is
 * either none (for text files), or Base64.
 * <p>
 * The file contents are automatically Base64 encoded for binary files.
 * <p>
 * Properties:
 * <dl class=props>
 * <dt>query<dd>If present, The form data is translated from form/multipart
 *     and placed into the request properties, prefixed by the value 
 *     of <code>query</code>. 
 * <dt>noEncode<dd>If present, no encoding is performed on file uploads.
 * <dt>savePattern=[glob pattern]
 * <dd>
 * If set, then the form is scanned for field names that match
 * <i>glob pattern</i>.  If a match is found, then the <i>next</i>
 * form element of type <i>file</i> is saved to a file in the document
 * root instead of being loaded as a property.  The name of the
 * file is specified by the value of the <code>saveName</code>
 * entry.
 * <dt>saveName=<code>name</code>
 * <dd>The name to use to save the file.  May contain ${...}
 * substitutions.  The variables ${fileName},  ${fieldName) and
 * ${prefix} may be used here as "special" variables to make creating
 * a file name easier.
 * <code>saveName</code>defaults to: <code>
 * ${prefix}-${fieldName}-${fileName}</code>
 * </code>
 * </dl>
 * [This has only been tested with Netscape Navigator and Mozilla.]
 *
 * @author Stephen Uhler
 * @version %W
 */

public class MultipartSetTemplate extends SetTemplate {

    public boolean
    init(RewriteContext hr) {
	String query = get(hr, "query");
	boolean noEncode = isTrue(hr, "noEncode") ;
	String type = (String) hr.request.headers.get("content-type");
	if (query != null && type != null &&
		type.startsWith("multipart/form-data")) {
	    processData(hr, query);
	}
        return super.init(hr);
    }

    /**
     * Extract the form/multipart data into request.props.
     * IF the content type is supplied with the file, use it.
     * Otherwise, consult our own configuration file for mime types.
     * See {@link sunlabs.brazil.server.FileHandler} for a
     * description of how to set mime types for file suffixes.
     * If no type can be determined, the "type" property will not be set.
     *
     * @param prefix:	The prefix used to set properties
     */

    void processData(RewriteContext hr, String prefix) {
	boolean noEncode = isTrue(hr, "noEncode");
	String query = get(hr, "query");
	String glob = get(hr, "savePattern");
	// don't do ${...} yet
	String saveName = hr.request.props.getProperty(hr.prefix + "saveName",
 		"${prefix}${fieldName}-${fileName}");
	boolean saveAs = false;		// save next file as this name

	/*
	 * Use a local properties object to make it easier to specify
	 * ${..} keys when defining the name of the file to save
	 */

	Properties local = new Properties(hr.request.props);
	local.put("prefix", hr.prefix);

	Split s = new Split(hr.request.postData);
	while (s.nextPart()) {
	    String fieldName = s.name();
	    String type = s.type();
	    String fileName = s.fileName();

	    put(hr, prefix, fieldName + ".headers" , s.header().trim());
	    if (fileName != null) {		// we have a file upload
		put(hr, prefix, fieldName + ".filename" , fileName);
		int index;
		if (type == null) {
		    type = FileHandler.getMimeType(fileName,
			    hr.request.props, hr.prefix);
		    log(hr, Server.LOG_DIAGNOSTIC, "Guessing type: " + type);
		}
		if (type != null) {
		    put(hr,  prefix, fieldName + ".type", type);
		}
	    } else if (Glob.match(glob, fieldName)) {
		saveAs = true;
	    }

	    // XXX allow option to write directly to a file
	    // Use a property to match the file name in the form, and
	    // re-write it

	    if (saveAs && fileName != null) {	// save the file
		local.put("fieldName", fieldName);
		local.put("fileName", fileName);
	        String fn = Format.subst(local, saveName);
		put(hr,  prefix, fieldName + ".file", fn);
		writeFile(hr, fn, s.stream());
		saveAs = false;
	    } else if (fileName==null || noEncode) {	// set name=value
		put(hr, prefix, fieldName, s.content());	
	    } else if (type != null && type.startsWith("text")) {
		put(hr, prefix, fieldName, s.content());	
		put(hr, prefix, fieldName + ".encoding", "none");
	    } else {
		put(hr, prefix, fieldName + ".encoding", "base64");
		put(hr, prefix, fieldName, s.content(true));	
	    }
	}
    }

    /**
     * Write out the file whose name is "name".  If name
     * starts with "/" make it relative to the document root.
     * Otherwise assume it is relative to the directory implied
     * by the URL.
     */

    void
    writeFile(RewriteContext hr, String name, InputStream in) {

	String root = get(hr, FileHandler.ROOT);
	if (root == null) {
	    root = hr.request.props.getProperty(FileHandler.ROOT, ".");
	}

	File file;
	if (name.startsWith("/")) { 
	    file = new File(root, name.substring(1));
	} else {
	    String dir = FileHandler.urlToPath(hr.request.url);
	    file = new File(root + dir);
	    if (!file.isDirectory()) {
		file = new File(file.getParent());
	    }
	    file = new File(file, name);
	}
	HttpInputStream hin = new HttpInputStream(in);
	try {
	    FileOutputStream out = new FileOutputStream(file);
	    hin.copyTo(out);
	    log(hr, Server.LOG_DIAGNOSTIC, "wrote file: " + file);
	} catch (IOException e) { 
	    log(hr, Server.LOG_LOG, "Can't write file: " + e.getMessage());
	}
    }

    // convenience methods 'cause we're not in  a tag

    /**
     * Get the value of a request property.  We can't use
     * hr.get() 'cause we're not in a tag.
     */

    String get(RewriteContext hr, String key) {
	String value = hr.request.props.getProperty(hr.prefix + key);
	return Format.subst(hr.request.props, value);
    }

    /**
     * put a value into request props with our prefix
     */

    void put(RewriteContext hr, String prefix, String key, String value) {
	hr.request.props.put(prefix + key , value);
    }

    boolean isTrue(RewriteContext hr, String key) {
	String value = get(hr, key);
	return (value != null && !Format.isFalse(value.trim().toLowerCase()));
    }

    void log(RewriteContext hr, int level, String msg) {
	hr.request.log(level, hr.prefix, msg);
    }

    /*
     * We need to define all template tags here,
     * as they are determined via reflection.
     */

    public void
    tag_set(RewriteContext hr) {
        super.tag_set(hr);
    }
    public void
    tag_property(RewriteContext hr) {
        tag_get(hr);
    }
    public void
    tag_get(RewriteContext hr) {
        super.tag_get(hr);
    }
    public void
    tag_import(RewriteContext hr) {
        super.tag_import(hr);
    }
    public void
    tag_tag(RewriteContext hr) {
        super.tag_tag(hr);
    }
    public void
    tag_slash_tag(RewriteContext hr) {
        super.tag_slash_tag(hr);
    }

    /**
     * Shamelessly copied from PushHandler
     * Split multipart data into its constituent pieces.  Use byte[] so we can
     * handle (potentially) large amounts of binary data.
     * This acts as an iterator, stepping through the parts,
     * extracting the appropriate
     * info for each part.
     *
     * This has been tested with the data Navigator sends as
     * encoding=form/multipart data.
     */

    static class Split {
	byte[] bytes;	// raw form/multipart data
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
	    partEnd = indexOf(bytes, partStart, bytes.length, bytes, 0,
		    bndryEnd);
	    if (partEnd < 0) {
		return false;
	    }
	    partEnd -=2; // back over \r\n
	    contentStart = indexOf(bytes, partStart, bytes.length,"\r\n\r\n")+4;
	    return true;
	}

	/**
	 * Get the content as a (base64) encoded string
	 * @param encode:  If true, base-64 encode, else no encoding
	 */

	public String
	content(boolean encode) {
	    if (encode) {
		return Base64.encode(bytes, contentStart, partEnd-contentStart);
	    } else {
		return new String(bytes, contentStart, partEnd-contentStart);
	    }
	}

	/**
	 * Get the content as an input stream
	 */

	public ByteArrayInputStream stream() {
	    return new ByteArrayInputStream(bytes,
		    contentStart, partEnd-contentStart);
	}

	/**
	 * Get the content as a string
	 */

	public String
	content() {
	    return content(false);
	}

	/**
	 * Return the header as a string
	 */

	public String header() {
	    return (new String(bytes, partStart, contentStart-partStart));
	}

	/**
	 * get the field name
	 */
	
	public String
	name() {
	    return part("name=\"", "\"");
	}

	/**
	 * Get the file name, if any
	 */

	public String
	fileName() {
	    return part("filename=\"", "\"");
	}

	/**
	 * Get the content type (if any)
	 */

	public String
	type() {
	    return part("Content-Type: ", "\r");
	}

	/**
	 * find info in the header
	 * @param start: String that matches portion of header before
	 *		 what we extract
	 * @param end: String that matches the termination of the part we
	 * 		extract
	 */
	
	String part(String pre, String post) {
	    int start = indexOf(bytes, partStart, contentStart, pre) +
		    pre.length();
	    int end = indexOf(bytes, start, contentStart, post);
	    if (start>=pre.length() && end >=0) {
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
     
    static int
    indexOf(byte[] src, int srcStart, int srcEnd, byte[] dst, int dstStart,
		int dstEnd) {
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

    static int
    indexOf(byte[] src, int srcStart, int srcEnd, String dst) {
        return (indexOf(src, srcStart, srcEnd, dst.getBytes(), 0,dst.length()));
    }
}
