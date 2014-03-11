/*
 * FetchTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2004-2006 Sun Microsystems, Inc.
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
 * Created by suhler on 04/04/02
 * Last modified by suhler on 06/08/02 13:45:54
 *
 * Version Histories:
 *
 * 1.5 06/08/02-13:45:54 (suhler)
 *   changed name to FetchTemplate, removed some debugging
 *   .
 *
 * 1.4 05/05/19-13:05:41 (suhler)
 *   only print stack trace if log > 3
 *
 * 1.3 04/05/24-15:24:15 (suhler)
 *   doc fixes
 *
 * 1.2 04/04/28-14:52:55 (suhler)
 *   doc cleanup
 *
 * 1.2 04/04/02-16:23:15 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 sunlabs/FetchTemplate.java
 *   Name history : 1 0 sunlabs/IncludeTemplate.java
 *
 * 1.1 04/04/02-16:23:14 (suhler)
 *   date and time created 04/04/02 16:23:14 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.server.Server;

/**
 * Template class for substituting html pages into an html page.
 * This class is used by the TemplateHandler.  This does not perform
 * traditional server-side include processing, whose normal purpose is to
 * include standard headers and footers.  That functionality is provided
 * instead by including the content into the template, and not the other
 * way around.
 * <p>
 * This include incorporates entire pages from other sites, either directly 
 * as part of the page content, or into a property, for further processing.
 * <p>
 * <b>NOTE:</b> This version depends on the behavior of JDK1.4.  It does not
 * use the Brazil Request class, but the URL class instead.  Thus
 * it has advantages that it automatically follows some redirects, and can
 * handle additional protocols, such as "https" and "ftp", but it does
 * not cache connections, and the proxy may not be set of a per request
 * basis. Attributes:
 * <dl class=attributes>
 * <dt>href<dd>Absolute url to fetch, and insert here
 * <dt>post<dd>Post data if any.  If set, an http POST is issued.
 * The post data is expected to be in www-url-encoded format.
 * <dt>alt<dd>Text to insert if URL can't be obtained.
 * <dt>name<dd>The name of the variable to put the result in.
 * If this is specified, the content is not included in place.
 * <dt>proxy<dd>The proxy:port to use as a proxy (if any).
 * If specified, it overrides the <code>proxy</code> property, 
 * in <code>request.props</code>.
 * <dt>getheaders<dd>The name of the variable prefix to use to
 * extract the http response headers.
 * If this not specified, no response headers are retrieved.
 * The result will be properties of the form:
 * <code>[getheaders].[header_name]=[header_value]</code>.
 * If multiple entries exist for a particular header name, the values
 * are combined as per HTTP conventions (e.g. v1, v2, ... vn).
 * The pseudo header <code>status</code>
 * will contain the http status line.
 * </dl>
 * Note: This used to be called the IncludeTemplate, but was
 * renamed to avaoid confusion with the other IncludeTemplate.
 *
 * @author		Stephen Uhler
 * @version		@(#)FetchTemplate.java	1.5
 */

public class FetchTemplate extends Template {

    static {
        HttpURLConnection.setFollowRedirects(true);
	// System.out.println("Initing FetchTemplate");
    }

    public void
    tag_include(RewriteContext hr) {
	String href = hr.get("href");	// the url to fetch
	String alt = hr.get("alt");	// the result if fetch failed
	String name = hr.get("name");	// the variable to put the result in
	String post = hr.get("post");	// post data (if any)
	String getheaders = hr.get("getheaders"); // retrieve response headers
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

	try {
            URL target = new URL(href);
            HttpURLConnection con = (HttpURLConnection) target.openConnection();
	    // System.out.println("Fetching: " + con.getFollowRedirects()  + "/" +  con.getInstanceFollowRedirects() + " " +target);
            // con.setInstanceFollowRedirects(true);
	    con.addRequestProperty("Via", "Brazil-Fetch/2.1");

	    if (post != null) {
		con.setDoOutput(true);
	        con.setRequestMethod("POST");
		OutputStream out = con.getOutputStream();
		// System.out.println("Writing post data");
		out.write(post.getBytes());
		// System.out.println("Writing post data DONE");
		out.close();
	    } else {
	        con.setRequestMethod("GET");
	    }
	    con.connect();
	    if (getheaders != null) {
		Map headers = con.getHeaderFields();
		Set s = headers.entrySet();
		Iterator it = s.iterator();
	        while(it.hasNext()) {
		    Map.Entry e = (Map.Entry) it.next();
		    String base = (String) e.getKey();
		    if (base == null) {
			continue;
		    }
		    String key = getheaders + "." + base.toLowerCase();
		    List l = (List) e.getValue();
		    if (l.size() == 1) {
		        hr.request.props.put(key, "" + l.get(0));
		    } else if (l.size() > 1) {
			String build = l.get(0).toString();
			for(int i=1;i<l.size();i++) {
			    build += ", " + l.get(i);
			}
		        hr.request.props.put(key, "" + build);
		    }
	        }
		hr.request.props.put(getheaders + ".status",
			"" + con.getResponseCode());
	    }
	    InputStream in = con.getInputStream();
	    ByteArrayOutputStream bo = new ByteArrayOutputStream();
	    int i;
	    while ((i=in.read()) != -1) {
	       bo.write(i);
	    }
	    in.close();
	    String data = bo.toString();

	    if (name!=null) {
		hr.request.props.put(name, data);
	    } else {
		hr.append(data);
	    }
	} catch (IOException e) {
	    System.out.println("Oops: " + e);
	    if (hr.server.logLevel > Server.LOG_LOG) {
	        e.printStackTrace();
	    }
	    if (name != null) {
		hr.request.props.put(hr.prefix + name, alt);
		hr.request.props.put(hr.prefix + "error", e.getMessage());
	    } else {
	        hr.append("<!-- " + e + " -->" + alt);
	    }
	}
	hr.killToken();
    }
}
