/*
 * ContentTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2007 Sun Microsystems, Inc.
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
 * Version:  2.5
 * Created by suhler on 99/06/28
 * Last modified by suhler on 07/04/04 14:30:59
 *
 * Version Histories:
 *
 * 2.5 07/04/04-14:30:59 (suhler)
 *   gather up all externally referenced javascript sources
 *
 * 2.4 06/11/13-10:44:43 (suhler)
 *   ignore script references to external files
 *
 * 2.3 06/01/06-13:50:59 (suhler)
 *   bug fix: inHead wasn't being reinitialized on every page.
 *   bug fix: The "prepend" attribute was being ignored for some properties
 *
 * 2.2 02/11/14-14:27:54 (suhler)
 *   handle empty bodies
 *
 * 2.1 02/10/01-16:36:44 (suhler)
 *   version change
 *
 * 1.21 02/07/24-10:46:25 (suhler)
 *   doc updates
 *
 * 1.20 02/01/29-14:29:35 (suhler)
 *   doc lint
 *
 * 1.19 01/09/13-09:23:54 (suhler)
 *   added "all" to extract the entire content
 *
 * 1.18 01/08/14-16:38:42 (suhler)
 *   doc lint
 *
 * 1.17 01/07/16-16:45:33 (suhler)
 *   add "prepend" option
 *
 * 1.16 00/12/27-12:22:42 (suhler)
 *   Fixed handling of content, script, and style.
 *   - multiple "content ... /content" pairs now work properly
 *
 * 1.15 00/12/11-20:24:23 (suhler)
 *   doc typo
 *
 * 1.14 00/12/11-13:30:00 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.13 00/12/04-08:45:34 (suhler)
 *   add satyle handling
 *
 * 1.12 00/10/31-10:19:25 (suhler)
 *   capture attributes of body tags
 *
 * 1.11 00/10/05-11:14:33 (suhler)
 *   extract http-eauiv tags to request headers
 *
 * 1.10 00/05/31-13:49:27 (suhler)
 *   name change
 *
 * 1.9 00/05/22-14:05:13 (suhler)
 *   doc updates
 *
 * 1.8 00/02/02-14:50:53 (suhler)
 *   removed debugging
 *
 * 1.7 99/12/07-10:55:39 (suhler)
 *   gather all javascript in the headers into a "script" property
 *
 * 1.6 99/10/18-10:26:01 (suhler)
 *   remove diagnostics
 *
 * 1.5 99/10/11-12:31:15 (suhler)
 *   copy some mime headers into the request properties
 *
 * 1.4 99/10/06-12:17:56 (suhler)
 *   bug fix
 *
 * 1.3 99/09/29-16:05:31 (cstevens)
 *   New HtmlRewriter object, that allows arbitrary rewriting of the HTML (by
 *   templates and others), instead of forcing the templates to return a string
 *   that contained all of the new HTML content in one big string.
 *
 * 1.2 99/09/01-12:16:02 (suhler)
 *   make url a request property
 *
 * 1.2 99/06/28-11:07:59 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/ContentTemplate.java
 *
 * 1.1 99/06/28-11:07:58 (suhler)
 *   date and time created 99/06/28 11:07:58 by suhler
 *
 */

package sunlabs.brazil.template;

import java.util.Dictionary;

/**
 * Template class for extracting content out of remote html pages.
 * This class is used by the TemplateHandler, for extracting
 * the "content" out of html documents for later integration with
 * a look-and-feel template using one or more of:
 * {@link SetTemplate}, 
 * {@link BSLTemplate}, 
 * or
 * {@link sunlabs.brazil.filter.ReplaceFilter},
 *
 * The plan is to snag the title and the content, and put them into
 * request properties.  The resultant processed output will be
 * discarded.  The following properties are gathered:
 * <dl class=props>
 * <dt>title		<dd> The document title
 * <dt>all		<dd> The entire content
 * <dt>bodyArgs		<dd> The attributes to the body tag, if any
 * <dt>content		<dd> The body, delimited by content.../content>.
 *			The text inside multiple &lt;content&gt;
 *			... &lt;/content&gt; pairs
 *			are concatenated together.
 * <dt>script		<dd> All "&lt;script&gt;"..."&lt;/script&gt;"
 *			tags found in the document head
 * <dt>scriptSrcs	<dd> A white-space delimited list of all "src"
			     attributes found in "script" tags.
 * <dt>style		<dd> All "&lt;style"&gt;..."&lt;/style"&gt;
 *			tags found in the document head
 * <dt>meta-[name]	<dd> Every meta tag "name" and "content"
 * <dt>link-[rel]	<dd> Every link tag "rel" and "href"
 * <dt>user-agent	<dd> The origin user agent
 * <dt>referer		<dd> The user agent referrer (if any)
 * <dt>last-modified	<dd> The document last modified time (if any) in std format
 * <dt>content-length	<dd> The document content length, as fetched from the origin server
 * </dl>
 * Properties:
 * <dl class=props>
 * <dt>prepend	<dd>Prepend this string to the property names define above,
 * that are populated by this template. (defaults to "").
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		%V% 2.2
 */

public class ContentTemplate extends Template {
    boolean inHead = true;
    String prefix;		// prefix all properties with this

    public boolean
    init(RewriteContext hr) {
	inHead=true;
	String all = hr.lex.rest();
	if (all != null) {
            prefix = hr.request.props.getProperty(hr.prefix + "prepend", "");
	    hr.request.props.put(prefix + "all", all);
	}
	return super.init(hr);
    }

    /**
     * Toss everything up to and including this entity.
     */

    public void
    tag_title(RewriteContext hr) {
	hr.reset();
    }

    /**
     * Gather up the title - no tags allowed between title .... /title.
     */

    public void
    tag_slash_title(RewriteContext hr) {
	hr.request.props.put(prefix + "title", hr.toString().trim());
	hr.reset();
    }

    /**
     * Append all "script" code while in the head section.
     * If the script has a "src" attribute, we'll put the "src" in
     * a variable so the template can deal with it (them?)
     * For now, ignore it.
     */

    public void
    tag_script(RewriteContext hr) {
	String src = hr.get("src");
	if (src == null) {
	    do_tag(hr, "script");
	} else {
	    String srcs = hr.request.props.getProperty(prefix + "scriptSrcs"); 
	    if (srcs != null) {
		hr.request.props.put(prefix + "scriptSrcs", srcs + " " +src);
	    } else {
		hr.request.props.put(prefix + "scriptSrcs", src);
	    }
        }
    }

    /**
     * Append all "style" code while in the head section.
     */

    public void
    tag_style(RewriteContext hr) {
        do_tag(hr, "style");
    }

    void
    do_tag(RewriteContext hr, String tag) {
	if (inHead) {
	    boolean save = hr.accumulate(false);
	    hr.nextToken();
	    String current = hr.request.props.getProperty(prefix + tag,"") +
		    hr.getBody();
	    hr.request.props.put(prefix + tag, current);
	    hr.accumulate(save);
	    hr.reset();
	}
    }

    /**
     * Mark end of head section.  All "script" content in the "body"
     * is left alone.
     */

    public void
    tag_slash_head(RewriteContext hr) {
	inHead = false;
    }

    /**
     * toss everything up to and including here, but turn on
     * content accumulation.
     */

    public void
    tag_content(RewriteContext hr) {
	hr.reset();
	hr.accumulate(true);
    }

    /**
     * Grab the "body" attributes, and toss all output to this point.
     */

    public void
    tag_body(RewriteContext hr) {
	inHead = false;
	String bodyArgs = hr.getArgs();
	if (bodyArgs != null) {
	    hr.request.props.put(prefix + "bodyArgs", bodyArgs);
	}
	hr.reset();
    }

    /**
     * Save the content gathered so far, and turn off content accumulation.
     */

    public void
    tag_slash_content(RewriteContext hr) {
	String content = hr.request.props.getProperty("content","") +
		hr.toString();
	hr.request.props.put(prefix + "content", content);
	hr.accumulate(false);
    }

    /**
     * If no content tags are present, use the entire "body" instead.
     */

    public void
    tag_slash_body(RewriteContext hr) {
	if (!hr.request.props.containsKey("content")) {
	    hr.request.props.put(prefix + "content", hr.toString());
	    hr.accumulate(false);
	}
    }

    /**
     * Extract data out of meta tags into the properties.
     * For "http-equiv" tags, set the corrosponding http respones header.
     */

    public void
    tag_meta(RewriteContext hr) {
	String name = hr.get("name", false);
	String equiv = hr.get("http-equiv", false);
	String content = hr.get("content", false);
	if ((name != null) && (content != null)) {
	    hr.request.props.put(prefix + "meta-" + name, content);
	} else if ((equiv != null) && (content != null)) {
	    hr.request.addHeader(equiv, content);
	}
    }

    /**
     * Extract data out of link tags into the properties.
     * Prefix the "rel" attribute with "link-" to use as the 
     * property name.
     */

    public void
    tag_link(RewriteContext hr) {
	String type = hr.get("rel", false);
	String href = hr.get("href", false);
	if ((type != null) && (href != null)) {
	    hr.request.props.put(prefix + "link-" + type, href);
	}
    }

    /**
     * Extract useful properties out of the http mime headers.
     */

    public boolean
    done(RewriteContext hr) {
	tag_slash_body(hr);

	transfer("user-agent", hr.request.headers, hr.request.props);
	transfer("referer", hr.request.headers, hr.request.props);
	transfer("last-modified", hr.request.responseHeaders, hr.request.props);
	transfer("content-length", hr.request.responseHeaders,hr.request.props);
	return true;
    }

    /**
     * Transfer an item to another hash table, if it exists
     */

    private boolean
    transfer (String key, Dictionary src, Dictionary dst) {
	Object obj = src.get(key);
	if (obj != null) {
	    dst.put(prefix + key, obj);
	    return true;
	} else {
	    return false;
	}
    }
}
