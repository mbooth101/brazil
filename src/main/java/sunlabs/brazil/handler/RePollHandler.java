/*
 * RePollHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2007 Sun Microsystems, Inc.
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
 * Version:  2.2
 * Created by suhler on 01/01/31
 * Last modified by suhler on 07/01/29 15:36:16
 *
 * Version Histories:
 *
 * 2.2 07/01/29-15:36:16 (suhler)
 *   remove unused imports
 *
 * 2.1 02/10/01-16:36:33 (suhler)
 *   version change
 *
 * 1.13 02/08/01-13:45:02 (suhler)
 *   re-arrange code to make testing easier
 *
 * 1.12 02/07/11-15:01:51 (suhler)
 *   use the new getContent() method in httpRequest that does a better job
 *   of charset decoding
 *
 * 1.11 02/07/10-11:26:03 (suhler)
 *   add encoding option
 *
 * 1.10 01/09/13-09:23:35 (suhler)
 *   remove uneeded import
 *
 * 1.9 01/08/14-16:38:19 (suhler)
 *   doc lint
 *
 * 1.8 01/08/06-17:46:31 (suhler)
 *   add [prefix].key option to name keys based on sub-matches
 *
 * 1.7 01/08/01-17:16:08 (suhler)
 *   - require at least on 'E' flag
 *   - change default falgs to SFE
 *   - lint docs
 *
 * 1.6 01/08/01-14:16:03 (suhler)
 *   Completely changed the way properties are extracted.
 *   OLD way:  Create a regular expression that turned the content into
 *   a java properties file, then "loaded" it
 *   NEW way   Matches and sub-matches are automatically extracted into
 *   properties based on numerical indeces (or names)
 *
 * 1.5 01/07/11-16:26:29 (suhler)
 *   doc update
 *
 * 1.4 01/06/04-11:05:06 (suhler)
 *   partially implemented new semantics - checked in temporarily
 *
 * 1.3 01/03/06-09:06:00 (suhler)
 *   ???
 *
 * 1.2 01/01/31-12:13:37 (suhler)
 *   about to add tcl post-processing option
 *
 * 1.2 01/01/31-10:19:38 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/RePollHandler.java
 *
 * 1.1 01/01/31-10:19:37 (suhler)
 *   date and time created 01/01/31 10:19:37 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.IOException;
import java.util.Properties;
import java.util.Hashtable;
import java.util.StringTokenizer;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.util.regexp.Regexp;
import sunlabs.brazil.util.regexp.Regsub;
import sunlabs.brazil.util.http.HttpRequest;

/**
 * Do regsub processing on content to extract properties.
 * <p>
 * Properties:
 * <dl class=props>
 * <dt>encoding	<dd>The character set encoding to use when converting
 *		the request results to a string.  Defaults to the
 *		default encoding.
 * <dt>prepend	<dd>The string to prepend to all properties.
 *		Extracted properties will contain the the "re" token
 *		as an additional prefix.
 * <dt>re	<dd>the list of "re" tokens to process in order.
 *		Each "re" token has the following attributes:
 * <dt><i>re</i>.exp
 * <dd>The regular expression to search for.
 * <dt><i>re</i>.sub
 * <dd>The regular expression substitution pattern. If 'E' is specified,
 *     the substitution is done after the extraction.
 * <dt><i>re</i>.names
 * <dd> A white-space delimited set of tokens to use instead of numerical
 *	indices to name the properties.
 *      The first name in the list names the entire match, the remaining 
 *      names name the sub-expressions.  If there are more properties 
 *      extracted than names provided, the "left over" properties will
 *      have numerical indeces.  This implies 'E'.
 *      <p>
 *      If the name "X" is used, no property will be extracted for that
 *      match.
 * <dt><i>re</i>.key
 * <dd> The index of the sub-match (starting at 1) that will be used to 
 *      name the row number portion of the property name instead of a
 *	counter.  This is useful if one of the sub-matches will be
 *	unique for each matching pattern.  This option is ignored if
 *	the "O" flag is specified, as there will be only one match so
 *	no "key" is required.
 *      
 * <dt><i>re</i>.flags
 * <dd> One or more ASCII flags to control how this "re" is processed.
 *	Consists of one or more of The following (defaults to "SFE"):.  
 *	Characters not on this list are ignored.
 *  <ul>
 *  <li><b>E	</b> Extract current result into server properties.  See
 *		    the rules for naming the properties, below.  At least
 *		    one regular expression Must have an "E" flag.
 *  <li><b>F	</b> Process if previous "RE" failed.
 *  <li><b>I	</b> Ignore case in expression
 *  <li><b>O	</b> only do one substitution or extraction, not all
 *  <li><b>R	</b> Reset content to original before proceeding
 *		Otherwise, the result of the previous substitution
 *		(if any) is used.
 *  <li><b>S	</b> Process if previous "RE" succeeded
 *  </ul>
 * </dl>
 * <p>
 * First remote content is obtained.  Then 
 * each regular expression token is processed in turn for the purpose
 * of extracting portions of that content into server properties.
 * [re].sub is used to transform the content before attempting
 * to extract properties.
 * <p>
 * Content is extracted into the following properties.
 * <dl>
 * <dt>prepend.[re].[m].[n]
 * <dd> The result of the expression associated with token "re".  'n' is
 *	the sub-expression number, and 'm' is the match number, both 
 *	starting at '0'.  If the 'O' flag is specified, there can
 *	only be one value for 'm', so it is not included (e.g. the
 *	name of the property will be "prepend.[re].[n]).
 * <dt> prepend.[re].matches
 * <dd> A list of matches, that may be used as an iterator to <b>foreach</b>.
 * <dt> prepend.[re].subexpressions
 * <dd> The number of sub-expressions associated with [re].
 * </dl>
 *
 * @author      Stephen Uhler
 * @version	%V% RePollHandler.java 2.2 
 */

public class RePollHandler extends PollHandler {
    String delim;
    Extract[] res = null;
    String urlPrefix;
    String encoding;		// character coding for extracting results

    public boolean
    init(Server server, String prefix) {
	urlPrefix = server.props.getProperty(prefix + "prefix", "/");
	encoding = server.props.getProperty(prefix + "encoding",
       		server.props.getProperty("encoding"));
	String list = server.props.getProperty(prefix + "re");
	if (list == null) {
	    server.log(Server.LOG_WARNING, prefix,
	    		"Missing " + prefix + "re");
	    return super.init(server, prefix);
	}
	StringTokenizer st = new StringTokenizer(list);
	res = new Extract[st.countTokens()];
	int i = 0;
	boolean hasExtract = false;
	while(st.hasMoreTokens()) {
	    String names[] = null;
	    String name = st.nextToken();
	    String exp = server.props.getProperty(name + ".exp");
	    String sub = server.props.getProperty(name + ".sub");
	    String flags = server.props.getProperty(name + ".flags", "SFE");
	    if (exp == null) {
		server.log(Server.LOG_WARNING, prefix,
			"Can't find " + name + ".exp in properties!!");
		return false;
            }
	    String nameList = server.props.getProperty(name + ".names");
	    int n = 0;
	    if (nameList != null) {
	        StringTokenizer lt = new StringTokenizer(nameList);
		names = new String[lt.countTokens()];
		while(lt.hasMoreTokens()) {
		    names[n++] = lt.nextToken();
		}
	    }

	    int keyIndex = 0;
	    try {
		String s = server.props.getProperty(name + ".key");
		keyIndex = Integer.decode(s).intValue();
	    } catch (Exception e) {}

            res[i] =  new Extract(name, exp, sub, flags, names, keyIndex);
            if (res[i].extract()) {
                hasExtract = true;
	    }
            i++;
	}
	if (!hasExtract) {
	    server.log(Server.LOG_WARNING, prefix,
		    "No properties will be extracted!!");
	    return false;
	}
        return super.init(server, prefix);
    }

    /**
     * Allow The url and post data (if any) to be changed.
     * A query parameter of the form "url=xxx" replaces the current url.
     * A query parameter of the form "post=xxx" replaces the post data, if any
     * was initially defined.
     */

    public boolean
    respond(Request request) {
	if (!request.url.startsWith(urlPrefix)) {
	    return false;
	}
	Hashtable query = request.getQueryData();
	String tmp = (String) query.get("url");
	if (tmp != null) {
	    url = tmp;
	    server.log(Server.LOG_DIAGNOSTIC, prefix,
		    "Got new url: " + url);
	}
	tmp = (String) query.get("post");
	if (post != null && tmp != null) {
	    post = tmp;
	    server.log(Server.LOG_DIAGNOSTIC, prefix,
		    "Got new post data: " + post);
	}
        return false;
    }

    /**
     * Fill the properties by extracting fields from the response.  This overrides fillProps.
     */

    public void
    fillProps(Properties props, HttpRequest target) throws IOException {
        if (res == null) {
            super.fillProps(props, target);
            return;
	}
	server.log(Server.LOG_INFORMATIONAL, prefix,
		"polling " + url);
	processText(props, target.getContent(encoding));
	return;
    }

    /**
     * Process the contents as a string through the regular expressions.
     * This is public, and separate from fillProps to make unit testing easier.
     */

    public void
    processText(Properties props, String data) {
	server.log(Server.LOG_DIAGNOSTIC, prefix,
		"Polled: " + data);
	String reset = new String(data);
	boolean previous = true;	// result of previous expression

	for (int i=0; i<res.length; i++) {
	    Extract ext = res[i];
	    if ((previous &&  ext.succeed()) || ((!previous) && ext.fail())) {
		String str = ext.reset() ? reset : data;
		if (ext.extract()) {
		    ext.extract(props, data);
		}
		if (ext.replace()) {
		    data = ext.replace(data);
		}
	        previous = ext.result();
	    }
	}
    } 

    /**
     * A "bag" to store regular expression extraction instructions
     */

    public static class Extract {
        String name;	// The name of the res
        String names[]; // Names to use instead of sub-exp's
        Regexp exp;	// the compiled regular expression
        String sub;	// The substituted value
        boolean reset;	// Reset before using
        boolean succeed;// Run only if prevous succeeded
        boolean fail;	// run only if previous failed
        boolean ok;	// Condition of last replace;
        boolean single;	// only find first match
        boolean extract;// do an extraction
        int subIndex;	// use this sub-match instead of index #
        String nullStr = "";  // what to extract for null match

        Extract(String name, String exp, String sub, String flags,
        		String[] names, int subIndex) {
            this.name = name;
            this.names = names;
            if (!name.endsWith(".")) {
                this.name += ".";
	    }
            this.exp=null;
            try {
	       this.exp = new Regexp(exp, (flags.indexOf("I") != -1));
	    } catch (IllegalArgumentException e) {
	        System.out.println("Token " + name + ": \"" + exp +
			"\" Is not valid: " + e.getMessage());
		throw e;
	    }
	    this.sub = sub;
	    this.subIndex = subIndex;
	    reset = (flags.indexOf("R") != -1);
	    fail = (flags.indexOf("F") != -1);
	    succeed = (flags.indexOf("S") != -1);
	    single = (flags.indexOf("O") != -1);
	    extract = (flags.indexOf("E") != -1);
	    ok = false;
        }

        /**
         * Do the substitution
         */

	public String replace(String data) {
	   String result;
	   if (single) {
	       result =  exp.sub(data, sub);
	       if (result == null) {
	           ok = false;
	           result = data;
	       } else {
	           ok = true;
	       }
	   } else {
	       result =  exp.subAll(data, sub);
	       ok = (result != data);
	   }
	   return result;
	}

        /**
         * Do the extraction
         * @param props		where to put the extracted properties
         * @param data		the data to extract from
         * @return	The number of extractions performed.
         */

	public int extract(Properties props, String data) {
	    int matches=0;
	    int subMatches =  exp.subspecs();
	    StringBuffer matchList = new StringBuffer();
	    String[] sub = new String[subMatches];
	    if (single && exp.match(data, sub)) {
	        matches=1;
	        matchList.append(" " + matches);
	        for (int i = 0; i < sub.length; i++) {
	            if (names != null && names.length > i) {
	                if (!names[i].equals("X")) {
			    props.put(name + names[i], sub[i]);
			}
	            } else {
			props.put(name + i, sub[i]);
		    }
	        }
	    } else if (!single)  {
	        Regsub rs = new Regsub(exp, data);
	        while(rs.nextMatch()) {
		    matches++;
		    matchList.append(" " + matches);
		    if (names != null) {
	                if (!names[0].equals("X")) {
			    props.put(name + matches + "." + names[0],
				    rs.matched());
			}
		    } else {
			props.put(name + matches, rs.matched());
		    }

		    // use sub-match as an index instead of a number

		    String otherKey = null;
		    if (subIndex > 0 && subIndex < subMatches) {
			otherKey =  rs.submatch(subIndex);
		    }
		    if (otherKey == null) {
			otherKey = "" + matches;
		    }
		    for (int i = 1; i < subMatches; i++) {
		        String key;
			if (names != null && names.length > i) {
			    if (names[i].equals("X")) {
			        continue;
			    }
		            key = name + otherKey + "." + names[i];
			} else {
		            key = name + otherKey + "." + i;
			}
		        props.put(key,
			     (rs.submatch(i)==null ? nullStr : rs.submatch(i)));
	  	    }
	        }
	    }
	    ok =  (matches > 0);
	    props.put(name + "matches", matchList.toString());
	    return matches;
        }

	/**
	 * true if the previous replace worked
	 */

	public boolean replace() {
	    return (sub != null);
	}

	public boolean extract() {
	    return extract;
	}

	public boolean result() {
	    return ok;
	}

	public boolean reset() {
	    return reset;
	}

	public boolean fail() {
	    return fail;
	}

	public boolean succeed() {
	    return succeed;
	}

	public String toString() {
	    return name;
	}
    }
}
