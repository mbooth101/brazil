/*
 * AsteriskHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2005-2006 Sun Microsystems, Inc.
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
 * Version:  1.17
 * Created by suhler on 05/05/25
 * Last modified by suhler on 06/11/13 10:23:07
 *
 * Version Histories:
 *
 * 1.17 06/11/13-10:23:07 (suhler)
 *   - add name mapping
 *   - better socket close handling
 *   - fix tag_ami bug
 *   .
 *
 * 1.16 06/08/02-14:04:46 (suhler)
 *   sync up with meetme server version.  Q expiration not yet added
 *
 * 1.15 06/06/02-17:09:18 (suhler)
 *   changes for registration timeouts (compiles, not tested)
 *
 * 1.14 06/04/25-14:54:48 (suhler)
 *   doc fixes
 *
 * 1.13 06/04/25-14:35:04 (suhler)
 *   add connection retries and retry intervals for asterisk servers that go away
 *
 * 1.12 06/02/07-14:01:50 (suhler)
 *   - make sure "Server" is added to every event
 *   - add "server" option to register, to restrict events to the particular server
 *
 * 1.11 06/02/06-15:43:34 (suhler)
 *   - add <amicommand> to allow for synchronous AMI commands
 *   - wait a random time before the first keep-alive to keep the keep alives
 *   for multiple servers out of step
 *   - change lots of the diagnostic output to real log() messages
 *   - add more documentation
 *
 * 1.10 06/02/01-16:13:04 (suhler)
 *   - add keep-alive timer
 *   - clean up code and docs.
 *   - make sure we restart properly on socket errors
 *
 * 1.9 05/11/10-11:12:12 (suhler)
 *   version from pbx-support
 *
 * 1.8 05/11/10-10:56:17 (suhler)
 *   old cleanup
 *
 * 1.7 05/06/06-21:06:18 (suhler)
 *   add de-event stuff
 *
 * 1.6 05/05/31-12:16:52 (suhler)
 *   reorganize
 *
 * 1.5 05/05/31-10:11:37 (suhler)
 *   move event cache management into a separate method
 *
 * 1.4 05/05/31-08:55:01 (suhler)
 *   version from Frances
 *
 * 1.3 05/05/31-08:49:49 (suhler)
 *   checkpoint
 *
 * 1.2 05/05/27-10:22:31 (suhler)
 *   checkpoint.
 *
 * 1.2 05/05/25-23:57:34 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 asterisk/AsteriskHandler.java
 *
 * 1.1 05/05/25-23:57:33 (suhler)
 *   date and time created 05/05/25 23:57:33 by suhler
 *
 */

package sunlabs.brazil.asterisk;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.template.QueueTemplate.Queue;
import sunlabs.brazil.template.QueueTemplate.QueueItem;
import sunlabs.brazil.template.QueueTemplate;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.StringMap;
import sunlabs.brazil.util.http.HttpInputStream;
import sunlabs.brazil.util.regexp.Regexp;

/**
 * Connect to asterisk manager api.  There is one connection
 * per server.  This is used both to issue Commands to Asterisk via
 * the manager interface, and to register for, and receive asynchronous
 * event notifications.
 * <p>
 * Usage:
 * <pre>
 * &lt;register queue=xxx key=xxx pattern=xxx [context=xxx server=xxx]&gt;
 *  - register interest in a manager event (or events).  All generated events
 *    will be available with &lt;dequeue name=xxx ...&gt;, where the "name"
 *    parameter of the &lt;dequeue&gt; matches the "queue" parameter of
 *    &lt;register&gt;.
 * &lt;unregister queue=xxx key=xxx&gt;
 *  - unregister interest in a previously registered event.
 * &lt;enqueue name="queue" from="my-q" ...&gt;
 *  - Send an command to the manager interface. "queue" is the name
 *    specified in the handler "queue" parameter.  The enqueue'd data
 *    must have an "action" key.  The result of the command is obtained by:
 *    &lt;dequeue name="my-q" ...&gt;  where "my-q" is the "from" attribute of
 *    the corrosponding &lt;enqueue&gt;
 * &lt;amicommand server=xxx action=xxx ...&gt;
 *  - A syncronous version of the &lt;enqueue&gt; ... &lt;dequeue&gt; above.
 *    See below for details.
 * </pre>
 * <p>
 */

public class AsteriskHandler extends Template implements Handler {

    static final String ID_DELIM = ":="; // Q identifier for ActionId
    static Events events = new Events(); // event rules go here
    /*
     * Keep track of the sever q names so we can do sanity checking
     * from the "amicommand" template
     */
    static Vector servers = new Vector(); // all installed servers go here
    String queue = null;	 // name of the q to accept commands from

    /*
     * Various Events use inconsistent names for event parameters.
     * This table is used to map names to a consistent set.
     */

    static Hashtable amiMap = new Hashtable();
    static {
	amiMap.put("CallerIDname", "CallerIDName");
	amiMap.put("CallerID", "CallerIDNum");
	amiMap.put("CallerIDnum", "CallerIDNum");
    }

    // the handler implementation starts here

    /**
     * Remember the host, port, id, and password for an asterisk
     * manager connection.  We don't require that the asterisk
     * server actually be running at server startup time.  We will try
     * hard to reconnect to the server if it goes away.
     * <dl class=props>
     * <dt>queue
     * <dd>The name of the Q to send manager commands to using the
     * &lt;enqueue name="queue" ...&gt;.
     * If not specifies, the "host:port" combination is used.
     * <dt>server
     * <dd>The server:port to use to contact the asterisk server
     * <dt>userid, password
     * <dd>The Manager credentials
     * <dt>debug=true|false
     * <dd>turn on more diagnostics on the console, depending on the current
     * server logging level. at "3", keep alives are logged, at "4", all events
     * are logged, and at "5" even more stuff is logged.
     * <dt>keepalive=n
     * <dd>If set, this handler will issue a keep-alive every "n"
     * seconds to the Asterisk Server.  If the keep-alive fails, 
     * a new connection will be attempted with the Asterisk server.
     * <dt>retry=n
     * <dd>Set the number of seconds to wait before retrying a broken
     * connection to an asterisk server (defaults to 10).
     * </dl>
     * To communicate with multiple asterisk servers, each should have
     * their own handler instance.  Servers are distinguished when sending
     * commands to them via the "queue" parameter, which should be
     * different for each server.  Alternately, the &lt;amicommand&gt; server
     * parameter (which is the same as the server queue name) may be used.
     * <p>
     * When using multiple servers, only one of the server handler configurations
     * should be listed as a template (there can only be one template instance
     * for a given entity) which server doesn't matter, as events get registered
     * for all servers (the "server" attribute of the response determines
     * where it came from).  As above, commands to a server are distinquished
     * with either the "queue" or "server" attributes, depending on whether
     * the command Qs are used directly, or the &lt;amicommand&gt;
     * template is used.
     * <p>To Do<br>
     * Figure out where to send unregistered events, such as "reload".
     */

    public boolean
    init(Server server, String prefix) {
	String host = server.props.getProperty(prefix + "server");
	queue = server.props.getProperty(prefix + "queue", host);
	String user = server.props.getProperty(prefix + "userid");
	String pass = server.props.getProperty(prefix + "password");
	boolean debug = Format.isTrue(server.props.getProperty(prefix+"debug"));
	int keepalive = 0;	// send keepalives (seconds)
	try {
	    keepalive = Integer.decode(server.props.getProperty(prefix +
		"keepalive")).intValue();
	} catch (Exception e) {}

	if (host==null || user==null || pass==null) {
	    server.log(Server.LOG_WARNING, prefix,
		"missing required parameter (server|userid|password)");
	    return false;
	}

	if (servers.contains(queue)) {
	    server.log(Server.LOG_WARNING, prefix,
		"queue \"" + queue + "\" already in use!");
	} else {
	    servers.addElement(queue);
	}

	ConnectInfo conn = new ConnectInfo(host, user, pass);
	try {
	    int sec =  Integer.decode(server.props.getProperty(prefix +
		"retry")).intValue();
	    conn.setRetryTime(sec * 1000);
	} catch (Exception e) {}

	if (conn.verifyHost()) {
	    AMIReader reader = new AMIReader(conn, queue, server, prefix);
	    reader.setDebug(debug);
	    reader.start();
	    if (keepalive > 0) {
		(new Keepalive(reader, keepalive)).start();
	    }
	    return true;
	} else {
	    server.log(Server.LOG_WARNING, prefix, "Unknown host: " + host);
	    return false;
	}
    }

    /**
     * The handler only registers * servers. No requests are handled.
     * Use &lt;register&gt; in a template instead.
     */

    public boolean
    respond(Request request) throws IOException {
	return false;
    }

    // The Template implementation starts here.

    /**
     * This only emits diagnostic information to stdout.
     */

    public void
    tag_asterisk(RewriteContext hr) {
	debug(hr);
	hr.killToken();
	String prefix = hr.get("prepend", hr.prefix + "event");
	int i = 1;
	Enumeration e = events.getEvents();
	while(e.hasMoreElements()) {
	    hr.request.props.put(prefix + "." + (i++),
		    ((EventItem) e.nextElement()).toString(";", ","));
	}
    }

    /**
     * Issue a synchronous command to the Asterisk AMI interface.
     * This is a convenience for using &lt;enqueue&gt;
     * and &lt;dequeue&gt; directly.
     * <p>
     * Attributes:
     * <dl>
     * <dt>server	<dd>The Asterisk server's Q (required).
     * <dt>action:	<dd>The action to perform.
     * <dt>variable=value <dd>one of the variables needed by the action.
     *	(There is a fixed list, See the manager docs for more detail).
     * <dt>timeout	<dd>The max time to wait for a response
     * <dt>prepend	<dd>What to prepend all the results too.
     * </dl>
     */

    public void
    tag_amicommand(RewriteContext hr) {
	debug(hr);
	hr.killToken();
	String server = hr.get("server"); // Q of server to send command to
	String from = server + "-ami" + hr.sessionId; // where to get the answer
	int timeout=5;
	try {
	    timeout = Integer.decode(hr.get("timeout")).intValue();
	} catch (Exception e) {}
	String prepend = hr.get("prepend");
	String action = hr.get("action");

	if (prepend == null) {
	    prepend = "";
	} else if (!prepend.endsWith(".")) {
	    prepend += ".";
	}

	if (action==null || server==null) {
	    debug(hr, "action and server attributes are required");
	    hr.request.props.put(prepend + "error", "action or server missing");
	    return;
	}

	if (!servers.contains(server)) {
	    debug(hr, "No such server running");
	    hr.request.props.put(prepend + "error", "invalid server");
	    return;
	}

	// copy the attributes into a new Map for the enqueue

	StringMap map = new StringMap();
	Enumeration e = hr.keys();
	while (e.hasMoreElements()) {
	    String key = ((String) e.nextElement()).toLowerCase();
	    if (key.equals("timeout") ||key.equals("server") ||
		    key.equals("prepend")) {
		continue;
	    }
	    map.add(key, hr.get(key));
	}

	/*
	 * We could have an event from a previous cancelled request.
	 * This can still break: we need to use ActionID matching 
	 * to get this right (XXX some day).
	 */
	QueueTemplate.getQ(from).clear();
        boolean qd = QueueTemplate.enqueue(server, from, map, false, false);
	QueueTemplate.QueueItem qi = QueueTemplate.dequeue(from, timeout);

	if (qi == null) {
	    hr.request.props.put(prepend + "error", "no data");
	} else {
	    StringMap result = (StringMap) qi.data;
	    for (int i = 0; i < result.size(); i++) {
	        hr.request.props.put(prepend + result.getKey(i), result.get(i));
            }
	}
    }

    /**
     * Register an event.
     * <pre>&lt;register queue=xxx key=xxx exp=xxx [context=xxx server=xxx]&gt;
     *  queue:  The Q name to send the results to.
     *  key:    The manager response key to match on.  Use "*" for all keys.
     *  exp:	A regular expression that matches a key value
     *  context: If specified, only events with this context are considered. 
     *  server: If specified, only events from this server are considered. 
     *	The server matches the "Server" item in the event, and is the server
     *  name, followed by a ":", then the port number (e.g. pbx.com:5038).
     * </pre>
     */

    public void
    tag_register(RewriteContext hr) {
	debug(hr);
	hr.killToken();
	String queue = hr.get("queue");
	String exp = hr.get("exp");
	String key = hr.get("key", "*");
	String context = hr.get("context");
	String serverName = hr.get("server");
	if (exp==null || queue==null) {
	    debug(hr, "append tag needs queue and exp");
	    return;
	}
	addEvent(queue, key, exp, context, serverName);
    }

    /**
     * Java access to adding event registrations.
     */
    
    public static void
    addEvent(String queue, String key, String exp, String context,
	    String serverName) {
	events.addEvent(queue, key, exp, context, serverName);
    }

    /**
     * Unregister an event (or events).  Match on "queue", "exp" and "key"
     */

    public void
    tag_unregister(RewriteContext hr) {
	debug(hr);
	hr.killToken();
	String queue = hr.get("queue");
	String exp = hr.get("exp");
	String key = hr.get("key");
	if (key==null && exp==null && queue==null && !hr.isTrue("all")) {
	    debug(hr, "Must specify all=true to remove all events");
	} else {
	    int removed = removeEvents(queue, key, exp);
	    debug(hr, removed + " events removed");
	}
    }

    /**
     * java access to removing event registrations.
     */

    public static int
    removeEvents(String queue, String key, String exp) {
	return events.removeEvents(queue, key, exp);
    }

    static final int RETRY_CONNECTION_MS=10000;

    /**
     * Keep the info needed to connect and authenticate 
     * to an asterisk AMI socket.
     */

    static class ConnectInfo {
	public static final int AMI_PORT=5038;	// asterisk manager port

	String host;		// the asterisk host
	String user;		// the user id
	String password; 	// the connection password
	int port=AMI_PORT;	// the port
	AmiStringMap login;	// the login map
	int retryMs;		// how long to wait before retrying a dead conn.

	/**
	 * Create the connection info
	 * @param host		asteriskserver[:port]
	 */

	public ConnectInfo(String host, String user, String password) {
	    this.user=user;
	    this.password=password;
	    retryMs = RETRY_CONNECTION_MS;

	    int indx = host.indexOf(":");
	    if (indx > 0) {
	        try {
		    port = Integer.decode(host.substring(indx+1)).intValue();
	        } catch (Exception e) {}
	        this.host = host.substring(0, indx);
	    } else {
		this.host=host;
	    }
	    login = null;
	}

	/**
	 * Set the connection retry timer.
	 */

	public void setRetryTime(int ms) {
	    retryMs = ms;
	}

	/**
	 * Get the connection retry timer.
	 */

	public int getRetryMs() {
	    System.out.println("Waiting " + (retryMs/1000) + "s ...");
	    return retryMs;
	}

	/**
	 * Check to see if the server name exists.
	 */

	public boolean
	verifyHost() {
	    try {
		InetAddress.getByName(host);
		return true;
	    } catch (UnknownHostException e) {
		return false;
	    }
	}

	/**
	 * Try to get a socket to asterisk.
	 */

	public Socket
	connect() throws UnknownHostException, IOException {
	    return new Socket(host, port);
	}

	/**
	 * Return command required to login to the manager.
	 */

	public AmiStringMap
	loginMap() {
	    if (login == null) {
		login = new AmiStringMap();
		login.add("Action", "login");
		login.add("Username", user);
		login.add("Secret", password);
	    }
	    return login;
	}

	/**
	 * Identify the asterisk server we are talking to.
	 */

	public String
	getHost() {
	    return host + ":" + port;
	}

	public String
	toString() {
	    return "AMI connection to " + host + ":" + port + " as " + user;
	}
    }

    /**
     * Keep track of an event listener entry.  [I'm not sure what this
     * should do yet.]  Each time an event arrives, we traverse the list
     * checking for each regexp match.  When a match is found, we send
     * the event to all the listening Q's.
     */

    public static class EventItem {
        Vector queues;	// The destination Q (or Q's)
        String name;	// name of event for Q (string form of regexp)
	String key;	// The key to match on
	String context; // the required context (if any)
	String serverName; // only this server (all if null)
        Regexp re;	// Regular expression to match event

        public EventItem(String queue, String key, String exp, String context,
		String serverName) {
	    name = exp;
	    re = new Regexp(exp);
	    this.key = key;
	    this.context = context;
	    this.serverName = serverName;
	    queues = new Vector();
	    queues.addElement(queue);
        }

	/**
	 * Add a new queue to an existing event. 
	 * @param queue		The destination Q
	 * @param exp		The regular expression
	 * @return		true if there is now an event/q match
	 */

        public boolean
	addQ2Event(String queue, String key, String exp, String context,
		String serverName) {
	    if (!exp.equals(name) || !key.equals(this.key)) {
	        return false;
	    } else if (this.context != null && !this.context.equals(context)) {
	        return false;
	    } else if (queues.indexOf(queue)<0) {
	        queues.addElement(queue);
	    }
	    return true;
        }

	/**
	 * Remove an exp/Q pair.  Return true if removed.
	 * @param queue		The destination Q to remove (or all if null)
	 * @param key		The event key to match on (null for all keys)
	 * @param exp		The event re (or null for all re's)
	 * @return		true if something was removed
	 */

        public boolean
	remQEvent(String queue, String key, String exp) {
	    if ((exp==null || exp.equals(name)) &&
		    (key==null || key.equals(this.key))) {
		int indx;
		if (queue == null && queues.size() > 0) {
		    // System.out.println("RMQ Clearing " + this);
		    queues.clear();
		    return true;
		} else if ((indx = queues.indexOf(queue)) >= 0) {
		    queues.removeElementAt(indx);
		    // System.out.println("RMQ Removing " + queue + " from " + this);
		    return true;
		}
	    } 
	    return false;
        }

	public int
	size() {
	    return queues.size();
	}

	/**
	 * Send an event to the q's if there is a match.
	 * XXX need to think about event format.
	 * XXX if key contains '*' or '?' do globbing
	 */

	public boolean
	send2Q(Dictionary event) {
	    String value = (String) event.get(key);
	    String context = (String) event.get("context");
	    String sn = (String) event.get("Server");
	    if (value != null && re.match(value) != null &&
			(this.context==null || context.equals(context)) &&
			(this.serverName==null || serverName.equals(sn))) {
		for(int i=0;i<queues.size();i++) {
		    String queue = (String) queues.elementAt(i);
		    QueueTemplate.enqueue(queue, name, event, false, false); 
		}
		/*
		System.out.println("AsteriskHandler sending: " + 
				value + "/" + event.get("usernum") +
				" (" + queues.size() + ") " + this);
		*/
		return true;
	    } else {
		return false;
	    }
	}

	public String
	toString() {
	    return "Event (" + name + "/" + key + ")=>" + queues;
	}

	/** Machine readable version */

	public String toString(String delim, String delim2) {
	    String result =  name + delim + key + delim +
		    context + delim + serverName + delim;
	    String d2 = "";
	    for (int i=0;i<queues.size();i++) {
		result += d2 + queues.elementAt(i);
		d2 = delim2;
	    }
	    return result;
	}
    }

    /**
     * A thread that reads responses from the AMI.  Each time we read a
     * response, we package it into a dictionary, and send it to the
     * appropriate Q or Q's.  We tacked the proper Q information onto
     * the "ActionID" key of the command so we can figure out where to
     * send the result.  If no Q matches the response, then it is an
     * unregistered event, so we ignore it.
     * <p>
     * Much of the useful information in a response is implied from
     * previous responses with the same "uniqueid" field.  We accumulate
     * of all the data associated with a "uniqueid", and send the accumulation
     * when needed.
     * <p>
     * If our connection to Asterisk fails, we kill the corrosponding
     * Writer thread, and attempt to re-establish the connection.
     */

    static class AMIReader extends Thread {
	ConnectInfo info;	// info required to connect to server
	String queue;		// name of Q to get command from
	String prefix;		// name of our prefix in server props
	Server server;		// a server reference for logging
	Writer writer = null;	// thread to send commands to *
	boolean debug=false;    // log stuff to stderr?

	Socket sock = null;	// socket to server
	HttpInputStream in;	// the input stream to read from

	// keep pending events for this server
	// keys are unique id's
	// values are the event StringMaps

	Hashtable cache = new Hashtable();

	public AMIReader(ConnectInfo info, String queue, Server server,
		String prefix) {
	    this.info = info;
	    this.queue = queue;
	    this.server=server;
	    this.prefix=prefix;

	    setName("asterisk listener: " + queue);
	    setDaemon(true);
	}

	public String
	getQueueName() {
	    return queue;
	}

	public String
	getHost() {
	    return info.getHost();
	}

	/**
	 * Connect to the * server if we aren't connected to it.
	 * Then login to it.
	 * XXX this needs to work with Writer better, and not
	 * send the login commands directly.
	 */

	boolean
	verifyConnection() {
	    Request.HttpOutputStream out;  // where to write commands to
	    String loginStr = info.loginMap().commandify(null);
	    if (sock==null) {
		try {
		    sock = info.connect();
		    in = new HttpInputStream(sock.getInputStream());
		    out = new Request.HttpOutputStream(sock.getOutputStream());
		    out.writeBytes(loginStr);
		    out.flush();
		} catch (IOException e) {
		    sock=null;
		    if (writer != null) {
			writer.die("failed to create socket");
			writer=null;
		    }
		    server.log(Server.LOG_WARNING, "asterisk",
				"Can't connect to " + this + " " + e);
		    return false;
		}
		if (queue != null) {
		    writer = new Writer(queue, out, this);
		    writer.start();
		} else {
		    server.log(Server.LOG_LOG, "asterisk",
				"No command queue listener defined");
		}
		return true;
	    }

	    // XXX we should check the result here
	    return true;
	}

        public void
	run() {
	    log(3,"Starting thread: " + info);

	    // We'll reuse this map for each event, so we need to
	    // give away copies.
	    //
	    AmiStringMap map = new AmiStringMap();

	    while(true) {
		if (!verifyConnection()) {
		    log(2,"Connection didn't restart, wait: " + info);
		    try {
			Thread.sleep(info.getRetryMs());
		    } catch (InterruptedException e) {
			log(5, (info.getRetryMs()/1000) + "s sleep interrupted: " + e);
		    }
		    continue;
		}
		map.clear();
		try {
		    map.read(in);
		} catch (IOException e) {
		    log(3,"read from asterisk failed: " + e);
		    try {
			Thread.sleep(info.getRetryMs());
		    } catch (InterruptedException e2) {
			log(5, (info.getRetryMs()/1000) + "s sleep interrupted: " + e2);
		    }
		    map.clear();
		    close();
		    cache.clear();
		    continue;
		}
		String type = "error";
		try {
		    type = map.getKey(0).toLowerCase();
		} catch (Exception e) {
		    log(3,"bad event type: " + map);
		}
		map.add("Server", getHost());

		if (type.equals("event")) {
		    AmiStringMap merged = getMergedMap(map);
		    boolean sent = events.processEvents(merged);
		    log(4,"Event: " + (sent ? "(SENT) ":"") + map);
		} else if (type.equals("response") && writer!= null) {
		    AmiStringMap reply = new AmiStringMap(map);
		    log(4,"Command response: " + reply);
		    writer.reply(reply, queue);
		} else {
		    log(3, "Unknown Asterisk AMI reponse: " + type);
		}
	    }
	}

	/**
	 *  force the socket closed, so we can reconnect
	 */

	public void close() {
	    if (sock != null) {
		try {
		    sock.close();
		} catch (Exception e) {
		   log(2, "Asterisk reader socket won't close: " + e);
		}
		sock = null;
	    }
	    if (writer != null) {
		writer.die("socket went away");
		writer=null;
	    }
	}

	/**
	 * Merge event with previous events and return the union.  This
	 * manages the cache of saved events.
	 * @param map	The current event
	 * @return	The original map or a "merged" map
	 */

	AmiStringMap
	getMergedMap(AmiStringMap map) { 
	    String id = map.get("uniqueid");
	    if (id == null) {
		log(5,"No unique-id for " + map);
		return new AmiStringMap(map);
		// return map;
	    }

	    AmiStringMap cached = (AmiStringMap) cache.get(id);
	    if (cached == null) {
		log(5, "caching info for unique-id " + id);
		cached = new AmiStringMap();
		cached.append((AmiStringMap)map, true);
		cache.put(id, cached);
	    } else {
		cached.append(map, false);
	    }
	    if ("Hangup".equals(map.get("event"))) {
		cache.remove("id");
		log(5, "Removing uniquid cache: " + id);
	    }
	    return new AmiStringMap(cached);
	}

	/**
	 * Read command from the command Q, and send them to the AMI.
	 * We need a different thread to read commands from the command Q, 
	 * and write them to the socket, so we won't block.  It will
	 * be managed by our reader thread.
	 */

	static class
	Writer extends Thread {
	    String queue;	// q to read commands from
	    AMIReader reader;
	    Request.HttpOutputStream out = null;  // where to write commands to
	    boolean alive=true;

	    Writer(String queue, Request.HttpOutputStream out, AMIReader reader) {
		this.queue = queue;
		this.out = out;
		this.reader = reader;
		setName("asterisk writer: " + queue);
		setDaemon(true);
	    }

	    /*
	     * Loop reading commands from the Q and sending them to AMI.
	     * if our AMI connection dies, then end this thread; the
	     * reader will detect the error and make a new one of us.
	     */

            public void
	    run() {
		System.out.println("Running command thread for: " + queue);
		while(alive) {
		    QueueTemplate.QueueItem item =
			    QueueTemplate.dequeue(queue, 10000);
		    if (item == null || !alive) {
			continue;
		    }
		    StringMap data = (StringMap) item.data;
		    reader.log(5, "AMI command from " + item.from + ": " + data);

		    String cmd = AmiStringMap.commandify(data, item.from);
		    if (cmd == null) {
		        System.out.println("Bad command");
			continue;
		    }
		    try {
		        out.writeBytes(cmd);
		        out.flush();
		    } catch (IOException e) {
		        System.out.println("Command write failed");
			// our reader will pick up this error and
			// call die()
		    }
		}
		System.out.println("Killed command thread for: " + queue);
	    }

	    /*
	     *  Extract the Q name out of the "ActionID".
	     *  We need to match the manager commands with their responses, 
	     *  and then send the response to the proper Q.  We'll tack 
	     *  the Q name onto the end of the ActionId, and strip it off
	     *  when the result comes back.
	     */

	    String getId(StringMap data) {
		String id = data.get("actionid");
		int index;
		if (id == null || (index=id.lastIndexOf(ID_DELIM)) < 0) {
		    System.err.println("Warning! no ID in ActionID");
		    return null;
		}
		data.remove("actionid");
		if (index > 0) {
		    data.put("ActionId", id.substring(0, index));
		}
		return id.substring(index+ID_DELIM.length());
	    }

	    /**
	     * We got a response to a command.  Deal with it.
	     */

	    public void
	    reply(AmiStringMap map, String from) {
	        String queue = getId(map);
		if (queue != null) {
		    QueueTemplate.enqueue(queue, from, map, false, false);
		} else {
		    //  XXX need to check for authentication failed!
		    System.out.println("No Q name in ActionID! " + map);
		}
	    }

	    /**
	     * Cause this thread to die.  The interrupt
	     * will terminate the wait in dequeue
	     */

	    void
	    die(String why) {
		System.out.println("Die!!: " + why);
		alive=false;
		interrupt();
	    }
	}

	public void setDebug(boolean debug) {
	    this.debug = debug;
	}

	public void log(int level, String s) {
	    if (debug) {
	        server.log(level, prefix, s);
	    }
        }
    }

    /**
     * Class to manage the set of events.  This implementation
     * maintains a vector of eventItems.
     *
     * XXX We need to detect when the requester of an event goes away
     * XXX without unregistering the event, so we can remove it for them.
     */

    public static class Events {
	Vector events;

	public
	Events () {
	    events = new Vector();
	}

	/**
	 * Add an event to the current set of events.
	 * If the event expression already exists, add the queue name
	 * to the existing event, otherwise create a new event item.
	 */

	public void
	addEvent(String queue, String key, String exp, String context,
		String serverName) {
	    boolean added = false;
	    for(int i=0;i<events.size();i++) {
		EventItem item = (EventItem) events.elementAt(i);
		if (item.addQ2Event(queue, key, exp, context, serverName)) {
		    added = true;
		    // System.out.println("appended new event item\n");
		    break;
		}
	    }
	    if (!added) {
		events.addElement(new EventItem(queue, key, exp, context, serverName));
		// System.out.println("Added new event item\n");
	    }
	}

	/**
	 * Remove events.
	 * If a parameter is null, it matches everything.
	 * Warning: if all three parameters are null, then
	 * all events will be removed.
	 */

	public int
	removeEvents(String queue, String key, String exp) {
	    int removed = 0;
	    for(int i=0;i<events.size();i++) {
		EventItem item = (EventItem) events.elementAt(i);
		if (item.remQEvent(queue, key, exp)) {
		    removed++;
		    if (item.size() == 0) {
			events.removeElementAt(i);
			i--;
		    }
		}
	    }
	    return removed;
	}

	/**
	 * Send the event to all the proper Q's.
	 */

	public boolean
	processEvents(Dictionary event) {
	    boolean sent = false;
	    event = mapNames(event);
	    for(int i=0;i<events.size();i++) {
		EventItem item = (EventItem) events.elementAt(i);
		if (item.send2Q(event)) sent=true;
	    }
	    return sent;
	}

	/**
	 * Map event parameter names.
	 * (This probably doesn't belong here).
	 */

	static Dictionary mapNames(Dictionary src) {
	    Enumeration e = src.keys();
	    while (e.hasMoreElements()) {
		Object key = e.nextElement();
		Object replace = amiMap.get(key);
		if (replace != null) {
		    src.put(replace, src.get(key));
		    src.remove(key);
		    }
		}
	    return src;
	}

	Enumeration getEvents() {
	    return events.elements();
	}

	public String
	toString() {
	    StringBuffer sb = new StringBuffer("Events: ");
	    for(int i=0;i<events.size();i++) {
		EventItem item = (EventItem) events.elementAt(i);
		sb.append("\n  ").append(item);
	    }
	    return sb.toString();
	}
    }

    /**
     * This class is built on top of the StringMap class and
     * adds methods for reading Asterisk ManagerInterface replies.
     * <p>
     * AMI responses are either:
     * <li>
     * <ul>Mime-style headers, followed by a blank line, or
     * <ul>The header "Response: follows", followed by zero or more
     * additional headers, followed by one or more
     * lines of output, followed by the line:
     * <pre>--END COMMAND--</pre>
     * Unfortunately, the first line of the following response can have a
     * ":" in it, making it indistinguishable from another header [they
     * should'a added a blank line after the last header].  We need to use
     * some heuristics to figure out if it's a header or data. grumph!
     * <p>XXX to do:<br>
     * Any time data follows, the "ActionID" key (if present) will always
     * be the last key before the data starts. We could use that, or if
     * the data consists of what looks like headers, then just make them
     * headers, and don't stuff them into "data", which is sort-of what
     * happens now.
     * </ul>
     * In the second case, all the response data is put in a header called:
     * data:
     * <p>
     * This is modelled after MimeHeaders.
     */

    public static class AmiStringMap extends StringMap {
	public AmiStringMap() {}

	public AmiStringMap(StringMap map) {
	    append(map, true);
        }

	public void
	read(HttpInputStream in) throws IOException {
            boolean hasContent=false;  // do we have additional content?
	    StringBuffer sb = null;
	    while(true) {
		String line = in.readLine();
		// System.out.println("[" + line + "]");
		if (line == null) {
		    throw new IOException("Null read from Asterisk");
		}
		if (sb != null && line.trim().equals("--END COMMAND--")) {
		    in.readLine();	// strip trailing NL
		    break;
		} else if (sb != null) {
		    sb.append(line).append("\n");
		} else if (line.length() == 0) { // end of headers
		    break;
		} else { // name: value header
		    int index = line.indexOf(":");
		    String key;
		    if (index > 0 && 
			    (key = line.substring(0,index).trim()).indexOf(" ")
			    < 0) { // key/value pair
		        String value = line.substring(index+1).trim();
		        if (key.toLowerCase().equals("response") &&
				value.toLowerCase().equals("follows")) {
			    hasContent=true;
		        }
		        add(key, value);
			// actionid is the last header!
			if (hasContent&&key.toLowerCase().equals("actionid")) {
		            sb = new StringBuffer();
			}
		    } else if (hasContent && size() > 0) {
		        sb = new StringBuffer(line).append('\n');
		    } else if (size() > 0) { // continuation line? 
		        String value = get(size()-1);
		        put(size()-1, value + "\r\n\t" + line.trim());
		    }
	        }
	    }
	    if (sb != null) {
		put("data", sb.toString());
	    }
	}

        /**
         * Turn an AmiMap into an asterisk command.
         * Make sure the "Action" keyword exists and is first.
	 * @param: id		add the "id" at the end of the actionid
         * @return 		null if there wasn't a valid command
         */

        public String
        commandify(String id) {
	    return commandify(this, id);
	}

        public static String
        commandify(StringMap map, String id) {
	    String action = map.get("action");
	    if (action == null) {
	        return null;
	    }
	    String existing = "";	// existing action id
	    StringBuffer sb = new StringBuffer("Action: ");
	    sb.append(action).append("\r\n");
	    for (int i = 0; i < map.size(); i++) {
	        String key = map.getKey(i);
	        if (key.toLowerCase().equals("action")) {
		    continue;
	        }
	        if (id != null && key.toLowerCase().equals("actionid")) {
		    existing=map.get(i);
		    continue;
		}
	        sb.append(key).append(": ");
	        sb.append(map.get(i)).append("\r\n");
	    }
	    if (id != null) {
	        sb.append("ActionId: " + existing + ID_DELIM + id);
	        sb.append("\r\n");
	    }
	    sb.append("\r\n");
	    return sb.toString();
        }
    }

    // wait a random amount for the first keepalive
    static Random rand = new Random();

    /**
     * Thread to issue keep-alives to our asterisk connection.
     * Every "interval" seconds, it issues a "ping" and waits for
     * a "pong".  If it doesn't get a response, it causes the connection
     * to Asterisk to be restarted.
     */

    static class Keepalive extends Thread {
	boolean first = true;
	int interval;	// keep alive interval
	AMIReader reader; // thread reading events from Asterisk
	String to;	  // name of sending Q
	StringMap ping = new StringMap();

	Keepalive(AMIReader reader, int interval) {
	    this.reader = reader;
	    if (interval < 5) interval=5;
	    this.interval = interval * 1000;
	    to = reader.getQueueName();
	    setName("keepalive-" + to);
	    setDaemon(true);
	    ping.put("Action", "Ping");
	}

        public void
	run() {
	    reader.log(3,"Starting keepalive thread");
	    String from = to + "ping";
	    while(true) {
		try {
		    if (first) {
			Thread.sleep(1000 + Math.abs(rand.nextInt())%interval);
			first = false;
		    } else {
		        Thread.sleep(interval);
		    }
		} catch (Exception e) {
		    reader.log(3,"Keepalive interrupted: " + e);
		}
		QueueTemplate.enqueue(to, from, ping, false, false);
		QueueTemplate.QueueItem item=QueueTemplate.dequeue(from, 5);

		// timeout, try to restart the connection

		if (item == null) {
		    reader.log(2,reader.getHost() + ": keepalive failed!");
		    // reader.interrupt();
		    reader.close();
		} else {
		    reader.log(3,reader.getHost() + " is alive!");
		}
	    }
	}
    }
}
