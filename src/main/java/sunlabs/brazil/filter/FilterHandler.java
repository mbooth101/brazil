/*
 * FilterHandler.java
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
 * Version:  2.3
 * Created by suhler on 99/07/29
 * Last modified by suhler on 06/11/13 15:07:14
 *
 * Version Histories:
 *
 * 2.3 06/11/13-15:07:14 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.2 04/11/30-15:19:38 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:39:04 (suhler)
 *   version change
 *
 * 1.22 02/07/24-10:44:24 (suhler)
 *   doc updates
 *
 * 1.21 01/08/27-12:13:49 (suhler)
 *   doc changes
 *
 * 1.20 01/08/03-18:21:46 (suhler)
 *   remove training  ws from classnames before trying to instantiate
 *
 * 1.19 01/07/20-11:32:58 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.18 01/07/17-14:14:07 (suhler)
 *   Use MatchUrl
 *
 * 1.17 01/01/14-14:52:11 (suhler)
 *   make fields public to allow scripting
 *
 * 1.16 00/12/11-13:26:22 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.15 00/12/08-16:47:29 (suhler)
 *   added exitOnError flag
 *   .
 *
 * 1.14 00/11/15-09:46:58 (suhler)
 *   better error messages
 *
 * 1.13 00/04/12-15:52:00 (cstevens)
 *   configuration property "wrap" -> "handler"
 *   configuration property "filter" -> "filters"
 *
 * 1.12 00/03/29-14:30:04 (cstevens)
 *   Disable chunked encoding before requesting data that will be filtered.
 *
 * 1.11 00/03/10-17:01:08 (cstevens)
 *   Now that the HTTP response headers is a first-class object, rewrote
 *   TailHandler/FilterHandler so that it doesn't have to re-extract the
 *   headers from the output stream of the wrapped handler.
 *
 * 1.10 00/02/11-08:57:01 (suhler)
 *   more diagnostics
 *
 * 1.9 99/10/18-10:26:28 (suhler)
 *   remove diagnostics
 *
 * 1.8 99/10/11-12:33:16 (suhler)
 *   use new MimeHeader class
 *
 * 1.7 99/10/06-12:38:08 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.5.1.2 99/10/06-12:29:48 (suhler)
 *   use mime headers
 *
 * 1.6 99/10/01-11:27:21 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.5.1.1 99/09/17-13:55:24 (suhler)
 *   lint
 *
 * 1.5 99/09/01-15:31:15 (suhler)
 *   fix diagnostics
 *   handle handlers that return false
 *
 * 1.4 99/08/30-09:37:52 (suhler)
 *   redo to manage a list of filters (needs more testing)
 *
 * 1.3 99/08/06-08:31:12 (suhler)
 *   added call to filter's respond method
 *
 * 1.2 99/07/30-10:48:44 (suhler)
 *   added call to filter init method
 *
 * 1.2 99/07/29-16:17:21 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 filter/FilterHandler.java
 *   Name history : 1 0 tail/TailHandler.java
 *
 * 1.1 99/07/29-16:17:20 (suhler)
 *   date and time created 99/07/29 16:17:20 by suhler
 *
 */

package sunlabs.brazil.filter;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.server.ChainHandler;
import sunlabs.brazil.util.MatchString;

import sunlabs.brazil.util.http.MimeHeaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Properties;


/**
 * The <code>FilterHandler</code> captures the output of another 
 * <code>Handler</code> and allows the ouput to
 * be modified. One or more
 * {@link sunlabs.brazil.filter.Filter Filters}
 * may be specified to change that output
 * before it is returned to the client.
 * <p>
 * This handler provides one of the core services now associated with
 * the Brazil Server: the ability to dynamically rewrite web content
 * obtained from an arbitrary source.
 * <p>
 * For instance, the <code>FilterHandler</code> can be used as a proxy for
 * a PDA.  The wrapped <code>Handler</code> would go to the web to
 * obtain the requested pages on behalf of the PDA.  Then, a 
 * <code>Filter</code> would examine all "text/html" pages and rewrite the
 * pages so they fit into the PDA's 200 pixel wide screen.  Another
 * <code>Filter</code> would examine all requested images and dynamically
 * dither them to reduce the wireless bandwidth consumed by the PDA.
 * <p>
 * The following configuration parameters are used to initialize this
 * <code>Handler</code>: <dl class=props>
 *
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URL that triggers this handler.
 * (See {@link MatchString}).
 *
 * <dt> <code>handler</code>
 * <dd> The name of the <code>Handler</code> whose output will be captured
 *	and then filtered.  This is called the "wrapped handler".  
 *
 * <dt> <code>filters</code>
 * <dd> A list of <code>Filter</code> names.   The filters are applied in
 *	the specified order to the output of the wrapped handler.
 * <dt> <code>exitOnError</code>
 * <dd> If set, the server's <code>initFailure</code> will set
 *	any of the filters fail to 
 *	initialize.  No handler prefix is required.
 * </dl>
 *
 * A sample set of configuration parameters illustrating how to use this
 * handler follows:
 * <pre>
 * handler=filter
 * port=8081
 *
 * filter.class=sunlabs.brazil.filter.FilterHandler
 * filter.handler=proxy
 * filter.filters=noimg
 *
 * proxy.class=sunlabs.brazil.proxy.ProxyHandler
 *
 * noimg.class=sunlabs.brazil.filter.TemplateFilter
 * noimg.template=sunlabs.brazil.template.NoImageTemplate
 * </pre>
 * These parameters set up a proxy server running on port 8081.  As with a
 * normal proxy, this proxy server forwards all HTTP requests to the target
 * machine, but it then examines all HTML pages before they are returned to
 * the client and strips out all <code>&lt;img&gt;</code> tags.  By applying
 * different filters, the developer could instead build a server <ul>
 * <li> to automatically dither embedded images down to grayscale (instead
 *	of simply stripping them all out)
 * <li> to apply pattern recognition techniques to strip out only the
 *	advertisements
 * <li> to examine and change arbitrary URLs on the page
 * <li> to extract the content from an HTML page and dynamically combine it
 *	with another file to produce a different look-and-feel.
 * </ul>
 * See the description under
 * {@link sunlabs.brazil.server.Handler#respond responnd}
 *  for a more detailed explaination.
 * 
 * @author	Stephen Uhler (stephen.uhler@sun.com)
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version		2.3
 */

public class FilterHandler
    implements Handler
{
    private static final String HANDLER = "handler";
    private static final String FILTERS = "filters";

    String prefix;
    
    MatchString isMine;            // check for matching url
    public Handler handler;
    public Filter[] filters;

    /**
     * Start the handler and filter classes.
     */

    public boolean
    init(Server server, String prefix)
    {
	this.prefix = prefix;
	
	String str;

	Properties props = server.props;
	
	isMine = new MatchString(prefix, server.props);
	boolean exitOnError= (props.getProperty("exitOnError") != null);

	/*
	 * Start the handler to fetch the content.
	 */

	str = props.getProperty(prefix + HANDLER, "");
	handler = ChainHandler.initHandler(server, prefix + HANDLER + ".",
		str);
	if (handler == null) {
	    return false;
	}
	server.log(Server.LOG_DIAGNOSTIC, prefix, "using handler: " + str);

	/*
	 * Gather the filters.
	 */

	str = props.getProperty(prefix + FILTERS, "");
	StringTokenizer names = new StringTokenizer(str);
	Vector v = new Vector();
	while (names.hasMoreTokens()) {
	    String name = names.nextToken();
	    server.log(Server.LOG_DIAGNOSTIC, prefix, "using filter: " + name);

	    Filter f = initFilter(server, prefix, name);
	    if (f != null) {
		v.addElement(f);
	    } else if (exitOnError) {
		server.log(Server.LOG_ERROR, prefix,
			"filter: " + name + " didn't start");
		server.initFailure=true;
	    }
	}
	if (v.size() == 0) {
	    server.log(Server.LOG_DIAGNOSTIC, prefix, "no filters");
	    return false;
	}
	filters = new Filter[v.size()];
	v.copyInto(filters);
	return true;
    }

    private static Filter
    initFilter(Server server, String prefix, String name)
    {
	String className = server.props.getProperty(name + ".class");
	if (className == null) {
	    className = name;
	} else {
	    prefix = null;
	}
	if (prefix == null) {
	    prefix = name + ".";
	}

	try {
	    Filter f = (Filter) Class.forName(className.trim()).newInstance();
	    server.log(Server.LOG_DIAGNOSTIC, prefix, "Creating: " + className);
	    if (f.init(server, prefix)) {
		return f;
	    }
	    server.log(Server.LOG_WARNING, name, "did not initialize");
	} catch (ClassNotFoundException e) {
	    server.log(Server.LOG_WARNING, prefix, "no such class:" + e);
	} catch (IllegalArgumentException e) {
	    server.log(Server.LOG_WARNING, prefix, "Invalid argument" + e);
	} catch (ClassCastException e) {
	    server.log(Server.LOG_WARNING, prefix, "is not a Filter");
	} catch (Exception e) {
	    server.log(Server.LOG_WARNING, prefix, "error initializing");
	    e.printStackTrace();
	}
	return null;
    }

    /**
     * Responds to an HTTP request by the forwarding the request to the
     * wrapped <code>Handler</code> and filtering the output of that
     * <code>Handler</code> before sending the output to the client.
     * <p>
     * At several stages, the <code>Filters</code> are given a chance to
     * short-circuit this process: <ol>
     *
     * <li> Each <code>Filter</code> is given a chance to examine the
     * request before it is sent to the <code>Handler</code>
     * by invoking its <i>respond()</i> method.  The
     * <code>Filter</code> may decide to change the request's properties.  
     * A <code>Filter</code> may even return some content to the client now,
     * by (calling <code>request.sendResponse()</code> and
     * returning <i>true</i>),
     * in which case, neither the <code>Handler</code> nor any further
     * <code>Filter</code>s are invoked at all.
     *
     * <li> Assuming the <i>respond()</i> methods of all the filters
     * returned <i>false</i> (which is normally the case),
     * the <code>handler's respond()</code> 
     * method is called, and is expected to generate content.
     * If no content is generated at this step,
     *  this handler returns <i>false</i>.
     *
     * <li> After the <code>Handler</code> has generated the response headers,
     * but before it has generated any content, each <code>Filter</code> is
     * asked if it would be interested in filtering the content.  If no
     * <code>Filter</code> is, then the subsequent content from the
     * <code>Handler</code> will be sent directly to the client.
     *
     * <li> On the other hand, if any <code>Filter</code> <b>is</b> interested
     * in filtering the content, then the output of the <code>Handler</code>
     * will be sent to each of the interested <code>Filter</code>s in order.
     * The output of each interested <code>Filter</code> is sent to the
     * next one; the output of the final <code>Filter</code> is sent to
     * the client.
     * <li>At this point, any one of the invoked <code>Filter</code>s
     * can decide to reject the content completely, instead of rewriting it.
     * </ol>
     * See {@link Filter} for a description of how to cause filters
     * to implement the various behaviors defined above.
     *
     * @param	request
     *		The HTTP request to be forwarded to one of the sub-servers.
     *
     * @return	<code>true</code> if the request was handled and content
     *		was generated, <code>false</code> otherwise.  
     *
     * @throws	IOException
     *		if there was an I/O error while sending the response to
     *		the client. 
     */

    public boolean
    respond(Request request) throws IOException {
        if (!isMine.match(request.url)) {
            return false;
	}

	/*
	 * Let each filter get a crack at the request as a handler.
	 */

	for (int i = 0; i< filters.length; i++) {
	    if (filters[i].respond(request)) {
		return true;
	    }
	}

	/*
	 * Capture output from handler.  When the handler is done, run the
	 * filters.
	 */

	FilterStream out = new FilterStream(request.out);
	request.out = out;

	try {
	    if (handler.respond(request) == false) {
		request.log(Server.LOG_DIAGNOSTIC, prefix,
			"No output from handler - skipping filters");
		return false;
	    }
	    if (out.shouldFilter) {
		return out.applyFilters(request);
	    } else {
		/*
		 * handler.respond() has already sent the response.
		 */
		return true;
	    }
	} finally {
	    out.restore(request);
	}
    }

    private class FilterStream
	extends Request.HttpOutputStream
    {
	boolean shouldFilter;
	Request.HttpOutputStream old;

	int count;
	Filter[] postFilters;

	public
	FilterStream(Request.HttpOutputStream old)
	{
	    super(new ByteArrayOutputStream());

	    this.old = old;
	}

	/**
	 * Check if any of the filters want to filter the data, based on
	 * what's in the HTTP headers.  If none of them do, then we don't
	 * have to filter the data at all, so restore the request's original
	 * output stream.
	 */

	public void
	sendHeaders(Request request)
	    throws IOException
	{
	    postFilters = new Filter[filters.length];
	    
	    for (int i = 0; i < filters.length; i++) {
		Filter f = filters[i];
		if (f.shouldFilter(request, request.responseHeaders)) {
		    postFilters[count++] = f;
		}
	    }

	    if (count == 0) {
		/*
		 * Based on the HTTP response headers, no filters want to
		 * process the content, so restore orginal output stream.
		 */

		request.log(Server.LOG_DIAGNOSTIC, prefix, 
			"no filters activated");
		restore(request);
		old.sendHeaders(request);
	    } else {
		/*
		 * Disable chunked encoding, so we get the content as bytes
		 * not as chunks.  We want only bytes so we can later apply
		 * the filters.
		 */

		request.version = 10;
		shouldFilter = true;
	    }
	}

	public boolean
	applyFilters(Request request)
	    throws IOException
	{
	    request.out.flush();
	    restore(request);

	    byte[] content = ((ByteArrayOutputStream) out).toByteArray();
	    for (int i = 0; i < count; i++) {
		content = postFilters[i].filter(request,
			request.responseHeaders, content);
		if (content == null) {
		    return false;
		}
	    }

	    request.sendResponse(content, null);
	    return true;
	}

	public void
	restore(Request request)
	{
	    request.out = old;
	}
    }
}
