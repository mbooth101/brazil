/*
 * LexHTML.java
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
 * The Initial Developer of the Original Code is: cstevens.
 * Portions created by cstevens are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): cstevens, suhler.
 *
 * Version:  2.2
 * Created by cstevens on 99/09/29
 * Last modified by suhler on 04/11/30 15:19:45
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:45 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:37:00 (suhler)
 *   version change
 *
 * 1.11 01/03/16-14:40:35 (cstevens)
 *   Vector.indexOf() rather than iterating over the elements of a Vector manually.
 *
 * 1.10 01/03/13-15:39:22 (cstevens)
 *   re: Tags with special body syntax (like <script> or <style>)
 *   -- Given "<script></script>" (with nothing between the "<script>"
 *   and the "</script>"), parser was not seeing "</script>" and
 *   it was grabbing the entire rest of the document as the body of
 *   the script tag.
 *   -- Singleton "<script src=foo />" will now also work.
 *
 * 1.9 00/05/31-13:52:39 (suhler)
 *   docs
 *
 * 1.8 00/05/10-10:49:27 (suhler)
 *   removed SetClosingTags()
 *   getClosingTags() is now a vector, making it easier to add new tags
 *
 * 1.7 99/10/19-19:00:40 (cstevens)
 *   access the special closing tags.
 *
 * 1.6 99/10/14-14:57:42 (cstevens)
 *   resolve wilcard imports.
 *
 * 1.5 99/10/14-12:59:19 (cstevens)
 *   Documentation.
 *   Use method to set the special tags like <script> and <style>, instead of
 *   a global property.
 *
 * 1.4 99/10/07-13:04:04 (cstevens)
 *   LexHTML automatically lower-cases reported tag name, for backwards
 *   compatibility with HtmlMunge, so user can call String.equals() instead of
 *   String.equalsIgnoreCase()
 *
 * 1.3 99/10/04-16:12:35 (cstevens)
 *   Merged changes between child workspace "/home/cstevens/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.1.1.1 99/10/04-16:03:38 (cstevens)
 *   Documentation for LexML and StringMap.
 *
 * 1.2 99/10/01-11:38:10 (suhler)
 *   no *'s
 *
 * 1.2 99/09/29-16:12:37 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 util/LexHTML.java
 *
 * 1.1 99/09/29-16:12:36 (cstevens)
 *   date and time created 99/09/29 16:12:36 by cstevens
 *
 */

package sunlabs.brazil.util;
import java.util.Vector;

/**
 * This class breaks up HTML into tokens.
 * <p>
 * <a name=intro></a>
 * This class differs slightly from LexML as follows: after certain tags,
 * like the <code>&lt;script&gt;</code> tag, the body that follows is
 * uninterpreted data and ends only at the next, in this case,
 * <code>&lt;/script&gt;</code> tag, not at the just the next
 * "&lt;" or "&gt;" character.  This is one way that HTML is not fully
 * compliant with XML.
 * <p>
 * The default set of tags that have this special processing is
 * <code>&lt;script&gt;</code>, <code>&lt;style&gt;</code>, and
 * <code>&lt;xmp&gt;</code>.  The user can change this by retrieving
 * the Vector of special tags via
 * <code>getClosingTags</code>, and modifying it as needed.
 *
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version		2.2
 */
public class LexHTML
    extends LexML
{
    static final String[] defaultClosingTags = {
	"script", "style", "xmp", "server"
    };
    Vector closingTags;

    String bodyTag;

    /**
     * Creates a new HTML parser, which can be used to iterate over the
     * tokens in the given string.
     *
     * @param	str
     *		The HTML to parse.
     */
    public
    LexHTML(String str)
    {
	super(str);
	closingTags = new Vector();
	for (int i = 0; i < defaultClosingTags.length; i++) {
	    closingTags.addElement(defaultClosingTags[i]);
	}
    }

    /**
     * Get the set of HTML tags that have the special body-processing
     * behavior mentioned <a href=#intro>above</a>.  The Vector
     * is returned; the caller may modify it after calling this method, 
     * which will affect this parser's settings.
     *
     * @param	tags
     *		The array of case-insensitive tag names that are only
     *		closed by seeing their "slashed" version.
     */
    public Vector
    getClosingTags()
    {
	return closingTags;
    }

    /**
     * Advances to the next token, correctly handling HTML tags that have
     * the special body-processing behavior mentioned <a href=#intro>above</a>.
     * The user can then call the other methods in this class to get
     * information about the new current token.
     * <p>
     * This method returns the uninterpreted data making up the body of a
     * special HTML tag as a token of type <code>LexML.STRING</code>, even
     * if the body was actually a comment or another tag.
     *
     * @return	<code>true</code> if a token was found, <code>false</code>
     *		if there were no more tokens left.
     */
    public boolean
    nextToken()
    {
	if (bodyTag != null) {
	    /*
	     * bodyTag was set when we saw one of the special tags that
	     * are only closed when we see "</" + tag + ">".  Look for that
	     * closing tag and return everything between the last tag and
	     * the start of the closing tag as the current token.
	     */

	    String end = "</" + bodyTag + ">";
	    int endLength = end.length();

	    String rest = rest();

	    int restLength = rest.length() - endLength;

	    bodyTag = null;
	    replace(rest);

	    int i;
	    for (i = 0; i < restLength; i++) {
		if (rest.regionMatches(true, i, end, 0, endLength)) {
		    type = STRING;
		    tokenEnd = i;
		    return true;
		}
	    }
	    return false;
	}

	if (super.nextToken() == false) {
	    return false;
	}
	if (getType() == TAG) {
	    String tag = getTag();
	    if ((closingTags.indexOf(tag) >= 0) && !isSingleton()) {
		bodyTag = tag;
	    }
	}
	return true;
    }

    /**
     * Gets the tag name at the begining of the current tag.  In HTML,
     * tag names are defined as case-insensitive, so the name returned
     * is converted to lower case for the convenience of the user.
     *
     * @return	The lower-cased tag name, or <code>null</code> if the
     *		current token does not have a tag name.
     *
     * @see	LexML#getTag
     */
    public String
    getTag()
    {
	String tag = super.getTag();
	if (tag == null) {
	    return null;
	}
	return tag.toLowerCase();
    }

    /**
     * Changes the string that this LexHTML is parsing.
     *
     * @param	str
     *		The string that this LexHTML should now parse.  
     */
    public void
    replace(String str)
    {
	this.bodyTag = null;
	super.replace(str);
    }

    

}
