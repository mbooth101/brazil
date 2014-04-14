/*
 * BSLTemplate.java
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
 * Contributor(s): cstevens, drach, guym, suhler.
 *
 * Version:  2.16
 * Created by cstevens on 99/10/21
 * Last modified by suhler on 06/11/13 10:36:59
 *
 * Version Histories:
 *
 * 2.16 06/11/13-10:36:59 (suhler)
 *   added "namespace" qualifier to foreach "glob" and "match"
 *
 * 2.15 06/07/26-15:38:48 (suhler)
 *   add "map" attributes for <extract match=...> when all=false
 *   .
 *
 * 2.14 05/06/17-15:46:52 (suhler)
 *   added <if expr="...numeric expression..."> as an if option
 *
 * 2.13 05/06/16-08:04:57 (suhler)
 *   move getNamespaceProperties into the rewriteContext
 *
 * 2.12 05/05/11-11:55:33 (suhler)
 *   added namespace=xxx to <if>
 *
 * 2.11 05/05/11-11:33:05 (suhler)
 *   add "namespace=xxx" option to <extract> to allow extraction results
 *   to go the the specified namespace
 *
 * 2.10 04/12/30-12:38:04 (suhler)
 *   javadoc fixes
 *
 * 2.9 04/12/15-12:30:13 (suhler)
 *   When using <foreach ... list="stuff" delim=[regexp] sort=no>
 *   then the matched delimiter and all of its sub-matches are made
 *   available in the scope of the foreach.
 *
 * 2.8 04/10/26-11:28:48 (suhler)
 *   doc fixes
 *
 * 2.7 04/05/24-15:13:13 (suhler)
 *   add "map" attribute to <extract>
 *
 * 2.6 04/04/28-13:54:54 (suhler)
 *   Mark the propertiesList that holds the foreach iteration values
 *   "transient", so any <imports> done inside of a loop aren't lost
 *   when the loop exits
 *
 * 2.5 03/10/06-09:19:36 (suhler)
 *   Fix NPE when result doesn't match on "all" case
 *
 * 2.4 03/07/10-09:24:04 (suhler)
 *   Use common "isTrue/isFalse" code in utin/format.
 *
 * 2.3 03/07/09-12:28:19 (suhler)
 *   bug fix for tagPrefix handling
 *
 * 2.2 03/07/07-14:04:45 (suhler)
 *   modified to use closingTag() conveniencer macros, so tagPrefixes work
 *   properly
 *
 * 2.1 02/10/01-16:36:45 (suhler)
 *   version change
 *
 * 1.51 02/07/24-10:47:18 (suhler)
 *   doc updates
 *
 * 1.50 02/05/31-11:21:22 (suhler)
 *   change references from PropsTemplate to SetTemplate
 *
 * 1.49 02/05/13-11:49:38 (suhler)
 *   add numeric sorting to glob and match
 *
 * 1.48 02/05/13-10:07:38 (suhler)
 *   <foreach glob=..> now uses the propertyName(glob) method of propertyLists
 *
 * 1.47 01/11/21-11:43:02 (suhler)
 *   doc fixes
 *
 * 1.46 01/08/29-08:57:14 (suhler)
 *   use the Glob enumerator in PropertiesList.  The <foreach .. glob ..>
 *   should be similarly redone
 *
 * 1.45 01/08/28-20:54:53 (suhler)
 *   added <if name=[glob pattern] any>
 *
 * 1.44 01/08/10-16:49:07 (guym)
 *
 * 1.43 01/08/10-14:56:24 (guym)
 *   Per code review, made Abort, Break, Continue flags an enum, plus fixed a couple of bugs
 *
 * 1.42 01/08/03-20:27:29 (guym)
 *   Finished documentation for <abort>, <break>, <continue> - also made code thread-safe.
 *
 * 1.41 01/08/03-15:27:35 (drach)
 *   Add PropertiesList
 *
 * 1.40 01/08/03-11:06:55 (guym)
 *
 * 1.39 01/08/01-15:00:40 (guym)
 *   Added <abort>, <break>, and <continue> tags to BSL - right now, only the <abort> tag has passed all regressions tests however.
 *
 * 1.38 01/07/26-16:41:43 (guym)
 *   Merged changes between child workspace "/home/guym/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.33.1.1 01/07/26-16:30:01 (guym)
 *   Added <break>, <abort> and <continue> functionality
 *
 * 1.37 01/07/17-18:40:17 (suhler)
 *   doc fixes
 *
 * 1.36 01/07/16-16:46:17 (suhler)
 *   use new Template convenience methods
 *
 * 1.35 01/07/11-16:27:04 (suhler)
 *   added <extract replace=xxx> option.
 *
 * 1.34 01/06/25-10:44:15 (suhler)
 *   Removed "while".  No-one is using it, and we can always put it back
 *   later
 *   Unified the treatment of "Boolean" attributes
 *   Added a nocase option to regexp matching
 *
 * 1.33 01/06/04-11:06:01 (suhler)
 *   allow an alternative to the empty string for missing properties
 *
 * 1.32 01/05/24-10:43:46 (suhler)
 *   added regular expression delimiters
 *
 * 1.31 01/05/22-16:58:13 (suhler)
 *   added "all" attribute to <extract> to permit extraction from all matches
 *
 * 1.30 01/05/11-14:50:08 (suhler)
 *   use template.debug()
 *
 * 1.29 01/05/11-10:42:15 (suhler)
 *   "compareToIgnoreCase" doesn't exist in jdk1.1
 *
 * 1.28 01/05/08-15:23:19 (cstevens)
 *   Case-insensitive sorting.
 *
 * 1.26.1.1 01/04/04-11:49:36 (suhler)
 *
 * 1.27 01/04/02-12:05:55 (cstevens)
 *   BSLTemplate.java: When glob or regexp pattern had more than 20 subexpressions,
 *   a limit was being exceeded that should have been detected, but instead was
 *   causing an ArrayOutOfBoundsException due to an off-by-one programming error.
 *   BSLTemplate.java: Add server property "${prefix}.limit" to the <while> tag to
 *   prevent excessive recursion.  It's a server property not a request property so
 *   accidental or malicious HTML cannot change the limit.
 *   BSLTemplate.java: The <foreach> tag now creates temporary variables in a new
 *   Properties object rather than in the main request.properties.  Seems to be a
 *   cleaner implementation.  It took longer to (A) save any old values, set the
 *   temporary values, and then restore the old values than to (B) push a new
 *   Properties object that lives for the body of the <foreach>.
 *
 * 1.26 01/03/23-13:23:27 (cstevens)
 *   <while> tag and documentation
 *
 * 1.25 01/02/13-13:40:48 (cstevens)
 *   Merged changes between child workspace "/home/cstevens/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.23.1.1 01/02/12-17:11:35 (cstevens)
 *   Record whether <extract> tag worked
 *
 * 1.24 01/01/14-14:51:42 (suhler)
 *   doc fixes.  Change prefix -> prepend
 *
 * 1.23 01/01/11-18:47:20 (cstevens)
 *   <extract> tag for BSL
 *
 * 1.22 00/12/11-13:30:30 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.21 00/10/26-15:59:52 (suhler)
 *   implement serializable
 *   .
 *
 * 1.20 00/10/05-15:51:50 (cstevens)
 *   PropsTemplate.subst() and PropsTemplate.getProperty() moved to the Format
 *   class.
 *
 * 1.19 00/07/06-15:49:21 (suhler)
 *   doc update
 *
 * 1.18 00/07/06-11:35:34 (cstevens)
 *   broke "sort=" in BSL.
 *
 * 1.17 00/07/05-14:11:49 (cstevens)
 *   PropsTemplate and BSLTemplate allow '[' and ']' to specify a variable
 *   substitution wherever a constant was used before.  For example, as in
 *   '<if name=stock value="[favorite]">' to compare the value of property "stock"
 *   not to the constant "[favorite]", but to the value of the property "favorite".
 *   In order to include a literal "[" in an option, use "\[" -- a backslash
 *   escapes the following character, whatever it is.
 *
 * 1.16 00/06/29-10:46:41 (suhler)
 *   added the '[]' capability to  glob and match values of foreach
 *
 * 1.15 00/05/31-13:50:26 (suhler)
 *   docs
 *
 * 1.14 00/05/24-11:26:46 (suhler)
 *   add docs
 *
 * 1.13 00/05/10-16:31:20 (suhler)
 *   Added the "delim=" option to <foreach list> and <foreach property>
 *   to specifiy the token delimiter in property lists.
 *
 * 1.12 00/05/10-13:37:01 (suhler)
 *   added docs
 *
 * 1.11 00/04/27-16:04:39 (cstevens)
 *   Merged changes between child workspace "/home/cstevens/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.9.1.1 00/04/27-16:02:24 (cstevens)
 *   Sorting in <foreach> tag.
 *
 * 1.10 00/04/26-16:08:27 (suhler)
 *   added documentation
 *
 * 1.9 00/04/17-14:25:58 (cstevens)
 *   PropsTemplate.java:
 *   1. exposes public method to parse and dereference composed property
 *   names (using the '[' and ']' characters).  Method is used by BSLTemplate.
 *
 * 1.8 00/04/12-15:55:48 (cstevens)
 *   debugging comments
 *
 * 1.7 00/03/29-16:16:10 (cstevens)
 *   PropsTemplate exposes public method to parse and dereference composed property
 *   names (using the '[' and ']' characters).  Method is used by BSLTemplate.
 *
 * 1.6 00/02/07-11:36:49 (cstevens)
 *   Merged changes between child workspace "/home/cstevens/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.4.1.1 00/02/07-11:32:35 (cstevens)
 *   If </if> was missing, BSL template got a null-pointer exception.
 *
 * 1.5 00/02/02-09:35:35 (suhler)
 *   claim to be serializable
 *
 * 1.4 99/11/17-10:20:03 (suhler)
 *   fixed debug flag
 *
 * 1.3 99/10/26-20:51:07 (cstevens)
 *   docs
 *
 * 1.2 99/10/26-17:12:53 (cstevens)
 *   Changed TclServerTemplate and BSLTemplate to use the config parameter "debug"
 *   to decide whether to emit comments into the resultant HTML file.
 *
 * 1.2 99/10/21-18:22:20 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/BSLTemplate.java
 *
 * 1.1 99/10/21-18:22:19 (cstevens)
 *   date and time created 99/10/21 18:22:19 by cstevens
 *
 */

package sunlabs.brazil.template;

import sunlabs.brazil.properties.PropertiesList;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.util.Glob;
import sunlabs.brazil.util.Sort;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.Calculator;
import sunlabs.brazil.util.regexp.Regexp;
import sunlabs.brazil.util.regexp.Regsub;
import sunlabs.brazil.session.SessionManager;

import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.Serializable;

/**
 * The <code>BSLTemplate</code> takes an HTML document with embedded "BSL"
 * markup tags in it and evaluates those special tags to produce a
 * standard HTML document.
 * <p>
 * BSL stands for Brazil Scripting Language.  BSL can be used to substitute
 * data from the request properties into the resultant document.  However,
 * rather than simple property substitution as is provided by the
 * <code>SetTemplate</code>, this class provides the ability to iterate
 * over and choose amongst the values substituted with a set of simple
 * flow-control constructs.
 * <p>
 * BSL uses the following special tags as its language constructs: <ul>
 * <li> <code>&lt;if&gt;</code>
 * <li> <code>&lt;foreach&gt;</code>
 * <li> <code>&lt;abort&gt;</code>
 * <li> <code>&lt;break&gt;</code>
 * <li> <code>&lt;continue&gt;</code>
 * <li> <code>&lt;extract&gt;</code>
 * </ul>
 * <p>
 * This template recursively evalutes the bodies/clauses of the BSL commands,
 * meaning that they may contain nested BSL and/or other tags defined by
 * other templates.
 * <p>
 * The following configuration parameter is used to initialize this
 * template.
 * <dl class=props>
 * <dt> <code>debug</code>
 * <dd> If this configuration parameter is present, this template replaces
 *	the BSL tags with comments, so the user can keep track of where
 *	the dynamically generated content is coming from by examining the
 *	comments in the resultant HTML document.  By default, the BSL tags
 *	are completely eliminated from the HTML document rather than changed
 *	into comments.
 * </dl>
 * <hr>
 * <h3>&lt;if&gt; TAG</h3>
 * The <code>&lt;if&gt;</code> tag evaluates one of its clauses dependant
 * upon the value of the provided conditions.  The other clauses are not
 * evaluated and do not appear in the resultant HTML document.
 * The general format of the <code>&lt;if&gt;</code> tag is as follows:
 * <pre>
 * &lt;if [not] <var>condition</var>&gt;
 *     <var>clause</var>
 * &lt;elseif [not] <var>condition</var>&gt;
 *     <var>clause</var>
 * &lt;else&gt;
 *     <var>clause</var>
 * &lt;/if&gt; </pre>
 *
 * The <code>&lt;elseif&gt;</code> and <code>&lt;else&gt;</code> tags are
 * optional, and multiple <code>&lt;elseif&gt;</code> tags may be present.
 * <code>&lt;elseif&gt;</code> may also be spelled <code>&lt;elif&gt;</code>
 * or <code>&lt;else if&gt;</code>.  The optional parameter <code>not</code>
 * reverses the sense of the specified condition.  
 * <p>
 * <a name=condition></a>
 * Following are the formats of the <var>condition</var>: <dl>
 * <p>
 * <dt> <code>&lt;if name=<var>var</var>&gt;</code>
 * <dd> Test if the value of property <var>var</var> is set and is not "",
 *	"false", "no", "off", or the number 0.
 * <p>
 * <dt> <code>&lt;if name=<var>var</var> value=<var>string</var>&gt;</code>
 * <dd> Test if the value of property <var>var</var> is equal to the
 *	given <var>string</var>.
 * <p>
 * <dt> <code>&lt;if name=<var>pattern</var> <var>any</var>&gt;</code>
 * <dd> Test if any property exists that matches matches the given
 *	{@link sunlabs.brazil.util.Glob glob} <var>pattern</var>.
 *      Note: This may be expensive if there are large numbers of
 *      properties.
 * <p>
 * <dt> <code>&lt;if name=<var>var</var> glob=<var>pattern</var>&gt;</code>
 * <dd> Test if the value of property <var>var</var> matches the given
 *	{@link sunlabs.brazil.util.Glob glob} <var>pattern</var>.
 * <p>
 * <dt> <code>&lt;if name=<var>var</var> match=<var>pattern</var>&gt;</code>
 * <dd> Test if the value of property <var>var</var> matches the given
 *	{@link sunlabs.brazil.util.regexp.Regexp regular expression}
 *	<var>pattern</var>.
 *      <p>
 *      if the attribute <code>nocase</code> is present, then a case
 *      insensitive match is performed.
 * <dt> <code>&lt;if expr=<var>numeric expression</var>&gt;</code>
 * <dd> The numeric expression is evaluated.  If the result is "1", then
 *	the condition is satisfied.  This uses
        {@link sunlabs.brazil.util.Calculator} to evaluate the expression.
	Any vaiable that is defined, but not "0"m "off" or "no" is considered
	* to have a value of "1" for the purposes of the expression evaluation.
	This allows (as an example) the expression
	  <code>"x && y && ! z"</code>
	to evaluate to "true" only if the variables "x" and "y", but not
	"z" are defined *(and not "0", "no" or "false".
 * </dl>
 * Normally, the variable is look-up in the Properties Stack, starting
 * with the Reqest Properties.  The attribute "namespace" may be used
 * to look-up the variable in the specified namespace.
 * <hr>
 * <h3>&lt;foreach&gt; TAG</h3>
 * The <code>&lt;foreach&gt;</code> tag repeatedly evaluates its body a
 * selected number of times.  Each time the body is evaluated, the provided
 * named property is set to the next word in the provided list of words.
 * The body is terminated by the <code>&lt;/foreach&gt;</code> tag.
 * This tag is especially useful for dynamically producing lists and tables.
 * <ul>
 * <li><pre>
 * &lt;foreach name=<var>var</var> list="<var>value1 value2 ...</var>" [delim="<var>chars</var>"]&gt;
 *     &lt;get <var>var</var>&gt;
 *     <var>body</var>
 * &lt;/foreach&gt;</pre>
 *
 * Iterate over the set of values "<var>value1 value2 ...</var>".  The named
 * property <var>var</var> is assigned each value in turn.
 * <p>
 * If the optional parameter <code>delim</code> specifies, the
 * delimiter for splitting the list into elements.
 * <ul>
 * <li>If <code>delim</code> is
 * a single character, then that character is used as the delimiter.
 * <li>If <code>delim</code> is not
 * specified or is the empty string "", the delimiter is whitespace.  
 * <li>Otherwise, the delimeter is taken to be a regular expression.
 * If the regular expression is invalid, the entire list is taken as
 * a single element.
 * <li>If the delimiter is a regular expression, and no sorting
 *     is requested (I was lazy), in addition to the property
 *     <var>var</var>, the properties
 *     <var>var.delime</var>, <var>var.delim.1</var> ...
 *     are made available that represent the value of the previous
 *     delimiter and all of its sub-matches (if any).
 * </ul>.
 *
 * <p><li><pre>
 * &lt;foreach name=<var>var</var> property=<var>property</var> [delim="<var>chars</var>"]&gt;
 *     &lt;get <var>var</var>&gt;
 *     <var>body</var>
 * &lt;/foreach&gt; </pre>
 *
 * Iterate over the values in the other <var>property</var>.  The value
 * of the other property is broken into elements and each element is
 * assigned to the named property <var>var</var> in turn.  This form is
 * equivalent to <code>&lt;foreach&nbsp;name=<var>var</var>&nbsp;list=${<var>property</var>}&gt;</code>.
 * <p>
 * If the optional parameter <code>delim</code> is specified, the characters
 * are delimiters for splitting the list into elements using the
 * <code>StringTokenizer</code> rules.  If <code>delim</code> is not
 * specified or is the empty string "", the delimiter is whitespace.  
 *
 * <p><li><pre>
 * &lt;foreach name=<var>var</var> glob=<var>pattern</var>&gt;
 *     &lt;get <var>var</var>.name&gt;
 *     &lt;get <var>var</var>.value&gt;
 *
 *     &lt;get <var>var</var>.name.1&gt;
 *     &lt;get <var>var</var>.name.2&gt;
 *     <var>body</var>
 * &lt;/foreach&gt; </pre>
 *
 * Iterate over all the properties whose name matches the
 * {@link sunlabs.brazil.util.Glob glob} <var>pattern</var>.  In turn, the
 * following properties are set:
 * <br>
 * <code><var>var</var>.name</code> is the name of the property.
 * <br>
 * <code><var>var</var>.value</code> is the value of the property.
 * <br>
 * <code><var>var</var>.name.1</code>, <code><var>var</var>.name.2</code>, ...
 * are the substrings matching the wildcard characters in the pattern, if any.
 * 
 * <p><li><pre>
 * &lt;foreach name=<var>var</var> match=<var>pattern</var>&gt;
 *     &lt;get <var>var</var>.name&gt;
 *     &lt;get <var>var</var>.value&gt;
 *
 *     &lt;get <var>var</var>.name.0&gt;
 *     &lt;get <var>var</var>.name.1&gt;
 *     &lt;get <var>var</var>.name.2&gt;
 *     <var>body</var>
 * &lt;/foreach&gt; </pre>
 *
 * Iterate over all the properties whose name matches the
 * {@link sunlabs.brazil.util.regexp.Regexp regular expression}
 * <var>pattern</var>.  In turn, the following properties are set:
 * <br>
 * <code><var>var</var>.name</code> is the name of the property.
 * <br>
 * <code><var>var</var>.value</code> is the value of the property.
 * <br>
 * <code><var>var</var>.name.0</code> is the substring that matched the whole
 * pattern.
 * <br>
 * <code><var>var</var>.name.1</code>, <code><var>var</var>.name.2</code>, ...
 * are the substrings matching the parenthesized subexpressions, if any.
 * <p>
 * NOTE:  In the current implementation, when there are large numbers of
 * property values, using <code>glob</code> is (potentially) much more
 * efficient than <code>match</code> for locating names.
 * </ul>
 * All the temporary properties that this tag creates are visible only
 * within the <var>body</var> for the duration of the
 * <code>&lt;foreach&gt;</code>.
 * <p>
 * if the attribute <code>nocase</code> is present, then a case
 * insensitive match is performed.
 * <p>
 * When either <code>glob</code> or <code>match</code> is specifies, then
 * the "namespace" attribute may be used to restrict the name lookups to start
 * with that namespace.  However, the values aren't restricted to being in the
 * specified namespace. [Not clear if this is useful for anything]
 * <h3>Sorting using <code>foreach</code></h3>
 * The &lt;foreach&gt; tag contains a feature to
 * change the order of iteration.  This facility is intended for
 * common sorting operations.  For general purpose manipulation of
 * the iteration order, the order should be defined either in another
 * handler, or by using the
 * {@link sunlabs.brazil.tcl.TclServerTemplate &lt;server&gt;}
 * directive.
 * <p>
 * The four additional parameters used to control sorting are:
 * <dl>
 * <dt> <code>reverse</code>
 * <dd> The list of items is iterated in the reverse order.
 * <p>
 * <dt> <code>sort[=<var>key</var>]</code>
 * <dd> The items to be iterated over are sorted.  If no <var>key</var> is
 *	supplied, the items are sorted by the property name.  If a
 *	<var>key</var> is supplied, its value is used as the sort key for
 *	the iteration.  For this to be meaningful, the key should contain
 *	variable substitutions (e.g. ${...}, see
 *	{@link sunlabs.brazil.util.Format#getProperty getProperty}).  A
 *	sample use of the sort key would be:
 * <pre>
 * &lt;foreach name=id property=employee.ids sort="${employee.${id}.last}, ${employee.${id}.first}"&gt;</pre>
 *	This option can be tricky to use correctly.  The following example
 *	will not sort employees by last name:
 * <pre>
 * 1. &lt;foreach name=id property=employee.ids sort="employee.${id}.last"&gt;</pre>
 *	Why?  Because another level of ${...} needs to be inserted in the
 *	sort key:
 * <pre>
 * 2. &lt;foreach name=id property=employee.ids sort="<b>${</b>employee.${id}.last<b>}</b>"&gt;</pre>
 *	Example (1) will just sort the literal strings "employee.1234.last",
 *	"employee.5678.last", etc. while example (2) will do the correct
 *	thing and sort the values "Stevens" and "Johnson".  Remember that BSL
 *	sorts based on exactly what you pass it and does not know that the
 *	provided string should be treated as another variable itself.
 * <p>
 * <dt> <code>numeric</code>
 * <dd> When used in conjunction with the <code>sort</code> parameter, it
 *      causes the items to be interpreted as numbers (or zero if the item
 *	doesn't look like a number).
 * <p>
 * <dt> <code>nocase</code>
 * <dd> When used in conjunction with the <code>sort</code> parameter, it
 *      causes the items to be sorted in a case-insensitive fashion.
 *	<p>
 *	Note that when
 *	used with <code>"glob=..."</code> or <code>"match=..."</code>, it
 *	does <b>NOT</b> cause the glob or regular expression to be case
 *	insensitive.  It causes the results of the glob or regexp to be
 *	sorted in a case-insensitive fashion.  There is currently no way
 *	to specify a case-insensitive glob or regular expression.
 * </dl>
 * <hr>
 * <h3>&lt;abort&gt; TAG</h3>
 * The <code>&lt;abort&gt;</code> tag terminates processing of the current
 * HTML page at the point it is evaluated.  All HTML on the page after the
 * <code>&lt;abort&gt;</code> tag is discarded, and the HTML processed up to
 * that point is returned.  This tag can be placed anywhere on a page,
 * including within a <code>&lt;foreach&gt;</code> or <code>&lt;if&gt;</code>
 * construct.
 * <p>
 * The following is an example usage of this tag: 
 * <p>
 * <pre>
 * &lt;foreach name=x list="0 1 2 3"&gt;
 *   &lt;if name=x value=3&gt;
 *     &lt;abort&gt;
 *   &lt;/if&gt;
 *   &lt;get name=x&gt;
 * &lt;/foreach&gt; 
 * Testing
 * </pre>
 * This example produces the output:
 * <p>
 * <pre>
 * 0 1 2
 * </pre>
 * <hr>
 * <h3>&lt;break&gt; TAG</h3>
 * The <code>&lt;break&gt;</code> tag terminates processing within a
 * <code>&lt;foreach&gt;</code> construct.  The processing of HTML 
 * continues immediately after the <code>&lt;/foreach&gt;</code>.  This 
 * tag can only be used inside of a <code>&lt;foreach&gt;</code> tag.
 * <p>
 * The following is an example usage of this tag: 
 * <p>
 * <pre>
 * &lt;foreach name=x list="0 1 2 3"&gt;
 *   &lt;if name=x value=3&gt;
 *     &lt;break&gt;
 *   &lt;/if&gt;
 *   &lt;get name=x&gt;
 * &lt;/foreach&gt; 
 * Testing
 * </pre>
 * This example produces the output:
 * <p>
 * <pre>
 * 0 1 2 Testing
 * </pre>
 * <hr>
 * <h3>&lt;continue&gt; TAG</h3>
 * The <code>&lt;continue&gt;</code> tag continues processing at the top 
 * of a <code>&lt;foreach&gt;</code> construct.  This skips any HTML 
 * after the <code>&lt;continue&gt;</code> tag and before the 
 * <code>&lt;/foreach&gt;</code>.  This tag can only be used inside of a 
 * <code>&lt;foreach&gt;</code> tag.
 * <p>
 * The following is an example usage of this tag: 
 * <p>
 * <pre>
 * &lt;foreach name=x list="0 1 2 3"&gt;
 *   &lt;if name=x value=2&gt;
 *     &lt;continue&gt;
 *   &lt;/if&gt;
 *   &lt;get name=x&gt;
 * &lt;/foreach&gt; 
 * Testing
 * </pre>
 * This example produces the output:
 * <p>
 * <pre>
 * 0 1 3 Testing
 * </pre>
 * <hr>
 * <h3>&lt;extract&gt; TAG</h3>
 * The <code>&lt;extract&gt;</code> tag permits portions of a property's
 * value to be extracted into additional properties, based on either glob
 * or regular expression patterns.  The extract tag takes the following
 * tag parameters:
 * <dl>
 * <dt> <code>name=<var>var</var></code>
 * <dd> The name of the property whose value will be split up and
 *	extracted.
 * <p>
 * <dt> <code>prepend=<var>base</var></code>
 * <dd>	Optional parameter that specifies the <var>base</var> string to
 *	prepend to the extracted properties.  The default value for
 *	<var>base</var> is the specified property name <var>var</var>.
 * <p>
 * <dt>	<code>glob=<var>pattern</var></code>
 * <dd>	The {@link sunlabs.brazil.util.Glob glob} <var>pattern</var> to
 *	match against.  In turn, the following properties are set:
 *	<br>
 *	<code><var>base</var>.1</code>, <code><var>base</var>.2</code>, ...
 *	are the substrings that matched the wildcard characters in
 *	the <var>pattern</var>.
 * <p>
 * <dt>	<code>match=<var>pattern</var></code>
 * <dd>	The {@link sunlabs.brazil.util.regexp.Regexp regular expression}
 *	<var>pattern</var> to match against.  In turn, the following
 *	properties are set:
 *	<br>
 *	<code><var>base</var>.0</code> is the substring that matched the
 *	whole <var>pattern</var>.
 *	<br>
 *	<code><var>base</var>.1</code>, <code><var>base</var>.2</code>, ...
 *	are the substrings that matched the parenthesized subexpressions
 *	in <var>pattern</var>, if any.
 *      <p>
 *      If the attribute <code>all</code> is present, then all matches and
 *      submatches are extracted into properties. The properties
 *      <code><var>base</var>.0</code>, <code><var>base</var>.1</code>,
 *	etc., are set to the 1st matched expression, the 2nd matched
 *      expression, etc.  The properties
 *      <code><var>base</var>.0.0</code>, <code><var>base</var>.0.1</code>...
 *      are set to the sub-matches of the first full match, and so forth.
 *	If any matches are found the following additional 
 *      properties are set:
 *      <dl class=props>
 *      <dt><code><var>base</var>matches</code>
 *      <dd>The number of times the regular expression was matched.
 *      <dt><code><var>base</var>submatches</code>.
 *      <dd>The number of sub-expressions for this regular expression.
 *      <dt><code><var>base</var>matchelist</code>.
 *      <dd>The list of matches (e.g. "1 2 3 ...").
 *      </dl>
 * <dt>	<code>replace=<var>substitution</var></code>
 * <dd> If specified (with match), then no portions of the value are
 *      extracted.  Instead, 
 *	a regular expression substitution is performed (see
 *      {@link sunlabs.brazil.util.regexp.Regexp#sub substitution}).
 *	The resultant substituted value is placed in the property:
 *      <var>prefix</var>.replace.
 * <dt><code>map</code>
 * <dd>A white space separated list of names that will be used to
 *     name sub-matches, instead of .1, .2, ... etc. See
 *     {@link #tag_extract} for more detail.
 * </dl>
 * One of <code>glob</code> or <code>match</code> must be specified.
 * <p>
 * In addition, the property <code><var>base</var>.matches</code> is
 * set to a value indicating the number of matches and submatches stored.
 * This property can be examined and compared with <code>0</code> to
 * determine if the <code>&lt;extract&gt;</code> tag matched at all.  If
 * there was no match, the numbered properties <code><var>base</var>.N</code>
 * are not set or changed from their previous value.
 * <hr>
 * Anytime an argument is specified to one of the BSL tags, variable
 * substitution as described in {@link sunlabs.brazil.util.Format#getProperty
 * getProperty} may be used.
 * <p>
 * Any time a boolean parameter (XXX) is allowed
 * (nocase, not, numeric, or reverse) it is considered false if
 * it takes any of the forms: XXX=0, XXX=no, XXX=false XXX="". If
 * it takes the forms XXX or XXX="anything else", the value is true.
 * <p>
 * see <a href=/bsl.html>a sample HTML page that contains some BSL
 * markup</a>.
 * @see SetTemplate
 */

public class BSLTemplate extends Template implements Serializable {
    String tagPrefix = "";	// prefix all our tags with this


    public boolean
    init(RewriteContext hr) {
	return super.init(hr);
    }

    /* Public states that we can put our RewriteContext into */

    public final static int ABORT    = 0x1;
    public final static int BREAK    = 0x2;
    public final static int CONTINUE = 0x4;
    
    /**
     * Handles the "abort" tag.
     */
    public void
    tag_abort(RewriteContext hr)
    {
	hr.killToken();

     	/* Tell our RewriteContext to abort */

        hr.abort();
    }

    /**
     * Handles the "break" tag.
     */
    public void
    tag_break(RewriteContext hr)
    {
	hr.killToken();

        /* Perform our break operation */

        if (!hr.setRewriteState(BREAK)) {
          debug(hr,"No enclosing <foreach> for this <break>");
        }
    }

    /**
     * Handles the "continue" tag.
     */
    public void
    tag_continue(RewriteContext hr)
    {
	hr.killToken();

        /* Perform our continue operation */

        if (!hr.setRewriteState(CONTINUE)) {
          debug(hr,"No enclosing <foreach> for this <continue>");
        }
    }

    /**
     * Handles the "foreach" tag.
     */
    public void
    tag_foreach(RewriteContext hr)
    {
	String name = hr.get("name");

	if (name == null) {
	    return;
	}

        hr.incrNestingLevel();

	hr.killToken();
	debug(hr);

	String arg;
	boolean done = false;

	if ((arg = hr.get("list")) != null) {
	    done = foreach_list(hr, name, arg, hr.get("delim"));
	} else if ((arg = hr.get("glob")) != null) {
	    done = foreach_match(hr, name, arg, true);
	} else if ((arg = hr.get("match")) != null) {
	    done = foreach_match(hr, name, arg, false);
	} else if ((arg = hr.get("property")) != null) {
	    arg = hr.request.props.getProperty(arg, "");
	    done = foreach_list(hr, name, arg, hr.get("delim"));
	}

	if (done == false) {
	    /*
	     * This doesn't mean "error", but that the body of the <foreach>
	     * was not processed, so we are not sitting at the </foreach>
	     * and we need to skip to it.
	     * For instance, <foreach name=x list=""><foo></foreach>.
	     */

	    hr.accumulate(false);
	    skipTo(hr, "foreach");
	    hr.accumulate(true);
	}
	debug(hr);
        hr.decrNestingLevel();
    }

    private static class SortThing
    {
	String index;
	String s;
	double d;
	
	public
	SortThing(String index, String key)
	{
	    this.index = index;
	    this.s = key;
	}

	public
	SortThing(String index, double d)
	{
	    this.index = index;
	    this.d = d;
	}
    }

    private static class Compare
	implements Sort.Compare
    {
	boolean numeric;
	boolean reverse;
	boolean nocase;
	
	Compare(boolean numeric, boolean reverse, boolean nocase)
	{
	    this.numeric = numeric;
	    this.reverse = reverse;
	    this.nocase = nocase;
	}

	public int
	compare(Object array, int index1, int index2)
	{
	    SortThing[] sa = (SortThing[]) array;
	    SortThing s1 = sa[index1];
	    SortThing s2 = sa[index2];

	    int result = 0;
	    if (numeric) {
		if (s1.d > s2.d) {
		    result = 1;
		} else if (s1.d == s2.d) {
		    result = 0;
		} else {
		    result = -1;
		}
	    } else if (nocase) {
		result = s1.s.toLowerCase().compareTo(s2.s.toLowerCase());
	    } else {
		result = s1.s.compareTo(s2.s);
	    }

	    if (reverse) {
		result = -result;
	    }
	    return result;
	}
    }

    /* This method checks to see if a <break> or <abort> tag has been processed */
    /* It returns a boolean value which is used in foreach_list() and           */
    /* foreach_match() to determine whether processing of the incoming HTML     */
    /* should continue.							        */

    private boolean
    isBreakAbort(RewriteContext hr)
    {
	if (!hr.checkRewriteState(ABORT) && !hr.checkRewriteState(BREAK)) {
          /* We haven't hit an <abort> or <break> tag - continue */
          /* processing */

          return false;
        } else if (hr.checkRewriteState(BREAK)) {
          /* We've encountered a <break> tag - set the flag back to */
          /* false so we can pop "up" one nesting level and continue */
          /* processing */

          hr.unsetRewriteState(BREAK);
          return true;
        } else if (hr.getNestingLevel() == 1) {
          /* We've encountered an <abort> tag within the outermost */
          /* <foreach> - set the flag to false.                    */
 
          hr.unsetRewriteState(ABORT);
          return true;
        } else {
          /* We've encountered an <abort> tag within a nested */
          /* <foreach> - leave the flag set to true and pop   */
          /* "up" one nesting level to continue processing the */
          /* abort "event" */

          return true;
        }
    }

    /* This method processes each element of a list within a <foreach> tag */
    /* e.g. - <foreach name=index list="0 1 2 3 4 5">			   */

    private boolean
    foreach_list(RewriteContext hr, String name, String list, String delim) {
	BSLProps local = new BSLProps(hr.request);

	boolean isRe = false;
	StringTokenizer st;
	if (delim == null || (delim.length() == 0)) {
	    st = new StringTokenizer(list);
	} else if (delim.length()==1) {
	    st = new StringTokenizer(list, delim);
	} else {
	    try {
		st = new ReStringTokenizer(list, delim);
		isRe = true;
	    } catch (IllegalArgumentException e) {
	        debug(hr, "Bad re: " + delim);
		st = new StringTokenizer(list, "");
	    }
	}

        String rest = hr.lex.rest();
	boolean any = false;

	String sort = hr.get("sort", false);  // don't expand ${..} yet
	boolean reverse = hr.isTrue("reverse");

	if ((sort == null) && (reverse == false)) {
	    while (st.hasMoreTokens()) {
		if (isRe) {	// previous token
		   Regsub rs = ((ReStringTokenizer)st).getRs();
		   String matched = rs.matched();
		   if (matched != null) {
		       local.push(name + ".delim", matched);
		       int subs = rs.getRegexp().subspecs();
		       for(int i=1; i<subs;i++) {
			    String sub = rs.submatch(i);
			    if (sub != null) {
				local.push(name + ".delim." + i, sub);
			    }
		       }
		   }
		}
		local.push(name, st.nextToken());
		any = true;

                processTo(hr, rest, "foreach");

                /* Check for a <break> or <abort> event */

                if (isBreakAbort(hr)) {
		  break;
                }
	    }
	} else if ((sort == null) && (reverse == true)) {
	    Vector v = new Vector();
	    while (st.hasMoreTokens()) {
		v.addElement(st.nextToken());
	    }
	    for (int i = v.size(); --i >= 0; ) {
		local.push(name, v.elementAt(i));
		any = true;

                processTo(hr, rest, "foreach");

                /* Check for a <break> or <abort> event */

                if (isBreakAbort(hr)) {
		  break;
                }
	    }
	} else {
	    boolean numeric = hr.isTrue("numeric");
	    boolean nocase = hr.isTrue("nocase");

	    if (sort.length() == 0) {
		sort = null;
	    }

	    Vector v = new Vector();
	    while (st.hasMoreTokens()) {
		String value = st.nextToken();
		String key = value;
		if (sort != null) {
		    local.push(name, value);
		    key = Format.subst(local, sort);
		}
		if (numeric) {
		    v.addElement(new SortThing(value, getDouble(key)));
		} else {
		    v.addElement(new SortThing(value, key));
		}
	    }
	    SortThing[] array = new SortThing[v.size()];
	    v.copyInto(array);

	    Sort.qsort(array, new Compare(numeric, reverse, nocase));

	    for (int i = 0; i < array.length; i++) {
		local.push(name, array[i].index);
		any = true;

                processTo(hr, rest, "foreach");

                /* Check for a <break> or <abort> event */

                if (isBreakAbort(hr)) {
                  break;
                }
	    }
	}

	local.restore();

	return any;
    }

    static private double getDouble(String s) {
	double d = 0;
	try {
	    d = Double.valueOf(s).doubleValue();
	} catch (Exception e) {}
	return d;
    }

    private static class Bag
    {
	BSLProps local;
	Regexp r;
	int subspecs;
	String pat;
	String nameProp;
	String valueProp;
	String[] sub;
	String[] subName;
    }

    private final static int MAX_MATCHES = 20;

    private boolean
    foreach_match(RewriteContext hr, String name, String pat, boolean glob)
    {
	Enumeration names;

	Properties props = hr.getNamespaceProperties();
	if (glob) {
	    if (props instanceof PropertiesList) {
	        names = ((PropertiesList) props).propertyNames(pat);
	    } else {
	        names = new PropertiesList(props).propertyNames(pat);
            }
	} else {
	    names = props.propertyNames();
	}

	BSLProps local = new BSLProps(hr.request);

	Bag bag = new Bag();
	bag.local = local;
	bag.subspecs = MAX_MATCHES;
	bag.pat = pat;
	bag.nameProp = name + ".name";
	bag.valueProp = name + ".value";

	if (glob) {
	    bag.subspecs = MAX_MATCHES;
	    bag.sub = new String[bag.subspecs + 1];
	} else {
	    try {
		bag.r = new Regexp(pat);
	    } catch (IllegalArgumentException e) {
		return false;
	    }
	    bag.subspecs = bag.r.subspecs();
	    bag.sub = new String[bag.subspecs];
	}

	bag.subName = new String[bag.sub.length];
	for (int i = 0; i < bag.subName.length; i++) {
	    bag.subName[i] = bag.nameProp + "." + i;
	}

	String rest = hr.lex.rest();
	boolean any = false;

	String sort = hr.get("sort", false);  // don't expand ${..} yet
	boolean numeric = hr.isTrue("numeric");
	boolean reverse = hr.isTrue("reverse");

	if (sort == null) {
	    /*
	     * Elements are being retrieved in random (hashtable) order.
	     * Don't bother returning randomly-ordered elements in reverse
	     * order.
	     */

	    while (names.hasMoreElements()) {
		String value = (String) names.nextElement();
		if (matches(bag, true, value)) {
		    any = true;

                    processTo(hr, rest, "foreach");

                    /* Check for a <break> or <abort> event */

                    if (isBreakAbort(hr)) {
                      break;
                    }
		}
	    }
	} else {
	    boolean setProps = (sort.length() > 0);

	    Vector v = new Vector();
	    while (names.hasMoreElements()) {
		String value = (String) names.nextElement();
		if (matches(bag, setProps, value)) {
		    String key = value;
		    if (setProps) {
			key = Format.subst(local, sort);
		    }
		    if (numeric) {
			v.addElement(new SortThing(value, getDouble(key)));
		    } else {
			v.addElement(new SortThing(value, key));
		    }
		}
	    }

	    SortThing[] array = new SortThing[v.size()];
	    v.copyInto(array);

	    boolean nocase = hr.isTrue("nocase");
	    Sort.qsort(array, new Compare(numeric, reverse, nocase));

	    for (int i = 0; i < array.length; i++) {
		String value = array[i].index;
		matches(bag, true, value);
		any = true;

                processTo(hr, rest, "foreach");

                /* Check for a <break> or <abort> event */

                if (isBreakAbort(hr)) {
                  break;
                }
	    }
	}

	local.restore();
	return any;
    }

    private boolean
    matches(Bag bag, boolean setProps, String name)
    {
	boolean match;
	
	if (bag.r == null) {
	    match = Glob.match(bag.pat, name, bag.sub);
	    if (match && setProps) {
		bag.sub[MAX_MATCHES] = null;
		for (int i = 0; bag.sub[i] != null; i++) {
		    bag.local.push(bag.subName[i + 1], bag.sub[i]);
		}
	    }
	} else {
	    match = bag.r.match(name, bag.sub);
	    if (match && setProps) {
		for (int i = 0; i < bag.subspecs; i++) {
		    if (bag.sub[i] == null) {
			bag.sub[i] = "";
		    }
		    bag.local.push(bag.subName[i], bag.sub[i]);
		}
	    }
	}
	if (match && setProps) {
	    bag.local.push(bag.nameProp, name);
	    bag.local.push(bag.valueProp, bag.local.getUncovered(name));
	}
	return match;
    }

    /**
     * Handles the "if" tag.
     */
    public void
    tag_if(RewriteContext hr)
    {
	hr.killToken();

	boolean test = true;
	while (test) {
	    debug(hr);
	    if (isTrue(hr)) {
		processClause(hr);
		break;
	    } else {
		test = false;
		hr.accumulate(false);
		while (hr.nextTag()) {
		    if (hr.isClosingFor("if", false)) {
			skipTo(hr, "if");
		    } else if (hr.isClosingFor("if")) {
			break;
		    } else if (hr.isClosingFor("else", false)) {
			hr.accumulate(true);
			if (hr.get("if", false) != null) {
			    test = true;
			    break;
			} 
			debug(hr);
			processClause(hr);
			break;
		    } else if (hr.isClosingFor("elseif", false) ||
				hr.isClosingFor("elif", false)) {
			hr.accumulate(true);
			test = true;
			break;
		    }
		}
	    }
	}
	debug(hr);
	hr.accumulate(true);
    }

    /* This method evaluates the arguments of an <if> and returns   */
    /* a boolean indicating whether the arguments are true or false */

    private boolean
    isTrue(RewriteContext hr)
    {
	boolean not = hr.isTrue("not");
	String name = hr.get("name");
	String expr = hr.get("expr"); // use the calculator
	boolean result = false;

	// new expr="expression" options
	if (name==null && expr != null) {
	    Calculator calc = new Calculator(hr.request.props);
	    calc.stringsValid(true);
	    try {
		result = Format.isTrue(calc.getValue(expr));
	    } catch (ArithmeticException e) {
		debug(hr, "Invalid expression: " + expr);
	    }
	    return not ^ result;
	}

	if (name == null) {
	    name = hr.getArgs();
	}

	Properties props = hr.getNamespaceProperties();
	String value = Format.getProperty(props, name, "");

	String arg;
	if ((arg = hr.get("value")) != null) {
	    result = arg.equals(value);
	} else if ((arg = hr.get("match")) != null) {
	    boolean ignoreCase = hr.isTrue("nocase");
	    try {
		result = (new Regexp(arg, ignoreCase).match(value) != null);
	    } catch (IllegalArgumentException e) {}
	} else if ((arg = hr.get("glob")) != null) {
	    result = Glob.match(arg, value);
	} else if (hr.isTrue("any")) {
	    result=hr.request.props.propertyNames(name).hasMoreElements();
	} else {
	    /*
	     * Unknown or no argument specified, treat value as boolean: if
	     * named property exists and is not "", "false", "no", "off", or
	     * 0, then return true.
	     */

	    if ((value.length() == 0) || Format.isFalse(value)) {
		result = false;
	    } else {
		try {
		    result = Integer.decode(value).intValue() != 0;
		} catch (Exception e) {
		    result = true;
		}
	    }
	}
	return not ^ result;
    }

    /* This method processes the body of an <if> tag - it is */
    /* called when the isTrue() method returns true          */

    private void
    processClause(RewriteContext hr)
    {
	while (hr.nextTag()) {
	    if (hr.isClosingFor("if")) {
		break;
	    } else if (hr.isClosingFor("else", false)
		    || hr.isClosingFor("elseif", false)
		    || hr.isClosingFor("elif", false)) { 
		hr.accumulate(false);
		skipTo(hr, "if");
		break;
	    } else {
		hr.process();
	    }
	}
    }

    /* This is a convenience method which grabs all tokens from the */
    /* current one to the end token that matches the one passed if  */

    private static void
    skipTo(RewriteContext hr, String match)
    {
	while (hr.nextTag()) {
	    if (hr.isClosingFor(match, false)) {
		skipTo(hr, match);
	    } else if (hr.isClosingFor(match)) {
		break;
	    }
	}
    }
   
    /* This method processes the body of a <foreach> tag - it is */
    /* called once for each iteration of the <foreach>           */

    private void
    processTo(RewriteContext hr, String source, String tag)
    {
	hr.lex.replace(source);
        while (hr.nextTag()) {
          if (hr.isClosingFor(tag)) {
            /* If we have hit the closing tag (usually a /foreach) */
            /* take the token out of the output stream and return */

	    hr.killToken();
	    break;
          }
          /* See if any templates have an interest in this token */

          hr.process();

          /* Check to see if we've encountered any <abort>, <break>     */
          /* or <continue> tags as a result of the template processing. */
          /* If we encounter an <abort>, we just return.  If we get     */
          /* a <break> or <continue>, we 'eat' the remaining tokens     */
          /* until the endTag, then return.                             */

          if (hr.checkRewriteState(ABORT)) {
            return;
          } else if (hr.checkRewriteState(BREAK) || hr.checkRewriteState(CONTINUE)) {
	    hr.accumulate(false);
	    skipTo(hr,tag);
	    hr.accumulate(true);
            break;
          }
        }

        /* Set our CONTINUE flag back to false */

        hr.unsetRewriteState(CONTINUE);
    }

    /**
     * Handle the [experimental] "extract" tag.
     * This permits parts of a property's value to be extracted into
     * additional properties, based on either glob or regular expression
     * patterns.
     * <br>
     * &lt;extract name= prepend= glob= match=&gt;
     * <dl>
     * <dt>name   <dd>The name of the property to extract
     * <dt>prepend<dd>The base name for all extracted properties 
     *		  (defaults to "name"). If it doesn't end with a ".", 
     *		  one is added.
     * <dt>glob   <dd>The glob pattern to use for extraction.  The text
     *		  matching each wildcard in the pattern is extracted.
     * <dt>match  <dd>The regular expression pattern to use for extraction.
     *		  The text matching each sub-expression is extracted. If
     *		  "glob" is specified, then "match" is ignored.
     * <dt>null  <dd>the value to return if there was no match
     *		 [or sub-match] (defaults to "").
     * <dt>map  <dd>a white space separated list of names that will be used to
     *		name sub-matches, instead of .1, .2, ... etc.  If there are
     *          more sub expressions than names, then the indeces are used
     *		after the names run out. The example:
     *		<pre>
     *		&lt;set name=entry value="joe:211A:x3321"&gt;
     *		&lt;extract name=entry glob=*:*:* map="name room phone"&gt;
     *		</pre>
     *          Will return the values:
     *          <pre>
     *          entry.name=joe
     *          entry.room=211A
     *          entry.phone=x3321
     *          </pre>
     *		In "glob" extraction, each wildcard in the glob pattern is assigned
     *		the next token in "map". in Regular expression extractions, when
     *		"all" is specified, the map names are used to name the sub-expressions.
     *          Without "all" the names are assigned like "glob", only the first name
     *		gets the entire match (e.g. you need one more name for "match" than
     *		for "glob".
     * <td> namespace <dd>
     *		Normally, results are extracted into the current request
     *		namespace.  If <i>namespace</i> is specified, then the results
     *		are placed into the named namespace.  The names "server"
     *		and "local" are special (see SetTemplate). 
     *		<p>NOTE: The namespace will be accessable by any other
     *		templates associated with the same TemplateRunner, using
     *		the default sessionTable (see SetTemplate).
     * </dl>
     */

    public void
    tag_extract(RewriteContext hr)
    {
	String name = hr.get("name");
	String prefix = hr.get("prepend");
	String glob = hr.get("glob");
	String match = hr.get("match");
	String nullStr = hr.get("null");
	String map = hr.get("map");
	String mapTable[] = {};

	if (nullStr == null) {	// what to return for no match
	    nullStr = "";
	}

	if (name == null) {
	    return;
	}

	if (map != null) {
	    StringTokenizer st = new StringTokenizer(map);
	    mapTable = new String[st.countTokens()];
	    int i=0;
	    while (st.hasMoreTokens()) {
		mapTable[i++] = st.nextToken();
	    }
	}

	String value = Format.getProperty(hr.request.props, name, "");

	hr.killToken();
	debug(hr);

	if (prefix == null) {
	    prefix = name;
	}
	if (prefix.endsWith(".") == false) {
	    prefix += ".";
	}

	// pick the proper namespace for extraction

	Properties props = hr.getNamespaceProperties();
	int matches = 0;
	
	if (glob != null) {
	    String[] sub = new String[MAX_MATCHES + 1];
	    if (Glob.match(glob, value, sub)) {
		sub[MAX_MATCHES] = null;
		for (int i = 0; sub[i] != null; i++) {
		    String s = mapTable.length > i ? mapTable[i] : "" + (i+1);
		    props.put(prefix + s, sub[i]);
		    matches++;
		}
	    }
	} else if (match != null) {
	    Regexp r;
	    try {
		r = new Regexp(match, hr.isTrue("nocase"));
		String replace = hr.get("replace");
		boolean all = hr.isTrue("all");

		if (replace != null) {
		    String result;
		    if (all) {
		        result = r.subAll(value, replace);
		    } else {
		        result = r.sub(value, replace);
		    }
		    props.put(prefix + "replace",
			    result==null ? nullStr :result);
		} else {
		   int subMatches = Math.min(MAX_MATCHES, r.subspecs());
		   String[] sub = new String[subMatches];
		   if (all) {	  // all matches
		       Regsub rs = new Regsub(r, value);
		       StringBuffer counter = new StringBuffer();
		       while(rs.nextMatch()) {
			   matches++;
			   props.put(prefix + matches, rs.matched());
			   for (int i = 1; i < subMatches; i++) {
			       String s = mapTable.length >= i ? mapTable[i-1] : "" + i;
			       props.put(prefix + matches + "." + s,
				       (rs.submatch(i)==null ? nullStr : rs.submatch(i)));
			   }
			   counter.append(matches + " ");
		       }
		       if (matches>0) {
			   props.put(prefix + "submatches", "" + subMatches);
			   props.put(prefix + "matchlist", counter.toString());
		       }
		   } else if (r.match(value, sub)) {	// first match
		       for (int i = 0; i < sub.length; i++) {
			   if (sub[i] == null) {
			       sub[i] = "";
			   }
		           String s = mapTable.length > i ? mapTable[i] : "" + i;
			   props.put(prefix + s, sub[i]);
			   matches++;
		       }
		   }
	       }
	    } catch (IllegalArgumentException e) {}
	}
	props.put(prefix + "matches", Integer.toString(matches));
    }

    /**
     * Properties object used by foreach for its local variables.
     */

    static class BSLProps extends PropertiesList {
	Request r;
	PropertiesList defaults;
	
	public
	BSLProps(Request r) {
	    this.r = r;
	    defaults = r.props;
	    addBefore(defaults);
	    r.props = this;
	}

	public Object
	put(Object key, Object value) {
	    return defaults.put(key, value);
	}

	public Object
	remove(Object key) {
	    return defaults.remove(key);
	}

	/**
	 * Mark this properties as transient, 'cause it will be
	 * removed after the foreach ends.
	 */

	public boolean isTransient() {
	    return true;
	}

	public void
	push(Object key, Object value) {
	    super.put(key, value);
	}

	public String
	getUncovered(String key) {
	    return defaults.getProperty(key);
	}

	public void
	restore() {
	    remove();
	    r.props = defaults;
	}
    }

    /**
     * StringTokenizer that uses regular expression patters as delimiters.
     * This version implements only the
     * the subset of StringTokenizer used by this file.
     * It's not general purpose.  If the
     * usage above changes, then this will probably break.
     */

    class ReStringTokenizer extends StringTokenizer {
	Regsub rs;	// the regexp to split on
	boolean more;	// have more tokens
	boolean done=false;

	public
	ReStringTokenizer(String str, String re)
	throws IllegalArgumentException {
	    super(str, re);
	    rs = new Regsub((new Regexp(re)), str);
	}

	/**
	 * StringTokenizer method
	 */

	public boolean
	hasMoreTokens() {
	    if (done) {
		return false;
	    } else {
		more = rs.nextMatch();
		// return (more || rs.rest().length() > 0);
		return true;
	    }
	}

	/**
	 * StringTokenizer method
	 */

	public String nextToken() {
	    if (more) {
		return rs.skipped();
	    } else {
		done=true;
		return rs.rest();
	    }
	}

	/**
	 * Allow access to the underlying regsub object
	 */

	public Regsub getRs() {
	    return rs;
	}
    }
}
