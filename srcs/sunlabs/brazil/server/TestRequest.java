/*
 * TestRequest.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.
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
 * Created by cstevens on 01/03/12
 * Last modified by suhler on 02/10/01 16:37:39
 *
 * Version Histories:
 *
 * 2.1 02/10/01-16:37:39 (suhler)
 *   version change
 *
 * 1.2 02/07/18-15:05:19 (suhler)
 *   remove import *
 *
 * 1.2 01/03/12-17:39:31 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 tests/TestRequest.java
 *
 * 1.1 01/03/12-17:39:30 (cstevens)
 *   date and time created 01/03/12 17:39:30 by cstevens
 *
 */

package sunlabs.brazil.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.net.Socket;

/**
 * Version of Request for testing purposes.  Arranges for
 * all output to be captured in one place so the ouput of a test
 * may be compared with the expected output.
 */

public class TestRequest
    extends Request
{
    ByteArrayOutputStream log = new ByteArrayOutputStream();

    public
    TestRequest(Server server, String request)
	throws IOException
    {
	super(server, new TestSocket(request));
	getRequest();
	log.reset();
    }
	
    public Object
    put(String key, String value)
    {
	return props.put(key, value);
    }

    public String
    log()
    {
	return log.toString();
    }

    public String
    result()
    {
	return ((TestSocket) sock).out.toString();
    }

    public void
    log(int level, Object obj, String message)
    {
	try {
	    if (obj != null) {
		log.write(obj.toString().getBytes());
		log.write(':');
		log.write(' ');
	    }
	    if (message != null) {
		log.write(message.getBytes());
	    }
	    log.write('\n');
	} catch (IOException e) {}
    }

    private static class TestSocket extends Socket
    {
	InputStream in;
	ByteArrayOutputStream out;
	
	public
	TestSocket(String request)
	{
	    in = new StringBufferInputStream(request);
	    out = new ByteArrayOutputStream();
	}

	public void
	close()
	    throws IOException
	{
	    in.close();
	    out.close();
	}

	public InputStream
	getInputStream()
	{
	    return in;
	}

	public OutputStream
	getOutputStream()
	{
	    return out;
	}
    }
}

    
    
