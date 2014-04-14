/*
 * QueueTemplate.java
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
 * Version:  1.7
 * Created by suhler on 06/01/27
 * Last modified by suhler on 06/12/10 10:12:38
 *
 * Version Histories:
 *
 * 1.7 06/12/10-10:12:38 (suhler)
 *   document "glob" attribute, add "delim"
 *   .
 *   .
 *
 * 1.6 06/11/13-15:40:28 (suhler)
 *   - internal shuffling in preparation for queue aging
 *
 * 1.5 06/08/04-09:35:59 (suhler)
 *   add an option to getQ to not create the Q if it doesn't already exist
 *
 * 1.4 06/06/15-09:54:15 (suhler)
 *   doc fixes
 *
 * 1.3 06/06/14-15:45:58 (suhler)
 *   add enough information to a queue so applications may destroy it when it
 *   is no longer in use.  This is intended to help the AsteriskTemplate
 *   decide when a client has gone away without unregistering for events.
 *   This allows long runnig applications that use Queue to dispose of unused
 *   ones in an application specific way
 *
 * 1.2 06/01/27-08:36:01 (suhler)
 *   Version from the Asterisk package
 *
 * 1.2 06/01/27-08:27:18 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/QueueTemplate.java
 *
 * 1.1 06/01/27-08:27:17 (suhler)
 *   date and time created 06/01/27 08:27:17 by suhler
 *
 */

package sunlabs.brazil.template;

import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.util.Glob;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.StringMap;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;

/* 
 * New version of Q template.
 * Instead of "message" and "meta", we will xfer a "properties" table
 * if name/value pairs.  This should be backward compatible
 */

/**
 * Template class for Managing simple Queues, allowing text communication
 * among sessions.
 * The "name" attribute names the Q.  When enqueueing messages, "name"
 * is a white space separated list of queue recipients. When Dequeueing
 * Messages, "name" is the recipient Queue.
 * <p>
 * The following tags are recognized.
 * <ul>
 * <li><code>&lt;enqueue name="recipients ..." data=var1 var2 ... varn"
 *     [glob="..." delim="." from="sender" nocreate="true|false"] &gt;</code>
 * <p>
 * <li><code>&lt;dequeue name="name" prepend="props prefix"
 *     timelimit="sec"&gt;</code>
 * If "timelimit" has a suffix of "ms", then the time is taken in ms.
 * <li><code>&lt;queueinfo name="q_name" prepend="props prefix" clear 
       remove=true|false create=true|false&gt;</code>
 * </ul>
 * This format is supported for backward compatibility with the old behavior.
 * (Just in case the compatibility isn't quite right, see QueueTemplateOld).
 * <ul>
 * <li><code>&lt;enqueue name="recipients ..." meta="anything" message="msg"
 *                       from="sender" [nocreate="true|false"] &gt;</code>
 * </ul>
 *
 * @author		Stephen Uhler
 * @version		 @(#)QueueTemplate.java	1.5 1.5
 */

public class QueueTemplate extends Template {
    public static final String Q_ID = "q";	// session table key

    /*
     * We need to remove stale queues.  For now, keep track of all
     * Q's created so we can at least find them.
     * XXX this should be per-server, not static.
     */

    // static Hashtable queueTable = new Hashtable();

    /**
     * Add a text message onto a named queue.
     * Attributes:<br>
     * - name:	the name of the queue (required). "name" is a <code>delim</code>
     *   list of queues to send the data too.<br>
     * - from:  the sender name<br>
     * - data:  a set of property names to send as name/value pairs.
     *   If Names provided of the form nnn#vvv, then "vvv" is taken as the
     *	 value to use if it is not already set, otherwise the empty
     *   string is used as the value.<br>
     * - nocreate: If set, The item will not be Queued if the Q for that
     *	 recipient does not already exist.<br>
     * - force:	  put to the queue even if its closed<br>
     * - delim:  The delimiter character for the list of names. (It defaults
     *   to " " for backward compatibility with the previous behavior)
     * <p>
     * The property "count" contains the number of recipients for which
     * the message was successfully Queued.  The property "error.name"
     * will contain an error message if Queueing failed. In both cases, 
     * the template prefix will be prepended.  It is not considered an
     * error condition for a message not to be delivered to a non existent
     * Queue if "nocreate" is set.
     * <p>
     * The experimental attribute "glob" may be used instead of "name",
     * in which case name is taken as the list of "delim" separated
     * tokens named by the first wildcard substring of the matching
     * glob pattern.
     */

    public void
    tag_enqueue(RewriteContext hr) {
	String names = hr.get("name");
        String glob = hr.get("glob");	// specify names as a glob pattern in properties
	String from = hr.get("from", "anonymous");
	String delim = hr.get("delim", " ");
	boolean noCreate = hr.isTrue("nocreate");
	boolean force = hr.isTrue("force");
	String vars=hr.get("data");

	// these are obsolete, and have been replaced by "data"
	String meta = hr.get("meta");
	String message = hr.get("message", "Hello");

	hr.killToken();
	debug(hr);

	/* supply names as a glob pattern - experimental */

        if (names==null && glob != null) {
	   StringBuffer sb = new StringBuffer();
	   Enumeration e = hr.request.props.propertyNames();
	   String[] substr = new String[1];
	   boolean ok = false;
	   while(e.hasMoreElements()) {
	       if (Glob.match(glob, (String) e.nextElement(), substr)) {
		   sb.append(ok?delim:"").append(substr[0]);
		   ok=true;
	       }
	   }
	   if (ok) {
	       names = sb.toString();
	       hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
		       "enqueue " + glob + "=(" + names + ")");
	   }
        }

        if (names==null) {
           debug(hr, "Missing Q name");
           return;
	}

	// gather dictionary of values to put into Q.  We'll use a
	// StringMap as we don't expect that many items.  We could
	// do wildcard matching.  Missing values get "".

	StringMap data = new StringMap();
	if (vars != null) {
	    StringTokenizer st = new StringTokenizer(vars);
	    while (st.hasMoreTokens()) {
	        String name = st.nextToken();
		String dflt = "";
		int idx = name.indexOf("#");
		if  (idx > 0) {
		    dflt = name.substring(idx+1);
		    name=name.substring(0,idx);
		}
		data.put(name, hr.request.props.getProperty(name, dflt));
	    }
	} else {		// backward compatible
	    data.put("message", message);
	    if (meta != null) {
	        data.put("meta", meta);
	    }
	}
	QueueItem item = new QueueItem(from, data);

	// set to the queue or queues

	StringTokenizer st = new StringTokenizer(names, delim);
	int count = 0;
	while (st.hasMoreTokens()) {
	    String name = st.nextToken();
	    Queue q = getQ(name, !noCreate);
	    if (q != null && !q.put(item, force)) {
	        hr.request.props.put(hr.prefix + "error." + name, " Q full");
	    } else {
		count++;
	    }
	}
	hr.request.props.put(hr.prefix + "count", "" + count);
    }

    /**
     * Allow a message to be enqueued from java code.
     * Use the {@link #enqueue(String to, String from, Dictionary data,
     * boolean noCreate, boolean force)} method instead.
     *
     * @deprecated
     * @param to:	The queue name
     * @param from:	The sender of the data
     * @param message:	The message to enqueue
     * @param meta:	The meta data, if any
     * @param noCreate:	If true, don't create a Q if it doesn't already exist
     * @param force:	Force item onto Q even if it is closed
     * @return		True, if the data was enqueued
     */

    public static boolean
    enqueue(String to, String from, String message, 
		String meta, boolean noCreate, boolean force) {
	StringMap data = new StringMap();
	data.put("message", message);
	data.put("meta", meta);
	QueueItem item = new QueueItem(from, data);
        return enqueue(to, from, data, noCreate, force);
    }

    /**
     * Allow a message to be enqueued from java code.
     * @param to:	The queue name (only a single q)
     * @param from:	The sender of the data
     * @param data:	a dictionary f name/value pairs
     * @param noCreate:	If true, don't create a Q if it doesn'a already exist
     * @param force:	Force item onto Q even if it is closed
     * @return		True, if the data was enqueued
     */

    public static boolean
    enqueue(String to, String from, Dictionary data,
		boolean noCreate, boolean force) {
	QueueItem item = new QueueItem(from, data);
	Queue q = getQ(to, !noCreate);

	// for backward compatibility
	item.message = (String) data.get("message");
	item.meta = (String) data.get("meta");

	return (q != null && q.put(item, force));
    }

    /**
     * Remove a Queue, freeing its resources.
     */

    public static void
    destroyQueue(String name) {
	SessionManager.remove(name, Q_ID);
	// queueTable.remove(name);
    }

    /**
     * Remove an item from the queue, and generate the appropriate properties.
     * Attributes:<br>
     * <dl>
     * <dt>name<dd>The name of the queue to examine
     * <dt>prepend<dd>The prefix in the properties table to use to
     * set the results.  Defaults to our prefix.
     * <dt>timelimit<dd>how long to wait for an item to appear, in sec.
     * Defaults to 30.
     * </dl>
     * If an item is retrieved, the following request properties
     * are set (preceded by prepend):
     * <dl class=props>
     * <dt>age<dd>Set how long the message has been q's (in seconds)
     * <dt>sent<dd>Set the timestamp (in sec) of when the item was q'd.
     * <dt>items<dd>Set the number of Q'd items.
     * <dt>from<dd>Set the (unauthenticated) sender.
     * <dt>error<dd>Something went wrong.  Set an error message.
     * </dl>
     * In addition, all name/value pairs send as part of the message
     * will be set as well.  (In prewvious versions of this template, 
     * that consists of "message" and (optionally) "meta".)
     * <p>
     * Note: this tag blocks until an item is received in the Queue
     * (or a timelimit expires).  As template processing is synchronized
     * based on sessions, care should be taken to avoid blocking
     * other (unrelated) session based requests while waiting on
     * the queue.
     */

    public void
    tag_dequeue(RewriteContext hr) {
	String name = hr.get("name");
	String prepend = hr.get("prepend", hr.prefix);
	String timelimit = hr.get("timelimit", "30");

	if (!prepend.endsWith(".")) {
	    prepend += ".";
	}

        int timer = 30;
	int scale=1000;
	if (timelimit.endsWith("ms")) {
	    timelimit = timelimit.substring(0,timelimit.length()-2);
	    scale=1;
	}
	try {
	    timer = Integer.decode(timelimit).intValue();
	} catch (Exception e) {}

	hr.killToken();
	debug(hr);
        if (name == null) {
           debug(hr, "Missing Q name");
           return;
	}
	Queue q = getQ(name);
	hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
		name + ": Q.get(" + timer + ")...");
	QueueItem item = (QueueItem) q.get(timer*scale);
	if (item != null) { 
	    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
		    name + ": Q.get(" + timer + ") " + item);

	    // dump message into properties

	    Enumeration keys = item.data.keys();
	    while(keys.hasMoreElements()) {
		String key = (String) keys.nextElement();
		hr.request.props.put(prepend + key, item.data.get(key));
	    }

	    // now put meta-data into properties, overriding message
	    // data if needed (?)

	    hr.request.props.put(prepend + "from", item.from);
	    hr.request.props.put(prepend + "age", "" +
		     (System.currentTimeMillis() - item.timestamp)/1000);
	    hr.request.props.put(prepend + "sent", "" + (item.timestamp/1000));
	} else {
	    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
		    name + ": Q.get() Timeout");
	    hr.request.props.put(prepend + "error", "timelimit");
	}
        return;
    }

    /**
     * Program access to the Q.
     * @param name;	The name of the Q.  A new Q will be created if
     *			it doesn't already exist.
     * @param timelimit: how long (in ms) to wait before returning
     * @return		The Queue item, or Null if no item is available.
     */

    public static QueueItem
    dequeue(String name, int timelimit) {
	Queue q = getQ(name);
	return (QueueItem) q.get(timelimit*1000);
    }

    /**
     * Return info about the Q, and optionally clear or remove it.
     * If the queue doesn't already exist, it is created.
     * Attributes:<br>
     * <dl>
     * <dt>name<dd>The name of the queue to examine
     * <dt>prepend<dd>The prefix in the properties table to use to
     * set the results.  Defaults to our prefix.
     * <dt>clear<dd>If set, then clear the queue
     * <dt>remove<dd>If set, then remove the queue
     * <dt>closed=[true|false]<dd>set the closed state of the Q
     * <dt>create=[true|false]<dd>Should the Q be created if it doesn't already
     *	exist (defaults to true).
     * </dl>
     * The following request properties
     * are set (preceded by prepend):
     * <dl>
     * <dt>lastIn<dd>The timestamp (ms) of the last Q insert attempt.
     * <dt>lastOut<dd>The timestamp (ms) of the last Q retrieval attempt.
     * <dt>size<dd>The number of items in the Q.
     * <dt>count<dd>The total number of items inserted into the Q
     * <dt>created<dd>The timestamp (in ms) of the Q's creation
     * <dt>expires<dd>The Q's expiration period (in ms)
     * </dl>
     * If the Q doesn't exist (e.g. create=false), then the property
     * "error" is set.
     */

    public void
    tag_queueinfo(RewriteContext hr) {
	String name = hr.get("name");
	String prepend = hr.get("prepend", hr.prefix);
	boolean clear = hr.isTrue("clear");
	boolean remove = hr.isTrue("remove");
	boolean notify = hr.isTrue("notify");
	boolean closed = (hr.get("closed") != null);
	boolean shouldCreate = (hr.get("create")==null || hr.isTrue("create"));
	int expire = -1;
	try {
	    expire = Integer.decode(hr.get("expires", "-1")).intValue();
	} catch (Exception e) {}

	if (!prepend.endsWith(".")) {
	    prepend += ".";
	}

	hr.killToken();
	debug(hr);
        if (name == null) {
           debug(hr, "Missing Q name");
           return;
	}
	Queue q = getQ(name, shouldCreate);
	if (q == null) {
	    hr.request.props.put(prepend + "error", "Q doesn't exist");
	    return;
	}
	hr.request.props.put(prepend + "lastIn", "" + q.lastIn());
	hr.request.props.put(prepend + "lastOut", "" + q.lastOut());
	hr.request.props.put(prepend + "size", "" + q.size());
	hr.request.props.put(prepend + "count", "" + q.count());
	hr.request.props.put(prepend + "closed", "" + q.isClosed());
	hr.request.props.put(prepend + "created", "" + q.getCreated());
	hr.request.props.put(prepend + "expires", "" + q.getExpires());
	if (closed) {
	    q.setClosed(hr.isTrue("closed"));
	}
	if (clear) {
	    q.clear();
	}
	if (notify) {
	    q.kick();
	}

	if (expire != -1) {
	    q.setExpires(expire);
	}

	if (remove) {
	    destroyQueue(name);
	}
    }


    /**
     * Return a Q.
     * All calls to the session manager go through here, so we
     * can reap old queues some day.
     */

    public static Queue
    getQ(String name, boolean create) {
        Queue q =  (Queue) SessionManager.getSession(name, Q_ID,
                create ? Queue.class : null);
	/*
	if (q != null) {
	    queueTable.put(name, q);
        }
	*/
        return q;
    }

    public static Queue
    getQ(String name) {
        return getQ(name, true);
    }

    /**
     * Return a hashtable of all the Q's by name.
    public static Hashtable getAllQueues() {
	return queueTable;
    }
     */

    /**
     * A bag of items to keep on the Q.
     * We could add other stuff later.
     */

    public static class QueueItem {
        public long timestamp;	// when the item was queued 
	public String from;	// Who sent it
	public Dictionary data; // message name/value pairs

	// these are for backward compatibility only.  Don't use.
	// They are needed by the old dequeue() method.

	public String message;	// What is the message
	public String meta;	// Arbitrary meta info about the message

        public QueueItem(String from, Dictionary data) {
	    timestamp = System.currentTimeMillis();
	    this.from=from;
	    this.data = data;

	    // backward compatibility
	    message=(String) data.get("message");
	    meta=(String) data.get("meta");
	}

	/**
	 * Add an item to the Q.
	 * "meta" is application specific "meta" data associated with 
	 * this Q item.
	 * @deprecated
	 * Use the other constructor instead
	 */

        public QueueItem(String from, String message, String meta) {
	    StringMap data = new StringMap();
	    data.put("message", message);
	    data.put("meta", meta);
	    timestamp = System.currentTimeMillis();
	    this.from=from;

	    // backward compatibility
	    this.message = message;
	    this.meta = meta;
	}

        public String toString() {
            return from + ": " + data + " (" + timestamp + ")";
        }
    }

    /**
     * Create an object queue.  "Getters" wait 'till something
     * appears in the queue.
     */

    public static class Queue {
	public static int max=100;  
	Vector q;	// where to hold the items
	Object mutex = new Object();
	long lastGet;	// timestamp of last get
	long lastPut;	// timestamp of last put
	int maxItems;	// max q size
	int count;	// number of items Q'd
	boolean closed; // The queue is closed
	long created;   // when was this queue created (ms)
	long expires;   // how much idle time before expiration

	/**
	 * Create a new Q of a maximum possible size
	 */

	public Queue() {
	   lastGet = lastPut = -1;
	   count = 0;
	   maxItems = max;
	   q = new Vector();
	   closed = false;
	   created = System.currentTimeMillis();
	   expires = 0;	// doesn't expire
        }

	/**
	 * Return the next item on the queue, waiting for up to
	 * "timeout" seconds or for an interrupt.
	 * @return the top of the Q, or null.
	 */

	public Object
	get(int timeout) {
	    try {
		String name = Thread.currentThread().getName();
	        synchronized (mutex) {
		    if (q.size() == 0) {
		        mutex.wait(timeout==0 ? 1 : timeout);
		    }
		    lastGet = System.currentTimeMillis();
		    if (q.size() > 0) {
		        Object data = q.firstElement();
		        q.removeElementAt(0);
			return data;
                    }
	        }
	    } catch (InterruptedException e) {}
	    return null;
	}

	/**
	 * Put an item on the queue if it's open and not full.
	 */

	public boolean
	put(Object item) {
	    return put(item, false);
	}

	/**
	 * Put an item on the queue if it's not full.
	 * If "force" is true, override the "closed" flag.
	 */

	public boolean
	put(Object item, boolean force) {
	    if (closed && !force) {
		return false;
	    }
	    boolean result;
	    String name = Thread.currentThread().getName();
            synchronized (mutex) {
		lastPut = System.currentTimeMillis();
                if (q.size() < maxItems) {
		    count++;
	            q.addElement(item);
	            mutex.notifyAll();
                    result = true;
	        } else {
		    result =  false;
		}
	    }
	    return result;
	}

	/**
	 * How many items are queue'd.
	 */

	public int
	size() {
	    return q.size();
	}

	/**
	 * Send a notify: for debugging
	 */

	public void kick() {
            synchronized (mutex) {
	        // System.out.println("*NOTIFY* " + mutex);
	        mutex.notifyAll();
	    }
	}

	/**
	 * Return the last time a Q insertion was attempted.
	 * @return	-1 if no attempts were made.
	 */

	public long
	lastIn() {
	    return lastPut;
	}

	/**
	 * Return the last time a Q removal was attempted.
	 * @return -1 if no attempts were made.
	 */

	public long
	lastOut() {
	    return lastGet;
	}

	/**
	 * Return the total number of items Q'd.
	 * @return The # of Q'd items.
	 */

	public long
	count() {
	    return count;
	}

	/**
	 * Get the expiration period of the Queue (in ms).
	 * The notion of when a queue expires is application dependent.
	 * Applications can look at count() lastIn(), lastOut(). and
	 * created() to determine when the Q is expired for them.
	 */

	public long
	getExpires() {
	    return expires;
	}

	/**
	 * Set the expiration period of the Queue (in ms).
	 */

	public void
	setExpires(long expires) {
	    this.expires = expires;
	}

	/**
	 * Return creation time (ms since epoch).
	 */

	public long getCreated() {
	     return created;
	}

	/**
	 * Clear the queue.
	 */

	public void
	clear() {
	   q.removeAllElements();
	   // lastGet = lastPut = -1;
	   // count = 0;
	}

	/**
	 * Set the closed state.
	 */

	public boolean setClosed(boolean closed) {
	    boolean result = this.closed;
	    this.closed = closed;
	    return result;
	}

	/**
	 * Get the closed state.
	 */

	public boolean isClosed() {
	   return this.closed;
	}

	public String toString() {
	    return("Queue got=" + lastGet + " put=" + lastPut + " " + q);
	}
    }
}
