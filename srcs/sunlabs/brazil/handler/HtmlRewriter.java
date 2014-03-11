/*
 * HtmlRewriter.java
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
 * Contributor(s): cstevens, guym, suhler.
 *
 * Version:  2.6
 * Created by cstevens on 99/09/29
 * Last modified by suhler on 06/04/25 14:28:22
 *
 * Version Histories:
 *
 * 2.6 06/04/25-14:28:22 (suhler)
 *   add "getMap()" to return a copy of the tag attributes
 *
 * 2.5 05/06/16-08:00:02 (suhler)
 *   Make "deQuote" public, and move to Format.
 *
 * 2.4 04/04/05-14:49:18 (suhler)
 *   add token counters, change quoting semantics
 *
 * 2.3 03/07/17-10:40:26 (suhler)
 *   change quote() again.  It now always adds "'s if plausible.  Although this
 *   perturbs the original document more than necessary, it's always safe, and
 *   gets around problems where ${} expansions might cause "'s to be required
 *
 * 2.2 03/07/07-15:54:14 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.13.1.1 03/07/07-15:52:34 (suhler)
 *   modify quoting convensions a little
 *
 * 2.1 02/10/01-16:36:25 (suhler)
 *   version change
 *
 * 1.13 02/07/24-10:45:30 (suhler)
 *   doc updates
 *
 * 1.12 02/05/01-11:21:08 (suhler)
 *   fix sccs version info
 *
 * 1.11 01/08/21-13:00:22 (guym)
 *   Fixed a bug where string map was null when get was being called
 *
 * 1.10 01/03/08-16:03:45 (cstevens)
 *   Handle singleton HTML tags like <br/> or <a name=foo/>.  Before, these forms
 *   were interpreted incorrectly as the "br/" tag or the "a" tag with the
 *   attribute "name" and the value "foo/".
 *
 * 1.9 00/12/27-12:12:09 (suhler)
 *   accumulate now returns its previous setting
 *
 * 1.8 00/10/31-10:18:10 (suhler)
 *   doc fixes
 *
 * 1.7 99/11/16-13:18:00 (cstevens)
 *   Rename "getValue" to "get" to make it more compatible with Dictionary
 *   naming scheme.
 *
 * 1.6 99/10/21-18:06:58 (cstevens)
 *   HtmlRewriter didn't re-emit parsed comments as comments.
 *
 * 1.5 99/10/19-18:35:52 (cstevens)
 *
 * 1.4 99/10/14-14:57:20 (cstevens)
 *   resolve wilcard imports.
 *
 * 1.3 99/10/07-12:59:08 (cstevens)
 *   Javadocs for HtmlRewriter.
 *
 * 1.2 99/09/30-14:10:05 (cstevens)
 *   Improperly quoting and dequoting HTML tag attributes led to forms/templates
 *   not working.
 *
 * 1.2 99/09/29-16:13:26 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/HtmlRewriter.java
 *
 * 1.1 99/09/29-16:13:25 (cstevens)
 *   date and time created 99/09/29 16:13:25 by cstevens
 *
 */

package sunlabs.brazil.handler;

import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.LexHTML;
import sunlabs.brazil.util.StringMap;

import java.util.Enumeration;

/**
 * This class helps with parsing and rewriting an HTML document.  The
 * source document is not changed; a new HTML document is built.
 * <p>
 * The user can sequentially examine and rewrite each token in the source
 * HTML document.  As each token in the document is seen, the user has
 * two choices: <ul>
 * <li> modify the current token.
 * <li> don't modify the current token.
 * </ul>
 * If the user modifies (or replaces, deletes, etc.) the current token,
 * then the resultant HTML document will contain that modification.  On
 * the other hand, if the user doesn't do anything with the current token,
 * it will appear, unchanged, in the resultant HTML document.
 * <p>
 * Parsing is implemented lazily, meaning, for example, that unless the
 * user actually asks for attributes of an HTML tag, this parser
 * does not have to spend the time breaking up the attributes.
 * <p>
 * This class is used by HTML filters to maintain the state of the
 * document and allow the filters to perform arbitrary rewriting.
 *
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version	@(#)HtmlRewriter.java	2.6
 */
public class HtmlRewriter
{
    /**
     * The parser for the source HTML document.
     */
    public LexHTML lex;

    /**
     * Storage holding the resultant HTML document.
     */
    public StringBuffer sb;

    /**
     * <code>true</code> if the last token was pushed back and should be
     * presented again next time.  Made <code>false</code> once the
     * pushedback token is presented.
     */
    boolean pushback;

    /**
     * <code>true</code> if <code>nextToken</code> should automatically
     * append unmodified tokens to the result.
     */
    boolean accumulate;

    /**
     * <code>true</code> if the user has already explicitly appended
     * something, so <code>nextToken</code> shouldn't append the
     * unmodified token.  
     */
    boolean appendToken;

    /**
     * <code>true</code> if the user has modified the tag name or
     * attributes of the current tag, so when this tag is appended, we
     * need to write out its parts rather than just emitting the raw token.
     */
    boolean tokenModified;

    int type;
    boolean singleton;
    String token;
    String tag;
    StringMap map;
    int tokenCount;	// count tokens
    int tagCount;	// count just tags

    /**
     * Creates a new <code>HtmlRewriter</code> from the given HTML parser.
     *
     * @param	lex
     *		The HTML parser.
     */
    public
    HtmlRewriter(LexHTML lex)
    {
	this.lex = lex;

	sb = new StringBuffer();
	accumulate = true;
	tokenCount=0;
	tagCount=0;
    }

    /**
     * Creates a new <code>HtmlRewriter</code> that will operate on the given
     * string.
     *
     * @param	str
     *		The HTML document.
     */
    public
    HtmlRewriter(String str)
    {
	this(new LexHTML(str));
    }

    /**
     * Returns the "new" rewritten HTML document.  This is normally called
     * once all of the tokens have been processed, and the user wants to
     * send on this rewritten document.
     * <p>
     * At any time, this method can be called to return the current state
     * of the HTML document.  The return value is the result of
     * processing the source document up to this point in time; the
     * unprocessed remainder of the source document is not considered.
     * <p>
     * Due to the implementation, calling this method may be expensive.
     * Specifically, calling this method a second (or further) time for
     * a given <code>HtmlRewriter</code> may involve copying temporary
     * strings around.  The pessimal case would be to call this method
     * every time a new token is appended.
     *
     * @return	The rewritten HTML document, up to this point in time.
     */
    public String
    toString()
    {
	return sb.toString();
    }

    /**
     * Advances to the next token in the source HTML document.
     * <p>
     * The other purpose of this function is to "do the right thing", which
     * is to append the token we just processed to the resultant HTML
     * document, unless the user has already appended something else.  
     * <p>
     * A sample program follows.  This program changes all
     * <code>&lt;img&gt;</code> tags to <code>&lt;form&gt;</code> tags,
     * deletes all <code>&lt;table&gt;</code> tags, capitalizes
     * and bolds each string token, and passes all other tokens through
     * unchanged, to illustrate how <code>nextToken</code> interacts with
     * some of the other methods in this class.
     * <pre>
     * HtmlRewriter hr = new HtmlRewriter(str);
     * while (hr.nextToken()) {
     *     switch (hr.getType()) {
     *     case LexHTML.TAG: 
     *         if (hr.getTag().equals("img")) {
     *             // Change the tag name w/o affecting the attributes.
     *             
     *             hr.setTag("form");
     *         } else if (hr.getTag().equals("table")) {
     *             // Eliminate the entire "table" token.
     *             
     *             hr.killToken();
     *         } 
     *         break;
     *             
     *     case LexHTML.STRING:
     *         // Append a new sequence in place of the existing token.
     *
     *         hr.append("&lt;b>" + hr.getToken().toUpperCase() + "&lt;/b>");
     *         break;
     *     }
     *     // Any tokens we didn't modify get copied through unchanged.
     * }
     * </pre>
     *
     * @return	<code>true</code> if there are tokens left to process,
     *		<code>false</code> otherwise.
     */
    public boolean
    nextToken()
    {
	tokenCount++;
	if (pushback) {
	    pushback = false;
	    return true;
	}

	if (appendToken && accumulate) {
	    appendToken();
	}

	token = null;
	tag = null;
	map = null;

	appendToken = true;
	tokenModified = false;

	if (lex.nextToken()) {
	    type = lex.getType();
	    if (type == LexHTML.TAG) {
		tagCount++;
	    }
	    singleton = lex.isSingleton();
	    return true;
	}
	return false;
    }

    /**
     * A convenence method built on top of <code>nextToken</code>.
     * Advances to the next HTML tag.  All intervening strings and comments
     * between the last tag and the new current tag are copied through
     * unchanged.  This method can be used when the caller wants to process
     * only HTML tags, without having to manually check the type of each
     * token to see if it is actually a tag.
     *
     * @return	<code>true</code> if there are tokens left to process,
     *		<code>false</code> otherwise.
     */
    public boolean
    nextTag()
    {
	while (nextToken()) {
	    if (getType() == LexHTML.TAG) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Gets the type of the current token.
     *
     * @return	The type.
     *
     * @see	LexHTML#getType
     */
    public int
    getType()
    {
	return type;
    }

    /**
     * Sets the type of the current token.
     */
    public void
    setType(int type)
    {
	this.type = type;
	tokenModified = true;
    }

    /**
     * See if the current tag a singleton.  A Singleton tag ends in "/", as
     * in <code>&lt;<br /&gt;></code>.
     */

    public boolean
    isSingleton()
    {
	return singleton;
    }

    /**
     * Make the current tag a singleton.  A Singleton tag ends in "/", as
     * in <code>&lt;<br /&gt;></code>.
     */

    public void
    setSingleton(boolean singleton)
    {
	this.singleton = singleton;
	tokenModified = true;
    }

    /**
     * Gets the raw string making up the entire current token, including
     * the angle brackets or comment delimiters, if applicable.
     *
     * @return	The current token.
     *
     * @see	LexHTML#getToken
     */
    public String
    getToken()
    {
	if (token == null) {
	    token = lex.getToken();
	}
	return token;
    }

    /**
     * Gets the current tag's name.  The name returned is converted to
     * lower case.
     *
     * @return	The lower-cased tag name, or <code>null</code> if the
     *		current token does not have a tag name
     *
     * @see	LexHTML#getTag
     */
    public String
    getTag()
    {
	if (tag == null) {
	    tag = lex.getTag();
	}
	return tag;
    }

    /**
     * Changes the current tag's name.  The tag's attributes are not changed.
     *
     * @param	tag
     *		New tag name
     */
    public void
    setTag(String tag)
    {
	this.tag = tag;
	tokenModified = true;
    }

    /**
     * Gets the body of the current token as a string.  
     *
     * @return	The body.
     *
     * @see	LexHTML#getBody
     */
    public String
    getBody()
    {
	return lex.getBody();
    }

    /**
     * Gets the arguments of the current token as a string.  
     *
     * @return	The body.
     *
     * @see	LexHTML#getArgs
     */
    public String
    getArgs()
    {
	return lex.getArgs();
    }

    /**
     * Returns the value that the specified case-insensitive key maps
     * to in the attributes for the current tag.  For keys that were
     * present in the tag's attributes without a value, the value returned
     * is the empty string.  In other words, for the tag
     * <code>&lt;table&nbsp;border rows=2&gt;</code>: <ul>
     * <li> <code>get("border")</code> returns the empty string "". 
     * <li> <code>get("rows")</code> returns <i>2</i>.
     * </ul>
     * <p>
     * Surrounding single and double quote marks that occur in the literal
     * tag are removed from the values reported.  So, for the tag
     * <code>&lt;a href="/foo.html"&nbsp;target=_top&nbsp;onclick='alert("hello")'&gt;</code>: <ul>
     * <li> <code>get("href")</code> returns <i>/foo.html</i> . 
     * <li> <code>get("target")</code> returns <i>_top</i> .
     * <li> <code>get("onclick")</code> returns <i>alert("hello")</i> .
     * </ul>
     *
     * @param	The key to lookup in the current tag's attributes.
     * 
     * @return	The value to which the specified key is mapped, or
     *		<code>null</code> if the key was not in the attributes.
     *
     * @see	LexHTML#getAttributes
     */
    public String
    get(String key)
    {
        String str;

	getAttributes();

        if (map != null) {
	  str = map.get(key);
	  if (str == null) {
	    return null;
	  }
        } else {
          return null;
        }
	return Format.deQuote(str);
    }

    /**
     * Maps the given case-insensitive key to the specified value in the
     * current tag's attributes.
     * <p>
     * The value can be retrieved by calling <code>get</code> with a
     * key that is case-insensitive equal to the given key.
     * <p>
     * If the attributes already contained a mapping for the given key,
     * the old value is forgotten and the new specified value is used.
     * The case of the prior key is retained in that case.  Otherwise
     * the case of the new key is used and a new mapping is made.
     *
     * @param	key
     *		The new key.  May not be <code>null</code>.
     *
     * @param	value
     *		The new value.  May be not be <code>null</code>.
     */
    public void
    put(String key, String value)
    {
	getAttributes();
	map.put(key, quote(value));
	tokenModified = true;
    }

    /**
     * Removes the given case-insensitive key and its corresponding value
     * from the current tag's attributes.  This method does nothing if the
     * key is not in the attributes.
     *
     * @param	key
     *		The key that needs to be removed.  Must not be
     *		<code>null</code>.
     */
    public void
    remove(String key)
    {
	getAttributes();
	map.remove(key);
	tokenModified = true;
    }

    /**
     * Returns an enumeration of the keys in the current tag's attributes.
     * The elements of the enumeration are the string keys.  The keys can
     * be passed to <code>get</code> to get the values of the attributes.
     * 
     * @return	An enumeration of the keys.
     */
    public Enumeration
    keys()
    {
	getAttributes();
	return map.keys();
    }

    /**
     * Instead of modifying an existing token, this method allows the user
     * to completely replace the current token with arbitrary new content.  
     * <p>
     * This method may be called multiple times while processing the current
     * token to add more and more data to the resultant HTML document.
     * Before and/or after calling this method, the <code>appendToken</code>
     * method may also be called explicitly in order to add the current token
     * to the resultant HTML document.
     * <p>
     * Following is sample code illustrating how to use this method
     * to put bold tags around all the <code>&lt;a&gt;</code> tags.
     * <pre>
     * HtmlRewriter hr = new HtmlRewriter(str);
     * while (hr.nextTag()) {
     *     if (hr.getTag().equals("a")) {
     *         hr.append("&lt;b&gt;");
     *         hr.appendToken();
     *     } else if (hr.getTag().equals("/a")) {
     *         hr.appendToken();
     *         hr.append("&lt;/b&gt;");
     *     }
     * }
     * </pre>
     * The calls to <code>appendToken</code> are necessary.  Otherwise,
     * the <code>HtmlRewriter</code> could not know where and when to
     * append the existing token in addition to the new content provided
     * by the user.
     *
     * @param	str
     *		The new content to append.  May be <code>null</code>,
     *		in which case no new content is appended (the equivalent
     *		of appending "").
     *
     * @see	#appendToken
     * @see	#killToken
     */
    public void
    append(String str)
    {
	if (str != null) {
	    sb.append(str);
	}
	appendToken = false;
    }

    /**
     * Appends the current token to the resultant HTML document.
     * If the caller has changed the current token using the 
     * <code>setTag</code>, <code>set</code>, or <code>remove</code>
     * methods, those changes will be reflected.
     * <p>
     * By default, this method is automatically called after each token is
     * processed unless the user has already appended something to the
     * resultant HTML document.  Therefore, if the user appends something
     * and also wants to append the current token, or if the user wants
     * to append the current token a number of times, this method must
     * be called.
     *
     * @see	#append
     * @see	#killToken
     */
    public void
    appendToken()
    {
	appendToken = false;
	if (tokenModified) {
	    getTag();
	    getAttributes();

	    if (getType() == LexHTML.COMMENT) {
		sb.append("<--");
	    } else {
		sb.append('<');
	    }
	    sb.append(tag);
	    int length = map.size();
	    for (int i = 0; i < length; i++) {
		sb.append(' ').append(map.getKey(i));
		String value = map.get(i);
		if ((value != null) && (value.length() > 0)) {
		    sb.append('=').append(value);
		}
	    }
	    if (isSingleton()) {
		if (length > 0) {
		    sb.append(' ');
		}
		sb.append('/');
	    }
	    if (getType() == LexHTML.COMMENT) {
		sb.append("-->");
	    } else {
		sb.append('>');
	    }
	} else {
	    sb.append(getToken());
	}
    }

    /**
     * Tells this <code>HtmlRewriter</code> not to append the current token
     * to the resultant HTML document.  Even if the user hasn't appended
     * anything else, the current token will be ignored rather than appended.
     *
     * @see	#append
     * @see	#killToken
     */
    public void
    killToken()
    {
	appendToken = false;
    }

    /**
     * Turns on or off the automatic accumulation of each token.
     * <p>
     * After each token is processed, the current token is appended to
     * to the resultant HTML document unless the user has already appended
     * something else.  By setting <code>accumulate</code> to
     * <code>false</code>, this behavior is turned off.  The user must then
     * explicitly call <code>appendToken</code> to cause the current token
     * to be appended.
     * <p>
     * Turning off accumulation takes effect immediately, while turning
     * on accumulation takes effect on the next token.  In other words,
     * whether the user turns this setting off or on, the current token
     * will not be added to the resultant HTML document unless the user
     * explicitly calls <code>appendToken</code>. 
     * <p>
     * Following is sample code that illustrates how to use this method
     * to extract the contents of the <code>&lt;head&gt;</code> of the
     * source HTML document.
     * <pre>
     * HtmlRewriter hr = new HtmlRewriter(str);
     * // Don't accumulate tokens until we see the &lt;head&gt; below.
     * hr.accumulate(false);
     * while (hr.nextTag()) {
     *     if (hr.getTag().equals("head")) {
     *         // Start remembering the contents of the HTML document,
     *         // not including the &lt;head&gt; tag itself.
     *
     *         hr.accumulate(true);
     *     } else if (hr.getTag().equals("/head")) {
     *         // Return everything accumulated so far.
     *
     *         return hr.toString();
     *     }
     * }
     * </pre>
     * This method can be called any number of times while processing
     * the source HTML document.
     *
     * @param	accumulate
     *		<code>true</code> to automatically accumulate tokens in the
     *		resultant HTML document, <code>false</code> to require
     *		that the user explicitly accumulate them.
     * @return	The previous accumulate setting
     *
     * @see	#reset
     */
    public boolean
    accumulate(boolean accumulate)
    {
	boolean was = this.accumulate;
	this.accumulate = accumulate;
	appendToken = false;
	return was;
    }

    /**
     * Forgets all the tokens that have been appended to the resultant
     * HTML document so far, including the current token.
     */
    public void
    reset()
    {
	sb.setLength(0);
	appendToken = false;
    }

    /**
     * Puts the current token back.  The next time <code>nextToken</code>
     * is called, it will be the current token again, rather than
     * advancing to the next token in the source HTML document.
     * <p>
     * This is useful when a code fragment needs to read an indefinite
     * number of tokens, but that once some distinguished token is found,
     * needs to push that token back so that normal processing can occur
     * on that token.
     */
    public void
    pushback()
    {
	pushback = true;
    }

    /**
     * Return count of tokens seen so far
     */

    public int
    tokenCount() {
	return tokenCount;
    }

    /**
     * Return count of tags seen so far
     */

    public int
    tagCount() {
	return tagCount;
    }

    /*
     * The set of characters that will turn-on quoting
     */
    // public static String needQuote="' \t%$";  // these need quoting

    /**
     * Helper class to quote a attribute's value when the value is being
     * written to the resultant HTML document.  Values set by the
     * <code>put</code> method are automatically quoted as needed.  This
     * method is provided in case the user is dynamically constructing a new
     * tag to be appended with <code>append</code> and needs to quote some
     * arbitrary values.
     * <p>
     * The quoting algorithm is as follows: <br>
     * If the string contains double-quotes, put single quotes around it. <br>
     * If the string contains any "special" characters, put double-quotes
     * around it.
     * <p>
     * This algorithm is, of course, insufficient for complicated
     * strings that include both single and double quotes.  In that case,
     * it is the user's responsibility to escape the special characters
     * in the string using the HTML special symbols like
     * <code>&amp;quot;</code> or <code>&amp;#34;</code>
     *
     * @return	The quoted string, or the original string if it did not
     *		need to be quoted.
     */

    public static String
    quote(String str) {
	if (str.indexOf('\"') >= 0) {
	    return "\'" + str + "\'";
	} else if (str.length() > 0) {
	    return "\"" + str + "\"";
	} else {
	    return "";
	}
    }

    /**
     * see if target contains any of the strings in candidates
     */

    static private boolean
    contains(String target, String candidates) {
	char[] check = candidates.toCharArray();
	for(int i=0;i<check.length;i++) {
	    if (target.indexOf(check[i]) >= 0) {
		return true;
	    }
	}
	return false;
    }

    private void
    getAttributes()
    {
	if (map == null) {
	    map = lex.getAttributes();
	}
    }

    /**
     * Return a copy of the StringMap of attributes.
     */

    public StringMap
    getMap() {
	getAttributes();
	StringMap result = new StringMap();
	result.append(map, true);
	return result;
    }
}
