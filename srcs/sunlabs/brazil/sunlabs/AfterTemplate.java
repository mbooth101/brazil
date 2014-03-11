/*
 * AfterTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2006 Sun Microsystems, Inc.
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
 * Created by suhler on 06/12/18
 * Last modified by suhler on 06/12/18 14:46:42
 *
 * Version Histories:
 *
 * 1.2 06/12/18-14:46:42 (suhler)
 *   fixed imports and docs
 *
 * 1.2 06/12/18-14:09:24 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/AfterTemplate.java
 *
 * 1.1 06/12/18-14:09:23 (suhler)
 *   date and time created 06/12/18 14:09:23 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Enumeration;
import sunlabs.brazil.properties.PropertiesList;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.template.TemplateRunner;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.http.MimeHeaders;

/**
 * Template for running markup after a while.  all the markup between
 * the &lt;after&gt; and matching &lt;/after&gt; tags are remembered.  After
 * "ms" msec have elapsed (defaults to 100), the remembered markup
 * is processed using (almost) the rewrite context that was in effect
 * when the &lt;after&gt; tag was recognized. A copy of the request
 * properties is made, but all other property lists are shared. 
 * <ul>
 * <li> &lt;after ms=n [eval=true|false]&gt;
 * </ul>
 * If "eval" is specified, then ${..} substitutions are performed on all
 * content before it is processed.  The &lt;after&gt; tags can nest.
 * The output from the processing is discarded, this element is used
 * for it's side effects.
 */

public class AfterTemplate extends Template {

    public void
    tag_after(RewriteContext hr) {
	boolean eval = hr.isTrue("eval");
	int ms=100;
	try {
	    ms = Integer.decode(hr.get("ms", "100")).intValue();
	} catch (Exception e) {}

        debug(hr);
        hr.killToken();

        String body = hr.snarfTillClose();

	if (eval) {
	    body = Format.subst(hr.request.props, body, true);
	}

	// Make a dummy request object that looks like this one

	AfterRunner ar = new AfterRunner(hr, body, ms);
	hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, "Scheduling in " +
		ms + "ms");
	ar.start();
    }

    static class AfterRunner extends Thread {
	Request request=null;	// the "dummy" request object.
	String prefix;
	TemplateRunner runner;	// the template processor
	String content;		// the content to process
	String id;		// the sessionID to use
	int ms;	// how long to wait before running
	boolean cancelled = false;	// some day XXX

	public AfterRunner(RewriteContext hr, String content, int ms) {
	    this.content = content;
	    this.ms = ms;
	    prefix = hr.prefix;
	    try {
		request = new CloneRequest(hr.request);
	    } catch (IOException e) {} // TestRe't doesn't thow one.

	    String tokens = hr.request.props.getProperty(hr.templatePrefix +
		"templates");
	    try {
		runner = new TemplateRunner(hr.server, hr.templatePrefix,
			tokens);
	    } catch (ClassCastException e) {
		request.log(Server.LOG_WARNING, prefix, e.getMessage());
		return;
	    } catch (ClassNotFoundException e) {
		request.log(Server.LOG_WARNING, prefix, e.getMessage());
		return;
	    }
	    id = request.props.getProperty("SessionID");
	}

	public void run () {
	    try {
		sleep(ms);
	    } catch (InterruptedException e) {}
	    if (!cancelled) {
		request.log(Server.LOG_DIAGNOSTIC, prefix,
			"processing {" + content + "}");
		String result = runner.process(request, content, id);
		request.log(Server.LOG_DIAGNOSTIC, prefix,
			"result {" + result + "}");
	    } else {
		request.log(Server.LOG_DIAGNOSTIC, prefix, "cancelled");
	    }
	}

	public void cancel() {
	    cancelled=true;
	    interrupt();
	}
    }

    /**
     * Create a snapshot of a completed request object for later use.
     * We only need to create the portions that will be used by the
     * template runner, which is used by the templates.
     */

    static class CloneRequest extends Request {

	public CloneRequest(Request request) throws IOException {
	    super(request.server, new FakeSocket(request.getSocket()));

	    props = new PropertiesList();
	    Enumeration en = request.props.keys();
	    while(en.hasMoreElements()) {
		String key = (String) en.nextElement();
		props.put(key, request.props.get(key));
	    }
	    props.addBefore(request.props.getNext());

	    serverProps = request.serverProps;
	    method=request.method;
	    url = request.url;
	    query = request.query;
	    protocol = request.protocol;
	    version = request.version;
	    headers = new MimeHeaders();
	    request.headers.copyTo(headers);
	    if (request.postData != null) {
		postData = new byte[request.postData.length];
		System.arraycopy(request.postData, 0, postData, 0,
			postData.length);
	    } else {
		postData = null;
	    }
	    serverProtocol = request.serverProtocol;
	    startMillis = request.startMillis;
	}
    }

    /**
     * Create the pieces of a socket I need for a request
     */

    static class FakeSocket extends Socket {
	InputStream in;
	ByteArrayOutputStream out;
	int port;
	int localPort;
	InetAddress address;

	public FakeSocket(Socket socket) {
	    in = new StringBufferInputStream("");
	    out = new ByteArrayOutputStream();
	    port = socket.getPort();
	    localPort = socket.getLocalPort();
	    address = socket.getInetAddress();
	}

	public void close() throws IOException {
	    in.close();
	    out.close();
	}

        public InputStream getInputStream() {
            return in;
        }

        public OutputStream getOutputStream() {
            return out;
        }

        public int getPort() {
            return port;
	}

        public int getLocalPort() {
            return localPort;
	}
	
	public InetAddress getInetAddress() {
	    return address;
	}

	public String toString() {
	    return "Fake socket " + port;
	}
    }
}
