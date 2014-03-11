/*
 * MacroTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2005 Sun Microsystems, Inc.
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
 * Version:  2.10
 * Created by suhler on 01/07/11
 * Last modified by suhler on 05/08/25 10:53:34
 *
 * Version Histories:
 *
 * 2.10 05/08/25-10:53:34 (suhler)
 *   added "isSingleton" and "args" variables to macros
 *
 * 2.9 05/06/16-07:58:44 (suhler)
 *   make sure quotes are removed from tag names
 *
 * 2.8 04/05/28-10:33:31 (suhler)
 *   add "subst=true" for macros in startup file
 *
 * 2.7 04/05/24-15:13:29 (suhler)
 *   remove left-over diagostics
 *
 * 2.6 04/05/21-12:28:36 (suhler)
 *   - The initial macros file is now interpreted as an xml template, and not
 *   a java properties format file [WARNING: backward compatibility]
 *   - main() may be used to convert the old style macro initialization tables
 *   (which no one used 'cause they were too hadr to generate) into the new
 *   format.
 *
 * 2.5 03/07/07-14:02:14 (suhler)
 *   use addClosingTag() convenience macro
 *
 * 2.4 02/12/19-11:37:15 (suhler)
 *   checkpoint
 *
 * 2.3 02/11/14-14:28:06 (suhler)
 *   handle empty bodies
 *
 * 2.2 02/11/05-15:10:12 (suhler)
 *   Accepted child'\''s version in workspace "/home/suhler/brazil/naws".
 *
 * 1.5.1.1 02/11/05-15:07:56 (suhler)
 *   Complete re-do of the macro template.
 *   Hopefully, this one will work better
 *
 * 2.1 02/10/01-16:36:51 (suhler)
 *   version change
 *
 * 1.5 02/07/24-10:46:39 (suhler)
 *   doc updates
 *
 * 1.4 02/01/29-14:53:38 (suhler)
 *   doc lint
 *
 * 1.3 01/07/17-14:17:32 (suhler)
 *   add initial macro file processing
 *
 * 1.2 01/07/16-16:49:36 (suhler)
 *   use new Template convenience methods
 *
 * 1.2 01/07/11-20:12:42 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/MacroTemplate.java
 *
 * 1.1 01/07/11-20:12:41 (suhler)
 *   date and time created 01/07/11 20:12:41 by suhler
 *
 */

package sunlabs.brazil.template;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import sunlabs.brazil.handler.ResourceHandler;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.LexML;
import java.util.Enumeration;

/**
 * Template class for defining macros.  Macros are defined by:
 * <pre>
 * &lt;definemacro name=<i>macro-name</i> [global=true|false]&gt; .... &lt;/definemacro&gt;
 * </pre>
 * The text (.....) has any leading and trailing whitespace
 * removed.
 * <p>
 * To expand a macro: &lt;macro-name name1=value1 ...&gt;.
 * All ${...} constructs in The previously saved macro body 
 * are processed, and the result replaces the <code>macro-name</code>
 * tag.  Attributes provided in the macro name override any variables
 * that exist in the request properties.
 * <p>
 * Several special variables are supplied, as if they were specified
 * as attributes, but only if otherwise not already defined
 * either as an attribute or as a property,
 * <dl>
 * <dd><code>isSingleton</code>
 * <dt>is set to "true" or "false" to indicate the tag has been specified
 * as a singleton.
 * <dt><code>args</code>
 * <dd>is the string of un-parsed arguments to this macro.
 * </dl>
 * <p>
 * Templates are processed by reading the input "document" a tag
 * at a time to generate the output document.
 * By default, the macro body is pushed onto the not-yet-processed
 * input stream; any tags contained in the macro body will be processed.
 * If the <code>defer</code> attribute is present,
 * the macro body is placed onto the output stream instead.
 * This is more efficient, but requires an additional filter pass
 * if the body of the macro contains tags that need to be processed
 * in the current context.
 * <p>
 * <dl class=props>
 * <dt>init
 * <dd>The name of the file (or resource) to read a default set of
 *     macro definitions from.  If an
 *     absolute path isn't specified, the file is taken relative to
 *     the document root.  The default macros are kept in the
 *     SessionManager on a per-server basis.  All macros defined
 *     in the "init" file are global.  All markup in this file outside
 *     of a macro definition is ignored.  If "subst" is present as
 *     an attribute of a macro definition in this file, then all
 *     ${...} are evaluated relative to "server.props" before the
 *     macro is defined.
 * <dt>subst
 * <dd>If specified, then any tags that are not processed by any
 *     templates will have all ${..} contructs in attribute
 *     values substituted.  This subsumes the function of the 
 *     "SubstAllTemplate".
 * </dl>
 * <p>
 * This is an experiment. The current implementation is flawed, although
 * it is less flawed than the previous one.
 * <p>
 * NOTE:<br>
 * The init files in previous releases used java properties format
 * files to define macros;  This version uses xml templates.  See "main"
 * below for a utility to convert the old properties format files to
 * the new format.
 *
 * @author		Stephen Uhler
 * @version		MacroTemplate.java 2.10
 */

public class MacroTemplate extends Template {
    public Properties macroTable=null;	// session specific definitions
    Properties initial=null;		// initial and global properties
    boolean shouldSubst = false;

    /**
     * Read in the inital macros, if needed.
     */

    public boolean
    init(RewriteContext hr) {
	hr.addClosingTag("definemacro");
	shouldSubst = (hr.request.props.getProperty(hr.prefix + "subst") != null);

	// Load in initial macros if macro table doesn't yet exist

        if (macroTable == null) {
	    initial = (Properties) 
			SessionManager.getSession(hr.prefix, "Macros",
			Properties.class);
	    String init = hr.request.props.getProperty(hr.prefix + "init");
	    if (init != null && initial.isEmpty()) {
		loadInitial(hr, init);
	    }
	    macroTable = new Properties(initial);
	}
	return super.init(hr);
    }

    /**
     * load initial macros, if any.
     * XXX  We should load from an XML file, not a properties file.
     */

    void loadInitial_old(RewriteContext hr, String init) {
       try {
	   InputStream in = ResourceHandler.getResourceStream(
		   hr.request.props, hr.prefix, init);
	   initial.load(in);
	   in.close();
	   hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
		   "loading initial macros: " + initial.toString());
       } catch (IOException e) {
	   hr.request.log(Server.LOG_WARNING, hr.prefix,
		   "Can't find macro init file: " + init);
       }
    }

    /**
     * Load initial macros from xml file.  All markup outside of
     * the "definemacro" and "/definemacro" tags are ignored
     * If a "subst" attribute is found, then evaluate all ${..} against
     * server.props before saving the macro body.  "subst" only applies
     * for the "init" macro table.
     */

    void loadInitial(RewriteContext hr, String init) {
        String src = null;
        try {
	   src =  ResourceHandler.getResourceString(
		   hr.request.props, hr.prefix, init);
        } catch (IOException e) {
	   hr.request.log(Server.LOG_WARNING, hr.prefix,
		   "Can't find macro init file: " + init);
        }
        LexML lex = new LexML(src);
        while (lex.nextToken()) {
	    if (lex.getType()==LexML.TAG &&
		    lex.getTag().equals("definemacro")) {
		String name=Format.deQuote(lex.getAttributes().get("name"));
		if (name==null || name.trim().equals("")) {
		    continue;
		}
		boolean doSubst = (lex.getAttributes().get("subst") != null);
		String value = snarfTillClose(lex, "definemacro").trim();
		if (doSubst) {
		    value = Format.subst(hr.server.props, value);
		}
		if (!value.equals("")) {
		    initial.put(name, value);
		    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
			"initial macro: [" + name + "] (" + value.length() +
				" chars)");
		} else {
		    initial.remove(name);
		}
	    } else {
		// System.out.println("Skipping tag: " + lex.getToken());
	    }
	}
	// initial.save(System.out, hr.prefix + " initial macros");
    }

    /**
     * Grab all the markup starting from the current tag until
     * the matching closing tag, and return as a string.
     */
    
    public static String snarfTillClose(LexML lex, String tag) {
	if (!lex.isSingleton()) {
	    StringBuffer sb = new StringBuffer();
	    String start = "<" + tag + " ";
	    String end = "</" + tag + ">"; // XXX not quite
	    int nest = 0;  // set nesting level
	    sb = new StringBuffer();
	    while (lex.nextToken()) {
		String t = lex.getToken();
		lex.getTag();	// force singleton checking!
		if (t.startsWith(start) && !lex.isSingleton()) {
		    nest++;
		} else if (t.equals(end) && (--nest < 0)) {
		    break;
		}
		sb.append(t);
	    }
	    return sb.toString();
	} else {
	    return "";
	}
    }

    /**
     * Define a new macro. use "name" as the macro name. Once defined, the tag
     * <code>&lt;name ...&gt;</code> will be replaced by the contents of the macro
     * named "name".
     * <pre>
     * &lt;definemacro name=nnn [global=true|false]&gt;...&lt;/definemacro&gt;
     * </pre>
     * <ul>
     * <li>If "global" is true, then the macro is defined for the entire server, otherwise
     * it is for the session only.
     * <li>if the body of the macro is an empty string, the macro definition is removed
     * <li>Macros defined for the session override the global ones.
     * <li><code>&lt;definemacro nnn&gt;</code> is a shortcut for
     *     <code>&lt;definemacro name=nnn&gt;</code>
     * </ul>
     */

    public void
    tag_definemacro(RewriteContext hr) {
	String name =  hr.get("name");
	boolean global = hr.isTrue("global");

	if (name == null) {
	    name = hr.getArgs();
	}
	if (name == null) {
	    return;
	}
	debug(hr);
	hr.accumulate(false);
	hr.nextToken();
	String body = hr.getBody().trim();
	hr.accumulate(true);
	Properties p = global ? initial : macroTable;
	if (body.equals("")) {
	   p.remove(name);
	   hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
			"removing macro: " + name);
	} else {
	   p.put(name, body);
	   // XXX we should keep formal parameter list for defaults here
	   hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
			"creating macro: [" + name + "] (" + body.length() + 
			(p==initial ? ") global" : ") local"));
        }
	hr.nextToken();	// eat the /definemacro
	debug(hr);
	hr.killToken();
    }

    /**
     * Run the macro, push formal parameters on the stack first.
     * This examimes all non-processed tags to see if they are macros,
     * and processes those that are.
     * <p>
     * If the parameter <code>defer=true</code> is present, the text of the macro is output
     * directly, with only ${..} substitutions performed.  Otherwise, the markup in the macro
     * body is rescanned and processed.
     */

    public void
    defaultTag(RewriteContext hr) {
        String tag = hr.getTag();
        String script = macroTable.getProperty(tag);
	if (script != null) {
	    debug(hr);
	    String body =  Format.subst(new Props(hr), script);
	    if (!hr.isTrue("defer")) {
	       String rest = hr.lex.rest();
	       if (rest != null) {
	           hr.lex.replace(body + rest);
	       } else {
	           hr.lex.replace(body);
	       }
	       hr.killToken();
	    } else {
	       hr.append(body);
	    }
	} else if (shouldSubst) {
            Enumeration e = hr.keys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                hr.put(key, hr.get(key));
	    }
        }
    }

    /**
     * Special Properties class for use with format.subst() that will
     * return any formal parameters first, then look in request props.
     */

    static class Props extends Properties {
        RewriteContext hr;
	String args = null;

	Props(RewriteContext hr) {
	    this.hr=hr;
	}

	public String
	getProperty(String key) {
	    String value = hr.get(key);
	    if (value == null &&
		    (value = hr.request.props.getProperty(key)) == null) {
		if (key.equals("isSingleton")) {
		    value = hr.isSingleton() ? "true" : "false";
		} else if (key.equals("args")) {
		    return hr.getArgs();
		}
	    }
	    return value;
	}
    }

    /**
     * Convert stdin properties format macro definition files
     * to the new template style.  Use this to update to the new
     * initial macro template format.
     */

    public static void main(String args[]) throws IOException {
	Properties in = new Properties();
	in.load(System.in);
	System.out.println("<!-- converted MacroTemplate init file -->");
	Enumeration e = in.keys();
	while (e.hasMoreElements()) {
	    String key = (String) e.nextElement();
	    String value = in.getProperty(key);
	    System.out.println("\n<definemacro name=\"" + key + 
		    "\" global=\"true\">");
	    System.out.println("  " + value);
	    System.out.println("</definemacro>");
	}
    }
}
