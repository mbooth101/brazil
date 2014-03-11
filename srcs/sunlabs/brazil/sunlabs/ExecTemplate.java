/*
 * ExecTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2005 Sun Microsystems, Inc.
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
 * Version:  2.6
 * Created by suhler on 01/05/21
 * Last modified by suhler on 05/05/19 13:05:09
 *
 * Version Histories:
 *
 * 2.6 05/05/19-13:05:09 (suhler)
 *   fix prepend processing (e.g. prepend="" works)
 *
 * 2.5 05/02/04-11:46:54 (suhler)
 *   added better diagnostics
 *
 * 2.4 05/01/26-22:48:27 (suhler)
 *   don't leave <exec> in
 *
 * 2.3 04/11/30-15:19:43 (suhler)
 *   fixed sccs version string
 *
 * 2.2 02/12/19-11:39:34 (suhler)
 *   add "usesh" flag
 *
 * 2.1 02/10/01-16:37:31 (suhler)
 *   version change
 *
 * 1.3 02/07/10-11:26:33 (suhler)
 *   add encoding option
 *
 * 1.2 01/07/16-16:52:21 (suhler)
 *   use new Template convenience methods
 *
 * 1.2 01/05/21-16:42:51 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sunlabs/ExecTemplate.java
 *
 * 1.1 01/05/21-16:42:50 (suhler)
 *   date and time created 01/05/21 16:42:50 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.util.http.HttpInputStream;

/**
 * template to exec a program, and return its arguments into request
 * properties .
 * <p>This template processes the <code>&lt;exec ...&gt;</code> tag.
 * The following attributes are supported. ${...} substitutions are
 * preformed before the command is run.
 * <dl class=attrib>
 * <dt>command
 * <dd>
 * The command to run.  The environment (and path) are inherited
 * from the server.  This is a required parameter.
 * <dt>usesh
 * <dd>There is a bug in "exec" that prevents passing arguments to a
 * command with embedded whitespace.  If this flag is present, then
 * the command "/bin/sh -c [command]" is run.  This only works on systems
 * where "/bin/sh" may be executed.
 * <dt>prepend
 * <dd>The name prepended to the properties produced by this tag
 * <dt>stdin
 * <dd>The standard input to send to the command (if any)
 * <dt>encoding
 * <dd>The character set encoding to use when converting the stdout
 * and stderr properties.  If no encoding attribute is present, the
 * encoding property is used instead.  Defaults to the default encoding.
 * </dl>
 * The following request properties are set as a side effect:
 * <dl class=props>
 * <dt>stdout
 * <dd>The standard output produced by the program, converted to a String
 * using the default encoding.
 * <dt>stderr
 * <dd>The standard error output produced by the program, converted to
 * a String using the default encoding.
 * <dt>code
 * <dd>The exit code for the program.
 * <dt>error
 * <dd>The error message, if something went wrong.
 * </dl>
 * <p>
 * Currently, there is no way to set the environment or current
 * directory for the program.
 *
 * @author      Stephen Uhler
 * @version		2.6
 */

public class
ExecTemplate extends Template {

    public void tag_exec(RewriteContext hr) {
        debug(hr);
	hr.killToken();
	boolean usesh = hr.isTrue("usesh");
	String command = hr.get("command");
	if (command == null) {
	    debug(hr, "Missing command attribute");
	    return;
	}
	String prepend = hr.get("prepend");	// property names prefix
	String encoding = hr.get("encoding", null);
	if (prepend == null) {
	    prepend = hr.prefix + ".";
	} else if (!prepend.equals("") && !prepend.endsWith(".")) {
	    prepend = prepend + ".";
	}

	try {
	    Process process;
	    if (usesh) {
		String sh[] = {"/bin/sh", "-c", ""};
		sh[2] = command;
		process = Runtime.getRuntime().exec(sh);
	    } else {
		process = Runtime.getRuntime().exec(command);
	    }
	    HttpInputStream in = new HttpInputStream(process.getInputStream());
	    HttpInputStream err = new HttpInputStream(process.getErrorStream());

	    // If we have stdin data in a property, send it to the process

	    String stdin = hr.get("stdin");
	    if (stdin != null) {
		OutputStream out = process.getOutputStream();
		out.write(stdin.getBytes());
		out.close();
	    }

	    // Read the streams into the proper properties

	    ByteArrayOutputStream buff = new ByteArrayOutputStream();
	    in.copyTo(buff);
	    in.close();
	    String enc = null;
	    if (encoding != null) {
	       enc = buff.toString(encoding);
	    } else {
	       enc = buff.toString();
	    }
	    hr.request.props.put(prepend + "stdout", enc);
	    buff.reset();

	    err.copyTo(buff);
	    err.close();
	    if (encoding != null) {
	       enc = buff.toString(encoding);
	    } else {
	       enc = buff.toString();
	    }
	    hr.request.props.put(prepend + "stderr", enc);

	    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
		"Waiting for: " + command);
	    process.waitFor();
	    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, "done code " +
		process.exitValue());

	    hr.request.props.put(prepend + "code", "" + process.exitValue());
	} catch (Exception e) {
	    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, e.getMessage());
	    hr.request.props.put(prepend + "error", e.getMessage());
	}

    }
}
