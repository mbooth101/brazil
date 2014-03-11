/*
 * LexML.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2006 Sun Microsystems, Inc.
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
 * The Initial Developer of the Original Code is: cstevens.
 * Portions created by cstevens are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): cstevens, suhler.
 *
 * Version:  2.7
 * Created by cstevens on 99/09/29
 * Last modified by suhler on 06/12/18 14:47:23
 *
 * Version Histories:
 *
 * 2.7 06/12/18-14:47:23 (suhler)
 *   bug fix: isSingleton() wasn't reporting singletons in some cases
 *
 * 2.6 04/12/30-12:40:00 (suhler)
 *   javadoc fixes.
 *
 * 2.5 04/11/30-15:19:45 (suhler)
 *   fixed sccs version string
 *
 * 2.4 04/11/18-12:23:20 (suhler)
 *   init bug
 *
 * 2.3 04/04/28-15:55:16 (suhler)
 *   added methods for better error diagnosis
 *
 * 2.2 03/08/01-16:17:27 (suhler)
 *   fixes for javadoc
 *
 * 2.1 02/10/01-16:37:00 (suhler)
 *   version change
 *
 * 1.13 02/07/24-10:49:53 (suhler)
 *   doc updates
 *
 * 1.12 02/02/04-14:34:21 (suhler)
 *   remove "new" close tag finding bahavior: allow sub-classes to implement it instead
 *   .
 *
 * 1.11 02/01/29-10:08:46 (suhler)
 *   Changed (new) unescaped ">" behavior back to old as the default.  Use
 *   allowGt() to turn on or off.
 *
 * 1.10 02/01/23-11:45:09 (suhler)
 *   - Changed the default behavior of LexML to allow unescaped
 *   >'s inside of quoted strings.  This could be a major incompatibility,
 *   so it needs thourough testing.  The "old" behavior can be
 *   re-established on a global or per-instance basis
 *
 * 1.9 01/08/20-16:38:30 (suhler)
 *   doc lint
 *
 * 1.8 01/07/30-17:32:58 (suhler)
 *   added internal "done" flag that allows references to getXXX() after
 *   nextToken returns false;
 *
 * 1.7 01/03/08-16:06:24 (cstevens)
 *   Handle singleton HTML tags like <br/> or <a name=foo/>.  Before, these forms
 *   were interpreted incorrectly as the "br/" tag or the "a" tag with the
 *   attribute "name" and the value "foo/".
 *
 * 1.6 01/01/16-14:19:08 (suhler)
 *   bug! (running off the end of comments)
 *
 * 1.5 00/05/31-13:52:45 (suhler)
 *   docs
 *
 * 1.4 99/10/21-18:24:15 (cstevens)
 *   Added ability to change a tag into a comment.  Used by BSL and Tcl templates,
 *   to keep track of where the substitution occurred when examining the resultant
 *   HTML document.
 *
 * 1.3 99/10/14-12:58:22 (cstevens)
 *   Documentation
 *
 * 1.2 99/10/04-16:03:32 (cstevens)
 *   Documentation for LexML and StringMap.
 *
 * 1.2 99/09/29-16:12:36 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 util/LexML.java
 *
 * 1.1 99/09/29-16:12:35 (cstevens)
 *   date and time created 99/09/29 16:12:35 by cstevens
 *
 */

package sunlabs.brazil.util;

/**
 * This class breaks angle-bracket-separated markup languages like SGML, XML,
 * and HTML into tokens.  It understands three types of tokens: <dl>
 * <dt> tags
 *	<dd> Formally known as "entities", tags are delimited by "&lt;" and
 *	     "&gt;".  The first word in the tag is the tag name and the
 *	     rest of the tag consists of the attributes, a set of
 *	     "name=value" or "name" data.  Spaces in tags are not significant
 *	     except for quoted values in the attributes.
 *
 * <dt> string
 *	<dd> Plain strings that are not in angle-brackets.  Spaces are
 *	     significant and preserved.
 *
 * <dt> comments
 *	<dd> Delimited by "&lt;!--" and "--&gt;".  All text between the
 *	     delimiters is part of the comment.  However, by convention,
 *	     some comments actually contain data and so the methods that
 *	     extract the fields from tags can be used to attempt to extract
 *	     the fields from comments, too.  Spaces are significant and
 *	     preserved in a comment, unless the comment is treated as a
 *	     tag, in which the tag rules apply.
 * </dl>
 * <p>
 * This class is intended to parse markup languages, not to validate them.
 * "Malformed" data is interpreted as graciously as possible, in order to
 * extract as much information as possible.  For instance: spaces are
 * allowed between the "&lt;" and the tag name, values in tags do not need
 * to be quoted, and unbalanced quotes are accepted.
 * <p>
 * <a name=badquote></a>
 * One type of "malformed" data specifically not handled is a quoted
 * "&gt;" character occurring within the body of a tag.  Even if it is
 * quoted, a "&gt;" in the attributes of a tag will be interpreted as the
 * end of the tag.  For example, the single tag <code>&lt;img src='foo.jpg'
 * alt='xyz&nbsp;&gt;&nbsp;abc'&gt;</code> will be erroneously broken by
 * this parser into two tokens: <ul>
 * <li> the tag <code>&lt;img src='foo.jpg' alt='xyz&nbsp;&gt;</code>
 * <li> the string "abc'&gt;" (and possibly whatever text follows after).
 * </ul>
 * Unfortunately, this type of "malformed" data is known to occur regularly.
 * <p>
 * This class also may not properly parse all well-formed XML tags, such
 * as tags with extended paired delimiters <code>&lt;&amp;</code> and
 * <code>&amp;&gt;</code>, <code>&lt;?</code> and <code>?&gt;</code>, or
 * <code>&lt;![CDATA[</code> and <code>]]&gt;</code>.
 * Additionally, XML tags that have embedded comments containing the
 * "&gt;" character will not be parsed correctly (for example:
 * <code>&lt;!DOCTYPE foo SYSTEM -- a &gt; b -- foo.dtd&gt;</code>),
 * since the "&gt;" in the comment will be interpreted as
 * the end of declaration tag, for the same reason mentioned
 * <a href=#badquote>above</a>.
 * <br>
 * Note:  this behavior may be changed on a per-application basis by
 * overriding the <code>findClose</code> method in a subclass.
 *
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version		2.7
 */

public class LexML
{
    /**
     * The value returned by <code>getType</code> for comment tokens
     */
    public static final int COMMENT	= 0;

    /**
     * The value returned by <code>getType</code> for tag tokens
     */
    public static final int TAG		= 1;

    /**
     * The value returned by <code>getType</code> for string tokens
     */
    public static final int STRING	= 2;

    private static final String SPACE	= " \t\r\n";
    private static final String SPACE_EQUAL = SPACE + "=";

    int type;
    boolean singleton;   // Tag of form <a/>
    boolean done;	// set when we run out of tokens

    protected String str;		// The string we are scanning
    int strEnd;
    int tokenStart;
    int tokenEnd;
    int tagStart;
    int tagEnd;
    int argsStart;
    int argsEnd;

    /**
     * Create a new ML parser, which can be used to iterate over the
     * tokens in the given string.
     *
     * @param	str
     *		The ML to parse.
     */
    public
    LexML(String str) {
	replace(str);
    }

    /**
     * Advances to the next token.  The user can then call the other methods
     * in this class to get information about the new current token.
     *
     * @return	<code>true</code> if a token was found, <code>false</code>
     *		if there were no more tokens left.
     */
    public boolean
    nextToken()
    {
	if (tokenEnd >= strEnd) {
	    done=true;
	    singleton = false;
	    type=STRING;
	    return false;
	}

	tokenStart = tokenEnd;
	if (str.startsWith("<!--", tokenStart)) {
	    try {
		tokenEnd = str.indexOf("-->", tokenStart + 4);
	    } catch (StringIndexOutOfBoundsException e) {
		tokenEnd = -1;
	    }
	    if (tokenEnd < 0) {
		str += "-->";
		tokenEnd = strEnd;
		strEnd += 3;
	    }
	    tokenEnd += 3;
	    type = COMMENT;
	} else if (str.charAt(tokenStart) == '<') {
	    tokenEnd = findClose(tokenStart);
	    if (tokenEnd < 0) {
		str += ">";
		strEnd++;
		tokenEnd = strEnd;
		done=true;
	    }
	    tokenEnd++;
	    type = TAG;
	} else {
	    tokenEnd = str.indexOf('<', tokenStart);
	    if (tokenEnd < 0) {
		tokenEnd = strEnd;
	    }
	    type = STRING;
	}
	return true;
    }

    /**
     * Find the closing tag "&gt;".<br>
     * This may be overriden by sub-classes to allow more sophisticated
     * behavior.
     * @param start	The starting index in <code>str</code> to look for
     *			the matching &gt;
     * @return		The index of <code>str<code> that contains the
     *			matching &gt;
     */
    protected int
    findClose(int start) {
	return str.indexOf('>', start);
    }

    /**
     * Gets the type of the current token.
     *
     * @return	The type.
     *
     * @see	#COMMENT
     * @see	#TAG
     * @see	#STRING
     */
    public int
    getType() {
	return type;
    }

    /**
     * A tag is a "singleton" if the closing "&gt;" is preceded by
     * a slash (/). (e.g. <code>&lt;br/&gt;</code>
     */

    public boolean
    isSingleton() {
	if (type == TAG) {
	    split();	// computes singleton as a side effect
	}
	return singleton;
    }

    /**
     * Gets the string making up the whole current token, including the
     * brackets or comment delimiters, if appropriate.
     *
     * @return	The current token.
     */
    public String
    getToken() {
	return done ? null : str.substring(tokenStart, tokenEnd);
    }

    /**
     * Gets the string making up the current token, not including the angle
     * brackets or comment delimiters, if appropriate.
     *
     * @return	The body of the token.
     */
    public String
    getBody() {
        if (done) {
            return null;
	} else if (type == TAG) {
	    return str.substring(tokenStart + 1, tokenEnd - 1);
	} else if (type == COMMENT) {
	    return str.substring(tokenStart + 4, tokenEnd - 3);
	} else {
	    return str.substring(tokenStart, tokenEnd);
	}
    }

    /**
     * Return the string we are currently processing
     */

    public String 
    getString() {
	return str;
    }

    /**
     * Return the current processing location.
     * @return		The character index of the current tag.
     */

    public int 
    getLocation() {
	return tokenStart;
    }

    private void
    split() {
	if (tagStart <= tokenStart) {
	    int off = tokenStart + 1;
	    int end = (type == TAG) ? tokenEnd - 1 : tokenEnd - 3;

	    tagStart = skip(SPACE, str, off, end);
	    tagEnd = next(SPACE, str, tagStart, end);
	    argsStart = skip(SPACE, str, tagEnd, end);
	    argsEnd = end;
	    singleton = false;

	    if (str.charAt(argsEnd - 1) == '/') {
		singleton = true;
		argsEnd--;
		if (argsStart > argsEnd) {
		    argsStart = argsEnd;
		}
		if (tagEnd > argsEnd) {
		    tagEnd = argsEnd;
		}
	    }
	}
    }

    /**
     * Gets the tag name at the beginning of the current tag.  In other
     * words, the tag name for <code>&lt;table&nbsp;border=3&gt;</code> is
     * "table".  Any surrounding space characters are removed, but the
     * case of the tag is preserved.
     * <p>
     * For comments, the "tag" is the first word in the comment.  This can
     * be used to help parse comments that are structured similar to regular
     * tags, such as server-side include comments like
     * <code>&lt;!--#include&nbsp;virtual="file.inc"&gt;</code>.  The tag in
     * this case would be "!--#include".
     *
     * @return	The tag name, or <code>null</code> if the current token
     *		was a string.
     *		
     */
    public String
    getTag() {
	if (type == STRING) {
	    return null;
	}
	split();
	return str.substring(tagStart, tagEnd);
    }

    /**
     * Gets the name/value pairs in the body of the current tag as a
     * string.
     *
     * @return	The name/value pairs, or <code>null</code> if
     *		the current token was a string.
     */
    public String
    getArgs() {
	if (type == STRING) {
	    return null;
	}
	split();
	return str.substring(argsStart, argsEnd);
    }
	

    /**
     * Gets the name/value pairs in the body of the current tag as a
     * table.
     * <p>
     * Any quote marks in the body, either single or double quotes, are
     * left on the values, so that the values can be easily re-emitted
     * and still form a valid body.
     * <p>
     * For names that have no associated value in the tag, the value is
     * stored as the empty string "".  Therefore, the two tags
     * <code>&lt;table&nbsp;border&gt;</code> and
     * <code>&lt;table&nbsp;border=""&gt;</code> cannot be distinguished
     * based on the result of calling <code>getAttributes</code>.
     *
     * @return	The table of name/value pairs, or <code>null</code> if
     * 		the current token was a string.
     */
    public StringMap
    getAttributes() {
	if (type == STRING) {
	    return null;
	}

	StringMap map = new StringMap();

	split();
	int off = argsStart;
	int end = argsEnd;
	String token = str;

	while (off < end) {
	    int nameStart = off;
	    int nameEnd = next(SPACE_EQUAL, token, off + 1, end);

	    String name = token.substring(nameStart, nameEnd);

	    off = skip(SPACE, token, nameEnd, end);
	    if ((off < end) && (token.charAt(off) == '=')) {
		off = skip(SPACE, token, off + 1, end);
		if (off < end) {
		    char ch = token.charAt(off);
		    int valueStart = off;
		    int valueEnd;

		    if ((ch == '"') || (ch == '\'')) {
			off++;
			if (off < end) {
			    off = token.indexOf(ch, off);
			    if (off < 0) {
				off = end;
			    }
			}
			off++;
			valueEnd = off;
		    } else {
			off = next(SPACE, token, off, end);
			valueEnd = off;
		    }
		    map.add(name, token.substring(valueStart, valueEnd));
		    off = skip(SPACE, token, off, end);
		    continue;
		}
	    }
	    map.add(name, "");
	}
	return map;
    }

    /**
     * Gets the rest of the string that has not yet been parsed.
     * <p>
     * Example use: to help the parser in circumstances such as the HTML
     * "&lt;script&gt;" tag where the script body doesn't the obey the rules
     * because it might contain lone "&lt;" or "&gt;" characters, which this
     * parser would interpret as the start or end of funny-looking tags.
     *
     * @return	The unparsed remainder of the string.
     *
     * @see 	#replace
     */
    public String
    rest() {
	return done ? null : str.substring(tokenEnd);
    }

    /**
     * Changes the string that this LexML is parsing.
     * <p>
     * Example use: the caller decided to parse part of the body,
     * and now wants this LexML to pick up and parse the rest of it.
     *
     * @param	str
     *		The string that this LexML should now parse.  Whatever
     *		string this LexML was parsing is forgotten, and it now
     *		starts parsing at the beginning of the new string.
     *
     * @see	#rest
     */
    public void
    replace(String str)
    {
	this.type=STRING;
	this.str = str;
	this.tokenStart = 0;
	this.tokenEnd = 0;

	this.tagStart = 0;
	singleton = false;
	if (str == null) {
	   done = true;
	} else {
	    this.strEnd = str.length();
	    done=(tokenEnd >= strEnd);
	}
    }

    private int
    skip(String pattern, String str, int i, int end)
    {
	for ( ; i < end; i++) {
	    if (pattern.indexOf(str.charAt(i)) < 0) {
		break;
	    }
	}
	return i;
    }

    private int
    next(String pattern, String str, int i, int end)
    {
	for ( ; i < end; i++) {
	    if (pattern.indexOf(str.charAt(i)) >= 0) {
		break;
	    }
	}
	return i;
    }
}
