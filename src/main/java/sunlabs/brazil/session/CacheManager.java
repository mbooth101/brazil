/*
 * CacheManager.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2004 Sun Microsystems, Inc.
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
 * Version:  2.2
 * Created by suhler on 00/08/25
 * Last modified by suhler on 04/04/05 14:44:45
 *
 * Version Histories:
 *
 * 2.2 04/04/05-14:44:45 (suhler)
 *
 * 2.1 02/10/01-16:39:08 (suhler)
 *   version change
 *
 * 1.8 02/06/05-16:15:40 (suhler)
 *   un privatize makeKey
 *
 * 1.7 01/05/01-11:02:39 (suhler)
 *   update for new sessionmanager api's
 *
 * 1.6 00/12/11-13:32:27 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.5 00/11/16-10:35:47 (suhler)
 *   change key representation for string keys, make internal methods
 *   accessable by subclasses
 *
 * 1.4 00/10/31-10:20:27 (suhler)
 *   doc fixes
 *
 * 1.3 00/10/06-10:44:39 (suhler)
 *   remove debug prints
 *
 * 1.2 00/08/25-14:38:10 (suhler)
 *   added docs
 *
 * 1.2 00/08/25-11:47:46 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 session/CacheManager.java
 *
 * 1.1 00/08/25-11:47:45 (suhler)
 *   date and time created 00/08/25 11:47:45 by suhler
 *
 */

package sunlabs.brazil.session;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import java.util.Hashtable;

/**
 * This <code>SessionManager</code> associates an object with a Session ID
 * to give Handlers the ability to maintain state that lasts for the
 * duration of a session instead of just for the duration of a request.
 * It should be installed as a handler, whoses init method will replace
 * the default session manager.
 * <p>
 * This version maintains a pool of hashtables.  Once they all
 * fill up - one of them gets tossed, causing any session info in it
 * to be lost.  It uses a simplified approximate LRU scheme.
 * The default session manager doesn't loose any session information, 
 * but grows the heap without bound as the number of sessions increase.
 * <P>
 * properties:
 * <dl class=props>
 * <dt>tables	<dd>The number of Hashtables in the pool (defaults to 6)
 * <dt>size	<dd>The max number of entries in each table (defaults to 1000).
 * </dl>
 *
 * @author	Stephen Uhler (stephen.uhler@sun.com)
 * @version	%V% CacheManager.java
 */

public class CacheManager extends SessionManager implements Handler {
    ScoredHashtable[] pool;	// pool of hashtables
    int active;			// active hashtable for insertions
    int maxElements;		// max elements allowed per table
    int maxTables;		// number of tables in the pool
    String prefix;		// Pattern matching url to do statistics

    /**
     * Install this class as the session manager.
     * Get the number of tables, and the max size per table.
     */

    public boolean
    init(Server server, String prefix) {
	sessions=null;		// don't use table in parent class;
	this.prefix = prefix;
	try {
	    String str = server.props.getProperty(prefix + "tables");
	    maxTables = Integer.decode(str).intValue();
	} catch (Exception e) {
	    maxTables = 6;
	}
	try {
	    String str = server.props.getProperty(prefix + "size");
	    maxElements = Integer.decode(str).intValue();
	} catch (Exception e) {
	    maxElements = 1000;
	}
	if (maxTables < 2) {
	    maxTables = 2;
	}
	if (maxElements < 1) {
	    maxElements = 1;
	}

	server.log(Server.LOG_DIAGNOSTIC, prefix, "\n  pool=" + maxTables +
			"\n  size=" + maxElements);
	active=0;
	pool = new ScoredHashtable[maxTables];
	pool[active] = new ScoredHashtable();
	SessionManager.setSessionManager(this);
	return true;
    }

    /**
     * Don't handle any URL requests (yet) 
     */

    public boolean
    respond(Request request) {
	return false;
    }

    protected Object
    getObj(Object session, Object ident) {
	String key = makeKey(session, ident);
	Object result = pool[active].get(key);
	for (int i=0; result == null && i < maxTables; i++) {
	    if (pool[i] != null && i != active) {
		result = pool[i].get(key);
	    }
	}
	return result;
    }

    protected void
    putObj(String key, Object value) {
	if ((pool[active].size()) >= maxElements) {
            flush();   // this changes "active" as a side effect
        }
	pool[active].put(key, value);
	/* System.out.println(" Inserting into " + active + 
	      " " + pool[active].size() + "/" + maxElements);
        */
    }

    protected void
    putObj(Object session, Object ident, Object value) {
        putObj(makeKey(session, ident), value);
    }

    /**
     * Remove an object from a session table.
     * Don't bother to remove the table if its empty
     */

    public void
    removeObj(Object session, Object ident) {
        String key = makeKey(session, ident);
	for (int i=0; i < maxTables; i++) {
	    if (pool[i] != null && pool[i].containsKey(key)) {
		pool[i].remove(key);
		return;
	    }
	}
	return;
    }

    /**
     * The active hashtable is too big, find the hashtable
     * with the worst Score, clear it, and set it as the active table.
     */

    protected void flush() {
	int worst = -1;		// score of worst table
	// System.out.println("Table " + active + " is full");
	for (int i = 0; i < maxTables; i++) {
	    if (i == active) {
		continue;	// never flush "newest" table
	    }
	    if (pool[i] == null) {
		active = i;
		pool[i] = new ScoredHashtable();
		System.out.println(" Created new table: " + active +
			"/" + maxTables);
		return;
	    }
	    int check = pool[i].score();
	    if (worst == -1 || check < worst) {
		worst = check;
		active = i;
	    }
	}
	pool[active].clear();
	// System.out.println(" clearing " + active + " score: " + worst);
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

    /**
     * Hashtable that counts successful get requests.
     * We use this to keep "score".
     */

    static class ScoredHashtable extends Hashtable {
	int hits = 0;

	public ScoredHashtable() {
	    super();
	}

	public Object get(Object key) {
	    Object result = super.get(key);
	    if (result != null) {
		hits++;
	    }
	    return result;
	}

	/**
	 * return the "score" of a hashtable.  Higher numbers represent
	 * "better" tables to keep.
	 */

	public int score() {
	    return hits;
	}

	public void clear() {
	    hits=0;
	    super.clear();
	}
    }
}
