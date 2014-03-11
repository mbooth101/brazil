/*
 * IncludeTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2004 Sun Microsystems, Inc.
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
 * Version:  2.4
 * Created by suhler on 99/05/06
 * Last modified by suhler on 04/08/30 10:29:38
 *
 * Version Histories:
 *
 * 2.4 04/08/30-10:29:38 (suhler)
 *   don't output inline post data
 *
 * 2.3 04/05/18-15:52:02 (suhler)
 *   add diagnostics so we can track remote fetches at level 4
 *
 * 2.2 04/04/05-14:51:00 (suhler)
 *   add additional attributes for sending query data
 *
 * 2.1 02/10/01-16:36:43 (suhler)
 *   version change
 *
 * 1.22 02/07/29-14:58:31 (suhler)
 *   bug fix: add the proper "host" header
 *
 * 1.21 02/07/24-10:47:12 (suhler)
 *   doc updates
 *
 * 1.20 02/07/17-14:19:56 (suhler)
 *   forgot to remove the <include> from the output stream if name=
 *   attribute is used
 *
 * 1.19 02/07/11-15:03:03 (suhler)
 *   use the new getContent() method of HttpRequest()
 *
 * 1.18 02/06/27-18:37:19 (suhler)
 *   add encoding option,
 *   add point-to-point headers
 *
 * 1.17 02/06/10-10:30:09 (suhler)
 *   add status http pseudo header
 *
 * 1.16 02/06/07-09:19:33 (suhler)
 *   added "addheaders" and "getheaders" options.
 *   This allows adding additional http headers to the request, as well
 *   as extracting the response headers
 *
 * 1.15 02/06/06-20:55:08 (suhler)
 *   add "post" attribute to enable posts
 *
 * 1.14 02/05/01-11:28:28 (suhler)
 *   fix sccs version info
 *
 * 1.13 02/04/25-13:43:50 (suhler)
 *   doc fixes
 *
 * 1.12 01/12/07-09:14:20 (suhler)
 *   added a proxy option
 *
 * 1.11 01/11/21-11:43:30 (suhler)
 *   doc fixes
 *
 * 1.10 01/11/16-14:19:29 (suhler)
 *   added "name=xxx" argument to place result in a property instead of
 *   inline
 *
 * 1.9 00/12/11-13:29:55 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.8 00/10/31-10:19:01 (suhler)
 *   doc fixes
 *
 * 1.7 00/05/31-13:49:13 (suhler)
 *   name change
 *
 * 1.6 00/05/22-14:05:06 (suhler)
 *   doc updates
 *
 * 1.5 00/02/11-13:54:56 (suhler)
 *   removed redundant try/catch
 *
 * 1.4 99/11/17-10:19:45 (suhler)
 *   new syntax: <include href=link>
 *   .
 *
 * 1.3 99/09/29-16:05:16 (cstevens)
 *   New HtmlRewriter object, that allows arbitrary rewriting of the HTML (by
 *   templates and others), instead of forcing the templates to return a string
 *   that contained all of the new HTML content in one big string.
 *
 * 1.2 99/06/28-10:51:52 (suhler)
 *   new api's
 *
 * 1.2 99/05/06-15:21:50 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/IncludeTemplate.java
 *
 * 1.1 99/05/06-15:21:49 (suhler)
 *   date and time created 99/05/06 15:21:49 by suhler
 *
 */

package sunlabs.brazil.template;

import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.http.HttpInputStream;
import sunlabs.brazil.util.http.HttpRequest;
import sunlabs.brazil.util.http.HttpRequest;
import sunlabs.brazil.util.http.MimeHeaders;
import sunlabs.brazil.util.http.HttpUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * Template class for substituting html pages into an html page.
 * This class is used by the TemplateHandler.  This does not perform
 * traditional server-side include processing, whose normal purpose is to
 * include standard headers and footers.  That functionallity is provided
 * instead by including the content into the template, and not the other
 * way around.
 * <p>
 * This include incorporates entire pages from other sites.
 * <dl class=props>
 * <dt>proxy	<dd>If specified, connect through a proxy. This should be
 *		in the form <code>host:port</code>, or <code>host</code>
 *		it the desired port is <code>80</code>.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		@(#)IncludeTemplate.java	2.4
 */
    /**
     * Convert the html tag "include" in to text for an included
     * html page.
     * Attributes processed
     * <dl class=attributes>
     * <dt>href<dd>Absolute url to fetch, and insert here
     * <dt>post<dd>Post data if any.  If set, an http POST is issued.
     * The post data is expected to be in www-url-encoded format.
     * <dt>alt<dd>Text to insert if URL can't be obtained.
     * <dt>name<dd>The name of the variable to put the result in.
     * If this is specified, the content is not included in place.
     * The template prefix is automatically prepended to the name.
     * <dt>proxy<dd>The proxy:port to use as a proxy (if any).
     * If specified, it overrides the <code>proxy</code> property, 
     * in <code>request.props</code>.
     * <dt>addheaders<dd>A white space delimited set of token names that
     * represent additional http headers to add to the target request.
     * For each token, the values <i>[token].name</i> and
     * <i>[token].value</i> in the
     * <code>request.props</code> are used for the header name and value
     * respectively.
     * <dt>encoding<dd>The character encoding to use if it can't be
     * automatically determined.   Defaults to the default platform encoding.
     * <dt>getheaders<dd>The name of the variable prefix to use to
     * extract the http response headers.
     * If this not specified, no response headers are retrieved.
     * The result will be properties of the form:
     * <code>[prefix].[getheaders].[header_name]=[header_value]</code>.
     * If multiple entries exist for a particular header name, the values
     * are combined as per HTTP conventions (e.g. v1, v2, ... vn).
     * The pseudo headers <code>status</code> and <code>encoding</code>
     * will contain the http status line and charset encoding, respectively.
     * <dt>error<dd>If <i>name</i> is specified and the result
     * could not be obtained, the error message is placed here.
     * <dt>queryList<dd>A list of property names whose names and values
     * will be added to the url as query parameters.  The values are properly
     * url-encoded.
     * </dl>
     * Example: <code>&lt;include href=http://www.foo.com/index.html&gt;</code>
     */

public class IncludeTemplate
    extends Template {

    public void
    tag_include(RewriteContext hr) {
	String href = hr.get("href");	// the url to fetch
	String alt = hr.get("alt");	// the result if fetch failed
	String name = hr.get("name");	// the variable to put the result in
	String post = hr.get("post");	// post data (if any)
	String addheaders = hr.get("addheaders"); // additional http headers
	String getheaders = hr.get("getheaders"); // retrieve response headers
	String encoding = hr.get("encoding", null); // retrieve response headers
	boolean postInline = hr.isTrue("postinline");  // inline post data
	// these are experimental
	boolean encodeQuery = hr.isTrue("encodeQuery");  // fix the url
	String queryList = hr.get("queryList");

	if (alt==null) {
	    alt = "";
	}
	String proxy = hr.get("proxy", "");
	String proxyHost = null;
	int proxyPort = 80;
	if (!proxy.equals("")) {
	    int index = proxy.indexOf(":");
	    if (index < 0) {
		proxyHost = proxy;
	    } else {
		proxyHost = proxy.substring(0,index);
		try {
		    proxyPort =
			Integer.decode(proxy.substring(index+1)).intValue();
		} catch (Exception e) {}
	    }
	}

	debug(hr);
	hr.killToken();

	// XXX This is wrong!

	if (postInline && !hr.isSingleton()) {
            StringBuffer sb = new StringBuffer();
	    int nest=0;
            while (hr.nextToken()) {
                String token = hr.getToken();
		hr.killToken();
                hr.getTag();   // force singleton checking!
                if (token.startsWith("<include ") && !hr.isSingleton()) {
                    nest++;
                } else if (token.equals("</include>") && (--nest < 0)) {
                    break;
                }
                sb.append(token);
            }
	    // hr.append(Format.subst(hr.request.props, hr.getBody(), true));
	    post = Format.subst(hr.request.props, sb.toString());
	    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
		"Posting: " + post);
	    debug(hr, post);
	}

	MimeHeaders clientHeaders = new MimeHeaders();
	hr.request.headers.copyTo(clientHeaders);
	HttpRequest.removePointToPointHeaders(clientHeaders, false);
	clientHeaders.remove("host");

	/*
	 * Fix broken query strings - this is dubious
	 */

	if (encodeQuery) {
	    int index = href.indexOf("?");
	    if (index > 0) {
		href = href.substring(0,index+1) + 
			href.substring(index+1).replace(' ', '+');
	    }
	}

	/*
	 * build a query string based on properties
	 */

	if (queryList != null) {
	    StringTokenizer st = new StringTokenizer(queryList);
            StringBuffer sb = new StringBuffer(href);
	    String delim = href.indexOf("?") > 0 ? "&" : "?";
	    while (st.hasMoreTokens()) {
	        String key = st.nextToken();
	        String value = hr.request.props.getProperty(key);
		sb.append(delim).append(key).append("=");
		if (value != null) {
		    sb.append(HttpUtil.urlEncode(value));
		}
		delim="&";
	    }
	    href = sb.toString();
	    debug(hr, "Using href: " + href);
	}

	HttpRequest target = new HttpRequest(href);
	clientHeaders.copyTo(target.requestHeaders);
	if (addheaders != null) {
	    target.addHeaders(addheaders, hr.request.props);
	}
	target.requestHeaders.putIfNotPresent("Host",
	    HttpUtil.extractUrlHost(href));
	target.setRequestHeader("Via", "Brazil-Include/2.0");

	if (proxyHost != null) {
	    debug(hr, "Using proxy: " + proxy);
	    target.setProxy(proxyHost, proxyPort);
	}
	hr.request.log(Server.LOG_INFORMATIONAL, hr.prefix,
		"Fetching: " + href);
	try {
	    if (post != null) {
		OutputStream out = target.getOutputStream();
		out.write(post.getBytes());
		out.close();
	    }
	    String enc = target.getContent(encoding);
	    if (name != null) {
		hr.request.props.put(hr.prefix + name, enc);
	    } else {
		hr.append(enc);
	    }
	    if (getheaders != null) {
	        Enumeration keys = target.responseHeaders.keys();
	        while(keys.hasMoreElements()) {
		    String key = (String) keys.nextElement();
		    String full = hr.prefix + getheaders + "." + key;
		    if (hr.request.props.containsKey(full)) {
		        hr.request.props.put(full,
				hr.request.props.getProperty(full) +
			        ", " + target.responseHeaders.get(key));
		    } else {
		        hr.request.props.put(full,
			        target.responseHeaders.get(key));
		    }
	        }
		hr.request.props.put(hr.prefix + getheaders + ".status",
			target.status);
		String en = target.getEncoding();
		if (en != null) {
		    hr.request.props.put(hr.prefix + getheaders +
			    ".encoding", en);
		}
	    }
	} catch (IOException e) {
	    if (name != null) {
		hr.request.props.put(hr.prefix + name, alt);
		hr.request.props.put(hr.prefix + "error", e.getMessage());
	    } else {
	        hr.append("<!-- " + e + " -->" + alt);
	    }
	}
	hr.request.log(Server.LOG_INFORMATIONAL, hr.prefix,
		"Done fetching: " + href + " " + target.status);
    }

    /**
     * Treat comments of the form:
     * <code>&lt;!#include ...&gt;</code> as if they were include tags.
     */

    public void
    comment(RewriteContext hr)
    {
	if (hr.getBody().startsWith("#include") == false) {
	    return;
	}
	tag_include(hr);
    }
}
