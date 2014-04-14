/*
 * SourceTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2004-2005 Sun Microsystems, Inc.
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
 * Version:  1.5
 * Created by suhler on 04/03/22
 * Last modified by suhler on 05/06/09 11:18:38
 *
 * Version Histories:
 *
 * 1.5 05/06/09-11:18:38 (suhler)
 *   Added "reprocess=true" attribute to re-process source'd file as a template.
 *
 * 1.4 05/01/26-22:48:36 (suhler)
 *   doc fixes
 *
 * 1.3 04/12/07-09:47:06 (suhler)
 *   added ability to "source" a document from the filesystem
 *
 * 1.2 04/03/22-15:04:53 (suhler)
 *   doc lint
 *
 * 1.2 04/03/22-15:01:49 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/SourceTemplate.java
 *
 * 1.1 04/03/22-15:01:48 (suhler)
 *   date and time created 04/03/22 15:01:48 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import sunlabs.brazil.handler.ResourceHandler;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.util.http.HttpInputStream;
import sunlabs.brazil.util.Format;

/**
 * Template class for incorporating the content of a local file into
 * the current document.
 * <pre>
 *  &lt;source src=file
 *	[encoding=enc name=property eval=true|false reprocess=true|false]
 *  &gt;
 * </pre>
 * Attributes:
 * <dl class=attribute>
 * <dt>src<dd>Where to find the document to source.  Unless starting with
 * "/", it is assumed to be relative to the document root.
 * <dt>eval
 * <dd>If true, all ${...} are evaluated as the file is read in from storage.
 * <dt>encoding
 * <dd>Specifies the character encoding to use.  If not specified
 * or invalid, the default encoding is used.
 * <dt>name
 * <dd>If set, the content is placed in the named variable instead
 * of being inserted in-line.
 * <dt>reprocess
 * <dd>If true, and <code>name</code> is not set (e.g. the content is inserted
 * in-line, then the content will be run through the normal template
 * processing before being inserted into the current document.
 * </dl>
 * <p>
 * <dl>
 * <dt>Examples:
 * <dd>
 * The form:
 * <code>&lt;source src=xml.tmpl eval=true&gt;</code>
 * can be used to read in XML templates (or template fragments) that
 * contain ${...} constructs that get <i>filled in</i> as the template
 * is read.
 * <p>
 * The form:
 * <code>&lt;source src=section1.html reprocess=true&gt;</code>
 * can be used as a convenient way to break a single logical file into
 * separate sections. (Note: this isn't currently implemented in a very
 * efficient way.
 * </dl>
 * @author Stephen Uhler
 * SCCS: @(#) SourceTemplate.java 1.5 05/06/09 11:18:38
 */

public class SourceTemplate extends Template {

    public void
    tag_source(RewriteContext hr) {
	String src = hr.get("src");	// the file to fetch
	String enc = hr.get("encoding");	// character encoding
	String name = hr.get("name");	// property to store content in
	boolean shouldEval=hr.isTrue("eval"); // run through subst()
	boolean shouldReprocess = hr.isTrue("reprocess");
        debug(hr);
        hr.killToken();

	if (src == null) {
	    debug(hr, "\"src\" not specified");
	    return;
	}

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	String result = null;
	try {
	    HttpInputStream hin = new HttpInputStream(
		    ResourceHandler.getResourceStream(
			hr.request.props, hr.prefix, src));
	    hin.copyTo(baos);
	    hin.close();
	} catch (IOException e) {
	    debug(hr, "Bad source file: " + e);
	    return;
	}
	if (enc != null) {
	    try {
		result= baos.toString(enc);
	    } catch (UnsupportedEncodingException e) {
		debug(hr, "Bad encoding format: " + enc);
	    }
	}
	if (result == null) {
	    result = baos.toString();
	}
	if (shouldEval) {
	    result = Format.subst(hr.request.props, result);
	}
	if (name != null) {
	    hr.request.props.put(name, result);
	} else if (shouldReprocess) {
	    String rest = hr.lex.rest();
	    if (rest != null) {
	        hr.lex.replace(result + rest);
	    } else {
	        hr.lex.replace(result);
	    }
	} else {
	    hr.append(result);
	}
    }
}
