/*
 * AliceHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2002-2006 Sun Microsystems, Inc.
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
 * The Initial Developer of the Original Code is: lc138592.
 * Portions created by lc138592 are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): lc138592, suhler.
 *
 * Version:  2.3
 * Created by lc138592 on 02/07/18
 * Last modified by suhler on 06/11/13 15:31:09
 *
 * Version Histories:
 *
 * 2.3 06/11/13-15:31:09 (suhler)
 *   MatchString moved packages from "handler" to "util"
 *
 * 2.2 02/10/28-22:37:58 (lc138592)
 *   Adds further support for handling dynamic creation of bots.
 *
 * 2.1 02/10/01-16:36:00 (suhler)
 *   version change
 *
 * 1.4 02/08/13-13:32:36 (lc138592)
 *   Added some more diagnostic output
 *
 * 1.3 02/08/06-11:24:53 (suhler)
 *   remove * imports
 *
 * 1.2 02/08/06-10:08:11 (suhler)
 *   restore leo's changes onto renamed previous version
 *
 * 1.2 70/01/01-00:00:02 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 slim/AliceHandler.java
 *   Name history : 1 0 slim/AliceHandle.java
 *
 * 1.1 02/07/18-17:33:47 (lc138592)
 *   date and time created 02/07/18 17:33:47 by lc138592
 *
 */

package sunlabs.brazil.slim;

// Leo Chao - May 20, 2002

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import org.alicebot.server.core.ActiveMultiplexor;
import org.alicebot.server.core.Bot;
import org.alicebot.server.core.Bots;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.Multiplexor;
import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import org.alicebot.server.core.responder.TextResponder;
// import org.alicebot.server.core.util.Toolkit;

/**
 * This class maintains the ALICE AI Engine.  Using this in conjunction with 
 * a template allows one to create an interface that can communicate with the
 * AI without having to go through the bulky and ultimately unipurpose 
 * ALICE servlet that came with ProgramD.  This, of course, uses the ProgramD
 * Alice AI as developed and released in Open Source by Richard Wallace.
 * <p>
 * The query parameter for the basic use are as follows:<br>
 * <li>transtext=<i>message to be sent</i>
 * <li>botname=<i>bot to talk to</i>
 * <p>
 * As an added kludge, there is a direct-call interface used with this
 * handler which may be used from a handler/template further down the
 * processing line by:<br>
 * <li>Setting "direct-call" property to non-null in the request
 * <li>Populate AliceHandler.DIRECT_CALL+"sayto" with the message.
 * <li>Populate AliceHandler.DIRECT_CALL+"user-name" with the sender.
 * <li>Populate AliceHandler.DIRECT_CALL+"botname" with the name of the bot.
 * <li>Calling AI.respond(hr);
 * <li>Output will be stored in the standard place (query.reponse and query.botname)
 * <p>
 * Configuration File Properties:<br>
 * <dl class=props>
 * <dt>prefix</dt>
 * <dd>Specify the expected url prefix for the handler.</dd>
 * <dt>search_names</dt>
 * <dd>Specifies the search engines scripts to use.  This is a space seperated list of search engines names.  Each name will have an associated <i>name</i>.script to specify the tcl script to run for the search and a <i>name</i>.name to specify the predicate name to use.</dd>
 * <dt>root</dt>
 * <dd>This is the root directory for Alice operations</dd>
 * </dl>
 *
 * @author Leo Chao
 */
public class AliceHandler implements Handler
{
    /**
     * Standard initialization.  Also initializes the Alice AI Engine as a
     * whole on server start up (only does this once).
     *
     * @param server {@link sunlabs.brazil.server.Server Server} object provided by the webserver.
     * @param prefix String indicating the URL-prefix expected for processing
     * @return Returns true if Alice is successfully initializated.
     */
    public boolean init(Server server, String prefix)
    {
	//Initialize variables
	this.server = server;
	cPre = prefix;
	cMatchString = new MatchString(prefix, server.props);
	
	if (!isLoaded)
            return initializeALICE(server);
        else
            return true;    
    }
    
    private void setSearchParameters(Server server, Bot bot)
    {
	String searches = server.props.getProperty(cPre+"search_names", "");
	StringTokenizer st = new StringTokenizer(searches, " ");
	String cur_search;
	String script, name;
	while(st.hasMoreTokens()) {
	    cur_search = st.nextToken();
	    script = server.props.getProperty(cur_search+".script", "");
	    name = server.props.getProperty(cur_search+".name", "undefined");
	    bot.addPredicateInfo(cur_search, name, false);
	    bot.addPredicateInfo(cur_search+"_script", script, false);
	    server.log(Server.LOG_DIAGNOSTIC, this, 
		       cur_search + " information added to " + bot.getID() +
		       "\n\t" + name + "\n\t" + script);
	}
    }

    private boolean initializeALICE(Server server)
    {
	String aliceroot = server.props.getProperty(cPre+"root", "");
        if (!Globals.isLoaded())
            Globals.load(aliceroot+"server.properties");
	
        ActiveMultiplexor.getInstance().initialize();
        Graphmaster.load(Globals.getStartupFilePath(), null);
        Graphmaster.markReady();

	Iterator ki = Bots.keysIterator();
	Bot b;
	while(ki.hasNext()) {
	    b = Bots.getBot((String)(ki.next()));
	    server.log(Server.LOG_DIAGNOSTIC, this, "Initializing " + b.getID());
	    setSearchParameters(server, b);
            server.props.put("bots_available."+b.getID(), b.getID());
	}

	server.log(Server.LOG_DIAGNOSTIC, this, "bot initialization complete, bots is " + Bots.getNiceList());
	isLoaded = true;
	AI = this;
	server.log(Server.LOG_DIAGNOSTIC, this, "Alice AI initialized.  " + AI);
        return true;
    }
          
    private  boolean isMyRequest(Hashtable props, Request req)
    {
	String say = (String)(props.get("sayto"));
	
	if (!cMatchString.match(req.url)) {
	    return false;
	}
	
	return (say != null);
    }
    
    /**
     * Processes an AI request response.  Essentially two functions in
     * one.  If the direct-call parameter is non-null this will
     * check for calling parameters with the AliceHandler.DIRECT_CALL
     * prefix.  Otherwise it will look for calling parameters in the
     * query domain.
     *
     * @param req {@link sunlabs.brazil.server.Request Request} object passed in by the webserver.
     * @return Returns false always.
     */
    public boolean respond(Request req)
    {
	boolean direct = (req.props.getProperty("direct-call") != null);
	Hashtable props = req.getQueryData();
       
	if (!direct && (!isMyRequest(props, req)))
	    return false;

	String s = (String)(props.get("sayto"));
	String user = "user";
	String botname = (String)(props.get("botname"));

	if (direct) {
	    s =  (String)(req.props.getProperty(DIRECT_CALL+"sayto"));
	    user = (String)(req.props.getProperty(DIRECT_CALL+"user-name"));
	    botname = (String)(req.props.getProperty(DIRECT_CALL+"botname"));
	}

	if (s == null) s = new String("");

	// Get parameters for use in talking to the AI engine
	String HOSTNAME = Globals.getHostName();
	String botid;
	Bot bot = null;

	if (botname == null) {
	    bot = Bots.getFirstBot();
	    if (bot == null) {
		server.log(Server.LOG_ERROR, this, "Error: No bots present");
		botid = null;
		return false;
	    }
	}
	else {
	    bot = Bots.getBot(botname);
	    if (bot == null) {
		server.log(Server.LOG_ERROR, this, "Bot " + botname + " does not exist.");
		botid = null;
		return false;
	    }
	}

	botid = bot.getID();
	// Default response string if nothing works
	String aliceResponse = new String("<i>*No response*</i>");
	
	// Talk to the bot
	//aliceResponse = Multiplexor.getResponse(s, HOSTNAME, botid, new TextResponder());
	Map map = bot.predicatesFor(user);
	String known_name = (String)(map.get("name"));
	if (known_name == null)
	    known_name = "null";
	if (known_name.equals("user")) {
	    map.put("name", user);
	    //return respond(req);
	}

	aliceResponse = Multiplexor.getResponse(s, user, botid, new TextResponder());
			  
	// If the bot doesn't say anything, make that clear
	if (aliceResponse.equals(""))
	    aliceResponse = "<i>*silence*</i>";
	       
	server.log(Server.LOG_INFORMATIONAL, this, user+" says: " + s);
	req.props.put("query.response", aliceResponse);
	req.props.put("query.botname", botid);
	server.log(Server.LOG_INFORMATIONAL, this, botid + " responds with: " + aliceResponse);
	return false;
    }

    /**
     * Reference to itself, useful for later call backs further down the
     * template/handler execution chain
     */
    public static AliceHandler AI;
    public static final String DIRECT_CALL = "DIRECT.";

    private String cPre;
    private Server server;
    private MatchString cMatchString;
    private boolean isLoaded = false;
}
