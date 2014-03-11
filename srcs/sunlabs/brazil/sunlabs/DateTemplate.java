/*
 * DateTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.
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
 * Created by suhler on 99/11/16
 * Last modified by suhler on 02/12/19 11:39:44
 *
 * Version Histories:
 *
 * 2.2 02/12/19-11:39:44 (suhler)
 *   doc update
 *
 * 2.1 02/10/01-16:37:28 (suhler)
 *   version change
 *
 * 1.13 02/05/16-11:16:00 (suhler)
 *   doc lint
 *
 * 1.12 01/11/19-21:08:33 (suhler)
 *   fixed doc typos
 *
 * 1.11 01/11/18-20:22:48 (suhler)
 *   add date scanning
 *
 * 1.10 01/11/13-11:38:29 (suhler)
 *   allow times to be specified in ms in additions to secs.
 *
 * 1.9 01/09/13-09:26:46 (suhler)
 *   remove uneeded import
 *
 * 1.8 01/07/17-14:17:50 (suhler)
 *   doc lint
 *
 * 1.7 01/07/16-16:51:57 (suhler)
 *   use new Template convenience methods
 *
 * 1.6 01/04/04-11:45:21 (suhler)
 *   remove token
 *
 * 1.5 01/04/03-12:14:47 (suhler)
 *   changed timebase back to seconds
 *
 * 1.4 01/03/06-09:09:27 (suhler)
 *   conform to new ClockFormat api
 *
 * 1.3 01/02/27-10:58:24 (suhler)
 *   Added "timezone" option
 *   Allow parameter substitution in attributes
 *   add "Set" attribute to set a variable instead of emitting date
 *
 * 1.2 00/06/20-08:58:07 (suhler)
 *   class rename
 *
 * 1.2 99/11/16-08:57:59 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/DateTemplate.java
 *
 * 1.1 99/11/16-08:57:58 (suhler)
 *   date and time created 99/11/16 08:57:58 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.util.ClockFormat;
import sunlabs.brazil.util.ClockScan;
import java.util.Date;

/**
 * Template for doing date and time calculations and formatting.
 * <pre>
 *    &lt;date format="strftime style date string" 
 *             ?time=nnn? ?zone=nnn? ?scan="xxx"? ?set="xxx"? &gt;
 * </pre>
 * <dl>
 * <dt>format	<dd>A "strftime" style date string (defaults to "%D %T").
 * <dt>time	<dd>Number of seconds since the epoc (defaults to now);
 * <dt>ms	<dd>Number of ms since the epoc (if time is not specified)
 * <dt>scan     <dd>A human readable date string to use. <i>time</i> is
 *		used for relative dates.  If not specified, <i>time</i>
 *		or <i>ms</i> are used as the time.  The algorithm
 *		(and implementation) was borrowed from the TCL
 *		"clock scan" command.
 * <dt>zone	<dd>If specified, then the specified timezone is used.
 *		GMT is used for unrecognized zones.
 *		Oherwise the server's timezone is used. (Example: "GMT");
 * <dt>set	<dd> if specified, set the indicated variable with the
 *		result; do not substitute the result inline.
 * </dl>
 * Variable substitutions of the for ${...} are permitted for the
 * attributes.
 */

public class DateTemplate extends Template {
    public void
    tag_date(RewriteContext hr) {
	String format = hr.get("format");
	String zone = hr.get("zone");
	String set = hr.get("set");
	String scan = hr.get("scan");
	int now = (int) (System.currentTimeMillis()/1000);
	boolean ms = false;
	String str = hr.get("time");
	if (str == null) {
	    str = hr.get("ms");
	    if (str != null) {
	        ms = true;
	    }
	}

	if (debug) {
	    hr.append("<!-- " + hr.getBody() + " -->");
	    if (zone != null && !ClockFormat.haveZone(zone)) {
		hr.append("<!-- Warning, invalid timezone: " + zone + "-->");
		zone= null;
	    }
	}
	try {
	    if (ms) {
	       now = (int) (Long.parseLong(str)/1000);
	    } else if (str != null) {
	       now = Integer.parseInt(str);
	    }
	} catch (Exception e) {
	    debug(hr, "Invalid time \"" + str + "\", using *now*");
	}

	// convert a "string" to a date

        boolean convertOK = true;
	if (scan != null) {
	    Date result = ClockScan.GetDate(scan,new Date(1000 * (long) now),
		       zone);
	    if (result == null) {
		convertOK = false;
		debug(hr, "Invalid date/time string \"" + scan + "\", using *now*");
	    } else {
		now = (int) (result.getTime()/1000);
	    }
	}

	String result = ClockFormat.format(now, format, zone);
	if (set != null) {
	    if (convertOK) {
		hr.request.props.put(set, result);
	    }
	    hr.killToken();
	} else {
	    hr.append(result);
	}
    }
}
