/*
 * LogHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2005 Sun Microsystems, Inc.
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
 * Version:  2.2
 * Created by suhler on 00/12/05
 * Last modified by suhler on 05/10/19 14:36:17
 *
 * Version Histories:
 *
 * 2.2 05/10/19-14:36:17 (suhler)
 *   doc fixes
 *
 * 2.1 02/10/01-16:36:30 (suhler)
 *   version change
 *
 * 1.8 02/07/24-10:45:18 (suhler)
 *   doc updates
 *
 * 1.7 02/05/01-11:21:29 (suhler)
 *   fix sccs version info
 *
 * 1.6 01/01/14-14:51:25 (suhler)
 *   make fields public to enable scripting
 *
 * 1.5 00/12/11-20:23:53 (suhler)
 *   doc typs
 *
 * 1.4 00/12/11-13:28:55 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.3 00/12/08-16:46:08 (suhler)
 *   doc fixes
 *   .
 *
 * 1.2 00/12/05-13:11:24 (suhler)
 *   added title
 *
 * 1.2 00/12/05-12:40:18 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/LogHandler.java
 *
 * 1.1 00/12/05-12:40:17 (suhler)
 *   date and time created 00/12/05 12:40:17 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import sunlabs.brazil.server.ChainHandler;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Format;

/**
 * Handler for logging information about requests.
 * Wraps another handler, and logs information
 * about each HTTP request to a file.
 * <p>
 * Request properties:
 * <dl class=props>
 * <dt>handler
 * <dd>The name of the handler to wrap.  This can either be the token
 *     for the class, or the class name itself.
 * <dt>logFile
 * <dd>The name of the file to log the output to.  If the file already exists, 
 *     data is appended to it.  If the file is removed, a new one is created.
 *     If no name is specified, one is invented that contains the name and
 *     port of the server.  Unless an absolute path is specified, 
 *     the log file is placed in the current directory.
 * <dt>flush
 * <dd>The number of lines of logging output that may be buffered in memory
 *     before being written out to the log file.  default to 25.
 * <dt>format
 * <dd>The format of the output string. Embedded strings of the form "%X"
 *     are replaced, based on the following values for "X":
 * <ul>
 * <li>% <br> A single "%"
 * <li>b <br> Bytes written to the client for this request.
 * <li>d <br> Time to service this request (ms).
 * <li>i <br> Client ip address.
 * <li>m <br> Request method (GET, POST, etc)
 * <li>M <br> Memory utilization (%).
 * <li>q <br> query string (if any)
 * <li>r <br> Requests used for this connection.
 * <li>s <br> HTTP result code.
 * <li>t <br> TimeStamp (ms since epoch).
 * <li>T <br> Number of active threads.
 * <li>u <br> URL for this request.
 * <li>v <br> HTTP protocol version (10 or 11).
 * </ul>
 * Defaults to "%u;%t:%d:%b".
 * <dt>props
 * <dd>If specified This string is tacked onto the end of the "format"
 * string.  Entries in the Request Properties may be included using
 * ${...} substitutions.
 * <dt>headers
 * <dd>If specified This string is tacked onto the end of the "props"
 * string.  Entries in the HTTPrequest headers may be included using
 * ${...} substitutions.
 * <dt>title
 * <dd>if present, this is output as the first line of the file
 * </dl>
 * <p>
 * See the {@link ChainSawHandler} for generating standard format
 * log files.
 *
 * @author      Stephen Uhler
 * @version     @(#)LogHandler.java	2.2
 */

public class LogHandler implements Handler {
    Server server;
    public Handler handler;		// the handler to log
    public String props;		// a substitutable string for props
    public String headers;		// a substitutable string for headers
    public String format;		// substitute misc stuff
    public String title;		// title the output file
    String handlerName;			// the name of the handler

    public int flush;			// log flush interval
    public File file;
    int count = 0;			// count flushing interval
    DataOutputStream log; 		// where to log the output to

    public boolean
    init(Server server, String prefix) {
        this.server = server;
	props = server.props.getProperty(prefix + "props", "");
	title = server.props.getProperty(prefix + "title");
	headers = server.props.getProperty(prefix + "headers", "");
	format = server.props.getProperty(prefix + "format", "%u;%t:%d:%b");
	handlerName = server.props.getProperty(prefix + "handler");
        handler=ChainHandler.initHandler(server, prefix, handlerName);

	String logFile = server.props.getProperty(prefix + "logFile",
		server.hostName + "-" + server.listen.getLocalPort() + ".log");
	try {
	    flush = Integer.parseInt(
	            server.props.getProperty(prefix + "flush", "25"));
	} catch (NumberFormatException e) {}

	try {
	    file = new File(logFile);
	    log = new DataOutputStream(new BufferedOutputStream(
		    new FileOutputStream(logFile, true)));
	    if (title != null) {
		log.writeBytes(title + "\n");
	    }
	} catch (IOException e) {
	    server.log(Server.LOG_WARNING, prefix, e.toString());
	    return false;
	}
        return (handler != null);
    }

    /**
     * Dispatch the request to the handler.  Log information if
     * dispatched handler returns true.
     */

    public boolean
    respond(Request request) throws IOException {
	if (!handler.respond(request)) {
	    return false;
	}
	long duration = System.currentTimeMillis() - request.startMillis;
	String msg = 
	        subst(request, format, duration) +
		Format.subst(request.props, props) +
		Format.subst(request.headers, headers) + "\n";

        /*
         * Write to the log file.  If there's a problem, try to open
	 * the file again.
         */
        
        if (!file.exists()) {
	    log.flush();
	    request.log(Server.LOG_WARNING, "Log file went away!");
	    log = new DataOutputStream(new BufferedOutputStream(
	    	    new FileOutputStream(file)));
	    if (title != null) {
		log.writeBytes(title + "\n");
	    }
	    count = 0;
        }

	log.writeBytes(msg);
	request.log(Server.LOG_DIAGNOSTIC, msg);
	if (count++ >= flush) {
	    log.flush();
	    count = 0;
	}
	return true;
    }

    /**
     * Compute built-in strings by mapping %n's to values
     */

    static boolean
    format(Request request, char c, StringBuffer buff, long duration) {
	switch(c) {
	case 'u':	// URL
	    buff.append(request.url);
	    break;
	case 't':	// timestamp
	    buff.append(request.startMillis);
	    break;
	case 'd':	// service time (ms)
	    buff.append(duration);
	    break;
	case 's':	// result status
	    buff.append(request.getStatus());
	    break;
	case 'b':	// bytes written
	    buff.append(request.out.bytesWritten);
	    break;
	case 'i':	// client ip address
	    buff.append(request.getSocket().getInetAddress().getHostAddress());
	    break;
	case 'r':	// socket reuse count
	    buff.append(request.getReuseCount());
	    break;
	case 'T':	// active threads
	    buff.append(Thread.activeCount());
	    break;
	case 'M':	// memory utilization
	    long currentMem = Runtime.getRuntime().freeMemory();
	    long totalMem = Runtime.getRuntime().totalMemory();
	    buff.append(currentMem * 100 /totalMem);
	    break;
	case 'm':	// method
	    buff.append(request.method);
	    break;
	case 'q':	// query string
	    buff.append(request.query);
	    break;
	case 'v':	// protocol version (10 or 11)
	    buff.append(request.version);
	    break;
	case '%':	// insert a %
	    buff.append("%");
	    break;
	default:
	    return false;
	}
	return true;
    }

    /**
     * Format a string.
     * Replace %X constructs.
     */

    public static String
    subst(Request request, String format, long duration) {
	int i = format.indexOf('%');
	if (i < 0) {
	    return format;
	}
	int len = format.length();
	StringBuffer result = new StringBuffer(format.substring(0, i));
	for ( ; i < len; i++) {
	    try {
		char ch = format.charAt(i);
		if (ch == '%') {
		    i++;
		    format(request, format.charAt(i), result, duration);
		} else {
		    result.append(ch);
		}
	    } catch (IndexOutOfBoundsException e) {
		/* Ignore malformed "%" sequences */
	    } catch (NullPointerException e) {
		/* Ignore non-existent properties or array */
	    }
	}
	return result.toString();
    }
}
