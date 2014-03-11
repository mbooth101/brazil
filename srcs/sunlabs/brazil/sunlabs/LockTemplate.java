/*
 * LockTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2006 Sun Microsystems, Inc.
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
 * Version:  1.1
 * Created by suhler on 06/08/07
 * Last modified by suhler on 06/08/07 16:24:29
 *
 * Version Histories:
 *
 * 1.2 06/08/07-16:24:30 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/LockTemplate.java
 *
 * 1.1 06/08/07-16:24:29 (suhler)
 *   date and time created 06/08/07 16:24:29 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.server.Server;
import java.util.Hashtable;

/**
 * Template to lock a resource.
 * &lt;lock name=xxx&gt; ... &lt;/lock&gt;
 * Claim exclusive access to the lock named "xxx".
 * Locks are used to serialize access to sections of markup, thus
 * insuring the integrity of data structures.  Traditionally, this was
 * done by managing sessions: since only one template may be active per
 * session, forcing all access to serialized regions into the same session
 * has the desired effect.  However, since sessions are typically managed on
 * a per URL basis, each serialized piece of markup needs to be in its own
 * URL, and a pair of SimpleSessionHandler/UrlMapperHandler needs to be
 * configured for each named serialized region.
 * <p>
 * Only one lock may be held at a time. The current lock (if any) is
 * automatically released at the end of the template.  Care should
 * be taken not to acquire a lock then invoke a blocking operation
 * (such as with the QueueTemplate) or deadlock may occur.
 * <p>
 * Example:
 * <pre>
 * &lt;lock name="server"&gt;
 *   &lt;set namespace="server" name=.....&gt;
 *   ...
 *   &lt;set namespace="server" name=.....&gt;
 * &lt;/lock&gt;
 * </pre>
 * This insures that no other session may access the code protected
 * by the "server" lock, either from this or any other template.
 *
 * @author      Stephen Uhler
 * @version	@(#)LockTemplate.java	1.1
 */

public class LockTemplate extends Template {

    /**
     * Acquire a lock, preventing any other session from accessing
     * the same locked section of markup.
     */

    public void tag_lock(RewriteContext hr) {
        debug(hr);
	hr.killToken();
	String name = hr.get("name");
	if (name == null) {
	    debug(hr, "Missing name attribute");
	    return;
	}
	boolean waited = Lock.getLock(name, hr.request);
	debug(hr,"waited=" + waited);
    }

    /**
     * Release the previosly named lock.
     */

    public void tag_slash_lock(RewriteContext hr) {
        debug(hr);
	hr.killToken();
	Lock.unlock(hr.request);
    }

    /*
     * Make sure my lock (if any) is released.
     */

    public boolean
    done(RewriteContext hr) {
	Lock.unlock(hr.request);
        return super.done(hr);
    }
}

/**
 * Manage a lock on an object.
 */

class Lock {

    /*
     * Global table of locks.  Each lock has 2 entries, one keyed on the
     * string name and one keyed on the object holding
     * the lock.
     */

    static Hashtable locks = new Hashtable(); // global table of locks

    String name;	// name of the lock
    Object owner;	// object to syncronize on

    private Lock(String name, Object owner) {
	this.name = name;	// string name for the lock
	this.owner = owner;	// the object to lock
    }

    /**
     * Wait for the lock holder to notify us.
     */

    void dowait() {
	synchronized (owner) {
	    try {
		owner.wait();
	    } catch (InterruptedException e) {
		System.out.println("wait interrupted: " + this);
	    }
	}
    }

    /**
     * Get a lock, waiting if already un use.
     * Release any locks I already own first.
     * @param name		The name for this lock
     * @param me		The (Request) object to lock
     */

    static boolean getLock(String name, Object me) {
	boolean haveWaited = false;
	unlock(me);
	Lock lock;
	synchronized (locks) {
	    lock = (Lock) locks.get(name);
	    if (lock == null) {
		lock = new Lock(name, me);
		locks.put(name, lock);
		locks.put(me, lock);
		return haveWaited;
	    }
	}
	synchronized (lock) {
	    while(lock.owner != null) {
		lock.dowait();
		haveWaited = true;
	    }
	    lock.owner=me;
	    locks.put(name, lock);
	    locks.put(me, lock);
	}
	return haveWaited;
    }

    /**
     * Return my lock (if any)
     */

    public static void unlock(Object me) {
	Object gone = null;
	synchronized (locks) {
	    Lock lock = (Lock) locks.get(me);
	    if (lock != null) {
		locks.remove(me);
		locks.remove(lock.name);
		gone = lock.owner;
		lock.owner=null;
	    }
	}
	if (gone != null) {
	    synchronized (gone) {
		gone.notifyAll();
	    }
	}
    }

    public String toString() {
	return "lock(" + name + "," + owner + ")";
    }
}
