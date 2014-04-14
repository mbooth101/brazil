/*
 * AliceResponse.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2002 Sun Microsystems, Inc.
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
 * Last modified by lc138592 on 02/10/28 22:38:27
 *
 * Version Histories:
 *
 * 2.3 02/10/28-22:38:27 (lc138592)
 *   Adds futher support for dynamic creation of bots.
 *
 * 2.2 02/10/21-21:41:51 (lc138592)
 *   New AliceResponse with some portions of the create/delete bot dynamically integrated.  Goes with the new adminbot.html.
 *
 * 2.1 02/10/01-16:36:00 (suhler)
 *   version change
 *
 * 1.6 02/08/13-13:33:00 (lc138592)
 *   Added a check for an uninitialized AliceHandler
 *
 * 1.5 02/08/06-11:23:56 (suhler)
 *   remove * imports
 *   fix typos
 *
 * 1.4 02/08/05-17:01:37 (lc138592)
 *   Added Javadocs
 *
 * 1.3 02/07/26-15:49:27 (lc138592)
 *   Added the bot properties tag for use with adminbot.html to allow the user to access and modify bot properties.
 *
 * 1.2 02/07/19-15:42:18 (lc138592)
 *   sunlabs became sublabs
 *
 * 1.2 70/01/01-00:00:02 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 slim/AliceResponse.java
 *
 * 1.1 02/07/18-17:33:48 (lc138592)
 *   date and time created 02/07/18 17:33:48 by lc138592
 *
 */

package sunlabs.brazil.slim;

// Leo Chao - June 20, 2002
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
// import java.util.Vector;
import org.alicebot.server.core.Bot;
import org.alicebot.server.core.Bots; 
import org.alicebot.server.core.PredicateInfo; 
import sunlabs.brazil.properties.PropertiesList;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.template.QueueTemplate;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;

 /**
 * This class is responsible for handling interaction between the Brazil
 * IM system and the ALICE AI system.  This sends direct-calls to the
 * AliceHandler class, which is assumed to be in existence, in order to
 * get AI responses to user inputs.
 *
 * A seperate thread is made for each bot allowing asynchronous bot responses, 
 * furthermore the initial thread is the only one that actually works with 
 * message delivery, the other threads are only response loops.
 *
 * This template defines the following tags:<br>
 * <li> {@link #tag_botmessage botmessage} <i>name=... from=... message=...</i>
 * <li> {@link #tag_botlogin botlogin} <i>name=...</i>
 * <lI> {@link #tag_botlogout botlogout} <i>name=...</i>
 * <li> {@link #tag_botproperties botproperties} <i>name=... action=... propName=... propValue=...</i>
 *
 * Lastly there is a BotInfo inner class to allow for ease of maintence of 
 * bot related data, esp. the thread killing aspect.
 *
 * @author Leo Chao
 */
public class AliceResponse extends Template
			   implements Runnable
{
    private String queryAI(BotInfo bi, QueueTemplate.QueueItem qi)
    {
	String response = "";
	bi.request.props.put(AliceHandler.DIRECT_CALL+"sayto", qi.message);
	bi.request.props.put(AliceHandler.DIRECT_CALL+"user-name", qi.from);
	bi.request.props.put(AliceHandler.DIRECT_CALL+"botname", cName);
	bi.request.props.put("direct-call", "true");

	if (AliceHandler.AI == null) {
	    server.log(Server.LOG_WARNING, this, "No AliceHandler initialized");
	    return "";
	}
	AliceHandler.AI.respond(bi.request);
	
	server.log(Server.LOG_INFORMATIONAL, this, "AI message recieved");
	response=bi.request.props.getProperty("query.response");
	if (response == null) {
	    server.log(Server.LOG_WARNING, this, "Unable to procure AI response");
	    response = "";
	}    	   
	
	return response;
    }

    private void putMessage(String message, String to)
    {
	QueueTemplate.Queue toMessageQ = (QueueTemplate.Queue)(SessionManager.getSession(to, "q", QueueTemplate.Queue.class));

	toMessageQ.put(new QueueTemplate.QueueItem(cName, message, cName + ":" + to + ":en:message/bot"));
    }

    /** The <code>run</code> method is used to start a seperate thread for each
     *  bot instance created.  It sits in a while loop blocking on the message
     *  queue for the bot in question.
     *
     */
    public void run()
    {
	QueueTemplate.QueueItem qi;
	BotInfo bi;
	String response;

	while(true) {
	    server.log(Server.LOG_DIAGNOSTIC, this, "Run loop");

	    bi = (BotInfo)(bots.get(cName));

	    if (bi.dying) {
	    server.log(Server.LOG_LOG, this, cName + " is dying, logging out");
		break;
	    }
	    
	    qi = (QueueTemplate.QueueItem)(bi.q.get(0));
	    if (qi == null) {
		server.log(Server.LOG_LOG, this, "Nothing on queue, looping again");
	    }
	    else {
		server.log(Server.LOG_LOG, this, "(" + cName + ") Message recieved: " + qi.message);
		response = queryAI(bi, qi);
		server.log(Server.LOG_LOG, this, "Response recieved: " + response);

		if (isCommand(response)) {
		    
		    response = processCommand(response);
		}

		String from = qi.from;
		putMessage(response, from);
	    }
	}

	//clean-up
	
	return;
    }

    private boolean isCommand(String msg)
    {
	return msg.startsWith("!command");
    }

    private String processCommand(String cmdline)
    {
	StringTokenizer toker = new StringTokenizer(cmdline, ":");
	String msg;
	String command;
	
	//get the command
	command = toker.nextToken();
	server.log(Server.LOG_DIAGNOSTIC, this, "command: " + command);
	if (!command.equals("!command")) {
	    server.log(Server.LOG_WARNING, this, "Non-command passed to processCommand: " + cmdline);
	    return cmdline;
	}

	command = toker.nextToken();
	if (command.startsWith("search-")) {
	    String searchutil = command.substring(command.indexOf('-')+1);
	    String searchwords = toker.nextToken();
	    msg = toker.nextToken();
	    String searchbot = toker.nextToken();
	    String respondto = toker.nextToken();

	    server.log(Server.LOG_DIAGNOSTIC, this, "\n\tbot: " + searchbot + "\n\tuser: " + respondto + "\n\twith: " + msg + "\n\tsearch using: " + searchutil + "\n\tsearch param: " + searchwords);

	    BotInfo bi = (BotInfo)(bots.get(searchbot));
	    server.log(Server.LOG_DIAGNOSTIC, this, bi.toString());
	    bi.addMessage(respondto, "SEARCH " + searchutil + " " + searchwords, respondto + ":"+ searchbot + ":en:message/bot");

	}
	else {
	    msg = cmdline;
	}
	return msg;
    }


    private void signal_logout(BotInfo bi)
    {
	QueueTemplate.QueueItem qi = new QueueTemplate.QueueItem("Administrator", "Logout Now", bi.name + " Administrator English command/botout");
        bi.q.put(qi);
    }

    /** 
     * This, the default constructor, is used to create the original template.  This
     * does <b>not</b> start a seperate thread and may be viewed as a "main" portion
     * of this class, processing all the tags that need to be done.
     *
     */
    public AliceResponse()
    {
	cName = "Template Processor";
	if (!started) {
	    bots = new Hashtable();
	}
	started = true;
    }

    /**
     * This constructor is used to create a new bot thread.  This new thread will 
     * wait for and handle messages as needed.
     *
     * @param botname the name of the bot to create
     */
    public AliceResponse(String botname)
    {
	cName = botname;
	
	server.log(Server.LOG_LOG, this, "Constructor for Bot " + botname + " invoked.");
	new Thread(this).start();
	server.log(Server.LOG_DIAGNOSTIC, this, "Bot thread started");
    }

    /**
     * Performs basic initialization tasks.
     *
     * @param The RewriteContext passed into the template by the web server.
     */
    public boolean init(RewriteContext hr)
    {
	server = hr.server;
	return true;
    }

    /**********************************************
     * ALICE RESPONSE HTML TEMPLATE TAGS          *
     **********************************************
     */

    /**
     * This handles the botproperties tag, allowing the user to:<br>
     * <li> Get predicate information from the bot using <i>action=list</i>
     * <li> Set a predicate, using <i>action=set</i> and <i>propName=...</i> with <i>propValue=...</i>
     * <li> Get a single predicate using <i>action=get</i> and <i>propName=...</i>
     *
     * @param hr The RewriteContext passed into the template by the web server.
     */
    public void tag_botproperties(RewriteContext hr)
    {
	Bot bot;
	String botname = hr.get("name");
	String action  = hr.get("action", "list");	
	if (botname == null) {
	    server.log(Server.LOG_ERROR, this, "Name must be specified");
	    return;
	}

	
	try {
	    bot = Bots.getBot(botname);
	}
	catch (Exception e) {
	    server.log(Server.LOG_ERROR, this, botname + " is not a valid botname");
	    server.log(Server.LOG_ERROR, this, "Exception trace:\n" + e);
	    return;
	}

	server.log(Server.LOG_DIAGNOSTIC, this, "Performing " + action+" with " +botname);
	if (action.equals("list")) {
	    HashMap botprops = bot.getPredicatesInfo();
	    Iterator i = (botprops.keySet()).iterator();

	    while(i.hasNext()) {
		PredicateInfo current = (PredicateInfo)(botprops.get(i.next()));
		hr.append("<b>" + 
			  current.name + "</b> is set to <i>" + 
			  current.defaultValue +
			  "</i><br>");
	    }
	}
	else if (action.equals("set")) {
	    String propName = hr.get("propname");
	    String propValue= hr.get("propvalue", "");
	    HashMap botprops = bot.getPredicatesInfo();

	    if (propName == null) {
		server.log(Server.LOG_ERROR, this, "set requires propname be defined");
		return;
	    }

	    if (propValue.equals("")) {
		botprops.remove(propName);
		server.log(Server.LOG_DIAGNOSTIC, this, propName + " removed");		
	    }
	    else {
		PredicateInfo pi = new PredicateInfo();
		pi.name=propName;
		pi.defaultValue=propValue;
		pi.returnNameWhenSet=false;
		botprops.put(propName, pi);
		server.log(Server.LOG_DIAGNOSTIC, this, propName + " set to " + propValue);
	    }
	}
	else if (action.equals("get")) {
	    String propName = hr.get("propname");
	    HashMap botprops = bot.getPredicatesInfo();

	    if (propName == null) {
		server.log(Server.LOG_ERROR, this, "set requires propname be defined");
		return;
	    }

	    hr.append(((PredicateInfo)(botprops.get(propName))).defaultValue);
	}
    }

    /**
     * This will handle sending messages to a bot.  The tag needs the following parameters:<br>
     * <li><b>name</b>: The bot to send to
     * <li><b>from</b>: Who the message is from, and where the response should be sent
     * <lI><b>message</b>: The message
     *
     * Note that there is no mechanism to specify meta data.
     *
     * @param hr The RewriteContext passed into the template by the web server.
     */
    public void tag_botmessage(RewriteContext hr)
    {
	String botname;
	String from;
	String message;
	
	server.log(Server.LOG_DIAGNOSTIC, this, "Initializing message");
	botname = hr.get("name");
	from = hr.get("from");
	message = hr.get("message");

	if ((botname == null) || (from == null) || 
	    (message == null)) {
	    server.log(Server.LOG_ERROR, this, "Invalid message parameters, botname, message and from all must be specified.");
	}

	server.log(Server.LOG_INFORMATIONAL, this, "Sending message (" + message + ") to " + botname);
	if (!bots.containsKey(botname)) {
	    server.log(Server.LOG_ERROR, this, "Bot " + botname + " not found");
	    server.log(Server.LOG_INFORMATIONAL, this, "bots contains: " + bots);
	    return;
	}

	BotInfo bi = (BotInfo)(bots.get(botname));
	bi.addMessage(from, message, from + ":" + botname + ":en:message/bot");
	hr.killToken();
    }

    /**
     * This tag essentially logs the bot out, not really fully functional at this
     * point.
     *
     * @param hr The RewriteContext passed into the template by the web server.
     */
    public void tag_botlogout(RewriteContext hr)
    {
	String botname;
	String channels;

	botname = hr.get("name");
	channels = hr.get("channels");

	if (botname == null) {
	    server.log(Server.LOG_ERROR, this, "No botname, cannot process");
	    return;
	}
	server.log(Server.LOG_LOG, this, botname+ " logging out");

	if (!bots.containsKey(botname)) { 
	    server.log(Server.LOG_WARNING, this, botname + " not in hashtable");
	    return;
	}

	BotInfo bf = (BotInfo)(bots.get(botname));
	bf.die();
	signal_logout(bf);
	hr.killToken();
    }

    private void do_login(String botname, RewriteContext hr)
    {
	server.log(Server.LOG_LOG, this, "Creating bot information\nName: " + botname);

	BotInfo bf = new BotInfo(botname, "", hr.request);
	bots.put(botname, bf);
	server.log(Server.LOG_INFORMATIONAL, this, "bots now contains: " + bots);
	bf.cRun = new AliceResponse(botname);

	hr.request.props.put("bot_login."+botname, "botname");
    }

    /**
     * Logs the bot in, but in reality it is a necessary pre-call to make before
     * using the HTML code to log the bot in so that the queue and state information
     * for a bot is properly maintained.  The only parameter is <b>name</b> which 
     * should be set to the name of the bot to login
     *
     * @param hr The RewriteContext passed into the template by the web server.
     */
    public void tag_botlogin(RewriteContext hr)
    {
	String botname;
	String channels;
	
	PropertiesList props = hr.request.props;

	botname = hr.get("name");
	channels = hr.get("channels");
	if (channels == null) {
	    channels = "";
	}
		
	if (botname == null) {
	    server.log(Server.LOG_INFORMATIONAL, this, 
		       "No botname specified, processing all");

	    for (Iterator biter = Bots.keysIterator();biter.hasNext();)
		do_login((String)(biter.next()), hr);

	    return;
	}
	
	if (!Bots.knowsBot(botname)) {
	    server.log(Server.LOG_ERROR, this, 
		       "Bot " + botname + " does not exist, aborting bot login.");
	}

	do_login(botname, hr);
	hr.killToken();
    }

    /**
    * Performs an action up the bot specified in name.  Valid actions 
    * include:<br>
    * <li>Create:  creates a new bot with name of <code>name</code>
    * <li>Delete:  removes a bot with name of <code>name</code>
    * @param hr The RewriteContext passed into the template by the web server.
    */
    public void tag_botaction(RewriteContext hr)
    {
    	String botname;
    	String action;
    	
    	server.log(Server.LOG_DIAGNOSTIC, "BOTACTION: ", "Tag Called");
        botname = hr.get("name");
    	if (botname == null) {
    		server.log(Server.LOG_INFORMATIONAL, "BOTACTION: ", "No botname specified on create.");
		return;
    	}
    	action = hr.get("action", "create");
	
	if (action.equals("create"))
	{
		if (Bots.knowsBot(botname)) {
		    server.log(Server.LOG_ERROR, "BOTACTION: ", 
				  "Bot " + botname + " does exist, aborting bot create.");
			hr.append(botname + " already exists, cannot create another bot of this name.");
		} else {
			Bot newbot = new Bot(botname);
			Bots.addBot(botname, newbot);
                        hr.server.props.put("bots_available."+botname, botname);
			hr.append(botname + " added.");
			return;
		}
		return;
	} else if (action.equals("delete"))
	{
		if (!Bots.knowsBot(botname)) {
		    server.log(Server.LOG_ERROR, this, 
				  "Bot " + botname + " does not exist, aborting bot deletion.");
			hr.append(botname + " does not exist.");	  
		    return;
		}		
		Bots.addBot(botname, null);
		hr.append(botname + " deleted.");
	} else {
		hr.append("Invalid action specified.");
	}
    }
    
    private String cName;
    private static boolean started;   
    private static Server server;
    public static Hashtable bots;

    private static class BotInfo
    {
	public String name;
	public String channels;
	public QueueTemplate.Queue q;
	public AliceResponse cRun;
	public Request request;

	public String toString()
	{
	    return "BotInfo for " + name + " @ " + super.toString();
	}

	public BotInfo(String n, String c, Request req)
	{
	    name = n;
	    channels = c;
	    q = new QueueTemplate.Queue();
	    dying = false;
	    request = req;
	}

	public void addMessage(String from, String message, String meta)
	{
	    QueueTemplate.QueueItem qi = new QueueTemplate.QueueItem(from, message, meta);
	    q.put(qi);
	}

	public void die()
	{
	    dying = true;
	}

	private boolean dying;
    }
}
