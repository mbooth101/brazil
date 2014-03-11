/*
 * Main.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1998-2007 Sun Microsystems, Inc.
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
 * Contributor(s): cstevens, drach, suhler.
 *
 * Version:  2.6
 * Created by suhler on 98/09/14
 * Last modified by suhler on 07/03/07 09:15:31
 *
 * Version Histories:
 *
 * 2.6 07/03/07-09:15:31 (suhler)
 *   minor doc fix
 *
 * 2.5 06/01/18-13:13:27 (suhler)
 *   add more default mime types
 *
 * 2.4 05/07/13-10:19:46 (suhler)
 *   add "-x" and "-D" flags
 *
 * 2.3 04/11/30-15:19:41 (suhler)
 *   fixed sccs version string
 *
 * 2.2 04/08/30-09:02:48 (suhler)
 *   "enum" became a reserved word, change to "enumer".
 *
 * 2.1 02/10/01-16:34:51 (suhler)
 *   version change
 *
 * 1.30 02/07/24-10:47:48 (suhler)
 *   doc updates
 *
 * 1.29 02/04/19-15:01:59 (drach)
 *   Removed dependency on BServletServerSocket
 *
 * 1.28 02/04/02-17:50:25 (drach)
 *   Add hooks so BrazilServlet can share code
 *
 * 1.27 01/08/21-10:59:49 (suhler)
 *   add support for "maxPost"
 *
 * 1.26 01/06/05-22:12:43 (drach)
 *   Reduce access control for brazil.servlet package
 *
 * 1.25 01/05/30-08:33:46 (drach)
 *   Reduce access control on selected fields and methods.
 *
 * 1.24 01/01/16-17:10:48 (suhler)
 *   add "timeout" to alter default server.timeout
 *
 * 1.23 00/12/04-08:58:26 (suhler)
 *   added -S option to perform ${..} substitution on config values
 *
 * 1.22 00/07/10-15:11:58 (cstevens)
 *   Main.main() and ResourceHandler.getResourceStream() didn't work under
 *   Windows due to platform-dependant file names.  Class.getResourceAsStream()
 *   requires '/' as separator, while File.getParent() requires '\'.  Added code
 *   to turn '/' into '\' and vice versa as necessary.  Both methods now accept
 *   either '\' or '/' in file names and/or resource names.
 *
 * 1.21 00/05/17-10:38:35 (suhler)
 *   - added documentation for "root" property resolution
 *   - config files are seached for in the "jar" file if not found in the filesystem
 *   - main accepts a class name as as an alternate "Server" class
 *
 * 1.20 00/05/15-15:38:04 (cstevens)
 *   "root" property was being resolved w.r.t. the wrong config file.  The last
 *   specification of the "root" property is now resolved w.r.t. the config file
 *   that specified the "root", not the last config file seen (which might be in
 *   a different directory).
 *
 * 1.19 00/04/17-16:39:31 (cstevens)
 *   root is relative to config file, so can type "run -config foo/bar/config.foo"
 *   and the "root" specified in that config file is relative to "foo/bar".
 *
 * 1.18 00/04/12-15:59:19 (cstevens)
 *   unneeded getClass()
 *
 * 1.17 99/11/16-19:09:07 (cstevens)
 *   expunge Server.initHandler and Server.initObject.
 *
 * 1.16 99/11/09-20:23:50 (cstevens)
 *   bugs revealed by writing tests.
 *
 * 1.15 99/11/03-10:38:37 (suhler)
 *   added "interfaceHost" option to restrict server socket to a single interface
 *
 * 1.14 99/10/26-18:53:34 (cstevens)
 *   inits and default values got broken by last set of changes.
 *
 * 1.13 99/10/14-12:53:02 (cstevens)
 *   Main runs all the classes specified in the "init" line in the config file
 *   before starting the server.
 *
 * 1.12 99/09/29-16:10:36 (cstevens)
 *   Consistent way of initializing Handlers and other things that want to get
 *   attributes from the config file.  Convenience method that constructs the
 *   object, sets (via reflection) all the variables in the object that correspond
 *   to values specified in the config file, and then calls init() on the object.
 *
 * 1.11 99/07/30-10:53:13 (suhler)
 *   added "listenQueue" property to set the initial socket
 *   listen Q.  defaults to 1024.
 *
 * 1.10 99/07/22-15:03:09 (suhler)
 *   add maxThreads option to set the mac # of allowable threads
 *
 * 1.9 99/07/09-10:05:57 (suhler)
 *   config file searched for in jar file first
 *
 * 1.8 99/03/30-09:25:11 (suhler)
 *   documentation updates
 *
 * 1.7 99/02/10-10:31:33 (suhler)
 *   fixed bug in previous version:
 *   - rename "prefix" to "defaultPrefix"
 *
 * 1.6 99/02/03-14:12:11 (suhler)
 *   - better error checking (still need more)
 *   - can start multiple servers at once (-start option)
 *   - can set initial property prefix (set "prefix" on the command line)
 *
 * 1.5 98/12/09-15:03:57 (suhler)
 *   added message for port in use
 *
 * 1.4 98/11/17-09:58:26 (suhler)
 *   Added "maxRequests" and "noKeepAlives" parameters to server.
 *
 * 1.3 98/09/22-16:58:17 (suhler)
 *   fixed arg parsing to handle extra properties on cmd line
 *
 * 1.2 98/09/21-14:51:22 (suhler)
 *   changed the package names
 *
 * 1.2 98/09/14-18:03:08 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 server/Main.java
 *   Name history : 1 0 Main.java
 *
 * 1.1 98/09/14-18:03:07 (suhler)
 *   date and time created 98/09/14 18:03:07 by suhler
 *
 */

package sunlabs.brazil.server;

/**
 * Start an HTTP/1.1 server.  The port number and handler class
 * are provided as arguments.
 *
 * @author	Stephen Uhler
 * @author	Colin Stevens
 * @version		2.6
 */

import sunlabs.brazil.util.Format;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.BindException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.StringTokenizer;
import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;

/**
 * Sample <b>main</b> program for starting an http server.
 *
 * A new thread is started for each
 * {@link Server},
 * listening on a socket for HTTP connections.
 * As each connection is accepted, a
 * {@link Request} object is constructed,
 * and the registered
 * {@link Handler} is called.
 * The configuration properties required by the server
 * and the handler (or handlers),  are gathered
 * from command line arguments and configuration files specified on the
 * command line. 
 * <p>
 * The command line arguments are processed in order from left to
 * right, with the results being accumulated in a properties object.
 * The server is then started with {@link Server#props} set to the
 * final value of the properties.
 * Some of the properties are interpreted directly by the server, 
 * such as the port to listen on, or the handler to use
 * (see {@link Server} for the complete list).  The rest
 * are arbitrary name/value pairs that may be used by the handler.
 * <p>
 * Although any of the options may be specified as name/value pairs, 
 * some of them: the ones interpreted by the server, the default
 * handler ({@link FileHandler}, or {@link Main}, 
 * may be prefixed with a "-".
 * Those options are explained below:
 * <dl>
 * <dt>  -p(ort)    <dd>The network port number to run the server on (defaults to 8080)
 * <dt>  -r(oot)    <dd>The document root directory, used by the FileHandler (defaults to .)
 * <dt>  -h(andler) <dd>The document handler class
 *			   (defaults to {@link FileHandler sunlabs.brazil.handler.FileHandler})
 * <dt>  -c(onfig)  <dd>A java properties file to add to the current properties.
 *			There may be several <i>-config</i> options.  Each
 *			file is added to the current properties.
 *			If the properties file contains a <code>root</code>
 *			property, it is treated specially.  See below.
 *			If the config file is not found in the filesystem, 
 *			it is read from the jar file, with this class as the
 *			virtual current directory if a relative path
 *			is provided.
 * <dt>  -i(p)      <dd>A space seperated list of hosts allowed to access this server
 *			If none are supplied, any host may connect.  The ip addresses
 *			are resolved once, at startup time.
 * <dt>  -l(og)     <dd>The log level (0->none, 5->max)
 *			Causes diagnostic output on the standard output.
 * <dt>  -s(tart)   <dd>Start a server.
 *			Allows multiple servers to be started at once.
 *			As soon as a <i>-s</i> is processed, as server is
 *			started as if all the options had been processed,
 *			then the current properties are cleared.
 *			Any options that follow are used for the next server.
 * <dt>  -D(elay) n  <dd>delay "n" seconds. This is useful in conjuction
 *			with "-s" to allow the previous server to initialize
 *			before proceeding.
 * <dt>  -S(ubstitute)  <dd>Perform ${..} substitutions on the current
 *			values.
 * <dt>  -x		<dd>Don't read the default resource config (only if
 *			first).
 * </dl>
 * <p>
 * Following these options, any additional additional pairs of 
 * names and values (no "-"'s allowed) are placed directly in
 * {@link Server#props}.
 * <p>
 * If the resource "/sunlabs/brazil/server/config" is found, it is used
 * to initialize the configuration.
 * <p>
 * If a non absolute <code>root</code> property is specified in a
 * <i>configuration</i> file, it is modified to resolve relative to the
 * directory containing the <i>configuration</i> file, and not the directory
 * in which the server was started.  If multiple <i>configuration</i> files
 * with root properties (or <code>-r</code> options, or "root" properties)
 * are specified, the last one tekes precedence.
 * <p>
 * The "serverClass" property may be set to the string to use as the server's
 * class, instead of "sunlabs.brazil.server.Server"
 */

public class Main {
    static final String CONFIG = "/sunlabs/brazil/server/config";

    public static void
    main(String[] args)
	throws Exception
    {
	boolean started = false;
	Properties config = new Properties();
	initProps(config);

	/*
	 * Try to initialize the server from a resource in the
	 * jar file, if available (unless first arg is "-x").
	 */

	if (args.length==0 || !args[0].equals("-x")) {
	    InputStream in = Main.class.getResourceAsStream(CONFIG);
	    if (in != null) {
	       config.load(in);
	       System.out.println("Found default config file");
	       in.close();
	    }
	}

	int i=0;
	try {
	    String rootBase = null;
	    String root = null;

	    for (i = 0; i < args.length; i++) {
	    	started = false;
		if (args[i].startsWith("-he")) {
		    throw new Exception();  // go to usage.
		} else if (args[i].startsWith("-ha")) {
		    config.put("handler",args[++i]);
		} else if (args[i].startsWith("-r")) {
		    root = args[++i];
		    rootBase = null;
		    config.put(FileHandler.ROOT, root);
		} else if (args[i].startsWith("-ho")) {
		    config.put("host",args[++i]);
		} else if (args[i].startsWith("-de")) {
		    config.put("default",args[++i]);
		} else if (args[i].startsWith("-ip")) {
		    config.put("restrict",config.getProperty("restrict","") + " " +
			    InetAddress.getByName(args[++i]));
		} else if (args[i].startsWith("-c")) {
		    String oldRoot = config.getProperty(FileHandler.ROOT);
		    File f = new File(args[++i]);

		    /*
		     * Look for config file in filesystem.  If found, slurp
		     * it in, and adjust the "root" if needed".  Otherwise, 
		     * look for it in the jar file (and leave the root alone).
		     */

		    if (f.canRead()) {
			try {
			    FileInputStream in = new FileInputStream(f);
			    config.load(in);
			    in.close();
			} catch (Exception e) {
			    System.out.println("Warning: " + e);
			    continue;
			}
			String newRoot = config.getProperty(FileHandler.ROOT);
			if (newRoot != oldRoot) {
			    root = newRoot;
			    rootBase = f.getPath();
			}
		    } else {
			InputStream in =Main.class.getResourceAsStream(args[i]);
			if (in != null) {
			   config.load(in);
			   rootBase = null;
			   in.close();
			}
		    }
		} else if (args[i].startsWith("-p")) {
		    config.put("port",args[++i]);
		} else if (args[i].startsWith("-D")) {
		    try {
			System.out.println("Pausing...");
		        Thread.sleep(Integer.decode(args[++i]).intValue()*1000);
		    } catch (Exception e) {}
		} else if (args[i].startsWith("-S")) {
		    Enumeration enumer = config.propertyNames();
		    while(enumer.hasMoreElements()) {
			String key = (String) enumer.nextElement();
			config.put(key, 
				Format.subst(config, config.getProperty(key)));
		    }
		} else if (args[i].startsWith("-l")) {
		    config.put("log",args[++i]);
		} else if (args[i].startsWith("-s")) {
		    if (startServer(config)) {
			System.out.println("Server started on " +
				config.getProperty("port", "8080"));
		    }

		    // make sure servers do not share the same properties

		    config = new Properties();
		    initProps(config);
		    started = true;
		} else if (args[i].equals(FileHandler.ROOT)) {
		    root = args[++i];
		    rootBase = null;
		    config.put(FileHandler.ROOT, root);
		} else if (!args[i].startsWith("-")) {
		    config.put(args[i],args[++i]);
		} else if (args[i].startsWith("-x")) {
		    // ignore
		} else {
		    System.out.println("Invalid flag : " + args[i]);
		    throw new Exception(); // go to usage.
		}
	    }

	    /*
	     * The last thing that specified a root wins.
	     *
     	     * If the root was last specified on the command line, use that
	     * as the base root of the system.
	     *
	     * If the last thing that specified a root was a config file,
	     * then the root in the config file must be combined with the
	     * basename of that config file to produce the real root.
	     *
	     * If the root wasn't specified at all by anything, then that
	     * is equivalent to leaving the root as the current dir.
	     */

	    if ((rootBase != null) && (new File(root).isAbsolute() == false)) {
                rootBase = rootBase.replace('/', File.separatorChar);
                rootBase = new File(rootBase).getParent();
                if (rootBase != null) {
		    config.put(FileHandler.ROOT,
                            rootBase + File.separator + root);
		}
	    }
	} catch (ArrayIndexOutOfBoundsException e) {
	    System.out.println("Missing argument after: " + args[i-1]);
	    return;
	} catch (Exception e) {
	    e.printStackTrace();
	    System.out.println("Usage: Main -conf <file> -port <port> " +
		    "-handler <class name> -root <doc_root> -ip <host> " +
		    "<name value>...");
	    return;
	}

	if (!started && startServer(config)) {
	    System.out.println("Server started on " +
		    config.getProperty("port", "8080"));
	}
    }

    /**
     * Start a server using the supplied properties.  The following
     * entries are treated.  Specially:
     * <dl>
     * <dt> handler
     * <dd> The name of the handler class (defaults to file handler)
     * <dt> host
     * <dd> The host name for this server
     * <dt> log
     * <dd> Diagnostic output level 0-5 (5=most output)
     * <dt> maxRequests
     * <dd> max number of requests for a single socket (default 25)
     *	    when using persistent connections.
     * <dt> listenQueue
     * <dd> max size of the OS's listen queue for server sockets
     * <dt> maxPost
     * <dd> max size of a content-length for a Post or Put in bytes.
     *	    (defaults to 2Meg).  The absolute limit is 2GB.
     * <dt> maxThreads
     * <dd> max number of threads allowed (defaults to 250)
     * <dt> port
     * <dd> Server port (default 8080)
     * <dt> defaultPrefix
     * <dd> prefix into the properties file, normally the empty string "".
     * <dt> restrict
     * <dd> list of hosts allowed to connect (defaults to no restriction)
     * <dt> timeout
     * <dd> The maximum time to wait for a client to send a complete request.
     *	    Defaults to 30 seconds.
     * <dt> interfaceHost
     * <dd> If specified, a host name that represents the network to server.
     *	    This is for hosts with multiple ip addresses.  If no network
     *	    host is specified, then connections for all interfaces are
     *	    accepted
     * </dl>
     * @param config	The configuration properties for the server
     */

    public static boolean
    startServer(Properties config)
    {
	String handler = FileHandler.class.getName();
	int port = 8080;
	int queue = 1024;
	
	// is the method invoked from a servlet?
	boolean servlet = config.getProperty("servlet_name") != null;

	handler = config.getProperty("handler", handler);
	try {
	    String str = config.getProperty("port");
	    port = Integer.decode(str).intValue();
	} catch (Exception e) {}
	try {
	    String str = config.getProperty("listenQueue");
	    queue = Integer.decode(str).intValue();
	} catch (Exception e) {}

	Server server = null;
	String interfaceHost =  config.getProperty("interfaceHost");
	String serverClass = config.getProperty("serverClass");
	String errMsg = null;
	try {
	    ServerSocket listen;
	    if (servlet) {
		listen = (ServerSocket)Class.forName(
                                  "sunlabs.brazil.servlet.BServletServerSocket"
                                  ).newInstance();
	    } else if (interfaceHost != null) {
		listen = new ServerSocket(port, queue,
			InetAddress.getByName(interfaceHost));
	    } else {
		System.out.println("Creating server socket on port: " + port);
		listen = new ServerSocket(port, queue);
	    }
	    if (serverClass != null) {
		server = (Server) Class.forName(serverClass).newInstance();
		server.setup(listen, handler, config);
	    } else {
		server = new Server(listen, handler, config);
	    }
	} catch (ClassNotFoundException e) {
	    errMsg = serverClass + " not found for class Server";
	} catch (IllegalAccessException e) {
	    errMsg = serverClass + " not available";
	} catch (InstantiationException e) {
	    errMsg = serverClass + " not instantiatable";
	} catch (ClassCastException e) {
	    errMsg = serverClass + " is not a sub-class of server";
	} catch (UnknownHostException e) {
	    errMsg = interfaceHost +
		    " doesn't represent a known interface";
	} catch (BindException e) {
	    errMsg = "Port " + port + " is already in use: " + e;
	} catch (IOException e) {
	    errMsg = "Unable to start server on port " + port +
	    	" :" + e;
	}

	if (errMsg != null) {
	    if (servlet) {
		config.put("_errMsg", errMsg);
	    } else {
		System.out.println(errMsg);
	    }
	    return false;
	}

	server.hostName = config.getProperty("host", server.hostName);
	server.prefix = config.getProperty("defaultPrefix", server.prefix);

	try {
	    String str = config.getProperty("maxRequests");
	    server.maxRequests = Integer.decode(str).intValue();
	} catch (Exception e) {}

	try {
	    String str = config.getProperty("maxThreads");
	    server.maxThreads = Integer.decode(str).intValue();
	} catch (Exception e) {}

	try {
	    String str = config.getProperty("maxPost");
	    server.maxPost = Integer.decode(str).intValue();
	} catch (Exception e) {}

	try {
	    String str = config.getProperty("timeout");
	    server.timeout = Integer.decode(str).intValue() * 1000;
	} catch (Exception e) {}

	/*
	 * Turn off keep alives entirely
	 */
	if (config.containsKey("noKeepAlives")) {
	    server.maxRequests = 0;
	}
	try {
	    String str = config.getProperty("log");
	    server.logLevel = Integer.decode(str).intValue();
	} catch (Exception e) {}

	
	{
	    Vector restrict = new Vector();
	    String str = config.getProperty("restrict", "");
	    StringTokenizer st = new StringTokenizer(str);
	    while (st.hasMoreTokens()) {
		try {
		    InetAddress addr = InetAddress.getByName(st.nextToken());
		    restrict.addElement(addr);
		} catch (Exception e) {}
	    }
	    if (restrict.size() > 0) {
		server.restrict = new InetAddress[restrict.size()];
		restrict.copyInto(server.restrict);
	    }
	}

	{
	    String str = config.getProperty("init", "");
	    StringTokenizer st = new StringTokenizer(str);
	    while (st.hasMoreTokens()) {
		String init = st.nextToken();
		server.log(Server.LOG_DIAGNOSTIC, "initializing", init);

		Object obj = initObject(server, init);

		if (obj == null) {
		    server.log(Server.LOG_DIAGNOSTIC, init,
			    "didn't initialize");
		}
	    }
	}

	if (servlet) {
	    config.put("_server", server);
	} else {
	    server.start();
	}
	return true;
    }

    public static Object
    initObject(Server server, String name)
    {
	String className = server.props.getProperty(name + ".class");
	if (className == null) {
	    className = name;
	} 
	String prefix = name + ".";

	Object obj = null;
	try {
	    Class type = Class.forName(className);
	    obj = type.newInstance();

	    Class[] types = new Class[] {Server.class, String.class};
	    Object[] args = new Object[] {server, prefix};

	    Object result = type.getMethod("init", types).invoke(obj, args);
	    if (Boolean.FALSE.equals(result)) {
		return null;
	    }
	    return obj;
	} catch (ClassNotFoundException e) {
	    server.log(Server.LOG_WARNING, className, "no such class");
	} catch (IllegalArgumentException e) {
	    server.log(Server.LOG_WARNING, className, "no such class");
	} catch (NoSuchMethodException e) {
	    return obj;
	} catch (Exception e) {
	    server.log(Server.LOG_WARNING, name, "error initializing");
	    e.printStackTrace();
	}
	return null;
    }

    /**
     * Initialize a properties file with some standard mime types
     * The {@link FileHandler} only delivers files whose suffixes
     * are known to map to mime types.  The server is started with 
     * the suffixes: .html, .txt, .gif, .jpg, pdf, .png, .css, .class, and .jar
     * predefined.  If additional types are required, they should be supplied as
     * command line arguments. 
     */

    public static void
    initProps(Properties config) {
	config.put("mime.html",  "text/html");
	config.put("mime.txt",   "text/plain");
	config.put("mime.css",   "text/css");
	config.put("mime.gif",   "image/gif");
	config.put("mime.jpg",   "image/jpeg");
	config.put("mime.png",   "image/png");
	config.put("mime.pdf",   "application/pdf");
	config.put("mime.class", "application/octet-stream");
	config.put("mime.jar",   "application/octet-stream");
	config.put("mime.jib",   "application/octet-stream");
    }
}
