/*
 * TemplateRunner.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2006 Sun Microsystems, Inc.
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
 * Contributor(s): cstevens, drach, suhler.
 *
 * Version:  2.4
 * Created by suhler on 99/05/06
 * Last modified by suhler on 06/08/01 14:07:44
 *
 * Version Histories:
 *
 * 2.4 06/08/01-14:07:44 (suhler)
 *   added templateFromTag() to allow greater introspection of our template classes
 *   minor doc fixes
 *   .
 *
 * 2.3 03/08/01-16:19:00 (suhler)
 *   fixes for javadoc
 *
 * 2.2 03/07/09-12:27:29 (suhler)
 *   added tagPrefix methods
 *
 * 2.1 02/10/01-16:36:48 (suhler)
 *   version change
 *
 * 1.34 02/07/24-10:47:07 (suhler)
 *   doc updates
 *
 * 1.33 02/07/19-15:10:01 (suhler)
 *   added (experimental) tagPrefix option to handle XML namespaces
 *
 * 1.32 02/06/24-15:39:47 (suhler)
 *   Better diagnostics for templates without a public constructor
 *
 * 1.31 02/05/01-11:28:06 (suhler)
 *   fix sccs version info
 *
 * 1.30 01/10/03-13:54:03 (suhler)
 *   added "tags seen" for diagnostic purposes
 *
 * 1.29 01/08/03-18:13:04 (suhler)
 *   remove white space from class names
 *
 * 1.28 01/05/11-14:52:22 (suhler)
 *   Make sure templates are Assignable from "TemplateInterface" instead
 *   of "Template", so templates don't have to extend Template
 *
 * 1.27 01/05/02-15:33:49 (drach)
 *   Add template tokens.
 *
 * 1.26 01/04/09-18:36:24 (suhler)
 *   fixed bug in
 *   _xx
 *
 * 1.25 00/12/11-13:30:24 (suhler)
 *   doc typo
 *
 * 1.24 00/11/21-13:24:21 (suhler)
 *   better error messages
 *
 * 1.23 00/10/31-10:19:33 (suhler)
 *   doc fixes
 *
 * 1.22 00/10/20-15:43:17 (suhler)
 *   Removed getContent() due to race condition.  process() now returns
 *   the content.
 *
 * 1.21 00/10/16-13:34:36 (suhler)
 *   Added 2 more methods:
 *   "string" dispatches on all text between tags
 *   "defaultTag" dispatches on all tags that aren't otherwised matched
 *   .
 *
 * 1.20 00/07/06-15:59:26 (suhler)
 *   doc fixes
 *
 * 1.19 00/07/05-14:11:19 (cstevens)
 *   Server object is in RewriteContext used by templates, so templates (such as
 *   the TclServerTemplate) can be initialized with the server and prefix, similar
 *   to how Handlers are initialized.
 *
 * 1.18 00/05/31-13:50:18 (suhler)
 *   name change, docs
 *
 * 1.17 00/04/12-15:55:34 (cstevens)
 *   Work with SerialPersist SessionManager
 *
 * 1.16 00/02/11-12:44:52 (suhler)
 *   handle comments
 *
 * 1.15 99/11/30-09:47:57 (suhler)
 *   remove diagnostics
 *
 * 1.14 99/11/16-19:08:11 (cstevens)
 *   wildcard imports.
 *
 * 1.13 99/10/28-17:23:17 (cstevens)
 *   ChangedTemplate
 *
 * 1.12 99/10/25-15:37:49 (cstevens)
 *   working on templates with symbolic names.
 *
 * 1.11 99/10/21-18:14:30 (cstevens)
 *   TemplateHandler now takes a list of Templates, rather than just one.  When
 *   parsing an HTML file, it will now dispatch to the union of all the tag
 *   methods defined in the list of Templates.  In that way, the user can
 *   compose things like the BSLTemplate to iterate over request properties with
 *   the PropsTemplate to substitute in their values.  Otherwise, it would have
 *   required N separate passes (via N separate TemplateHandlers) over the HTML
 *   file, one for each Template and/or level of recursion in the BSLTemplate.
 *
 * 1.10 99/10/14-14:57:26 (cstevens)
 *   resolve wilcard imports.
 *
 * 1.9 99/10/06-12:18:48 (suhler)
 *
 * 1.8 99/09/29-16:05:59 (cstevens)
 *   New HtmlRewriter object, that allows arbitrary rewriting of the HTML (by
 *   templates and others), instead of forcing the templates to return a string
 *   that contained all of the new HTML content in one big string.
 *
 * 1.7 99/08/04-18:44:11 (suhler)
 *   I forget, sorry
 *
 * 1.6 99/06/28-10:51:10 (suhler)
 *   separate template processing class
 *
 * 1.5 99/05/25-09:21:21 (suhler)
 *   modified to use new HtmlMunger
 *
 * 1.4 99/05/24-17:38:51 (suhler)
 *   Changed to use new HtmlMunger.  I already did this change once, but
 *   it got lost somehow ???
 *
 * 1.3 99/05/13-09:23:00 (suhler)
 *   ??
 *
 * 1.2 99/05/06-15:22:53 (suhler)
 *   added the ability to use a mapping table
 *
 * 1.2 99/05/06-14:12:25 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 handlers/templates/TemplateRunner.java
 *   Name history : 1 0 handlers/Template.java
 *
 * 1.1 99/05/06-14:12:24 (suhler)
 *   date and time created 99/05/06 14:12:24 by suhler
 *
 */

package sunlabs.brazil.template;

import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.util.regexp.Regexp;
import sunlabs.brazil.util.regexp.Regsub;
import sunlabs.brazil.util.LexHTML;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;

/**
 * Class for processing html templates.
 * An html template is an ordinary html string, with additional
 * application specific tags added (sort of like XML).  Each
 * tag is mapped to a java method of a template class, that rewrites its
 * tag into normal html.
 * <p>
 * The mechanism used to map templates into sessions is inadaquate, and
 * should be fixed in a future version.  In the current implementation,
 * Each session maintains its own set of template instances.  Instance
 * variables in template classes may be used to hold session specific
 * state.  Calls to a template are synchronized on the session id;
 * only one request per session is dealt with simultaneously.
 *
 * @author Colin Stevens
 * @author Stephen Uhler
 * @version %W
 */

public class TemplateRunner
{
    static final String TAG_PREFIX = "tagPrefix"; 
    static final String TEMPLATE = "template";

    private Server server;
    private String prefix;		// prefix of the invoker

    private int tagsProcessed = 0;
    private int tagsSeen = 0;
    private String error = null; 	// The last error

    private Class[] types;		// Types of templates.
    private String[] prefixes;		// Template specific prefixes
    private Hashtable dispatch;		// Map: tag -> class index, method.

    private static final Regexp hex = new Regexp("_x[0-9a-zA-Z][0-9a-zA-Z]");
    private static final Regexp.Filter hexFilter = new Regexp.Filter() {
	public boolean filter(Regsub rs, StringBuffer sb) {
	    String match = rs.matched();
	    int hi = Character.digit(match.charAt(2), 16);
	    int lo = Character.digit(match.charAt(3), 16);
	    sb.append((char) ((hi << 4) | lo));

	    return true;
	}
    };

    private static class Dispatch {
	int index;
	Method method;
	String prefix;

	public Dispatch(int index, Method method, String prefix) {
	    this.index = index;
	    this.method = method;
	    this.prefix = prefix;
	}
    }

    /**
     * Process an HTML template with a template processing class.
     * We peruse the template class, looking at all of its methods.
     * When when we process a template, we match the html tags against
     * the declared methods in the template class.  Each
     * method name of the form <code>tag_<i>xxx</i></code> or
     * <code>tag_slash_<i>xxx</i></code> is invoked when the corrosponding
     * <i>&lt;xxx&gt;</i> or <i>&lt;/xxx&gt;</i> tag is found.
     * <p>
     * Each instance of <code>_x<i>nn</i></code> in the method name
     * is replaced by the corrosponding hex code for the character.
     * This allows non-printable tags to to be processed with templates.
     * <p>
     * The methods <code>init</code> and <code>done</code> are each called
     * once, at the beginning and end of the page respectively.  These methods
     * are called for all templates, in the order they are specified in
     * the <i>templates</i> parameter.
     * <p>
     * There are three methods that may be defined that don't follow the naming
     * convention described above. They are:
     * <ul>
     * <li><code>comment</code><br>
     * is called for each html/XML comment.
     * <li><code>string</code><br>
     * is called for all text between any tags.
     * <li><code>defaultTag</code><br>
     * is called for every tag that does not specifically have a tag method.
     * If more than one template defines one of these methods, only the
     * first template's method will be called.
     * </ul>
     * <p>
     * If the server property "tagPrefix" associated with each template's
     * properties prefix exists, it is used to prefix each tag name
     * (this feature is for experimental support of XML namespaces,
     * and probably doesn't belong here).
     * <p>
     * @param	server
     *		The HTTP server that created the <code>Handler</code> or
     *		<code>Filter</code> that invoked this
     *		<code>TemplateRunner</code>
     * @param	prefix
     *		The prefix associated with the parent <code>Handler</code>
     *		or <code>Filter</code>
     * @param	names
     *		The names of the Template classes or property prefixes (i.e.
     *          tokens) that, when concatenated with ".class" define a property
     *          that names a Template class.  This TemplateRunner will
     *		dispatch to the methods described by the union of all the
     *		tag methods in the given Template classes.
     *		<p>
     *		The <code>init</code> and <code>done</code> methods for each
     *		template specified will be called in order.  If any of
     *		the calls returns <code>false</code>, this handler terminates
     *		and no output is generated.
     *		<p>
     *		The names "comment", "string",  and "defaultTag" are
     *		handled specially.
     */
    public
    TemplateRunner(Server server, String prefix, String names)
	throws ClassNotFoundException, ClassCastException
    {
	this.server = server;
	this.prefix = prefix;

	dispatch = new Hashtable();

	Vector types = new Vector();
	Vector prefixes = new Vector();
	int count = 0;

	StringTokenizer st = new StringTokenizer(names);
	while (st.hasMoreTokens()) {
	    String temPrefix = null;
	    String temName = st.nextToken();
	    // System.out.println("Processing template: " + temName);
	    String className = server.props.getProperty(temName + ".class");
	    if (className == null) {
		className = temName;
		temPrefix = prefix;
	    } else {
		temPrefix = temName + ".";
	    }

	    Class temType = Class.forName(className.trim());
            if (TemplateInterface.class.isAssignableFrom(temType) == false) {
		throw new ClassCastException(temType.getName());
	    }

	    types.addElement(temType);
	    prefixes.addElement(temPrefix);

	    Method[] methods = temType.getMethods();
	    String tagPrefix = server.props.getProperty(temPrefix + TAG_PREFIX,
		    server.props.getProperty(prefix + TAG_PREFIX, ""));
	    for (int i = 0; i < methods.length; i++) {
		Method method = methods[i];
		String name = method.getName();
		if (name.equals("comment") && dispatch.get(name) == null) {
		    dispatch.put(name, new Dispatch(count, method, temPrefix));
		    // System.out.println("Found comment in: " + temName);
		    continue;
		}
		if (name.equals("string") && dispatch.get(name) == null) {
		    dispatch.put(name, new Dispatch(count, method, temPrefix));
		    // System.out.println("Found string in: " + temName);
		    continue;
		}
		if (name.equals("defaultTag") && dispatch.get(name) == null) {
		    dispatch.put(name, new Dispatch(count, method, temPrefix));
		    // System.out.println("Found defaultTag in: " + temName);
		    continue;
		}
		if (name.startsWith("tag_") == false) {
		    continue;
		}
		name = name.substring(4);
		if (name.startsWith("slash_")) {
		    name = "/" + tagPrefix + name.substring(6);
		} else {
		    name = tagPrefix + name;
		}
		name = hex.sub(name, hexFilter);

		if (dispatch.get(name) == null) {
		    dispatch.put(name, new Dispatch(count, method, temPrefix));
		}
	    }
	    count++;
	}

	types.copyInto(this.types = new Class[count]);
	prefixes.copyInto(this.prefixes = new String[count]);
    }

    /**
     * Process an html template file, using the supplied template processing
     * Return the content of the template just processed, or null if
     * there was no template processed.
     *
     * @param   content
     *		The template.
     *
     * @param   sessionId
     *		An arbitrary identifier used to locate the correct instance
     *		of the template class for processing this template.  The
     *		first time an identifier is used, a new instance is created.
     *
     * @param   args
     *		The arguments passed to the templates init method.
     *
     * @return 	content or null
     */
    public String
    process(Request request, String content, String sessionId)
    {
	tagsProcessed = 0;
	tagsSeen = 0;
	// error = null;

	/*
	 * Now look up the session object.  If this is a new session, then we
	 * get a new object, otherwise we get the last one we used for this
	 * session
	 */

	Vector templates = (Vector) SessionManager.getSession(sessionId,
		prefix + TEMPLATE, Vector.class);

	synchronized (templates) {
	    if (templates.size() != types.length) {
		templates.setSize(0);
		for (int i = 0; i < types.length; i++) {
		    try {
			templates.addElement(types[i].newInstance());
		    } catch (Exception e) {
			throw new ClassCastException("Missing constructor " +
				types[i].getName() + "()");
		    }
		}
	    }
	    RewriteContext hr = new RewriteContext(server, prefix, request,
		    content, sessionId, this, templates);

	    /*
	     * Call the init() method of all the Templates.
	     */
	    for (int i = 0; i < types.length; i++) {
		Template obj = (Template) templates.elementAt(i);
		hr.prefix = prefixes[i];  // see discussion in RewriteContext
		if (obj.init(hr) == false) {
		    error = types[i] + " " + request.url +
		            ": init Rejecting request";
		    return null;
		}
	    }

	    /*
	     * Process the document.
	     */
	    while (hr.nextToken()) {
		process(hr);
	    }

	    /*
	     * Call the done() method of all the Templates.
	     */
	    for (int i = 0; i < templates.size(); i++) {
		Template obj = (Template) templates.elementAt(i);
		hr.prefix = prefixes[i];  // see discussion in RewriteContext
		if (obj.done(hr) == false) {
		    error = types[i] + " " + request.url +
		       	    ": done rejecting request";
		    return null;
		}
	    }
	    return hr.toString();
	}
    }

    /**
     * Processes the next token in the HTML document represented by the
     * given <code>RewriteContext</code>.  Processing a token involves either
     * dispatching to a tag-handling method in one of the
     * <code>Template</code> objects, or just advancing to the next token
     * if no <code>Template</code> was interested.
     *
     * @param	hr
     *		The RewriteContext holding the state of the HTML document.
     */
    public void
    process(RewriteContext hr)
    {
	switch (hr.getType()) {
	    case LexHTML.COMMENT:
	    case LexHTML.STRING:
	    case LexHTML.TAG: {
		String tag;
		if (hr.getType() == LexHTML.COMMENT) {
		    tag = "comment";
		    // System.out.println("(comment)");
		} else if (hr.getType() == LexHTML.STRING) {
		    tag = "string";
		} else {
		    tag = hr.getTag();
		    tagsSeen++;
		}
		Dispatch d = (Dispatch) dispatch.get(tag);
		if (hr.getType() == LexHTML.TAG && d == null) {
		    d = (Dispatch) dispatch.get("defaultTag");
		}
		if (d != null) {
		    Template obj = (Template) hr.templates.elementAt(d.index);
		    // see discussion in RewriteContext regarding prefix
		    hr.prefix = d.prefix;
		    try {
			d.method.invoke(obj, hr.args);
			tagsProcessed++;
		    } catch (InvocationTargetException e) {
			hr.append("<!-- " + tag + ":  " +
				e.getTargetException() + " -->");
			e.getTargetException().printStackTrace();
		    } catch (Exception e) {
			hr.append("<!-- " + tag + ":  " + e + " -->");
			e.printStackTrace();
		    }
		}
	    }
	}
    }

    /**
     * Return the last error message generated, or null of no
     * errors have occurred since the last call to "process".
     * XXX not thread safe between calls to process() and getError().
     */

    public String
    getError() {
	return error;
    }

    /**
     * Return the # of HTML tags seen in the previous call to "process".
     */

    public int
    tagsSeen() {
	return tagsSeen;
    }
    /**
     * Return the # of tags replaced in the previous call to "process".
     */

    public int
    tagsProcessed() {
	return tagsProcessed;
    }

    /**
     * Return the object instance of the template that will process this
     * tag (if any).  This allows templates to cooperate with each other.
     * If you need to use this method, then one of us did something wrong.
     */

    protected Template templateFromTag(RewriteContext hr, String tag) {
	Template template = null;
	Dispatch d = (Dispatch) dispatch.get(tag);
	if (d != null) {
	    template = (Template) hr.templates.elementAt(d.index);
	}
	return template;
    }
}
