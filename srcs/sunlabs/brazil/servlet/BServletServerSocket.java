/*
 * BServletServerSocket.java
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
 * Version:  2.2
 * Created by drach on 01/06/01
 * Last modified by suhler on 04/11/30 15:19:41
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:41 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:39:15 (suhler)
 *   version change
 *
 * 1.4 02/04/19-15:00:28 (drach)
 *   Added a no argument constructor
 *
 * 1.3 01/06/11-17:06:17 (suhler)
 *   fix javadoc errors
 *
 * 1.2 01/06/04-14:41:46 (suhler)
 *   package move
 *
 * 1.2 01/06/01-16:31:01 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 servlet/BServletServerSocket.java
 *
 * 1.1 01/06/01-16:31:00 (drach)
 *   date and time created 01/06/01 16:31:00 by drach
 *
 */

package sunlabs.brazil.servlet;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.net.SocketException;
import java.net.SocketImplFactory;

/**
 *
 * This is a special <code>ServerSocket</code> intended for use by the
 * Brazil Servlet Adapter. It provides dummy methods for the
 * <code>ServerSocket</code> operations used by the Brazil Toolkit.  Do
 * not use this class for anything other than it's intended use as a
 * <code>ServerSocket</code> object that is passed to the constructor of
 * a <code>Server</code> object by the <code>BrazilServlet</code>'s
 * <code>init</code> method.
 *
 * @author	Steve Drach <drach@sun.com>
 * @version		2.2
 *
 * @see         java.net.ServerSocket
 * @see         BServletServerSocket
 * @see         sunlabs.brazil.server.Server
 * @see         BrazilServlet
 * @see         BServletRequest
 */
public class BServletServerSocket extends ServerSocket {

    private InetAddress bindAddr;
    private int port;
    
    /**
     * Creates a <code>BServletServerSocket</code> object
     * using -1 as a port number.
     */
    public BServletServerSocket() throws IOException {
	this(-1, 50, null);
    }

    /**
     * Creates a <code>BServletServerSocket</code> object.
     *
     * @param	port the port number this object encapsulates.
     *
     * @see     #setLocalPort(int port)
     */
    public BServletServerSocket(int port) throws IOException {
	this(port, 50, null);
    }

    private BServletServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
	super(port+40000);  // big hack, but not sure what else to do
	close();            // here, other than close it fast

	if (port < -1 || port > 0xFFFF)
	    throw new IllegalArgumentException(
		       "Port value out of range: " + port);
	try {
	    if (bindAddr == null)
		bindAddr = InetAddress.getLocalHost();
	    
	    this.bindAddr = bindAddr;
	    this.port = port;
	} catch(SecurityException e) {
	    close();
	    throw e;
	} catch(IOException e) {
	    close();
	    throw e;
	}
    }

    /**
     * Returns the local address to which this socket is bound.
     *
     * @return  the local address number to which this socket is bound.
     */
    public InetAddress getInetAddress() {
	return bindAddr;
    }

    /**
     * Sets the local port to which this socket is bound.  Not a method in
     * <code>ServerSocket</code> because the port is fixed at construction
     * time.  It's needed here because a Servlet doesn't know it's local
     * port until it services it's first request.
     *
     * @return  the local port number to which this socket is bound.
     */
    public void setLocalPort(int port) {
	this.port = port;
    }

    /**
     * Returns the local port to which this socket is bound.
     *
     * @return  the local port number to which this socket is bound.
     */
    public int getLocalPort() {
	return port;
    }

    /**
     * Dummy method, not implemented.
     *
     * @return        null
     *
     * @exception     IOException if method called.
     */
    public Socket accept() throws IOException {
	if (true)
	    throw new IOException("Method not implemented!");
	return null;
    }

    /**
     * Closes this socket.
     *
     * @exception  IOException  if an I/O error occurs when closing this socket.
     */
    public void close() throws IOException {
	super.close();
    }

    /**
     * Dummy method, not implemented.
     *
     * @param timeout doesn't do anything.
     *
     * @exception     SocketException if method called.
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException {
	throw new SocketException("Method not implemented!");
    }

    /**
     * Dummy method, not implemented.
     *
     * @return        -1
     * @exception     IOException if method called.
     */
    public synchronized int getSoTimeout() throws IOException {
	if (true)
	    throw new IOException("Method not implemented!");
	return -1;
    }

    /**
     * Converts this socket to a <code>String</code>.
     *
     * @return  a string representation of this socket.
     */
    public String toString() {
	return "ServerSocket[addr=" + getInetAddress() + 
		",localport=" + getLocalPort()  + "]";
    }

    /**
     * Dummy method, not implemented.
     *
     * @param fac     doesn't do anything.
     *
     * @exception     IOException if method called.
     */
    public static synchronized void setSocketFactory(SocketImplFactory fac) throws IOException {
	throw new IOException("Method not implemented!");
    }
}
