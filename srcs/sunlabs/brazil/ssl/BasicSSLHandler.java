/*
 * BasicSSLHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2005-2007 Sun Microsystems, Inc.
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
 * Version:  1.2
 * Created by suhler on 05/07/13
 * Last modified by suhler on 07/01/12 14:32:49
 *
 * Version Histories:
 *
 * 1.2 07/01/12-14:32:49 (suhler)
 *   remove the password from the properties if it was entered directly
 *
 * 1.2 05/07/13-10:09:06 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/BasicSSLHandler.java
 *
 * 1.1 05/07/13-10:09:05 (suhler)
 *   date and time created 05/07/13 10:09:05 by suhler
 *
 */

package sunlabs.brazil.ssl;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.http.*;
import sunlabs.brazil.util.http.HttpInputStream;

/**
 * Start an "ssl" server.
 * This requires jdk1.4+.
 * <dl class=props>
 * <dt>sslport<dd>The port to start the server on.  If no port is specified, 
 *     Then the existing server port is reused.  <b>Note</b>: Do not use
 *     a prefix when specifying this property, it is precicely "sslport".
 * <dt>enable<dd>Must be set to "true" to turn on ssl server.
 * <dt>store<dd>The path name to the certificate keystore.
 * <dt>password<dd>The certificate store password. If the password starts
 * with "@", the rest of the password is taken to be the name of the file
 * containing the password. "@-" causes the password to be read from stdin.
 * <dt>auth<dd>Require a valid client certificate (not useful).
 * </dl>
 * Keystores are created with:
 * <pre>keytool -genkey -keystore [store_name] -keyalg RSA</pre>
 */

public class BasicSSLHandler implements Handler{
    String prefix = null;

    public boolean
    init(Server server, String prefix) {
	System.out.println("init ssl hander for server " + server);
        if (!Format.isTrue(server.props.getProperty(prefix + "enable"))) {
	    server.log(Server.LOG_DIAGNOSTIC, prefix,
		"server not enabled");
            return false;
        }
	this.prefix = prefix;
        int port = server.listen.getLocalPort();
        String store = server.props.getProperty(prefix + "store");
        if (store != null) {
           System.setProperty("javax.net.ssl.keyStore", store);
        }
        String pass = getPassword(server);
        if (pass != null) {
           System.setProperty("javax.net.ssl.keyStorePassword", pass);
        }
  
	int sslport = port;
        try {
	    String str = server.props.getProperty("sslport");
	    sslport = Integer.decode(str).intValue();
        } catch (Exception e) {}
	System.out.println("sslport=" + sslport + " port=" + port);

        int queue = 1024;
        try {
	    String str = server.props.getProperty("listenQueue");
	    queue = Integer.decode(str).intValue();
        } catch (Exception e) {}
    
        ServerSocketFactory factory = SSLServerSocketFactory.getDefault();
	ServerSocket listen;

	/*
	 * If we are re-using the existing port, we need to close it
	 * before creating the new server socket.  If the later operation
	 * fails, we're screwed.
	 */

        try {
	    if (port == sslport) {
                server.listen.close();
		listen = factory.createServerSocket(sslport, queue);
	    } else {
		listen = factory.createServerSocket(sslport, queue);
                server.listen.close();
	    }
	} catch (IOException e) {
	    if (port == sslport) {
		server.initFailure=true;
		server.log(Server.LOG_ERROR, prefix,
			"can't start server: " + e);
	    } else {
		server.log(Server.LOG_WARNING, prefix,
			"Server disabled: " + e);
	    }
            return false;
        }
        
        if (Format.isTrue(server.props.getProperty(prefix + "auth"))) {
	    ((SSLServerSocket) listen).setWantClientAuth(true);
	}
        server.listen = listen;
        server.protocol="https";
	server.log(Server.LOG_WARNING, prefix,
	    "Installed ssl server on port: " + sslport);
        return true;
    }

    /**
     * Get the password, either from the properties, or a file.
     */

    String getPassword(Server server) {
        String pass = server.props.getProperty(prefix + "password");
	if (pass==null || !pass.startsWith("@")) {
	   server.props.remove(prefix + "password");
	   return pass;
	}
	String file = pass.substring(1);
	HttpInputStream in;
	try {
	    if (file.equals("-")) {
		in = new HttpInputStream(System.in);
		pass = in.readLine();
	    } else {
		in = new HttpInputStream(new FileInputStream(file));
		pass = in.readLine();
		in.close();
	    }
	} catch (IOException e) {
	    server.log(Server.LOG_WARNING, prefix,
		"Can't read password file: " + e);
	    return null;
	}
	return pass;
    }

    public boolean
    respond(Request request) throws IOException {
	return false;
    }
}
