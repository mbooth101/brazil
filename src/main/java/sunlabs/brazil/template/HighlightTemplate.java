/*
 * HighlightTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.
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
 * Version:  2.1
 * Created by suhler on 00/10/16
 * Last modified by suhler on 02/10/01 16:36:50
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:36:50 (suhler)
 *   version change
 *
 * 1.12 02/04/25-13:43:17 (suhler)
 *   doc fixes
 *
 * 1.11 00/12/11-20:24:40 (suhler)
 *   doc typo
 *
 * 1.10 00/12/11-13:30:48 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.9 00/11/20-13:21:47 (suhler)
 *
 * 1.8 00/11/16-11:28:01 (suhler)
 *   added edit on no-process flag
 *
 * 1.7 00/11/16-09:38:21 (suhler)
 *   Redo for better behavior.
 *
 * 1.6 00/11/15-09:48:02 (suhler)
 *   better diagnostics
 *
 * 1.5 00/11/06-10:48:35 (suhler)
 *   make serializable
 *
 * 1.4 00/10/31-10:19:56 (suhler)
 *   doc fixes
 *
 * 1.3 00/10/17-21:38:46 (suhler)
 *   - matchcase was backwards
 *   - don't highlight in head sections
 *   - don't terminate filtering if no highlight set
 *
 * 1.2 00/10/17-12:20:34 (suhler)
 *   lint
 *
 * 1.2 00/10/16-13:35:25 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/HighlightTemplate.java
 *
 * 1.1 00/10/16-13:35:24 (suhler)
 *   date and time created 00/10/16 13:35:24 by suhler
 *
 */

package sunlabs.brazil.template;

import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.regexp.Regexp;
import sunlabs.brazil.util.Format;
import java.util.Properties;
import java.io.Serializable;

/**
 * Template class for highlighting text that matches a regular expression.
 * All text between html/xml entities is matched against a regular expression.
 * For each portion of text that matches the expression, a pair of
 * html/xml tags is added on either side of all matched text.
 * Highlighting is automatically turned off inside of head, script, style, 
 * and server tags.
 * <p>
 * Properties.  These are recomputed for every page that <code>
 * highlight</code> changes.
 * <dl class=props>
 * <dt>highlight	<dd>A regular expression that with match any text
 *			between entities.
 * <dt>tag		<dd>the html/xml tag pair that will be added before and
 *			after all text maching "highlight", above. The 
 *			default is "&lt;font&gt; ..... &lt;/font&gt;
 * <dt>options		<dd>the set of name=value options that will be added
 *			to the starting tag of the tag pair, above.  The
 *			default is "color=red".
 * <dt>matchCase	<dd>If specifies, matches are case sensitive.
 *			The default is to ignore case when matching.
 * <dt>substitute	<dd>The string to substitute for the matched text.
 *			This is for advanced uses.  If specified, the values
 *			for <code>tag</code> and <code>options</code> are
 *			ignored.  The default is:
 *                      &lt;${tag} ${options}&gt;&amp;&lt;/${tag}&gt;
 *			The format of the string is a regular expression
 *			substitution string, which supports ${} style
 *			variable substitutions from the request properties.
 * <dt>mustHighlight	<dd>If not set, the entire document is surrounded
 *			by implicit <code>highlight</code> tags.  If set
 *			no highlighting will take place until an actual
 *			<code>highlight</code> tag is present.
 * <dt>exit		<dd>If set, the template "init" method will
 *			return false, and no further processing will
 *			take place.  This is useful if this template
 *			is used by itself.
 * </dl>
 * <p>
 * The following html tags are processed:
 * <dl>
 * <dt>highlight
 * <dt>/highlight
 * <dd> Only text between these tags is considered for highlighting.
 * <dt>nohighlight
 * <dt>/nohighlight
 * <dd>Temporarily undoes the effect of a <code>highlight</code> tag.
 * In the current implementation, <code>highlight</code> and 
 * <code>nohighlight</code> don't nest.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version		2.1 HighlightTemplate.java %V% 0
 */

public class HighlightTemplate extends Template implements Serializable {
    static final String HIGHLIGHT = "highlight";	// text to highlight
    static final String TAG = "tag";			// tag to surround with
    static final String OPTIONS = "options";		// "tag" attributes
    static final String MATCHCASE = "matchCase";	// set for case sens.
    static final String SUBSTITUTE = "substitute";	//
    static final String MUST = "mustHighlight";		//
    static final String EXIT = "exit";			//

    String sub;		// what we substitute
    boolean matchCase;	// true to do cases sensitive matching
    Regexp re = null;		// regular expression to match
    String lastExp = "";	// cached exp string
    String exp;			// exp string

    int dont;			// if > 0, don't highlight
    boolean inHighlight;	// in a highlight tag
    boolean inNoHighlight;	// in a no highlight tag

    /**
     * This gets called at every page, at the beginning.  If this is our first
     * time, get the config stuff out of the request properties.
     */

    public boolean
    init(RewriteContext hr) {
	Properties props = hr.request.props;
	dont=0;
	exp = props.getProperty(hr.prefix + HIGHLIGHT, "");
	if (exp.equals("")) {
	    re = null;
	    if (props.getProperty(hr.prefix + EXIT) != null) {
		return false;
	    }
	} else if (!lastExp.equals(exp)) {
	    lastExp = exp;
	    matchCase = (props.getProperty(hr.prefix + MATCHCASE) != null);
	    re = new Regexp(exp, !matchCase);
	    String tag = props.getProperty(hr.prefix + TAG, "font");
	    String options = props.getProperty(hr.prefix + OPTIONS,
		"color=red");
	    sub = props.getProperty(hr.prefix + SUBSTITUTE);
	    if (sub == null) {
		sub = "<" + tag + " " + options + ">&</" + tag + ">";
	    } else {
	        sub = Format.subst(props, sub);
	    }
	    inHighlight = props.getProperty(hr.prefix + MUST) == null;
	    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
		"Setting: (" + sub + ") for: " + exp);
	}
	return true;
    }

    /**
     * Gets all text between tags - highlighting it appropriately.
     * To restrict the tag set, define the entities and set the shouldHighlight
     * flag appropriately.
     */

    public void
    string(RewriteContext hr) {
    	String body = hr.getToken();
	// System.out.println("Re " + inHighlight + inNoHighlight + dont + re);
        if (inHighlight && !inNoHighlight && dont==0 &&
		re != null && re.match(body) != null) {
            hr.append(re.subAll(body, sub));
        }
    }

    /**
     * Don't do highlight inside the following sections
     */

    public void tag_head(RewriteContext hr)          { push(); }
    public void tag_slash_head(RewriteContext hr)    { pop(); }
    public void tag_script(RewriteContext hr)        { push(); }
    public void tag_slash_script(RewriteContext hr)  { pop(); }
    public void tag_style(RewriteContext hr)         { push(); }
    public void tag_slash_style(RewriteContext hr)   { pop(); }
    public void tag_server(RewriteContext hr)        { push(); }
    public void tag_slash_server(RewriteContext hr)  { pop(); }

    void push() { dont++; }
    void pop() { if (dont > 0) { dont--; } }

    /**
     * The special entities <code>highlight</code> and
     * <code>nohighlight</code>
     * may be used to turn highlighting on or off in certain areas.
     */

    public void
    tag_highlight(RewriteContext hr) {
	inHighlight=true;
    }

    public void
    tag_slash_highlight(RewriteContext hr) {
	inHighlight=false;
    }

    public void
    tag_nohighlight(RewriteContext hr) {
	inNoHighlight=true;
    }

    public void
    tag_slash_nohighlight(RewriteContext hr) {
	inNoHighlight=false;
    }
}
