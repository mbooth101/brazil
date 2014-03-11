/*
 * ExprProps.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2004 Sun Microsystems, Inc.
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
 * The Initial Developer of the Original Code is: drach.
 * Portions created by drach are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): drach, suhler.
 *
 * Version:  2.3
 * Created by drach on 01/07/11
 * Last modified by suhler on 04/05/24 14:41:51
 *
 * Version Histories:
 *
 * 2.3 04/05/24-14:41:51 (suhler)
 *   doc fixes
 *
 * 2.2 03/07/07-14:44:01 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 2.1 02/10/01-16:36:30 (suhler)
 *   version change
 *
 * 1.6.1.1 02/09/20-10:53:46 (suhler)
 *   removed diagnostics
 *
 * 1.6 02/06/27-18:36:46 (suhler)
 *   don't try to calculate anything that doesn't have a valid operator
 *
 * 1.5 01/11/21-11:43:36 (suhler)
 *   doc fixes
 *
 * 1.4 01/08/17-17:23:55 (drach)
 *   Use most recent request.props instead of cached version
 *
 * 1.3 01/07/16-16:58:18 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.1.1.1 01/07/16-16:50:28 (suhler)
 *   minor doc fixes
 *
 * 1.2 01/07/16-16:15:55 (drach)
 *   Add comments to both.  Add get method to ExprProps.
 *
 * 1.2 01/07/11-16:01:58 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 handlers/ExprProps.java
 *   Name history : 1 0 properties/ExprProps.java
 *
 * 1.1 01/07/11-16:01:57 (drach)
 *   date and time created 01/07/11 16:01:57 by drach
 *
 */

package sunlabs.brazil.properties;

import sunlabs.brazil.server.Request;
import sunlabs.brazil.util.Calculator;
import sunlabs.brazil.util.regexp.Regexp;

import java.util.Properties;

/**
 *
 * <code>ExprProps</code> is a subclass of <code>Properties</code> that
 * is "smart" in the sense that it wraps a <code>Calculator</code>
 * object, passing <code>get</code> and <code>getProperty</code> keys to
 * the <code>Calculator</code> for processing.
 * <p>
 * Keys are first searched for in the <code>Properties</code>
 * object.  If not found, the key is passed to the <code>getValue</code>
 * method of the wrapped <code>Calculator</code> object.  The
 * <code>Calculator</code> will return <code>null</code> if the key
 * consists of only a name or Brazil token and that name/token evaluates
 * to 0.  In this case, a value associated with the key can not be
 * "found" and the <code>get</code> or <code>getproperty</code>method returns
 * <code>null</code> also.
 * <p>
 * Only property names that "look" like expressions (e.g. contain
 * at least one of characters in [&|*+/%=!<>-]) are passed to the calculator.
 * 
 * @author	Steve Drach &lt;drach@sun.com&gt;
 * @version     2.3, 04/05/24
 *
 * @see java.util.Properties
 * @see sunlabs.brazil.util.Calculator
 * @see ExprPropsHandler
 */
public class ExprProps extends Properties {

    private Calculator c;
    private Request request;
    // only keys that have at least one of these are considered
    private static Regexp exp = new Regexp("[&|+*/%=!<>-]");

    /**
     * This constructor creates a <code>Calculator</code> instance with
     * this instance of <code>ExprProps</code> as it's symbol table.
     */
    public ExprProps() {
	super();
	c = new Calculator(this);
    }

    /**
     *
     * This constructor creates a <code>Calculator</code> instance with
     * this instance of <code>ExprProps</code> as it's symbol table and
     * with the <code>Properties</code> instance referenced by the
     * parameter <code>defaults</code> as it's set of default values.
     *
     * @param defaults                the defaults
     */
    public ExprProps(Properties defaults) {
	super(defaults);
	c = new Calculator(this);
    }

    /**
     * This constructor creates a <code>Calculator</code> instance with
     * <code>request.props</code> as it's symbol table. 
     *
     * @param request                 the <code>Request</code> instance
     */
    public ExprProps(Request request) {
	super();
	/*
	 * Note that request.props may have it's value changed, by BSLTemplate
	 * for example, after Calculator is initialized.  Save request so
         * a new request.props can be passed to each call to
         * Calculator.getValue().
         */
	this.request = request;
	c = new Calculator(request.props);
    }

    /**
     * This constructor creates a <code>Calculator</code> instance with
     * <code>request.props</code> as it's symbol table and with the
     * <code>Properties</code> instance referenced by the parameter
     * <code>defaults</code> as it's set of default values.
     *
     * @param request                 the <code>Request</code> instance
     * @param defaults                the defaults
     */
    public ExprProps(Request request, Properties defaults) {
	super(defaults);
	this.request = request;
	c = new Calculator(request.props);
    }

    /**
     * 
     * Returns the value to which the specified key is mapped in this
     * <code>Hashtable</code>.  If the key is not found, then it's value
     * is computed by treating the key as an arithmetic expression or
     * statement.
     * <p>
     * If, during the computation, an <code>ArithmeticException</code> is
     * thrown, the key <code>compute.error</code> is set in the base
     * <code>Properties</code>.  The value associated with the key is
     * an error message.
     *
     * @param key                     the <code>Hashtable</code> key
     *
     * @return                        the value in this table with the
     *                                specified key or <code>null</code>
     */
    public Object get(Object key) {
	Object val = super.get(key);
	if (val == null && key instanceof String &&
		null != exp.match((String) key)) {
	    try {
		remove("compute.error");
		if (request != null) {
		    // request.props may have changed (see BSLTemplate)
		    val =  c.getValue((String)key, request.props);
		} else {
		    val =  c.getValue((String)key);
		}
	    } catch (ArithmeticException e) {
		put("compute.error", e.getMessage());
		val = null; // should be "" perhaps but that breaks things
	    }
	}
	return val;
    }

    private boolean calculating;  // prevent infinite recursion

    /**
     * 
     * Searches for the property with the specified key in this property list.
     * If the key is not found in this property list, then it's value is
     * computed by treating the key as an arithmetic expression or statement.
     * <p>
     * If, during the computation, an <code>ArithmeticException</code> is
     * thrown, the key <code>compute.error</code> is set in the base
     * <code>Properties</code>.  The value associated with the key is
     * an error message.
     * <p>
     * If the result of the computation is <code>null</code>, the default
     * property list, and its defaults, recursively, are then checked. The
     * method returns <code>null</code> if the property is not found.
     *
     * @param key                     the property key
     *
     * @return                        the value in this property list with the
     *                                specified key or <code>null</code>
     *
     * @see java.util.Properties#defaults
     */
    public String getProperty(String key) {
	String val = super.getProperty(key);
	if (val == null && !calculating && null != exp.match(key)) {
	    try {
		remove("compute.error");
		calculating = true;
		/*
                 * It's critical that keys that contain only a name
                 * return a value of null rather than 0 so they can
                 * be found in Property instances further down the
                 * list.
                 */
		if (request != null) {
		    // request.props may have changed (see BSLTemplate)
		    val =  c.getValue(key, request.props);
		} else {
		    val =  c.getValue(key);
		}
		if (val != null) {
		    // System.err.println("Calculator: " + key + " -> " + val);
		}
	    } catch (ArithmeticException e) {
		put("compute.error", e.getMessage());
		val = null; // should be "" perhaps but that breaks things
	    } finally {
		calculating = false;
	    }
	}
	return ((val == null) && (defaults != null)) ? defaults.getProperty(key) : val;
    }
}
