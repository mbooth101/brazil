/*
 * ValidateTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2006 Sun Microsystems, Inc.
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
 * The Initial Developer of the Original Code is: tzhao.
 * Portions created by tzhao are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): Fake, suhler.
 *
 * Version:  1.3
 * Created by tzhao on 06/12/12
 * Last modified by suhler on 06/12/13 15:40:46
 *
 * Version Histories:
 *
 * 1.3 06/12/13-15:40:46 (suhler)
 *   Accepted child'\''s version in workspace "/export/home/suhler/naws".
 *
 * 1.2 06/12/12-16:37:08 (suhler)
 *   date and time created 06/12/12 16:37:08 by tzhao
 *
 * 1.2 70/01/01-00:00:01 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/ValidateTemplate.java
 *
 * 1.1 70/01/01-00:00:00 (Fake)
 *   Fake CodeManager delta to combine two files with no common deltas
 *
 */

package sunlabs.brazil.sunlabs;

import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Glob;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.Calculator;
import sunlabs.brazil.util.regexp.Regexp;
import java.util.Dictionary;

import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * The <code>ValidateTemplate</code> is designed to validate
 * HTML forms.  It performs data validation on sets of
 * properties (e.g. form values) by comparing the values against
 * validation constraints that are specified in other variables.
 * <p>
 * The "glob" attribute defines the set of variables to be
 * validated, and the "types" attribute maps each variable name
 * matched by "glob" into the variable that names the validation
 * tokens for that variable to be validated.
 * The "types" attribute, which is used to identify the
 * validation types may contain variables of the form, ${n} where
 * 0 &lt; n &lt; 10 which represent the string value matching the nth wildcard
 * in the "glob" attribute.
 * <br>
 * Example: glob=*.*.*.*.* and types=types.${3}.${1} <br>
 * will validate a.b.c.d.e against the types contained in types.c.a<br>
 * The default for <code>types</code> is validate.${1}
 * <p>
 * For each validate request, all properties matching the specified
 * <code>glob</code> pattern will be validated against pre-loaded validation
 * keys specified by the glob pattern match <code>types</code>.  The result 
 * will be stored in properties prepended by the <code>prefix</code> attribute,
 * which defaults to the template's prefix.
 * <p>
 * <pre>
 *    &lt;validate glob=<var>pattern</var> [prefix=<var>prepend</var> 
 *      types=<var>pattern (${1-9}.${1-9})</var>]&gt;
 * </pre>
 *
 * <p>
 * <b>Properties validated by ValidateTemplate</b>
 * <p>
 * Validation of properties <code>propNames</code> against glob,
 * regular expression, logic/arithmetic expression, max
 * value (integer), max length (string), min value(integer), min length (string)
 * if validation properties exist.  If there are n validation properties, then
 * all n are evaulated.  A property can fail on multiple validations.
 *
 * <dl>
 * <dt><var>types</var>         <dd>contains the user specified validation key glob 
 *                              pattern to find  a space separated list of 
 *                              validation parameters.
 *                              The typest is built from
 *                              <code>types</code> and <code>glob</code>
 *                              <br>type1 type2 type3 ...
 *                              <br>Example: numeric ssn ...
 *                              <br>If empty <code>type</code> =
 *                              <code>propName</code>
 * <dt><var>types</var>.glob    <dd>contains a glob pattern to match against
 *                              <code>propName</code> defined in the types list.
 * <dt><var>types</var>.regex   <dd>contains a regular expression pattern to match
 *                              against <code>propName</code> defined in the
 *                              types list.
 * <dt><var>types</var>.expr    <dd>contains a boolean/arithmetic expression
 *                              with variable subsititutions to evaluate.
 * <dt><var>types</var>.maxint  <dd>contains the maximum an integer value can have
 *                              if <code>propName</code> is an integer.
 * <dt><var>types</var>.minint  <dd>contains the minimum an integer value can have
 *                              if <code>propName</code> is an integer.
 * <dt><var>types</var>.maxlen  <dd>contains the maximum length of a string if
 *                              <code>propName</code> is a string.
 * <dt><var>types</var>.minlen  <dd>contains the minimum length of a string if
 *                              <code>propName</code> is a string
 * </dl>
 * <p>
 * <b>Properties set by ValidateTemplate</b>
 * <p>
 * If validation fails, the error message to be displayed to the user is up to
 * the developer.  ValidateTemplate does not store any error messages.
 * <br>
 * The default for prepend is <code>validate.</code>
 *
 * <dl>
 * <dt>prepend.numfailed    <dd>contains the number of failed validations.
 * <dt>prepend.failedlist   <dd>contains the list of query properties that
 *                              failed validation along with the type and
 *                              reason.
 *                              <br>Example: propName:type.reason
 *                              <br>         query.1:numeric.glob
 * </dl>
 *
 * Variable substitutions of the for ${...} are permitted for the
 * attributes.

 * @author		Tony Zhao
 * @version		ValidateTemplate.java
 */

public class ValidateTemplate extends Template {
    
    private Calculator calc;
    
    public boolean init(RewriteContext hr) {
        super.init(hr);
        calc = new Calculator(hr.request.props);
        return true;
    }
    
    public void tag_validate(RewriteContext hr) {
        Enumeration propNames;
        int numFailed = 0;
        StringBuffer failedList = new StringBuffer();
        
        String req_glob = hr.get("glob");
        String prefix = hr.get("prepend", hr.prefix);
        String typesformat = hr.get("types", false);
        
        if(req_glob == null || req_glob.equals("")) {
            debug(hr, hr.prefix + ": No glob pattern specified, skipping");
        } else {
            if(typesformat == null || typesformat.equals("")) {
                typesformat = hr.prefix;
                if (!typesformat.equals("") && typesformat.endsWith(".") == false) {
                    typesformat += ".${1}";
                } else {
                    typesformat += "${1}";
                }
            }
            
            hr.killToken();
            debug(hr);
            
            if (!prefix.equals("") && prefix.endsWith(".") == false) {
                prefix += ".";
            }
            
            propNames = hr.request.props.propertyNames(req_glob);
            Properties props = hr.getNamespaceProperties();
            
            while (propNames.hasMoreElements()) {
                String propName = (String)propNames.nextElement();
                GlobProperties typeprops = new GlobProperties(props, req_glob, propName);
                String newName = GlobFormat.subst(typeprops, typesformat);
                String types = hr.request.props.getProperty(newName);
                String prop = hr.request.props.getProperty(propName, null);
                
                hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, "Looking for " + newName +" and got: " + types);
                
                StringTokenizer st;
                if(types != null && !types.equals("")) {
                    st = new StringTokenizer(types);
                } else {
                    st = new StringTokenizer(propName);
                    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, "Using default type: " + propName);
                }
                
                
                if(st.countTokens() == 0){
                    debug(hr, hr.prefix + ": No properties to validate with glob=" + req_glob);
                }
                
                while (st.hasMoreTokens()) {
                    String type = st.nextToken();
                    
                    //Glob
                    String glob = props.getProperty(type + ".glob");
                    if(glob != null && !glob.equals("")) {
                        if(!Glob.match(glob, prop)) {
                            failedList.append(propName + ":" + type + ".glob ");
                            numFailed++;
                        }
                    }
                    
                    //Regex
                    try {
                        String regex = props.getProperty(type + ".regex");
                        if(regex != null && !regex.equals("")) {
                            if(!validateRegex(hr, prop, regex)) {
                                failedList.append(propName + ":" + type + ".regex ");
                                numFailed++;
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        debug(hr, hr.prefix + ": Regex for " + propName + " is invalid");
                    }
                    
                    //Expr
                    try {
                        String expr = props.getProperty(type + ".expr");
                        if(expr != null && !expr.equals("")) {
                            if(!validateExpr(prop, expr)) {
                                failedList.append(propName + ":" + type + ".expr ");
                                numFailed++;
                            }
                        }
                    } catch (ArithmeticException e) {
                        debug(hr, hr.prefix + ": Expr for " + propName + " is invalid");
                    }
                    
                    //Max Integer
                    try {
                        String maxstr = props.getProperty(type + ".maxint");
                        if(maxstr != null && !maxstr.equals("")) {
                            int max = Integer.decode(maxstr).intValue();
                            if(!validateMaxInt(prop, max)) {
                                failedList.append(propName + ":" + type + ".maxint ");
                                numFailed++;
                            }
                        }
                    } catch (NumberFormatException e) {
                        debug(hr, hr.prefix + ": MaxInt for " + propName + " or " + propName + "is an invalid integer");
                    }
                    
                    //Min Integer
                    try {
                        String minstr = props.getProperty(type + ".minint");
                        if(minstr != null && !minstr.equals("")) {
                            int min = Integer.decode(minstr).intValue();
                            if(!validateMinInt(prop, min)) {
                                failedList.append(propName + ":" + type + ".minint ");
                                numFailed++;
                            }
                        }
                    } catch (NumberFormatException e){
                        debug(hr, hr.prefix + ": MinInt for " + propName + " or " + propName + "is an invalid integer");
                    }
                    
                    //Max length
                    try {
                        String maxstr = props.getProperty(type + ".maxlen");
                        if(maxstr != null && !maxstr.equals("")) {
                            int max = Integer.decode(maxstr).intValue();
                            if(!validateMaxLen(prop, max)) {
                                failedList.append(propName + ":" + type + ".maxlen ");
                                numFailed++;
                            }
                        }
                    } catch (NumberFormatException e) {
                        debug(hr, hr.prefix + ": MaxInt for " + propName + " is an invalid integer");
                    }
                    
                    //Min length
                    try {
                        String minstr = props.getProperty(type + ".minlen");
                        if(minstr != null && !minstr.equals("")) {
                            int min = Integer.decode(minstr).intValue();
                            if(!validateMinLen(prop, min)) {
                                failedList.append(propName + ":" + type + ".minlen ");
                                numFailed++;
                            }
                        }
                    } catch (NumberFormatException e){
                        debug(hr, hr.prefix + ": MinLen for " + propName + " is an invalid integer");
                    }
                }
            }
            props.put(prefix + "numfailed", "" + numFailed);
            if(numFailed >0) {
                props.put(prefix + "failedlist", failedList.toString());
            }
        }
    }
    
    static boolean validateRegex(RewriteContext hr, String prop, String regex) throws IllegalArgumentException {
        Regexp regexp;
        
        regexp = new Regexp(regex, hr.isTrue("nocase"));
        if (regexp.match(prop) != null) {
            return true;
        }
        return false;
    }
    
    boolean validateExpr(String prop, String expr) throws ArithmeticException {
        boolean result;
        calc.stringsValid(true);
        return Format.isTrue(calc.getValue(expr));
    }
    
    static boolean validateMaxInt(String prop, int max) throws NumberFormatException {
        int propValue = Integer.parseInt(prop);
        if(propValue > max) {
            return false;
        }
        return true;
    }
    
    static boolean validateMinInt(String prop, int min) throws NumberFormatException {
        int propValue = Integer.parseInt(prop);
        if(propValue < min) {
            return false;
        }
        return true;
    }
    
    static boolean validateMaxLen(String prop, int max) {
        if(prop.length() > max) {
            return false;
        }
        return true;
    }
    
    static boolean validateMinLen(String prop, int min) {
        if(prop.length() < min) {
            return false;
        }
        return true;
    }
    
    /**
     * Special version of a properties that uses the sub expresions
     * of the supplied glob pattern and type to define the keys 1-9.
     * @param
     */
    
    public static class GlobProperties extends Properties {
        Properties realProps;	// normal hr.request.props
        String name;		// glob pattern to match properties to check
        String subStr[] = new String[9];
        
        public GlobProperties(Properties props, String pattern, String name) {
            realProps = props;
            this.name = name;
            Glob.match(pattern, name, subStr);
        }
        
        public String getProperty(String key) {
            int index;
            if (key.length() == 1 && (index="12345678".indexOf(key)) >= 0) {
                String result = subStr[index];
                return result == null ? "" : result;
            } else {
                return realProps.getProperty(key);
            }
        }
    }
    
    /**
     * Special version of a format that uses uses the previously defined
     * GlobProperties.
     */
    
    public static class GlobFormat extends Format {
        
        private static String getProperty(Dictionary dict, String name) {
            int hash = name.indexOf('#');
            String def = "";
            
            if (hash >= 0) {
                def = name.substring(hash + 1);
                name = name.substring(0, hash);
            }
            
            Object obj;
            if (dict instanceof Properties) {
                obj = ((Properties) dict).getProperty(name);
            } else if(dict instanceof GlobProperties) {
                obj = ((GlobProperties) dict).getProperty(name);
            } else {
                obj = dict.get(name);
            }
            String value = (obj == null) ? null : obj.toString();
            
            if (value == null || value.length() == 0)
                value = def;
            
            return value;
        }
    }
}
