/*
 * DigestAuthHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2004 Sun Microsystems, Inc.
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
 * Created by suhler on 04/04/23
 * Last modified by suhler on 04/12/30 12:39:24
 *
 * Version Histories:
 *
 * 1.5 04/12/30-12:39:24 (suhler)
 *   javadoc fixes.
 *
 * 1.4 04/11/23-16:08:52 (suhler)
 *   add a simple scheme for managing dynamic credentials by rereading the
 *   credentials file
 *
 * 1.3 04/11/03-08:55:47 (suhler)
 *   compare url n auth request with "url.orig", not "request.url"
 *
 * 1.2 04/05/24-15:24:36 (suhler)
 *   doc fixes
 *
 * 1.2 04/04/23-15:28:01 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/DigestAuthHandler.java
 *
 * 1.1 04/04/23-15:28:00 (suhler)
 *   date and time created 04/04/23 15:28:00 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Properties;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.util.Guid;
import sunlabs.brazil.util.regexp.Regexp;
import sunlabs.brazil.util.regexp.Regsub;

/**
 * Perform digest authentication.
 * This is a minimal implementation of RFC 2617
 * The "optional" qos parameter is required by IE (only qop="auth" is supported).
 * The "password" file is read at startup time, either as a resource
 * or from the file system, and may contain
 * either plain text or digested passwords (see main() below to digest
 * passwords). 
 * <p>
 * Future enhancements
 * <ul>
 * <li>Better dynamic operation
 * <li>Optional digest parameter handling
 * <li>Nonce time-to-live checking
 * </ul>
 * Sample auth request header
 * <pre>  
 * WWW-Authenticate: Digest
 *    realm="myrealm",
 *    qop="auth",					[req'd for IE]
 *    nonce="dcd98b7102dd2f0e8b11d0f600bfb0c093",
 *    opaque="5ccc069c403ebaf9f0171e9517f40e41",	[optional]
 *    domain="/foo"					[optional]
 * </pre>
 * Sample client return header
 * <pre>
 *  Authorization: Digest
 *    username="name",
 *    realm="foo@bar",
 *    nonce="mynonce10",
 *    uri="/da.html",
 *    response="d58f3f9fa7554da651d3f1901d22ea04",
 *    qop=auth,
 *    nc=00000001,
 *    cnonce="b6ac242cb324c38a"
 *
 * response algorithm:
 * 
 * A1 = md5(user:realm:pass)
 * A2 = md5(method:uri)
 * response=md5(A1:nonce:nonceCount:cnonce:qop:A2)
 * - all MD5's are represented as hex: [0-9a-f]
 * - all quotes (") are removed before digesting
 * </pre>
 * <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Specify which url's this handler applies to.
 * <dt>realm
 * <dd>The string presented to the user for validation.  This must also
 * match any "digested" passwords.
 * <dt>credentials
 * <dd>A java-properties format file of credentials.  The keys are the
 * users, the values are either the "A1" values described above,
 * or the user's password.
 * <dt>isDynamic
 * <dd>If set (to anything), when authentication for a user is requested
 * that is not in the credentials table and the credentials table has
 * changed since last read, the table is re-read, in case the user has been
 * added since the credentials were loaded.
 * <dt>allowBogusIE
 * <dd>Internet Explorer does not use the query parameters as part
 * of the "uri" calculation.  This is a bug (and a security risk, as
 * it allows replay attacts to other than the url requested).  If this
 * variable is set, then it allows IE to work in this case.
 * <dt>username
 * <dd>If the user was validated, this field is filled out by the handler.
 * </dl>
 */

public class DigestAuthHandler implements Handler {
    MatchString isMine;            // check for matching url
    Properties credentials = new Properties();
    String realm;
    boolean allowBogus = false;	// IE bug workaround
    boolean isDynamic = false;	// allow dynamic credentials updates
    long lastModified = 0;	// last modified time if credentials file
    File credFile = null;	// path to credentials file for dynamic use

    public boolean
    init(Server server, String propsPrefix) {
	isMine = new MatchString(propsPrefix, server.props);
	String file=server.props.getProperty(propsPrefix + "credentials");
	allowBogus = (server.props.getProperty(propsPrefix + 
		"allowBogusIE") != null);
	isDynamic = (server.props.getProperty(propsPrefix + 
		"isDynamic") != null);
	try {
	    server.log(Server.LOG_DIAGNOSTIC, propsPrefix,
		    "Loading credentials file " + file);
	    InputStream in = ResourceHandler.getResourceStream(server.props,
		    propsPrefix, file);
	    credentials.load(in);
	    in.close();
	} catch (Exception e) {
	    server.log(Server.LOG_WARNING, propsPrefix,
		    "Loading credentials file " + e);
	    // e.printStackTrace();
	    if (!isDynamic) {
	        return false;
	    }
	}

	if (isDynamic) {
	    String path = ResourceHandler.getResourcePath(server.props,
		    propsPrefix, file);
	    credFile = new File(path);
	    lastModified = credFile.lastModified();
	}

	// make sure our VM can compute the digest.

	try {
	   MessageDigest.getInstance("MD5");
	} catch (NoSuchAlgorithmException e) {
	    server.log(Server.LOG_WARNING, propsPrefix,
		    "Can't find MD5 provider");
	    return false;
	}

	realm = server.props.getProperty(propsPrefix + "realm", propsPrefix);
	return true;
    }

    public boolean
    respond(Request request) throws IOException {
	if (!isMine.match(request.url)) {
	    return false;
	}

	String auth = request.headers.get("authorization");
	if (auth == null) {
	    reject(request, "Authentication Required");
	    return true;
	}
	if (!auth.startsWith("Digest")) {
	    reject(request, "Invalid Authentication scheme");
	    return true;
	}

	Properties h = extractAuth(auth);

	// make sure the url matches

	if (!matchUrl(request, h.getProperty("uri"))) {
	    reject(request, "Bad uri");
	    request.log(Server.LOG_WARNING, isMine.prefix(),
		    "Possible Digest Authentication breakin attempt!");
	    return true;
	}

	// make sure username is in credentials file

	String user = h.getProperty("username");
	if (user == null) {
	    reject(request, "invalid authentication header: no username");
	    return true;
	}

	String A1 = credentials.getProperty(user);
	if (A1 == null && isDynamic) {
	    updateCredentials(request);
	    A1 = credentials.getProperty(user);
	}
	if (A1 == null) {
	    reject(request, "No user in credentials table: " + user);
	    return true;
	}

	if (!isMd5Digest(A1)) {
            A1 = computeA1(user, realm, A1);
	    request.log(Server.LOG_LOG, isMine.prefix(),
		    "Found plain password in auth file");
	}

	// compute the digest

	if (!responseOk(A1, request.method, h)) {
	    reject(request, "Invalid credentials for " + user);
	    return true;
	}

	// place username in request props

	request.props.put(isMine.prefix() + "username", user);
	return false;
    }

    /**
     * If the credentials file has been modified, re-load it
     */

    void updateCredentials(Request r) {
	long current = credFile.lastModified();
	if (current > lastModified) {
	    lastModified = current;
	    try {
		FileInputStream fin = new FileInputStream(credFile);
		credentials.load(fin);
		fin.close();
		r.log(Server.LOG_LOG, isMine.prefix(),
		    "re-reading credentials file");
	    } catch (IOException e) {
		r.log(Server.LOG_WARNING, isMine.prefix(),
		    "ERROR re-reading credentials file: " + e);
	    }
	}
    }

    /**
     * Check the digest response string.
     * @param A1	The "A1" hash from the RFC
     * @param method	The http request method.
     * @param h		Properties containing all the name=value options
     *			from the http authentiation header field
     *			(see {@link #extractAuth(String)}).
     */

    public static boolean responseOk(String A1, String method, Properties h) {
	String A2 = computeA2(method, h.getProperty("uri"));
        String digest = computeResponse(A1, A2, h.getProperty("nonce"),
		h.getProperty("nc"), h.getProperty("cnonce"),
		h.getProperty("qop"));
	return digest.equals(h.getProperty("response"));
    }

    // these are for others to use as needed.

    /**
     * Compute the A1 parameter as per the RFC.
     */

    public static String
    computeA1(String user, String realm, String pass) {
	String s = user + ":" + realm + ":" + pass;
	return md5Digest(s);
    }

    /**
     * Compute the A2 parameter as per the RFC.
     */

    public static String
    computeA2(String method, String uri) {
	return md5Digest(method + ":" + uri);
    }

    /**
     * Compute the expected client response attribute value.
     */

    public static String
    computeResponse(String A1, String A2, String nonce,
	    String nc, String cnonce, String qop) {
	return md5Digest(A1 + ":" + nonce + ":" + nc + ":" + cnonce + 
		":" + qop + ":" + A2);
    }

    /**
     * Given the "WWW-Authenticate" header value and additional client info, 
     * generate the value of the "Authorization" header.
     * The "request" should contain "realm", "nonce", "qop" and optionally "opaque".
     * This is a convenience method for clients to use to athenticate to
     * this server implementation.
     * @param request	The string value of the "WWW-Authenticate" header from the server
     * @param user	The userid
     * @param pass	The password associated with this user
     * @param method	"GET", "POST", etc.
     * @param uri	The requested url (e.g. "/index.html")
     * @param nc	The "nonce count", or number of times the client has used
     *			The "nonce" presented by the server (e.g. "0000001").
     * @param cnonce	An opaque value provided by the client
     */

    public static String genResponseHeader(String request, String user,
	    String pass, String method, String uri, String nc,
	    String cnonce) {
        Properties h = extractAuth(request); // stuff in auth req header
	String realm = h.getProperty("realm");
	String nonce = h.getProperty("nonce");
	String qop = h.getProperty("qop");
	String opaque = h.getProperty("opaque");
        String A1 = computeA1(user, realm, pass);
        String A2 = computeA2(method, uri);
        String response = computeResponse(A1, A2, nonce, nc, cnonce, qop);
	return "Digest username=\"" + user + "\", realm=\"" + realm +
		"\", nonce=\"" + nonce + "\", uri=\"" + uri +
		"\", response=\"" + response + "\", qop=\"" + qop +
		"\", nc=\"" + nc + "\", cnonce=\"" + cnonce + "\"" + 
		(opaque==null ? "" : ", opaque=\" + opaque + \"");
    }

    static char[] cnvt = {
	'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'
    };

    /**
     * Compute the md5 digest of a string, returning the 
     * digest as a hex string.
     */

    public static String md5Digest(String s) {
	MessageDigest digest;
	try {
	   digest = MessageDigest.getInstance("MD5");
	} catch (NoSuchAlgorithmException e) {
	    return null;
	}
	for(int i=0;i<s.length();i++) {
	    byte b = (byte)(s.charAt(i) & 0xff);
	    digest.update(b);
	}
	byte[] bin = digest.digest();
	char[] result = new char[bin.length*2];

	int j=0;
	for(int i=0;i<bin.length;i++) {
	    result[j++] = cnvt[(bin[i]>>4)&0xf];
	    result[j++] = cnvt[bin[i]&0xf];
	}
	return new String(result);
    }

    /**
     * Make sure the digest uri matches the requested uri.
     */

    boolean matchUrl(Request r, String url) {
	String orig = r.props.getProperty("url.orig", r.url);
	if (r.query.equals("")) {
	    return orig.equals(url);
	} else {
	    boolean ok = (orig + "?" + r.query).equals(url);
	    if (!ok && allowBogus && orig.equals(url)) {
		r.log(Server.LOG_WARNING, isMine.prefix(),
			"Allowing bogus IE authentication");
		return true;
	    }
	    return ok;
	}
    }

    static Regexp digestRe = new Regexp("^[0-9a-f]+$");

    /**
     * See if a string is a valid md5 digest.
     */

    public static boolean isMd5Digest(String s) {
	return s.length() == 32 && (digestRe.match(s) != null);
    }

    static Regexp re = new Regexp(" ([a-z]+)=((\"([^\"]*)\")|([^ ,]*))");

    /**
     * Parse an auth header, placing the results into a Properties object.
     * Format is: Digest key=value, key=value, ...
     * values may be in "'s.
     */

    public static Properties extractAuth(String header) {
	Properties h = new Properties();
	Regsub rs = new Regsub(re, header);
	while (rs.nextMatch()) {
	    String key = rs.submatch(1);
	    String value = rs.submatch(4);
	    if (value == null) {
		value = rs.submatch(2);
	    }
	    h.put(key.toLowerCase(), value);
	}
	// h.list(System.out);
	return h;
    }

    /**
     * Send an auth header with a "rejection" message.
     */

    void
    reject(Request request, String reason)
	throws IOException {
	request.addHeader("WWW-Authenticate",
	    "Digest realm=\"" + realm + "\", " +
	    "qop=\"auth\", " +
	    "nonce=\"" + Guid.getString() + "\"");
	request.log(Server.LOG_DIAGNOSTIC, isMine.prefix(), reason);
	request.sendResponse(Format.subst(request.props, reason),
		"text/html", 401);
    }

    /**
     * Convert a "plain text" password file into a digested one.  Any
     * existing digests are left alone.
     * <pre>
     * Usage: DigestAuthHandler [realm]
     * </pre>
     * The stdin, in Properties format, is emitted on stdout with
     * all plain-text passwords digested.
     * If an entry is already digested, it is left alone.
     * <p>
     * Note, this handler will except either plaintext or digested
     * passwords in the credentials file.
     */

    public static void main(String args[]) throws Exception {
	if (args.length != 1) {
	    System.out.println("usage: DigestAuthHandler [realm]");
	    System.out.println("  A properties file is filtered to " +
		"replace plaintext passwords with digested ones");
	    System.exit(1);
	}

	Properties p = new Properties();
	p.load(System.in);
	int i=0;
	Enumeration e = p.keys();
	while(e.hasMoreElements()) {
	    String key = (String) e.nextElement();
	    String value = p.getProperty(key);
	    if (!isMd5Digest(value)) {
		p.put(key, computeA1(key, args[0], value));
		i++;
	    }
	}
	p.save(System.out, "Digested with realm: " + args[0]);
	System.err.println("" + i + " passwords digested");
	System.exit(0);
    }
}
