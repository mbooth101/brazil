/*
 * AsteriskAGIHandler.java
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
 * Version:  1.7
 * Created by suhler on 05/06/06
 * Last modified by suhler on 06/05/08 10:28:13
 *
 * Version Histories:
 *
 * 1.7 06/05/08-10:28:13 (suhler)
 *   add "agi_host" parameter containing the asterisk server's hostname
 *   .
 *
 * 1.6 06/05/08-09:59:48 (suhler)
 *   prefix session id with properties prefix
 *   .
 *
 * 1.5 06/04/25-14:54:41 (suhler)
 *   doc fixes
 *
 * 1.4 05/11/10-10:56:48 (suhler)
 *   doc cleanup
 *
 * 1.3 05/06/07-15:58:07 (suhler)
 *   redo the way the socket streams are handled
 *   make sure commands are written in a single packet
 *   misc cleanups
 *
 * 1.2 05/06/07-15:54:35 (suhler)
 *   checkpoint
 *
 * 1.2 05/06/06-21:03:10 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 asterisk/AsteriskAGIHandler.java
 *
 * 1.1 05/06/06-21:03:09 (suhler)
 *   date and time created 05/06/06 21:03:09 by suhler
 *
 */

package sunlabs.brazil.asterisk;

import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.server.TestRequest;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.template.TemplateRunner;
import sunlabs.brazil.util.http.HttpInputStream;
import sunlabs.brazil.util.http.MimeHeaders;

/**
 * FAGI (fast AGI) handler and template for Asterisk.  
 * This handler/template starts a server listening on the * FAGI port.
 * Anytime it gets an agi request from * it creates a dummy
 * request object (sort of like TestRequest) to simulate an http
 * request, reads a file implied by the request agi:... string, and
 * processes the file through the template runner.
 * The <agi...> template can be used to interact with * via
 * standard agi commands, and the web via the SetTemplate and namespaces.
 * The template output is discarded (if debug is enables, it is printed on
 * the server console); everything is done via side effect.
 * This allows us to interact with the ordinary template variables and
 * namespaces.
 * I'm still not sure how to deal with sessions, so we'll use a 
 * different one for each uniqueid in the agi request. (This is a bad idea
 * unless we delete completed sessions "by hand").
 *<p>(Implementation notes)<br>
 * This class implements 4 different threads:
 * - handler/init: to get the config params and start the listening socket
 * - The thread that listens and accepts connections from *
 * - the threads that handle the incoming agi requests
 * - the threads that do the template <agi...> stuff
 */

public class AsteriskAGIHandler
extends Template implements Handler, Runnable {
     Server server;	
     String prefix;
     Socket sock;		// socket from *
     ServerSocket listen;	// server socket

     // these are for tag_agi.

     HttpInputStream in = null;
     Request.HttpOutputStream out = null;

     public AsteriskAGIHandler() {
	 listen = null;
	 sock = null;
     }

     /**
      * Constructor to create the listener instance.
      */

     AsteriskAGIHandler(Server server, String prefix, ServerSocket listen) {
	 this.server = server;
	 this.prefix = prefix;
	 this.listen = listen;
	 sock = null;
     }

     /**
      * Constructor to create an AGI request handler thread.
      */

     AsteriskAGIHandler(Server server, String prefix, Socket sock) {
	 this.server = server;
	 this.prefix = prefix;
	 this.sock = sock;
	 listen = null;
     }

    /**
     * Start a Listening socket thread, and wait for AGI connections.
     */

    public boolean
    init(Server server, String prefix) {
	this.server = server;
	this.prefix = prefix;

	int port = 4573;  // default agi port
	try {
	    String str = server.props.getProperty(prefix + "port");
	    port = Integer.decode(str).intValue();
	} catch (Exception e) {}
	try {
	    ServerSocket listen = new ServerSocket(port);
	    new Thread(new AsteriskAGIHandler(server, prefix, listen)).start();
	} catch (IOException e) {
	    server.log(Server.LOG_ERROR, prefix, "Server socket failed: " + e);
	    return false;
	}

	// We should verify we are one of the templates, otherwise this
	// isn't useful.

	String tokens = server.props.getProperty(prefix + "templates", "");
	String token = prefix.substring(0, prefix.length()-1);
	if (tokens.indexOf(token) < 0) {
	    server.log(Server.LOG_WARNING, prefix,
		    "templates don't include: " + token);
	}
	return true;
    }

    /**
     * We don't handle any "normal" requests.
     * @return	always false
     */

    public boolean
    respond(Request request) throws IOException {
	return false;
    }

    /**
     *  Open the socket's streams at top of page.
     *  This will be used by the &lt;agi&gt; calls.
     */

    public boolean init(RewriteContext hr) {
	try {
	    in = new HttpInputStream(hr.request.sock.getInputStream());
	    out = new Request.HttpOutputStream(
	            hr.request.sock.getOutputStream());
	} catch (IOException e) {
	    hr.request.log(Server.LOG_WARNING, "Oops getting * stream: " + e);
	    return false;
	}
	return super.init(hr);
    }

    /**
     * Close the socket connection.
     */

    public boolean done(RewriteContext hr) {
	try {
	    in.close();
	    out.close();
	    hr.request.sock.close();
	} catch (IOException e) {
	    hr.request.log(Server.LOG_WARNING, "Oops closing * stream: " + e);
	}
	return true;
    }

    /**
     * Provide the 'agi' tag.
     * &lt;agi command="agi command"&gt;
     * The result is placed in "agi_result".
     * NOTE: the thread running this instance doesn't set 
     * any of the instance variables.  We get everything from "hr".
     */

    public void
    tag_agi(RewriteContext hr) {
	debug(hr);
	hr.killToken();
	String cmd = hr.get("command").trim();
	if (cmd==null) {
	    debug(hr, "No agi command!");
	    return;
	}

	String line;
	try {
	    hr.server.log(Server.LOG_DIAGNOSTIC, hr.prefix, "Write: (" +
		    cmd + ")");
	    writeLine(out, cmd);
	    line = in.readLine();
	    debug(hr, line);
	    hr.server.log(Server.LOG_DIAGNOSTIC, hr.prefix, "Read: (" +
		    line + ")");
	} catch (IOException e) {
	    line="Error: " + e;
	}
	hr.request.props.put("agi_result", line);
	return;
    }

    /**
     * Write a NL string as ASCII in one network packet.
     * Some older Asterisk's AGI interface requires each agi command
     * must be in its own network packet.  This tries to
     * insure that happens.
     */

    void writeLine(OutputStream out, String s) throws IOException {
	int len = s.length();
	byte[] b = new byte[len+1];
	for (int i=0;i<len;i++) {
	    b[i] = (byte) s.charAt(i);
	}
	b[len] = (byte) '\n';
	out.write(b);
	out.flush();
    }

    /**
     * Either start a listening socket or handle an AGI request.
     */

    public void run() {
	if (sock==null) {
	    run_listen();
	} else {
	    run_request();
	}
    }

    /**
     * Wait for a connection, accept it, and start a thread to handle it.
     */

    void run_listen() {
	while(true) {
	    try {
	        Socket sock = listen.accept();
	        new Thread(new AsteriskAGIHandler(server,prefix,sock)).start();
	    } catch (IOException e) {
		System.out.println("Accept oops: " + e);
		server.log(Server.LOG_WARNING, prefix, "accept error: " + e);
	    }
	}
    }

    /*
     * Gets called at each AGI request.
     * All the heavy lifting goes on here.
     */

    void run_request() {
	HttpInputStream in;
	MimeHeaders headers = new MimeHeaders();
	server.log(Server.LOG_LOG, prefix, "New AGI request");
	try {
	    in = new HttpInputStream(sock.getInputStream());
	    headers.read(in);
	    // in.close(); // the agi tag will open a new stream.
	} catch (IOException e) {
	    server.log(Server.LOG_WARNING, prefix,"Can't get AGI data");
	    try {sock.close();} catch (IOException es) {}
	    return;
	}
	server.log(Server.LOG_DIAGNOSTIC, prefix, "headers: " + headers);

	// add an agi_host

	headers.put("agi_host", sock.getInetAddress().getHostName());

	// At this point we've got all the headers
	// invent a url based on the agi command,
	// then read in and find the associated template file.

	headers.putIfNotPresent("agi_network_script", "");
        String path = FileHandler.urlToPath(headers.get("agi_network_script"));
	String id = headers.get("agi_uniqueid");
	if (id == null) {
	    id = "noid";
	    server.log(Server.LOG_WARNING, prefix, "No uniqueID from *");
	}

	String root = server.props.getProperty(prefix + "root",
		server.props.getProperty("root", "."));
	File file = new File(root, path);

	// We don't care about mime types here, so just go for it!

	String content = null;	// the template to process
        try {
	    FileInputStream fin = new FileInputStream(file);
	    byte[] buf = new byte[fin.available()];
	    fin.read(buf);
	    fin.close();
	    content = new String(buf);
	} catch (IOException e) {
	    server.log(Server.LOG_LOG, prefix, "no template! " + e);
	    try {sock.close();} catch (IOException es) {}
	    return;
	}

	// Make a dummy request object, and populate it properly.
	// We should create our own request object, but the one in
	// "test" is close enough, so we'll use it instead.

	String tokens = server.props.getProperty(prefix + "templates",
		"sunlabs.brazil.asterisk.AsteriskAGIHandler.class");
	TemplateRunner runner;
	try {
	    runner = new TemplateRunner(server, prefix, tokens);
	} catch (ClassCastException e) {
	    server.log(Server.LOG_ERROR, e.getMessage(), "not a Template");
	    try {sock.close();} catch (IOException es) {}
	    return;
	} catch (ClassNotFoundException e) {
	    server.log(Server.LOG_ERROR, e.getMessage(), "unknown class");
	    try {sock.close();} catch (IOException es) {}
	    return;
	}

	String req = "GET /" + headers.get("agi_network_script") +
		" HTTP/1.0\r\n\r\n";

	Request request = null;
	try {
	    request = new TestRequest(server, req);
	} catch (IOException e) {} // TestRe't doesn't thow one.
	request.sock = sock;  // our connection to *

	for (int i = 0; i < headers.size(); i++) {
	    request.props.put(headers.getKey(i), headers.get(i));
        }
	request.props.put("SessionID", prefix + id);

	// the "runner" will create a new instance of us (as needed)
	// for this id.

	String result = runner.process(request,	content, id);
	server.log(Server.LOG_DIAGNOSTIC, prefix, "result {\n" + result+ "}");
	return;
    }
}
