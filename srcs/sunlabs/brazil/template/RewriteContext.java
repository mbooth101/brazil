/*
 * RewriteContext.java
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
 * The Initial Developer of the Original Code is: cstevens.
 * Portions created by cstevens are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): cstevens, drach, guym, suhler.
 *
 * Version:  2.9
 * Created by cstevens on 99/09/29
 * Last modified by suhler on 06/12/18 14:37:27
 *
 * Version Histories:
 *
 * 2.9 06/12/18-14:37:27 (suhler)
 *   add snarfTillClose() to allow grabbing all content inside a tag-pair.
 *   This supports nesting
 *
 * 2.8 06/11/13-11:59:55 (suhler)
 *   add substAttributeValues() convenience method
 *
 * 2.7 06/08/01-14:06:04 (suhler)
 *   added templateFromTag()
 *
 * 2.6 05/06/16-08:05:26 (suhler)
 *   Moved getNamespaceProperties here!
 *
 * 2.5 03/08/01-16:19:12 (suhler)
 *   fixes for javadoc
 *
 * 2.4 03/07/10-09:23:51 (suhler)
 *   Use common "isTrue/isFalse" code in utin/format.
 *
 * 2.3 03/07/09-12:28:03 (suhler)
 *   added "templatePrefix" variable so templates can refer to template
 *   wide prefixes if needed
 *
 * 2.2 03/07/07-14:04:05 (suhler)
 *   add convenience methods for checking for closing tags.  This deals with
 *   tagPrefix's properly
 *
 * 2.1 02/10/01-16:36:49 (suhler)
 *   version change
 *
 * 1.14 01/10/03-13:54:27 (suhler)
 *   addrd "tagsSeen" for diagnostics
 *
 * 1.13 01/09/13-09:23:59 (suhler)
 *   remove uneeded import
 *
 * 1.12 01/08/10-14:56:25 (guym)
 *   Per code review, made Abort, Break, Continue flags an enum, plus fixed a couple of bugs
 *
 * 1.11 01/08/03-20:28:09 (guym)
 *   Added support for <abort>, <break>, <continue> processing
 *
 * 1.10 01/07/16-16:48:14 (suhler)
 *   Added convenience methods
 *
 * 1.9 01/05/02-15:32:24 (drach)
 *   Add template tokens
 *
 * 1.8 00/07/06-15:48:56 (suhler)
 *   doc update
 *
 * 1.7 00/07/05-14:10:21 (cstevens)
 *   Server object is in RewriteContext used by templates, so templates (such as
 *   the TclServerTemplate) can be initialized with the server and prefix, similar
 *   to how Handlers are initialized.
 *
 * 1.6 00/06/29-10:46:06 (suhler)
 *   Added serverProps field (is this a good idea?).
 *
 * 1.5 00/05/31-13:48:03 (suhler)
 *   name change
 *
 * 1.4 00/05/24-11:26:37 (suhler)
 *   add docs
 *
 * 1.3 99/10/28-17:22:50 (cstevens)
 *   ChangedTemplate
 *
 * 1.2 99/10/21-18:13:59 (cstevens)
 *   TemplateHandler now takes a list of Templates, rather than just one.  When
 *   parsing an HTML file, it will now dispatch to the union of all the tag
 *   methods defined in the list of Templates.  In that way, the user can
 *   compose things like the BSLTemplate to iterate over request properties with
 *   the PropsTemplate to substitute in their values.  Otherwise, it would have
 *   required N separate passes (via N separate TemplateHandlers) over the HTML
 *   file, one for each Template and/or level of recursion in the BSLTemplate.
 *
 * 1.2 99/09/29-16:13:31 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 handlers/templates/RewriteContext.java
 *   Name history : 1 0 handlers/templates/HtmlRewriter.java
 *
 * 1.1 99/09/29-16:13:30 (cstevens)
 *   date and time created 99/09/29 16:13:30 by cstevens
 *
 */

package sunlabs.brazil.template;

import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import sunlabs.brazil.handler.HtmlRewriter;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.util.Format;

/**
 * A variant containing instance variables that may be referenced by
 * rewriting filters.  Every implementation of the template class
 * may define methods of the form:
 * <code>tag_<i>xxx</i></code>
 * or
 * <code>tag_slash_<i>xxx</i></code>
 * which will get called when the corrosponding HTML entity
 * <code>&lt;xxx ...&gt;</code>
 * or
 * <code>&lt;/xxx ...&gt;</code>
 * is found in the content being filtered.
 * <p>
 * An instance of this class is passed to each <code>tag</code>
 * method, permitting introspection of the current filtering context.
 *
 * @version %V% RewriteContext.java
 */

public class RewriteContext extends HtmlRewriter {
    /**
     * The server object, as passed to a handler's init method.
     */
    public Server server;		// The server, containing properties.
    /**
     * The prefix to use for locating keys in the server or request properties
     * objects.  This is a dynamic value that changes during template
     * processing.  Before each template is processed, it is set to the
     * prefix associated with the template class that the current tag belongs
     * to.
     */
    public String prefix;
    /**
     * This is the prefix defined by the invoker of a set of templates.  Its
     * value corrosponds to the class that invokes a set of templates.
     */
    public String templatePrefix;
    /**
     * The Request object, as passed to a handler's respond method.
     */
    public Request request;		// The current request
    /**
     * A unique <b>session id</b>, if available.
     */
    public String sessionId;		// The session ID (if any)
    /**
     * Application specific placeholder.
     */
    Object[] args;			// app. specific extra stuff

    TemplateRunner runner;
    Vector templates;

    /* Private flags enumeration for RewriteContext state   */
    /* Note that the BSLTemplate has defined the 0,1,2 bits */
    /* for these flags  - Hex values 0x1, 0x2, 0x4          */

    private int stateFlags; 

    /* Private state variable to track the depth of our recursive descent */

    private int nestingLevel; 

    /* Our constructor */

    public
    RewriteContext(Server server, String prefix, Request request,
	    String content, String sessionId, TemplateRunner runner,
	    Vector templates) {
	super(content);

	this.server = server;
	this.prefix = prefix;
	this.templatePrefix = prefix;
	this.request = request;
	this.sessionId = sessionId;
	this.runner = runner;
	this.templates = templates;
	this.args = new Object[] {this};

        stateFlags = 0;
        nestingLevel = 0;
    }

    /**
     * Cause this RewriteContext to abort its processing.
     */

    public void abort() {
        this.lex.replace("");
        setRewriteState(BSLTemplate.ABORT);
    }

    /**
     * Set the given state of this RewriteContext.
     */
     
    public boolean
    setRewriteState(int state) {
        boolean nesting = (nestingLevel >= 1);

        if (nesting) {
          stateFlags |= state;  
        } 

        return nesting;
    }

    /**
     * Unset the given state of this RewriteContext.
     */
     
    public void 
    unsetRewriteState(int state) {
        stateFlags &= ~state;
    }

    /**
     * Check whether this RewriteContext is in the specified state.
     */

    public boolean
    checkRewriteState(int state) {
        return (stateFlags & state) != 0;
    }

    /**
     * Increment the nesting level counter.
     */

    public void 
    incrNestingLevel() {
        nestingLevel++;
    }

    /**
     * Decrement the nesting level counter.
     */

    public void 
    decrNestingLevel() {
        if (nestingLevel != 0) {
          nestingLevel--;
        } else {
          System.err.println("Someone tried to decrement the nesting level past zero!");
        }
    }

    /**
     * Return the current nesting level counter.
     */

    public int
    getNestingLevel() {
        return nestingLevel;
    }

    /**
     * Invoke a template on this token, if any template is interested in this
     * token.  The template may consume more than just this token, if it
     * wants.
     */

    public void
    process() {
	runner.process(this);
    }

    /* Convenience methods */

    /**
     * Return the number of HTML tags seen to this point.
     *
     */

    public int
    tagsSeen() {
	return runner.tagsSeen();
    }

    /**
     * overwrite "get" to automatically do ${...} substitutions
     * The default is "true".  Setting the default to "false" and
     * recompiling is more backward compatible (but less useful).
     */

    public String
    get(String name) {
	return get(name, true);
    }

    /**
     * Get an attribute value, and optionally perform ${...} substitutions.
     */

    public String
    get(String name, boolean subst) {
	if (subst) {
	    return Format.subst(request.props, super.get(name));
	} else {
	    return(super.get(name));
	}
    }

    /**
     * Get a tag attribute, with a default value.
     * The name is looked-up as an attribute in the current tag.  If
     * it is not found, the configuration property by the same name is used.
     * If that is not found, <code>dflt</code> is used instead.
     *
     * @param name	The name of the attribute to look up
     * @param dflt	The default attribute value (may be null)
     * @return		The value of "name"
     */

    public String
    get(String name, String dflt) {
	String result = get(name);
	if (result == null) {
	    result = request.props.getProperty(prefix + name, dflt);
	}
	return result;
    }

    /**
     * Determine the value of a boolean attribute in the current tag.
     * ${...} substitution is performed on the value.
     * @param name	The name of the  boolean attribute
     * @return          false if the value is:
     *                    null, "", "0", "no", "off", or "false"
     *                  true otherwise.
     *                  "attribute=" is false, but "attribute" with no
     *                  value is true;
     */

    public boolean
    isTrue(String name) {
        String value = get(name);
        if (value==null) {
            return false;
        }
        if (value.length() == 0) {
            return (getArgs().toLowerCase().indexOf(name + "=") == -1);
        }
        value = value.trim().toLowerCase();
	if (Format.isFalse(value)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * See if the current token is the closing tag for the given string.
     * Account for tag prefix, if any.
     * @param tag	tag whose match ( e.g. /[prefix]tag is to be found
     * @return		true if the current tag closes "tag"
     */

    public boolean isClosingFor(String tag) {
	return isClosingFor(tag, true);
    }
    /**
     * See if the current token is the closing tag for the given string.
     * Account for tag prefix, if any.
     * @param tag	tag whose match ( e.g. /[prefix]tag is to be found
     * @param close	if set, "tag" matches  "/[prefix]tag", otherwise
     *			tag matches "[prefix]tag".
     * @return		true if the current tag closes "tag"
     */

    public boolean isClosingFor(String tag, boolean close) {
	String currentTag = getTag();
	if (currentTag == null || (close && !currentTag.startsWith("/")) ||
		!currentTag.endsWith(tag)) {
	    return false;
	}
	return currentTag.equals((close ? "/" : "") + getTagPrefix() + tag);
    }

    /**
     * Grab all the markup between the current tag, and the corrosponding
     * closing tag.  Nesting is supported. If there is no markup, or
     * the current tag is a singleton, the empty string is returned.
     * The starting and ending tags are not included in the markup.
     */

    public String snarfTillClose() {
	if (isSingleton()) {
	    return "";
	}
	String match = getTag();
	int nestingLevel = 0;
	StringBuffer sb = new StringBuffer("");
	while(nextToken()) {
	    String tag = getTag();
	    if (tag == null) {
		sb.append(getToken());
		continue;
	    }
	    if (tag.equals(match) && !isSingleton()) {
		nestingLevel++;
	    } else if (tag.equals("/" + match) && (--nestingLevel < 0)) {
		nextToken();
		break;
	    }
	    sb.append(getToken());
	}
	return sb.toString();
    }

    /**
     * Add a closing tag to the list of tags that mark un-interpreted text
     * Deal with the tag prefix, if any
     */

    public void addClosingTag(String tag) {
	String add = getTagPrefix() + tag;
	if (lex.getClosingTags().indexOf(add) == -1) {
	    lex.getClosingTags().addElement(add);
	}
    }

    /**
     * get the tag prefix associated with the current tag.  
     */

    String getTagPrefix() {
	return server.props.getProperty(prefix + TemplateRunner.TAG_PREFIX,
		    server.props.getProperty(templatePrefix +
		    TemplateRunner.TAG_PREFIX, ""));
    }

    /**
     * Get the proper properties table based on the "namespace"
     * attribute. The namespaces "local" (the default) and "server"
     * are special. The "sessionTable" cinfiguration
     * property can be used to override the default session-table, 
     * which defaults to the template handler/filter prefix.
     */

    public Properties
    getNamespaceProperties() {
	Properties props;
	String namespace = get("namespace");
	if (namespace==null || namespace.equals("local")) {
	    props = request.props;
	} else if ("server".equals(namespace)) {
	    props = server.props;
	} else {
	    String sessionTable = get("sessionTable", templatePrefix);
	    props = (Properties) SessionManager.getSession(namespace,
		sessionTable, Properties.class);
	}
	return props;
    }

    /**
     * Get the template that will process the supplied tag (if any).
     * This allows template classes to get a handle on the template instances
     * of other classes running in the same template processing step.  If
     * you need this (and sometimes you do), then there is an architectual
     * flaw, either in the template system or your classes or both.
     *
     * @param tag	the name of the html tag that will get processed
     * @return		The template instance, if one exists.
     */

    public Template
    templateFromTag(String tag) {
	return runner.templateFromTag(this, tag);
    }

    /**
     * Substitute all attribute values.
     */

    public void
    substAttributeValues() {
        Enumeration e = keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            put(key, get(key)); // do substitutions
        }
    }
}
