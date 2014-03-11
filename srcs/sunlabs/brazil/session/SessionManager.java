/*
 * SessionManager.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1998-2004 Sun Microsystems, Inc.
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
 * Contributor(s): cstevens, rinaldo, suhler.
 *
 * Version:  2.2
 * Created by suhler on 98/09/14
 * Last modified by suhler on 04/04/05 14:45:12
 *
 * Version Histories:
 *
 * 2.2 04/04/05-14:45:12 (suhler)
 *   change implementation to use 1 hash table with a contatenated key
 *
 * 2.1 02/10/01-16:39:08 (suhler)
 *   version change
 *
 * 1.16 02/07/24-10:48:01 (suhler)
 *   doc updates
 *
 * 1.15 02/05/22-09:44:45 (suhler)
 *   improve error message
 *
 * 1.14 01/08/07-11:36:27 (suhler)
 *   added docs
 *
 * 1.13 01/05/01-11:02:53 (suhler)
 *   rewrite
 *
 * 1.12 00/12/08-16:47:54 (suhler)
 *   doc fixes
 *
 * 1.11 00/04/17-14:34:03 (cstevens)
 *   doc
 *
 * 1.10 00/04/12-16:04:32 (cstevens)
 *   SessionManager rewritten to allow runtime selection of different
 *   SessionManagers.  Now session manager can be chosen in the "init= " line of
 *   the config file.  Previously, had to compile various files that all
 *   impersonated "sunlabs.brazil.handler.SessionManager" and set classpath
 *   to point to the desired one.  Now there are three implementations of
 *   the SessionManager:
 *   1. Default session manager, no persistence.
 *   2. Using PJama
 *   3. Using Serializable and ObjectOutputStream.
 *
 * 1.9 99/11/30-09:46:05 (suhler)
 *   changed default message: notify only if persistence is on
 *
 * 1.8 99/08/06-12:29:44 (suhler)
 *   moved pjama stuff into util package
 *
 * 1.7 99/03/30-09:31:09 (suhler)
 *   documentation update
 *
 * 1.6 99/02/10-11:52:57 (suhler)
 *   pJama wrapper moved to server package
 *
 * 1.5 99/01/05-10:58:03 (suhler)
 *   moved pJama wrappers into separate class
 *
 * 1.4 98/12/09-15:03:22 (suhler)
 *   Added pJama stuff
 *
 * 1.3 98/09/21-14:55:24 (suhler)
 *   changed the package names
 *
 * 1.2 98/09/17-17:59:15 (rinaldo)
 *
 * 1.2 98/09/14-18:03:10 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 3 2 session/SessionManager.java
 *   Name history : 2 1 handlers/SessionManager.java
 *   Name history : 1 0 SessionManager.java
 *
 * 1.1 98/09/14-18:03:09 (suhler)
 *   date and time created 98/09/14 18:03:09 by suhler
 *
 */

package sunlabs.brazil.session;

import java.util.Hashtable;

/**
 * The <code>SessionManager</code> associates an object with a Session ID
 * to give Handlers the ability to maintain state that lasts for the
 * duration of a session instead of just for the duration of a request.
 * <p>
 * The <code>SessionManager</code> operates as a bag of globally
 * accessible resources.  Existing subclasses of the
 * <code>SessionManager</code> also provide persistence, that is, a way to
 * recover these resources even if the server process is terminated and
 * later restarted, to get back to the state things were in.
 * <p>
 * A session manager is like a Dictionary only with fewer guarantees.
 * Enumeration is not possible, and a "get" might return null even if there was 
 * a previous "put", so users should be prepared to handle that case.
 * <p>
 * Unlike a typical dictionary, a session manager uses two keys to identify
 * to identify each resource.  The first key, by convention, represents
 * a client session id.  The second (or resource) key is chosen to identify the
 * resource within a session.
 * <p>
 * Care should be used when choosing resource keys, as they are global
 * to a JVM, which may have several unrelated Brazil servers running at
 * once.  Sharing session manager resources between servers can cause
 * unexpected behavior if done unintentionally.
 * <p>
 * Existing session manager implementations arrange for session resources 
 * that are <code>Java Properties</code> to be saved across restarts
 * of the JVM, and should be used when practical.
 *
 * @author	Stephen Uhler (stephen.uhler@sun.com)
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version	2.2, 04/04/05
 */

public class SessionManager {
    private static SessionManager sm = new SessionManager();

    /**
     * Installs the given <code>SessionManager</code> object as the
     * default session manager to be invoked when <code>getSession</code>
     * is called.
     *
     * @param	mgr
     *		The <code>SessionManager</code> object.
     */

    public static void
    setSessionManager(SessionManager mgr) {
	sm = mgr;
    }

    /**
     * Returns the object associated with the given Session ID.  Passing in
     * the same (hash-key equivalent) Session ID will return the same object.
     * This convenience method reflects common usage.
     *
     * @param	session
     *		The Session ID for the persistent session information.  If
     *		the session does not exist, a new one is created.
     *
     * @param	ident
     *		An arbitray identifier used to determine which object
     *		(associated with the given <code>session</code>) the caller
     *		wants.
     *
     * @param	type
     *		The Class of the object to create.  If the given
     *		<code>session</code> and <code>ident</code> did not specify
     *		an existing object, a new one is created by calling
     *		<code>newInstance</code> based on the <code>type</code>.
     *		If <code>null</code>, then this method returns
     *		<code>null</code> if the object didn't exist, instead of
     *		allocating a new object.
     *
     * @return	an object of type <code>type</code>, or null if
     *		the object doesn't exist and <code>type</code> is null.
     */

    public static Object
    getSession(Object session, Object ident, Class type) {
        Object obj = sm.getObj(session, ident);
        if (type != null && obj == null) {
	    try {
		obj = type.newInstance();
	    } catch (Exception e) {
		throw new IllegalArgumentException(type.getName() +
			": " + e.getClass().getName());
	    }
	    sm.putObj(session, ident, obj);
	}
	return obj;
    }

    /*
     * Allow access to std get/put/remove methods of the installed
     * Session Manager.
     */

    /**
     * get an object from the session manager.
     * This static method will dispatch to the currently installed
     * <code>SessionManager</code> instance.
     */

    public static Object
    get(Object session, Object ident) {
	return sm.getObj(session, ident);
    }

    /**
     * put an object into the session manager.
     * This static method will dispatch to the currently installed
     * <code>SessionManager</code> instance.
     */

    public static void
    put(Object session, Object ident, Object data) {
	sm.putObj(session, ident, data);
    }

    /**
     * Remove an object from the session manager.
     * This static method will dispatch to the currently installed
     * <code>SessionManager</code> instance.
     */

    public static void
    remove(Object session, Object ident) {
	sm.removeObj(session, ident);
    }

    /* Default Implementation */

    /**
     * NOTE: The previous implementation breaks for java > 1.1.  Use
     *     this one instead.
     * A <code>Hashtable</code> used when mapping Session IDs to objects.
     * <p>
     * the key for the hashtable is derived from the "session" and "ident"
     * objects.
     */

    protected Hashtable sessions = new Hashtable();

    /**
     * Returns the object associated with the given Session ID and ident.
     */

    protected Object
    getObj(Object session, Object ident) {
	String key = makeKey(session, ident);
	return sessions.get(key);
    }

    /**
     * Associates an object with a session id and ident.
     * "value" may not be null.
     */

    protected void
    putObj(Object session, Object ident, Object value) {
	String key = makeKey(session, ident);
	sessions.put(key, value);
    }

    /**
     * Removes the object associated with the given Session ID and ident.
     */

    protected void
    removeObj(Object session, Object ident) {
	String key = makeKey(session, ident);
	sessions.remove(key);
    }

    /**
     * Invent a single key from the 2 separate ones
     */

    protected String
    makeKey(Object session, Object ident) {
	if (ident == null) {
	    ident = "null";
        }
	if (session == null) {
	    session = this.getClass();
	}
	if (session instanceof String && ident instanceof String) {
	    return session + "-" + ident;
	} else {
	    return "" + session.hashCode() + "-" + ident.hashCode();
	}
    }
}
