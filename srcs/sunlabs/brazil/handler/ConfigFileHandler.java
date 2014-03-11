/*
 * ConfigFileHandler.java
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
 * Contributor(s): cstevens, rinaldo, suhler.
 *
 * Version:  2.2
 * Created by suhler on 99/06/28
 * Last modified by suhler on 04/08/30 09:02:07
 *
 * Version Histories:
 *
 * 2.2 04/08/30-09:02:07 (suhler)
 *   "enum" became a reserved word, change to "enumer".
 *
 * 2.1 02/10/01-16:36:34 (suhler)
 *   version change
 *
 * 1.35 02/07/24-10:45:56 (suhler)
 *   doc updates
 *
 * 1.34 01/12/10-16:11:12 (suhler)
 *   doc lint
 *
 * 1.33 01/05/18-09:11:40 (suhler)
 *   work with either CookieSessionHAndler -or- SessionFilter
 *
 * 1.32 01/01/22-19:02:30 (cstevens)
 *   Remove RechainableProperties from Request.  In order to insert shared
 *   Properties objects, use request.addSharedProps() instead of manually
 *   rechaining the properties using props.setDefaults().
 *
 * 1.31 00/12/11-13:28:21 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.30 00/11/20-13:28:06 (suhler)
 *   lint
 *   D
 *
 * 1.29 00/11/20-13:21:35 (suhler)
 *   doc fixes
 *
 * 1.28 00/10/05-11:06:46 (suhler)
 *   The property "glob" can be used to match a patterns of request.props
 *   that may be set by the user that are NOT in the default config file.  If
 *   "glob" is specified, no default file is required.
 *
 * 1.27 00/05/31-13:47:26 (suhler)
 *   doc cleanup
 *
 * 1.26 00/05/24-11:35:36 (suhler)
 *   added a "copy" property to implement old behavior for backward compatibility
 *
 * 1.25 00/05/22-14:03:48 (suhler)
 *   doc updates
 *
 * 1.24 00/05/15-12:04:29 (suhler)
 *   added chaining back in, removing (impossible) behind option
 *   added more diags
 *
 * 1.23 00/05/12-15:25:10 (suhler)
 *   temporarily back out properties chaining
 *
 * 1.22 00/05/10-10:54:06 (suhler)
 *   No longer add config properties directly to request.props.
 *   Instead, use the ChainedProperties.
 *   added "noOverride" option to prevent properties from ovverriding
 *   values that already exist
 *
 * 1.21 00/04/20-11:50:15 (cstevens)
 *   copyright.
 *
 * 1.20 00/04/12-15:53:16 (cstevens)
 *   imports
 *
 * 1.19 00/03/29-14:35:55 (cstevens)
 *   pedantic use of Request.getQueryData()
 *
 * 1.18 00/02/11-12:42:44 (suhler)
 *   arguments to Match wrong
 *
 * 1.17 00/02/03-14:24:40 (suhler)
 *   added "no content" option
 *
 * 1.16 99/12/15-12:02:39 (suhler)
 *   Properties we not being passed on if a "set" was atempted, and no
 *   values were changed
 *
 * 1.15 99/12/07-10:55:17 (suhler)
 *   set request properties *after* processing SET query parameters
 *
 * 1.14 99/11/30-09:47:38 (suhler)
 *   Redo.  Only generate session based files when really needed
 *
 * 1.12.1.1 99/11/14-10:34:55 (rinaldo)
 *   Fix Error with Mime Type
 *
 * 1.13 99/11/01-11:56:37 (suhler)
 *   ConfigFile handler gets root property at each request
 *
 * 1.12 99/10/26-17:11:10 (cstevens)
 *   Get rid of public variables Request.server and Request.sock:
 *   A. In all cases, Request.server was not necessary; it was mainly used for
 *   constructing the absolute URL for a redirect, so Request.redirect() was
 *   rewritten to take an absolute or relative URL and do the right thing.
 *   B. Request.sock was changed to Request.getSock(); it is still rarely used
 *   for diagnostics and logging (e.g., ChainSawHandler).
 *
 * 1.10.1.1 99/10/23-19:47:18 (rinaldo)
 *
 * 1.11 99/10/11-12:30:44 (suhler)
 *   change POST to GET when done processing.
 *
 * 1.10 99/10/06-12:28:41 (suhler)
 *   fix log
 *
 * 1.9 99/10/06-12:20:32 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.8 99/10/01-11:26:36 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.7.1.2 99/09/27-17:47:30 (suhler)
 *   Entirely new semantics.  This will break all existing uses:
 *   - the "config" file is always merged into the request properties
 *   - a separate url is used to set the properties
 *   - a default properties file is required, and the existing properties limit
 *   what is allowed to be set.
 *
 * 1.7.1.1 99/09/10-11:02:53 (suhler)
 *   ???
 *
 * 1.7 99/08/06-12:11:06 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.5.1.2 99/08/06-12:05:18 (suhler)
 *   use glob in util directory
 *   ,
 *   .
 *   D
 *
 * 1.5.1.1 99/08/04-18:44:31 (suhler)
 *   Changes to allow session specific file names
 *
 * 1.6 99/07/27-22:02:32 (rinaldo)
 *   Stephen, you will probably not like what I did but it is late and I have to
 *   have the properties I just set be available to all sunsequent url requests tak
 *   a look at XXX.
 *
 * 1.5 99/07/14-11:37:20 (rinaldo)
 *   Allow absolute URLS.
 *
 * 1.4 99/07/13-09:37:41 (suhler)
 *   look up the redirect property in the form/query data
 *
 * 1.3 99/06/29-14:38:20 (suhler)
 *   use new redirector
 *
 * 1.2 99/06/29-09:37:12 (suhler)
 *   re-wrote config handler to be more useful.
 *   It now works in conjuction with the form template handler
 *   see config.config for an example
 *   .
 *
 * 1.2 99/06/28-10:46:26 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/ConfigFileHandler.java
 *
 * 1.1 99/06/28-10:46:25 (suhler)
 *   date and time created 99/06/28 10:46:25 by suhler
 *
 */

package sunlabs.brazil.handler;

import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Glob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Handler for manipulating per-user session state that can be
 * represented as ASCII name/value pairs.  The state for each session
 * is stored in a file, which is expected to be in java properties format.
 *
 * If "prefix" is matched, the contents of the (usually cached) config file
 * for the current session is added to the request properties.
 *
 * If the url matches the "set" property, the contents of the config
 * file are changed based on the supplied query parameters (either GET
 * of POST).  If no config file exists for the session, one is created
 * from a default properties file.  Only properties already in the
 * config file may be changed using the "set" method.
 *
 * If a "%" is specified in the file name, it is replaced by the
 * SessionID property, if any, or "common" if sessions aren't used.
 * This should be replaced with something more general, so we can have
 * more arbitrary mappings between request and the session info.
 * <p>
 * The following request properties are used:
 * <dl class=props>
 * <dt>prefix	<dd> The URL prefix required for all documents
 * <dt>set	<dd> The url pattern to match setting properties.
 *		     Currently, it must also match "prefix".
 * <dt>noContent<dd> a url, matching the "set" pattern that causes
 *		     a "204 no content" to be returned  to the client
 *			(experimental).
 * <dt>name	<dd> The name of the config file. the first "%" is replaced
 *		     by the current SessionID.
 * <dt>default	<dd> The default properties file to "seed" session properties
 * <dt>glob	<dd> Properties that match this "glob" pattern may be set
 *		     using the "set" pattern.  If this property is specified,
 *		     the "default" property is optional.
 * <dt>root	<dd> The document root (no properties prefix required). If the
 *		     "name" or "default" properties don't start with a "/", 
 *		     this is used as the current directory.
 *		   
 * </dl>
 * If "%" is specified in the file name, a new session file is 
 * created only if 1) a property is changed from the default, and 2)
 * A cookie was received by the browser.
 * <p>
 * See also: {@link sunlabs.brazil.template.SetTemplate}
 * which is preferrable in most cases, providing a templated based
 * (instead of URL based) 
 * mechanism for maintaining persistent properties.
 *
 * @author		Stephen Uhler
 * @version		2.2, 04/08/30
 */

public class ConfigFileHandler implements Handler {
    static final String SET = "set";   // glob matching url setting props
    static final String PREFIX = "prefix";   // prefix for adding to props
    static final String NAME = "name";     // name of the config file
    static final String DEFAULT = "default";    // the default config file
    static final String ROOT = "root";    // root of config file
    static final String GLOB = "glob";  

    String propsPrefix;
    String name;	// name of the config file
    String urlPrefix;	// prefix for any request we handle
    String set;	// glob pattern to match url needed to modify props
    String nc;	// glob pattern to match url needed for no content
    String match; // glob pattern to match settable props not in deflt file
    String root;	// where to find the root of the config file
    Properties defaultProperties;	// where to find the defaults
    boolean copy = false;	// undocumented backwart compat flag

    /**
     * Make sure default properties exist before starting this handler, 
     * or that "match" is specified".
     */

    public boolean
    init(Server server, String prefix) {
	propsPrefix = prefix;
	urlPrefix = server.props.getProperty(prefix + PREFIX, "/");
	set = server.props.getProperty(prefix + SET, urlPrefix + prefix);
	nc = server.props.getProperty(prefix + "noContent", "X");
	match = server.props.getProperty(prefix + GLOB);
	name = server.props.getProperty(prefix + NAME, prefix + ".cfg");
	copy = (server.props.getProperty(prefix + "copy") != null);
	root = server.props.getProperty(ROOT, ".");
	String defaultName = server.props.getProperty(prefix + DEFAULT);
	File defaultFile = null;
	if (defaultName != null) {
	    if (defaultName.startsWith("/")) {
		defaultFile = new File(defaultName);
	    } else {
		defaultFile = new File(root, defaultName);
	    }
	}
	defaultProperties = new Properties();
	try {
	    FileInputStream in = new FileInputStream(defaultFile);
	    defaultProperties.load(in);
	    in.close();
	} catch (IOException e) {
	    if (match == null) {
		server.log(Server.LOG_WARNING, prefix, "No default file: " +
		defaultFile + " ConfigFileHandler NOT installed!");	
		return false;
	    }
	}
	server.log(Server.LOG_DIAGNOSTIC, prefix, "\n  set=" + set +
		"\n  name=" + name + "\n  default=" + defaultFile);
	return true;
    }

    /**
     * Extract the session state into the request object, optionally
     * modifying the properties.  If the properties are modified,
     * they are stored in a file for safe keeping.
     */

    public boolean
    respond(Request request) throws IOException {
	if (!request.url.startsWith(urlPrefix)) {
	    return false;
	}

	/*
	 * Get the existing properties.
	 */

	root = request.props.getProperty(ROOT, root);
	String id = request.props.getProperty("SessionID", "common");
	Properties p = (Properties) SessionManager.getSession(id,
		propsPrefix, Properties.class);

	/*
	 * If no properties exist, read in the session
	 * properties, otherwise use the default properties
	 */

	if (p.isEmpty()) {
	    File file = getPropsFile(name, id);
	    request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
		    "No properties, looking in: " + file);
	    try {
		FileInputStream in = new FileInputStream(file);
		p.load(in);
		in.close();
	    } catch (IOException e) {
		request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
		    "Can't find config file: " +
		    file + " using default properties");	
		Enumeration enumer = defaultProperties.propertyNames();
		while(enumer.hasMoreElements()) {
		    String key = (String) enumer.nextElement();
		    p.put(key, defaultProperties.getProperty(key));
		}
	    }
	}

	/*
	 * If "SET" then update the properties file
	 */

	if (Glob.match(set, request.url)) {
	    Dictionary update = request.getQueryData(null);
	    // System.out.println("Setting from: " + update);
	    Enumeration enumer = update.keys();
	    boolean changed = false;
	    synchronized (p) {
		while(enumer.hasMoreElements()) {
		    String key = (String) enumer.nextElement();
		    if (p.containsKey(key) || isMatch(key)) {
			p.put(key, update.get(key));
			changed = true; // XXX not quite
		    } else {
			request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
			    "Can't set key " + key +
			    ", name doesn't exist in default file" +
			    " or doesn;t match: " + match);
		    }
		}
		if (!changed) {
		    request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
			    "Set called, nothing changed!!");
		}

		/*
		 * If the client has cookies enabled, try to save the
		 * properties.  This depends upon a side effect of the
		 * "sessionHandler" to let us know if the browser
		 * actually sent us our cookie.
		 */

		if (changed && (name.indexOf("%") < 0 ||
			request.props.containsKey("UrlID") ||
			request.props.containsKey("gotCookie"))) {
		    File file = getPropsFile(name, id);
		    try {
			FileOutputStream out = new FileOutputStream(file);
			p.save(out, request.serverUrl() + request.url +
			    " (from ConfigFileHandler: " + propsPrefix + ")");
			out.close();
			request.log(Server.LOG_DIAGNOSTIC, propsPrefix,
			    "Saving configuration properties to " + file);
		    } catch (IOException e) {
			request.log(Server.LOG_WARNING, propsPrefix,
				"Can't save properties to: " + file);
		    }
		}
	    }

	    /*
	     * If this is a post, trash the input and change to a get.
	     * XXX This is probably incorrect.
	     */

	    if (request.method.equals("POST")) {
		request.method = "GET";
		request.postData = null;
		request.headers.put("Content-Length", "0");
	    }	
	}

	if (copy) {
	    Enumeration enumer = p.keys();
	    while(enumer.hasMoreElements()) {
		String key = (String) enumer.nextElement();
		request.props.put(key, p.getProperty(key));
	    }
	} else {
	    request.addSharedProps(p);
//	    request.log(Server.LOG_DIAGNOSTIC, propsPrefix, "Chaining " +
//		    p + " onto request.props");
	}
	return false;
    }

    /**
     * True if key "s" matches glob pattern in "match".  If
     * match is null, then no match.
     */

    private boolean
    isMatch(String s) {
	return (match != null && Glob.match(match, s));
    }

    /**
     * Find the config file name
     */

    private File
    getPropsFile(String name, String id) {
	int index;
	if ((index = name.indexOf("%")) >= 0) {
	    name = name.substring(0,index) + id + name.substring(index+1);
	}
	File file;
	if (name.startsWith("/")) {
	    file = new File(name);
	} else {
	    file = new File(root, name);
	}
	return file;
    }
}
