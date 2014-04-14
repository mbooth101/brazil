/*
 * PropertiesCacheManager.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.
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
 * Created by suhler on 00/11/16
 * Last modified by suhler on 02/11/25 15:50:49
 *
 * Version Histories:
 *
 * 2.2 02/11/25-15:50:49 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.11.1.1 02/11/25-15:44:17 (suhler)
 *   expose "Saveable" interface to allow non properties objects to be saved
 *
 * 2.1 02/10/01-16:39:09 (suhler)
 *   version change
 *
 * 1.11 02/07/24-10:48:09 (suhler)
 *   doc updates
 *
 * 1.10 02/06/05-16:15:26 (suhler)
 *   add "defer" to defer loading sessions until we need them
 *
 * 1.9 02/01/29-14:35:56 (suhler)
 *   doc lint
 *
 * 1.8 01/05/01-11:02:09 (suhler)
 *   Update for new SessionManager Interface
 *
 * 1.7 01/04/04-16:58:24 (suhler)
 *   better diagnostics
 *
 * 1.6 01/03/15-10:15:40 (suhler)
 *   Prune empty properties objects before saving
 *
 * 1.5 00/12/11-13:32:32 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.4 00/12/08-16:48:02 (suhler)
 *   doc fixes
 *
 * 1.3 00/11/20-13:22:26 (suhler)
 *   doc fixes
 *
 * 1.2 00/11/16-11:56:35 (suhler)
 *   stupid typo
 *
 * 1.2 00/11/16-10:32:35 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 session/PropertiesCacheManager.java
 *
 * 1.1 00/11/16-10:32:34 (suhler)
 *   date and time created 00/11/16 10:32:34 by suhler
 *
 */

package sunlabs.brazil.session;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Glob;

/**
 * A version of the CacheManager that saves out any session state
 * that is either a "java properties" object, or implements "Saveable"
 * into a directory in the filesystem, 
 * one file per entry, then
 * restores them on server startup.  This is a "poor man's" serialization, 
 * that saves only ascii state represented in properties files.
 * This permits a wider variety of changes to be made to the
 * server code, yet still have the ability to read in the proper
 * session information.
 * <p>
 * Classes that are not properties files may implement "Saveable", which 
 * has the store() and load() methods from Properties; they are expected to
 * generate and restore the state of the object in Properties format.
 * <p>
 * This handler/sessionManager can take an ascii-readable "snapshot" of
 * the server state, for all state that is a java properties object (or
 * implements Saveable).
 * It doesn't perturb the existing state.
 * <p>Properties:
 * <dl class=props>
 * <dt>storeDir
 * <dd>The directory to use to store the state files.  It is created as needed
 *	when the state is saved. Defalts to "store".
 * <dt>match
 * <dd>A glob pattern that matches the url (or url?query if a query is used.
 * Defaults to "*\?*save=true".
 * <dt>filePrefix
 * <dd>A prefix pattern to use for all session files.  Defaults to the
 * handler prefix.
 * <dt>defer
 * <dd>If set, the saved session information is not reconstructed upon
 * startup.  Only the list of sessions is read in; the session information
 * is restored only when needed.
 * </dl>
 *
 * @author	Stephen Uhler (stephen.uhler@sun.com)
 * @version	%V% PropertiesCacheManager.java
 */

public class
PropertiesCacheManager extends CacheManager
implements Handler, Serializable {
    private static final String STORE = "storeDir";
    private static final String MATCH = "match";
    private static final String PREFIX = "filePrefix";
    private static final String DEFER = "defer";

    /*
     * Change VERSION to indicate in-compatibility across code versions
     */

    private static final String VERSION = "types.1";

    String storeDir;				// diretory for stored state
    String match;				// glob match for "save" url
    String filePrefix;				// prefix on saved state files
    Properties types = new Properties();	// saved sessions
    Server server;		// server reference for logging
    boolean defer;		// true to defer loading of sessioninfo
    File dir;			// store directory

    public boolean
    init(Server server, String prefix) {
	super.init(server, prefix);
	this.server = server;
	storeDir = server.props.getProperty(prefix + STORE, "store");
	match = server.props.getProperty(prefix + MATCH, "*\\?*save=true");
	filePrefix = server.props.getProperty(prefix + PREFIX, prefix);
	defer = (server.props.getProperty(prefix + DEFER) != null);

	/*
	 * Read in the directory of saved state, if any.
	 * Each file name is in the form [prefix][key].
	 * A separate file, [prefix]types contains the names and
	 * types of each session.
	 */
	 
	dir = new File(storeDir);
	try {
	    FileInputStream in =
		    new FileInputStream(new File(dir, filePrefix + VERSION));
	    types.load(in);
	    in.close();
	    server.log(Server.LOG_WARNING, prefix,
		"Loading saved state from: " + storeDir);
	} catch (Exception ex) {
	    server.log(Server.LOG_WARNING, prefix,
		"Can't find existing state file: " + filePrefix +
		VERSION + " in: " + dir);
	    defer=false;		// no point
	}
	if (defer) {
	    server.log(Server.LOG_DIAGNOSTIC, prefix,
		    "Deferring Loading " + types.size() + " sessions");
	} else {
	    Enumeration el = types.keys();
	    while (el.hasMoreElements()) {
	        String key = (String) el.nextElement();
                loadKey(key);
	    }
	}
	return true;
    }

    /**
     * Load a properties object from a file, put into the session mgr
     */
    
    Object loadKey(String key) {
	String type = types.getProperty(key);
	Object data = null;

	if (type != null) {
	    server.log(Server.LOG_DIAGNOSTIC, prefix,
		    "Loading session " + key + ", type " + type);
	    try {
	        data = Class.forName(type).newInstance();
	        FileInputStream in = new FileInputStream(
			    new File(dir,filePrefix + key));
		load(data,in);
	        putObj(key, data);
	        in.close();
	    } catch (Exception e) {
	        server.log(Server.LOG_WARNING, prefix,
		    "Error reading state file " + dir + "/" +
		    key + ": " + e.getMessage());
	    }
	}
	return data;
    }

    public boolean
    respond(Request request) {
	String check;
	if (request.query.equals("")) {
	    check = request.url;
	} else {
	    check = request.url + "?" + request.query;
	}
	if (Glob.match(match, check)) {
	    save(request);
	}
	return false;
    }

    /**
     * If we have deferred session loading, check here and get it!
     */

    protected Object
    getObj(Object session, Object ident) {
	Object result = super.getObj(session, ident);
	if (defer && result == null) {
	    String key = makeKey(session, ident);
            result = loadKey(key);
	} 
	return result;
    }


    /**
     * Save all of the properties files!
     * We make a TOC file that has type info, so we can reconstitue the
     * proper subclasses of Properties.
     */

    int
    save(Request request) {
	File dir = new File(storeDir);
	dir.mkdirs();	// make sure directories exist
	int count = 0;
	int deleted = 0;
	Properties types = new Properties();

	/*
	 * If we read in the existing types file to start with, we can
	 * "augment" the state from a previous save.
	 */

	request.log(Server.LOG_LOG, prefix, "saving all session state");
	try {
	    FileInputStream in =
		    new FileInputStream(new File(dir, filePrefix + VERSION));
	    types.load(in);
	    in.close();
	    request.log(Server.LOG_DIAGNOSTIC, prefix,
		    "reading existing session state file");
	} catch (Exception ex) {
	    request.log(Server.LOG_DIAGNOSTIC, prefix,
		    "No existing state files found");
	}

	/*
	 * Iterate through the tables, saving every session that is a
	 * properties file.
	 */
	 
	synchronized (pool) {
	    for (int i = 0; i < maxTables; i++) {
		Hashtable h = pool[i];
		if (h != null) {
		    Enumeration e = h.keys();
		    while (e.hasMoreElements()) {
			Object key = e.nextElement();
			Object value = h.get(key);
			if (value instanceof Properties ||
				value instanceof Saveable) {
			    if (isEmpty(value)) {
			    	h.remove(key);
			    	deleted++;
				request.log(Server.LOG_DIAGNOSTIC, prefix,
				    "deleting session " + key);
			    	continue;
			    }
			    request.log(Server.LOG_DIAGNOSTIC, prefix,
				"Saving state for session " + key);
			    String type = value.getClass().getName();
			    types.put(key, type);
			    try {
				FileOutputStream  props = new FileOutputStream(
					new File(dir,filePrefix  +
					key.toString()));
				save(value, props,
				    "Brazil server state from " +
				    request.serverUrl());
				count++;
				props.close();
			    } catch (IOException ex) {
				request.log(Server.LOG_LOG, prefix,
				        "Failed: " + ex.getMessage());
			    }
			} else {
			    // System.out.println("Not a properties: " + key);
			}
		    }
		}
	    }

	    /*
	     * Now write out the TOC file
	     */

	    try {
		FileOutputStream  out = new FileOutputStream(
			new File(dir, filePrefix  + VERSION));
		types.save(out, "Brazil server state from " +
			request.serverUrl());
		out.close();
	    } catch (IOException ex) {
		request.log(Server.LOG_DIAGNOSTIC, prefix,
			"Saving TOC file");
	    }
	}
	request.log(Server.LOG_LOG, prefix,
		"sessions saved: " + count + " deleted: " + deleted);
	return count;
    }


    /**
     * convenience methods for hiding the differences between saveable and
     * Properties.
     */

    boolean isEmpty(Object o) {
       if (o instanceof Saveable) {
	   return ((Saveable)o).isEmpty();
       } else {
	   return ((Properties)o).isEmpty();
       }
    }

    void save(Object o, OutputStream out, String header)  throws IOException {
       if (o instanceof Saveable) {
	   ((Saveable)o).save(out, header);
       } else {
	   ((Properties)o).save(out, header);
       }
    }

    void load(Object o, InputStream in)  throws IOException {
       if (o instanceof Saveable) {
	   ((Saveable)o).load(in);
       } else {
	   ((Properties)o).load(in);
       }
    }

    /**
     * This interface allows for persistence of non-properties session objects.
     * These methods should behave precisely like the corrosponding methods
     * of the Properties class.
     */

    public interface Saveable {

	/**
	 * Recreate the object from the ascii representation stored as a
	 * Properties format file.
	 */

	public void load(InputStream in) throws IOException;

	/**
	 * Create an ascii representation of this object in a Java Properties
	 * format.
	 */

	public void save(OutputStream out, String header) throws IOException;

	/**
	 * The current object state is the "default"; "save" does not need to
	 * write out any state.
	 */

	public boolean isEmpty();
    }
}
