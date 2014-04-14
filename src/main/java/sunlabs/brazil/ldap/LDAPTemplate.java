/*
 * LDAPTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2004 Sun Microsystems, Inc.
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
 * The Initial Developer of the Original Code is: cstevens.
 * Portions created by cstevens are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): cstevens, suhler.
 *
 * Version:  2.3
 * Created by cstevens on 01/01/11
 * Last modified by suhler on 04/11/30 15:19:39
 *
 * Version Histories:
 *
 * 2.3 04/11/30-15:19:39 (suhler)
 *   fixed sccs version string
 *
 * 2.2 04/01/27-17:19:51 (suhler)
 *   Fixed limit=nn so it works now
 *
 * 2.1 02/10/01-16:39:12 (suhler)
 *   version change
 *
 * 1.9 01/09/13-09:24:39 (suhler)
 *   remove uneeded import
 *
 * 1.8 01/07/16-16:50:04 (suhler)
 *   use new Template convenience methods
 *
 * 1.7 01/06/04-11:06:23 (suhler)
 *   added debug option
 *
 * 1.6 01/03/23-14:28:44 (cstevens)
 *   NullPointerException when storing LDAP information in hash table.
 *   Theory: looking up a non-existent employee returns null.
 *   Practice: it seems a non-null entry can be returned, but all the fields
 *   in the entry will be null.
 *
 * 1.5 01/02/13-13:41:55 (cstevens)
 *   Merged changes between child workspace "/home/cstevens/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.3.1.1 01/02/12-15:57:39 (cstevens)
 *   Resources: explicitly release LDAP connection rather than waiting for GC to
 *   get it, or can run out of file descriptors.
 *
 * 1.4 01/01/14-14:54:40 (suhler)
 *   doc fixes
 *
 * 1.3 01/01/12-16:54:16 (cstevens)
 *   move LDAPTemplate to sunlabs.brazil.ldap package.
 *
 * 1.2 01/01/12-08:24:26 (suhler)
 *   removed wildcarded imports
 *
 * 1.2 01/01/11-17:20:21 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 ldap/LDAPTemplate.java
 *   Name history : 1 0 handlers/templates/LDAPTemplate.java
 *
 * 1.1 01/01/11-17:20:20 (cstevens)
 *   date and time created 01/01/11 17:20:20 by cstevens
 *
 */

package sunlabs.brazil.ldap;

import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import sunlabs.brazil.template.Template;
import sunlabs.brazil.template.RewriteContext;

import netscape.ldap.LDAPAttribute;
import netscape.ldap.LDAPConnection;
import netscape.ldap.LDAPEntry;
import netscape.ldap.LDAPException;
// import netscape.ldap.LDAPSearchConstraints;
import netscape.ldap.LDAPSearchResults;
import netscape.ldap.LDAPv2;

/**
 * The <code>LDAPTemplate</code> is invoked to process LDAP tags embedded in
 * a document.  This version requires the "ldap40.jar" file from the
 * Netscape Navigator distribution.
 * <p>
 * The LDAPTemplate uses the following special tag: <ul>
 * <li> <code>&lt;ldap&gt;</code>
 * </ul>
 * <p>
 * When an LDAP tag is seen, the LDAP database is searched and the results
 * are used to populate the request properties.
 * <p>
 * The following configuration parameters are used to perform the search.
 * The parameters may appear either in the request properties (preceded by
 * the prefix of this template as specified in the configuration file) or as
 * named arguments in the LDAP tag.
 * <dl class=props>
 * <dt> <code>prefix</code>
 * <dd>	The string prefix for the property names that will be stored
 *	in the request properties to hold the results.  If not specified,
 *	defaults to the prefix of this template as specified in the
 *	configuration file.<p>
 *
 * <dt> <code>dn</code>
 * <dd>	The Distinguished Name (DN) to lookup in the LDAP server.  The format
 *	of a DN is described in RFC-1779.  The "dn" and "search" options are
 *	mutually exclusive.  When "dn" is specified, only zero or one result
 *	will be returned from the LDAP database.  The result (if any) will be
 *	stored in the request properties as follows:
 * <pre>
 * &lt;ldap dn="uid=6105,ou=people,o=WebAuth" prefix=<i>name</i>&gt;
 * &lt;property <i>name</i>.dn&gt;
 * &lt;property <i>name</i>.cn&gt;
 * &lt;property <i>name</i>.sn&gt;
 * &lt;property <i>name</i>.objectclass&gt;</pre>
 *	etc.  The property <code><i>name</i>.dn</code> is the DN that was
 *	found.  Other properties will be defined as shown, based on the
 *	attributes present in the LDAP record. <p>
 *
 * <dt> <code>search</code>
 * <dd> The search filter to use when searching the LDAP server.  The format
 *	of a search filter is described in RFC-1558.  The "search" and "dn"
 *	options are mutually exclusive.  When "search" is specified, zero or
 *	more results will be returned from the LDAP database.  The results
 *	will be stored in the request properties as follows:
 * <pre>
 * &lt;ldap search="(givenname=scott)" prefix=<i>name</i>&gt;
 * &lt;property <i>name</i>.rows&gt;
 * &lt;property <i>name</i>.rowcount&gt;
 * 
 * &lt;property <i>name</i>.0.dn&gt;
 * &lt;property <i>name</i>.0.cn&gt;
 * &lt;property <i>name</i>.0.mail&gt;
 *
 * &lt;property <i>name</i>.1.dn&gt;
 * &lt;property <i>name</i>.1.cn&gt;
 * &lt;property <i>name</i>.1.pager&gt;</pre>
 *	etc.  The property <code><i>name</i>.rows</code> is set to the list
 *	of record indices found, and can be used by the BSL tag
 *	<code>&lt;foreach&nbsp;name=<i>x</i>&nbsp;property=<i>name</i>.rows&gt;</code> to
 *	iterate over all records.  Other properties will be defined for
 *	each of the records found as shown, based on the attributes present
 *	in the each of the LDAP records. <p>
 *
 * <dt> <code>base</code>
 * <dd> The Distinguished Name of the base record that forms the root of the
 *	search tree in the LDAP database.  Used only with the "search" option.
 *	Defaults to "".  This would be a good option to specify in the
 *	configuration file rather than in the LDAP tag. <p>
 *
 * <dt> <code>scope</code>
 * <dd> The scope of the LDAP search, one of <ul>
 * <li> "base"	Search only in base record (specified by the "base" option).
 * <li> "one"	Search only records one level below the base record.
 * <li> "sub"	Search the entire subtree below the base record. </ul>
 *	Used only with the "search" option.  Defaults to "sub".  This would
 *	be a good option to specify in the configuration file rather than in
 *	the LDAP tag. <p>
 *
 * <dt> <code>attributes</code>
 * <dd> The space-delimited list of attribute names to return from the
 *	LDAP "dn" or "search" operation.  If empty or unspecified, all
 *	attributes for the record are returned.  Not all records in the
 *	LDAP database have the same attributes.  Defaults to "". <p>
 *
 * <dt> <code>host</code>
 * <dd> The hostname of the LDAP server, of the form <code>"host"</code>
 *	or <code>"host:port"</code> if the server is not running on the
 *	standard LDAP port.  Defaults to "".  This would be a good option to
 *	specify in the configuration file rather than in the LDAP tag. <p>
 *
 * <dt> <code>authenticate</code>
 * <dd> The Distinguished Name used for authenticating to the LDAP server,
 *	if necessary.  Defaults to "".  This would be a good option to specify
 *	in the configuration file rather than in the LDAP tag. <p>
 *
 * <dt> <code>password</code>
 * <dd> The password sent when the "authenticate" option is used.  Defaults
 *	to "".  
 * <dt> <code>limit</code>
 * <dd> The maxumum number of records returned.  defaults to 1000.
 * <dt> <code>timeout</code>
 * <dd> The maxumum time to wait for a response, in ms. Defaults to
 *	30000 (30s).
 * </dl>
 *
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version		%I%
 */

public class LDAPTemplate
    extends Template 
{
    private static String[]
    split(String str)
    {
	if (str == null) {
	    return null;
	}
	StringTokenizer st = new StringTokenizer(str);
	int n = st.countTokens();
	String[] strs = new String[n];
	for (int i = 0; i < n; i++) {
	    strs[i] = st.nextToken();
	}
	return strs;
    }

    /**
     * Process &lt;ldap&gt; tags.
     */

    public void
    tag_ldap(RewriteContext hr)
    {

	String host = hr.get("host", "");
	String auth = hr.get("authenticate", null);
	String password = hr.get("password", null);
	debug(hr);
	hr.killToken();

	String name = hr.get("prefix");
	name = (name == null) ? hr.prefix : name;
	if (name.endsWith(".") == false) {
	    name += ".";
	}

	String[] attrs = split(hr.get("attributes"));

	LDAPConnection ld = new LDAPConnection();
	int i = 0;
	StringBuffer sb = new StringBuffer();
	try {
	    ld.connect(host, LDAPv2.DEFAULT_PORT, auth, password);

	    String dn = hr.get("dn");
	    if (dn != null) {
		LDAPEntry entry = ld.read(dn, attrs);

		/*
		 * Empirically: An entry != null may be returned even if no
		 * match was found.
		 */
		if ((entry != null) && (entry.getDN() != null)) {
		    shove(hr.request.props, name, entry, attrs);
		}
		return;
	    }

	    String base = hr.get("base", null);
	    String str = hr.get("scope", null);
	    int scope = LDAPv2.SCOPE_SUB;
	    if ("base".equals(str)) {
		scope = LDAPv2.SCOPE_BASE;
	    } else if ("one".equals(str)) {
		scope = LDAPv2.SCOPE_ONE;
	    }

	    String search = hr.get("search");

	    try {
		ld.setOption(LDAPv2.SIZELIMIT,
			Integer.decode(hr.get("limit","1000")));
	    } catch (Exception e) {
		System.out.println("Ldap error: " + e);
	    }

	    try {
		ld.setOption(LDAPv2.TIMELIMIT,
			Integer.decode(hr.get("timeout","30000")));
	    } catch (Exception e) {
		System.out.println("Ldap error: " + e);
	    }
	    
	    LDAPSearchResults results = ld.search(base, scope, search, attrs,
		    false);
	    for (i = 0; results.hasMoreElements(); i++) {
		shove(hr.request.props, name + i + ".", results.next(), attrs);
		sb.append(i).append(' ');
	    }

	    ld.disconnect();
	} catch (LDAPException e) {
	    hr.request.props.put(name + "error", e.errorCodeToString());
	    hr.request.props.put(name + "errorCode", "" +e.getLDAPResultCode());
	    System.out.println("LDAP error: " + e);
	} finally {
	    hr.request.props.put(name + "rows", sb.toString().trim());
	    hr.request.props.put(name + "rowcount", "" + i);
	    try {
		ld.disconnect();
	    } catch (Exception e) {}
	}
    }

    private static void
    shove(Properties props, String prefix, LDAPEntry entry, String[] names)
    {
	props.put(prefix + "dn", entry.getDN());
	if (names == null) {
	    Enumeration e = entry.getAttributeSet().getAttributes();
	    while (e.hasMoreElements()) {
		LDAPAttribute attr = (LDAPAttribute) e.nextElement();
		props.put(prefix + attr.getName(), getAttributeValue(attr));
	    }
	} else {
	    for (int i = 0; i < names.length; i++) {
		LDAPAttribute attr = entry.getAttribute(names[i]);
		String value = (attr == null) ? "" : getAttributeValue(attr);
		props.put(prefix + names[i], value);
	    }
	}
    }

    private static String
    getAttributeValue(LDAPAttribute attr)
    {
	StringBuffer sb = new StringBuffer();
	Enumeration e = attr.getStringValues();
	while (e.hasMoreElements()) {
	    sb.append(e.nextElement()).append('|');
	}
	if (sb.length() > 0) {
	    sb.setLength(sb.length() - 1);
	}
	return sb.toString();
    }
}
