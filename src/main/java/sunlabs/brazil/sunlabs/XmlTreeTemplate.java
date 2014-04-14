/*
 * XmlTreeTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2003-2006 Sun Microsystems, Inc.
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
 * Version:  1.3
 * Created by suhler on 03/10/23
 * Last modified by suhler on 06/06/15 12:50:47
 *
 * Version Histories:
 *
 * 1.3 06/06/15-12:50:47 (suhler)
 *   look for the "namespace" request property if the namespace isn't specified
 *
 * 1.2 04/03/22-15:40:40 (suhler)
 *   first pass at modifying trees
 *
 * 1.2 03/10/23-13:10:28 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/XmlTreeTemplate.java
 *
 * 1.1 03/10/23-13:10:27 (suhler)
 *   date and time created 03/10/23 13:10:27 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import sunlabs.brazil.server.FileHandler;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.util.Format;
import java.util.StringTokenizer;

// XXX This is preliminary and subject to change XXX
/**
 * Manage a tree representation of an XML document.
 * The xml document is manipulated via the "xmltree" tag.  the "content"
 * of the tree is retrieved by "flattening" the tree and accessing its contents
 * as name/value pairs.
 *
 * <pre>
 * Attributes:
 *   src:	the source file for this template, relative to the doc root
 *   eval:	process source file through subst()
 *   root:	where to lookup the template file
 *   xml:       The source xml, if "src" is not specified
 *		This may be used if the "xml" resides in a property
 *   name:	the name (and prefix) of the tree thingy
 *   delim:     which delimeter to use (defaults to ".")
 *   default:	what to use as the default "suffix" (defaults to "cdata")
 *   attribute: If not "", it specifies an attribute to use to name a 
 *              particular node.  If an attribute is specified, and multiple
 *              nodes have the same name, then all nodes in the tree may not
 *		be uniquly identifiable.
 *   dflt:      The value to use if attribute is specified, but the node doesn't
 *		have one (defaults to "unknown").
 *   namespace: which namespace to use.  If not specified,      
 *              the property [prefix].namespace is used, where [prefix]
 *              is RewriteContext.templatePrefix.  Otherwise the sessionID is
 *              used.
 *   tags:	The list of xml tags to be processed.  All tags not on this
 *		list are considered to be "singletons".  tags="" clears the
 *		list, and all tags will be processed.
 *
 * Portions of the tree are accessed using ordinary BSL variable, whos names
 * map to elements of the tree.  The variables have the form:
 * name.path.suffix, where:
 * - name is the name of the tree
 * - path is the path name of a particular node
 * - suffix indicates which aspect of the node is returned.  See below
 *   for the list of suffixes.
 *
 * The "." which separates the name, path, and suffix, as well as
 * the elements within the path may be changed to another character 
 * with the "delim" attribute.
 *
 * Nodes are identified by their path name, which is the "delim" separated
 * list of node names, starting from the root node.  The node names, which
 * identify a particular node in the tree, are configurable using
 * "attribute" attribute.  By default, a node is named by:
 *  tag(index), where "tag" is the xml tag, and "index" is the index of
 *  that tag witin its parent, starting from 0.
 *
 * The suffix may be one of:
 * - cdata		Returns the plain text between this tag and the next
 *			one.  Since closing tags (e.g. </foo>) are not
 *			considered nodes in the tree, any "cdata" after
 *			a closing tag is associated with the previous tag.
 * - tag		The name of the current tag
 * - index		The index of this tag within its parent.
 * - attributes		A space delimited list of attribute names associated
 *			with this node
 * - children		A space delimited list of direct child node names
 *			for this node.
 * - childCount		The number of direct child nodes
 * - [attribute].value	The value of [attribute], if it exists
 * - all		A space delimited list of all decendants of this
 *			node (including this node)
 * - glob		A space delimited list of all nodes whos names match
 *			the supplied glob pattern [broken]
 * Examples:
 * Consider the following XML snippet in the file called sample.xml:
 *
 *  &lt;sample name="main" a="1" b="2"&gt;
 *    &lt;part name="shoe" price="17"&gt;hard soles&lt;/part&gt;
 *    &lt;part name="hat" price="11"&gt;Green, with feathers&lt;/part&gt;
 *  &lt;/sample&gt;
 *  &lt;order name="joe"&gt;
 *    &lt;description&gt;
 *      This is a description of the order
 *    &lt;/description&gt;
 *    &lt;payment name="ante up" method="cash" amount="28" /&gt;
 *  &lt;/order&gt;
 *
 * The following tags should cause the indicated variables to "exist" with
 * the values shown:
 *  &lt;xmltree name=tree src=sample.xml&gt;
 *  tree.all= tree.sample(0) tree.sample(0).part(0) tree.sample(0).part(1) tree.order(0) tree.order(0).description(0)
tree.order(0).payment(0)
 *  tree.order(0).description(0).cdata = This is a description of the order 
 *  tree.order(0).description(0) = This is a description of the order 
 *    [the default suffix is "cdata"]
 *  tree.sample(0).children = part(0) part(1)
 *  sample(0).all = tree.sample(0) tree.sample(0).part(0) tree.sample(0).part(1)
 *  sample(0).part(0) = hard soles
 *  sample(0).part(0).attributes = name price
 *  sample(0).part(0).price.value = "17"
 *  sample(0).childCount = 2
 *
 *  &lt;xmltree name=tree delim=: attribute=name&gt;
 *   [ since src=sample.xml is already defined, it isn't required]
 *  all = tree:"main" tree:"main":"shoe" tree:"main":"hat" tree:"joe" tree:"joe":unknown tree:"joe":"ante up"
 *  "main":"shoe" = hard soles
 *  "main":"shoe":price:value) = "17"
 *
 *  &lt;xmltree name=tree delim=. attribute=name default=children&gt;
 *  sample(0) = part(0) part(1)
 *
 *  When combined with the BSLTemplate, the "foreach" tag may be used
 *  to iterate through all the node names, in which case the default
 *  value (e.g. cdata) is used.
 * 
 *  If the XML is invalid, the property "name".error is set to a hopefully
 *  useful error message
 * </pre>
 */

public class XmlTreeTemplate extends Template {

    public void
    tag_xmltree(RewriteContext hr) {
       String name=hr.get("name");		// the name of the tree
       String namespace= "XmlTreeTemplate:" +
	    hr.get("namespace", hr.request.props.getProperty(
	    hr.templatePrefix + "namespace",hr.sessionId));

       debug(hr);
       hr.killToken();

       if (name == null) {
	   debug(hr, "missing tree name");
	   return;
       }

       XmlTree tree = (XmlTree)
	       SessionManager.getSession(namespace, name, XmlTree.class);

       String src = hr.get("src");		// the src  file name
       if (src != null) {
	   String root = hr.get("root",
		   hr.request.props.getProperty("root", "."));
	   File file = new File(root + FileHandler.urlToPath(src));
	   System.out.println("Fetching: " + file);
	   try {
	       src = XmlTree.getFile(new FileInputStream(file));
	   } catch (IOException e) {
	       debug(hr, "Can't read file: " + e.getMessage());
	       hr.put(name + ".error", e.getMessage());
	   }
	   if (hr.isTrue("eval") && src != null) {
	       src = Format.subst(hr.request.props, src);
	   }
       } else {
	   src = hr.get("xml");
       }
       if (tree.size() == 0  && src == null) {
	   debug(hr, "new tree and no source document");
	   return;
       }
       tree.setPrefix(name);  // XX only on 1st try

       // set the path delimiter character

       String delim = hr.get("delim");
       if (delim != null) {
	   tree.setDelim(delim);
       }

       // set the default node value

       String dflt = hr.get("default");
       if (dflt != null) {
	   tree.setDflt(dflt);
       }

       // set the way items are named

       String att=hr.get("attribute");
       if (att != null && att.equals("")) {
           tree.setComparator(new XmlTree.DefaultNodeName());
       } else if (att != null) {
           tree.setComparator(new XmlTree.DefaultNodeName(att,
	       hr.get("dflt", "unknown")));
       }

       // set the list of tags to process

       String tags = hr.get("tags", null);
       if (tags != null) {
	   StringTokenizer st = new StringTokenizer(tags);
	   tree.setTags(null);
	   while (st.hasMoreTokens()) {
	       tree.setTag(st.nextToken());
	   }
       }

       if (src != null) {
	   try {
	       tree.replace(src);
	   } catch (IllegalArgumentException e) {
	       debug(hr, e.getMessage());
	       hr.put(name + ".error", e.getMessage());
	       return;
	   }
       }

       // Modify the tree ?

       String nodeName = hr.get("modify");
       if (nodeName != null) {
	   String cdata = hr.get("cdata");
	   if (cdata != null) {
		tree.setCdata(nodeName, cdata);
	   }
	   String key = hr.get("key");
	   if (key != null) {
	       tree.setAttribute(nodeName, key,   hr.get("value"));
	   }
       }

       if (!hr.isSingleton() && tree != null) {	
	   hr.request.addSharedProps(tree);
       }
   }
}
