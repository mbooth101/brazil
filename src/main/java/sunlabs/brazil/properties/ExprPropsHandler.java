/*
 * ExprPropsHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2006 Sun Microsystems, Inc.
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
 * The Initial Developer of the Original Code is: drach.
 * Portions created by drach are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): drach, suhler.
 *
 * Version:  2.5
 * Created by drach on 01/07/11
 * Last modified by suhler on 06/11/13 15:02:26
 *
 * Version Histories:
 *
 * 2.5 06/11/13-15:02:26 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.4 04/12/30-12:37:45 (suhler)
 *   doc fixes
 *
 * 2.3 04/05/24-14:42:18 (suhler)
 *   added discussion on how to use the calculator with <if> ... </if>
 *
 * 2.2 03/07/15-09:11:07 (suhler)
 *   Doc fixes for pdf version of the manual
 *
 * 2.1 02/10/01-16:36:39 (suhler)
 *   version change
 *
 * 1.14 02/05/02-13:09:16 (drach)
 *   Fix documentation.
 *
 * 1.12.1.1 02/05/01-11:22:48 (suhler)
 *   fix sccs version info
 *
 * 1.13 02/05/01-11:05:50 (drach)
 *   Add SCCS keywords back in
 *
 * 1.12 02/05/01-10:29:43 (suhler)
 *   steve's version
 *
 * 1.10.1.1 02/05/01-08:10:32 (drach)
 *   Revert back to properties package and passing in request.
 *
 * 1.11 02/04/27-12:35:40 (drach)
 *   Move to handler package and pass in request.props rather than request.
 *
 * 1.10 01/08/22-16:07:43 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.9 01/08/22-14:41:52 (drach)
 *   Add comments to PropertiesList and change some methods.
 *
 * 1.8.1.1 01/08/13-09:50:18 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.7.1.1 01/08/13-09:45:27 (suhler)
 *   added docs
 *
 * 1.8 01/08/13-09:10:09 (drach)
 *   Change server.props back to Properties object.
 *
 * 1.7 01/08/03-15:31:11 (drach)
 *   Add PropertiesList
 *
 * 1.6 01/07/20-11:33:14 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.5 01/07/17-18:30:32 (suhler)
 *   doc fixes
 *
 * 1.4 01/07/17-14:17:41 (suhler)
 *   use MatchUrl
 *
 * 1.3 01/07/16-17:00:50 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.1.1.1 01/07/16-16:50:43 (suhler)
 *   monir doc fixes
 *
 * 1.2 01/07/16-16:15:56 (drach)
 *   Add comments to both.  Add get method to ExprProps.
 *
 * 1.2 01/07/11-16:01:59 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 handlers/ExprPropsHandler.java
 *   Name history : 1 0 properties/ExprPropsHandler.java
 *
 * 1.1 01/07/11-16:01:58 (drach)
 *   date and time created 01/07/11 16:01:58 by drach
 *
 */

package sunlabs.brazil.properties;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.MatchString;

import java.io.IOException;

/**
 *
 * The <code>ExprPropsHandler</code> installs an expression evaluator as a
 * "smart properties" into the current request object, enabling arithmetic and
 * logical expression evaluation in property name lookups.
 * <p>
 * The following configuration parameters are used:
 * <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Only URL's that match are allowed.
 * (See {@link sunlabs.brazil.handler.MatchString}).
 * </dl>
 * <p>
 * Using the expression evaluator can be a bit tricky, as the evaluator
 * works by interpreting a property name as an expression, and using its
 * the expression result as its value.  For example, the construct:
 * <pre>
 * "${x + 4 == 3}"
 * </pre>
 * will evaluate to either "1" or "0", depending upon the value of "x".
 * For use with the <code>&lt;if&gt;</code> constuct of the
 * {@link sunlabs.brazil.template.BSLTemplate}, the following construct:
 * <pre>
 * &lt;if name="${x + 4 == 3}"&gt; ... [if expression] ... &lt;/if&gt;
 * </pre>
 * Will take (or not take) the "if expression" if there is a property
 * named "1" that is set (to anything but 0 or false), but a property
 * named "0" is not set.  An entry in a server configuration file:
 * <pre>
 * 1=true
 * </pre>
 * will do the trick.
 * <p>
 * alternately, the construct:
 * <pre>
 * &lt;if name=true value="${x + 4 == 3}"&gt; ... [if expression] ... &lt;/if&gt;
 * </pre>
 * Will work as expected only if there is a configuration property:
 * <pre>
 * true=1
 * </pre>
 * The choice of the name "true" is arbitrary, it could be any
 * valiable whose value is "1".
 * 
 * @author	Steve Drach &lt;drach@sun.com&gt;
 * @version     2.5, 06/11/13
 *
 * @see ExprProps
 * @see sunlabs.brazil.server.Request
 */
public class ExprPropsHandler implements Handler {

    private MatchString isMine;            // check for matching url

    public boolean
    init(Server server, String prefix) {
	isMine = new MatchString(prefix, server.props);
	return true;
    }

    /**
     *
     * Creates an instance of <code>ExprProps</code> that uses
     * <code>request.props</code> for the wrapped
     * <code>Calculator</code>'s symbol table.
     *
     * @see sunlabs.brazil.util.Calculator
     *
     * @return false
     */
    public boolean
    respond(Request request) throws IOException {
	if (isMine.match(request.url)) {
	    PropertiesList pl = new PropertiesList(new ExprProps(request), true);
	    pl.addBefore(request.serverProps);
	}
	return false;
    }
}
