/*
 * SunProxy.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2004 Sun Microsystems, Inc.
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
 * Version:  2.3
 * Created by cstevens on 99/09/29
 * Last modified by suhler on 04/11/30 15:19:40
 *
 * Version Histories:
 *
 * 2.3 04/11/30-15:19:40 (suhler)
 *   fixed sccs version string
 *
 * 2.2 03/08/01-16:19:26 (suhler)
 *   fixes for javadoc
 *
 * 2.1 02/10/01-16:37:10 (suhler)
 *   version change
 *
 * 1.13 02/06/03-15:53:43 (suhler)
 *   docs
 *
 * 1.12 00/05/22-14:06:21 (suhler)
 *   doc updates
 *
 * 1.11 00/04/17-14:27:36 (cstevens)
 *   do't proxy all #.#.#.# addresses.
 *
 * 1.10 00/03/29-16:17:00 (cstevens)
 *   error messages.
 *
 * 1.9 00/03/10-17:10:02 (cstevens)
 *   Fix wildcard and unneeded import
 *
 * 1.8 00/02/11-12:49:17 (suhler)
 *   better disgnostics
 *
 * 1.7 99/11/01-11:57:20 (suhler)
 *   fix imports, make a handler
 *
 * 1.6 99/10/26-18:12:24 (cstevens)
 *   documentation.
 *   Eliminate unused SocketFactory.newSocket(InetAddress, int) method.
 *
 * 1.5 99/10/19-18:57:26 (cstevens)
 *   Differentiate between "connection refused" and "unknown host" when attempting
 *   to communicate via sun itelnet proxy.
 *
 * 1.4 99/10/14-12:49:55 (cstevens)
 *   Remove Server.initFields().
 *
 * 1.3 99/10/04-16:03:05 (cstevens)
 *   wildcard imports.
 *
 * 1.2 99/09/30-14:11:47 (cstevens)
 *   Better error message if couldn't connect because machine wasn't listening
 *   on specified port, rather than saying "unknown host".
 *
 * 1.2 99/09/29-16:14:26 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 proxy/SunProxy.java
 *
 * 1.1 99/09/29-16:14:25 (cstevens)
 *   date and time created 99/09/29 16:14:25 by cstevens
 *
 */

package sunlabs.brazil.proxy;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

import sunlabs.brazil.util.SocketFactory;
import sunlabs.brazil.util.http.HttpInputStream;
import sunlabs.brazil.util.http.HttpRequest;
import sunlabs.brazil.util.regexp.Regexp;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Sun Specific implementation of a SocketFactory and
 * proxy-decider that work together to decipher
 * the specifics of the Sun Internet setup.
 * <p>
 * NOTE:  The mechanism used by this class has been disabled;
 * it is included here to illustrate how to write a custom proxy.
 *
 * @author	Stephen Uhler (stephen.uhler@sun.com)
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version		2.3
 */
public class SunProxy
    implements SocketFactory, UseProxy, Handler
{
    private static final String TUNNEL_HOST = "tunnelHost";
    private static final String TUNNEL_PORT = "tunnelPort";
    private static final String TIMEOUT = "timeout";

    private String tunnelHost = "sun-barr.ebay";
    private int tunnelPort = 3666;
    private int timeout = 100000;

    /**
     * Called when the <code>Server</code> is being initialized to install
     * this object as the <code>SocketFactory</code> for the
     * <code>HttpRequest</code>.
     * <p>
     * This procedure is very specific and specialized to the Brazil server.
     * This functionality should probably be moved into a separate "Init"
     * class that: <ul>
     * <li> examines the configuration properties.
     * <li> creates a new instace of the <code>SunProxy</code> class.
     * <li> installs this object as the <code>SocketFactory</code>.
     * </ul>
     * That would disentangle this class from the Brazil server, since
     * nothing else in this class is tied to the implementation.
     * <p>
     * As it stands now, the first time this method is called, the side
     * effect is to install itself as the <code>SocketFactory</code>.
     * This method will also be called subsequently when instances of the
     * <code>ProxyHandler</code> instantiate this object as a
     * <code>UseProxy</code> decider, but in that case, no configuration
     * properties need to be examined.
     *
     * @param	server
     *		The http server that owns this object.  This object uses
     *		<code>Server.props</code> to obtain run time configuration
     *		information.
     *
     * @param	prefix
     *		A prefix to prepend to all of the keys that this object
     *		uses to extract configuration information out of
     *		<code>Server.props</code>.
     *
     * @return	<code>true</code> always, indicating success.
     */
    public boolean
    init(Server server, String prefix)
    {
	if (HttpRequest.socketFactory == null) {
	    server.log(Server.LOG_WARNING, prefix, "Turning on Sun Proxy");
	    Properties props = server.props;

	    tunnelHost = props.getProperty(prefix + TUNNEL_HOST, tunnelHost);

	    try {
		String str = props.getProperty(prefix + TUNNEL_PORT);
		tunnelPort = Integer.decode(str).intValue();
	    } catch (Exception e) {}

	    try {
		String str = props.getProperty(prefix + TIMEOUT);
		timeout = Integer.decode(str).intValue();
	    } catch (Exception e) {}

	    HttpRequest.socketFactory = this;
	}
	server.log(Server.LOG_WARNING, prefix, "Sun Proxy enabled");
	return true;
    }

    /**
     * Handler http requests - doesn't do anything yet
     */
    
    public boolean respond(Request request) {return false;}

    /**
     * Determines if the user should use an HTTP proxy when sending an HTTP
     * request to the specified host and port.
     * <p>
     * Whether or not to proxy may depend upon the HTTP proxy the caller is
     * using.  Currently, there is no way to capture and use this information.
     * <p>
     * The decision is different than deciding if the host is local, because
     * using the itelnet tunnelling trick we can reach some external hosts
     * from within the firewall.  This routine is therefore in cohoots with
     * the itelnet behavior, and requires that the caller use this object
     * as the <code>SocketFactory</code> if this method returns
     * <code>true</code>.
     * <p>
     * Observed behavior: <ul>
     * <li> DNS is messed up, and can resolve the hostname of some machines
     *	    that are not accessible.  Simplifying assumption: all #.#.#.#
     *	    must be proxied
     * <li> pangaea.eng(.sun.com) is inside the firewall and accessible via
     *      a direct socket.  Return <code>false</code>.
     * <li> www.sgi.com and www.microsoft.com are outside the firewall but
     *      accessible via an itelnet socket.  Return <code>false</code>.
     * <li> www.sun.com and docs.sun.com are outside the firewall but not
     *	    accessible via an itelnet socket.  Return <code>true</code>.
     * </ul>
     *
     * @param	host
     *		The host name.
     *
     * @param	port
     *		The port number.
     *
     * @return	<code>true</code> if the user should send the HTTP request
     *		via an HTTP proxy, <code>false</code> if the user can
     *		send the HTTP request directly to the specified named host.
     *
     * @implements  UseProxy#useProxy
     */
    public boolean
    useProxy(String host, int port)
    {
	if (host.startsWith("192.9.")) {
	    /*
	     * Sun addresses that cannot be accessed from sun-barr.ebay!
	     */
	    return true;
	}
	if (host.endsWith(".sun.com") && (dots(host) == 2)) {
	    /*
	     * Machines like www.sun.com or java.sun.com that cannot be
	     * accessed from sun-barr.ebay.
	     */
	     
	    return true;
	}
	return false;
    }

    private static int
    dots(String host)
    {
	int dots = 0;
	int index = -1;

	while (true) {
	    index = host.indexOf('.', index);
	    if (index < 0) {
		break;
	    }
	    dots++;
	    index++;
	}

	return dots;
    }

    private static final Regexp direct
	    = new Regexp("^(([^.]+\\.[^.]+\\.sun\\.com)|([^.]+\\.[^.]+))$");

    private boolean
    isDirect(String host)
    {
        return direct.match(host, (String[]) null);
    }

    public Socket
    newSocket(String host, int port)
	throws IOException
    {
	Socket sock = null;
    
	if (isDirect(host)) {
	    try {
/*System.out.println("trying local " + host + ":" + port + " " + System.currentTimeMillis());*/
		sock = new Socket(host, port);
		sock.setSoTimeout(timeout);
/*System.out.println("connected local " + host + ":" + port + " " + System.currentTimeMillis());*/
		return sock;
	    } catch (ConnectException e) {
		throw e;
	    } catch (IOException e) {}
	}

	try {
/*System.out.println("trying itelnet " + host + ":" + port + " " + System.currentTimeMillis());*/
	    sock = new Socket(tunnelHost, tunnelPort);
	    sock.setSoTimeout(timeout);

/*System.out.println("asking for " + host + ":" + port + " " + System.currentTimeMillis());*/

	    PrintStream out = new PrintStream(sock.getOutputStream());
	    out.println(host + " " + port);
	    out.flush();
	    out = null;

	    HttpInputStream in = new HttpInputStream(sock.getInputStream());
	    while (true) {
		String line = in.readLine();
/*System.out.println(line);*/
		if ((line == null) || line.endsWith("refused")) {
		    throw new ConnectException(host + ":" + port);
		}
		if (line.startsWith("connected to")) {
/*System.out.println("connected itelnet " + host + ":" + port + " " + System.currentTimeMillis());*/
		    return sock;
		}
		if (line.endsWith("unknown host")) {
		    throw new UnknownHostException(host);
		}
	    }
	} catch (IOException e) {
	    if (sock != null) {
		sock.close();
	    }
	    throw e;
	}
    }
}
