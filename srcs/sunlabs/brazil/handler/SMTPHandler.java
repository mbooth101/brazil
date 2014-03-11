/*
 * SMTPHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2007 Sun Microsystems, Inc.
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
 * Version:  2.9
 * Created by suhler on 00/01/11
 * Last modified by suhler on 07/01/29 15:36:24
 *
 * Version Histories:
 *
 * 2.9 07/01/29-15:36:24 (suhler)
 *   remove unused imports
 *
 * 2.8 06/11/13-15:04:29 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.7 06/01/06-16:41:31 (suhler)
 *   bug fix: always pass in Server, for detailed debugging, rework <param> stuff
 *
 * 2.6 05/12/23-19:53:32 (suhler)
 *   eliminate bogus warning
 *
 * 2.5 05/01/11-09:56:31 (suhler)
 *   don't look for param tags outside of <sendmail>  ... </sendmail>
 *
 * 2.4 04/10/28-12:47:51 (suhler)
 *   doc fixes
 *
 * 2.3 04/10/27-13:33:45 (suhler)
 *   - better documentation
 *   - better diagnostics
 *
 * 2.2 03/08/01-16:18:11 (suhler)
 *   fixes for javadoc
 *
 * 2.1 02/10/01-16:36:28 (suhler)
 *   version change
 *
 * 1.18 02/05/01-11:22:02 (suhler)
 *   fix sccs version info
 *
 * 1.17 02/04/25-14:08:31 (suhler)
 *   don't re-check server connection when used as a template
 *
 * 1.16 02/03/06-09:15:41 (suhler)
 *   don't "ping" email server on each template page
 *
 * 1.15 01/11/21-11:42:46 (suhler)
 *   doc fixes
 *
 * 1.14 01/08/14-16:37:57 (suhler)
 *   doc lint
 *
 * 1.13 01/07/23-15:52:17 (suhler)
 *   Use StringMaps instead of Hashtables to preserver header order.
 *
 * 1.12 01/07/20-12:05:51 (suhler)
 *   Stupid typo (that any reasonable test woul'a caught).
 *
 * 1.11 01/07/20-11:32:17 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.10 01/07/17-14:16:04 (suhler)
 *   use MatchUrl
 *
 * 1.9 01/07/16-16:44:58 (suhler)
 *   use new template convenience methods
 *
 * 1.8 01/07/12-14:25:12 (suhler)
 *   added the capability to do:
 *   <sendmail ...>
 *   <param name=.. value=...> ...
 *   </sendmail>
 *   to add additional email headers.
 *
 * 1.7 01/06/29-11:24:41 (suhler)
 *   May also function as a template for sending email
 *
 * 1.6 01/01/14-14:51:10 (suhler)
 *   stupif bug
 *
 * 1.5 00/12/11-13:28:31 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.4 00/07/07-17:01:47 (suhler)
 *   remove System.out.println(s)
 *
 * 1.3 00/05/31-13:47:35 (suhler)
 *   doc cleanup
 *
 * 1.2 00/04/20-11:50:27 (cstevens)
 *   copyright.
 *
 * 1.2 70/01/01-00:00:02 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/SMTPHandler.java
 *
 * 1.1 00/01/11-10:41:08 (suhler)
 *   date and time created 00/01/11 10:41:08 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.StringTokenizer;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.util.StringMap;

import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;

/**
 * Handler (or template) for Sending an email message via SMTP.
 * The following server properties are used:
 * <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URL that triggers this handler.
 * (See {@link MatchString}).
 * <dt>host	<dd> The mail host (e.g. listening on the SMTP port).
 *		Defaults to "localhost".
 * </dl>
 * <h3>
 * Template to send an email message via SMTP.
 * </h3>
 * The &lt;sendmail&gt; tag looks for the attributes:
 * <dl class=attributes>
 * <dt>to	<dd>The message recipient(s) [required]
 *		Recipients are be delimited by one or more
 *		commas (,), spaces or tabs.
 *		This is the actual destination list.  If "To:" headers are
 *		desired, they should be incorporated using
 *		<code>&lt;param&gt;</code> tags (see below).
 * <dt>from <dd>The originator [required]
 * <dt>body <dd>The text message [optional]
 * <dt>subject <dd>The subject [optional]
 * </dl>
 * If an error occurred communicating with the smtp server, 
 * the property <code>[prefix].error</code> will contain the
 * error message.
 * <p> 
 * There are 2 ways of using the <code>sendmail</code> template:
 * <ol>
 * <li>&lt;sendmail from=... to=... body=... subject=... /&gt;
 * <li>&lt;sendmail from=... to=... body=... subject=... &gt;
 *     <br>&lt;param name=... value=... &gt;
 *     <br> ...
 *     <br>&lt;param name=... value=... &gt;
 *     &lt;/sendmail&gt;
 * </ol>
 * The second method is useful when additonal email headers are
 * required.  Each <code>param</code> tag adds an additional header
 * to the email message.  There may be multiple headers of the same <code>
 * name</code>, and their order is preserved.
 * <p>
 * When a singleton tag is used,
 * the <code>To:</code> header is filled in to match the actual
 * recipients.
 * If you need to specify <code>cc</code>, <code>bcc</code>, or other
 * mail headers  use <code>param</code> tags.
 * <p>
 * Note:<br>
 * The <code>to</code> attribute, which is required,
 * specifies the actual recipients.  When <code>to</code> is specified
 * as part of a <code>param</code> tag, it is the recipient list presented
 * to the email recipient, which may have nothing to do wih the actual
 * recipients.  In the singleton case, they are made the same.
 * <h3>
 * Send an email message based on the query data
 * </h3>
 * <p>
 * Query parameters:
 * <dl>
 * <dt>to		<dd> To address.  Multible addresses should be delimited
 *			     by spaces or commas.
 * <dt>from		<dd> From address
 * <dt>message	<dd> Message
 * <dt>subject	<dd> Subject
 * </dl>
 * Either the message or subject may be null, but not both.
 *
 * @author		Stephen Uhler
 * @version		@(#)SMTPHandler.java	2.9
 */

public class SMTPHandler extends Template implements Handler {
    String mailHost;	// host with smtp listener
    String me;		// my host name
    MatchString isMine;            // check for matching url
    boolean checkConnection = true;
    boolean inSendmail;	// are we in the scope of a sendmail tag?

    public boolean
    init(Server server, String prefix) {
	mailHost = server.props.getProperty(prefix + "host", "localhost");
	me = server.hostName;
	isMine = new MatchString(prefix, server.props);
	inSendmail=false;

	/*
	 * Make sure we can communicate with the host
	 */

	if (checkConnection) {
	    try {
		Socket sock = new Socket(mailHost,25);
		DataInputStream in = new DataInputStream(
			new BufferedInputStream(sock.getInputStream()));
		String line = in.readLine();
		in.close();
		sock.close();
		server.log(Server.LOG_DIAGNOSTIC, prefix, line);
		if (line.startsWith("220")) {
		    return true;
		} else {
		    return false;
		}
	    } catch (IOException e) {
		server.log(Server.LOG_WARNING, prefix,
			"Can't contanct SMTP server: " + e);
		return false;
	    }
	} else {
	    return true;
	}
    }

    public boolean
    respond(Request request) throws IOException {
        if (!isMine.match(request.url)) {
            return false;
	}
	Hashtable data = request.getQueryData();
	Hashtable subj = new Hashtable(1);
	String to = (String) data.get("to");
	String from = (String) data.get("from");
	String subject = (String) data.get("subject");
	String message = (String) data.get("message");
	if (to==null || from==null || (subject==null && message==null)) {
	     request.sendError(400, "Missing query parameters");
	     return true;
	}
	if (subject == null) {
	    subject = "(none)";
	}
	subj.put("subject", subject);
	smtp(me, mailHost, from, to, message, headers, request.server);
	request.props.put(mailHost, "sent"); // ??
	return false;
    }

    String to;
    String from;
    String body;
    String subject;
    StringMap headers;
    boolean sent;

    public boolean init(RewriteContext hr) {
	checkConnection = false;
        sent = true;
	headers = new StringMap();
	super.init(hr);
	mailHost=hr.request.props.getProperty(hr.prefix + "host", "localhost");
	me = hr.server.hostName;
	return true;
    }

    /**
     * set-up an email message for sending.  If this is a singleton
     * tag, then send the mail.
     */

    public void
    tag_sendmail(RewriteContext hr) {

	inSendmail=true;
	debug(hr);
	hr.killToken();
	to = hr.get("to");
	from = hr.get("from");
	body = hr.get("body");
	subject = hr.get("subject");
	headers.clear();

	if (hr.isSingleton()) {
	    sent = true;
	    if (subject != null) {
		headers.add("Subject", subject);
	    }
	    headers.add("To", to);		// add to recipient list
	    try {
	        smtp(me, mailHost, from, to, body, headers, hr.server);
	    } catch (IOException e) {
	        hr.request.props.put(hr.prefix + ".error", "Can't send mail: " +
			    e.getMessage());
	    }
	} else {
	    sent = false;
	}
    }

    /**
     * If we haven't sent the mail yet - send it.
     */

    public void
    tag_slash_sendmail(RewriteContext hr) {
	inSendmail=false;
	debug(hr);
	hr.killToken();
        if (!sent) {
	    sent = true;
	    if (subject != null) {
		headers.add("Subject", subject);
	    }
	    try {
	        smtp(me, mailHost, from, to, body, headers, hr.server);
	    } catch (IOException e) {
	        hr.request.props.put(hr.prefix + ".error", "Can't send mail: " +
			    e.getMessage());
	    }
        } else {
	  debug(hr, "Checking: mail already sent!");
        }
    }

    /**
     * If we run off the end of the page, but there is email pending
     * to be sent, send it anyway.
     */

    public boolean
    done(RewriteContext hr) {
	if (!sent) {
	    tag_slash_sendmail(hr);
	}
	return true;
    }

    /**
     * Add an additional email header.
     * Headers are added in the order in which they are processed.
     * The special header "body" changes the body value instead.
     * <p>
     * look for: <code>&lt;param name="..." value="...&gt;.</code>
     * Which will add the email header: <code>name:&nbsp;value</code>.
     * <p>
     * The name "body" is special, and will cause the email body to be replaced.
     */

    public void tag_param(RewriteContext hr) {
	if (!inSendmail) {
	    return;
	}
	debug(hr);
	hr.killToken();
        String name = hr.get("name");
        String value = hr.get("value");
	if ("body".equals(name)) {
	    body = value;
	} else {
	    headers.add(name, value);
	}
    }

    /**
     * Send an email message via smtp - simple version.
     * @param fromHost	the hostname of the sender (may be null)
     * @param smtpHost	the SMTP host (whose smtp daemon to contact)
     * @param from	who the email is from
     * @param to	a space delimited list of recepients
     * @param body	The message body
     * @param headers	message headers (may be null)
     * @throws		IOException, if any errors occured (yuk)
     *
     * Either the headers Or body may be null, but not both.
     */

    public static void
    smtp(String fromHost, String smtpHost, String from, String to,
	    String body, StringMap headers) throws IOException {
	smtp(fromHost, smtpHost, from, to, body, headers, null);
    }
    public static void
    smtp(String fromHost, String smtpHost, String from, String to,
	    String body, StringMap headers, Server server) throws IOException {
	Socket sock = null;
	DataInputStream in = null;
	DataOutputStream out = null;
	if (server != null) {
	    server.log(Server.LOG_DIAGNOSTIC, "smtp", "sending email...");
	}
	try {
	    sock = new Socket(smtpHost,25);
	    in = new DataInputStream(
		    new BufferedInputStream(sock.getInputStream()));
	    out = new DataOutputStream(
		    new BufferedOutputStream(sock.getOutputStream()));

	    /* Do the protocol - in line */

	    send(in, out, null, "220", server);
	    if (fromHost != null) {
		send(in, out, "HELO " + fromHost, "250", server);
	    }
	    send(in, out, "MAIL FROM:<" + from + ">", "250", server);
	    StringTokenizer st = new StringTokenizer(to, ", \t");
	    while (st.hasMoreTokens()) {
		send(in, out, "RCPT TO:<" + st.nextToken() + ">", "250", server);
	    }
	    send(in, out, "DATA", "354", server);
	    if (headers != null) {
	        for (int i = 0; i < headers.size(); i++) {
		    String header = headers.getKey(i) + ": " + headers.get(i);
		    out.writeBytes(header + "\r\n");
		    if (server != null) {
		        server.log(Server.LOG_DIAGNOSTIC, " -> ", header);
		    }
		}
	    }
	    out.writeBytes("\r\n");
	    if (body != null) { 
		out.writeBytes(body);
		if (server != null) {
		    server.log(Server.LOG_DIAGNOSTIC, " -> ", body);
		}
	    }
	    out.writeBytes("\r\n.\r\nQUIT\r\n");
	    out.flush();
	} finally {
	    try {
		in.close();
		out.close();
		sock.close();
	    } catch (Exception e) {}
	}
    }

    /**
     * Do a transaction with SMTP server
     */

    private static void
    send(DataInputStream in, DataOutputStream out, String msg, String expect,
	Server server) throws IOException {
	if (msg != null) {
	    out.writeBytes(msg + "\r\n");
	    out.flush();
	    if (server != null) {
	        server.log(Server.LOG_DIAGNOSTIC, " -> ", msg);
	    }
	}
	String line = in.readLine();
	if (server != null) {
	    server.log(Server.LOG_DIAGNOSTIC, " <- ", line);
	}
	if (!line.startsWith(expect)) {
	    if (server != null) {
	        server.log(Server.LOG_DIAGNOSTIC, "smtp",
			"Oops, should start with: " + expect);
	    }
	    throw new IOException(line);
	}
    }
}
