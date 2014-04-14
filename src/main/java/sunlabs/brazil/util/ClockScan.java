/*
 * ClockScan.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.
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
 * Version:  2.1
 * Created by suhler on 01/11/18
 * Last modified by suhler on 02/10/01 16:37:32
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:37:32 (suhler)
 *   version change
 *
 * 1.3 02/07/24-10:49:01 (suhler)
 *   doc updates
 *
 * 1.2 01/11/18-20:23:00 (suhler)
 *   remove * imports
 *
 * 1.2 01/11/18-20:04:32 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/ClockScan.java
 *
 * 1.1 01/11/18-20:04:31 (suhler)
 *   date and time created 01/11/18 20:04:31 by suhler
 *
 */

package sunlabs.brazil.util;

import java.text.DateFormatSymbols;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

/**
 * This class implements time and date scanning.
 * It was adapted from the TCL implementation found in JACL.
 */

/*
 *	Shamelessly stolen from the the TCL clock command
 *
 * Copyright (c) 2001 Sun Microsystems
 * Copyright (c) 1998 Christian Krone.
 * Copyright (c) 1997 Cornell University.
 * Copyright (c) 1995-1997 Sun Microsystems, Inc.
 * Copyright (c) 1992-1995 Karl Lehenbauer and Mark Diekhans.
 */

public class ClockScan {

static final int EPOCH_YEAR = 1970;
public static boolean debug = false;

/**
 *      Scan a human readable date string and construct a Date.
 * <p>
 *
 * Results:
 *      The scanned date (or null, if an error occured).
 */

public static Date
GetDate(
    String dateString,		// Date string to scan
    Date baseDate,		// Date to use as base
    String zone)		// Timezone, or local if null
{
    Calendar calendar = Calendar.getInstance();
    Calendar now = Calendar.getInstance();
    if (baseDate == null) {
        baseDate = new Date(System.currentTimeMillis()/1000);
    }
    now.setTime(baseDate);
    calendar.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
	now.get(Calendar.DAY_OF_MONTH), 0, 0 ,0);
    if (zone != null) {
	calendar.setTimeZone(TimeZone.getTimeZone(zone));
    }

    ClockToken[] dt = GetTokens(dateString);

    ParsePosition parsePos = new ParsePosition(0);
    ClockRelTimespan diff = new ClockRelTimespan();
    int hasTime = 0;
    int hasZone = 0;
    int hasDate = 0;
    int hasDay = 0;
    int hasRel = 0;

    while (parsePos.getIndex() < dt.length) {
        if (ParseTime(dt, parsePos, calendar)) {
	    hasTime++;
	} else if (ParseZone(dt, parsePos, calendar)) {
	    hasZone++;
	} else if (ParseDate(dt, parsePos, calendar)) {
	    hasDate++;
	} else if (ParseDay(dt, parsePos, calendar)) {
	    hasDay++;
	} else if (ParseRel(dt, parsePos, diff)) {
	    hasRel++;
	} else if (ParseNumber(dt, parsePos, calendar,
			       hasDate > 0 && hasTime > 0 && hasRel == 0)) {
	    if (hasDate == 0 || hasTime == 0 || hasRel > 0) {
	        hasTime++;
	    }
	} else {
	    return null;
	}
    }

    if (hasTime > 1 || hasZone > 1 || hasDate > 1 || hasDay > 1) {
        return null;
    }

    // The following line handles years that are specified using
    // only two digits.  The line of code below implements a policy
    // defined by the X/Open workgroup on the millinium rollover.
    // Note: some of those dates may not actually be valid on some
    // platforms.  The POSIX standard startes that the dates 70-99
    // shall refer to 1970-1999 and 00-38 shall refer to 2000-2038.
    // This later definition should work on all platforms.

    int thisYear = calendar.get(Calendar.YEAR);
    if (thisYear < 100) {
        if (thisYear >= 69) {
	    calendar.set(Calendar.YEAR, thisYear+1900);
	} else {
	    calendar.set(Calendar.YEAR, thisYear+2000);
	}
    }

    if (hasRel > 0) {
        if (hasTime == 0 && hasDate == 0 && hasDay == 0) {
	    calendar.setTime(baseDate);
	}
	calendar.add(Calendar.SECOND, diff.getSeconds());
	calendar.add(Calendar.MONTH, diff.getMonths());
    }

    return calendar.getTime();
}

/**
 *      Parse a time string and sets the Calendar.
 *	A time string is valid, if it confirms to the following yacc rule:
 * <pre>
 *	time    : tUNUMBER tMERIDIAN
 *	        | tUNUMBER ':' tUNUMBER o_merid
 *	        | tUNUMBER ':' tUNUMBER tSNUMBER
 *	        | tUNUMBER ':' tUNUMBER ':' tUNUMBER o_merid
 *	        | tUNUMBER ':' tUNUMBER ':' tUNUMBER tSNUMBER
 *	        ;
 *</pre>
 *<p>
 * Results:
 *      True, if a time was read (parsePos was incremented and calendar
 *	was set according to the read time); false otherwise.
 */

private static boolean
ParseTime (
    ClockToken[] dt,		// Input as scanned array of tokens
    ParsePosition parsePos,	// Current position in input
    Calendar calendar		// calendar object to set
)
{
    int pos = parsePos.getIndex();

    if (pos+5 < dt.length &&
	dt[pos].isUNumber() &&
	dt[pos+1].is(':') &&
	dt[pos+2].isUNumber() &&
	dt[pos+3].is(':') &&
	dt[pos+4].isUNumber() &&
	dt[pos+5].isSNumber()) {
	calendar.set(Calendar.HOUR, dt[pos].getInt());
	calendar.set(Calendar.MINUTE, dt[pos+2].getInt());
	calendar.set(Calendar.SECOND, dt[pos+4].getInt());
        parsePos.setIndex(pos+6);
        return true;
    }
    if (pos+4 < dt.length &&
	dt[pos].isUNumber() &&
	dt[pos+1].is(':') &&
	dt[pos+2].isUNumber() &&
	dt[pos+3].is(':') &&
	dt[pos+4].isUNumber()) {
        parsePos.setIndex(pos+5);
	ParseMeridianAndSetHour(dt, parsePos, calendar, dt[pos].getInt());
	calendar.set(Calendar.MINUTE, dt[pos+2].getInt());
	calendar.set(Calendar.SECOND, dt[pos+4].getInt());
        return true;
    } 
    if (pos+3 < dt.length &&
	dt[pos].isUNumber() &&
	dt[pos+1].is(':') &&
	dt[pos+2].isUNumber() &&
	dt[pos+3].isSNumber()) {
	calendar.set(Calendar.HOUR, dt[pos].getInt());
	calendar.set(Calendar.MINUTE, dt[pos+2].getInt());
        parsePos.setIndex(pos+4);
        return true;
    }
    if (pos+2 < dt.length &&
	dt[pos].isUNumber() &&
	dt[pos+1].is(':') &&
	dt[pos+2].isUNumber()) {
        parsePos.setIndex(pos+3);
	ParseMeridianAndSetHour(dt, parsePos, calendar, dt[pos].getInt());
	calendar.set(Calendar.MINUTE, dt[pos+2].getInt());
        return true;
    }
    if (pos+1 < dt.length &&
	dt[pos].isUNumber() &&
	dt[pos+1].is(ClockToken.MERIDIAN)) {
        parsePos.setIndex(pos+1);
	ParseMeridianAndSetHour(dt, parsePos, calendar, dt[pos].getInt());
        return true;
    }
    return false;
}

/**
 *      Parse a timezone string and sets the Calendar.
 *	A timezone string is valid, if it confirms to the following yacc rule:
 *	zone    : tZONE tDST
 *	        | tZONE
 *	        | tDAYZONE
 *	        ;
 *
 * Results:
 *      True, if a timezone was read (parsePos was incremented and calendar
 *	was set according to the read timezone); false otherwise.
 *
 * Side effects:
 *      None.
 *
 */

private static boolean
ParseZone (
    ClockToken[] dt,		// Input as scanned array of tokens
    ParsePosition parsePos,	// Current position in input
    Calendar calendar		// calendar object to set
)
{
    int pos = parsePos.getIndex();

    if (pos+1 < dt.length &&
	dt[pos].is(ClockToken.ZONE) &&
	dt[pos+1].is(ClockToken.DST)) {
        calendar.setTimeZone(dt[pos].getZone());
        parsePos.setIndex(pos+2);
        return true;
    }
    if (pos < dt.length &&
	dt[pos].is(ClockToken.ZONE)) {
        calendar.setTimeZone(dt[pos].getZone());
        parsePos.setIndex(pos+1);
        return true;
    }
    if (pos < dt.length &&
	dt[pos].is(ClockToken.DAYZONE)) {
        calendar.setTimeZone(dt[pos].getZone());
        parsePos.setIndex(pos+1);
        return true;
    }
    return false;
}

/**
 *-----------------------------------------------------------------------------
 *
 * ParseDay --
 *
 *      Parse a day string and sets the Calendar.
 *	A day string is valid, if it confirms to the following yacc rule:
 *	day     : tDAY
 *	        | tDAY ','
 *	        | tUNUMBER tDAY
 *	        ;
 *
 * Results:
 *      True, if a day was read (parsePos was incremented and calendar
 *	was set according to the read day); false otherwise.
 *
 * Side effects:
 *      None.
 *
 *-----------------------------------------------------------------------------
 */

private static boolean
ParseDay (
    ClockToken[] dt,		// Input as scanned array of tokens
    ParsePosition parsePos,	// Current position in input
    Calendar calendar		// calendar object to set
)
{
    int pos = parsePos.getIndex();

    if (pos+1 < dt.length &&
	dt[pos].is(ClockToken.DAY) &&
	dt[pos+1].is(',')) {
	calendar.set(Calendar.DAY_OF_MONTH, dt[pos].getInt());
        parsePos.setIndex(pos+2);
        return true;
    }
    if (pos+1 < dt.length &&
	dt[pos].isUNumber() &&
	dt[pos+1].is(ClockToken.DAY)) {
	calendar.set(Calendar.DAY_OF_MONTH, dt[pos+1].getInt());
        parsePos.setIndex(pos+2);
        return true;
    }
    if (pos < dt.length &&
	dt[pos].is(ClockToken.DAY)) {
	calendar.set(Calendar.DAY_OF_MONTH, dt[pos].getInt());
        parsePos.setIndex(pos+1);
        return true;
    }
    return false;
}

/**
 *-----------------------------------------------------------------------------
 *
 * ParseDate --
 *
 *      Parse a date string and sets the Calendar.
 *	A date string is valid, if it confirms to the following yacc rule:
 *	date	: tUNUMBER '/' tUNUMBER
 *		| tUNUMBER '/' tUNUMBER '/' tUNUMBER
 *		| tMONTH tUNUMBER
 *		| tMONTH tUNUMBER ',' tUNUMBER
 *		| tUNUMBER tMONTH
 *		| tEPOCH
 *		| tUNUMBER tMONTH tUNUMBER
 *		;
 *
 * Results:
 *      True, if a date was read (parsePos was incremented and calendar
 *	was set according to the read day); false otherwise.
 *
 * Side effects:
 *      None.
 *
 *-----------------------------------------------------------------------------
 */

private static boolean
ParseDate (
    ClockToken[] dt,		// Input as scanned array of tokens
    ParsePosition parsePos,	// Current position in input
    Calendar calendar		// calendar object to set
)
{
    int pos = parsePos.getIndex();

    if (pos+4 < dt.length &&
	dt[pos].isUNumber() &&
	dt[pos+1].is('/') &&
	dt[pos+2].isUNumber() &&
	dt[pos+3].is('/') &&
	dt[pos+4].isUNumber()) {
	calendar.set(Calendar.DAY_OF_MONTH, dt[pos+2].getInt());
	calendar.set(Calendar.MONTH, dt[pos].getInt()-1);
	calendar.set(Calendar.YEAR, dt[pos+4].getInt());
        parsePos.setIndex(pos+5);
        return true;
    }
    if (pos+3 < dt.length &&
	dt[pos].is(ClockToken.MONTH) &&
	dt[pos+1].isUNumber() &&
	dt[pos+2].is(',') &&
	dt[pos+3].isUNumber()) {
	calendar.set(Calendar.DAY_OF_MONTH, dt[pos+1].getInt());
	calendar.set(Calendar.MONTH, dt[pos].getInt());
	calendar.set(Calendar.YEAR, dt[pos+3].getInt());
        parsePos.setIndex(pos+4);
        return true;
    }
    if (pos+2 < dt.length &&
	dt[pos].isUNumber() &&
	dt[pos+1].is('/') &&
	dt[pos+2].isUNumber()) {
	calendar.set(Calendar.DAY_OF_MONTH, dt[pos+2].getInt());
	calendar.set(Calendar.MONTH, dt[pos].getInt()-1);
        parsePos.setIndex(pos+3);
        return true;
    }
    if (pos+2 < dt.length &&
	dt[pos].isUNumber() &&
	dt[pos+1].is(ClockToken.MONTH) &&
	dt[pos+2].isUNumber()) {
	calendar.set(Calendar.DAY_OF_MONTH, dt[pos].getInt());
	calendar.set(Calendar.MONTH, dt[pos+1].getInt());
	calendar.set(Calendar.YEAR, dt[pos+2].getInt());
        parsePos.setIndex(pos+3);
        return true;
    }
    if (pos+1 < dt.length &&
	dt[pos].is(ClockToken.MONTH) &&
	dt[pos+1].isUNumber()) {
	calendar.set(Calendar.DAY_OF_MONTH, dt[pos+1].getInt());
	calendar.set(Calendar.MONTH, dt[pos].getInt());
        parsePos.setIndex(pos+2);
        return true;
    }
    if (pos+1 < dt.length &&
	dt[pos].isUNumber() &&
	dt[pos+1].is(ClockToken.MONTH)) {
	calendar.set(Calendar.DAY_OF_MONTH, dt[pos].getInt());
	calendar.set(Calendar.MONTH, dt[pos+1].getInt());
        parsePos.setIndex(pos+2);
        return true;
    }
    if (pos < dt.length &&
	dt[pos].is(ClockToken.EPOCH)) {
	calendar.set(Calendar.DAY_OF_MONTH, 1);
	calendar.set(Calendar.MONTH, 0);
	calendar.set(Calendar.YEAR, EPOCH_YEAR);
        parsePos.setIndex(pos+1);
        return true;
    }
    return false;
}

/**
 *-----------------------------------------------------------------------------
 *
 * ParseNumber --
 *
 *      Parse a number and sets the Calendar.
 *	If argument mayBeYear is true, this number is conidered as year,
 *	otherwise it is date and time in the form HHMM.
 *
 * Results:
 *      True, if a number was read (parsePos was incremented and calendar
 *	was set according to the read day); false otherwise.
 *
 * Side effects:
 *      None.
 *
 *-----------------------------------------------------------------------------
 */

private static boolean
ParseNumber (
    ClockToken[] dt,		// Input as scanned array of tokens
    ParsePosition parsePos,	// Current position in input
    Calendar calendar,		// calendar object to set
    boolean mayBeYear		// number is considered to be year?
)
{
    int pos = parsePos.getIndex();

    if (pos < dt.length &&
	dt[pos].isUNumber()) {
        parsePos.setIndex(pos+1);
        if (mayBeYear) {
	    calendar.set(Calendar.YEAR, dt[pos].getInt());
	} else {
	  calendar.set(Calendar.HOUR_OF_DAY, dt[pos].getInt()/100);
	  calendar.set(Calendar.MINUTE, dt[pos].getInt()%100);
	  calendar.set(Calendar.SECOND, 0);
	}
        return true;
    }
    return false;
}

/**
 *-----------------------------------------------------------------------------
 *
 * ParseRel --
 *
 *      Parse a relative time specification and sets the time difference.
 *	A relative time specification is valid, if it confirms to the
 *	following yacc rule:
 *	rel	: relunit tAGO
 *		| relunit
 *		;
 *
 * Results:
 *      True, if a relative time specification was read (parsePos was
 *	incremented and the time difference was set according to the read
 *	relative time specification); false otherwise.
 *
 * Side effects:
 *      None.
 *
 *-----------------------------------------------------------------------------
 */

private static boolean
ParseRel (
    ClockToken[] dt,		// Input as scanned array of tokens
    ParsePosition parsePos,	// Current position in input
    ClockRelTimespan diff	// time difference to evaluate
)
{
    if (ParseRelUnit(dt, parsePos, diff)) {
        int pos = parsePos.getIndex();
        if (pos < dt.length &&
	    dt[pos].is(ClockToken.AGO)) {
	    diff.negate();
	    parsePos.setIndex(pos+1);
	}
        return true;
    }
    return false;
}

/**
 *-----------------------------------------------------------------------------
 *
 * ParseRelUnit --
 *
 *      Parse a relative time unit and sets the time difference.
 *	A relative time unit is valid, if it confirms to the
 *	following yacc rule:
 *	relunit : tUNUMBER tMINUTE_UNIT
 *		| tSNUMBER tMINUTE_UNIT
 *		| tMINUTE_UNIT
 *		| tSNUMBER tSEC_UNIT
 *		| tUNUMBER tSEC_UNIT
 *		| tSEC_UNIT
 *		| tSNUMBER tMONTH_UNIT
 *		| tUNUMBER tMONTH_UNIT
 *		| tMONTH_UNIT
 *		;
 *
 * Results:
 *      True, if a relative time unit was read (parsePos was incremented and
 *	the time difference was set according to the read relative time unit);
 *	false otherwise.
 *
 * Side effects:
 *      None.
 *
 *-----------------------------------------------------------------------------
 */

private static boolean
ParseRelUnit (
    ClockToken[] dt,		// Input as scanned array of tokens
    ParsePosition parsePos,	// Current position in input
    ClockRelTimespan diff	// time difference to evaluate
)
{
    int pos = parsePos.getIndex();

    if (pos+1 < dt.length &&
	(dt[pos].isUNumber() || dt[pos].isSNumber()) &&
	dt[pos+1].is(ClockToken.MINUTE_UNIT)) {
        diff.addSeconds(dt[pos].getInt()*dt[pos+1].getInt()*60);
        parsePos.setIndex(pos+2);
        return true;
    } else if (pos+1 < dt.length &&
	(dt[pos].isUNumber() || dt[pos].isSNumber()) &&
	dt[pos+1].is(ClockToken.SEC_UNIT)) {
        diff.addSeconds(dt[pos].getInt());
        parsePos.setIndex(pos+2);
        return true;
    } else if (pos+1 < dt.length &&
	(dt[pos].isUNumber() || dt[pos].isSNumber()) &&
	dt[pos+1].is(ClockToken.MONTH_UNIT)) {
        diff.addMonths(dt[pos].getInt()*dt[pos+1].getInt());
        parsePos.setIndex(pos+2);
        return true;
    } else if (pos < dt.length &&
	dt[pos].is(ClockToken.MINUTE_UNIT)) {
        diff.addSeconds(dt[pos].getInt()*60);
        parsePos.setIndex(pos+1);
        return true;
    } else if (pos < dt.length &&
	dt[pos].is(ClockToken.SEC_UNIT)) {
        diff.addSeconds(1);
        parsePos.setIndex(pos+1);
        return true;
    } else if (pos < dt.length &&
	dt[pos].is(ClockToken.MONTH_UNIT)) {
        diff.addMonths(dt[pos].getInt());
        parsePos.setIndex(pos+1);
        return true;
    }
    return false;
}

/**
 *-----------------------------------------------------------------------------
 *
 * ParseMeridianAndSetHour --
 *
 *      Parse a meridian and sets the hour field of the calendar.
 *	A meridian is valid, if it confirms to the following yacc rule:
 *	o_merid : // NULL
 *		| tMERIDIAN
 *		;
 *
 * Results:
 *      None; parsePos was incremented and the claendar was set according
 *	to the read meridian.
 *
 * Side effects:
 *      None.
 *
 *-----------------------------------------------------------------------------
 */

private static void
ParseMeridianAndSetHour(
    ClockToken[] dt,		// Input as scanned array of tokens
    ParsePosition parsePos,	// Current position in input
    Calendar calendar,		// calendar object to set
    int hour			// hour value (1-12 or 0-23) to set.
)
{
    int pos = parsePos.getIndex();
    int hourField;

    if (pos < dt.length &&
	dt[pos].is(ClockToken.MERIDIAN)) {
        calendar.set(Calendar.AM_PM, dt[pos].getInt());
        parsePos.setIndex(pos+1);
	hourField = Calendar.HOUR;
    } else {
	hourField = Calendar.HOUR_OF_DAY;
    }

    if (hourField == Calendar.HOUR && hour == 12) {
        hour = 0;
    }
    calendar.set(hourField, hour);
}

/**
 *-----------------------------------------------------------------------------
 *
 * GetTokens --
 *
 *      Lexical analysis of the input string.
 *
 * Results:
 *      An array of ClockToken, representing the input string.
 *
 * Side effects:
 *      None.
 *
 *-----------------------------------------------------------------------------
 */

private static ClockToken[]
GetTokens (
    String in		// String to parse
)
{
    ParsePosition parsePos = new ParsePosition(0);
    ClockToken dt;
    Vector tokenVector = new Vector(in.length());

    while ((dt = GetNextToken(in, parsePos)) != null) {
        tokenVector.addElement(dt);
    }

    ClockToken[] tokenArray = new ClockToken[tokenVector.size()];
    tokenVector.copyInto(tokenArray);

    if (debug) {
        for (int ix = 0; ix < tokenArray.length; ix++) {
	    if (ix != 0) {
	        System.err.print(",");
	    }
	    System.err.print(tokenArray[ix].toString());
	}
	System.err.println("");
    }

    return tokenArray;
}

/**
 *-----------------------------------------------------------------------------
 *
 * GetNextToken --
 *
 *      Lexical analysis of the next token of input string.
 *
 * Results:
 *      A ClockToken representing the next token of the input string,
 *	(parsePos was incremented accordingly), if one was found.
 *	null otherwise (e.g. at end of input).
 *
 * Side effects:
 *      None.
 *
 *-----------------------------------------------------------------------------
 */

private static ClockToken
GetNextToken (
    String in,			// String to parse
    ParsePosition parsePos	// Current position in input
)
{
    int pos = parsePos.getIndex();
    int sign;

    while (true) {
        while (pos < in.length() && Character.isSpaceChar(in.charAt(pos))) {
	    pos++;
	}
	if (pos >= in.length()) {
	    break;
	}

	char c = in.charAt(pos);
	if (Character.isDigit(c) || c == '-' || c == '+') {
	    if (c == '-' || c == '+') {
	        sign = c == '-' ? -1 : 1;
		if (!Character.isDigit(in.charAt(++pos))) {
		    // skip the '-' sign
		  continue;
		}
	    } else {
	        sign = 0;
	    }
	    int number = 0;
	    while (pos < in.length() 
		   && Character.isDigit(c = in.charAt(pos))) {
	        number = 10 * number + c - '0';
		pos++;
	    }
	    if (sign < 0) {
	        number = -number;
	    }
	    parsePos.setIndex(pos);
	    return new ClockToken(number, sign != 0);
	}
	if (Character.isLetter(c)) {
	    int beginPos = pos;
	    while (++pos < in.length()) {
	        c = in.charAt(pos);
		if (!Character.isLetter(c) && c != '.') {
		    break;
		}
	    }
	    parsePos.setIndex(pos);
	    return LookupWord(in.substring(beginPos, pos));
	}
	parsePos.setIndex(pos+1);
	return new ClockToken(in.charAt(pos));
    }
    parsePos.setIndex(pos+1);
    return null;
}

/**
 *-----------------------------------------------------------------------------
 *
 * LookupWord --
 *
 *      Construct a ClockToken for the given word.
 *
 * Results:
 *      A ClockToken representing the given word.
 *
 * Side effects:
 *      None.
 *
 *-----------------------------------------------------------------------------
 */

private static ClockToken LookupWord(
    String word			// word to lookup
)
{
    int ix;
    String[] names;
    String[][] zones;

    if (word.equalsIgnoreCase("am") || word.equalsIgnoreCase("a.m.")) {
        return new ClockToken(ClockToken.MERIDIAN, Calendar.AM);
    }
    if (word.equalsIgnoreCase("pm") || word.equalsIgnoreCase("p.m.")) {
        return new ClockToken(ClockToken.MERIDIAN, Calendar.PM);
    }

    // See if we have an abbreviation for a day or month.

    boolean abbrev;
    if (word.length() == 3) {
        abbrev = true;
    } else if (word.length() == 4 && word.charAt(3) == '.') {
        abbrev = true;
        word = word.substring(0, 3);
    } else {
        abbrev = false;
    }

    DateFormatSymbols symbols = new DateFormatSymbols(Locale.US);
    if (abbrev) {
        names = symbols.getShortMonths();
    } else {
        names = symbols.getMonths();
    }
    for (ix = 0; ix < names.length; ix++) {
        if (word.equalsIgnoreCase(names[ix])) {
	    return new ClockToken(ClockToken.MONTH, ix);
	}
    }
    if (abbrev) {
        names = symbols.getShortWeekdays();
    } else {
        names = symbols.getWeekdays();
    }
    for (ix = 0; ix < names.length; ix++) {
        if (word.equalsIgnoreCase(names[ix])) {
	    return new ClockToken(ClockToken.DAY, ix);
	}
    }

    // Drop out any periods and try the timezone table.

    StringBuffer withoutDotsBuf = new StringBuffer(word.length());
    for (ix = 0; ix < word.length(); ix++) {
        if (word.charAt(ix) != '.') {
	    withoutDotsBuf.append(word.charAt(ix));
	}
    }

    String withoutDots = new String(withoutDotsBuf);
    zones = symbols.getZoneStrings();

    for (ix = 0; ix < zones.length; ix++) {
        if (withoutDots.equalsIgnoreCase(zones[ix][2]) ||
	    withoutDots.equalsIgnoreCase(zones[ix][4])) {
 	    TimeZone zone = TimeZone.getTimeZone(zones[ix][0]);
	    return new ClockToken(ClockToken.ZONE, zone);
	}
    }
    if (withoutDots.equalsIgnoreCase("dst")) {
	return new ClockToken(ClockToken.DST, null);
    }

    // Strip off any plural and try the units.

    String singular;
    if (word.endsWith("s")) {
        singular = word.substring(0, word.length()-1);
    } else {
        singular = word;
    }
    if (singular.equalsIgnoreCase("year")) {
	return new ClockToken(ClockToken.MONTH_UNIT, 12);
    } else if (singular.equalsIgnoreCase("month")) {
	return new ClockToken(ClockToken.MONTH_UNIT, 1);
    } else if (singular.equalsIgnoreCase("fortnight")) {
	return new ClockToken(ClockToken.MINUTE_UNIT, 14*24*60);
    } else if (singular.equalsIgnoreCase("week")) {
	return new ClockToken(ClockToken.MINUTE_UNIT, 7*24*60);
    } else if (singular.equalsIgnoreCase("day")) {
	return new ClockToken(ClockToken.MINUTE_UNIT, 24*60);
    } else if (singular.equalsIgnoreCase("hour")) {
	return new ClockToken(ClockToken.MINUTE_UNIT, 60);
    } else if (singular.equalsIgnoreCase("minute")) {
	return new ClockToken(ClockToken.MINUTE_UNIT, 1);
    } else if (singular.equalsIgnoreCase("min")) {
	return new ClockToken(ClockToken.MINUTE_UNIT, 1);
    } else if (singular.equalsIgnoreCase("second")) {
	return new ClockToken(ClockToken.SEC_UNIT, 1);
    } else if (singular.equalsIgnoreCase("sec")) {
	return new ClockToken(ClockToken.SEC_UNIT, 1);
    }

    if (singular.equalsIgnoreCase("tomorrow")) {
	return new ClockToken(ClockToken.MINUTE_UNIT, 1*24*60);
    } else if (singular.equalsIgnoreCase("yesterday")) {
	return new ClockToken(ClockToken.MINUTE_UNIT, -1*24*60);
    } else if (singular.equalsIgnoreCase("today")) {
	return new ClockToken(ClockToken.MINUTE_UNIT, 0);
    } else if (singular.equalsIgnoreCase("now")) {
	return new ClockToken(ClockToken.MINUTE_UNIT, 0);
    } else if (singular.equalsIgnoreCase("last")) {
	return new ClockToken(-1, false);
    } else if (singular.equalsIgnoreCase("this")) {
	return new ClockToken(ClockToken.MINUTE_UNIT, 0);
    } else if (singular.equalsIgnoreCase("next")) {
	return new ClockToken(2, false);
    } else if (singular.equalsIgnoreCase("ago")) {
	return new ClockToken(ClockToken.AGO, 1);
    } else if (singular.equalsIgnoreCase("epoch")) {
	return new ClockToken(ClockToken.EPOCH, 0);
    }

    // Ignore military timezones.

    return new ClockToken(word);
}


/**
 *-----------------------------------------------------------------------------
 *
 * CLASS ClockToken --
 *
 *      An object of this class represents a lexical unit of the human
 *	readable date string. It can be one of the following variants:
 *
 *	- signed number,
 *	  = occurence can be asked by isSNumber(),
 *	  = value can be retrieved by means of getInt();
 *	- unsigned number,
 *	  = occurence can be asked by isUNumber(),
 *	  = value can be retrieved by means of getInt();
 *	- a single character (delimiters like ':' or '/'),
 *	  = occurence can be asked by is(), e.g. is('/');
 *	- a word (like "January" or "DST")
 *	  = occurence can be asked by is(), e.g. is(ClockToken.AGO);
 *	  = value can be retrieved by means of getInt() or getZone().
 *
 *-----------------------------------------------------------------------------
 */

static class ClockToken {
    final static int SNUMBER     = 1;
    final static int UNUMBER     = 2;
    final static int WORD        = 3;
    final static int CHAR        = 4;
    final static int MONTH       = 5;
    final static int DAY         = 6;
    final static int MONTH_UNIT  = 7;
    final static int MINUTE_UNIT = 8;
    final static int SEC_UNIT    = 9;
    final static int AGO         = 10;
    final static int EPOCH       = 11;
    final static int ZONE        = 12;
    final static int DAYZONE     = 13;
    final static int DST         = 14;
    final static int MERIDIAN    = 15;

    private int kind;
    private int number;
    private String word;
    private char c;
    private TimeZone zone;

    ClockToken(int number, boolean signed) {
        this.kind = signed ? SNUMBER : UNUMBER;
	this.number = number;
    }
    ClockToken(int kind, int number) {
        this.kind = kind;
	this.number = number;
    }
    ClockToken(int kind, TimeZone zone) {
        this.kind = kind;
	this.zone = zone;
    }
    ClockToken(String word) {
        this.kind = WORD;
	this.word = word;
    }
    ClockToken(char c) {
        this.kind = CHAR;
	this.c = c;
    }

    public boolean isSNumber() {
        return kind == SNUMBER;
    }
    public boolean isUNumber() {
        return kind == UNUMBER;
    }
    public boolean is(char c) {
        return this.kind == CHAR && this.c == c;
    }
    public boolean is(int kind) {
        return this.kind == kind;
    }    
    int getInt() {
        return (int) number;
    }
    TimeZone getZone() {
        return zone;
    }

    public String toString() {
        if (isSNumber()) {
	    return "S"+Integer.toString(getInt());
	} else if (isUNumber()) {
	    return "U"+Integer.toString(getInt());
	} else if (kind == WORD) {
	    return word;
	} else if (kind == CHAR) {
	    return new Character(c).toString();
	} else if (kind == ZONE || kind == DAYZONE) {
	    return zone.getID();
	} else {
	    return "("+kind+","+getInt()+")";
	}
    }
} // end ClockToken

/**
 *-----------------------------------------------------------------------------
 *
 * CLASS ClockRelTimespan --
 *
 *      An object of this class can be used to track the time difference during
 *	the analysis of a relative time specification.
 *
 *	It has two read only properties 'seconds' and 'months', which are set
 *	to 0 during initialization and which can be modified by means of the
 *	addSeconds(), addMonths() and negate() methods.
 *
 *-----------------------------------------------------------------------------
 */

static class ClockRelTimespan {
    ClockRelTimespan() {
        seconds = 0;
	months = 0;
    }
    void addSeconds(int s) {
        seconds += s;
    }
    void addMonths(int m) {
        months += m;
    }
    void negate() {
        seconds = -seconds;
        months = -months;
    }
    int getSeconds() {
        return seconds;
    }
    int getMonths() {
        return months;
    }
    private int seconds;
    private int months;
}

public static void
main(String[] args) throws Exception
    {
    debug=true;
    Date result = GetDate(args[0], null, null);
    System.out.println("" + result);
    }
} // end ClockCmd
