/*
 * CgiHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1998-2006 Sun Microsystems, Inc.
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
 * Version:  2.4
 * Created by suhler on 98/09/14
 * Last modified by suhler on 06/06/14 10:26:09
 *
 * Version Histories:
 *
 * 2.4 06/06/14-10:26:09 (suhler)
 *   added "runwith" and "hoheaders" to allow apache mod_xxx simulation
 *   .
 *
 * 2.3 05/11/27-18:26:21 (suhler)
 *   allow configuration choice of current -vs- original url
 *
 * 2.2 05/07/12-10:34:58 (suhler)
 *   check for HTTPS was wrong
 *
 * 2.1 02/10/01-16:36:22 (suhler)
 *   version change
 *
 * 1.22 02/05/02-11:15:33 (suhler)
 *   doc fixes, version updatew
 *
 * 1.21 02/05/02-08:48:30 (drach)
 *   Choose Runtime.exec method at run time
 *
 * 1.20 02/02/25-08:58:08 (suhler)
 *   use "url.orig" instead of request.url if available.
 *
 * 1.19 01/03/13-09:46:19 (suhler)
 *   added comments to show how to fix "CHDIR" bug when running under jdk1.3
 *   .
 *
 * 1.18 01/03/13-09:18:45 (suhler)
 *   return server error if CGI script fails
 *
 * 1.17 00/12/11-13:26:51 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.16 00/10/31-10:17:46 (suhler)
 *   doc fixes
 *
 * 1.15 00/10/05-21:17:56 (suhler)
 *   reject cgi scripts that have bad output
 *
 * 1.14 00/05/31-13:45:33 (suhler)
 *
 * 1.13 00/05/22-14:03:27 (suhler)
 *   doc updates
 *
 * 1.12 00/04/20-11:48:07 (cstevens)
 *   copyright.
 *
 * 1.11 99/12/07-10:54:33 (suhler)
 *   fixed script name
 *
 * 1.10 99/10/26-17:07:45 (cstevens)
 *   Get rid of public variables Request.server and Request.sock
 *   In all cases, Request.server was not necessary in the Handler.
 *   Request.sock was changed to Request.getSock(); it is still rarely used for
 *   diagnostics and logging (e.g., ChainSawHandler).
 *
 * 1.9 99/10/12-10:03:25 (suhler)
 *   temporary fix to use new mimeheader semantics
 *
 * 1.8 99/09/15-14:38:59 (cstevens)
 *   Rewritign http server to make it easier to proxy requests.
 *
 * 1.7 99/04/22-12:51:57 (suhler)
 *   Added "custom" property to enable placing handler properties into
 *   the cgi script environment
 *
 * 1.6 99/03/30-09:29:11 (suhler)
 *   documentation update
 *
 * 1.5 99/01/06-08:31:37 (suhler)
 *   fixed version string
 *
 * 1.4 98/10/22-10:44:32 (suhler)
 *   fixed bug that required a training /
 *
 * 1.3 98/10/22-09:23:25 (suhler)
 *   Added better diagnostics.
 *   There is still a bug that requires cgi scripts to end in a "/"
 *   - will be fixed soon
 *
 * 1.2 98/09/21-14:53:36 (suhler)
 *   changed the package names
 *
 * 1.2 98/09/14-18:03:03 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 handlers/CgiHandler.java
 *   Name history : 1 0 CgiHandler.java
 *
 * 1.1 98/09/14-18:03:02 (suhler)
 *   date and time created 98/09/14 18:03:02 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Server;

/**
 * Handler for implementing cgi/1.1 interface.
 * This implementation allows either suffix matching (e.g. .cgi) to identify
 * cgi scripts, or prefix matching (e.g. /bgi-bin). Defaults to "/".
 * All output from the cgi script is buffered (e.g. chunked encoding is
 *  not supported).
 * <br>
 * NOTE: in versions of Java prior to release 1.3, the ability to set
 * a working directory when running an external process is missing.
 * This handler automatically checks for this ability and sets the
 * proper working directory, but only if the underlying VM supports it.
 * <p>
 * The following request properties are used:
 * <dl class=props>
 * <dt>root	<dd> The document root for cgi files
 * <dt>suffix	<dd> The suffix for cgi files (defaults to .cgi)
 * <dt>prefix	<dd> The prefix for all cgi files (e.g. /cgi-bin)
 * <dt>url	<dd> "o(riginal)" or "c(urrent)".
 *		If an upstream handler has changed the URL, this specifes
 *		which url to look for the cgi script relative to.  The default
 *		is to use the original url.
 * <dt>custom	<dd> set to "true" to enable custom environment variables.
 *		If set, all server properties starting with this handler's
 *		prefix are placed into the environment with the name:
 *		<code>CONFIG_<i>name</i></code>, where <i>name</i> is the
 *		property key, in upper case, with the prefix removed.
 *		This allows cgi scripts to be customized in the server's
 *		configuration file.
 * <dt>runwith <dd>The command to use to run scripts.
 *		The absolute file path is added as the last
 *		parameter.  If not specified, the file name is run as the
 *		command.
 * <dt>noheaders<dd>
 *		According to the CGI spec, cgi documents are to begin with
 *		properly formed http headers to specifie the type, return status
 *		and optionally other meta information about the request.
 *		if "noheaders" is specified, then the content is expected
 *		to *not* have any http headers, and the content type is
 *		as implied by the url suffix.
 * </dl>
 * See example configuration in the samples included with the distribution.
 *
 * @author      Stephen Uhler
 * @version     2.4, 06/06/14
 */

public class CgiHandler implements Handler {
    private String propsPrefix;     // string prefix in properties table
    private int port;           // The listening port
    private String protocol;    // the access protocol http/https
    private String hostname;    // My hostname
    private static final String ROOT = "root";      // property for document root
    private static final String SUFFIX = "suffix";      // property for suffix string
    private static final String PREFIX = "prefix";      // All cgi scripts must start with this
    private static final String CUSTOM = "custom";      // add custom query variables
    private static final String URL = "url";      // how to map url to file

    private static String software = "Mini Java CgiHandler 0.3";
    private static Hashtable envMap;    // environ maps

    /**
     * construct table of CGI environment variables that need special handling
     */

    static {
	envMap = new Hashtable(2);
	envMap.put("content-length", "CONTENT_LENGTH");
	envMap.put("content-type", "CONTENT_TYPE");
    }

    public CgiHandler() {}

    /**
     * One time initialization.  The handler configuration properties are
     * extracted and set in
     * {@link #respond(Request)} to allow
     * upstream handlers to modify the parameters.
     */

    public boolean
    init(Server server, String prefix)
    {
	propsPrefix = prefix;
	port = server.listen.getLocalPort();
	hostname = server.hostName;
	protocol = server.protocol;
	return true;
    }

    /** 
     * Dispatch and handle the CGI request. Gets called on ALL requests.
     * Set up the environment, exec the process, and deal
     * appropriately with the input and output.
     *
     * In this implementation, all cgi script files must end with a
     * standard suffix, although the suffix may omitted from the url.
     * The url /main/do/me/too?a=b will look, starting in DocRoot, 
     * for main.cgi, main/do.cgi, etc until a matching file is found.
     * <p>
     * Input parameters examined in the request properties:
     * <dl>
     * <dt>Suffix<dd>The suffix for all cgi scripts (defaults to .cgi)
     * <dt>DocRoot<dd>The document root, for locating the script.
     * </dl>
     */

    public boolean
    respond(Request request)
    {
	String[] command;   // The command to run
	Process cgi;        // The result of the cgi process

	// Find the cgi script associated with this request.
	// + turn the url into a file name
	// + search path until a script is found

	String which =  request.props.getProperty(propsPrefix + URL, "o");
	String prefix = request.props.getProperty(propsPrefix + PREFIX, "/");

	String url;
	if (which.indexOf("c") >= 0) {
	    url = request.url;
	} else {
	    url = request.props.getProperty("url.orig");
	}

	if (!url.startsWith(prefix)) {
	    return false;
	}

	boolean needheaders =
		request.props.getProperty(propsPrefix + "noheaders") == null;
	boolean useCustom = !request.props.getProperty(propsPrefix +
		CUSTOM, "").equals("");
	String suffix = request.props.getProperty(propsPrefix + SUFFIX, ".cgi");
	String root = request.props.getProperty(propsPrefix + ROOT,
		request.props.getProperty(ROOT, "."));
	request.log(Server.LOG_DIAGNOSTIC,propsPrefix + " suffix=" + suffix +
		" root=" + root + " url: " + url);

	int start = 1;
	int end = 0;
	File name = null;
	while (end < url.length()) {
	    end = url.indexOf(File.separatorChar, start);
	    if (end < 0) {
		end = url.length();
	    }
	    String s = url.substring(1, end);
	    if (!s.endsWith(suffix)) {
		s += suffix;
	    }
	    name = new File(root, s);
	    request.log(Server.LOG_DIAGNOSTIC,propsPrefix + " looking for: " +
	    		name);
	    if (name.isFile()) {
		break;
	    }
	    name = null;
	    start = end+1;
	}

	if (name == null) {
	    return false;
	}

	/*
	 * If there is a single query parameter but no value, then tack
	 * it on as an additional argument (is this right?)
	 */

	boolean extra = (request.query.indexOf("=") == -1);

	String runwith = request.props.getProperty(propsPrefix + "runwith");
	String path = name.getAbsolutePath();
        String type = FileHandler.getMimeType(path, request.props, propsPrefix);
	int i = 0;

	if (runwith != null) {
	    request.log(Server.LOG_DIAGNOSTIC,propsPrefix + " command= " + 
			runwith + " " + path);
	    StringTokenizer st = new StringTokenizer(runwith);
	    command = new String[st.countTokens() + (extra ? 2: 1)];
	    while (st.hasMoreTokens()) {
		command[i++] = st.nextToken();
	    }
	} else { 
	    command = new String[extra ? 2 : 1];
	    request.log(Server.LOG_DIAGNOSTIC,propsPrefix+" command= " + path);
	}
	command[i] = path;

	if (extra) {
	    command[i+1] = request.query;
	}

	/*
	 * Build the environment string.  First, get all the http headers
	 * most are transferred directly to the environment, some are
	 * handled specially.  Multiple headers with the same name are not
	 * handled properly.
	 */

	Vector env = new Vector();
	Enumeration keys = request.headers.keys();
	while(keys.hasMoreElements()) {
	    String key = (String) keys.nextElement();
	    String special = (String) envMap.get(key.toLowerCase());
	    if (special != null) {
		env.addElement(special + "=" + request.headers.get(key));
	    } else {
		env.addElement("HTTP_"
		    + key.toUpperCase().replace('-','_')
		    + "="
		    + request.headers.get(key));
	    }
	}

	// Add in the rest of them

	env.addElement("GATEWAY_INTERFACE=CGI/1.1");
	env.addElement("SERVER_SOFTWARE=" + software);
	env.addElement("SERVER_NAME=" + hostname);
	env.addElement("PATH_INFO=" + url.substring(end));

	String pre = url.substring(0,end);
	if (pre.endsWith(suffix)) {
	    env.addElement("SCRIPT_NAME=" + pre);
	} else {
	    env.addElement("SCRIPT_NAME=" + pre + suffix);
	}
	env.addElement("SERVER_PORT=" + port);
	env.addElement("REMOTE_ADDR=" +
		request.getSocket().getInetAddress().getHostAddress());
	env.addElement("PATH_TRANSLATED=" + root + url.substring(end));
	env.addElement("REQUEST_METHOD=" + request.method);
	env.addElement("SERVER_PROTOCOL=" + request.protocol);
	env.addElement("QUERY_STRING=" + request.query);

	String serverUrl =  request.serverUrl();
	env.addElement("SERVER_URL=" + serverUrl);

	if (serverUrl.startsWith("https:")) {
	    env.addElement("HTTPS=on");
	}

        /*
         * add in the "custom" environment variables, if requested
         */

        if (useCustom) {
            int len = propsPrefix.length();
	    keys = request.props.propertyNames();
	    while(keys.hasMoreElements()) {
		String key = (String) keys.nextElement();
		if (key.startsWith(propsPrefix)) {
		    env.addElement("CONFIG_" +
			    key.substring(len).toUpperCase() +
			    "=" + request.props.getProperty(key, null));
		}
	    }
	    env.addElement("CONFIG_PREFIX=" + propsPrefix);
        }

	String environ[] = new String[env.size()];
	env.copyInto(environ);
	request.log(Server.LOG_DIAGNOSTIC,propsPrefix + " ENV= " + env);

	// Run the script

	try {
	    cgi = exec(command, environ, new File(name.getParent()));

	    DataInputStream in = new DataInputStream(
		    new BufferedInputStream(cgi.getInputStream()));

	    // If we have data, send it to the process

	    if (request.postData != null) {
		OutputStream toGci = cgi.getOutputStream();
		
		toGci.write(request.postData, 0, request.postData.length);
		toGci.close();
	    }

	    // Now get the output of the cgi script.  Start by reading the
	    // "mini header", then just copy the rest

	    String head;
	    type = (type==null ? "text/html" : type);
	    int status = 200;
	    while(needheaders) {
		head = in.readLine();
		if (head==null || head.length() == 0) {
		    break;
		}
		int colonIndex = head.indexOf(':');
		if (colonIndex < 0) {
		    request.sendError(500, "Missing header from cgi output");
		    return true;
		}
		String lower = head.toLowerCase();
		if (lower.startsWith("status:")) {
		    try {
			status = Integer.parseInt(
				head.substring(colonIndex+1).trim());
		    } catch (NumberFormatException e) {}
		} else if (lower.startsWith("content-type:")) {
		    type = head.substring(colonIndex+1).trim();
		} else if (lower.startsWith("location:")) {
		    status = 302;
		    request.addHeader(head);
		} else {
		    request.addHeader(head);
		}
	    }

	    /*
	     * Now copy the rest of the data into a buffer, so we can count it.
	     * we should be doing chunked encoding for 1.1 capable clients XXX
	     */

	    ByteArrayOutputStream buff = new ByteArrayOutputStream();
	    int c;
	    while((c = in.read()) >= 0) {
		buff.write(c);
	    }

	    request.sendHeaders(status, type, buff.size());
	    buff.writeTo(request.out);
	    request.log(Server.LOG_DIAGNOSTIC, propsPrefix, 
	    	"Cgi output " + buff.size());
	    cgi.waitFor();
	} catch (Exception e) {
	    // System.out.println("oops: " + e);
	    //e.printStackTrace();
	    request.sendError(500, "CGI failure", e.getMessage());
	}
	return true;
    }

    private int params;
    private Method execMethod;

    private void search() throws Exception {
	Method[] m = Runtime.class.getDeclaredMethods();
	int n = -1;
	for (int i = 0; i < m.length; i++) {
	    if (m[i].getName().equals("exec")) {
		Class[] c = m[i].getParameterTypes();
		if (c.length == 3 && c[0] == String[].class) {
		    // we have 3 arg exec for sure
		    params = 3;
		    n = i;
		    break;
		}
		if (c.length == 2 && c[0] == String[].class) {
		    // let's save it in case we need it, but keep looking
		    params = 2;
		    n = i;
		}
	    }
	}

	if (n == -1) {
	    throw new Exception("No method exec(String[], ...) found in Runtime");
	}

	execMethod = m[n];
    }

    private Object[] args;

    private Process exec(String[] cmd, String[] envp, File dir)
	throws Exception
    {
	if (execMethod == null) {
	    search();
	    if (params == 2) {
		args = new Object[2];
	    } else {
		args = new Object[3];
	    }
	}

	args[0] = cmd;
	args[1] = envp;
	if (params == 3) {
	    args[2] = dir;
	}
	
	return (Process)execMethod.invoke(Runtime.getRuntime(), args);
    }
}
