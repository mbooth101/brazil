/*
 * SimpleSessionHandler.java
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
 * Created by suhler on 01/08/01
 * Last modified by suhler on 06/11/16 15:44:13
 *
 * Version Histories:
 *
 * 2.5 06/11/16-15:44:13 (suhler)
 *   bug fix: "value" configuration parameter wasn't being initialized properly
 *
 * 2.4 06/11/13-15:04:19 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.3 05/06/22-13:42:42 (suhler)
 *   - cleaned up the documentations
 *   - don't subst \n's in value initially, so we don't need to \ the \'s
 *
 * 2.2 04/12/15-12:38:02 (suhler)
 *   make inner class static
 *
 * 2.1 02/10/01-16:36:31 (suhler)
 *   version change
 *
 * 1.10 02/06/13-17:57:56 (suhler)
 *   Changed the behaviour of extract= to look first in http headers, then
 *   in request props
 *
 * 1.9 02/06/11-17:47:05 (suhler)
 *   add option to invert the sense of the re match
 *
 * 1.8 02/03/05-12:42:40 (suhler)
 *   allow existing session id's to be modified
 *
 * 1.7 02/02/27-15:06:16 (suhler)
 *   add additional variables for session re's to match on
 *
 * 1.6 02/02/26-14:41:29 (suhler)
 *   doc fixes
 *
 * 1.5 02/02/26-13:37:17 (suhler)
 *   rewrite.
 *   Hopefully this one is easier to use.
 *   .
 *
 * 1.4 01/12/10-16:11:20 (suhler)
 *   doc lint
 *
 * 1.3 01/10/06-17:42:03 (suhler)
 *   Added options for "thin client" (Yopy) demo, to allow client-based
 *   authentication using http headers
 *
 * 1.2 01/08/14-16:38:31 (suhler)
 *   doc lint
 *
 * 1.2 01/08/01-11:07:01 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/SimpleSessionHandler.java
 *
 * 1.1 01/08/01-11:07:00 (suhler)
 *   date and time created 01/08/01 11:07:00 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.IOException;
import java.util.Vector;
import java.util.Properties;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Base64;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.StringMap;
import sunlabs.brazil.util.regexp.Regexp;
import sunlabs.brazil.util.MatchString;

/**
 * Handler for creating browser sessions based
 * on information found in the http request.
 * This handler provides a single session-id that may be used by
 * other handlers.
 * <p>
 * The following server properties are used:
 * <dl class=props>
 * <dt><code>prefix, suffix, glob, match</code>
 * <dd>Specify the URL that triggers this handler
 * (See {@link MatchString}).
 * <dt> <code>session</code>
 * <dd> The name of the request property that the Session ID will be stored
 *	in, to be passed to downstream handlers.  The default value is
 *	"SessionID".  If the property already exists, and is not empty, 
 *      no session will be defined (unless force=true).
 * <dt> <code>extract</code>
 * <dd> If specified, a string to use as the session-id.  ${...} values will
 *      be searched for first in the HTTP header values, and then
 *	in the request properties.
 *      <p>
 *      In addition to the actual HTTP headers, 
 *      the pseudo http headers <code>ipaddress, url, method, and query</code>
 *	are made available for ${...} substitutions.
 * <dt> <code>re</code>
 * <dd> If specified, a regular expression that the extracted data must match.
 *      if it doesn't match, no session id is installed.
 *      The default is ".", which matches any non-empty string.
 *	If the first character is "!" then the sense of the match is inverted,
 *	But only for determining whether a match "succeeded" or not.
 *	no sub-matches may be used in computing the key value in this case.
 * <dt> <code>value</code>
 * <dd> The value of the session ID.  May contain &amp; or \n (n=0,1,2...) 
 *	constructs to substitute
 *	matched sub-expressions of <code>re</code>.  The default is "&amp;" , which
 *	uses the entire string  "extract" as the <code>session</code> id.
 *	${...} are substituted (but not \'s) for <code>value</code> before
 *	looking
 *	for '\n' sequences that are part of the regular expression matches.
 * <dt> <code>digest</code>
 * <dd> If set, the "value" is replaced by the base64 encoding of the
 *	MD5 checksum of <code>value</code>.
 * <dt> <code>force</code>
 * <dd> If set (to anything), a session ID is set even if one already 
 * 	exists.
 * </dl>
 * If no options are provided, the client's IP address is used as the
 * session ID.
 * <p>
 * Examples:
 * <dl>
 * <dt>Pick the session based on the browser
 * <dd><pre>
 * [prefix].extract=${user-agent}
 * [prefix].re=.*(Netscape|Lynx|MSIE).*
 * [prefix].value=\\1
 * </pre>
 * <dt>This is similar to the "old" behavior.
 * <dd><pre>
 * [prefix].extract=${user-agent}${ipaddress}
 * [prefix].digest=true
 * </pre>
 * <dt>Look for a special authorization token, and set a request property
 *     to the value
 * <dd><pre>
 * [prefix].extract=${Authorization}
 * [prefix].re=code:([0-9]+)
 * [prefix].value=id\\1
 * </pre>
 * </dl> 
 *
 * @author		Stephen Uhler
 * @version		@(#)SimpleSessionHandler.java	2.5
 */

public class SimpleSessionHandler implements Handler {

    public String valueTemplate;	   // the resultant SessionID, if any.
    public Regexp regexp;	   // re that the id must match

    MatchString isMine;            // check for matching url
    String session;		   // property name to hold session ID
    String extract;		   // the string to get the ID from
    boolean force;		   // set sessioneven if exists
    boolean invert=false;	   // invert the sense of "match"
    MessageDigest digest = null;
    
    public boolean
    init(Server server, String prefix) {
	isMine = new MatchString(prefix, server.props);
	session = server.props.getProperty(prefix + "session", "SessionID");
	extract = server.props.getProperty(prefix + "extract", "${ipaddress}");
	force = (server.props.getProperty(prefix + "force") != null);
	valueTemplate = server.props.getProperty(prefix + "value", "&");
	if (server.props.getProperty(prefix + "digest") != null) {
	   try {
	       digest = MessageDigest.getInstance("SHA");
	   } catch (NoSuchAlgorithmException e) {
	       server.log(Server.LOG_WARNING, prefix,
		       "Can't find SHA implementation");
	       digest=null;
           }
        }

	String match = server.props.getProperty(prefix + "re", ".+");
	if (match.startsWith("!")) {
	    match = match.substring(1);
	    invert=true;
	}
	try {
	    regexp = new Regexp(match);
	} catch (Exception e) {
	    regexp=null;
	    server.log(Server.LOG_WARNING, prefix, "Bad expression:" + e);
	    return false;
	}
	return true;
    }

    public boolean
    respond(Request request) throws IOException {
	if (!isMine.match(request.url)) {
	    return false;
        }
	String current = request.props.getProperty(session);
	if (!force && current != null && !current.equals("")) {
	    request.log(Server.LOG_INFORMATIONAL, isMine.prefix(),
		    session + " already exists, skipping");
	    return false;
        }

	/*
	 * Build a properties object for Format.subst that looks for
	 * "special" values, then mime headers, then regular properties
	 */
	 

	Props props = new Props(request.headers, request.props);
	props.extra("ipaddress",
		request.getSocket().getInetAddress().getHostAddress());
	props.extra("url",request.url);
	props.extra("query",request.query);
	props.extra("method",request.method);

        String key = Format.subst(props, extract);
	String id;	// this will be the session id
	String result = Format.subst(request.props, valueTemplate, true); // ${} only, not '\'
	if (invert) {
	   if (regexp.match(key) == null) {
	       id = result;
	   } else {
	       id = null;
	   }
	} else {
	    id = regexp.sub(key, result);
	}
	if (id == null) {
	    request.log(Server.LOG_DIAGNOSTIC, isMine.prefix(), "(" + key +
		        ") doesn't match re, not set");
            return false;
	}

	if (digest != null) {
	   digest.reset();
	   id = Base64.encode(digest.digest(id.getBytes()));
	}
        request.props.put(session, id);
	request.log(Server.LOG_DIAGNOSTIC, isMine.prefix(),
		 "Using (" + id + ") as session id");
	return false;
    }

    /**
     * Add a few name/value pairs in front of a dictionary so Format.subst()
     * will find them when it calls get().
     * Sigh!
     */

    static class Props extends StringMap {
	StringMap map;	// the dictionary with most of the stuff
	Properties defaults;
	Vector extra;		// the extra name/value pairs

	Props(StringMap map, Properties defaults) {
	    this.map = map;
	    this.defaults = defaults;
	    extra = new Vector();
	}

	void extra(Object name, Object value) {
	    extra.addElement(name);
	    extra.addElement(value);
	}

	public String
	get(String key) {
	    for(int i=0; i < extra.size(); i+=2) {
		if (key.equals(extra.elementAt(i))) {
		    return (String) extra.elementAt(i+1);
		}
	    }
	    String result = map.get(key);
	    if (result == null && defaults != null) {
		result = defaults.getProperty(key);
	    }
	    return result;
	}
    }
}
