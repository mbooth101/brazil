/*
 * SetTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2007 Sun Microsystems, Inc.
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
 * Version:  2.16
 * Created by suhler on 00/10/25
 * Last modified by suhler on 07/01/08 15:14:56
 *
 * Version Histories:
 *
 * 2.16 07/01/08-15:14:56 (suhler)
 *   add <unimport>
 *
 * 2.15 06/11/13-12:07:33 (suhler)
 *   - started a general purpose "convert" thing
 *   - added "lower" and "trim" conversions
 *   - added "track" attribute to track changes to the console
 *   - removed "tag_map", which is now in the MiscTemplate
 *
 * 2.14 06/08/01-14:07:08 (suhler)
 *   - added doImport() public method to tell us to import remembered namespaces
 *   - fixed remember bug - a namespace could be chained multiple times
 *
 * 2.13 06/06/15-12:49:34 (suhler)
 *   the <namespace> tag now set a "namespace" request property
 *   .
 *
 * 2.12 06/05/04-22:29:08 (suhler)
 *   bug fix: when an empty namespace is imported, then filled with
 *   <namespace load=>, the namespace wasn't being properly imported
 *
 * 2.11 05/11/27-18:42:06 (suhler)
 *   assume url.orig is always defined
 *
 * 2.10 05/07/13-10:19:19 (suhler)
 *   add headers.hostname and headers.hostport
 *
 * 2.9 05/06/08-11:14:40 (suhler)
 *   change "headers.counter" to use server requestCount instead of acceptCount,
 *   which is more useful.
 *
 * 2.8 05/05/11-11:35:20 (suhler)
 *   Make the default "sessionTable" the template handler/filter prefix, and
 *   not the SetTemplate prefix by default [*** potential incompatibility ***].
 *   This allows all templates in the same template handler/filter to share
 *   namespaces by default
 *
 * 2.7 04/11/03-08:36:05 (suhler)
 *   url.request is now set in request
 *
 * 2.6 04/04/28-14:51:25 (suhler)
 *   doc format fix
 *
 * 2.5 04/04/19-14:36:40 (suhler)
 *   Add convert=lowercase to set
 *
 * 2.4 02/12/19-11:37:31 (suhler)
 *   add <namespace clear=>
 *
 * 2.3 02/11/04-13:53:34 (suhler)
 *   don't clear non-existing namespaces
 *
 * 2.2 02/10/24-11:58:04 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.37.1.1 02/10/24-11:58:01 (suhler)
 *   - namespace clear "clears" but does not unking the namespace
 *   - better error checking for missing attributes
 *   .
 *
 * 2.1 02/10/01-16:36:47 (suhler)
 *   version change
 *
 * 1.37 02/04/18-11:16:10 (suhler)
 *   added "set" option to get
 *
 * 1.36 02/02/04-14:31:45 (suhler)
 *   remove allowGt stuff; see AllowGtTemplate instead
 *
 * 1.35 02/01/29-10:03:47 (suhler)
 *   added <allowgt>...</allowgt> tags to enable unescaped >'s inside
 *   of quoted attribute strings.  Thus  The following should work:
 *   <allowgt>
 *   <if name="${x > 4}"> ...... </if>
 *   </allowgt>
 *   <img src="foo.gif> ...
 *   This works in conjunction with the new LexML stuff
 *
 * 1.34 02/01/23-09:51:03 (suhler)
 *   doc updates, lint
 *
 * 1.33 01/12/07-09:05:03 (suhler)
 *   doc cleanup
 *
 * 1.32 01/11/21-11:43:19 (suhler)
 *   doc fixes
 *
 * 1.31 01/11/13-11:40:52 (suhler)
 *   added "load" and "store" to <namespace> to allow saving and fetching a
 *   namespace from a java properties file
 *
 * 1.30 01/09/25-17:35:44 (suhler)
 *   added "imports" directive to auto-import namespaces on every page
 *
 * 1.29 01/09/23-17:36:05 (suhler)
 *   remove debugging code
 *
 * 1.28 01/08/29-08:57:52 (suhler)
 *   add max length argument to <get ...>
 *
 * 1.27 01/08/28-20:56:29 (suhler)
 *   added <namespace name=> ... </namespace>
 *   and <namespace name=... clear .>
 *
 * 1.26 01/08/27-18:59:25 (suhler)
 *   add "clear" option to <import> to zap a namespace
 *
 * 1.25 01/07/20-10:44:40 (suhler)
 *   doc fixes
 *
 * 1.24 01/07/18-16:32:40 (suhler)
 *   - Added headers.timestamp and headers.counter
 *   - fixed really stupid bug
 *
 * 1.23 01/07/16-16:46:54 (suhler)
 *   add PropsTemplate functionallity as "<get>", add <tag name=xx> option
 *
 * 1.22 01/07/11-16:27:31 (suhler)
 *   added "server" namespace
 *
 * 1.21 01/05/24-10:44:02 (suhler)
 *   fixed memory leak
 *
 * 1.20 01/05/23-09:21:46 (suhler)
 *   Bug fix.  Empty tables were never chaind if they were being created
 *   during a page.  Keep track of imported but not yet created tables, so
 *   they may be chained if they come into existence during a page
 *
 * 1.19 01/05/11-14:50:31 (suhler)
 *   use template.debug()
 *
 * 1.18 01/05/11-10:37:35 (suhler)
 *   rewrite:
 *   "local" is a synonym for namespace=local
 *   autoImport=false will turn off the implicit import of the session namespace
 *
 * 1.17 01/05/07-11:23:46 (suhler)
 *   bug.  No namespace was using namespace "".
 *
 * 1.16 01/04/05-14:49:26 (suhler)
 *   bug fix
 *
 * 1.15 01/04/05-10:00:48 (suhler)
 *   don't create empty session tables
 *
 * 1.14 01/03/27-14:51:47 (suhler)
 *   > Added the ability to set and use values from alternate session tables
 *   > with <set namespace=xxx> and <import namespace=xxx>
 *
 * 1.13 01/03/26-08:49:00 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.11.1.1 01/03/26-08:45:35 (suhler)
 *   Partial implementation of experimantal "namespaces".
 *
 * 1.12 01/03/23-14:28:21 (cstevens)
 *   Add "default" option to set template, based on observed usage.  <property>
 *   has "default" option, but this is only useful if the property is going to
 *   be substituted into HTML.  Adding "default" to <set> allows easier use of a
 *   possibly-undefined property whose value is going to be passed to another
 *   template via ${...}.
 *
 * 1.11 01/02/12-15:56:32 (cstevens)
 *   less logging - don't stringify the entire hashtable when setting a local var.
 *
 * 1.10 01/01/22-19:03:38 (cstevens)
 *   Remove RechainableProperties from Request.  In order to insert shared
 *   Properties objects, use request.addSharedProps() instead of manually
 *   rechaining the properties using props.setDefaults().
 *
 * 1.9 00/12/12-13:04:59 (suhler)
 *   Changed the default behavior to be persistent.
 *   use "local" keyword for a local copy only
 *   .
 *
 * 1.8 00/12/11-13:30:52 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.7 00/12/05-13:11:35 (suhler)
 *   remove debugging
 *
 * 1.6 00/11/20-13:21:53 (suhler)
 *   doc fixes
 *
 * 1.5 00/11/16-09:38:54 (suhler)
 *   added ability to set properties from url queries when running as a handler
 *
 * 1.4 00/11/15-09:48:51 (suhler)
 *   make persistent properties shareable
 *
 * 1.3 00/10/26-16:00:12 (suhler)
 *   implement serializablew
 *
 * 1.2 00/10/25-20:32:51 (suhler)
 *   added mustMatch to restict what may be set
 *
 * 1.2 00/10/25-20:16:59 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/SetTemplate.java
 *
 * 1.1 00/10/25-20:16:58 (suhler)
 *   date and time created 00/10/25 20:16:58 by suhler
 *
 */

package sunlabs.brazil.template;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.StringTokenizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import sunlabs.brazil.properties.PropertiesList;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.Glob;
import sunlabs.brazil.util.http.HttpUtil;

/**
 * Template (and handler) class for setting and getting values to
 * and from the current (or other) request context.
 * This class is used by the TemplateHandler.  The following tags
 * are processed by this class:
 * <ul>
 * <li>&lt;set name=.. [value=..] [namespace=..]&gt;
 * <li>&lt;get name=.. [default=..] [namespace=..] [max=nnn] [convert=html|url|none] [set=..]&gt;
 * <li>&lt;namespace name=.. [clear] [remove] [store=xxx] [load=xxx]&gt;
 * <li>&lt;import namespace=.. /&gt;
 * <li>&lt;unimport namespace=.. /&gt;
 * <li>&lt;tag [name=..]&gt;
 * <li>&lt;/tag&gt;
 * </ul>
 * <p>
 * The tag<code>&lt;set&gt;</code> is processed with the following
 * attributes:
 * <dl class=attributes>
 * <dt> name=<i>value</i>
 * <dd> The name of the entry to set in the request properties.
 *
 * <dt> value=<i>value</i>
 * <dd> The value to store in the property.  If this option is not specified,
 *	then the existing value for this property will be removed.
 *
 * <dt> default=<i>other</i>
 * <dd>	If <code>default</code> is specified and the specified <i>value</i>
 *	was the empty string "", then store this <i>other</i> value in the
 *	property instead.  <i>other</i> is only evaluated in this case.
 *
 * <dt> local
 * <dd> By default, variables are set (or removed from) a namespace that is
 *	persistent across all requests for the same session. If
 *	<code>local</code> is specified, the values only remain for the
 *	duration of the current request. [Deprecated - use namespace=local]
 *
 * <dt> namespace
 * <dd> The namespace to use to set or delete the property.  The default
 *	is <code>namespace=${SessionID}</code>, unless <code>set</code> is
 *      inside the scope of an enclosing <code>namespace</code> tag.
 *      Namespaces are used to define
 *	variable scopes that may be managed explicitly in template pages.
 *	<p>
 *	Two namespaces are treated specially:
 *	<ul>
 *	<li><b>local</b><br>
 *	may be used to set (or clear) values
 *	from <code>request.props</code>.
 *	<li><b>server</b><br>
 *	may be used to set (or clear) values
 *	from <code>server.props</code>, unless "noserver" is defined
 *	in which case hte "Server" namespace is not treated specially.
 *      </ul>
 * </dl>
 * <p>
 * The <code>&lt;namespace&gt;</code>..<code>&lt;/namespace&gt;</code> tag
 * pairs are used to specify a default namespace other than the 
 * <code>SessionId</code> for the <b>set</b> and <b>import</b> tags
 * or manipulate a namespace.
 * When a namespace is in effect, the request property [prefix].namespace
 * is set to the current namespace, where [prefix] is the
 * RewriteContext.templatePrefix.  This permits other templates (such as the
 * ListTemplate) to use the current namespace setting.
 * These tags don't nest.
 * <p>
 * attributes for <code>namespace</code>:
 * <dl class=attributes>
 * <dt> name=<i>value</i>
 * <dd> The default namespace to use for the <b>set</b> and <code>import</code>
 *	default namespace name.
 * <dt> clear
 * <dd> If set, namespace <code>name</code> is cleared.
 * <dt> remove
 * <dd> If set, namespace <code>name</code> is removed.
 * <dt> load=<i>file_name</i>
 * <dd> If specified, and the property <code>saveOk</code> is set, then
 *	the specified namespace will be saved loaded from a file in java
 *      properties
 *	format.  Relative path names are resolved with respect to the
 *	document root.
 * <dt> store=<i>file_name</i>
 * <dd> If specified, and the property <code>saveOk</code> is set, then
 *	the specified namespace will be stored to a file in java properties
 *	format.  Relative path names are resolved with respect to the
 *	document root.
 * </dl>
 * When using <code>load</code> or <code>store</code>, the namespace
 * names "local" and "server" are treated specially, referring to
 * "request.props" and "server.props" respectively.
 * <p>
 * The tag<code>&lt;import&gt;</code> (and <code>&lt;unimport&gt;</code>)
 * are processed with the following
 * attributes:
 * <dl class=attributes>
 * <dt> namespace=<i>value</i>
 * <dd> The name/value pairs assiciated with the namespace are
 *	made available for the remainder of the page.
 * </dl>
 * <p>
 * The "unimport" taq should probably be replaced by &lt;import&gt;
 * <p>
 * The "get" (formerly property) tag has the following attributes:
 * <dl>
 * <dt>name
 * <dd>The name of the variable to get
 * <dt>namespace
 * <dd>The namespace to look in.  By default, the variable is searched
 *     for in "request.props"
 * <dt>default
 * <dd>The value to use if no value matches "name".
 * <dt>convert
 * <dd>The conversion to perform on the value of the data before
 *     substitution: "html", "url", "lower, "trim", or "none" (the default). For
 *     "html", any special html syntax is escaped.
 *     For "url", the data will be suitable for transmission as an
 *     http URL.  The "lower" and "trim" options convert to lowercase and
 *     remove leading and trailing whitespace, respectively.
 * <dt>max
 * <dd>The output is truncated to at most <code>max</code> characters.
 * </dl>
 * If a single attribute is specified, with no "=", then is is taken
 * to be the "name" parameter.  Thus:
 * <code>&lt;get foo&gt;</code> is equivalent to:
 * <code>&lt;get name="foo"&gt;</code>.
 * <p>
 * Request Properties:
 * <dl class=props>
 * <dt>sessionTable
 * <dd>The name of the SessionManager table to use for
 *     storing values.  Defaults to the template handler's prefix.
 *     When configured
 *     in one server both as a handler and a template, the sessionTable should
 *     be set to the same value in both cases to get a useful result.
 * <dt>debug
 * <dd>If set, the original tag is included in a comment, 
 *     otherwise it is removed entirely.
 * <dt>mustMatch
 * <dd>Set to a glob pattern that all names must match
 *     in order to be set.  This may be used to prevent malicious
 *     html pages (what a concept) from changing inappropriate values.
 * <dt>noserver
 * <dd>The "server" namespace will no longer be mapped to
 *	<code>server.props</code>
 * <dt>noSet
 * <dd>If set, then the "set" tag will be disabled.
 * <dt>querySet
 * <dd>If set, then properties may be set in query 
 *     parameters, to the "handler" portion, but only
 *     if they match the glob pattern.
 * <dt>autoImport
 * <dd>If set to "1", the namespace for the session is 
 *     automatically imported. (defaults to "1");
 * <dt>imports
 * <dd>Defines a set of (white space delimited) namespaces that will
 *     automatically be imported at the beginning of each page.
 *     Each namespace name will be processed for ${...} substitutions before
 *     an import is attempted.  If the namespace doesn't already exist, 
 *     the import is ignored.
 * <dt>query	<dd> The query parameters are inserted into the request object,
 *	        prefixed by the value assigned to "query".
 * <dt>headers	<dd> The mime headers are inserted into the request object,
 *		prefixed by the value assigned to "headers".
 *              <p>In addition, the following properties (prefixed with
 *		"headers") are also set:
 *              <ul>
 *		<li><b>address</b> The ip address of the client
 *		<li><b>counter</b> A monotonically increasing counter
 *		    (# of client requests accepted since the server started).
 *		<li><b>method</b> The request method (typically GET or POST).
 *		<li><b>protocol</b> The client HTTP protocol level (for 1.0 or 1.1)
 *		<li><b>query</b> The current query data, if any.
 *		<li><b>timestamp</b> A timestamp (in ms since epoch) from when
 *			this request was first accepted.
 *		<li><b>url</b> The current url.
 *		<li><b>hostname, hostport</b> The name and port parts of the
 *		"host" header, if set.
 *              </ul>
 * <dt>url.orig	<dd> If set and "headers" are requested, this value is used as
 *		the <code>url</code> instead of the one in request.url.
 * </dl>
 * <p>
 * Normally, any persistent properties held by the SetTemplate are 
 * chained onto the request.props when the init method is found.  If this
 * template is installed as an up-stream handler, then the persistent
 * properties associated with the session are made available at that time.
 * <p>
 * When used as a handler, the following property is used:
 * <dl class=props>
 * <dt>session=<i>value</i>
 * <dd>The request property to find the session information in.
 *     Normally this should be the same as the session property used
 *     by the container calling this as a template.
 * <dt>saveOk
 * <dd>This must be specified in order for the
 *     "namespace store" or "namespace load" functions to operate.
 * </dl>
 * 
 * @author		Stephen Uhler
 * @version		@(#)SetTemplate.java	2.15
 */

public class SetTemplate extends Template implements Serializable, Handler {
    String mustMatch;		// glob-pattern of permitted names
    String prefix = null;	// non-null if instance is a handler
    String session;		// the handler property for the session var
    String sessionTable;	// the "other" session key
    String querySet;		// if non-null set values from query
    boolean allowServer=true;	// allow sets to server properties
    boolean noSet = false;	// if true, "set" is disabled"
    String current;		// current namespace 

    Vector remember = new Vector();	// future chainable namespaces

    /**
     * Chain the session-id properties into the request chain, if
     * there are any.  If "query" or "headers" are requested for "get", 
     * then add in those properties to request.props.
     */

    public boolean
    init(RewriteContext hr) {
	Properties props = hr.request.props;
	current = null;
	String query = props.getProperty(hr.prefix + "query");
	if (query != null) {
	    Dictionary h = hr.request.getQueryData(null);
	    Enumeration keys = h.keys();
	    while(keys.hasMoreElements()) {
		String key = (String) keys.nextElement();
		props.put(query + key, h.get(key));
	    }
	}
	String headers = props.getProperty(hr.prefix + "headers");
	if (headers != null) {
	    Enumeration keys = hr.request.headers.keys();
	    while(keys.hasMoreElements()) {
		String key = (String) keys.nextElement();
		props.put(headers + key.toLowerCase(),
			hr.request.headers.get(key));
	    }
	    props.put(headers + "method", hr.request.method);
	    props.put(headers + "url", props.getProperty("url.orig"));
	    props.put(headers + "query", hr.request.query);

	    String protocol = hr.request.serverProtocol == null ?
		hr.request.server.protocol :  hr.request.serverProtocol;
	    props.put(headers + "protocol", protocol);

    	    props.put(headers + "address",
		"" + hr.request.getSocket().getInetAddress().getHostAddress());
	    props.put(headers + "timestamp", "" + hr.request.startMillis);
	    props.put(headers + "counter", "" + hr.server.requestCount);

	    String host = props.getProperty(headers + "host");
	    if (host != null) {
		int indx = host.indexOf(":");
		if (indx < 0) {
		    props.put(headers + "hostname", host);
		    props.put(headers + "hostport", protocol.equals("http")?
			"80" : "443");
		} else {
		    props.put(headers + "hostname", host.substring(0,indx));
		    props.put(headers + "hostport", host.substring(indx+1));
		}
	    }
	}

	remember.removeAllElements();
	mustMatch = hr.request.props.getProperty(hr.prefix + "mustMatch");
	allowServer = (null == hr.request.props.getProperty(hr.prefix +
		"noserver"));
	noSet = (null != hr.request.props.getProperty(hr.prefix +
		"noSet"));
	sessionTable = hr.request.props.getProperty(hr.prefix +
		"sessionTable", hr.templatePrefix);

	String autoImport = hr.request.props.getProperty(hr.prefix +
	    "autoImport", "1");
	if  (autoImport.equals("1")) {
	    Properties p = (Properties) SessionManager.getSession(hr.sessionId,
		    sessionTable, null);
	    if (p != null) {
		hr.request.addSharedProps(p);
	    } else {
	        remember.addElement(hr.sessionId);
		hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
			"Remembering (init) " + sessionTable);
	    }
	}

	String imports = hr.request.props.getProperty(hr.prefix + "imports");
	if (imports != null) {
	    StringTokenizer st = new StringTokenizer(imports);
	    while (st.hasMoreTokens()) {
	        String token = Format.subst(hr.request.props, st.nextToken());
	        Properties p = (Properties) SessionManager.getSession(token,
		    sessionTable, null);
	        if (p != null) {
		    hr.request.addSharedProps(p);
		    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
			    "Auto importing: " + token);
		}
	    }
	}
	return super.init(hr);
    }

    /**
     * Set the value of a variable.  Allow variable substitutions in the name
     * and value.  Don't create property tables needlessly.
     * <p>
     * Attributes:
     * <dl>
     * <dt>name
     * <dd>The name of the variable to set
     * <dt>value
     * <dd>The value to set if to.
     * <dt>namespace
     * <dd>The namespace to look in.  By default, the variable is set in
     *     the namespace associated with the current "SessionId".
     * <dt>local
     * <dd>A (deprecated) alias for "namespace=local", or the current
     *     request.props.
     * </dl>
     */

    public void
    tag_set(RewriteContext hr) {
	boolean shouldTrack = Format.isTrue(hr.request.props.getProperty(hr.prefix + "track"));
	if (noSet) {
	    return;
	}
        debug(hr);
	hr.killToken();

	String name =  hr.get("name");
	if (name == null) {
	    debug(hr, "missing variable name");
	    return;
	}
	if (mustMatch!=null && !Glob.match(mustMatch, name)) {
	    debug(hr, name + " doesn't match " + mustMatch);
	    return;
	}
	String value =  convert(hr,hr.get("value"));
	String namespace =  hr.get("namespace");
	boolean local = (hr.isTrue("local") || "local".equals(namespace));
	Class create  = (value != null)  ? Properties.class : null;

	if (namespace == null) {
	    namespace = (current == null) ? hr.sessionId : current;
	}

	/*
	 * Find the correct property table.  If we are
	 * clearing an entry, don't make the table if it doesn't exist.
	 */

	Properties p;	// table to set or clear
	if (local) {
	    p = hr.request.props;
	    namespace="local";
	} else if (allowServer && "server".equals(namespace)) {
	    p = hr.server.props;
	} else  {
	    p = (Properties) SessionManager.getSession(namespace,
		sessionTable, create);
	}

	// set or clear the entry

	if (value != null) {
	    if (value.equals("")) {
		String dflt = hr.get("default");
		if (dflt != null) {
		    value = dflt;
		}
	    }
	    if (p.size()==0 && remember.contains(namespace)) {
		hr.request.addSharedProps(p);
		remember.removeElement(namespace);
		hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
			"Importing " + namespace);
	    }
	    if (shouldTrack) {
		System.out.println("<set>: (" + name + "=" + value + 
		    ") in namespace: (" + namespace + ")");
	    }
	    p.put(name, value);
	    debug(hr, namespace + "(" + name + ") = " + value);
	} else if (p != null) {
	    p.remove(name);
	    if (shouldTrack) {
		System.out.println("<set> Clearing the entry (" + name +
			") from namespace (" + namespace + ")");
	    }
	}
    }

    /**
     * Convert the html tag "property" in to the request's property
     * DEPRECATED - use "get"
     */

    public void
    tag_property(RewriteContext hr) {
        tag_get(hr);
    }

    /**
     * Replace the tag "get" with the value of the
     * variable specified by the "name" attribute.
     * <p>
     * Attributes:
     * <dl>
     * <dt>name
     * <dd>The name of the variable to get
     * <dt>namespace
     * <dd>The namespace to look in.  By default, the variable is searched
     *     for in "request.props".  The namespace "server" is used to look in
     *	   the server's namespace.  The namespace "local" is a synonym for the
     *	   default namespace.
     * <dt>default
     * <dd>The value to use if no value matches "name".
     * <dt>convert
     * <dd>The conversion to perform on the value of the data before
     *     substitution: "html", "url", or "none" (the default). For
     *     "html", any special html syntax is escaped.
     *     For "url", the data will be suitable for transmission as an
     *     http URL.
     * <dt>max
     * <dd>Truncate the String to at most <code>max</code> characters.
     *     Max must be at least one, and truncation occurs after
     *     any conversions.
     * <dt>set
     * <dd>The resultant value is placed into the request property named by
     *     the <code>set</code> attribute, and not inserted into the
     *     HTML stream.  If none of "namespace", "convert", or "match"
     *	   is used, then this simply copies the property from one name
     *     to another.
     * </dl>
     * If a single attribute is specified, with no "=", then is is taken
     * to be the "name" parameter.  Thus:
     * <code>&lt;get foo&gt;</code> is equivalent to:
     * <code>&lt;get name="foo"&gt;</code>.
     */

    public void
    tag_get(RewriteContext hr) {
	String name = hr.getArgs();
	String result = null;
	if (name.indexOf('=') >= 0) {
	    name = hr.get("name");
	    result = hr.get("default");
	} else {
	    name = Format.subst(hr.request.props, name);
	}

	if (name == null) {
	    debug(hr, "get: no name");
	    return;
	}

	String namespace = hr.get("namespace");
	if (namespace != null) {
	    Properties p;
	    if (namespace.equals("server")) {
		p = hr.server.props;
	    } else if (namespace.equals("local")) {
		p = hr.request.props;
	    } else {
	        p = (Properties) SessionManager.getSession(namespace,
		    sessionTable, null);
	    }
	    result = p==null ? result : p.getProperty(name, result);
	} else {
	    result = hr.request.props.getProperty(name, result);
	}

	if (result != null) {
	    result = convert(hr, result);
	    int max = 0;
	    try {
		String str = hr.get("max");
		max = Integer.decode(str).intValue();
	    } catch (Exception e) {}
	    if (max > 0 && max < result.length()) {
		result = result.substring(0,max);
	    }

	    String set = hr.get("set");  // set to value only 
	    if (set != null) {
	       hr.request.props.put(set, result);
	    } else {
	       hr.append(result);
	    }
	} else {
	    debug(hr, "property: no value for " + name);
	}
	hr.killToken();
    }

    /**
     *  This should be a general purpose utility
     */

    static String convert(RewriteContext hr, String s) {
	String convert = hr.get("convert");
	if (convert != null) {
	    if (convert.indexOf("lower")>=0) {
		s = s.toLowerCase();
	    }
	    if (convert.indexOf("trim")>=0) {
		s = s.trim();
	    }
	    if (convert.indexOf("html")>=0) {
		s = HttpUtil.htmlEncode(s);
	    }
	    if (convert.indexOf("url")>=0) {
		s = HttpUtil.urlEncode(s);
	    }
	}
	return s;
    }

    /**
     * Import all the data from the named namespace.  The namespace
     * associated with the session ID is imported automatically
     * for backward compatibility.  If the namespace doesn't exist, 
     * don't create it now, but remember it needs to be "Chained"
     * if it is created on this page.
     */

    public void
    tag_import(RewriteContext hr) {
        debug(hr);
	hr.killToken();
	String namespace =  hr.get("namespace");
	if (namespace == null && current != null) {
	    namespace = current;
	    // System.out.println("Importing namespace: " + namespace);
	}
	if (namespace != null) {
	    Properties p = (Properties) SessionManager.getSession(namespace,
		    sessionTable, null);
	    if (p != null) {
	        hr.request.addSharedProps(p);
	    } else if (!remember.contains(namespace)){
	        remember.addElement(namespace);
		hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
			"Remembering " + namespace);
	    }
	}
    }
    
    /**
     * Un-import a previously imported namespace.
     */

    public void
    tag_unimport(RewriteContext hr) {
        debug(hr);
	hr.killToken();
	String namespace =  hr.get("namespace");
	if (namespace == null && current != null) {
	    namespace = current;
	}
	if (namespace != null) {
	    Properties p = (Properties) SessionManager.getSession(namespace,
		    sessionTable, null);
	    if (p != null) {
	        PropertiesList pl = hr.request.props.wraps(p);
		if (pl != null) {
		    pl.remove();
		    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
			    "un-Importing: " + namespace);
	        }
	    } else if (!remember.contains(namespace)) {
	        remember.removeElement(namespace);
		hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
			"unimporting remembered namespace: " + namespace);
	    }
	}
    }

    /**
     * Set the default namespace for "set" and "import".
     */

    public void
    tag_namespace(RewriteContext hr) {
	String name = hr.get("name");
        debug(hr);
	hr.killToken();
	if (name != null) {
	    if (!hr.isSingleton()) {
	        current = name;
		hr.request.props.put(hr.templatePrefix + "namespace", current);
	    }
	    String load = hr.get("load");
	    String store = hr.get("store");
	    boolean ok =
		  (null != hr.request.props.getProperty(hr.prefix + "saveOk"));

	    // XXX should dis-allow ambiguous options

	    Properties p;	// which property to load or set
		boolean isSm = false;	// true if session manager table
		if (name.equals("local")) {
			p = hr.request.props;
		} else if (name.equals("server")) {
			p = hr.server.props;
		} else {
			p = (Properties) SessionManager.getSession(name,
				sessionTable, null);
			isSm = true;
		}

	    if (ok && store != null && p != null) {
		   store(p, hr, store, name);
	    } else if (store != null) {
		debug(hr, "namespace store failed: (allowed=" + ok + ")");
	    }

	    if (ok && load != null) {
		if (p == null) {
		    p = (Properties) SessionManager.getSession(name,
			sessionTable, Properties.class);
		}
		load(p, hr, load);
	        if (remember.contains(name)) {
		    hr.request.addSharedProps(p);
		    remember.removeElement(p);
		    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
			"load: Importing " + name);
		}
	    } else if (load != null) {
		debug(hr, "namespace load failed: (allowed=" + ok + ")");
	    }

	    if (isSm && hr.isTrue("clear") && p != null) {
		p.clear();
	    }
	    if (isSm && hr.isTrue("remove") && p != null) {
		SessionManager.remove(name, sessionTable);
		p = null;
	    }
	} else {
	    debug(hr, "No namespace to import");
	}
    }
 
    /**
     * Convert a file name into a file object.
     * relative paths use are resolved relative to the document root
     * @param p		properties file to find the doc root in
     * @param prefix	the properties prefix for root
     * @param name	the name of the file
     */

    public File
    file2path(Properties p, String prefix, String name) {
	File file;
	if (name.startsWith("/")) {
	    return new File(name);
	} else {
	    String root = p.getProperty(prefix + "root", 
		    p.getProperty("root", "."));
	    return new File(root, name);
	}
    }

    /**
     * load a namespace from a java properties file
     */

    boolean
    load(Properties p, RewriteContext hr, String name) {
	File file = file2path(hr.request.props, hr.prefix, name);
	hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
		"loading namespace " + file);
	try {
	    FileInputStream in = new FileInputStream(file);
	    p.load(in);
	    in.close();
	    return true;
	} catch (IOException e) {
	    hr.request.log(Server.LOG_LOG, hr.prefix,
		"Can't load namespace " + file);
	    return false;
	}
    }

    /**
     * Store a namespace into a java properties file
     */

    boolean
    store(Properties p, RewriteContext hr, String name, String title) {
	File file = file2path(hr.request.props, hr.prefix, name);
	try {
	    FileOutputStream out = new FileOutputStream(file);
	    p.save(out, hr.request.serverUrl() + hr.request.url +
		" (for namespace " + title + ")");
	    out.close();
	    hr. request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
		"Saving namespace " + title + " to " + file);
	    return true;
	} catch (IOException e) {
	    hr. request.log(Server.LOG_WARNING, hr.prefix,
		    "Can't save namespace " + title + " to " + file);
	    return false;
	}
    }

    /**
     * Clear the default namespace for "set" and "import".
     */

    public void
    tag_slash_namespace(RewriteContext hr) {
        debug(hr);
	hr.killToken();
	current = null;
	hr.request.props.remove(hr.templatePrefix + "namespace");
    }

    /**
     * Map a set of variables from one name to another.
     * <dl class=attributes>
     * <dt>namespace 
     * <dd>The namespace of the destination (defaults to current namespace)
     * <dt>match
     * <dd>The regular expression that matches variable names in the
     *	   current context
     * <dt>replace
     * <dd>The substitution expression used to derice the new names
     * <dt>convert
     * <dd>Which conversions to perform on the data.  May be
     *	   "html", "lower", "url", "trim", or "none".  The default is "none"
     * <dt>clobber
     * <dd>"true" or "false".  If "false" (the default) existing variables
     *     will not be overridden
     * <dt>clear
     * <dd>"true" or "false".  If "true" (the default) the source variable
     *     will be deleted
     * </dl>
     */

    /**
     * Insert a literal "&lt;".
     * Using the current scheme, there is no easy way to substitute into
     * a tag parameter.  So we'll invent a "magic" tag (called tag)
     * that will allow us to create entities dynamically.  Thus values
     * can be substituted into entities by escaping the entity as in:
     * <pre>
     * &lt;tag&gt;a href=&lt;property href&gt;&lt;/tag&gt;
     * </pre>
     * <p>
     * The [optional] attribute "name" may be used to specify the
     * name of the tag, which will be emmitted just after the "&lt;".
     */

    public void
    tag_tag(RewriteContext hr) {
	String name = hr.get("name");
	hr.append("<");
	if ( name != null) {
	    hr.append(name);
	}
    }

    /**
     * Insert a literal "&gt;"
     */

    public void
    tag_slash_tag(RewriteContext hr) {
	hr.append(">");
    }

    /**
     * Get the 2 SessionManager keys, "sessionTable" and "session" (SessionID).
     */

    public boolean
    init(Server server, String prefix) {
	this.prefix = prefix;
	querySet = server.props.getProperty(prefix + "querySet");
	sessionTable = server.props.getProperty(prefix + "sessionTable",
		prefix);
	session=server.props.getProperty(prefix + "session", "SessionID");
	return true;
    }

    /**
     * Chain a SessionManager entries onto the request properties, 
     * and optionally allow setting of request props from query parameters.
     * Only the session id table can be chained, and 
     * values (if any) are set in request.props (i.e. namespace=local)
     */

    public boolean
    respond(Request request) {
	String id = request.props.getProperty(session);
	if (id != null) {
	    Properties p = (Properties) SessionManager.getSession(id,
		    sessionTable, null);
	    if (p != null) {
	        request.addSharedProps(p);
		request.log(Server.LOG_DIAGNOSTIC, prefix,
			"Chaining: " + id + "," + sessionTable);
	    } else {
		request.log(Server.LOG_DIAGNOSTIC, prefix,
			"No table: " + id + "," + sessionTable);
	    }
	}

	/*
	 * Set some properties as part of the request.
	 * Should they be persistent or not?
	 */

	if (querySet!=null) {
	    Dictionary h = request.getQueryData(null);
	    Enumeration keys = h.keys();
	    while(keys.hasMoreElements()) {
		String key = (String) keys.nextElement();
	        if (Glob.match(querySet, key)) {
		    request.props.put(key, h.get(key));
		}
	    }
	}
	return false;
    }

    /**
     * See if a remembered namespace is actually in use, and import it.
     * (This is a workaround).  If some other template causes a namespace
     * that was previously imported but empty to be created, then this
     * tells us that it is now populated, and should be added to the
     * chain. [a better implementation might have us register interest
     * in a namespace, so we would be called automatically upon creation,
     * instead of requiring the creator to know about us and make this
     * call explicitly.]
     */

    public boolean
    doImport(RewriteContext hr) {
	boolean result = false;
	String namespace= hr.get("namespace");
	if (namespace != null && remember.contains(namespace)) {
	    Properties p = (Properties) SessionManager.getSession(namespace,
				sessionTable, null);
	    if (p != null) {
	        hr.request.addSharedProps(p);
		remember.removeElement(namespace);
		result=true;
	    }
	}
	return result;
    }
}
