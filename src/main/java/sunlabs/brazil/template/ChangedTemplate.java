/*
 * ChangedTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.
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
 * Version:  2.1
 * Created by cstevens on 99/10/28
 * Last modified by suhler on 02/10/01 16:36:40
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:36:40 (suhler)
 *   version change
 *
 * 1.10 02/05/01-11:27:42 (suhler)
 *   fix sccs version info
 *
 * 1.9 00/12/11-13:30:35 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.8 00/10/27-08:15:48 (suhler)
 *   akkow serialization
 *
 * 1.7 00/07/07-17:01:58 (suhler)
 *   remove System.out.println(s)
 *
 * 1.6 00/05/31-13:50:31 (suhler)
 *   name change
 *
 * 1.5 00/04/12-15:55:58 (cstevens)
 *   imports
 *
 * 1.4 99/11/16-19:08:19 (cstevens)
 *   wildcard imports.
 *
 * 1.3 99/11/01-12:38:32 (suhler)
 *   loog for root based on prefix first
 *
 * 1.2 99/11/01-10:56:53 (cstevens)
 *   Only keep track of the last-accessed time for files that were named in
 *   the HREF of some previously seen <changed> section, instead of all files
 *   that were accessed.
 *
 * 1.2 99/10/28-17:23:48 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/ChangedTemplate.java
 *
 * 1.1 99/10/28-17:23:47 (cstevens)
 *   date and time created 99/10/28 17:23:47 by cstevens
 *
 */

package sunlabs.brazil.template;

import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.session.SessionManager;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

/**
 * This <code>Template</code> adds an icon to HREFs to indicate when the
 * file being referred to is new, changed, or unchanged with respect
 * to the user's session.  
 * <p>
 * In order for the <code>ChangedTemplate</code> to work, the following
 * must happen. <ul>
 * <li> All files whose HREFs should be rewritten must pass through the
 *	<code>ChangedTemplate</code>.  All HREFs seen between
 *	<code>&lt;changed&gt;</code> and <code>&lt;/changed&gt;</code> tags
 *	will be rewritten so that an appropriate icon appears next to the
 *	HREF.  
 *
 * <li> All files whose last-accessed time is being tracked must also pass
 *	through this <code>ChangedTemplate</code>.  Whenever the
 *	<code>ChangedTemplate</code> sees a file that was named in some
 *	previously seen <code>&lt;changed&gt;</code> section, that file's
 *	last-accessed time will be updated.  Only the files named in a
 *	<code>&lt;changed&gt;</code> section are tracked.
 * </ul>
 * Warning:
 * The <code>ChangedTemplate</code> may have to keep track of a lot of data
 * per session, specifically, the names of all the files being tracked and
 * the last time the user accessed them.
 * <p>
 * The <code>ChangedTemplate</code> examines the property "fileName", set
 * (for example) by the <code>FileHandler</code>, in order to update the
 * last-accessed time of a file as it passes by.  If the "fileName" property
 * is not set, the last-accessed time will not be updated.
 * <p>
 * The <code>ChangedTemplate</code> also assumes that all local HREFs it sees
 * can be directly translated into the corresponding file name based on the
 * "root" property and the URL of the current file.  Getting that file name
 * is necessary so its last-modified time (on disk) can be compared to its
 * last-accessed time (per session).
 * <p>
 * The <code>ChangedTemplate</code> uses the following properties: <dl class=props>
 * <dt> fileName
 * <dd> A request property containing the full path name of the current file,
 *	used to keep track of the last time that file was accessed by the
 *	current user.  A <code>Handler</code> or other code may set this
 *	property if it wishes the file to be tracked.
 *
 * <dt> root
 * <dd> The root of the document hierarchy.  An HREF must resolve to a file
 *	in this hierarchy so its last-modified time can be checked.  If the
 *	file does not exist, the HREF will not be rewritten.
 *
 * <dt> always
 * <dd> If this property is present, the <code>ChangedTemplate</code> always
 *	rewrites the HREFs, instead of just when they appear within the
 *	<code>&lt;changed&gt;</code> and <code>&lt;/changed&gt;</code> tags.
 *
 * <dt> new
 * <dd> The HTML to substitute into the document if the HREF refers to a
 *	file that has never been accessed by the user.  If absent, the HREF
 *	for new files will not be rewritten.
 *
 * <dt> changed
 * <dd> The HTML to substitute into the document if the HREF refers to a
 *	file that has changed since the last time it was accessed by the
 *	user.  If absent, the HREF for changed files will not be rewritten.
 * 
 * <dt> unchanged
 * <dd> The HTML to substitute into the document if the HREF refers to a
 *	file that has not changed since the last time it was accessed by
 *	the user.  If absent, the HREF for unchanged files will not be
 *	rewritten.
 * </dl>
 *
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version	@(#)ChangedTemplate.java	2.1
 */
public class ChangedTemplate
    extends Template implements Serializable
{
    private static final String NEW = "new";
    private static final String CHANGED = "changed";
    private static final String UNCHANGED = "unchanged";
    private static final String ALWAYS = "always";

    boolean check;
    Hashtable changed;
    URL url;

    String newToken;
    String changedToken;
    String unchangedToken;
    String always;

    private void
    setup(RewriteContext hr)
    {
	if (changed == null) {
	    changed = (Hashtable) SessionManager.getSession(hr.sessionId,
		    this.getClass(), Hashtable.class);

	    String prefix = hr.prefix;
	    Properties props = hr.request.props;
	    newToken = props.getProperty(prefix + NEW);
	    changedToken = props.getProperty(prefix + CHANGED);
	    unchangedToken = props.getProperty(prefix + UNCHANGED);
	    always = props.getProperty(prefix + ALWAYS);
	}
    }

    /**
     * Records that this file has just been accessed.
     */
    public boolean
    init(RewriteContext hr)
    {
	Properties props = hr.request.props;

	String path = props.getProperty("fileName");
	if (path != null) {
	    setup(hr);
	    if (changed.get(path) != null) {
		changed.put(path, new Long(System.currentTimeMillis()));
	    }
	}
	check = (always != null);
	try {
	    url = new URL("http://localhost" + hr.request.url);
	} catch (Exception e) {
	    return false;
	}
	return true;
    }

    public boolean
    done(RewriteContext hr)
    {
	changed = null;
	url = null;
	return true;
    }

    public void
    tag_changed(RewriteContext hr)
    {
	hr.killToken();
	check = true;
    }

    public void
    tag_slash_changed(RewriteContext hr)
    {
	hr.killToken();
	check = false;
    }

    public void
    tag_a(RewriteContext hr)
    {
	if (check == false) {
	    return;
	}
	String href = hr.get("href");
	try {
	    /*
	     * Turn href into the referenced file name.  Assume that the href
	     * refers to a static file with the same "root" property as was
	     * used to generate this file.
	     */

	    String root = hr.request.props.getProperty(hr.prefix +
		    FileHandler.ROOT,
		    hr.request.props.getProperty(FileHandler.ROOT, "."));
	    File file = new File(root, new URL(url, href).getFile());

	    if (file.exists() == false) {
		return;
	    }

	    /*
	     * Get the token to display for when the file is new, changed, or
	     * unchanged with respect to the session.
	     */

	    setup(hr);
	    String path = file.getPath();
	    Long seen = (Long) changed.get(path);
	    if (seen == null) {
		seen = new Long(0);
		changed.put(path, seen);
	    }

	    String token;
	    if (seen.longValue() == 0) {
		token = newToken;
	    } else if (seen.longValue() < file.lastModified()) {
		token = changedToken;
	    } else {
		token = unchangedToken;
	    }
	    hr.append(token);
	    hr.appendToken();
	} catch (Exception e) {
	    /*
	     * Ignore all File errors and the href not being set.
	     */
	}
    }
}
