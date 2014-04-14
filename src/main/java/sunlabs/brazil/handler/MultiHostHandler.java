/*
 * MultiHostHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2007 Sun Microsystems, Inc.
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
 * Version:  2.6
 * Created by cstevens on 99/11/03
 * Last modified by suhler on 07/01/29 15:35:18
 *
 * Version Histories:
 *
 * 2.6 07/01/29-15:35:18 (suhler)
 *   remove unused import
 *
 * 2.5 06/01/06-13:53:33 (suhler)
 *   - remove the "inherit" stuff, it wasn't correct.
 *   - add a "config" option to allow each virtual server to have it's
 *   own configuration file
 *   - integrate properly with PropertyLists, so each request gets (mostly) the
 *   right properties.
 *   - Factor the code so sub-classes can do wierd stuff with servers.
 *
 * 2.4 05/12/22-16:42:35 (suhler)
 *   doc fixes
 *
 * 2.3 05/12/22-16:17:19 (suhler)
 *   add "inherit" flag to allow each virtual host to have its own server properties
 *
 * 2.2 05/12/21-10:25:38 (suhler)
 *   - bug fix: port#'s were being included in host matching (not any more)
 *   - better diagnostics
 *
 * 2.1 02/10/01-16:36:36 (suhler)
 *   version change
 *
 * 1.7.1.1 02/04/18-11:13:41 (suhler)
 *   revert to previous behavior - maybe some other day
 *
 * 1.8 02/02/18-22:34:36 (suhler)
 *   allow virtual hosts to be glob patterns.  Although this implementation
 *   switches from O(0) to O(n) for finding the matching host, the number of
 *   hosts is expected to be small (a score or less).  If this proves not to
 *   be the case, then we'll re-examine this.
 *
 * 1.7 02/02/14-12:36:06 (suhler)
 *   bug introduced by last server.java change
 *
 * 1.6 01/06/13-09:29:02 (suhler)
 *   package move
 *
 * 1.5 00/12/11-13:28:26 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.4 00/05/24-11:26:06 (suhler)
 *   add docs
 *
 * 1.3 99/11/16-19:07:53 (cstevens)
 *   wildcard imports
 *
 * 1.2 99/11/09-20:23:18 (cstevens)
 *   bugs revealed by writing tests.
 *
 * 1.2 99/11/03-17:51:52 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/MultiHostHandler.java
 *
 * 1.1 99/11/03-17:51:51 (cstevens)
 *   date and time created 99/11/03 17:51:51 by cstevens
 *
 */

package sunlabs.brazil.handler;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Glob;
import sunlabs.brazil.properties.PropertiesList;
import java.util.Vector;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * The <code>MultiHostHandler</code> allows the user to handle a set
 * of host names that are all running on the same IP address.  This
 * handler looks at the http "Host" header and redispatches the request to the
 * appropriate sub-server.
 * <p>
 * Only the main server is actually listening to the port on the specified IP
 * address.  The sub-servers are not running in separate threads.  Indeed,
 * they are not "running" at all.  They exist merely as a convenient bag to
 * hold each of the server-specific configuration parameters.
 * <p>
 * The <code>respond</code> method of the main handler for the appropriate
 * sub-server is called directly from the <code>respond</code> method of
 * this handler.  
 * <p>
 * This handler uses the following configuration parameters: <dl class=props>
 *
 * <dt> servers
 * <dd> The list of prefixes for the other servers.  Each server will be
 *	initialized from the main <code>server.props</code> with the
 *	specified prefix.  In this way, the configuration parameters for
 *	all the sub-servers can be stored in the same <code>Properties</code>
 *	object.
 *
 * <dt> <i>prefix</i>.host
 * <dd> Each server is started with a given <i>prefix</i>.  The property
 *	<i>prefix</i>.host specifies a Glob pattern for a virtual hostname
 *      the server
 *	will be expected to handle.  If this property is not specified,
 *	the server's virtual hostname will just be <i>prefix</i>.
 *	If multiple host patterns could match a given "Host" header, 
 *	the first match in the "servers" list matches first.
 *
 * <dt> <i>prefix</i>.handler
 * <dd> The main handler for the server with the given <i>prefix</i>.  If
 *	this property is not specified, it defaults to the
 *	<code>FileHandler</code>.
 *
 * <dt> <i>prefix</i>.config
 * <dd> Read in the file specified by "config" to initialize this sub-server's
 *	server properties. The file is expected to be in java properties
 *	format.  If not specified, this sub-server shares a copy
 *	of the main server's properties, otherwise, the main server's properties
 *	are used as the "default".  If this property is specified and no
 *	config file is found, then the sub-server isn't started.
 *	<p>
 *	The property "root", if included in the "config" file, is treated
 *	specially: If it does not represent an absolute path, then it is
 *	resolved relative to the main server's root.
 * </dl>
 *
 * <dt> <i>prefix</i>.log
 * <dd> The log level for the server with the given <i>prefix</i>.  If this
 *	property is not specified, it defaults to the log level of the
 *	parent server.
 * </dl>
 *
 * A sample set of configuration parameters illustrating how to use this
 * handler follows:
 * <pre>
 * handler=host
 * port=8081
 * log=5
 * 
 * host.class=sunlabs.brazil.server.MultiHostHandler
 * host.servers=mars jupiter saturn
 * 
 * mars.host=www.mars.com
 * mars.log=2
 * mars.handler=mars.file
 * mars.file.class=sunlabs.brazil.server.FileHandler
 * mars.file.root=public_html/mars
 * 
 * jupiter.host=jupiter.planet.org
 * jupiter.handler=sunlabs.brazil.server.FileHandler
 * jupiter.root=public_html/jupiter
 * 
 * saturn.host=*.saturn.planet.org
 * saturn.handler=sunlabs.brazil.server.FileHandler
 * saturn.root=public_html/saturn
 * </pre>
 * These parameters set up a normal <code>Server</code> on port 8081,
 * running a <code>MultiHostHandler</code>.  The <code>MultiHostHandler</code>
 * will create three additional servers that respond to the virtual hosts
 * "www.mars.com", "jupiter.planet.org", and "<anything>.saturn.planet.org". The
 * "mars" server will have a <code>Server.prefix</code> of "mars",
 * so that all other configuration parameters that the "mars" server
 * examines can begin with "mars" and be kept distinct from the "jupiter"
 * and "saturn" parameters.
 * <p>
 * The main server and the three sub-servers will all share the
 * same properties object, but can use their own individual prefixes to
 * keep their data separate (because "inherit" is not set).
 * <p>
 *
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version	2.6, 07/01/29
 */
public class MultiHostHandler implements Handler {
    String prefix;
    private Vector servers = new Vector();

    /**
     * Initializes the servers for the virtual hosts.  After creating
     * and initializing each sub-server, the <code>init</code> method of the
     * main handler for each sub-server is called.
     *
     * @param	server
     *		The HTTP server that created this handler.
     *
     * @param	prefix
     *		A prefix to prepend to all of the keys that this
     *		handler uses to extract configuration information.
     *
     * @return	<code>true</code> if at least one sub-server was found and
     *		could be initialized, <code>false</code> otherwise.
     *		Diagnostic messages are logged for each sub-server started.
     */
    public boolean
    init(Server server, String prefix) {
	this.prefix = prefix;
	Properties props = server.props;
	
	String list = props.getProperty(prefix + "servers", "");
	StringTokenizer st = new StringTokenizer(list);
	while (st.hasMoreTokens()) {
	    String name = st.nextToken();
	    server.log(Server.LOG_DIAGNOSTIC, prefix,
		    "creating subserver " + name);

            Server sub = subServer(server, name);

	    if (sub!=null && sub.init()) {
	        servers.addElement(new Host(sub.hostName, sub));
	    }
	}
	return (servers.size() > 0);
    }

    /*
     * Create a sub-server.
     * @param main: The main server
     * @param name: name of the sub-server
     */

    Server subServer(Server main, String name) {
	Properties props = main.props;
	String handler = props.getProperty(name + ".handler",
		FileHandler.class.getName());

	String config = props.getProperty(name + ".config");
	if (config != null) {
	    try {
		InputStream in = ResourceHandler.getResourceStream(props,
			"", config);
		props = new Properties(props);
		props.load(in);
		in.close();
		main.log(Server.LOG_DIAGNOSTIC, name,
			"Fetching config file: " + config + " for " + name);
	    } catch (IOException e) {
		main.log(Server.LOG_WARNING, name,
			"Can't find config (" + config + ") for (" + name + ")");
		return null;
	    }

	    // fix up the root

	    String mainroot = main.props.getProperty("root",".");
	    String root = (String)props.get("root");
	    if (root != null) {
		File froot = new File(root);
		if (!froot.isAbsolute()) {
		    froot = new File(mainroot, root);
		}
	        props.put("root", froot.getPath());
	        main.log(Server.LOG_DIAGNOSTIC, name,
			    "Setting root for: " + name + " to " +
			    froot.getPath());
	    }
	}

	Server sub = new Server(main.listen, handler, props);
	sub.hostName = props.getProperty(name + ".host", name);
	sub.prefix = name;
	if (!name.endsWith(".")) {
            sub.prefix += ".";
        }
	try {
	    String str = props.getProperty(name + ".log");
	    sub.logLevel = Integer.decode(str).intValue();
	} catch (Exception e) {
	    sub.logLevel = main.logLevel;
	}
	sub.maxRequests = main.maxRequests;
	sub.timeout = main.timeout;
	return sub;
    }

    /**
     * Responds to an HTTP request by examining the "Host:" request header
     * and dispatching to the main handler of the server that handles
     * that virtual host.  If the "Host:" request header was not specified,
     * or named a virtual host that was not initialized in <code>init</code>
     * from the list of virtual hosts, this method returns without
     * handling the request. Port numbers are not used for host matching.
     *
     * @param	request
     *		The HTTP request to be forwarded to one of the sub-servers.
     *
     * @return	<code>true</code> if the sub-server handled the message,
     *		<code>false</code> if it did not.  <code>false</code> is
     *		also returned if the "Host:" was unspecified or unknown.
     */

    public boolean
    respond(Request request) throws IOException {
	String host = request.getRequestHeader("Host");
	Server orig = request.server;
	if (host == null) {
	    return false;
	}
	int idx = host.indexOf(":");
	if (idx > 0) {
	    host = host.substring(0, idx);
	}
	for(int i=0;i<servers.size();i++) {
	    Host h = (Host)servers.elementAt(i);
	    Server server=h.getServer(host);

	    /*
	     * We neet to swap in the proper sub-server.  This is a bit
	     * dicy as there is really only one server, yet each "virtual
	     * host" wants the illusion of their own server.  Some of the
	     * server properties are shared, some aren't.  User beware.
	     */

	    if (server != null) {
		Server newServer = doServer(request, server);
		if (newServer != null && newServer != server) {
		    h.setServer(newServer);
		    request.log(Server.LOG_DIAGNOSTIC, prefix,
			"replacing server object " + newServer + 
			" with " + server);
		    server = newServer;
		}
		server.requestCount++;
	        request.server = server;
	        PropertiesList pl = new PropertiesList(server.props);
		request.props.addBefore(pl);
		request.log(Server.LOG_DIAGNOSTIC, prefix,
			"Dispatching host=" + host + " to subserver " + h);
	        boolean result = server.handler.respond(request);
		request.server = orig;
	        return result;
	    }
	}
	request.log(Server.LOG_DIAGNOSTIC, prefix,
		    host + ": no matching sub-server");
	return false;
    }

    /**
      * Experiment to allow sub-servers to be restarted.
      * Return the server, unless it's restarted, in which case
      * you get a new one.  This reqires:
      * - the "config" option ....
      */

    Server doServer(Request request, Server server) {
        return server;
    }

    /**
     * Structure to keep track of servers and their glob patterns.
     */

    static class Host {
	Server server;		// the host name
	String pattern;		// the glob pattern
    
	Host(String pattern, Server server) {
	    this.server = server;
	    this.pattern = pattern;
	}

	Server
	getServer(String host) {
	    if (Glob.match(pattern, host)) {
		return server;
	    } else {
		return null;
	    }
	}

	void
        setServer(Server server) {
	    this.server=server;
        }

        public String toString() {
	    return pattern + "=" + server;
        }
    }
}
