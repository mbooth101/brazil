/*
 * TclTemplateChannel.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2004 Sun Microsystems, Inc.
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
 * Created by cstevens on 99/10/19
 * Last modified by suhler on 04/11/30 15:19:44
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:44 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:37:25 (suhler)
 *   version change
 *
 * 1.4 00/05/31-13:52:17 (suhler)
 *   name change
 *
 * 1.3 99/10/21-18:27:19 (cstevens)
 *   Merged changes between child workspace "/home/cstevens/ws/brazil/naws" and
 *   parent workspace "/net/mack/export/ws/brazil/naws".
 *
 * 1.1.1.1 99/10/21-18:21:52 (cstevens)
 *   jImport
 *
 * 1.2 99/10/20-15:08:42 (suhler)
 *   fixed imports
 *
 * 1.2 99/10/19-19:00:57 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 tcl/TclTemplateChannel.java
 *   Name history : 1 0 tclHandlers/TclTemplateChannel.java
 *
 * 1.1 99/10/19-19:00:56 (cstevens)
 *   date and time created 99/10/19 19:00:56 by cstevens
 *
 */

package tcl.lang;

import sunlabs.brazil.template.RewriteContext;

import java.io.IOException;

/**
 * This class is used to replace the stdout for the Tcl Interp.
 * Everything written to stdout will instead be appended to the HTML
 * document.
 * <p>
 * This class is in the <code>tcl.lang</code> package because
 * <code>tcl.lang.Channel</code> and <code>tcl.lang.TclIO</code>
 * aren't public but we need to access those classes to define new
 * channels.
 *
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version		2.2
 */
public class TclTemplateChannel
    extends Channel
{
    RewriteContext hr;
    Interp interp;

    /**
     * Creates a replacement for stdout in this interp.
     * <p>
     * For each call to this procedure, there should eventually be a call
     * to <code>unregister</code> to restore the original stdout for this
     * interp and to release the resources associated with the given
     * <code>RewriteContext</code>.
     *
     * @param	interp
     *		The interp in which to replace stdout.
     *
     * @param	hr
     *		The RewriteContext to which all data written to stdout will
     *		be appended.
     */
    public
    TclTemplateChannel(Interp interp, RewriteContext hr) 
    {
	this.interp = interp;
	this.hr = hr;

	/*
	 * Unlike the standard C implementation of Tcl, you cannot actually
	 * close "stdout" and then open another channel which will become the
	 * new "stdout", because in JACL registering a channel does not
	 * automatically take over the next available closed standard stream.
	 * However, if you register a new channel called "file1", it does
	 * replace the standard "stdout".
	 */

	setChanName("file1");
	TclIO.registerChannel(interp, this);
    }

    /**
     * Removes this channel from the interp.  After calling this, the
     * original stdout for this interp will be restored.
     */
    public void
    unregister()
    {
	TclIO.registerChannel(interp, TclIO.getStdChannel(StdChannel.STDOUT));
    }

    public String
    read(Interp interp, int type, int numBytes)
	throws IOException, TclException
    {
	throw new TclException(interp, "channel \""
		+ getChanName() + "\" wasn't opened for reading");
    }

    public void
    write(Interp interp, String s) 
	throws IOException, TclException
    {
	hr.append(s);
    }

    public void close() throws IOException
    {
    }
	    
    public void flush(Interp interp) throws IOException, TclException
    {
    }

    public void seek(long offset, int mode) throws IOException
    {
    }
    
    public long tell() throws IOException
    {
        return((long)-1);
    }

    public boolean eof() {
	return true;
    }
}
