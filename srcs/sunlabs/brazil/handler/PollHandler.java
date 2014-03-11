/*
 * PollHandler.java
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
 * The Initial Developer of the Original Code is: suhler.
 * Portions created by suhler are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): suhler.
 *
 * Version:  2.5
 * Created by suhler on 00/12/13
 * Last modified by suhler on 06/12/11 14:48:12
 *
 * Version Histories:
 *
 * 2.5 06/12/11-14:48:12 (suhler)
 *   - allow relative url's
 *   - fix code to match the documentation
 *
 * 2.4 04/08/30-09:02:15 (suhler)
 *   "enum" became a reserved word, change to "enumer".
 *
 * 2.3 02/11/04-14:01:40 (suhler)
 *   oops
 *
 * 2.2 02/11/04-13:55:55 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.15.1.1 02/11/04-13:55:38 (suhler)
 *   move check for redundant poll into the right place
 *
 * 2.1 02/10/01-16:36:23 (suhler)
 *   version change
 *
 * 1.15 02/07/30-16:21:22 (suhler)
 *   - fixed bug in then==now computation
 *   - code reordering
 *
 * 1.14 02/07/29-15:29:43 (suhler)
 *   make sure a "host" header is present
 *
 * 1.13 02/07/11-15:02:27 (suhler)
 *   changed interface to fillProps to take an HttpRequest object instead of
 *   an input stream
 *
 * 1.12 02/07/10-11:26:23 (suhler)
 *   add note to indicate future encoding option
 *
 * 1.11 02/06/13-17:56:13 (suhler)
 *   don't do interval checking at init time
 *
 * 1.10 02/04/18-11:09:53 (suhler)
 *   add ${..} evaluation of url's
 *
 * 1.9 02/02/26-14:24:54 (suhler)
 *   - made "namespace" a separate configuration parameter (it was convolved with
 *   "prepend")
 *   - added "headers" parameter to specify additional http headers added to each
 *   polled request
 *
 * 1.8 01/11/21-11:42:54 (suhler)
 *   doc fixes
 *
 * 1.7 01/08/13-16:22:55 (suhler)
 *   added "format" option to spedify format for "Cron" matching of date/times
 *
 * 1.6 01/08/01-17:14:31 (suhler)
 *   Allow properties to be extracted into a sessionmanager session,
 *   accessable via <set namespace=...>
 *
 * 1.5 01/07/10-17:26:48 (suhler)
 *   make url, post, and interval public, so they may be
 *   modified from python or tcl
 *
 * 1.4 01/06/04-11:04:11 (suhler)
 *   allow "post" input
 *
 * 1.3 01/03/06-09:05:29 (suhler)
 *   Add:
 *   - talk through proxy
 *   - better error diagnostics
 *   - more robust behavior in the face of errors
 *
 * 1.2 00/12/14-17:48:28 (suhler)
 *   added cron like capability, allowing a url fetch based on a schedule
 *
 * 1.2 00/12/13-12:16:37 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/PollHandler.java
 *
 * 1.1 00/12/13-12:16:36 (suhler)
 *   date and time created 00/12/13 12:16:36 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.SimpleTimeZone;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.http.HttpRequest;
import sunlabs.brazil.util.http.HttpUtil;
import sunlabs.brazil.util.regexp.Regexp;
import sunlabs.brazil.session.SessionManager;

/**
 * Handler for periodically polling another web site, whose
 * results are (optionally) added to the server's properties.
 * This also includes the ability to request URL's on a cron-like schedule.
 * <p>
 * The result of fetching the url is expected to be a text document in
 * java Properties format. 
 * <p>
 * Properties:
 * <dl class=props>
 * <dt>url	<dd>URL to fetch periodically.
 *		any ${...} constructs are evaluated at each poll, with
 *		the values in the server properties object. If the URL
 *		starts with "/", then the current server is used.
 * <dt>post	<dd>The "post" data, if any. ${...} are evaluates as per
 *		<code>url</code> above.
 * <dt>headers	<dd>A list of white space delimited tokens that refer to
 *		additional HTTP headers that are added onto the polled
 *		request.  For each token the server properties
 *		<code>[token].name</code> and <code>[token].value</code>
 *		define a new http header.
 * <dt>interval	<dd>The interval (in seconds) to fetch the url.  Defaults
 *		to 10 seconds. If <i>match</i> is specified, this is the
 *		interval used to check for a time/date match.
 *              At each "interval", the current time format is computed, based
 *		on "format", below.  If the computed format has not
 *		changed since the previous poll, then no poll is done.
 *		The interval is recalculated after each poll.
 * <dt>fast     <dd>If set, don't wait "interval" before 1st poll.
 * <dt>prepend  <dd>The string to prepend to the properties.
 *		If not supplied	no properties are loaded.
 * <dt>namespace<dd>The namespace to use to store the properties to.
 *		If the <code>sessionTable</code> (see below)parameter is 
 *		identical to the <code>sessionTable</code> parameter of
 *		the <code>SetTemplate</code>, then this specifies the
 *		<code>namespace</code> parameter that may be used with
 *		the <code>SetTemplate</code> "namespace" parameter to
 *		obtain the extracted data.  Defaults to the "prepend" parameter.
 * <dt>match	<dd>If specified, a regular expression that must match
 *	        the current time for this URL to run.  The format to match is
 *		specified by the "format" parameter, below.
 *		"EEE-dd-HH-mm" (eg: Thu-Dec, 14, 14:12 pm).
 * <dt>format	<dd>a {@link java.text.SimpleDateFormat date format specifier}
 *		to use for matching "match"
 *		patterns.  Defaults to "EE-MM-dd-HH-mm".
 * <dt>proxy	<dd>If specified, connect through a proxy. This should be
 *		in the form <code>host:port</code>, or <code>host</code>
 *		it the desired port is <code>80</code>.
 * <dt>sessionTable
 * <dd>The name of the SessionManager table to use for
 *     storing values.  By default, properties are stored in
 *     server.props.  The value should match the <code>sessionTable</code>
 *     used by the {@link sunlabs.brazil.template.SetTemplate} to allow
 *     values obtained by this handler to be accessable from within
 *     templates.
 *     <p>
 *     If the <code>sessionTable</code> is set, the <code>namespace</code>
 *     value is used to name the table (e.g. the <code>namespace</code>
 *     specified by {@link sunlabs.brazil.template.SetTemplate}.  If no
 *     namespace parameter is given, then <code>prepend</code> is used
 *     as the namespace parameter.
 * </dl>
 * <p>
 * If prepend is specified, the following additional
 * properties are
 * created, and added to the properties with the specified prefix.
 * <dl class=props>
 * <dt>count.attempts	<dd>The total number of polls attemped.
 * <dt>count.errors	<dd>The total number of poll failures.
 * <dt>error.at		<dd>The poll attempt # for the last failure.
 * <dt>error.msg	<dd>The message describing the last failure.
 * <dt>error.time	<dd>The timestamp of the last failure.
 * <dt>timestamp	<dd>The timestamp for the last successful poll.
 * </dl>
 *
 * @author      Stephen Uhler
 * @version	%V% PollHandler.java 2.5 
 */

public class PollHandler extends Thread implements Handler {
    public String url;		// url to fetch
    public String post;	// post data to send
    public int interval;	// how often to fetch it (ms)
    long pollCount = 0;	// # of times polled
    long errorCount = 0;	// # of times polls failed
    Server server;	// our server.
    String prefix;	// our prefix
    String prepend;	// our prefix in the props table
    String namespace;	// our namespace in the session manager
    Regexp re;		// the re to match the date/time expression
    String then;	// Last time we processed this url
    String proxyHost=null;	// The proxyhost to use (if any)
    String sessionTable;	// the "other" session key
    SimpleDateFormat date;	// date format for "cron" matching
    int proxyPort = 80; 	// The proxyport
    String tokens;		// our tokens in server.props
    String match;
    boolean fast;		// make first poll withoug waiting

    /**
     * Set up the initial configuration, and kick off a thread
     * to periodically fetch the url.
     */

    public boolean
    init(Server server, String prefix) {
	this.server = server;
	this.prefix = prefix;

	prepend = server.props.getProperty(prefix + "prepend");
	namespace = server.props.getProperty(prefix + "namespace", prepend);
	url = server.props.getProperty(prefix + "url");
	post = server.props.getProperty(prefix + "post");
	tokens = server.props.getProperty(prefix + "headers");
	fast= (server.props.getProperty(prefix + "fast") != null);
	sessionTable = server.props.getProperty(prefix + "sessionTable");

	String proxy = server.props.getProperty(prefix + "proxy");
	if (proxy != null) {
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
	    server.log(Server.LOG_DIAGNOSTIC, prefix, "Proxy to: " +
		proxyHost + ":" + proxyPort);
	}

	server.log(Server.LOG_DIAGNOSTIC, prefix, "Polling: " +  url);

	String format = server.props.getProperty(prefix + "format",
	   "EE-MM-dd-HH-mm");
	date = new SimpleDateFormat(format, Locale.US);
        date.setTimeZone(SimpleTimeZone.getDefault());

	if (url == null) {
	    server.log(Server.LOG_WARNING, prefix, "Invalid url");
	    return false;
	}

        /*
         * work on the match stuff
	 */

	match = server.props.getProperty(prefix + "match");
	re = null;
	if (match != null) {
	    try {
		re = new Regexp(match, true);
	    } catch (Exception e) {
		server.log(Server.LOG_WARNING, prefix, "Bad expression:" + e);
	    }
	}

	Thread poller = (Thread) this;
        poller.setDaemon(true);
        poller.start();
        return true;
    }

    /**
     * This might allow control over the polling via requests at a later date.
     * For now, it always returns false.
     */

    public boolean
    respond(Request request) {
        return false;
    }

    /**
     * Periodically poll the url, and copy the results into the server
     * properties.
     */

    public void
    run() {
	Properties props;
	if (sessionTable != null) {
	    props = (Properties) SessionManager.getSession(namespace,
		    sessionTable, Properties.class);
	    props.list(System.out);
	} else {
	    props = new Properties();
	}
	boolean first = fast;
        while(true) {
	    interval=10;
	    try {
	        String str = server.props.getProperty(prefix + "interval");
	        interval = Integer.decode(str).intValue();
	    } catch (Exception e) {}
	    if (interval < 1) {
		interval=10;
	    }
	    server.log(Server.LOG_DIAGNOSTIC, prefix, "Sleeping: " +
			interval);
	    try {
	        Thread.sleep(first ? 2 : interval * 1000);
	    } catch (InterruptedException e) {
	        break;
	    }
            first = false;

	    if (re != null) {
		String now = date.format(new Date(System.currentTimeMillis()));
		server.log(Server.LOG_DIAGNOSTIC, prefix, "Checking: " +
			now + " matches " + match);
		if (now.equals(then)) {
		    server.log(Server.LOG_DIAGNOSTIC, prefix,
			    "   Already polled at: " +  now);
		    continue;
		}
		if (re.match(now) == null) {
		    server.log(Server.LOG_DIAGNOSTIC, prefix, now +
			" match failed");
		   continue;
	        }
	    then = now;
	    }

	    url = Format.subst(server.props, url);
	    if (url.startsWith("/")) {
	       int port = server.listen.getLocalPort();
	       url = server.protocol + "://" + server.hostName +
			(port != 80 ? ":" + port : "") + url;
	    }
	    server.log(Server.LOG_DIAGNOSTIC, prefix, "Polling: " +  url);
	    pollCount++;
	    if (sessionTable == null) {
	        props.clear();
	    }
	    HttpRequest target = new HttpRequest(url);
	    if (proxyHost != null) {
		target.setProxy(proxyHost, proxyPort);
	    }
	    if (tokens != null) {
		target.addHeaders(tokens, server.props);
	    }
	    target.requestHeaders.putIfNotPresent("Host",
		    HttpUtil.extractUrlHost(url));
	    try {
		if (post != null) {
		    OutputStream out = target.getOutputStream();
		    String data = Format.subst(server.props, post);
		    out.write(data.getBytes()); // XXX choose encoding here
		    out.close();
		}
		target.connect();
		if (prepend != null) {
		    int code = target.getResponseCode();
		    if (code == 200) {
		        fillProps(props, target);
		    } else {
		        throw new Exception("Request failed, code: " + code);
		    }
		}
	    } catch (Exception e) {
	        server.log(Server.LOG_DIAGNOSTIC, prefix, "target error: " + e);
	        errorCount++;
		if (prepend != null) {
	            props.put(prepend + "error.msg", e.getMessage());
	            props.put(prepend + "error.time",
			    "" + System.currentTimeMillis());
	            props.put(prepend + "error.at", "" + pollCount);
		}
	    } finally {
	        target.close();
	    }
	    server.log(Server.LOG_DIAGNOSTIC, prefix, " found " + props.size() +
		" items");
	    if (prepend != null) {
	        props.put("timestamp", "" + System.currentTimeMillis());
	        props.put("count.attempts", "" + pollCount);
	        props.put("count.errors", "" + errorCount);
	    }

	    /*
	     * Copy the properties into the server.
	     */

	    if (prepend != null && sessionTable == null) {
		Enumeration enumer = props.propertyNames();
		while(enumer.hasMoreElements()) {
		    String key = (String) enumer.nextElement();
		    server.props.put(prepend + key, props.getProperty(key));
		}
	    }
	}
    }

    /**
     * Fill the properties from the input stream
     */

    public void
    fillProps(Properties props, HttpRequest target)  throws IOException {
	InputStream in = target.getInputStream();
	props.load(in);
	in.close();
    }
}
