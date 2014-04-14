/*
 * DialogHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1998-2006 Sun Microsystems, Inc.
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
 * Version:  2.2
 * Created by suhler on 98/12/04
 * Last modified by suhler on 06/11/13 15:02:08
 *
 * Version Histories:
 *
 * 2.2 06/11/13-15:02:08 (suhler)
 *   move MatchString to package "util" from "handler"
 *
 * 2.1 02/10/01-16:36:29 (suhler)
 *   version change
 *
 * 1.11 01/07/20-11:31:34 (suhler)
 *   MatchUrl -> MatchString
 *
 * 1.10 01/07/17-14:16:20 (suhler)
 *   use MatchUrl
 *
 * 1.9 00/12/11-13:28:02 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.8 00/05/31-13:46:44 (suhler)
 *   doc cleanup
 *
 * 1.7 00/04/20-13:42:31 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.6 00/04/20-11:49:42 (cstevens)
 *   copyright.
 *
 * 1.5.1.1 00/04/18-16:48:15 (suhler)
 *
 * 1.5 99/10/07-12:58:34 (cstevens)
 *   javadoc lint.
 *
 * 1.4 99/03/30-09:32:06 (suhler)
 *   documentation update
 *
 * 1.3 98/12/06-16:38:22 (suhler)
 *   handler rename
 *
 * 1.2 98/12/04-14:22:30 (suhler)
 *   fixed import list
 *
 * 1.2 98/12/04-14:16:51 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 handlers/DialogHandler.java
 *   Name history : 1 0 handlers/Dialog.java
 *
 * 1.1 98/12/04-14:16:50 (suhler)
 *   date and time created 98/12/04 14:16:50 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.io.IOException;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.MatchString;

/**
 * Sample handler for popping up a dialog box on the server.
 * This is used for interactive authentication of web pages, allowing
 * an <i>operator</i> on the server's computer to allow or deny access
 * to pages on a per request basis.
 * Input parameters examined in the request properties:
 * <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URL that triggers this handler.
 * (See {@link MatchString}).
 * <dt>default<dd>The message to appear in the dialog box.
 * Defaults to <b>Request from Client</b>.
 * <dt>denied<dd>The message to appear in the "permission denied" spot.
 * </dl>
 * If query data is present, it is used as the message.
 * <p>
 * Note:  This is the only class in the entire system that requires AWT.
 *
 * @author      Stephen Uhler
 * @version     2.2, 06/11/13
 */

public class DialogHandler extends java.applet.Applet implements Handler {

/*
 * The user interface components
 */

/**
 * @serial
 */
public Panel frame_1;

/**
 * @serial
 */
public Button ok;

/**
 * @serial
 */
public Label title;

/**
 * @serial
 */
public Button cancel;

MatchString isMine;            // check for matching url
String prefix;		// my properties prefix

/**
 * @serial
 */
Frame f;		// The dialog box

/**
 * @serial
 */
Dimension screen;	// The screen dimentions - so we can do centering

/**
 * @serial
 */
boolean abort;		// True if the user hit "cancel"

/**
 * @serial
 */
String message = "User Request for URL";		// Default message

/**
 * Do one time initialization.
 */
public boolean
init(Server server, String prefix) {
    this.prefix = prefix;
    isMine = new MatchString(prefix);
    f = new Frame("Message from Client");
    screen = f.getToolkit().getScreenSize();
    init();
    f.add("Center", this);
    f.pack();
    return true;
}

/**
 * Pop up a dialog box on the server machine.
 * Allow the operator to select <b>yes</b> or <b>no</b>.
 * If the request is allowed, it is passed on to the next handler.
 */

public boolean
respond(Request request) throws IOException {
    if (!isMine.match(request.url, request.props)) {
        return false;
    }
    message = request.props.getProperty(prefix + "default",
	"Request from Client");
    String denied = request.props.getProperty(prefix + "denied",
	"<title>denied!</title>Requested URL denied by server");
    if (request.query.length() == 0) {
	title.setText(message);
    } else {
	title.setText(request.query);
    }
    Dimension size = f.getSize();
    try {
	synchronized (f) {
	    f.setVisible(true);
	    f.setLocation((screen.width-size.width)/2,
	    		(screen.height-size.height)/2);
	    while (f.isVisible()) {
		f.wait();
	    }
	}
    } catch (InterruptedException e) {
    	request.log(Server.LOG_WARNING,"Got: " + e);
    }
    if (abort) {
        request.sendResponse(denied, "text/html", 404);
        return true;
    } else {
	return false;
    }
}

public void dismiss(boolean how) {
    synchronized (f) {
    	abort = !how;
    	f.setVisible(false);
    	f.notifyAll();
    }
}

/**
 * Machine generated code.
 * Everything after here was automatically generated
 * SpecTcl generated class Dialog, version 1.0
 */

public void init() {
	// main panel
	GridBagLayout grid = new GridBagLayout();
	int rowHeights[] = {0,30,20};
	int columnWidths[] = {0,30};
	double rowWeights[] = {0.0,0.0,0.0};
	double columnWeights[] = {0.0,0.0};
	grid.rowHeights = rowHeights;
	grid.columnWidths = columnWidths;
	grid.rowWeights = rowWeights;
	grid.columnWeights = columnWeights;

	// container frame_1 in this.
	GridBagLayout frame_1_grid = new GridBagLayout();
	int frame_1_rowHeights[] = {0,14};
	int frame_1_columnWidths[] = {0,30,30};
	double frame_1_rowWeights[] = {0.0,0.0};
	double frame_1_columnWeights[] = {0.0,0.0,0.0};
	frame_1_grid.rowHeights = frame_1_rowHeights;
	frame_1_grid.columnWidths = frame_1_columnWidths;
	frame_1_grid.rowWeights = frame_1_rowWeights;
	frame_1_grid.columnWeights = frame_1_columnWeights;

	frame_1 = new Panel();
	this.add(frame_1);
	
	ok = new Button();
	ok.setLabel("OK");
	frame_1.add(ok);
	
	title = new Label();
	title.setFont(new Font("Helvetica",Font.PLAIN + Font.BOLD , 18));
	title.setText(message);
	title.setAlignment(Label.CENTER);
	this.add(title);
	
	cancel = new Button();
	cancel.setLabel("Cancel");
	frame_1.add(cancel);
	
	// Geometry management
	GridBagConstraints con = new GridBagConstraints();
	reset(con);
	con.gridx = 1;
	con.gridy = 2;
	con.anchor = GridBagConstraints.CENTER;
	con.fill = GridBagConstraints.NONE;
	grid.setConstraints(frame_1, con);

	reset(con);
	con.gridx = 1;
	con.gridy = 1;
	con.anchor = GridBagConstraints.CENTER;
	con.fill = GridBagConstraints.NONE;
	frame_1_grid.setConstraints(ok, con);

	reset(con);
	con.gridx = 1;
	con.gridy = 1;
	con.anchor = GridBagConstraints.CENTER;
	con.fill = GridBagConstraints.NONE;
	grid.setConstraints(title, con);

	reset(con);
	con.gridx = 2;
	con.gridy = 1;
	con.anchor = GridBagConstraints.CENTER;
	con.fill = GridBagConstraints.NONE;
	frame_1_grid.setConstraints(cancel, con);

	// Resize behavior management and parent heirarchy
	setLayout(grid);
	frame_1.setLayout(frame_1_grid);
}

public boolean handleEvent(Event event) {
	if (event.target == ok && event.id == event.ACTION_EVENT) {
		dismiss(true);
	} else if (event.target == cancel && event.id == event.ACTION_EVENT) {
		dismiss(false);
	} else {
		return super.handleEvent(event);
	}
	return true;
}

private void reset(GridBagConstraints con) {
    con.gridx = GridBagConstraints.RELATIVE;
    con.gridy = GridBagConstraints.RELATIVE;
    con.gridwidth = 1;
    con.gridheight = 1;
 
    con.weightx = 0;
    con.weighty = 0;
    con.anchor = GridBagConstraints.CENTER;
    con.fill = GridBagConstraints.NONE;
 
    con.insets = new Insets(0, 0, 0, 0);
    con.ipadx = 0;
    con.ipady = 0;
}
}
