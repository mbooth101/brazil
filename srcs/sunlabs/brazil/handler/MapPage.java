/*
 * MapPage.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2007 Sun Microsystems, Inc.
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
 * Version:  2.4
 * Created by suhler on 99/06/28
 * Last modified by suhler on 07/03/21 15:53:36
 *
 * Version Histories:
 *
 * 2.4 07/03/21-15:53:36 (suhler)
 *   add clear patterns
 *
 * 2.3 05/07/12-10:36:28 (suhler)
 *   document no "https" support.
 *
 * 2.2 03/08/01-16:18:34 (suhler)
 *   fixes for javadoc
 *
 * 2.1 02/10/01-16:36:31 (suhler)
 *   version change
 *
 * 1.19 02/05/01-11:20:56 (suhler)
 *   fix sccs version info
 *
 * 1.18 00/12/27-12:11:48 (suhler)
 *   doc fixes
 *
 * 1.17 00/12/23-11:37:39 (suhler)
 *   allow a "null" prefix to skip rewriting urls beginning with '/'
 *
 * 1.16 00/12/04-08:45:23 (suhler)
 *   change link->src to link->href
 *
 * 1.15 00/11/20-13:21:27 (suhler)
 *   doc fixes
 *
 * 1.14 00/11/13-22:20:20 (suhler)
 *   added <layer src=...> to the rewrite table
 *
 * 1.13 00/07/07-17:01:33 (suhler)
 *   remove System.out.println(s)
 *
 * 1.12 00/05/31-13:47:15 (suhler)
 *   doc cleanup
 *
 * 1.11 00/05/24-11:25:43 (suhler)
 *   add docs
 *
 * 1.10 00/02/25-09:03:34 (suhler)
 *   added the "script/src" pair to the default rewrite table
 *
 * 1.9 00/02/03-14:23:45 (suhler)
 *   off by one error
 *
 * 1.8 00/02/02-14:48:43 (suhler)
 *   remove redundant //'s in mapped url's (I hope this is right)
 *
 * 1.7 99/10/07-09:16:14 (suhler)
 *   added src input tag
 *
 * 1.6 99/10/06-12:17:32 (suhler)
 *   use new rewriting sematics (will change back later)
 *
 * 1.5 99/10/01-14:27:34 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.4 99/09/29-15:54:07 (cstevens)
 *   New HtmlRewriter object, that allows arbitrary rewriting of the HTML (by
 *   templates and others), instead of forcing the templates to return a string
 *   that contained all of the new HTML content in one big string.
 *
 * 1.3.1.1 99/09/10-11:02:44 (suhler)
 *   added regexp pattersn
 *
 * 1.3 99/09/01-15:34:18 (suhler)
 *   add diagnostic messages
 *
 * 1.2 99/08/06-12:04:57 (suhler)
 *   changed provate fields to public
 *
 * 1.2 99/06/28-11:04:09 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/MapPage.java
 *
 * 1.1 99/06/28-11:04:08 (suhler)
 *   date and time created 99/06/28 11:04:08 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import sunlabs.brazil.util.regexp.Regexp;

/**
 * Utility class to rewrite links inside of web pages so they appear to
 * come from a different site.
 * <p>
 * Note: This only works for "http", not "https".
 *
 * @author      Stephen Uhler
 * @version	@(#)MapPage.java	2.4
 */

public class MapPage {
    /**
     * Initialized to all tag/attribute pairs whose attribute values are
     * considered for rewriting.
     */
    public Hashtable tagMap;	// tag attributes to map
    public Hashtable urlMap;	// mapping table
    public Vector patternMap;	// mapping table for glob patterns
    public String prefix;	// prefix to prepend onto mapped url's
    				// that start with "/"
    public static boolean log = false;	// enable/disable diagnostics
    public int count;	// number of tags rewritten

    /**
     * Create a site mapper.  The following table maps all the entity/attribute
     * combinations that are (or could be) URL's to (possibly) rewrite.
     * @param prefix	Every link starting with "/" has the leading
     * 			slash replaced by <code>prefix</code>.  If prefix is
     *			<i>null</i>, then only fully qualified url's are
     *			considered for rewriting.
     */

    public MapPage(String prefix) {
    	this.prefix = prefix;
    	urlMap = null;
	patternMap = new Vector();
    	count = 0;
    	tagMap = new Hashtable(19);
        tagMap.put("a","href");
        tagMap.put("applet","codebase");
        tagMap.put("area","href");
        tagMap.put("base","href");
        tagMap.put("body","background");
        tagMap.put("embed","src"); 
        tagMap.put("form","action");
        tagMap.put("frame","src");
        tagMap.put("img","src");
        tagMap.put("layer","src");
        tagMap.put("link","href");
        tagMap.put("object","codebase");
        tagMap.put("param","value"); // Not sure about this one
        tagMap.put("td","background");
        tagMap.put("th","background");
        tagMap.put("input","src");	// image input items?
        tagMap.put("script","src");	// ???
        log("prefix: " + prefix);
    }

    /**
     * Change the prefix that will replace the leading "/" in a URL.
     */

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        log("prefix: " + prefix);
    }

    /**
     * add a tag/attribute pair to the rewrite list.
     * If the attribute is null, remove the tag.
     */

    public void addTag(String name, String attribute) {
        if (attribute == null) {
	    tagMap.remove(name);
	} else {
	    tagMap.put(name.toLowerCase(), attribute.toLowerCase());
	}
    }

    /**
     * Set the map table for cross-linked sites.
     * Foreach key in the table that matches a URL, replace the key
     * portion of the url with the key's value.
     */

    public void setMap(Hashtable map) {
    	urlMap = map;
    }

    /**
     * Add or remove an item to the map table
     * @param name	The prefix of the url to match
     * @param value	The prefix to replace it with.  If null, 
     *			remove the prefix
     */

    public void
    addMapEntry(String name, String value) {
    	if (value == null && urlMap != null) {
	    urlMap.remove(name);
	    return;
    	} else if (urlMap == null) {
	    urlMap = new Hashtable();
	}
	urlMap.put(name, value);
    }

    /**
     * Add or remove an item to the pattern table
     * @param pattern	The prefix pattern of the url to match
     *			Full tcl8.0-style regexps are supported
     * @param replacement The prefix to replace it with.  If null, 
     *			remove the prefix.  \n's are replaced by the
     *			corrosponding sub-matches in the name
     * <P>
     * Patterns are stored in a vector, with each pattern taking 3
     * concecutive elements: the pattern, the replacement rule, and
     * the compiled expression.  This way they are searched in order.
     * Sample usage:
     *   http://www.([^.]*).com/     /site/\1/
     * will replace the url: http://www.foo.com/a/b.html with
     *   /site/foo/a/b.html
     */

    public void 
    addPatternEntry(String pattern, String replacement) {
	if (!pattern.startsWith("^")) {
	    pattern = "^" + pattern;
	}
	// System.out.println("Adding pattern: " + pattern + " (" + replacement + ")");
	int index = patternMap.indexOf(pattern);
	if (index > 0 && replacement == null) {
	    patternMap.removeElementAt(index);
	    patternMap.removeElementAt(index);
	    patternMap.removeElementAt(index);
	} else if (index > 0) {
	    patternMap.setElementAt(replacement, ++index);
	    patternMap.setElementAt(new Regexp(pattern), ++index);
	} else if (replacement != null) {
	    patternMap.addElement(pattern);
	    patternMap.addElement(replacement);
	    patternMap.addElement(new Regexp(pattern));
	}
    }

    /**
     * Clear the pattern map.
     */

    public void clearPatterns() {
	patternMap.clear();
    }

    /**
     * How many tags have been mapped?
     */

    public int mapCount() {
    	return count;
    }

    /**
     * Rewrite all the url's in this document.  This is accomplished
     * via repeated calls to {@link #convertString}.
     *
     * @param content	The HTML to be processed.
     * @return		The smae HTML, will all URL's rewritten.
     *			URL's starting with "/" have the "/" replaced
     *			with the prefix.  All others are re-written based
     *			on the supplied mapping tables.
     */

    public String convertHtml(String content) {
    	Hashtable h;	// the tag parameters
	HtmlRewriter hr = new HtmlRewriter(content);
	while (hr.nextTag()) {
	    String tag = hr.getTag().toLowerCase();
	    String param = (String) tagMap.get(tag);
	    if (param == null) {
		continue;
	    }
	    String value = hr.get(param);
	    if (value == null) {
		continue;
	    }
	    String fixed = convertString(value);
	    if (fixed != null) {
		hr.put(param, fixed);
	    }
	}
	return hr.toString();
    }

    /**
     * Rewrite a url inside a tag parameter.
     * 
     * @param fix	The value of the tag to be rewritten (fixed)
     * @return		null of the existing value is OK,
     *			otherwise the new value is returned
     */

    public String convertString(String fix) {
	if (fix.startsWith("/")) {
	    if (prefix == null) {
		return null;
	    }
	    count++;
	    String result =  prefix + fix.substring(1);
	    log(fix + " -> " + result);
	    return result;
	}

	// XXX this doesn't deal with "https" !!

	if (fix.startsWith("http://") && urlMap != null) {
	    if (fix.indexOf("/",7) == -1) {
		log("  adding missing /");
		fix += "/";
	    }
	    Enumeration e = urlMap.keys();
	    while (e.hasMoreElements()) {
		String match = (String) e.nextElement();
		log("   " +    fix + " <?> " + match);
		if (fix.startsWith(match)) {
		    count++;
		    String result = urlMap.get(match) +
			    fix.substring(match.length());
		    log(fix + " -> " + result);
		    return result;
		}
	    }
	}

	/*
	 * check the regexp patterns
	 */

	for (int i = 0; i<patternMap.size(); i+= 3) {
	    Regexp exp = (Regexp) patternMap.elementAt(i+2);
	    String replace = exp.sub(fix, (String) patternMap.elementAt(i+1));
	    if (replace != null) { 
		count++;
		return replace;
	    }
	}
	log("No mapping for (" + fix + ")");
	return null;
    }

    /**
     * diagnostic output
     */

    public void
    log(String message) {
        if (log) {
	    System.out.println("MapPage: " + message);
        }
    }
}
