/*
 * ATHandler.java
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
 * Version:  2.2
 * Created by lc138592 on 02/07/18
 * Last modified by suhler on 06/11/13 15:31:04
 *
 * Version Histories:
 *
 * 2.2 06/11/13-15:31:04 (suhler)
 *   MatchString moved packages from "handler" to "util"
 *
 * 2.1 02/10/01-16:35:59 (suhler)
 *   version change
 *
 * 1.6 02/09/10-11:02:43 (suhler)
 *   change "link" message for audio
 *
 * 1.5 02/08/13-13:32:23 (lc138592)
 *   Tidyed up the code
 *
 * 1.4 02/08/06-11:23:26 (suhler)
 *   remove * imports
 *
 * 1.3 02/08/05-17:01:28 (lc138592)
 *   Added Javadocs
 *
 * 1.2 02/07/19-15:41:42 (lc138592)
 *   Removed dependency on DebugPrinter
 *
 * 1.2 70/01/01-00:00:02 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 slim/ATHandler.java
 *
 * 1.1 02/07/18-17:33:47 (lc138592)
 *   date and time created 02/07/18 17:33:47 by lc138592
 *
 */

package sunlabs.brazil.slim;

// Leo Chao, June 15, 2002
import java.util.Hashtable;
import java.util.Vector;
import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.template.QueueTemplate.Queue;
import sunlabs.brazil.template.QueueTemplate.QueueItem;
import sunlabs.brazil.template.QueueTemplate;

/**
 * This Handler is responsible for transmitting audio messages to and from
 * the IM server and the various clients.  Note that each client is expected
 * to have code to handle the special ! command strings this produces.  No
 * data is actually transmitted to the clients, only a command message that 
 * contains the text to display and the hyperlink to the actual data which is
 * written to file on the server side.
 *
 * @author Leo Chao
 */
public class ATHandler implements Handler
{
    /**
     * Standard initialization.  Also sets up the various audio roots needs
     * to maintain and find the audio clips server side.
     *
     * @param server {@link sunlabs.brazil.server.Server Server} object provided by the webserver.
     * @param prefix String indicating the URL-prefix expected for processing
     * @return Returns true always.
     */
    public boolean init(Server server, String prefix)
    {
	// instance variable initialization
	this.server = server;
	cPre = prefix;
	cMS = new MatchString(prefix, server.props);

	// Audio File root initialization
	cAudioRoot = server.props.getProperty(cPre+"audio_root", "/");
	if (cAudioRoot.charAt(cAudioRoot.length()-1) != '/')
	    cAudioRoot += "/";
	cHardAudioRoot =server.props.getProperty(cPre+"data_prefix", "") + cAudioRoot;
	if ((cHardAudioRoot.charAt(0) != '.') && (cHardAudioRoot.charAt(0) == '/'))
	    formattedRoot = "." + cHardAudioRoot;
	else
	    formattedRoot = cHardAudioRoot;

	cClips = new Vector();

	server.log(Server.LOG_DIAGNOSTIC, this, "ATHandler initialized");
	server.log(Server.LOG_DIAGNOSTIC, this, "root: " + cAudioRoot);
	server.log(Server.LOG_DIAGNOSTIC, this, "hard data root: " + cHardAudioRoot);
	server.log(Server.LOG_DIAGNOSTIC, this, "fully formatted: " + formattedRoot);
	return true;
    }

    private boolean isMyRequest(Request req)
    {
	String type;
	if(!cMS.match(req.url))
	    return false;

	type = (String)(req.headers.get("content-type"));
	if ((type == null) || (!type.equals("audio/raw")))  {
	    server.log(Server.LOG_DIAGNOSTIC, this, "Type match failed");
	    if (type == null)
		type = new String("null");
	    return false;
	}

	return true;
    }

    /**
     * Currently gets an audio request in audio/raw type, and tags on the
     * needed basic header, and stores to files.  Also passes a message
     * along to the queues to indicate the reciept of the audio clip.
     *
     * @param req {@link sunlabs.brazil.server.Request Request} object passed in by the webserver.
     * @return Returns false always.
     */
    public boolean respond(Request req)
    {
	int len;
	int headerSize = 24;
	String filename;

	byte[] audioData;

	if (!isMyRequest(req)) {
	    server.log(Server.LOG_DIAGNOSTIC, this, "Match failed, exiting respond");
	    return false;
	}
	
	len = Integer.parseInt((String)(req.headers.get("Content-Length")));
	filename = getFilename();

	audioData = BasicAudioTools.tagBasicHeader(req.postData, len, 
						   BasicAudioTools.ML8, 8000);
	BasicAudioTools.storeToFile(formattedRoot+filename, audioData, len+24);
	putMessage(cAudioRoot+filename, len+24, req);

	return false;
    }

    private void putMessage(String filename, int size, Request req)
    {
	server.log(Server.LOG_INFORMATIONAL, this, "Placing new message("+filename+", "+size+")");
	Hashtable qdata = req.getQueryData();
	String user = (String)(qdata.get("user"));
	String to = (String)(qdata.get("to"));
	// XXX hardwiring the message is BOGUS
	String message = "!link=" + filename + ":" + 
	     ((size + 4000)/8000) + " second voice message:";
	
	String meta = user+":"+to+":bt:audio/basic";
	String sessionID = req.props.getProperty("SessionID");

	server.log(Server.LOG_INFORMATIONAL, this, "Placing message: " + message);
	server.log(Server.LOG_INFORMATIONAL, this, "From: " + user + " To: " + to);
	server.log(Server.LOG_INFORMATIONAL, this, "Session: " + sessionID);
	server.log(Server.LOG_INFORMATIONAL, this, "Meta: " + meta);

	QueueTemplate.Queue fromMessageQ = (QueueTemplate.Queue)(SessionManager.getSession(user, "q", QueueTemplate.Queue.class));
	QueueTemplate.Queue toMessageQ = (QueueTemplate.Queue)(SessionManager.getSession(to, "q", QueueTemplate.Queue.class));

        if ( (user != null) && (to != null)){
	    fromMessageQ.put(new QueueTemplate.QueueItem(user, message, meta));
	    if (!user.equals(to))
		toMessageQ.put(new QueueTemplate.QueueItem(user, message, meta));
	}
    }

    private String getFilename()
    {
	String filename;
	int size;
	
	size = cClips.size();
	filename = cPre+"ac."+size+".au";
	cClips.add(filename);	

	return filename;
    }

    private String cPre;
    private MatchString cMS;
    private String cAudioRoot;
    private String cHardAudioRoot;
    private String formattedRoot;

    private Server server;
    private Vector cClips;
}
