/*
 * PJamaSessionManager.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2006 Sun Microsystems, Inc.
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
 * Created by cstevens on 00/04/11
 * Last modified by suhler on 06/01/17 09:45:29
 *
 * Version Histories:
 *
 * 2.2 06/01/17-09:45:29 (suhler)
 *   nit
 *
 * 2.1 02/10/01-16:39:08 (suhler)
 *   version change
 *
 * 1.6 01/07/16-16:51:43 (suhler)
 *   move PJwrapper inside to reflect deprecated status
 *
 * 1.5 01/05/01-11:01:44 (suhler)
 *   update for new SessionManager interface
 *
 * 1.4 00/05/31-13:51:55 (suhler)
 *   docs
 *
 * 1.3 00/04/17-14:35:17 (cstevens)
 *   Inherit getSessionObject from SessionManager.
 *
 * 1.2 00/04/12-16:04:39 (cstevens)
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
 * 1.2 00/04/11-15:30:45 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 session/PJamaSessionManager.java
 *
 * 1.1 00/04/11-15:30:44 (cstevens)
 *   date and time created 00/04/11 15:30:44 by cstevens
 *
 */

package sunlabs.brazil.session;

import java.util.Hashtable;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Use pJama to implement persistant sessions.
 *
 * @author		Stephen Uhler
 * @version		1.0, 08/20/98
 */

public class PJamaSessionManager
    extends SessionManager
{
    Hashtable sessions;

    public 
    PJamaSessionManager()
    {
	sessions = (Hashtable) PJwrapper.initStore("BrazilStore",
		Hashtable.class);
	if (PJwrapper.isPersistent()) {
	    System.out.println("Using PJama persistent store!");
	}
    }

    /**
     * Return the object associated with this session.  If the session doesn't
     * already exist, it is created by calling newinstance() with
     * <code>myClass.</code>
     *
     * @param session		The object identifying the session
     * @param ident		The session user's identity
     * @param myClass		The class to create a new instance of for a new session
     */
    protected void
    putObj(Object session, Object ident, Object value)
    {
	super.putObj(session, ident, value);

	// Stabilize the store every time - for now

	PJwrapper.stabilize();
	return;
    }
}

/**
 * Wrap a pJama invocation, to allow for persistent operation.
 * pJama is an implementation of orthoginal persistance, allowin session
 * state to be recovered after server shut-downs.
 * See <a href="http://www.sun.com/research/forest/">here</a>
 * for more information.
 * This utility class allows a server application to run with the pJama
 * persistent VM by dynamically locating  and binding the classes at run time.
 * If pJama is not available, a standard <b>transient</b> implementation is
 * used instead.
 *
 * @author		Stephen Uhler
 * @version            @(#) PJwrapper.java 1.4 99/08/06 12:31:42
 *
 * NOTE: This class used to be public, but since pJama is not widely
 * available (and not expected to be so) it was moded here 'cause this
 * is the only class that uses it.
 */

 /*
 * To use with the persistent store, identify a "root" object for persistence.
 * This is normally a static reference:
 *   Static Type root= null;		// <Type> is the objects class
 *
 * When you are ready to start the store:
 *   if (root == null) {
 *      root = initStore("some identifying string", Type.class)
 *   }
 *
 * Any time you want stabilization, call:
 *   stabilize()
 *
 * If the store didn't start, you still get a valid instance of <root>, 
 * but isPersistent() returns false, and getError() returns why.
 */

class PJwrapper {
    private static boolean persistent = false;	// is persistence on?
    private static String error = null;		// last error message
    private static Class pClass;

    private PJwrapper() {}

    /**
     * See if we are using persistence.
     * @return	true, is persistence is enabled.
     */

    public static boolean isPersistent() {
    	return persistent;
    }

    /**
     * Return the cause of most recent failure.
     */

    public static String getError() {
   	return error;
    }

    /**
     * Do a one-time store initialization,
     * @param  The string name to identify this store
     * @param  The type of the object to be the store root
     * @return The root object.  If one didn't exist, it is
     *		created with newInstance()
     */

    public static Object initStore(String name, Class type) {
	Object root = null;
	Object store = getStore();
	if (store == null) {
	    try {
		root =  (Object) (type.newInstance());
	    } catch (Exception e) {
	    	error = e.toString();
	    }
	    return root;
	}
	try {
	    Class[] rootParams = {java.lang.String.class};
	    Object[] rootArgs = { name };
	    Method rootMethod = pClass.getMethod("getPRoot",rootParams);
	    root = (Object) rootMethod.invoke(store, rootArgs);
	    if (root == null) {
		Class[] newParams =
			{java.lang.String.class, java.lang.Object.class};
		Object[] newArgs = { name, null };
		root = (Object) type.newInstance();
		newArgs[1] = root;
		Method newMethod = pClass.getMethod("newPRoot",newParams);
		newMethod.invoke(store, newArgs);
	    }
	    persistent = true;
	} catch (Exception e) {
	    error = e.toString();
	    try {
		root = (Object) (type.newInstance());
	    } catch (Exception e2) {
	    	error += " " + e2.toString();
	    }
	}
	return root;
    }

    /**
     * Stabilize the store
     * @return		true if successful.  Use getError() to extract
     *			     reason for failure
     */

    public static boolean stabilize() {
        boolean result = false;
        if (!persistent)  {
	    return false;
	}
	try {
	    Object store = getStore();
	    Class[] stabilizeParams = new Class[0];
	    Object[] stabilizeArgs = new Object[0];
	    Method stableMethod = pClass.getMethod("stabilizeAll",stabilizeParams);
	    stableMethod.invoke(store,stabilizeArgs);
	    result = true;
	} catch (Exception e) {
	    error = e.toString();
	}
	return result;
    }

    /**
     * get an initial store reference
     * @return		null, if no store available
     */

    private static Object getStore() {
    	Object store = null;
	try {
	    pClass = Class.forName("org.opj.store.PJStoreImpl");
	    Class[] storeParams = new Class[0];
	    Object[] storeArgs = new Object[0];
	    Method storeMethod = pClass.getMethod("getStore",storeParams);
	    store = storeMethod.invoke(null,storeArgs);
	    persistent = true;
	} catch (InvocationTargetException e) {
	    error = e.getTargetException().toString();
	} catch (Exception e) {
	    error = e.toString();
	}
	return store;
    }
}
