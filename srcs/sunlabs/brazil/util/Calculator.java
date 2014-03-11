/*
 * Calculator.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2005 Sun Microsystems, Inc.
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
 * Version:  2.4
 * Created by drach on 01/06/29
 * Last modified by suhler on 05/06/17 15:49:54
 *
 * Version Histories:
 *
 * 2.4 05/06/17-15:49:54 (suhler)
 *   added stringsValid() to alter the way non-numeric strings are
 *   converted to numbers
 *
 * 2.3 04/11/30-15:19:45 (suhler)
 *   fixed sccs version string
 *
 * 2.2 03/07/10-09:24:20 (suhler)
 *   Use common "isTrue/isFalse" code in utin/format.
 *
 * 2.1 02/10/01-16:37:02 (suhler)
 *   version change
 *
 * 1.4 01/08/17-16:57:57 (drach)
 *   Add getValue(String, Dictionary) method
 *
 * 1.3 01/07/16-16:54:36 (suhler)
 *   monro fix to main
 *
 * 1.2 01/07/13-16:39:38 (drach)
 *   Add comments and a test driver.
 *
 * 1.2 01/06/29-11:04:56 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 util/Calculator.java
 *
 * 1.1 01/06/29-11:04:55 (drach)
 *   date and time created 01/06/29 11:04:55 by drach
 *
 */

package sunlabs.brazil.util;

import java.text.DecimalFormat;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

/**
 *
 * Calculator implements a simple arithmetic expression evaluator.  It
 * can evaluate typical expressions with the "normal" operators and
 * precedence. Formally, the BNF for the supported grammar is:
 * <code><pre>
 * &lt;stmt&gt;    ::= &lt;var&gt; = &lt;expr&gt; | &lt;expr&gt;
 * &lt;expr&gt;    ::= &lt;rexpr&gt; | &lt;expr&gt; &lt;bool op&gt; &lt;rexpr&gt;
 * &lt;bool op&gt; ::= && | &lt;or&gt;
 * &lt;or&gt;      ::= ||
 * &lt;rexpr&gt;   ::= &lt;aexpr&gt; | &lt;rexpr&gt; &lt;rel op&gt; &lt;aexpr&gt;
 * &lt;rel op&gt;  ::= &lt; | &lt;= | &gt; | &gt;= | == | !=
 * &lt;aexpr&gt;   ::= &lt;term&gt; | &lt;aexpr&gt; &lt;add op&gt; &lt;term&gt;
 * &lt;add op&gt;  ::= + | -
 * &lt;term&gt;    ::= &lt;factor&gt; | &lt;term&gt; &lt;mult op&gt; &lt;factor&gt;
 * &lt;mult op&gt; ::= * | / | %
 * &lt;factor&gt;  ::= &lt;var&gt; | &lt;num&gt; | ! &lt;factor&gt; | ( &lt;expr&gt; )
 * &lt;var&gt;     ::= &lt;letter&gt; | &lt;var&gt; &lt;var2&gt;
 * &lt;var2&gt;    ::= &lt;letterordigit&gt; | . | _
 * &lt;num&gt;     ::= &lt;unum&gt; | + &lt;unum&gt; | - &lt;unum&gt;
 * &lt;unum&gt;    ::= &lt;int&gt; | &lt;int&gt; . | &lt;int&gt; . &lt;int&gt; | . &lt;int&gt;
 * &lt;int&gt;     ::= &lt;digit&gt; | &lt;int&gt; &lt;digit&gt;
 * </pre></code>
 * A &lt;letter&gt; is defined as a Java <code>char</code> for which
 * <code>Char.isLetter(char)</code> is <code>true</code>.  A
 * &lt;letterordigit&gt; is defined as a Java <code>char</code> for which
 * <code>Char.isLetterOrDigit(char)</code> is <code>true</code>. A digit
 * is defined as a Java <code>char</code> for which
 * <code>Char.isDigit(char)</code> is <code>true</code>.
 * <p>
 * Values for <code>&lt;var&gt;</code> are looked up in the supplied
 * <code>Dictionary</code>.  If <code>&lt;var&gt;</code> can not be found,
 * it is assumed to have the value zero.  If the value found is "true" or
 * "yes" (case insensitive), it is assumed to be one.  Similarly, if the
 * value found is "false" or "no", it is assumed to be zero.  Assignment
 * to <code>&lt;var&gt;</code> stores the computed value in the same
 * <code>Dictionary</code>.
 * <p>
 * The period in <code>&lt;unum&gt;</code>, if there is one, must be
 * immediately adjacent to surrounding <code>&lt;int&gt;</code>s.
 * 
 * @author	Steve Drach &lt;drach@sun.com&gt;
 * @version		2.4
 */
public class Calculator {

    private Token t;
    private Tokenizer tknizr;
    private String error;
    private Dictionary symbols;
    private DecimalFormat decimalFormat;
    private boolean allStringsValid = false;

    /**
     *
     * The no argument constructor will create an internal
     * <code>Hashtable</code> in which it looks up and stores values
     * associated with variables.
     *
     * @see java.util.Hashtable
     */
    public Calculator() {
	this(new Hashtable());
    }

    /**
     *
     * This constructor will use the <code>Dictionary</code> parameter to
     * lookup and store values associated with variables.
     *
     * @param d                       the <code>Dictionary</code> object
     *                                that serves as a symbol table
     *
     * @see java.util.Dictionary
     */
    public Calculator(Dictionary d) {
	symbols = d;
	decimalFormat = new DecimalFormat();
	decimalFormat.setGroupingUsed(false);
    }

    /**
     * Normally, variables whose values are "on", "yes", or
     * "true" and converted to "1.0", while the values "off", "no", and
     * "false" are converted to "0.0".  All other values are considered
     * an error.  By passing "true", all normally invalid strings are
     * given a value of "1.0".
     * <p>
     */

    public void stringsValid(boolean allStringsValid) {
	this.allStringsValid = allStringsValid;
    }

    /**
     *
     * Computes the value of the statement passed in the parameter
     * string and returns a string representation of the result.
     * If the input statement consists only of a variable name and
     * the result of the computation is zero, <code>null</code> is
     * returned.
     *
     * @param stmt                      a string representation of
     *					an arithmetic expression or
     *                                  assignment
     *
     * @exception ArithmeticException 	occurs when a result is
     *					improper (e.g. infinity) or
     *					when the input statement can
     *                                  not be parsed
     *
     * @return                          a string representation of
     *                                  the computed result or
     *                                  <code>null</code>
     */
    public String getValue(String stmt) throws ArithmeticException {
	tknizr = new Tokenizer(stmt);
	t = tknizr.next();
	String value = stmt();
	if (error != null) {
	    String s = error;
	    error = null;
	    throw new ArithmeticException(s);
	}
	return value;
    }

    /**
     *
     * Computes the value of the statement passed in the parameter
     * string and returns a string representation of the result.
     * If the input statement consists only of a variable name and
     * the result of the computation is zero, <code>null</code> is
     * returned.  The second parameter is used as a symbol table
     * for the duration of this method call.  Note this method is
     * not thread safe!
     *
     * @param stmt                      a string representation of
     *					an arithmetic expression or
     *                                  assignment
     *
     * @param d                         the temporary symbol table
     *
     * @exception ArithmeticException 	occurs when a result is
     *					improper (e.g. infinity) or
     *					when the input statement can
     *                                  not be parsed
     *
     * @return                          a string representation of
     *                                  the computed result or
     *                                  <code>null</code>
     */
    public String getValue(String stmt, Dictionary d) throws ArithmeticException {
	Dictionary symbols = this.symbols;
	this.symbols = d;
	String value = null;
	try {
	    value = getValue(stmt);
	} finally {
	    this.symbols = symbols;
	}
	return value;
    }

    /*
     * <stmt> ::= <var> = <expr> | <expr>
     */
    private String stmt() {
	if (error != null)
	    return "";
	
	debug("stmt");

	double value = 0.0;

	if (t.type == VAR) {
	    String name = t.name;
	    value = t.value;
	    t = tknizr.next();
	    if (t.type == ASSIGN) {
		t = tknizr.next();
		String s = toString(expr());
		if (s.length() != 0)
		    symbols.put(name, s);
		return s;
	    }
	    if (t.type == END) {
		if (value == 0.0)
		    return null;  // for compatibility with a wrapper Property
		else
		    return toString(value);
	    }
	    t = tknizr.pushback();
	}
	
	return toString(expr());
    }

    private String toString(double value) {
	if (t.type != END) {
	    error("Unexpected token: " + t.name);
	    return "";
	}
	if (Double.isNaN(value)) {
	    error("NaN");
	    return "";
	}
	if (Double.isInfinite(value)) {
	    error("Infinity");
	    return "";
	}
	return decimalFormat.format(value);
    }

    /*
     * <expr> ::= <rexpr> | <expr> <bool op> <rexpr>
     * <bool op> ::= && | <or>
     * <or> ::= ||
     */
    private double expr() {
	if (error != null)
	    return 0.0;

	debug("expr");

	int i = 0;

	double value = rexpr();

	loop: while (true) {
	    switch (t.type) {
	    case AND:
		t = tknizr.next();
		i = (new Double(rexpr())).intValue();
		value = (i != 0 && (new Double(value)).intValue() != 0) ? 1.0 : 0.0;
		break;
	    case OR:
		t = tknizr.next();
		i = (new Double(rexpr())).intValue();
		value = (i != 0 || (new Double(value)).intValue() != 0) ? 1.0 : 0.0;
		break;
	    default:
		break loop;
	    }
	}

	return value;
    }

    /*
     * <rexpr> ::= <aexpr> | <rexpr> <rel op> <aexpr>
     * <rel op> ::= < | <= | > | >= | == | !=
     */
    private double rexpr() {
	if (error != null)
	    return 0.0;

	debug("rexpr");

	double value = aexpr();

	loop: while (true) {
	    switch (t.type) {
	    case LT:
		t = tknizr.next();
		value = value < aexpr() ? 1.0 : 0.0;
		break;
	    case LE:
		t = tknizr.next();
		value = value <= aexpr() ? 1.0 : 0.0;
		break;
	    case GT:
		t = tknizr.next();
		value = value > aexpr() ? 1.0 : 0.0;
		break;
	    case GE:
		t = tknizr.next();
		value = value >= aexpr() ? 1.0 : 0.0;
		break;
	    case EQ:
		t = tknizr.next();
		value = value == aexpr() ? 1.0 : 0.0;
		break;
	    case NE:
		t = tknizr.next();
		value = value != aexpr() ? 1.0 : 0.0;
		break;
	    default:
		break loop;
	    }
	}

	return value;
    }

    /*
     * <aexpr> ::= <term> | <aexpr> <add op> <term>
     * <add op> ::= + | -
     */
    private double aexpr() {
	if (error != null)
	    return 0.0;

	debug("aexpr");

	double value = term();

	loop: while (true) {
	    switch (t.type) {
	    case PLUS:
		t = tknizr.next();
		value += term();
		break;
	    case MINUS:
		t = tknizr.next();
		value -= term();
		break;
	    default:
		break loop;
	    }
	}

	return value;
    }

    /*
     * <term> ::= <factor> | <term> <mult op> <factor>
     * <mult op> ::= * | / | % 
     */
    private double term() {
	if (error != null)
	    return 0.0;

	debug("term");

	double value = factor();

	loop: while (true) {
	    switch (t.type) {
	    case STAR:
		t = tknizr.next();
		value *= factor();
		break;
	    case SLASH:
		t = tknizr.next();
		value /= factor();
		break;
	    case MOD:
		t = tknizr.next();
		value %= factor();
		break;
	    default:
		break loop;
	    }
	}

	return value;
    }

    /*
     * <factor> ::= <var> | <num> | ! <factor> | ( <expr> )
     */
    private double factor() {
	if (error != null)
	    return 0.0;

       	debug("factor");

	boolean minus = false;

	switch (t.type) {
	case MINUS:
	    minus = true;
	case PLUS:
	    t = tknizr.next();
	    break;
	}

	double value = 0.0;

	switch (t.type) {
	case VAR:
	    value = t.value;
	    if (minus)
		value *= -1.0;
	    t = tknizr.next();
	    break;
	case NUM:
	    value = t.value;
	    if (minus)
		value *= -1.0;
	    t = tknizr.next();
	    break;
	case NOT:
	    t = tknizr.next();
	    if (minus)
		value *= -1.0;
	    if ((new Double(factor())).intValue() == 0)
		value = 1.0;
	    break;
	case LPAREN:
	    t = tknizr.next();
	    value = expr();
	    if (minus)
		value *= -1.0;
	    if (t.type == RPAREN)
		t = tknizr.next();
	    else
		error("Right parenthesis expected: " + tknizr.remainder());
	    break;
	default:
	    error("Unrecognized factor: " + tknizr.remainder());
	    break;
	}

	return value;
    }

    private void error(String msg) {
	if (error == null)
	    error = msg.substring(0,1).toUpperCase() + msg.substring(1);
    }

    /**
     *
     * Set <code>true</code> for debug output.  The output probably won't
     * make sense to anyone other than the author.
     */
    public boolean debugging;

    private void debug(String msg) {
	if (debugging)
	    System.out.println(msg);
    }

    private void debug(Token t) {
	if (debugging)
	    System.out.println(t);
    }

    private class Token {
	int type;
	double value;
	String name;

	public String toString() {
	    StringBuffer sb = new StringBuffer("type=" + type);
	    if (type == VAR)
		sb.append("\nname=" + name);
	    if (type >= VAR)
		sb.append("\nvalue=" + value);
	    return sb.toString();
	}

    }

    private class Tokenizer {
	private Token[] token;
	private char[] chars;
	private int i, t;

	Tokenizer(String str) {
	    chars = str.trim().toCharArray();
	    i = 0;
	    token = new Token[2];
	    token[0] = new Token();
	    token[1] = new Token();
	    t = 1;
	}

	private boolean pb;

	Token pushback() {
	    pb = true;
	    t = 1 - t;
	    debug("token is free");
	    debug(token[t]);
	    return token[t];
	}

	Token next() {
	    if (pb) {
		pb = false;
		t = 1 - t;
		debug("token is free");
		debug(token[t]);
		return token[t];
	    }

	    debug("token costs $$");

	    t = 1 - t;

	    while (i < chars.length && chars[i] == ' ')
		i++;

	    if (i >= chars.length) {
		token[t].type = END;
		debug(token[t]);
		return token[t];
	    }

	    debug("char=" + chars[i]);

	    int tmp = 0;
	    switch (chars[i]) {
	    case '+':
		tmp = PLUS;
		break;
	    case '-':
		tmp = MINUS;
		break;
	    case '*':
		tmp = STAR;
		break;
	    case '/':
		tmp = SLASH;
		break;
	    case '%':
		tmp = MOD;
		break;
	    case '(':
		tmp = LPAREN;
		break;
	    case ')':
		tmp = RPAREN;
		break;
	    case '=':
		tmp = ASSIGN;
		if (++i < chars.length && chars[i] == '=')
		    tmp = EQ;
		else
		    --i;
		break;
	    case '!':
		tmp = NOT;
		if (++i < chars.length && chars[i] == '=')
		    tmp = NE;
		else
		    --i;
		break;
	    case '<':
		tmp = LT;
		if (++i < chars.length && chars[i] == '=')
		    tmp = LE;
		else
		    --i;
		break;
	    case '>':
		tmp = GT;
		if (++i < chars.length && chars[i] == '=')
		    tmp = GE;
		else
		    --i;
		break;
	    case '&':
		if (++i < chars.length && chars[i] == '&')
		    tmp = AND;
		else
		    --i;
		break;
	    case '|':
		if (++i < chars.length && chars[i] == '|')
		    tmp = OR;
		else
		    --i;
		break;
	    }

	    if (tmp != 0) {
		i++;
		token[t].type = tmp;
		token[t].name = signs[tmp];
		debug(token[t]);
		return token[t];
	    }

	    int j = i;
	    while ((Character.isDigit(chars[i]) || chars[i] == '.')
		   && ++i < chars.length);
	    if (j != i) {
		String s = new String(chars, j, i-j);
		try {
		    token[t].value = (new Double(s)).doubleValue();
		} catch (NumberFormatException e) {
		    if ((s = e.getMessage()).equals("."))
			s = "Single point";
		    error(s + ": " + remainder(j));
		}
		token[t].type = NUM;
		token[t].name = s;
		debug(token[t]);
		return token[t];
	    }

	    if (Character.isLetter(chars[i])) {
		j = i;
		while ((Character.isLetterOrDigit(chars[i]) || chars[i] == '.'
			|| chars[i] == '_') && ++i < chars.length);
		String name = new String(chars, j, i-j);
		Object o;
		if (symbols instanceof Properties)
		    o = ((Properties)symbols).getProperty(name);
		else
		    o = symbols.get(name);
		if (o == null) {
		    token[t].value = 0.0;  // do not store name=0 in symbols
		} else {
		    String s = o.toString().trim();
		    try {
			if (s.length() == 0) {
			    token[t].value = 0.0;
			} else if (Character.isLetter(s.charAt(0))) {
			    if (Format.isFalse(s)) {
				token[t].value = 0.0;
			    } else if (allStringsValid || Format.isTrue(s)) {
				token[t].value = 1.0;
			    } else {
				s = "Invalid value '" + s + "' from";
				throw new NumberFormatException(s);
			    }
			} else {
			    token[t].value = (new Double(s)).doubleValue();
			}
		    } catch (NumberFormatException e) {
			if ((s = e.getMessage()).equals("."))
			    s = "Single point";
			error(s + ": " + remainder(j));
		    }
		}
		token[t].type = VAR;
		token[t].name = name;
		debug(token[t]);
		return token[t];
	    }

	    error("Unrecognized token: " + remainder());
	    token[t].type = END;
	    debug(token[t]);
	    return token[t];
	}

	String remainder() {
	    return new String(chars, i, chars.length-i);
	}

	String remainder(int i) {
	    return new String(chars, i, chars.length-i);
	}
    }

    /**
     *
     * A test driver for the calculator.  Type in arithmetic expressions
     * or assignments and see the results.  Use "dump" to see contents of
     * all assigned variables.
     *
     * @param args                      required signature for
     *                                  <code>main</code> method, not used
     */
    public static void main(String[] args) {
	Properties p = new Properties();
	Calculator c = new Calculator(p);
	java.io.BufferedReader in = new java.io.BufferedReader(
				    new java.io.InputStreamReader(System.in));
	while (true) {
	    System.err.print(": ");
	    try {
		String line = in.readLine();
		if ("dump".equals(line)) {
		    p.list(System.out);
		    continue;
		}
		String value = c.getValue(line);
		if (value == null)
		    value = "0";
		System.out.println(value);
	    } catch (ArithmeticException e) {
		System.out.println(e.getMessage());
	    } catch (Exception e) {};
	}
    }

    private static final int END = -1;
    private static final int PLUS = 1;
    private static final int MINUS = 2;
    private static final int STAR = 3;
    private static final int SLASH = 4;
    private static final int LPAREN = 5;
    private static final int RPAREN = 6;
    private static final int NOT = 7;
    private static final int AND = 8;
    private static final int OR = 9;
    private static final int ASSIGN = 10;
    private static final int MOD = 11;
    private static final int EQ = 12;
    private static final int NE = 13;
    private static final int LT = 14;
    private static final int LE = 15;
    private static final int GT = 16;
    private static final int GE = 17;
    private static final int VAR = 20;
    private static final int NUM = 21;

    private static final String[] signs = {
	"", "+", "-", "*", "/", "(", ")", "!", "&&", "||",
	"=", "%", "==", "!=", "<", "<=", ">", ">="
    };
}
