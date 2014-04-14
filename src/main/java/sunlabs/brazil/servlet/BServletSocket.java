/*
 * BServletSocket.java
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
 * Last modified by suhler on 04/11/30 15:19:42
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:42 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:39:15 (suhler)
 *   version change
 *
 * 1.3 01/06/11-17:06:27 (suhler)
 *   fix javadoc errors
 *
 * 1.2 01/06/04-14:41:52 (suhler)
 *   package move
 *
 * 1.2 01/06/01-16:31:02 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 servlet/BServletSocket.java
 *
 * 1.1 01/06/01-16:31:01 (drach)
 *   date and time created 01/06/01 16:31:01 by drach
 *
 */

package sunlabs.brazil.servlet;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * This is a special <code>Socket</code> intended for use by the Brazil
 * Servlet Adapter.  It provides dummy methods for the <code>Socket</code>
 * operations used by the Brazil Toolkit.  Do not use this
 * class for anything other than it's intended use as a
 * <code>Socket</code> object that is passed to the constructor
 * of a <code>Request</code> object by the <code>BServletRequest</code>'s
 * constructor.
 *
 * @author	Steve Drach <drach@sun.com>
 * @version		2.2
 *
 * @see         java.net.Socket
 * @see         BServletServerSocket
 * @see         BrazilServlet
 * @see         BServletRequest
 * @see         sunlabs.brazil.server.Request
 */
public class BServletSocket extends Socket {

    private InputStream in;
    private OutputStream out;

    private InetAddress address;
    private int port;
    private InetAddress localAddr;
    private int localPort;

    /**
     * Creates a <code>BServletSocket</code> object.
     *
     * @param	req the <code>HttpServletRequest</code> passed to 
     *              <code>BrazilServlet</code> by the servlet container.
     * @param   res the <code>HttpServletResponse</code> passed to 
     *              <code>BrazilServlet</code> by the servlet container.
     *
     * @see         BrazilServlet
     */
    public BServletSocket(HttpServletRequest req, HttpServletResponse res)
        throws UnknownHostException, IOException
    {
	// remote port is arbitrary and we can't get it from Servlet API anyway
	this(InetAddress.getByName(req.getRemoteHost()), 9999,
	     InetAddress.getByName(req.getServerName()), req.getServerPort(),
	     true);
	in = req.getInputStream();
	out = res.getOutputStream();
    }

    private BServletSocket(InetAddress address, int port, InetAddress localAddr,
		  int localPort, boolean stream) throws IOException {

	if (port < 0 || port > 0xFFFF) {
	    throw new IllegalArgumentException("port out range:"+port);
	}

	if (localPort < 0 || localPort > 0xFFFF) {
	    throw new IllegalArgumentException("port out range:"+localPort);
	}

	try {
	    if (localAddr != null || localPort > 0) {
		if (localAddr == null) {
		    // localAddr = InetAddress.anyLocalAddress;
		    localAddr = InetAddress.getLocalHost();
		}
		this.localAddr = localAddr;
		this.localPort = localPort;
	    }
	    this.address = address;
	    this.port = port;
	} catch (Exception e) {
	    close();
	    throw new IOException(e.getMessage());
	}
    }

    /**
     * Returns the address to which the socket is connected.
     *
     * @return  the remote IP address to which this socket is connected.
     */
    public InetAddress getInetAddress() {
	return address;
    }

    /**
     * Returns the local address to which the socket is bound.
     *
     * @return the local address to which the socket is bound.
     */
    public InetAddress getLocalAddress() {
	if (localAddr == null) {
	    try {
		localAddr = InetAddress.getLocalHost();
	    } catch (UnknownHostException u) {
		u.printStackTrace();
	    }
	}
	return localAddr;
    }

    /**
     * Returns the remote port to which this socket is connected.
     *
     * @return  the remote port number to which this socket is connected.
     */
    public int getPort() {
	return port;
    }

    /**
     * Returns the local port to which this socket is bound.
     *
     * @return  the local port number to which this socket is bound.
     */
    public int getLocalPort() {
	return localPort;
    }

    /**
     * Returns an input stream for this socket.
     *
     * @return     an input stream for reading bytes from this socket.
     *
     * @exception  IOException  if an I/O error occurs when creating the
     *               input stream.
     */
    public InputStream getInputStream() throws IOException {
	if (in == null)
	    throw new IOException("No InputStream associated with Socket");
	return in;
    }

    /**
     * Returns an output stream for this socket.
     *
     * @return     an output stream for writing bytes to this socket.
     *
     * @exception  IOException  if an I/O error occurs when creating the
     *               output stream.
     */
    public OutputStream getOutputStream() throws IOException {
	if (out == null)
	    throw new IOException("No OutputStream associated with Socket");
	return out;
    }

    /**
     * Dummy method, not implemented.
     *
     * @param on  doesn't do anything.
     *
     * @exception SocketException if method called.
     */
    public void setTcpNoDelay(boolean on) throws SocketException {
	throw new SocketException("Method not implemented!");
    }

    /**
     * Dummy method, not implemented.
     *
     * @return    <code>false</code>
     *
     * @exception SocketException if method called.
     */
    public boolean getTcpNoDelay() throws SocketException {
	if (true)
	    throw new SocketException("Method not implemented!");
	return false;
    }

    /**
     * Dummy method, not implemented.
     * 
     * @param on     doesn't do anything.
     * @param linger doesn't do anything.
     *
     * @exception    SocketException if method called.
     */
    public void setSoLinger(boolean on, int linger) throws SocketException {
	throw new SocketException("Method not implemented!");
    }

    /**
     * Dummy method, not implemented.
     *
     * @return    -1
     *
     * @exception SocketException if method called.
     */
    public int getSoLinger() throws SocketException {
	if (true)
	    throw new SocketException("Method not implemented!");
	return -1;
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
     * @return    -1
     *
     * @exception SocketException if method called.
     */
    public synchronized int getSoTimeout() throws SocketException {
	if (true)
	    throw new SocketException("Method not implemented!");
	return -1;
    }

    /**
     * Dummy method, not implemented.
     *
     * @param size doesn't do anything.
     *
     * @exception SocketException if method called.
     */
    public synchronized void setSendBufferSize(int size)
    throws SocketException{
	throw new SocketException("Method not implemented!");
    }

    /**
     * Dummy method, not implemented.
     *
     * @return    -1
     *
     * @exception SocketException if method called.
     */
    public synchronized int getSendBufferSize() throws SocketException {
	if (true)
	    throw new SocketException("Method not implemented!");
	return -1;
    }

    /**
     * Dummy method, not implemented.
     *
     * @param size doesn't do anything.
     *
     * @exception SocketException if method called.
     */
    public synchronized void setReceiveBufferSize(int size)
    throws SocketException{
	throw new SocketException("Method not implemented!");
    }

    /**
     * Dummy method, not implemented.
     *
     * @return    -1
     *
     * @exception SocketException if method called.
     */
    public synchronized int getReceiveBufferSize()
    throws SocketException{
	if (true)
	    throw new SocketException("Method not implemented!");
	return -1;
    }

    /**
     * Dummy method, not implemented.
     *
     * @param on  doesn't do anything.
     *
     * @exception SocketException if method called.
     */
    public void setKeepAlive(boolean on) throws SocketException {
	throw new SocketException("Method not implemented!");
    }

    /**
     * Dummy method, not implemented.
     *
     * @return    <code>false</code>
     *
     * @exception SocketException if method called.
     */
    public boolean getKeepAlive() throws SocketException {
	if (true)
	    throw new SocketException("Method not implemented!");
	return false;
    }

    /**
     * Closes this socket.
     *
     * @exception  IOException  if an I/O error occurs when closing this socket.
     */
    public synchronized void close() throws IOException {
    }

    /**
     * Dummy method, not implemented.
     *
     * @exception IOException if method called.
     */
    public void shutdownInput() throws IOException
    {
	throw new IOException("Method not implemented!");
    }
    
    /**
     * Dummy method, not implemented.
     *
     * @exception IOException if method called.
     */
    public void shutdownOutput() throws IOException
    {
	throw new IOException("Method not implemented!");
    }

    /**
     * Converts this socket to a <code>String</code>.
     *
     * @return  a string representation of this socket.
     */
    public String toString() {
	return "Socket[addr=" + address +
	    ",port=" + port +
	    ",localport=" + localPort + "]";
    }
}
