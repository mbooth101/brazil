/*
 * TestInputStream.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.
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
 * Version:  2.1
 * Created by cstevens on 99/11/09
 * Last modified by suhler on 02/10/01 16:37:35
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:37:35 (suhler)
 *   version change
 *
 * 1.2 00/04/24-13:05:12 (cstevens)
 *   wildcard imports
 *
 * 1.2 99/11/09-20:22:05 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 tests/TestInputStream.java
 *
 * 1.1 99/11/09-20:22:04 (cstevens)
 *   date and time created 99/11/09 20:22:04 by cstevens
 *
 */

package tests;

import java.io.InputStream;

public class TestInputStream
    extends InputStream
{
    int length;
    
    public
    TestInputStream(int length)
    {
	this.length = length;
    }

    public int
    read()
    {
	if (length <= 0) {
	    return -1;
	} else {
	    length--;
	    return 'A';
	}
    }
}
