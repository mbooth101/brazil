/*
 * XmlTree.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2003-2007 Sun Microsystems, Inc.
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
 * Version:  1.13
 * Created by suhler on 03/10/20
 * Last modified by suhler on 07/01/14 15:13:03
 *
 * Version Histories:
 *
 * 1.13 07/01/14-15:13:03 (suhler)
 *   add getRoot() method to allow walking the tree programaticaaly[D
 *
 * 1.12 05/06/16-08:01:00 (suhler)
 *   Remove redundant string dequoter, and use the one in Format
 *
 * 1.11 04/12/30-12:39:17 (suhler)
 *   javadoc fixes.
 *
 * 1.10 04/04/02-10:06:40 (suhler)
 *   added better error checkin
 *
 * 1.9 04/03/22-15:09:41 (suhler)
 *   beginning of tree modification
 *
 * 1.8 03/11/12-13:09:43 (suhler)
 *   checkpoint
 *
 * 1.7 03/10/23-13:10:56 (suhler)
 *   checkpoint.  Basic flow works; need to get proper attr names
 *
 * 1.6 03/10/22-13:58:31 (suhler)
 *   checkpoint
 *
 * 1.5 03/10/22-10:26:33 (suhler)
 *   about to redo "Equals" to getName (somehow)
 *
 * 1.4 03/10/21-17:46:18 (suhler)
 *
 * 1.3 03/10/21-17:41:21 (suhler)
 *   checkpoint (search)
 *
 * 1.2 03/10/21-16:32:52 (suhler)
 *   checkpoint
 *
 * 1.2 03/10/20-15:32:44 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/XmlTree.java
 *
 * 1.1 03/10/20-15:32:43 (suhler)
 *   date and time created 03/10/20 15:32:43 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import sunlabs.brazil.session.PropertiesCacheManager;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.Glob;
import sunlabs.brazil.util.LexML;
import sunlabs.brazil.util.StringMap;

/**
 * Create a tree representation of an xml file whose parts may be
 * referenced as a dictionary.  This is currently "read only".
 */

public class
XmlTree extends Dictionary implements PropertiesCacheManager.Saveable {
    String ident = null;	// String identifying this tree
    Node root = null;		// the root of the tree
    int nodes = 0;		// the number of nodes
    Hashtable tags = null;	// only process these tags
    NodeName nodeName = new DefaultNodeName();  // use tag names for comparison
    String prefix = null;       // global key prefix
    String delim = ".";		// global level delimiter
    String dfltSuffix = "cdata"; // implied suffix

    // Table of suffixes for node names
    // "normal":   look up a node value
    // "special":  The name isn't really a node
    // "strip":    The name has another suffix to strip

    static Hashtable suffixes;
    static {
	suffixes = new Hashtable();
        suffixes.put("cdata","normal");
        suffixes.put("tag","normal");
        suffixes.put("index","normal");
        suffixes.put("attributes","normal");
        suffixes.put("children","normal");
        suffixes.put("childCount","normal");
        suffixes.put("xml","normal");
        suffixes.put("value","strip");
        suffixes.put("glob","special");
        suffixes.put("all","normal");
    }

    /**
     * Make an empty tree.
     */

    public XmlTree() {
    }

    /**
     * Given an XML string, build the tree.
     */

    public XmlTree(String src)  throws IllegalArgumentException {
	replace(src);
    }

    public void setIdent(String ident) {
	this.ident = ident;
    }

    public Node getRoot() {
	return root;
    }

    /**
     * Add an element to the tag process list.
     * Once a Process tag is defined, only tags defined are
     * processed.  All other tags are treated as singletons
     */

    public void setTag(String tag) {
	if (tags == null) {
	    tags = new Hashtable();
	}
	tags.put(tag.toLowerCase(), "");
    }

    /**
     * Set the list of tags to process
     */

    public void setTags(Hashtable tags) {
	this.tags = tags;
    }

    public Hashtable getTags() {
	return tags;
    }

    /**
     * set the name of this tree
     */

    public void setPrefix(String prefix) {
	this.prefix = prefix;
    }

    /**
     * set the node delimiter.
     */

    public void setDelim(String delim) {
	this.delim = (delim == null ? "." : delim);
    }

    /*
     * Set the default "suffix" to use if no suffix is supplied.
     */

    public boolean setDflt(String dflt) {
	if (dflt != null && suffixes.get(dflt) != null) {
	    dfltSuffix  = dflt;
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Set the class that determines a node's name.
     * This may be used to change the way nodes are named in an arbitrary
     * fashion.
     */

    public void setComparator(NodeName nodeName) {
	// System.out.println("Setting comparator to: " + nodeName);
	this.nodeName = nodeName;
    }
  
    /** Print a tree
     * @param node: The starting node
     * @param sb:   where to append the results to
     * @param level: the nesting level
     */

    public String toString() {
	if (root == null) {
	    return "(null)";
	}
	StringBuffer sb = new StringBuffer();
	toString(root, sb, 0);
	return sb.toString();
    }

    public void toString(Node node, StringBuffer sb, int level) {
	sb.append("\n");

	indent(sb, level);
	sb.append("<" + node.getTag());

	// do the attributes

	StringMap attrs = node.getAttributes();
	if (attrs != null) {
	    for(int i = 0; i< attrs.size(); i++) {
	        String key = attrs.getKey(i);
	        String value = fix(attrs.get(i));
	        sb.append(" ").append(key).append("=").append(value);
	    }
	}
	if (node.isSingle()) {
	    sb.append (" />");
	} else {
	    sb.append(">");
	}


	// do the "other" text

	String cdata = node.getCdata();
	if (cdata != null) {
	    sb.append("\n");
	    indent(sb, level+1);
	    sb.append(cdata);
	}

	if (!node.isSingle()) {
	    int n = node.childCount();
	    for (int i=0; i<n; i++) {
		toString(node.getChild(i), sb, level+1);
	    }
	    sb.append("\n");
	    indent(sb, level);
	    sb.append("</" + node.getTag() + ">");
	}
    }

    /** 
     * Quote an attribute properly.
     * This doesn't distinguish FOO="" from FOO
     */

    static String fix(String s) {
	if (s.startsWith("\"") || s.startsWith("\'")) {
	    return s;
	} else if (s.indexOf("\"") < 0) {
	    return "\"" + s + "\"";
	} else {
	    return "\'" + s + "\'";
	}
    }

    // insert some spaces

    static void indent(StringBuffer sb, int level) {
	for(int i=0; i<level; i++) {
	    sb.append("  ");
	}
    }

    /**
     * Replace the XmlTree with new markup.
     * @param src:	the xml data
     */

    public void replace(String src)  throws IllegalXmlException {
	nodes = 0;
	root = new Node("ROOT", false, null, null, Node.ROOT, 0);// dummy root
        Stack stack = new Stack();		// parent stack
	stack.push(new StackInfo(root, 0));
	Node parent = root;
	LexML lex = new LexML(src);
	Node current = root;
	IllegalXmlException ex = null;

	while(lex.nextToken()) {
	    switch (lex.getType()) {
	    case LexML.COMMENT:
		// System.out.println("Got comment: " + lex.getBody());
		break;
	    case LexML.STRING:
		current.appendCdata(lex.getBody());
		break;
	    case LexML.TAG:
		String name = lex.getTag().toLowerCase();
		if (name.startsWith("/")) {	// pop stack if proper nesting
		    name = name.substring(1);
		    if (tags != null && !tags.containsKey(name)) {
			// System.out.println("Skipping /" + name);
			continue;
		    }

		    // parse error, what should we do?

		    if (!name.equals(parent.getTag())) {
			int sl = line(lex.getString(),
				((StackInfo)stack.peek()).position);
			ex = IllegalXmlException.getEx(ex,  ident, sl, 
			    parent.getTag(), line(lex), "</" + name + ">");

			/*
			 * if matching tag is on the stack, pop until we
			 * get there.  Otherwise ignore the closing tag
			 */

			for (int i=stack.size()-2;i>0;i--) {
			    Node node=((StackInfo)stack.elementAt(i)).parent;
			    String tag = node.getTag();
			    if (tag.equals(name)) {
				while (++i <= stack.size()) {
				    /*
				    System.out.println("popping " +
					    stack.peek());
				    */
				    stack.pop();
				}
			        stack.pop();
			        current=parent=((StackInfo)stack.peek()).parent;
			        break;
			    }
			}
			continue; // ignore it?
		    } else {
		        stack.pop();
			current = parent = ((StackInfo) stack.peek()).parent;
		    }
		} else {
		    boolean single = lex.isSingleton();
		    if (!single && tags != null && !tags.containsKey(name)) {
			// System.out.println(name + ": setting to single");
			single=true;
		    }
		    int count = ((StackInfo) stack.peek()).getCount(name);
                    Node n = new Node(name, single,
			    lex.getAttributes(), parent, Node.TAG, count);
		    current = n;
		    nodes++;
	            parent.addChild(n);
		    if (!single) {
		        stack.push(new StackInfo(n, lex.getLocation()));
		        parent = n;
		    }
		}
		break;
	    default:
		System.out.println("Oops, invalid type!");
		break;
	    }
	}
	/*
	 * if we still have stuff on the stack, add an error for each tag
	 */

	stack.pop();
	while (stack.size() > 2) {
	    stack.pop();
	    parent = ((StackInfo) stack.peek()).parent;
	    int sl = line(lex.getString(), ((StackInfo)stack.peek()).position);
	    ex = IllegalXmlException.getEx(ex,  ident, sl, parent.getTag(),
		    1+line(lex.getString(), lex.getLocation()), "eof");
	}
	if (ex != null) {
	    throw ex;
	}
    }

    // These methods can be used to modify the tree.
    // this is just a start.

    public boolean setAttribute(String name, String key, String value) {
	Node n = search(name);
	if (n != null) {
	    n.putAttribute(key, value);
	    return true;
	} else {
	    return false;
	}
    }

    public boolean setCdata(String name, String data) {
	Node n = search(name);
	if (n != null) {
	    n.setCdata(data);
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Do some more reasonable error handling.  We'll stuff more info
     * into the exception.
     *
     */

    public static class IllegalXmlException extends IllegalArgumentException {
	public Vector errors = null;	// accumulate errors in xml file

	public IllegalXmlException(String s) {
	    super(s);
	}

	public int addError(int sl, String st, int cl, String ct) {
	    if (errors == null) {
		errors = new Vector();
	    }
	    errors.add(new XmlErrorInfo(sl, st, cl, ct));
	    return errors.size();
	}

	// convenience initializer

	public static
	IllegalXmlException getEx(IllegalXmlException e,  String s,
		int sl, String st, int cl, String ct) {
	    if (e == null) {
		e = new IllegalXmlException(s);
	    }
	    e.addError(sl, st, cl, ct);
	    return e;
	}

	public String toString() {
	    String result = "" + errors.size() + " error(s) processing: " + 
		getMessage();
	    for (int i=0;i<errors.size();i++) {
		XmlErrorInfo xi = (XmlErrorInfo) errors.elementAt(i);
		result += "\n" + (i+1) + ")\t" +
		    "found \"" + xi.currentTag + "\" at line " +
		    xi.currentLine +
		    ", need match for \"" + xi.startTag + "\" at line " +
		    xi.startLine;
	    }
	    return result;
	}
    }

    public static class XmlErrorInfo {
	public int startLine;		// starting line # for error
	public String startTag;	// starting tag
	public int currentLine;	// line generating error (-1 for EOF)
	public String currentTag;	// tag error discovered on

	public XmlErrorInfo(int startLine, String startTag,
			int currentLine, String currentTag) {
	    this.startLine = startLine;
	    this.startTag = startTag;
	    this.currentLine = currentLine;
	    this.currentTag = currentTag;
	}
    }

    /*
     * Provide a mechanism for comparing nodes to string names so we
     * can control the mapping of names to nodes arbitrarily
     */

    public interface NodeName {
	public String getName(Node n);
    }

    /**
     * The node is named by the specified attribute.  If no attribute
     * is specified, use the tag name instead, followed by the index in ().
     */

    public static class DefaultNodeName implements NodeName {
	String attribute;	// which attribute to use as a name
	String dflt;		// The no-name default

	public DefaultNodeName() {
	    this(null, null);
	}

	public DefaultNodeName(String attribute, String dflt) {
	    this.attribute = attribute;
	    this.dflt = (dflt == null ? "" : dflt);
	}

	public String getName(Node n) {
	    if (n == null) {
		return null;
	    } else if (attribute == null) {
		return n.getTag() + "(" + n.getIndex() + ")";
	    } else {
		String name = n.getAttribute(attribute);
		return name==null ? dflt : Format.deQuote(name);
	    }
	}

	public String toString() {
	    return attribute + "(" + dflt + ")";
	}
    }

    /**
     * Find a node in the tree by name, starting at the root.
     * @param s		The node pathname
     * @return		The node, if found, or null
     */

    public Node search(String s) {
	StringTokenizer st = new StringTokenizer(s, delim);
	if (prefix != null && !prefix.equals(st.nextToken())) {
	    return null;
	}
	// System.out.println("Searching for: " + s + " [" + prefix + delim);
	return search(root, st);
    }

    /**
     * Find a node in the tree by name, starting under any node.
     */

    public Node
    search(Node node, StringTokenizer st) {
	if (st.hasMoreTokens()) {
	    String name = st.nextToken();
            // System.out.println("Looking for: " + name + " in: " + node);
            for(int i = 0; i<node.childCount(); i++) {
		Node child = node.getChild(i);
		if (name.equals(nodeName.getName(child))) {
		    return search(child, st);
		}
		// System.out.println("  skipping: " + child);
	    }
	    // System.out.println("   NOPE");
	    return null;
	} else {
	    // System.out.println("Got: " + node);
	    return node;
	}
    }

    /**
     * Find all nodes that match a glob pattern, starting at the root.
     */

    public Vector match(String pattern) {
	StringTokenizer st = new StringTokenizer(pattern, delim);
	if (prefix != null && !prefix.equals(st.nextToken())) {
	    return null;
	}
	Vector results = new Vector();
	match(root, st, results);
	return results;
    }

    /**
     * Find all nodes that match a glob pattern, starting at any node.
     */

    public void
    match(Node node, StringTokenizer st, Vector results) {
	if (st.hasMoreTokens()) {
	    String pattern = st.nextToken();
            for(int i = 0; i<node.childCount(); i++) {
		Node child = node.getChild(i);
		String name = nodeName.getName(child);
		if (Glob.match(pattern, name)) {
		    // System.out.println(" YES");
		    match(child, st, results);
		} else {
		    // System.out.println(" NO");
		}
	    }
	} else {
	    // System.out.println("Appending: " + node);
	    results.addElement(node);
	}
    }

    // Dictionary methods

    public Enumeration elements() {
	Vector v = new Vector();
	elements(root, v);
	// System.out.println("Tree Returning elements");
	return v.elements();
    }

    // elements helper

    public static void elements(Node n, Vector v) {
	if (n.type != Node.ROOT) {
	    v.addElement(n);
	}
	for(int i = 0; i<n.childCount(); i++) {
	    elements(n.getChild(i), v);
	}
    }

    public Enumeration
    keys() {
	Vector v = new Vector();
	keys(root, prefix, delim, v);
	// System.out.println("Tree Returning keys: " + v);
	return v.elements();
    }

    // keys helper

    public void keys(Node n, String prefix, String delim, Vector v) {
	if (n.type != Node.ROOT) {
	    String name = nodeName.getName(n);
	    if (prefix == null) {
	        prefix = name;
	    } else {
	        prefix += delim + name;
	    }
	    v.addElement(prefix);
	}
	for(int i = 0; i<n.childCount(); i++) {
	    keys(n.getChild(i), prefix, delim, v);
	}
    }

    /**
     * Given a node description, return the value, if any.
     * Descriptions are of the form:
     *   [prefix].name.[suffix]
     * where :
     *  [prefix] is the name of the tree
     *  "." is the current delimiter,
     *  name is the path name of a node in the tree
     *  [suffix] specifies which part of the node to return as a string.
     *  See getpart() for the list of valid suffixes.
     *
     */

    public Object get(Object k) {
	if (!(k instanceof String) || !((String)k).startsWith(prefix)) {
	    return null;
	}

        return getPart((String) k);
    }

    /**
     * Given a node descriptor, return the result. XXX not done
     *
     * modifiers:
     *  cdata:		return cdata
     *  tag:		the name
     *  index:		which tag within whis parent
     *  attributes:	the list of attribute names
     *  children:	the list of children
     *  childCount:	the number of children
     *  <attribute>.value	the value for attribute <attribute>
     *  glob		nodes matching the glob pattern
     *  all		all nodes under this one
     */

    public String getPart(String s) {
	// System.out.println("GET " + s);
	int i = s.lastIndexOf(delim);
	if (i < 0) {
	     return null;
	}
	String result = null;
	String suffix = s.substring(i+1);
	String root = s.substring(0, i);
	Node node=null;   // The node to process (if any)

	String type = (String) suffixes.get(suffix);

	if (type == null) {	// use default name
	    type = (String) suffixes.get(dfltSuffix);
	    suffix = dfltSuffix;
	    root = s;
	}

	// normal node properties: fetch the node

	if (type.equals("normal")) {
	    node = search(root);
	    // System.out.println("Normal node: " + root + " ("  + suffix + ") " + node);

	    if (node == null) {
		// System.out.println("  No node -> null");
		return null;
	    }
	}

	// normal node properties

	if (suffix.equals("cdata")) {
	    result =  node.getCdata();
	    if (result == null) {
		result = "";
	    }
	} else if (suffix.equals("tag")) {
	    result =  node.getTag();
	} else if (suffix.equals("index")) {
	    result =  "" + node.getIndex();
	} else if (suffix.equals("attributes")) { // attribute list
	    StringMap attributes = node.getAttributes();
	    StringBuffer sb = new StringBuffer();
	    for (i = 0; i<attributes.size();i++) {
		sb.append(attributes.getKey(i)).append(" ");
	    }
	    result =  sb.toString();
	} else if (suffix.equals("children")) {	// child names
	    int count = node.childCount();
	    if (count == 0) {
		result =  "";
	    } else {
		StringBuffer sb = new StringBuffer();
		for(i=0;i<node.childCount();i++) {
		    Node child = node.getChild(i);
		    sb.append(nodeName.getName(child)).append(" ");
		}
		result= sb.toString();
	    }
	} else if (suffix.equals("xml")) {	// return markup
	    StringBuffer sb = new StringBuffer();
	    toString(node, sb, 0);
	    result = sb.toString();
	} else if (suffix.equals("childCount")) {
	    result= "" + node.childCount();
	} else if (suffix.equals("all")) {
	    Vector v = new Vector();
	    keys(node, prefix, delim, v);
	    StringBuffer sb = new StringBuffer();
	    for(i=0;i<v.size();i++) {
		sb.append(v.elementAt(i)).append(" ");
	    }
	    result= sb.toString();
        } else if (suffix.equals("value")) {	// attribute value
	    i = root.lastIndexOf(delim);
	    if (i < 0) {
	        result = null;
	    } else {
	        String attribute = root.substring(i+1);
		root = root.substring(0, i);
	        node = search(root);
		if (node != null) {
		    result = node.getAttribute(attribute);
		} else {
		    result = null;
		}
	    }
	} else if (suffix.equals("glob")) {
	    Vector v = match(root);
	    StringBuffer sb = new StringBuffer();
	    for(i=0;i<v.size();i++) {
		sb.append(nodeName.getName((Node)v.elementAt(i))).append(" ");
	    }
	} else {
	    System.out.println("Suffix unimplemented: " + suffix);
	}
	return result;
    }

    // read only for now

    public Object put(Object k, Object v) {
	System.out.println("Tree ignoring put of: " + k + "=" + v);
	return null;
    }

    // read only for now

    public Object
    remove(Object o) {
	return null;
    }

    public int
    size() {
	return nodes;
    }

    // Saveable

    public boolean
    isEmpty() {
	return nodes == 0;
    }

    // end of dictionary stuff

    public void
    load(InputStream in)  throws IOException {
	replace(getFile(in));
    }

    public void
    save(OutputStream out, String header)  throws IOException {
	out.write(("<!-- " + header + "-->\n" + this).getBytes());
    }

    // end of saveable

    // Misc internal functions

    /**
     * Find the line at the current processing position.
     */

    static int line(LexML lex) {
	return line(lex.getString(), lex.getLocation());
    }

    static int line(String str, int stop) {
	int line = 1;		// current line;
	int index = 0;		// where to start
	while ((index = str.indexOf('\n', index)) > 0 &&
			index < stop) {
	    line++;
	    index++;
	}
	return line;
    }


    public static void
    main(String[] args) {
	XmlTree x = new XmlTree();
	x.setIdent(args[0]);
	for(int i = 1; i<args.length;i++) {
	    // System.out.println("Setting tag: " + args[i]);
            x.setTag(args[i]);
	}
	try {
	    x.replace(getFile(args[0]));
	    System.out.println(args[0] + " OK");
	} catch (IllegalXmlException e) {
	    System.out.println(e.toString());
	} catch (IOException e) {
	    System.out.println("" + e);
	}
    }

    // read a file name into a string

    public static String getFile(String s) throws IOException {
	FileInputStream in = new FileInputStream(s);
	return getFile(in);
    }

    // read a stream into a string

    public static String getFile(InputStream in) throws IOException {
	byte[] b = new byte[in.available()];
	in.read(b);
	String data = new String(b);
	in.close();
	return data;
    }

    /**
     * Info to keep on stack.  We keep the char position with the
     * node so we can generate more useful error messages for invalid
     * documents.
     */

    static class StackInfo {
	public Node parent;		// parent node
	public int position;		// char position in src
	public Hashtable names;		// existing child names, for node index

	public StackInfo(Node parent, int position) {
	    this.parent = parent;
	    this.position = position;
	    this.names = null;
	}

	// count siblings by name

	public int getCount(String name) {
	    int result = 0;
	    if (names == null) {
		names = new Hashtable();
	    }

	    Incr incr = (Incr) names.get(name);
	    if (incr == null) {
		names.put(name, new Incr());
	    } else {
		result = incr.incr();
	    }
	    // System.out.println("X "  + result + ": " + name +  " " + names);
	    return result;
	}

	public String toString() {
	    return "Stack: " + parent + " children(" + names + ")";
	}
    }

    // counter class for hashtable values

    static class Incr {
	int count;

	public Incr() {
	    count = 0;
	}

	public int incr() {
	    count++;
	    return count;
	}

	public String toString() {
	  return "" + count;
        }
    }

    /**
     * This describes a node of the XML tree
     */

    public static class Node {

	// node types
	static final int ROOT=1;
	static final int TAG=2;
	static final int COMMENT=3;

	String tag;		// the tag name
	int type;		// tag type
	int index;		// index (from 0) of sibling with same name
	boolean singleton;	// is singleton
	StringMap attributes;	// the tag attributes
	String cdata;		// the text 'till the next <

	Node parent;		// my parent node
	Vector children;	// my child nodes

        public Node(String tag, boolean singleton, 
		StringMap attributes, Node parent, int type, int index) {
	    this.tag=tag;
	    this.singleton=singleton;
	    this.type = type;
	    this.index = index;
	    this.attributes = attributes;
	    this.cdata = null;
	    this.parent = parent;
	    this.children = null;
        }

	// call this to modify the cdata

	public void setCdata(String s) {
	    cdata = s;
	}

	// call this to muck with the attributes

	public void putAttribute(String key, String value) {
	    if (value != null) {
		attributes.put(key, value);
	    } else {
		attributes.remove(key);
	    }
	}
    
        public void appendCdata(String s) {
	    s = s.trim();
	    if (s.length() > 0) {
		cdata = cdata == null ? s : cdata + " " + s;
	    }
        }
    
        public void addChild(Node child) {
    	if (children==null) {
	       children = new Vector();
	    }
	    children.addElement(child);
        }

	public Node getChild(int i) {
	    return (Node) children.elementAt(i);
	}

        public String getTag() {
	    return tag;
        }

	public int getIndex() {
	    return index;
	}

	public boolean isSingle() {
	    return singleton;
	}

	public void setSingle(boolean s) {
	    singleton = s;
	}

        public StringMap getAttributes() {
	    return attributes;
        }

        public String getAttribute(String name) {
	    return attributes==null ? null : attributes.get(name);
        }

        public String getCdata() {
	    return cdata;
        }

	public Node getParent() {
	    return parent;
	}

	public int childCount() {
	    return children==null ? 0 : children.size();
	}

        public String toString() {
           return  "Node: " + tag + "[" + index + "] (attr=" + attributes +
	       ") " + cdata;
        }
    }
}
