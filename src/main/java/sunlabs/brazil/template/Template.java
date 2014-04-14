/*
 * Template.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2003 Sun Microsystems, Inc.
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
 * Version:  2.2
 * Created by cstevens on 99/09/29
 * Last modified by suhler on 03/07/10 09:23:41
 *
 * Version Histories:
 *
 * 2.2 03/07/10-09:23:41 (suhler)
 *   Use common "isTrue/isFalse" code in utin/format.
 *   "debug" may be set on TemplateHander or Template filter prefix as a default
 *   to the "debug" setting on each template
 *
 * 2.1 02/10/01-16:36:46 (suhler)
 *   version change
 *
 * 1.10 02/05/01-11:27:58 (suhler)
 *   fix sccs version info
 *
 * 1.9 01/09/12-16:50:47 (suhler)
 *   remove debug
 *
 * 1.8 01/09/10-14:02:27 (suhler)
 *   remove html comments from debug messages - comments don't nest in html
 *
 * 1.7 01/08/07-11:45:30 (suhler)
 *   sdded docs
 *
 * 1.6 01/07/16-16:47:41 (suhler)
 *   make debug public
 *
 * 1.5 01/05/15-16:41:29 (suhler)
 *   change debug to protected
 *
 * 1.4 01/05/11-14:51:18 (suhler)
 *   added a debug() convenience method for emitting diagnostics
 *   in the output as html comments
 *
 * 1.3 00/05/31-13:49:45 (suhler)
 *   name change, docs
 *
 * 1.2 00/02/11-08:55:54 (suhler)
 *   Treat comments like ordinary tags - remove from interface
 *
 * 1.2 99/09/29-16:14:14 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/Template.java
 *
 * 1.1 99/09/29-16:14:13 (cstevens)
 *   date and time created 99/09/29 16:14:13 by cstevens
 *
 */

package sunlabs.brazil.template;

/**
 * Parent for all classes that are templates.  This class is used
 * primarily as a sanity check, to make sure only classes that intend to
 * be used as templates actually are.  Subclasses are expected to implement
 * methods whose names corrospond to html/XML tags that are discovered
 * and called via introspection.
 * <p>
 * In the future, various "dummy" subclasses  may be used as alternate parents
 * to indicate which session behavior is expected.
 * <p>
 * In the current implementation, access to each instance
 * is restricted to one thread at a time.
 * In many situations this is a good compromise
 * between ease of coding templates and server performance.
 * As a new instance is created for
 * each session, multiple threads may be active at once, although only one
 * per session.
 * <p>
 * In the future, it is expected that an alternative mode of running
 * templates will be available that
 * will remove the current synchonization lock around each template
 * instance, allowing multiple threads to access each instance at once.
 * <p>
 * To take advantage of this new scheme
 * templates should be written
 * <i>thread safe</i>.  They should avoid using instance variables
 * if possible.  If state needs to be passed among methods, it may
 * be stored in the <code>RewriteContext</code>, as each thread has its own.
 * <p>
 * Configuration properties:
 * <dl class=props>
 * <dt> <code>debug</code>
 * <dd> If this configuration parameter is true, sub-classes that
 *	call the <i>debug</i> method will have diagnostic information
 *	added to the html output as comments.  If no <code>debug</code>
 *      property is associated with the template prefix, the value of
 *      <code>debug</code> with the prefix for the class registering this
 *      template is used instead.
 * </dl>
 *
 * @author Stephen Uhler
 * @Version %W
 */
import sunlabs.brazil.util.regexp.Regexp;
import sunlabs.brazil.util.Format;

public class Template implements TemplateInterface {
    private static final String DEBUG = "debug";
    public transient boolean debug = false;
    static Regexp re;

    static {
       re = new Regexp("--");
    }

    /**
     * Called before this template processes any tags.
     */

    public boolean
    init(RewriteContext hr) {
	debug = Format.isTrue(hr.request.props.getProperty(hr.prefix + DEBUG,
		hr.request.props.getProperty(hr.templatePrefix + DEBUG)));
	return true;
    }

    /**
     * Called after all tags have been processed, one final chance.
     */

    public boolean
    done(RewriteContext hr) {
	return true;
    }

    /**
     * Add a diagnostic comment to html output, if "debug" is set
     * in the request properties.  The comment may be modified
     * to prevent nested comments.
     * @param msg	The message to include as a comment
     */

    protected void
    debug(RewriteContext hr, String msg) {
	if (debug && msg != null) {
	    // System.out.println("Debugging (" + msg + ")");
	    hr.append("<!-- " + re.subAll(msg, "==") + " -->");
	}
    }

    /**
     * Add current tag as a comment to the html
     */

    protected void
    debug(RewriteContext hr) {
        debug(hr, hr.getBody());
    }
}
