/*
 * SubstPropsHandler.java
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
 * Created by suhler on 04/04/27
 * Last modified by suhler on 06/11/13 14:57:53
 *
 * Version Histories:
 *
 * 1.5 06/11/13-14:57:53 (suhler)
 *   Move MatchString from handler -> util
 *
 * 1.4 04/11/30-15:19:44 (suhler)
 *   fixed sccs version string
 *
 * 1.3 04/10/26-11:27:31 (suhler)
 *   add docs
 *
 * 1.2 04/04/28-14:53:05 (suhler)
 *   doc bug fixes
 *
 * 1.2 04/04/27-14:54:59 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/SubstPropsHandler.java
 *
 * 1.1 04/04/27-14:54:58 (suhler)
 *   date and time created 04/04/27 14:54:58 by suhler
 *
 */

package sunlabs.brazil.properties;

import java.util.Hashtable;
import java.util.StringTokenizer;
import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.regexp.Regexp;
import sunlabs.brazil.util.http.HttpUtil;
import java.io.IOException;

import java.util.Properties;

/**
 * Handler that performs value conversions on ${...} substitutions.
 * For any property whose name matches the supplied regular expression,
 * The source value is "converted" based on a token in the regular expression.
 * <p>
 * This Handler is a generalization of the <code>convert</code> attribute
 * of the <code>get</code> tag of the <code>SetTemplate</code>.  Unlike
 * the implementation in the <code>SetTemplate</code> that implements a 
 * small, fixed set of conversions of property values in the context of
 * <code>get</code>, this handler allows plug-able conversion filters, and
 * performs the conversions any time ${...} substitutions are resolved, not
 * just in the context of the <code>get</code> tag.
 * <p>
 * This requires the addition of new syntax in ${...}
 * substitutions to specify the both the
 * conversion (or filter) to apply, and the value to apply it to.
 * This new syntax is configurable using the <code>match</code>,
 * <code>key</code>, and <code>token</code> attributes, but defaults to:
 * ${filter(value)} where <code>filter</code> represents the conversion
 * filter, and <code>value</code> represents the property name whose contents
 * is filtered.
 * <p>
 * Any class that implements the <code>Convert</code> interface can be 
 * loaded and called to perform filtering.  Filters that implement all the
 * options of the <code>&lt;get ... convert=xxx&gt;</code> conversion options
 * are included.
 * <p>
 * See the examples, below for the details.
 * <dl class=props>
 * <dt>match
 * <dd>A regular expression that matches a property name that is a candidate
 * for filtering.  This expression should have at least 2 sets of ()'s
 * in order to gather values for "key" and "token" below.  The
 * default value is <code>^([a-z]+)\([^)]+\)$</code>
 * <dt>key
 * <dd>The regular expression substitution string used to represent the
 * actual property name to filter.  The default is <code>\\2</code>
 * <dt>token
 * <dd>The regular expression substitution string used to represent the
 * filter name or "token".  The default is <code>\\1</code>
 * <p>
 * Using the defaults for "match", "key", and "token", a property named
 * "foo" would be represented as <code>${xxx(foo)}</code> where
 * "xxx" is the name of the conversion filter.
 * <dt>tokens
 * <dd>
 * A witespace separated list of filter names or "token"s that map the
 * conversion filters to conversion classes.  For each token (e.g. foo), there
 * should be a property of the form "foo.class" which specifies the name
 * of the class that implements the filter, (and implements the Convert
 * interface described below).
 * Any additional properties (e.g. x, y, z) needed to initialize a filter
 * should be present in the properties file as "foo.x, foo.y...".
 * <dt>[token].code
 * <dd>The name to match the "token" in the property name.  The default
 * is "[token]".
 * </dl>
 * <p>
 * This class contains sample implementations of the <code>convert</code>
 * interface.  See below for their functions.
 * 
 * @author	Stephen Uhler
 * @version		1.5
 *
 * @see java.util.Properties
 */

public class SubstPropsHandler implements Handler {

    MatchString isMine;	// check for matching url
    Regexp matchRe;	// expr that matches a property
    String keySub;	// Substitution string that gets the real key
    String tokenSub;	// Substitution string that matches conversion token
    Hashtable map = new Hashtable();	// map from token to mapping function
    static Class[] types = new Class[] {String.class, Properties.class};

    public boolean
    init(Server server, String prefix) {
	isMine = new MatchString(prefix, server.props);
	matchRe = new Regexp(server.props.getProperty(prefix + "match",
		"(^[a-z]+)\\(([^)]+)\\)$"));
	keySub = server.props.getProperty(prefix + "key", "\\2");
	tokenSub=server.props.getProperty(prefix + "token", "\\1");
	StringTokenizer st = new StringTokenizer(
		server.props.getProperty(prefix + "tokens"));

	// build table of conversions

	while(st.hasMoreTokens()) {
	    String token = st.nextToken();
	    String className = server.props.getProperty(token + ".class");
	    String match=server.props.getProperty(token + ".code", token);
	    String name = prefix;
	    if (className == null) {
		className = token;	// use token as prefix;
	    } else {
		name = token;
	    }
	    try {
		Class type = Class.forName(className);
		Object obj = type.newInstance();
		String pre;
		if (name.endsWith(".")) {
		   pre=name;
		} else {
		   pre= name + ".";
		}
		Object[] args = new Object[] {pre, server.props};
		Object result = type.getMethod("init", types).invoke(obj, args);
		if (Boolean.FALSE.equals(result)) {
		    continue;
		}
		if (! (obj instanceof Convert)) {
		    server.log(Server.LOG_WARNING,className, "wrong interface");
		    continue;
		}
		map.put(name, obj);
		server.log(Server.LOG_DIAGNOSTIC, token,
			"instantiating class: " + obj);
	    } catch (Exception e) {
		server.log(Server.LOG_WARNING, prefix, "Oops: " + e);
		e.printStackTrace();
	    }
	}
	return true;
    }

    public boolean
    respond(Request request) throws IOException {
	if (isMine.match(request.url)) {
	    SubstProps p = new SubstProps(request);
	    PropertiesList pl = new PropertiesList(p);
	    pl.addBefore(request.serverProps);
	}
	return false;
    }

    public String toString()  {
	return matchRe + "-" + keySub + "-" + tokenSub + "=" + map;
    }

    /**
     * This class implements a properties object that knows how
     * to extract the "name" and "filter" from a properly constructed
     * name, and to invoke the filter on the value of the encoded
     * name.
     */

    public class SubstProps extends Properties {
	Request r;
	public SubstProps(Request r) {
	    super();
	    this.r = r;
	}

	/**
	 * If the key doesn't exist, but the "derived" key and value
	 * do exist, then return the substituted value
	 */

	public Object get(Object key) {
	    Object val = super.get(key);
	    String newKey;
	    if (val == null && key instanceof String &&
		    null != matchRe.match((String) key)) {
		newKey = matchRe.sub((String) key, keySub);
		val = r.props.get(newKey);
		if (val != null) {
		    String t = matchRe.sub((String) key, tokenSub);
		    val = convert((String)val, t);
		}
	    }
	    return val;
	}

	public String getProperty(String key) {
	    String val = super.getProperty(key);
	    if (val == null && null != matchRe.match(key)) {
		String newKey = matchRe.sub(key, keySub);
		val = r.props.getProperty(newKey);
		if (val != null) {
		    String t = matchRe.sub((String) key, tokenSub);
		    val = convert(val, t);
		}
	    }
	    return val;
	}

	/**
	 * Given a value, compute the converted value.
	 */

	String convert(String value, String token) {
	    String result=value;
	    if (token != null) {
		Convert cnvt = (Convert) map.get(token);
		if (cnvt != null) {
		    result =  cnvt.map(value);
		}
	    }
	    return result;
	}
    }

    /**
     * Class that maps strings to strings.
     */

    public interface Convert {
	/**
	 * This is called once at creation time to provide this
	 * class with configuration information.  Any configuration
	 * parameters required in "p" are prefixed with [prefix].
	 */
	public boolean init(String prefix, Properties p);

	/**
	 * Map the value.
	 */
	public String map(String value);
    }

    // Sample mapping classes

    /**
     * Convert a value to lowercase.
     */

    public static class LowerCase implements Convert {
	public boolean init(String prefix, Properties p) {
	    return true;
	}
	public String map(String value) {
	    return value.toLowerCase();
	}
    }

    /**
     * HTML escape a value.
     */

    public static class Html implements Convert {
	public boolean init(String prefix, Properties p) {
	    return true;
	}
	public String map(String value) {
	    return HttpUtil.htmlEncode(value);
	}
    }

    /**
     * URL encode a String.
     */

    public static class Url implements Convert {
	public boolean init(String prefix, Properties p) {
	    return true;
	}
	public String map(String value) {
	    return HttpUtil.urlEncode(value);
	}
    }

    /**
     * Do a regexp substitution on a value.
     * This takes the following initialization parameters:
     * <dl class=props>
     * <dt>match
     * <dd>A Regular expression that matches the string value.
     * <dt>sub
     * <dd>The regular expression substitution to perform.
     * All occurances of "match" are substututed.
     * </dl>
     */

    public static class Resub implements Convert {
	Regexp match = null;
	String sub = null;

	public boolean init(String prefix, Properties p) {
	    String matchStr=p.getProperty(prefix + "match");
	    sub = p.getProperty(prefix + "sub");
	    match = new Regexp(matchStr);
	    if (match==null || sub == null) {
		System.out.println("Missing 'match' and 'sub'");
		return false;
	    }
	    return true;
	}

	public String map(String value) {
	    return match.subAll(value, sub);
	}
    }
}
