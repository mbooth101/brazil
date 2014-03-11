/*
 * RestartHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2004 Sun Microsystems, Inc.
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
 * Created by suhler on 04/11/24
 * Last modified by suhler on 04/11/30 11:00:51
 *
 * Version Histories:
 *
 * 1.2 04/11/30-11:00:51 (suhler)
 *   add "keep" flag to allow config file to augment current config
 *
 * 1.2 04/11/24-16:02:03 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/RestartHandler.java
 *
 * 1.1 04/11/24-16:02:02 (suhler)
 *   date and time created 04/11/24 16:02:02 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * Restart the server when a sigHUP is received.
 * Only The handlers are restarted, by creating new instances of them,
 * and calling the respective init() methods.  None of the other server
 * properties (such as the listening port) are effected.  
 * Any requests that are currently in-progress complete using the old
 * configuration.
 * <p>
 * NOTES:
 * <ul>
 * <li> Supplying an invalid configuration file can render the
 * server inoperable.
 * <li>The non-portable <code>sun.misc.Signal*</code> classes are used.
 * </ul>
 * <p>
 * Properties:
 * <dl class=props>
 * <dt>config
 * <dd>The name of the configuration file to use for this server.
 * Relative paths are resolved relative to the current directory.
 * If no file is specified, the server continues to use its existing
 * configuration [which has presumably been modified in-place].
 * If a config file is specified  and
 * the config file hasn't changed, then no restart is done.
 * <dt>keep<dd>If set, and a config file is specified, the existing
 * configuration (server.props) is not cleared first.
 * </dl>
 */

public class RestartHandler implements Handler, SignalHandler {
    Server server;		// reference to our server
    String prefix;		// our name in the config file
    boolean shouldKeep;		// keep existing config
    File configFile = null;	// our config file
    long lastModified;		// last mofified time of config file

    public boolean
    init(Server server, String prefix) {
	this.server = server;
	this.prefix = prefix;

	String file = server.props.getProperty(prefix + "config");
	shouldKeep =  (server.props.getProperty(prefix + "keep") != null);
	if (file != null) {
	    configFile = new File(file);
	}
	lastModified = 0;	// in case we've been started differently
	Signal.handle(new Signal("HUP"), this);
	return true;
    }

    public boolean
    respond(Request request) {
	return false;
    }
    
    /**
     * Restart the server after re-reading the config file
     */

    public void
    handle(Signal sig) {
	server.log(Server.LOG_DIAGNOSTIC, prefix,
			"SIGHUP received");
	if (configFile == null) {
	    server.log(Server.LOG_WARNING, prefix,
			"SIGHUP restarting server");
	    server.restart(server.props.getProperty("handler"));
	} else {
	    long current = configFile.lastModified();
	    if (current > lastModified) {
		lastModified = current;
		server.log(Server.LOG_DIAGNOSTIC, prefix,
			"Restarting server using new config: " + configFile);
		try {
		    FileInputStream in = new FileInputStream(configFile);
		    Properties config;
		    if (shouldKeep) {
			config = server.props;
		    } else {
			config = new Properties();
		    }
		    config.load(in);
		    in.close();
		    server.props = config;
		    server.restart(config.getProperty("handler"));
		} catch (Exception e) {
		    server.log(Server.LOG_WARNING, prefix,
			    "restarting sever " + e);
		}
	    } else {
		server.log(Server.LOG_WARNING, prefix,
		    "unchanged config file, NOHUP ignored");
	    }
	}
    }
}
