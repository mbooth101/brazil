/*
 * MenuTemplate.java
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
 * Version:  2.1
 * Created by lc138592 on 02/07/18
 * Last modified by suhler on 02/10/01 16:36:01
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:36:01 (suhler)
 *   version change
 *
 * 1.7 02/09/10-11:03:09 (suhler)
 *   add "selected" and "endselected" options
 *
 * 1.6 02/08/06-11:24:12 (suhler)
 *   remove * imports
 *
 * 1.5 02/08/05-17:01:51 (lc138592)
 *   Added Javadocs
 *
 * 1.4 02/07/26-21:44:24 (suhler)
 *   use replace instead of replaceAll:
 *   - replaceAll is a 1.4 feature, not 1.1
 *
 * 1.3 02/07/26-15:50:13 (lc138592)
 *   stylistic changed, also removed &lt and &gt hardcoded replacements
 *
 * 1.2 02/07/19-15:06:51 (lc138592)
 *   Removed &lt and &gt, fixed entires updating
 *
 * 1.2 70/01/01-00:00:02 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 slim/MenuTemplate.java
 *
 * 1.1 02/07/18-17:33:49 (lc138592)
 *   date and time created 02/07/18 17:33:49 by lc138592
 *
 */

package sunlabs.brazil.slim;

// Leo Chao - July 10, 2002
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import sunlabs.brazil.properties.PropertiesList;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.util.http.HttpUtil;

/**
 * This is a template for producing hypertext based menus in a 
 * brazil powered webpage.  At this state you may use div and prepend
 * to format the entires in the manner you desire. Essentially div
 * inserts the string between each entry and prepend inserts the string
 * before each entry.
 * <p>
 * The query parameters are:
 * <br>
 * <dl>
 * <dt>title</dt> 
 * <dd>the top level name of the menu to display</dd>
 * <dt>name</dt>
 * <dd>the name of the variable to associate with the menu choice</dd>
 * <dt>page</dt>
 * <dd>the page to refresh to</dd>
 * <dt>options</dt>
 * <dd>the options available in the following format:<br>
 *           <i>value</i>:<i>label</i>|<i>value</i>:<i>label</i>|...</dd>
 * <dt>div</dt>
 * <dd>a character or string to use to divide the options for a particular menu.  This defaults to '|'.</dd>
 * <dt>pre</dt>
 * <dd>puts the string in front of each entry.  Defaults to the empty string</dd>
 * <dt>forcequery</dt> 
 * <dd>force the presence of a query parameter whenever a menu click occurs.  This is a hack.</dd>
 * <dt>selected, endselected</dt> 
 * <dd>The markup to identify a selected item.  Defaults to
 *     &lt;b&gt; and &lt;/b&gt;.
 * </dl><p>
 * The list of menus is maintain OUT OF SESSION state in the class itself
 * using a Hashtable, this will need to be changed.
 *
 * @author Leo Chao
 */
public class MenuTemplate extends Template
{
    /**
     * Initializes internal data structures.
     *
     * @param The RewriteContext passed into the template by the web server.
     * @return Returns true always
     */
    public boolean init(RewriteContext hr)
    {
	if (!started) {
	    started=true;
	    cMenus = new Hashtable();
	}
	server = hr.server;
	return true;
    }

    /**
     * This method implements the linkmenu tag.  See above for what query 
     * parameters the tag is expecting.
     *
     * @param The RewriteContext passed into the template by the web server.    
     */
    public void tag_linkmenu(RewriteContext hr)
    {
	String header, name, entries, current, divider, page, open, close, force, prepend, key, titlelink;
	LinkMenu LM;
	server.log(Server.LOG_DIAGNOSTIC, this, "Processing linkmenu");
	PropertiesList query = hr.request.props;

	//Process parameters
	name = (String)hr.get("name");
	page = (String)hr.get("page");
	header = (String)hr.get("title", name);
	entries = (String)hr.get("options", "");
	prepend = (String)hr.get("pre", "");
	divider = (String)hr.get("div", "|");
	titlelink = (String)hr.get("tlink");
	force = (String)hr.get("forcequery", "force=null");

	String onPre=hr.get("selected","<b>");
	String onPost=hr.get("endselected","</b>");

	key = name+":"+header.replace(' ', '_');
	server.log(Server.LOG_DIAGNOSTIC, this, "key is " + key);
	open = (String)query.getProperty("query.expand");
	close = (String)query.getProperty("query.close");
	
	if ((name == null) || (page == null)) {
	    server.log(Server.LOG_ERROR, this, "Both name and page must be specified");
	    return;
	}
	
	server.log(Server.LOG_DIAGNOSTIC, this, "entries (" + entries + ")");
	
	if (open != null) {
	    server.log(Server.LOG_DIAGNOSTIC, this, "open is: "  + open);
	    LM = (LinkMenu)(cMenus.get(open));
	    if (LM == null)
		server.log(Server.LOG_WARNING, this, open + " is not a valid LinkMenu");
	    else
		LM.setOpen();
	} else if (close != null) {
	    server.log(Server.LOG_DIAGNOSTIC, this, "close is: " + close);
	    LM = (LinkMenu)(cMenus.get(close));
	    LM.setClosed();
	}
	

	// Maintain menu state

	//used to determine the currently selected value
	current = hr.request.props.getProperty(name);  
	
	if (!cMenus.containsKey(key)) {
	    server.log(Server.LOG_DIAGNOSTIC, this, "creating new menu: "+name);
	    LM = new LinkMenu(header, name, entries);
	    cMenus.put(key, LM);
	}
	else {
	    LM = (LinkMenu)(cMenus.get(key));
	    LM.updateEntries(entries);
	    server.log(Server.LOG_DIAGNOSTIC, this, "Menu " + LM.cName + " retrieved.");
	}
	LM.cCur = current;

	// Produce the menu in its current state
	String qname = HttpUtil.htmlEncode(LM.cName);
	String qkey = LM.cName +":" + LM.cHeader.replace(' ', '_');
	if(LM.isOpen()) {
	    if(titlelink != null) {
		hr.append("<i>"+
                 "<a href=\""+page+"?"+force+"&close="+qkey+"\">-</a>" +
                 "<a href=\""+titlelink+"\">"+LM.cHeader +
		 "</a>:</i> ");
	    }
	    else {
		hr.append("<i><a href=\""+page+"?"+force+"&close="+qkey+"\">"+LM.cHeader+"</a>:</i> ");
	    }
	    int n = LM.cEntries.size();
	    int i;

	    for (i=0;i<n;i++) {
		LinkMenuItem lmi = (LinkMenuItem)(LM.cEntries.elementAt(i));
		if (lmi.value.equals(LM.cCur))
		    hr.append(onPre);

		if (i+1 != n) {
		    hr.append(prepend+"<a href=\""+page+"?"+force+"&setName="+qname+"&setValue="+lmi.value+"\">"+lmi.name+"</a> "+divider);
		}
		else {
		    hr.append(prepend+"<a href=\""+page+"?"+force+"&setName="+qname+"&setValue="+lmi.value+"\">"+lmi.name+"</a>");
		}
		if (lmi.value.equals(LM.cCur))
		    hr.append(onPost);
		server.log(Server.LOG_DIAGNOSTIC, this, "Producing menu item: " + lmi);
	    }
	}
	else {
	    if(titlelink != null) {
		hr.append("<a href=\""+page+"?"+force+"&expand="+qkey+"\">+</a>" +
                 "<a href=\""+titlelink+"\">"+LM.cHeader +
		 "</a>");
	    }
	    else {
		hr.append("<a href=\""+page+"?"+force+"&expand="+qkey+"\">"+LM.cHeader+"</a>");
	    }
	}
    }


    /*
     * The internal data structure for a link menu, uses a Vector to track options
     */
    private static class LinkMenu
    {
	public LinkMenu (String header, String name, String entries)
	{
	    cName = name;
	    cHeader = header;
	    processEntries(entries);
	    cExpanded = false;
	}

	public boolean isOpen()
	{
	    return cExpanded;
	}
	
	public void setOpen()
	{
	    cExpanded = true;
	}

	public void setClosed()
	{
	    cExpanded = false;
	}

	public void toggleOpen()
	{
	    if (cExpanded)
		cExpanded = false;
	    else
		cExpanded = true;
	}

	public void updateEntries(String entries)
	{
	    processEntries(entries);
	}

	// This merely parses the options parameter and puts the values
	// into LinkMenuItems and drops those into the cEntires vector
	private void processEntries(String entries)
	{
	    String entry, value, name;
	    StringTokenizer toker = new StringTokenizer(entries, "|");
	    cEntries = new Vector();

	    while(toker.hasMoreElements()) {
		entry = toker.nextToken();
		StringTokenizer breaker = new StringTokenizer(entry, ":");
		value = breaker.nextToken();
		name = breaker.nextToken();
		server.log(Server.LOG_DIAGNOSTIC, this, "["+entry+"] Entry: " + name + " (" + value+") added to LinkMenu " + cName);
		MenuTemplate.LinkMenuItem lmi = new LinkMenuItem(value, name);
		cEntries.add(lmi);
	    }
	}

	public String cName;
	public String cHeader;
	public Vector cEntries;
	public String cCur;
	
	private boolean cExpanded;
    }

    // A struct to maintian link menu item data
    private static class LinkMenuItem
    {
	public LinkMenuItem(String nvalue, String nname)
	{
	    value = nvalue;
	    name = nname;
	}

	public String toString()
	{
	    return "LinkMenuItem " + name + " = " + value;
	}

	public String value;
	public String name;
    }

    private static Hashtable cMenus;

    private static boolean started=false;
    private static Server server;
}
