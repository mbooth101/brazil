/*
 * AllowGtTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2002-2003 Sun Microsystems, Inc.
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
 * Version:  2.2
 * Created by suhler on 02/02/04
 * Last modified by suhler on 03/08/01 16:18:54
 *
 * Version Histories:
 *
 * 2.2 03/08/01-16:18:54 (suhler)
 *   fixes for javadoc
 *
 * 2.1 02/10/01-16:36:53 (suhler)
 *   version change
 *
 * 1.2 02/04/25-13:44:16 (suhler)
 *   doc fixes
 *
 * 1.2 02/02/04-14:31:16 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/AllowGtTemplate.java
 *
 * 1.1 02/02/04-14:31:15 (suhler)
 *   date and time created 02/02/04 14:31:15 by suhler
 *
 */

package sunlabs.brazil.template;

import sunlabs.brazil.util.LexHTML;

/**
 * Template that changes the behavior of the HTML/XML parser
 * to allow unescaped &gt;'s inside of entity attribute values.
 * On the down side, it doesn't deal with unmatched "'s gracefully
 * <p>
 * By default, the LexML parser does not allow a &gt; inside of an
 * entity body.  The parser allows applications to provide ways of allowing
 * embedded &gt;'s, based on whatever syntax they like,  This example
 * will ignore a &gt; (as the end of the entity) if inside a
 * quoted attribute value.
 * <p>
 * No new markup is supported; the behavior of the HTML/XML parsing
 * is altered for the duration of the page.  This template is useful
 * primarily to demonstrate how to change the parser token processing.
 *
 * @author Stephen Uhler
 * @version %W
 */


public class AllowGtTemplate extends Template {

    /**
     * Replace the "default" parser with our modified one.
     */
    public boolean
    init(RewriteContext hr) {
	hr.lex = new AllowGtLex(hr.lex.rest());
	return true;
    }

    /**
     * Variant of the LexHTML parser that alters the &gt; search.
     */

    class AllowGtLex extends LexHTML {
       AllowGtLex(String str) {
	   super(str);
       }

       /**
        * skip over &gt;'s if they are inside of quoted attribute values.
        */

       protected int
       findClose(int start) {
	   return findClose(start, 0);
       }
     
       private int
       findClose(int start, int quotes) {
	   int close = super.findClose(start);
	   if (close < 0) {
	      return close;
	   } else {
	      int iq = start;
	      while ((iq = str.indexOf('"', iq)) < close && iq >=0) {
	         quotes++;
	         iq++;
	      }
	      if ((quotes&1) == 0 || iq < 0) {
	         return close;
	      } else {
	         return findClose(close+1, quotes);
	      }
          }
       }
   }
}
