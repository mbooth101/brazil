/*
 * VelocityFilter.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2002-2004 Sun Microsystems, Inc.
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
 * Version:  2.3
 * Created by drach on 02/04/19
 * Last modified by suhler on 04/11/30 15:19:46
 *
 * Version Histories:
 *
 * 2.3 04/11/30-15:19:46 (suhler)
 *   fixed sccs version string
 *
 * 2.2 03/07/28-09:25:38 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.5.1.1 03/07/15-09:12:31 (suhler)
 *   doc fixes for pdf version of the manual
 *
 * 2.1 02/10/01-16:39:56 (suhler)
 *   version change
 *
 * 1.5 02/05/29-16:29:51 (suhler)
 *   added docs
 *
 * 1.4 02/05/17-09:43:43 (drach)
 *   Fix so it compiles with 1.1
 *
 * 1.3 02/05/07-07:05:09 (drach)
 *   Add methods to allow read only access to Server and Request fields
 *
 * 1.2 02/05/02-14:56:42 (drach)
 *   Major modifications.
 *
 * 1.2 02/04/19-15:25:05 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 velocity/VelocityFilter.java
 *
 * 1.1 02/04/19-15:25:04 (drach)
 *   date and time created 02/04/19 15:25:04 by drach
 *
 */

package sunlabs.brazil.velocity;

import java.io.StringWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Hashtable;

import sunlabs.brazil.filter.Filter;
import sunlabs.brazil.handler.ResourceHandler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.http.MimeHeaders;

import bsh.Interpreter;
import bsh.EvalError;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.FieldMethodizer;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;

/**
 * A filter for processing markup that is a Velocity template.  The filter
 * will "merge" the content with a BeanShell script to produce output content.
 * <p>
 * The following server properties are used:
 * <dl class=props>
 * <dt>script	<dd>The name of a BeanShell script file.
 * <dt>session  <dd>The name of the propery containing the
 *     session.  The default session, if none is found, is "common".
 * </dl>
 * <p>
 * The BeanShell script is found by first looking in the filesystem.  If
 * not found, the classpath is searched.  It's okay not to specify or
 * use a BeanShell script.  The content will still be processed by
 * the Velocity engine.
 * <p>
 * The BeanShell interpreter is managed by session and is persistent during
 * the session.  The interpreter has access to the <code>Server</code>
 * object, the prefix string, the <code>Request</code> object, and the
 * Velocity context through the variables "server", "prefix", "request",
 * and "context".
 * <p>
 * A new Velocity context is created each time the <code>filter</code>
 * method is called.  For convenience, the context is initially populated
 * with the same objects as the BeanShell.  They are referenced by the
 * same names ("server", "prefix", "request", and "context").  However,
 * due to the design of Velocity, the public fields of the <code>Server</code>
 * and <code>Request</code> objects are read only.  The field values can not
 * be changed by the template code.
 * <p>
 * Each time the <code>filter</code> method is called, a new Velocity
 * context is created and populated. Then the BeanShell script, if it exists,
 * is passed through <code>Format.subst</code> for variable substitution
 * and then passed to the BeanShell interpreter.  Finally the content (i.e. a
 * Velocity template) and the context are passed to the Velocity engine
 * for processing.
 * <p>
 * Note: Velocity requires at least a 1.2 Java VM.  Attempts to use Velocity
 * with a 1.1 VM will most likely produce incorrect results.  Also note that
 * the Brazil system must be compiled with a 1.2+ compiler so that core
 * components of Brazil are compatible with the Velocity engine.
 * <p>
 * @author	Steve Drach &lt;drach@sun.com&gt;
 * @version		2.3
 */
public class VelocityFilter implements Filter, LogSystem {
    String script;
    String prefix;
    Server server;
    String session;

    public boolean
    init(Server server, String prefix) {
	this.server = server;
	this.prefix = prefix;
	boolean initok = true;
	session = server.props.getProperty(prefix + "session", "common");
	script = server.props.getProperty(prefix + "script");
	if (script != null) {
	    try {
		script = ResourceHandler.getResourceString(server.props,
							   prefix, script);
	    } catch (IOException e) {
		log(Server.LOG_ERROR, "BeanShell IOException: ", e);
		initok = false;
	    }
	}
	if (initok && script != null) {
	    Interpreter bsh = (Interpreter)SessionManager.getSession(session,
					     prefix, Interpreter.class);
	    try {
		bsh.set("server", server);
		bsh.set("prefix", prefix);
	    } catch (EvalError e) {
		log(Server.LOG_WARNING, "BeanShell EvalError: ", e);
		initok = false;
	    }
	}
	if (initok) {
	    try {
		Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, this);
		Velocity.init();
	    } catch (Exception e) {
		log(Server.LOG_ERROR, "Velocity Exception: ", e);
		initok = false;
	    }
	}
	return initok;
    }

    /**
     * This is the request object before the content was fetched
     */

    public boolean respond(Request request) {
	return false;
    }

    /**
     * Only filter text/* documents
     */

    public boolean
    shouldFilter(Request request, MimeHeaders headers) {
	String type = headers.get("content-type");
	return (type != null && type.toLowerCase().startsWith("text/"));
    }

    /**
     * Execute the BeanShell script if it exists and then process the
     * content as a Velocity template.
     */

    public byte[]
    filter(Request request, MimeHeaders headers, byte[] content) {
	boolean aok = true;

	VelocityContext context = new VelocityContext();
	context.put("context", context);
	context.put("prefix", prefix);

	try {
	    context.put("request", new Vrequest(request));
	} catch (Exception e) {
	    log(Server.LOG_WARNING, "Context error with request: ", e);
	    context.put("request", request);
	}

	try {
	    context.put("server", new Vserver(server));
	} catch (Exception e) {
	    log(Server.LOG_WARNING, "Context error with server: ", e);
	    context.put("server", server);
	}

	if (script != null) {
	    Interpreter bsh = (Interpreter)SessionManager.getSession(session,
							         prefix, null);
	    try {
		bsh.set("context", context);
		bsh.set("request", request);
		bsh.eval(Format.subst(request.props, script));
	    } catch (EvalError e) {
		log(Server.LOG_WARNING, "BeanShell EvalError: ", e);
		aok = false;
	    }
	}

	StringWriter w = new StringWriter();
	if (aok) {
	    try {
		Velocity.evaluate(context, w, "Velocity", new String(content));
	    } catch (ResourceNotFoundException e) {
		log(Server.LOG_WARNING, "Velocity ResourceNotFoundException: ", e);
		aok = false;
	    } catch (IOException e) {
		log(Server.LOG_WARNING, "Velocity IOException: ", e);
		aok = false;
	    } catch (MethodInvocationException e) {
		log(Server.LOG_WARNING, "Velocity MethodInvocationException: ", e);
		aok = false;
	    } catch (ParseErrorException e) {
		log(Server.LOG_WARNING, "Velocity ParseErrorException: ", e);
		aok = false;
	    }
	}

	if (aok) {
	    return w.toString().getBytes();
	} else {
	    return content;
	}
    }

    private void log(int level, String msg, Exception e) {
	if (e == null) {
	    server.log(level, prefix, msg);
	} else {
	    server.log(level, prefix, msg + e.getMessage());
	}
    }

    /**
     * Velocity LogSystem interface implementation.
     */

    public void init(RuntimeServices rsvc) {
    }

    public void logVelocityMessage(int level, String message) {
	switch(level) {
	case LogSystem.INFO_ID:
	    level = Server.LOG_INFORMATIONAL;
	    break;
	case LogSystem.WARN_ID:
	    level = Server.LOG_WARNING;
	    break;
	case LogSystem.ERROR_ID:
	    level = Server.LOG_ERROR;
	    break;
	}
	log(level, message, null);
    }

    /*
     * These classes make public fields accessable like Properties keys.
     * Unfortunately, this is read-only since Velocity only looks for
     * put methods on instances of Maps.
     */

    private Hashtable serverFields;

    /**
     * A helper class for Velocity that provides read only access
     * to the public fields of the <code>Server</code> object.  This
     * class and it's methods should only be used by the Velocity
     * engine.
     */
    public class Vserver extends Server {
	public Vserver(Server server) throws Exception {
	    if (serverFields == null) {
		serverFields = new Hashtable();
		Field[] fields = getClass().getFields();
		for (int i = 0; i < fields.length; i++) {
		    serverFields.put(fields[i].getName(), fields[i]);
		}
	    }

	    Enumeration e = serverFields.elements();
	    while (e.hasMoreElements()) {
		Field field = (Field)(e.nextElement());
		if (!Modifier.isFinal(field.getModifiers())) {
		    field.set(this, field.get(server));
		}
	    }
	}

	public Object get(String fieldName) {
	    try {
		Field f = (Field)(serverFields.get(fieldName));
		if (f != null) {
		    return f.get(this);
		}
	    } catch(Exception e) {
		this.log(Server.LOG_WARNING, "Vserver get: ", e.getMessage());
	    }
	    return null;
	}
    }

    private Hashtable requestFields;

    /**
     * A helper class for Velocity that provides read only access
     * to the public fields of the <code>Request</code> object.  This
     * class and it's methods should only be used by the Velocity
     * engine.
     */
    public class Vrequest extends Request {
	public Vrequest(Request request) throws Exception {
	    if (requestFields == null) {
		requestFields = new Hashtable();
		Field[] fields = getClass().getFields();
		for (int i = 0; i < fields.length; i++) {
		    requestFields.put(fields[i].getName(), fields[i]);
		}
	    }

	    Enumeration e = requestFields.elements();
	    while (e.hasMoreElements()) {
		Field field = (Field)(e.nextElement());
		if (!Modifier.isFinal(field.getModifiers())) {
		    field.set(this, field.get(request));
		}
	    }
	}

	public Object get(String fieldName) {
	    try {
		Field f = (Field)(requestFields.get(fieldName));
		if (f != null) {
		    return f.get(this);
		}
	    } catch(Exception e) {
		this.log(Server.LOG_WARNING, "Vrequest get: ", e.getMessage());
	    }
	    return null;
	}
    }
}
