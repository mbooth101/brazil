/*
 * ChainSawHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.
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
 * Contributor(s): cstevens, suhler.
 *
 * Version:  2.1
 * Created by suhler on 99/04/08
 * Last modified by suhler on 02/10/01 16:36:27
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:36:27 (suhler)
 *   version change
 *
 * 1.16 02/03/06-11:14:43 (suhler)
 *   fixed output format as per rinaldos instructions
 *
 * 1.15 00/12/11-18:45:10 (suhler)
 *   typo
 *   .
 *
 * 1.14 00/12/11-13:27:24 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.13 00/12/08-16:45:22 (suhler)
 *   doc fixes
 *   .
 *
 * 1.12 00/12/05-13:11:13 (suhler)
 *   changed semantics of bytesWritten
 *
 * 1.11 00/06/29-10:45:12 (suhler)
 *   Remove private outputStream.
 *   Counting capability is now in request.HttpOutputStream
 *
 * 1.10 00/06/26-10:13:11 (suhler)
 *   Make compatible (I hope) with FilterHandler
 *
 * 1.9 00/04/20-11:50:08 (cstevens)
 *   copyright.
 *
 * 1.8 00/03/10-17:03:42 (cstevens)
 *   Eliminated DataOutputStream from Request.java.
 *
 * 1.7 99/10/26-18:52:14 (cstevens)
 *   header must be spelled as "Referer".
 *
 * 1.6 99/10/26-17:10:46 (cstevens)
 *   Get rid of public variables Request.server and Request.sock:
 *   A. In all cases, Request.server was not necessary; it was mainly used for
 *   constructing the absolute URL for a redirect, so Request.redirect() was
 *   rewritten to take an absolute or relative URL and do the right thing.
 *   B. Request.sock was changed to Request.getSock(); it is still rarely used
 *   for diagnostics and logging (e.g., ChainSawHandler).
 *
 * 1.5 99/10/01-11:26:30 (cstevens)
 *   Change logging to show prefix of Handler generating the log message.
 *
 * 1.4 99/09/15-14:40:20 (cstevens)
 *   Rewritign http server to make it easier to proxy requests.
 *
 * 1.3 99/05/06-15:22:37 (suhler)
 *   lint (for jikes)
 *
 * 1.2 99/04/09-12:43:45 (suhler)
 *   fixed wild-carded imports
 *   allow for buffering output
 *   permit log file rotations
 *
 * 1.2 99/04/08-09:51:42 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/ChainSawHandler.java
 *
 * 1.1 99/04/08-09:51:41 (suhler)
 *   date and time created 99/04/08 09:51:41 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat; 
import java.util.Date; 
import sunlabs.brazil.server.ChainHandler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

/**
 * Variant of the chain handler for doing standard logging.
 * Don't use on fine furniture.
 * <p>
 * Output is a variant of the common logfile format.
 * The common logfile format is as follows: 
 * <pre>
 * remotehost rfc931 authuser [date] "request" status bytes
 * </pre>
 * <dl>
 * <dt>remotehost
 * <dd>Remote hostname (or IP number if DNS hostname is not available, or if DNSLookup is Off. 
 * <dt>rfc931
 * <dd>The remote logname of the user. 
 * <dt>authuser
 * <dd>The username as which the user has authenticated himself. 
 * <dt>[date]
 * <dd>Date and time of the request. 
 * <dt>"request"
 * <dd>The request line exactly as it came from the client. 
 * <dt>status
 * <dd>The HTTP status code returned to the client. 
 * <dt>bytes
 * <dd>The content-length of the document transferred. 
 * <dt> "referrer" (optional)
 * <dd> the referring url
 * <dt> "user agent" (optional)
 * <dd> "The user agent making the request
 * </dl>
 * <p>
 * Additional Configuration options:
 * <dl class=props>
 * <dt> logFile <dd> The name of the file to write the logs to.
 * <dt> flush   <dd> The number of requests between flushes to the file
 * </dl>
 * If the logFile is removed, the server creates a new one.
 * Thus logs may be truncated by periodically moving them to
 * another name (at least on unix).
 * <p>
 * See the {@link LogHandler} handler for generating logs whose
 * contents are configurable.
 *
 * @author		Stephen Uhler
 * @version		2.1, 02/10/01
 */

public class ChainSawHandler extends ChainHandler {
    static public final String LOG = "logFile";
    static public final String FLUSH = "flush";
    static public final int BUFSIZE = 4096;
    static SimpleDateFormat sdf;	// standard log format date string
    DataOutputStream log; 	// where to log the output to
    File file;
    int count = 0;		// count flushing interval
    int flush = 1;		// how may requests to flush at

    static {
       sdf = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z"); 
    }

    public boolean
    init(Server server, String prefix) {
	String logFile = server.props.getProperty(prefix + LOG,
		server.hostName + "-" + server.listen.getLocalPort() + ".log");
	try {
	    flush = Integer.parseInt(
	            server.props.getProperty(prefix + FLUSH, "1"));
	} catch (NumberFormatException e) {
	// ignore
	}

	try {
	    file = new File(logFile);
	    log = new DataOutputStream(new BufferedOutputStream(
		    new FileOutputStream(file),BUFSIZE));
	} catch (IOException e) {
	    server.log(Server.LOG_WARNING, prefix, e.toString());
	    return false;
	}
	server.log(Server.LOG_DIAGNOSTIC, prefix, "Log file: " + logFile);
	return super.init(server, prefix);
    }

    /**
     * Run the chain-handler, counting the # of bytes of output generated
     * by its chained handlers.
     */

    public boolean
    respond(Request request) throws IOException {
    	String req = "\"" + request.method + " " + request.url + " " +
		request.protocol + "\"";
	String now = sdf.format(new Date());
    	boolean result = false;
	IOException oops = null;

    	try {
	    result = super.respond(request);
	} catch (IOException e) {
	    oops = e;
	}
	int code =  200;
	if (!result) {
	    code = 404;
	}
	request.out.flush();
    	
	String refer;
	if (request.headers.get("Referer") != null) {
	    refer = "\"" + request.headers.get("Referer") + "\"";
	} else {
	    refer = "-";
	}

	String agent;
	if (request.headers.get("User-Agent") != null) {
	    agent = "\"" + request.headers.get("User-Agent") + "\"";
	} else {
	    agent = "-";
	}

    	count = request.out.bytesWritten;
    	String msg = request.getSocket().getInetAddress().getHostAddress() +
    		" - - [" + now + "]" + " " + req + " " +
    		code + " " + count + " " + refer + " " + agent + "\n";

        /*
         * Write to the log file.  If there's a problem, try to open
	 * the file again.
         */
        
        if (!file.exists()) {
	    log.flush();
	    request.log(Server.LOG_WARNING, "Log file went away!");
	    log = new DataOutputStream(new BufferedOutputStream(
	    	    new FileOutputStream(file), BUFSIZE));
	    count = 0;
        }

	log.writeBytes(msg);
	request.log(Server.LOG_DIAGNOSTIC, msg);
	if (count++ >= flush) {
	    log.flush();
	    count = 0;
	}
    	if (oops != null) {
	    throw oops;
	}
    	return result;
    }
}
