/*
 * BrazilServlet.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2004 Sun Microsystems, Inc.
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
 * Version:  2.7
 * Created by drach on 01/06/01
 * Last modified by suhler on 04/11/30 15:19:42
 *
 * Version Histories:
 *
 * 2.7 04/11/30-15:19:42 (suhler)
 *   fixed sccs version string
 *
 * 2.6 04/04/28-14:51:48 (suhler)
 *   doc format fixes
 *
 * 2.5 03/07/23-16:00:37 (suhler)
 *   stupidity
 *
 * 2.4 03/07/23-13:12:35 (suhler)
 *   add "servlet_container property"
 *   don't strip off servlet context, the "url" has it removed instead
 *
 * 2.3 03/07/17-10:43:55 (suhler)
 *   Changed the way the servlet finds its config file and document root, by
 *   viewing the "web.xml" file as a replacement for ...server.Main [args] and
 *   modelling it the same way.
 *   Always use the servlet context root to resolve "cwd()" as the directory
 *   of the script that started the web container is almost never useful
 *
 * 2.2 03/05/29-16:46:37 (drach)
 *   Set the socket to the servers listener socket.
 *
 * 2.1 02/10/01-16:39:16 (suhler)
 *   version change
 *
 * 1.7 02/04/02-17:51:18 (drach)
 *   Remove duplicate code and use a method in Main
 *
 * 1.6 02/02/20-16:35:17 (suhler)
 *   reflact change in Server.java
 *
 * 1.5 01/07/13-16:41:50 (drach)
 *   Fix spelling error in documentation.
 *
 * 1.4 01/06/11-17:06:37 (suhler)
 *   fix javadoc errors
 *
 * 1.3 01/06/05-22:09:54 (drach)
 *   package move
 *
 * 1.2 01/06/04-14:41:59 (suhler)
 *   package move
 *
 * 1.2 01/06/01-16:31:03 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 servlet/BrazilServlet.java
 *
 * 1.1 01/06/01-16:31:02 (drach)
 *   date and time created 01/06/01 16:31:02 by drach
 *
 */

package sunlabs.brazil.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Main;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

/**
 *
 * This is the Brazil Toolkit Servlet Adapter.  It allows one to run
 * applications built with the Brazil Toolkit in any web server that
 * provides a <code>Servlet</code> container that implements the Servlet
 * 2.2 API.
 * <p>
 * The servlet should be installed as a web application as specified in
 * the Java Servlet Specification, v2.2.  The servlet deployment
 * descriptor should create a configuration that causes the request path
 * to consist of only the context path and pathInfo elements.  There
 * should not be a servlet path element.  See section 5 of the
 * specification for more details (note that the definitions of each path
 * element are are more accurately stated in the 2.3 specification).  The
 * deployment descriptor should be similar to the following:
 * 
 * <code><pre>
 * &lt;web-app&gt;
 *     &lt;display-name&gt;BrazilServlet servlet container for Brazil&lt;/display-name&gt;
 *     &lt;description&gt;
 * 	This servlet wraps the Brazil Toolkit so it can run within
 * 	any web server environment that supports the 2.2 Servlet API
 *     &lt;/description&gt;
 * 
 *     &lt;servlet&gt;
 *         &lt;servlet-name&gt;BrazilServlet&lt;/servlet-name&gt;
 *         &lt;servlet-class&gt;sunlabs.brazil.servlet.BrazilServlet&lt;/servlet-class&gt;
 *         &lt;init-param&gt;
 *             &lt;param-name&gt;config&lt;/param-name&gt;
 *             &lt;param-value&gt;configA|configB&lt;/param-value&gt;
 *         &lt;/init-param&gt;
 *     &lt;/servlet&gt;
 * 
 *     &lt;servlet-mapping&gt;
 *         &lt;servlet-name&gt;BrazilServlet&lt;/servlet-name&gt;
 *         &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *     &lt;/servlet-mapping&gt;
 * &lt;/web-app&gt;
 * </pre></code>
 * 
 * The default configuration file for the Brazil application is loaded
 * from the system resource <code>/sunlabs/brazil/server/config</code>.
 * Additional configuration files, which may override properties in the
 * default file, are searched by looking at the "|" separated list of
 * file names associated with <code>config</code> servlet parameter.
 * File names that are not "absolute" are located relative to the
 * servlet context root  If no <code>config</code> servlet parameter
 * is defined, the file named "config" (if any) in the servlet context
 * root is used.
 * <p>
 * The Brazil <code>root</code> property if obtained from the "root"
 * servlet parameter.  If none is provided, the "root" property from
 * the configuration files (last one wins) is used.  As before, relative
 * root directories are resolved with respect to the servlet context
 * root.
 * <p>
 * The following properties are set by this class:
 * <dl class=props>
 * <dt><code>url.servlet</code></dt>
 * <dd>The original URL requested by the client.
 * <dt><code>context_path</code></dt>
 * <dd>The servlet context path</dd>
 * <dt><code>servlet_name</code></dt>
 * <dd>The servlet's name</dd>
 * <dt><code>servlet_container</code></dt>
 * <dd>The name of the servlet container</dd>
 * </dl>
 * 
 * @author	Steve Drach &lt;drach@sun.com&gt;
 * @version		2.7
 *
 * @see         BServletSocket
 * @see         BServletServerSocket
 * @see         BServletRequest
 */
public final class BrazilServlet extends GenericServlet {

    private static final String CONFIG = "/sunlabs/brazil/server/config";

    private ServerSocket socket;
    private String handler;
    private Properties props;
    private Server server;
    private boolean initok;

    /**
     *
     * Called by the servlet container to place the servlet into
     * service.
     *
     * @param config 			the <code>ServletConfig</code> object
     *					that contains configuration
     *					information for this servlet
     *
     * @exception ServletException 	if an exception occurs that
     *					interrupts the servlet's normal
     *					operation
     *
     * @see javax.servlet.Servlet#init
     */
    public void init(ServletConfig config) throws ServletException {
	super.init(config);

	/*
	 * Locate the config file by:
	 * 1) looking in the jar file as a resource 
	 * 2) looking in web.xml for "config", containing
	 *    a list config files, separated
	 *    by "|".  If the files don't start with "/", they are resolved
	 *    relative to the servlet context root.  Files that don't exist
	 *    are ignored.
	 * 3) Look for "config" in the servlet context root, but only
	 *    if no "config" property is found in step 2.
	 *
	 * Think of the "web.xml" file as a replacement for "Main" and
	 * its arguments in the standalone case.  "Main" is used primarily
	 * for specifying the config file, root directory, and port.
	 */

	props = new Properties();
	Main.initProps(props);
	props.put("servlet_name", config.getServletName());

	if (loadConfigFromResource(props, CONFIG)) {
	    log("Found default config file");
	}

	String configList = getInitParameter("config");
	if (configList == null) {
	    configList = "config";
	    System.err.println("Can't find 'brazil.config' in web.xml");
	}
	StringTokenizer st = new StringTokenizer(configList, "|");
	String root = getServletConfig().getServletContext().
		getRealPath("/");
	while (st.hasMoreTokens()) {
	    String configFileName = st.nextToken();
	    File f = new File(configFileName);
	    if (!f.isAbsolute()) {
		f = new File(root, configFileName);
		System.err.println("Looking for file: " + f);
		log("Looking for config file: " + f);
	    }
	    try {
		FileInputStream fin = new FileInputStream(f);
		props.load(fin);
		fin.close();
	    } catch (IOException e) {
		log("invalid config file " + e);
	    }
	}

        /*
	 * Find the document root.
	 * 1) look in web.xml for a root property
	 * 2) look in the config file.
	 */
	String docRoot = getServletContext().getInitParameter("root");
	if (docRoot == null) {
	    docRoot = props.getProperty("root", ".");
	}
	File f = new File(docRoot);
	if (!f.isAbsolute()) {
	    f = new File(root, docRoot);
	}
	props.put("root", f.getAbsolutePath());
	System.err.println("Root is: " + props.getProperty("root"));

	if (Main.startServer(props)) {
	    try {
		server = (Server)props.get("_server");
		props.remove("_server");
		socket = server.listen;
	    } catch (ClassCastException e) {
		throw new ServletException(
				     "ClassCastException: " + e.getMessage());
	    }
	} else {
	    throw new ServletException(
			   props.getProperty("_errMsg", "startServer failed"));
	}

	initok = server.init();
    }

    private boolean loadConfigFromResource(Properties config, String name) {
	if (name == null)
	    return false;
	InputStream in = getClass().getResourceAsStream(name);
	if (in != null) {
	    try {
		props.load(in);
		in.close();
		return true;
	    } catch (IOException e) {}
	}
	return false;
    }

    /**
     * Called by the servlet container to allow the servlet to respond to
     * a request.
     * 
     * <p>This method implements the abstract method declared in the
     * <code>GenericServlet</code> super class.
     *
     * @param req 	the <code>ServletRequest</code> object
     *			that contains the client's request
     *
     * @param res 	the <code>ServletResponse</code> object
     *			that will contain the servlet's response
     *
     * @exception ServletException 	if an exception occurs that
     *					interferes with the servlet's
     *					normal operation occurred
     *
     * @exception IOException 		if an input or output
     *					exception occurs
     *
     * @see javax.servlet.Servlet#service
     */
    public void service(ServletRequest req, ServletResponse res)
	throws ServletException, IOException {
    
	HttpServletRequest httpReq = null;
	HttpServletResponse httpRes = null;

	try {
	    httpReq = (HttpServletRequest)req;
	    httpRes = (HttpServletResponse)res;
	} catch (ClassCastException e) {
	    throw new ServletException(e.getMessage());
	}
	
	if (initok) {
	    Request request = null;
	    try {
		request = new BServletRequest(server, httpReq, httpRes);
	    } catch (UnknownHostException e) {
		throw new ServletException("UnknownHostException: " + e.getMessage());
	    }

	    // So the log file reports the port accurately
	    ((BServletServerSocket)socket).setLocalPort(req.getServerPort());

	    if (request.getRequest()) {
		request.props.put("context_path", httpReq.getContextPath());
		{
		    String s = httpReq.getServletPath();
		    if (s != null && s.length() != 0)
			server.log(Server.LOG_WARNING, null, "Servlet path is "
				   + s + " -- not an empty string.  Possible "
				   + "URL mapping error.");
		    request.props.put("servlet_path", s);

		    if ((s = httpReq.getPathInfo()) == null)
			s = "";
		    request.props.put("path_info", s);
		}
		request.props.put("servlet_container",
		    getServletContext().getServerInfo());

		server.requestCount++;

		if (server.handler.respond(request) == false) {
		    request.sendError(404, null, request.url);
		}
		server.log(Server.LOG_INFORMATIONAL, null, "request done");
	    }
	    try {
		request.out.close();
	    } catch (IOException e) {};
	    server.log(Server.LOG_INFORMATIONAL, null, "socket close");  // true?
	} else
	    httpRes.sendError(500);	    
    }


   /**
     * Called by the servlet container to indicate to a servlet that the
     * servlet is being taken out of service.  
     *
     * @see javax.servlet.Servlet#destroy
     */
    public void destroy() {
	super.destroy();
	try {
	    socket.close();
	} catch (IOException e) {}
	socket = null;
    }
}
