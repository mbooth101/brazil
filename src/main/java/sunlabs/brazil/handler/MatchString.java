/*
 * MatchString.java
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
 * The Initial Developer of the Original Code is: suhler.
 * Portions created by suhler are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): suhler.
 *
 * Version:  1.1
 * Created by suhler on 06/11/20
 * Last modified by suhler on 06/11/20 17:43:45
 *
 * Version Histories:
 *
 * 1.2 06/11/20-17:43:46 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/MatchString.java
 *
 * 1.1 06/11/20-17:43:45 (suhler)
 *   date and time created 06/11/20 17:43:45 by suhler
 *
 */

package sunlabs.brazil.handler;

import java.util.Properties;

/**
 * Deprecated - do not use.
 * This class has moved to the sunlabs.brazil.handler package.
 * It is here for backward compatibility only.
 * @deprecated
 */

public class MatchString extends sunlabs.brazil.util.MatchString {
    public MatchString(String propsPrefix) {
	super(propsPrefix);
    }
    public MatchString(String propsPrefix, Properties props) {
	super(propsPrefix, props);
    }
}
