/*
 * SqlTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2000-2005 Sun Microsystems, Inc.
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
 * Contributor(s): cstevens, suhler.
 *
 * Version:  2.7
 * Created by suhler on 00/05/08
 * Last modified by suhler on 05/05/11 14:20:52
 *
 * Version Histories:
 *
 * 2.7 05/05/11-14:20:52 (suhler)
 *   - added type=query|system|update to <sql> to run executeQuery(),
 *   execute(), and executeUpdate() respectively
 *   - added timeout=[seconds] to <sql>
 *   - fixed "prefix" to eliminate redundant '.'s in property named
 *
 * 2.6 04/11/30-15:19:42 (suhler)
 *   fixed sccs version string
 *
 * 2.5 04/08/30-09:01:53 (suhler)
 *   "enum" became a reserved word, change to "enumer".
 *
 * 2.4 04/04/28-15:57:13 (suhler)
 *   added attributes for spedifying n/a values and 0 or 1 based indexing
 *
 * 2.3 03/08/21-12:46:28 (suhler)
 *   typo in "rowcount" property
 *
 * 2.2 03/07/07-14:45:16 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.11.1.1 03/07/07-14:08:15 (suhler)
 *   use addClosingTag() convenience method
 *
 * 2.1 02/10/01-16:39:11 (suhler)
 *   version change
 *
 * 1.11 01/08/03-18:23:21 (suhler)
 *   remove training  ws from classnames before trying to instantiate
 *
 * 1.10 01/06/04-14:10:15 (suhler)
 *   package move
 *
 * 1.9 00/12/11-13:32:37 (suhler)
 *   add class=props for automatic property extraction
 *
 * 1.8 00/10/05-15:51:58 (cstevens)
 *   PropsTemplate.subst() and PropsTemplate.getProperty() moved to the Format
 *   class.
 *
 * 1.7 00/07/07-17:02:31 (suhler)
 *   remove System.out.println(s)
 *
 * 1.6 00/07/07-15:32:51 (suhler)
 *   doc fixes
 *
 * 1.5 00/07/06-15:49:42 (suhler)
 *   doc update
 *
 * 1.4 00/05/31-13:52:03 (suhler)
 *   name change
 *
 * 1.3 00/05/19-11:50:30 (suhler)
 *   redo of error processing - its still not right though
 *
 * 1.2 00/05/10-10:44:21 (suhler)
 *   doc updates
 *
 * 1.2 00/05/08-17:37:52 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 sql/SqlTemplate.java
 *
 * 1.1 00/05/08-17:37:51 (suhler)
 *   date and time created 00/05/08 17:37:51 by suhler
 *
 */

package sunlabs.brazil.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Properties;

import sunlabs.brazil.util.StringMap;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.template.RewriteContext;

/**
 * Sample Template class for running SQL queries via jdbc and
 * placing the results into the request properties for further processing.
 * <p>
 * Foreach session, a connection is made to an sql database via jdbc.
 * Session reconnection is attempted if the server connection breaks.
 * An SQL query is issued, with the results populating the request properties.
 * The following server properties are used:
 * <dl class=props>
 * <dt>driver	<dd>The name of the jdbc driver class for the desired database.
 *		Currently, only one driver may be specified.
 *		(e.g. <code><i>prefix</i>.driver=org.gjt.mm.mysql.Driver</code>).
 * <dt>url	<dd>The jdbc url used to establish a connection with the
 *		database. (e.g.
 *		 <code><i>prefix</i>.url=jdbc:mysql://host/db?user=xxx&amp;password=yyy</code>).
 * <dt>sqlPrefix<dd>The properties prefix for any additional parameters
 * 		that are required for this connection. For example:
 *		<pre>
 *		 <i>prefix</i>.sqlPrefix=params
 *		 params.user=my_name
 *		 params.password=xxx
 *		</pre>
 *		All of the parameters are supplied to the jdbc connection
 *		at connection time.
 * </dl>
 * The <code>driver</code> and <code>url</code> parameters are required.
 * All of the code between <code>&lt;sql&gt;...&lt;/sql&gt;</code>
 * is taken to be an SQL query, and sent to the appropriate database
 * for execution.  The result of the query is placed into the request
 * properties for use by other templates, such as the
 * {@link sunlabs.brazil.template.BSLTemplate BSLTemplate} or
 * {@link sunlabs.brazil.template.PropsTemplate PropsTemplate}.
 * <p>
 * For a discussion of how the results map to properties,
 * {@link #tag_sql see below}.
 *
 * @author		Stephen Uhler
 * @version		2.7
 */

public class SqlTemplate extends Template  {
    Connection con = null;	// our connection to the database;
    String prefix;		// our properties prefix
    Properties sqlProps = null;	// extra properties to hand to connection
    boolean initialized = false;
    String url;			// the jdbc url to connect to

    public boolean
    init(RewriteContext hr) {
	super.init(hr);
	hr.addClosingTag("sql");
        if (initialized) {
            return (con != null);
	}
	initialized = true;
	prefix = hr.prefix;
        Properties props = hr.request.props;
	String driver = props.getProperty(hr.prefix + "driver");
        url = props.getProperty(hr.prefix + "url");

	if (driver == null || url == null) {
	    hr.request.log(Server.LOG_WARNING, hr.prefix,
		    " needs url and driver parameters");
	    return false;
	}

	// System.out.println(prefix + "url=" + url + " driver=" + driver);

	/*
	 * Get the extra properties passed to each sql connection
	 */

	String pre = props.getProperty(hr.prefix + "sqlPrefix");
	if (pre != null) {
	    Enumeration enumer = props.propertyNames();
	    int len= pre.length();
	    sqlProps = new Properties();
	    while(enumer.hasMoreElements()) {
		String key = (String) enumer.nextElement();
		if (key.startsWith(pre)) {
		    sqlProps.put(key.substring(len), props.getProperty(key));
		}
	    }
	    // System.out.println("Con props: " + sqlProps);
	}

	/*
	 * Load the jdbc driver and create a connection.
	 */

	try {
	    Class.forName(driver.trim());
	} catch (ClassNotFoundException e) {
	    hr.request.log(Server.LOG_WARNING, hr.prefix, e.getMessage());
	    return false;
	}
	con = setupSql(url, sqlProps);
	hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
		"Got sql connection: " + con);
	return (con != null);
    }

    /**
     * Replace the SQL query with the appropriate request properties.
     * Look for the following parameters:
     * <i>(NOTE - This interface is preliminary, and subject to change)</i>.
     * <dl>
     * <dt>debug	<dd>Include diagnostics in html comments
     * <dt>prefix	<dd>prefix to prepend to all results.
     *			    Defaults to template prefix
     * <dt>max		<dd>The max # of rows returned (default=100)
     * <dt>na		<dd>Value to reurn for NULL.  Defaults to "n/a"
     * <dt>type		<dd>The type of SQL command, one of "query", "system",
     *			or "update".  these values map to the JDBC calls
     *			executeQuery(), execute() and executeUpdate()
     *			respectively. Defaults to "query".
     * <dt>timeout	<dd>The number of seconds to wait for the query
     *			to finish.  Defaults to "0": wait forever
     * <dt>eval		<dd>If present, do ${...} to entire query. (see
     *			{@link sunlabs.brazil.util.Format#getProperty getProperty}).
     * <dt>zeroIndex	<dd>if true, row counts start at 0, not 1
     * <dt>index	<dd>If present, use column 1 as part of the name.
     *			    Otherwise, an index name is invented.
     * </dl>
     * For all queries, the following properties (with the prefix prepended)
     * are set:
     * <dl>
     * <dt>columns		<dd>The number of columns returned
     * <dt>rowcount		<dd>The number of rows returned 
     * </dl>
     * Foreach entry in the resultant table, its property is:
     * <code>${prefix}.${table_name}.${columname}.${key}</code>.  If
     * the <code>index</code> parameter is set, the key is the value of
     * the first column returned. Otherwise the key is the row number, 
     * and the additional property <code>${prefix}.rows</code> contains a
     * list of all the row numbers returned.
     */

    public void
    tag_sql(RewriteContext hr) {
	debug = hr.isTrue("debug");
	boolean eval  = hr.isTrue("eval");
	String type =  hr.get("type", "query");
	boolean useIndex  = hr.isTrue("index");
	boolean zeroIndex = hr.isTrue("zeroIndex");
	String na = hr.get("na", "n/a");

	String pre = hr.get("prefix");
	if (pre == null) {
	    pre = prefix;
	}
	if (!pre.equals("") && pre.endsWith(".") == false) {
	    pre+= ".";
	}
	int max=100;
	try {
	    max = Integer.decode(hr.get("max", "100")).intValue();
	} catch (Exception e) {}

	int timeout=0;
	try {
	    timeout = Integer.decode(hr.get("timeout", "0")).intValue();
	} catch (Exception e) {}

        debug(hr);
	hr.accumulate(false);
	hr.nextToken();
	String query = hr.getBody();
	hr.accumulate(true);
	hr.nextToken();	// eat the </sql>

        Properties props = hr.request.props;
	if (eval) {
	    query = Format.subst(props, query);
	}
	debug(hr, query);

	/*
	 * The connection to the server might have timed out.  If so
	 * the connection will fail.  If that happens, try to re-open the 
	 * connection.
	 */

	Statement stmt = null;
	ResultSet result = null;
	int updateCount = -1;
	ResultSetMetaData meta = null;
	boolean retry = false;

	try {
	    stmt = doSQL(query, type, timeout);
	    result = stmt.getResultSet();
	    if (result != null) {
	        meta = result.getMetaData();
	    }
	    updateCount = stmt.getUpdateCount();
	} catch (SQLException e) {
	    debug(hr, "Connection failed, will retry: " + 
		e.getMessage());
	    retry = true;
	}

	/*
	 * Now run the query, stuffing the results into the properties.
	 * This is pretty stupid right now.  The first column is used as
	 * the "index" if useIndex is set.  Otherwise a counter is used.
	 */

	try {
	    if (retry) {
		con = setupSql(url, sqlProps);
	        stmt = doSQL(query, type, timeout);
		result = stmt.executeQuery(query);
		if (result != null) {
		    meta = result.getMetaData();
		}
	        updateCount = stmt.getUpdateCount();
	    }
	    props.put(pre + "updateCount", "" + updateCount);
	    if (meta == null || result==null) {
	       return;
	    }

	    int rows = 0;
	    int count = meta.getColumnCount();
	    StringBuffer list = null;
	    props.put(pre + "columns", "" + count);
	    if (!useIndex) {
		list = new StringBuffer();
	    }
	    while (result != null && result.next() && rows++ < max) {
		String first;	// name of property row
		if (useIndex) {
		    first = result.getString(1);
		} else {
		    first = "" + (zeroIndex ? rows-1 : rows);
		    list.append(first).append(" ");
		}
		for(int i=(useIndex?2:1); i<=count; i++) {
		    String name = deriveName(pre, meta, first, i);
		    String value = result.getString(i);
		    if (value == null) {
			value = na;
		    }
		    props.put(name,value);
		    // debug(hr,name + "=" + value);
		}
	    }
	    if (list != null) {
		props.put(pre + "rows", list.toString());
	    }
	    props.put(pre + "rowcount", "" + rows);
	} catch (SQLException e) {
	    props.put(pre + "error", e.getMessage());
	    debug(hr, "Failed: " + e.getMessage());
	    hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix, e.getMessage());
	    props.put(pre + "rowcount", "0");
	}
    }

    public Statement
    doSQL(String query, String type, int timeout) throws SQLException {
	Statement stmt = con.createStatement();
	if (timeout > 0) {
	    stmt.setQueryTimeout(timeout);
	}
	if (type.equals("query")) {
	    stmt.executeQuery(query);
	} else if (type.equals("update")) {
	    stmt.executeUpdate(query);
	} else if (type.equals("system")) {
	    stmt.execute(query);
	}
	return stmt;
    }


    /**
     * Convenience method for deriving props names
     */

    String
    deriveName(String prefix, ResultSetMetaData meta, String suffix, int i) {
	String table = "n/a";
	String column = "n/a";
	try {
	    table = meta.getTableName(i);
	} catch (SQLException e) {}
	try {
	    column = meta.getColumnName(i);
	} catch (SQLException e) {}
	String result =  prefix + table + "." + column + "." + suffix;
	// System.out.println("Getting name: " + result);
	return result;
    }

    public void
    tag_slash_sql(RewriteContext hr) {
	// System.out.println("Got /sql tag");
    }

    /**
     * Setup a connection to an SQL server.
     * Assume driver is already registered.
     */

    public Connection
    setupSql(String url, Properties props) {
	Connection con  = null;
        try {
	    con = DriverManager.getConnection(url, props);
	} catch (SQLException e) {
	    System.out.println("SQL Exception: " + e);
	    e.printStackTrace();
	} catch (Exception e) {
	    System.out.println("Exception in SQL setup: " + e);
	    e.printStackTrace();
	}
	return con;
    }
}
