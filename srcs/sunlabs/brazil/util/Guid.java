/*
 * Guid.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2002-2004 Sun Microsystems, Inc.
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
 * Created by suhler on 02/05/10
 * Last modified by suhler on 04/11/30 15:19:45
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:45 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:37:02 (suhler)
 *   version change
 *
 * 1.2 02/05/10-15:13:51 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 util/Guid.java
 *
 * 1.1 02/05/10-15:13:50 (suhler)
 *   date and time created 02/05/10 15:13:50 by suhler
 *
 */

package sunlabs.brazil.util;

import java.util.Random;

/**
 * Utility to generate GUID's (Globally Unique Identifiers).  We'll
 * fill in more methods as needed.
 *
 * @author      Stephen Uhler
 * @version		2.2
 */

public class Guid {
    private static Guid guidFactory = new Guid();
    static Random random = new Random();

    /**
     * Allow global replacement of the GUID generator.  Applications
     * wishing to install their own GUID generators should sub-class
     * Guid, override the getGuid() method, and use this method to
     *  install their generator.
     */

    public static void
    setGuidImpl(Guid factory) {
	guidFactory = factory;
    }

    /**
     * Return a GUID as a string.
     */

    public static String getString() {
	return guidFactory.getGuidString();
    }

    /**
     * Return a GUID as a string.  This is completely arbitrary, and
     * returns the hexification of a random value followed by a
     * timestamp.
     */

    protected String getGuidString() {
       long rand = (random.nextLong() & 0x7FFFFFFFFFFFFFFFL) |
      	 0x4000000000000000L;
       return Long.toString(rand, 32) +
	      Long.toString(System.currentTimeMillis()&0xFFFFFFFFFFFFFL, 32);
    }
}
