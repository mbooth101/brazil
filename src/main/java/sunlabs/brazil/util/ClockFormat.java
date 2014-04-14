/*
 * ClockFormat.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2004 Sun Microsystems, Inc.
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
 * Last modified by suhler on 04/12/30 12:38:48
 *
 * Version Histories:
 *
 * 2.2 04/12/30-12:38:48 (suhler)
 *   javadoc fixes
 *
 * 2.1 02/10/01-16:37:28 (suhler)
 *   version change
 *
 * 1.5 01/04/04-11:45:05 (suhler)
 *   fixed %s
 *
 * 1.4 01/04/03-12:14:33 (suhler)
 *   changed timebase back to seconds
 *
 * 1.3 01/03/06-09:09:02 (suhler)
 *   time base is now ms, not sec.  Add additional % options
 *
 * 1.2 01/02/27-10:57:06 (suhler)
 *   added timezone parameter
 *
 * 1.2 99/11/16-08:57:59 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/ClockFormat.java
 *
 * 1.1 99/11/16-08:57:58 (suhler)
 *   date and time created 99/11/16 08:57:58 by suhler
 *
 */

package sunlabs.brazil.util;

/*
 * ClockFormat.java --
 * Shamelessly stolen from jacl
 *
 * Copyright (c) 1999 Sun Microsystems, Inc.
 * Copyright (c) 1998 Christian Krone.
 * Copyright (c) 1997 Cornell University.
 * Copyright (c) 1995-1997 Sun Microsystems, Inc.
 * Copyright (c) 1992-1995 Karl Lehenbauer and Mark Diekhans.
 *
 * From: ClockCmd.java,v 1.2 1999/05/16 06:16:37 dejong Exp $
 *
 */

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This class implements the "strftime" style clock format command.
 * It decodes the following %X format strings:
 * <dl>
 * <dt>'%%' <dd>Insert a %. 
 * <dt>'%A' <dd>Full weekday name (Monday, Tuesday, etc.). 
 * <dt>'%a' <dd>Abbreviated weekday name (Mon, Tue, etc.). 
 * <dt>'%B' <dd>Full month name. 
 * <dt>'%C' <dd>Century (00 - 99).
 * <dt>'%c' <dd>Locale specific date and time. 
 * <dt>'%D' <dd>Date as %m/%d/%y. 
 * <dt>'%d' <dd>Day of month (01 - 31). 
 * <dt>'%e' <dd>Day of month (1 - 31), no leading zeros. 
 * <dt>'%H' <dd>Hour in 24-hour format (00 - 23). 
 * <dt>'%h' <dd>Abbreviated month name (Jan,Feb,etc.). 
 * <dt>'%I' <dd>Hour in 12-hour format (01 - 12). 
 * <dt>'%j' <dd>Day of year (001 - 366). 
 * <dt>'%k' <dd>Hour in 24-hour format (0 - 23), no leading zeros. 
 * <dt>'%l' <dd>Hour in 12-hour format (1 - 12), no leading zeros. 
 * <dt>'%M' <dd>Minute (00 - 59). 
 * <dt>'%m' <dd>Month number (01 - 12). 
 * <dt>'%n' <dd>Insert a newline. 
 * <dt>'%p' <dd>AM/PM indicator. 
 * <dt>'%R' <dd>Time as %H:%M. 
 * <dt>'%r' <dd>Time as %I:%M:%S %p. 
 * <dt>'%S' <dd>Seconds (00 - 59). 
 * <dt>'%s' <dd>seconds since epoch. 
 * <dt>'%T' <dd>Time as %H:%M:%S. 
 * <dt>'%t' <dd>Insert a tab. 
 * <dt>'%U' <dd>Week of year (01-52), Sunday is first day.
 * <dt>'%u' <dd>Weekday number (1 - 7) Sunday = 7. 
 * <dt>'%V' <dd>ISO 8601 Week Of Year (01 - 53). 
 * <dt>'%W' <dd>Week of year (01-52), Monday is first day. 
 * <dt>'%w' <dd>Weekday number (0 - 6) Sunday = 0. 
 * <dt>'%X' <dd>Locale specific time format. 
 * <dt>'%x' <dd>Locale specific date format. 
 * <dt>'%Y' <dd>Year with century (e.g. 1990) 
 * <dt>'%y' <dd>Year without century (00 - 99). 
 * <dt>'%Z' <dd>Time zone name. 
 * </dl>
 */

public class ClockFormat {

/**
 * Formats a time value based on seconds into a human readable string.
 *
 * @param clockVal	Seconds since the epoch
 * @param format	The strftime style format string. If format is 
 *			null, then "%a %b %d %H:%M:%S %Z %Y" is used.
 * @param zone		The time zone abbreviation (e.g. GMT, or PST)
 * @return		The formatted string.
 *
 */

public static String
format(int clockVal, String format, String zone) {
    Date date = new Date((long)(clockVal)*1000);
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat fmt, locFmt;
    FieldPosition fp = new FieldPosition(0);

    if (format == null) {
	format = "%a %b %d %H:%M:%S %Z %Y";
    }

    calendar.setTime(date);
    if (zone != null) {
        calendar.setTimeZone(TimeZone.getTimeZone(zone));
    }
    fmt = new SimpleDateFormat("mm.dd.yy", Locale.US);
    fmt.setCalendar(calendar);
	
    StringBuffer result = new StringBuffer();
    for (int ix = 0; ix < format.length(); ix++) {
        if (format.charAt(ix) == '%' && ix+1 < format.length()) {
	    switch (format.charAt(++ix)) {
	        case '%': // Insert a %. 
		    result.append('%');
		    break;
	        case 'a': // Abbreviated weekday name (Mon, Tue, etc.). 
		    fmt.applyPattern("EEE");
		    fmt.format(date, result, fp);
		    break;
	        case 'A': // Full weekday name (Monday, Tuesday, etc.). 
		    fmt.applyPattern("EEEE");
		    fmt.format(date, result, fp);
		    break;
	        case 'b': case 'h': // Abbreviated month name (Jan,Feb,etc.). 
		    fmt.applyPattern("MMM");
		    fmt.format(date, result, fp);
		    break;
	        case 'B': // Full month name. 
		    fmt.applyPattern("MMMM");
		    fmt.format(date, result, fp);
		    break;
	        case 'c': // Locale specific date and time. 
		    locFmt = (SimpleDateFormat)DateFormat.getDateTimeInstance(
			          DateFormat.SHORT, DateFormat.SHORT);
		    locFmt.setCalendar(calendar);
		    locFmt.format(date, result, fp);
		    break;
	        case 'C': // Century (00 - 99).
		    int century = calendar.get(Calendar.YEAR)/100;
		    result.append((century < 10 ? "0" : "") + century);
		    break;
	        case 'd': // Day of month (01 - 31). 
		    fmt.applyPattern("dd");
		    fmt.format(date, result, fp);
		    break;
	        case 'D': // Date as %m/%d/%y. 
		    fmt.applyPattern("MM/dd/yy");
		    fmt.format(date, result, fp);
		    break;
	        case 'e': // Day of month (1 - 31), no leading zeros. 
		    fmt.applyPattern("d");
		    String day = fmt.format(date);
		    result.append((day.length() < 2 ? " " : "") + day);
		    break;
	        case 'H': // Hour in 24-hour format (00 - 23). 
		    fmt.applyPattern("HH");
		    fmt.format(date, result, fp);
		    break;
	        case 'I': // Hour in 12-hour format (01 - 12). 
		    fmt.applyPattern("hh");
		    fmt.format(date, result, fp);
		    break;
	        case 'j': // Day of year (001 - 366). 
		    fmt.applyPattern("DDD");
		    fmt.format(date, result, fp);
		    break;
	        case 'k': // Hour in 24-hour format (0 - 23), no leading zeros. 
		    fmt.applyPattern("H");
		    String h24 = fmt.format(date);
		    result.append((h24.length() < 2 ? " " : "") + h24);
		    break;
	        case 'l': // Hour in 12-hour format (1 - 12), no leading zeros. 
		    fmt.applyPattern("h");
		    String h12 = fmt.format(date);
		    result.append((h12.length() < 2 ? " " : "") + h12);
		    break;
	        case 'm': // Month number (01 - 12). 
		    fmt.applyPattern("MM");
		    fmt.format(date, result, fp);
		    break;
	        case 'M': // Minute (00 - 59). 
		    fmt.applyPattern("mm");
		    fmt.format(date, result, fp);
		    break;
	        case 'n': // Insert a newline. 
		    result.append('\n');
		    break;
	        case 'p': // AM/PM indicator. 
		    fmt.applyPattern("aa");
		    fmt.format(date, result, fp);
		    break;
	        case 'r': // Time as %I:%M:%S %p. 
		    fmt.applyPattern("KK:mm:ss aaaa");
		    fmt.format(date, result, fp);
		    break;
	        case 'R': // Time as %H:%M. 
		    fmt.applyPattern("hh:mm");
		    fmt.format(date, result, fp);
		    break;
	        case 's': // seconds since epoch. 
		    result.append((int) (calendar.getTime().getTime()/1000));
		    break;
	        case 'S': // Seconds (00 - 59). 
		    fmt.applyPattern("ss");
		    fmt.format(date, result, fp);
		    break;
	        case 't': // Insert a tab. 
		    result.append('\t');
		    break;
	        case 'T': // Time as %H:%M:%S. 
		    fmt.applyPattern("hh:mm:ss");
		    fmt.format(date, result, fp);
		    break;
	        case 'u': // Weekday number (1 - 7) Sunday = 7. 
		    int dayOfWeek17 = calendar.get(Calendar.DAY_OF_WEEK);
		    if (dayOfWeek17 == calendar.SUNDAY) {
		        result.append(7);
		    } else {
		        result.append(dayOfWeek17 - Calendar.SUNDAY);
		    }
		    break;
	        case 'U': // Week of year (01-52), Sunday is first day.
		    int weekS = GetWeek(calendar, Calendar.SUNDAY, false);
		    result.append((weekS < 10 ? "0" : "") + weekS);
		    break;
	        case 'V': // ISO 8601 Week Of Year (01 - 53). 
		    int isoWeek = GetWeek(calendar, Calendar.MONDAY, true);
		    result.append((isoWeek < 10 ? "0" : "") + isoWeek);
		    break;
	        case 'w': // Weekday number (0 - 6) Sunday = 0. 
		    int dayOfWeek06 = calendar.get(Calendar.DAY_OF_WEEK);
		    result.append(dayOfWeek06-calendar.SUNDAY);
		    break;
	        case 'W': // Week of year (01-52), Monday is first day. 
		    int weekM = GetWeek(calendar, Calendar.MONDAY, false);
		    result.append((weekM < 10 ? "0" : "") + weekM);
		    break;
	        case 'x': // Locale specific date format. 
		    locFmt = (SimpleDateFormat)DateFormat.getDateInstance(
						   DateFormat.SHORT);
		    locFmt.setCalendar(calendar);
		    locFmt.format(date, result, fp);
		    break;
	        case 'X': // Locale specific time format. 
		    locFmt = (SimpleDateFormat)DateFormat.getTimeInstance(
						   DateFormat.SHORT);
		    locFmt.setCalendar(calendar);
		    locFmt.format(date, result, fp);
		    break;
	        case 'y': // Year without century (00 - 99). 
		    fmt.applyPattern("yy");
		    fmt.format(date, result, fp);
		    break;
	        case 'Y': // Year with century (e.g. 1990) 
		    fmt.applyPattern("yyyy");
		    fmt.format(date, result, fp);
		    break;
	        case 'Z': // Time zone name. 
		    fmt.applyPattern("zzz");
		    fmt.format(date, result, fp);
		    break;
	        default:
		    result.append(format.charAt(ix));
		    break;
	    }
	} else {
	  result.append(format.charAt(ix));
	}
    }
    return (result.toString());
}

/**
 * Adjust the base time based on specified timezone
 */

static long adjustMillis(Calendar calendar, String zone) {
    long millis = calendar.getTime().getTime();
    if (zone != null) {
	Calendar localCalendar = Calendar.getInstance();
	localCalendar.setTimeZone(TimeZone.getTimeZone(zone));
	localCalendar.setTime(calendar.getTime());
	millis -= localCalendar.get(Calendar.ZONE_OFFSET)
		+ localCalendar.get(Calendar.DST_OFFSET);
    }
    return millis;
}

/**
 *-----------------------------------------------------------------------------
 *
 * GetWeek --
 *
 *      Returns the week_of_year of the given date.
 *	The weekday considered as start of the week is given as argument.
 *	Specify iso as true to get the week_of_year accourding to ISO.
 *
 * Results:
 *      Day of the week .
 *
 *-----------------------------------------------------------------------------
 */

private static int
GetWeek(
    Calendar calendar,		// Calendar containing Date.
    int firstDayOfWeek,		// this day starts a week (MONDAY/SUNDAY).
    boolean iso			// evaluate according to ISO?
)
{
    if (iso) {
        firstDayOfWeek = Calendar.MONDAY;
    }

    // After changing the firstDayOfWeek, we have to set the time value anew,
    // so that the fields of the calendar are recalculated.


    calendar.setFirstDayOfWeek(firstDayOfWeek);
    calendar.setMinimalDaysInFirstWeek(iso ? 4 : 7);
    calendar.setTime(calendar.getTime());
    int week = calendar.get(Calendar.WEEK_OF_YEAR);

    if (!iso) {
	// The week for the first days of the year may be 52 or 53.
	// But here we have to return 0, if we don't compute ISO week.
	// So any bigger than 50th week in January will become 00.

	if (calendar.get(Calendar.MONTH) == Calendar.JANUARY && week > 50) {
	    week = 0;
	}
    }
    return week;
}

/**
 * See if a particular timezone is valid
 */

public static boolean
haveZone(String zone) {
    TimeZone t = TimeZone.getTimeZone(zone);
    String ok = t.getID();
    return (zone.equals(ok));
}

/**
 * Test main:
 * ClockFormat format ?time? ?zone?
 */

public static void
main(String[] args) throws Exception {
    int time =(int) (System.currentTimeMillis()/1000);
    try {
	time = Integer.decode(args[1]).intValue();
    } catch (Exception e) {}
    String zone = null;
    try {
	zone = args[2];
    } catch (Exception e) {}
    try {
	System.out.println(format(time, args[0], zone));
    } catch (Exception e) {
	System.out.println("Usage: ClockFormat <format> ?<sec>? ?<zone>?");
    }
}
}
