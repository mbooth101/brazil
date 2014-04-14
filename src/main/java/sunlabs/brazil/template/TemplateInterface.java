/*
 * TemplateInterface.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2003 Sun Microsystems, Inc.
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
 * Created by suhler on 01/05/11
 * Last modified by suhler on 03/08/01 16:18:50
 *
 * Version Histories:
 *
 * 2.2 03/08/01-16:18:50 (suhler)
 *   fixes for javadoc
 *
 * 2.1 02/10/01-16:36:51 (suhler)
 *   version change
 *
 * 1.2 02/05/01-11:28:47 (suhler)
 *   fix sccs version info
 *
 * 1.2 01/05/11-11:46:28 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 handlers/templates/TemplateInterface.java
 *
 * 1.1 01/05/11-11:46:27 (suhler)
 *   date and time created 01/05/11 11:46:27 by suhler
 *
 */

package sunlabs.brazil.template;

/**
 * Interface for templates.  Most templates are expected to extend
 * the Template class.  This interface is for those cases where
 * a template must extend something else.
 *
 * @author Stephen Uhler
 * @version %W
 */

public interface TemplateInterface {

    /**
     * Called before this template processes any tags.
     */
    public boolean init(RewriteContext hr);

    /**
     * Called after all tags have been processed, one final chance.
     */
    public boolean done(RewriteContext hr);
}
