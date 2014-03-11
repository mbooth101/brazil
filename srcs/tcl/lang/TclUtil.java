/*
 * TclUtil.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2004 Sun Microsystems, Inc.
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
 * Version:  2.2
 * Created by cstevens on 00/04/24
 * Last modified by suhler on 04/11/30 15:19:44
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:44 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:37:26 (suhler)
 *   version change
 *
 * 1.2 00/04/24-13:04:06 (cstevens)
 *   doc
 *
 * 1.2 00/04/24-11:35:23 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 tcl/TclUtil.java
 *
 * 1.1 00/04/24-11:35:22 (cstevens)
 *   date and time created 00/04/24 11:35:22 by cstevens
 *
 */

package tcl.lang;

/**
 * The <code>TclUtil</code> class "corrects" oversights in the
 * <code>tcl.lang</code> package by making public certain methods that
 * were not public.
 *
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version		2.2
 */

public class TclUtil
{
    private TclUtil() {}
    
    public static void
    setVar(Interp i, String name, String value, int flags)
	throws TclException
    {
	i.setVar(name, value, flags);
    }

    public static void
    setVar(Interp i, String name, TclObject value, int flags)
	throws TclException
    {
	i.setVar(name, value, flags);
    }
}
