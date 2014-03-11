/*
 * EmailTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2001-2004 Sun Microsystems, Inc.
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
 * The Initial Developer of the Original Code is: guym.
 * Portions created by guym are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): guym, suhler.
 *
 * Version:  2.2
 * Created by guym on 01/06/05
 * Last modified by suhler on 04/11/30 15:19:38
 *
 * Version Histories:
 *
 * 2.2 04/11/30-15:19:38 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:39:14 (suhler)
 *   version change
 *
 * 1.48 02/04/18-11:15:09 (suhler)
 *   document bug
 *
 * 1.47 02/03/06-09:06:16 (suhler)
 *   better diagnostics
 *
 * 1.46 02/02/25-11:38:58 (suhler)
 *   added better mail sending diagnostics
 *
 * 1.45 02/01/31-10:16:11 (suhler)
 *   added "bcc=user@domain" option to <sendmail> tag
 *
 * 1.44 01/11/21-11:42:37 (suhler)
 *   doc fixes
 *
 * 1.43 01/10/16-16:02:19 (suhler)
 *   bug fix for missing messages
 *
 * 1.42 01/10/03-13:53:31 (suhler)
 *   * <folder list...> may be called on an arbitrary directory.
 *   - make a cursory check for "validity" before returning a folder
 *   - return folders in a single property to alleviate performance issues
 *   when querying large directories
 *   - add "glob=" to gain acceptable performance (the "validity" check on
 *   each folder can be expensive)
 *   * handleFatalError doesn't provide sufficient debugging info for tracking
 *   down problems
 *   - added "Exception" argument, to centralize error tracking
 *   - Changed "errmsg" on most calls to be more useful
 *   - moved call to log() into method, to provide better diagnostics
 *   * Modified getMessageObjects
 *   - moved "startmsg" and "msglimit" processing inside, allowing us to add
 *   new options (such as searching) in only on spot
 *   - Getting message info for a single message was (sometimes)
 *   returning an array to lots of message objects, then ignoring all but the
 *   first.
 *   * Not fixed: no check for never-supplied default directory
 *
 * 1.41 01/09/27-09:53:45 (suhler)
 *   Temporary patches:
 *   - handleFatalError now takes an Exception as a parameter, to permit better
 *   diagnosing of errors
 *   - Several places are checked for "ReadOnlyFolderExceptions" to prevent
 *   bogus "lost connection to server" errors
 *   - Code added to detect a refile into a bad folder from tossing a bogus
 *   "lost connection to mail server" error
 *   * Many (if not all) of the "lost connection to mail server" messages
 *   are erroneous, and could be more accurately detected by examining the
 *   exceptions.
 *
 * 1.40 01/09/25-13:55:48 (suhler)
 *   forgot check for null message array
 *
 * 1.39 01/09/20-13:50:27 (guym)
 *   Fixed an off-by-one error with prevmsgnum
 *
 * 1.38 01/09/20-11:10:39 (guym)
 *   Merged changes between child workspace "/home/guym/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.35.1.1 01/09/20-11:05:01 (guym)
 *   Rewrote getMessageObjects() to be easier to read, and more efficient.
 *
 * 1.37 01/09/19-15:22:50 (suhler)
 *   - made a filter so it can get session info from session filter
 *   - temporary pathes for message window bugs
 *
 * 1.36 01/09/17-15:18:03 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.35 01/09/17-15:00:47 (guym)
 *   Cleanup of code from comments made in code review
 *
 * 1.34.1.1 01/09/13-09:22:46 (suhler)
 *   check for null pointer
 *
 * 1.34 01/09/07-11:03:42 (guym)
 *   Fixed bug in getnewheaders that was causing the pointer to the last old message to be incorrectly set.
 *
 * 1.33 01/08/31-16:11:47 (guym)
 *   Fixed refile implementation to correctly refile before a purge or the select of a new folder.
 *
 * 1.32 01/08/30-15:44:54 (guym)
 *   Added support for 'server:port' in email server specification, and also fixed a nagging bug that was causing Subject lines in the email client to 'dissappear'.
 *
 * 1.31 01/08/29-13:37:05 (guym)
 *   Re-architected the refilemsg capability for better response time - also made all performance measurements print as LOG_DIAGNOSTIC messages
 *
 * 1.30 01/08/29-10:27:19 (guym)
 *   Broke up the performance monitoring for better granularity
 *
 * 1.29.1.1 01/08/27-18:21:03 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.29 01/08/27-17:15:50 (guym)
 *   Fixed a problem with the onError handling not working if a connHandle wasn't found.
 *
 * 1.28.1.1 01/08/27-12:55:12 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.28 01/08/27-12:10:37 (guym)
 *   First round of changes to support attachments in the compose functionality.
 *
 * 1.27.1.1 01/08/21-14:20:45 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.27 01/08/21-12:32:39 (guym)
 *   Added javadoc documentation for <sendmail> tag, and also added a private boolean to keep track of user state
 *
 * 1.26.1.1 01/08/20-15:31:22 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/home/guym/ws/brazil/naws".
 *
 * 1.25.1.2 01/08/20-15:30:02 (suhler)
 *   don't put nulls in request props
 *
 * 1.26 01/08/20-15:30:02 (guym)
 *
 * 1.25.1.1 01/08/17-10:01:15 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.25 01/08/16-14:05:26 (guym)
 *   Fine-tuned the exception handling so that fatal errors are *only* ones that require re-authentication with the mail server.  Non-fatal errors are returned in a seperate property
 *
 * 1.24 01/08/14-17:57:20 (guym)
 *   Fixed the retrieval of 'new' messages to allow for client side filtering of newly arrived email messages
 *
 * 1.23 01/08/13-12:45:37 (guym)
 *   Fixed a bug where creating folders was not working
 *
 * 1.22.1.1 01/08/13-09:49:25 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.21.1.1 01/08/13-09:47:16 (suhler)
 *   added debugging (temporary)
 *
 * 1.22 01/08/13-09:30:42 (guym)
 *   Fixed compilation bug by changing hr.setRewriteState(RewriteContext.ABORT) hr.abort()
 *
 * 1.21 01/08/06-16:42:31 (guym)
 *   Added support for a per tag onError parameter, which specifies the location of a page to be redirected to in case of an error.
 *
 * 1.20 01/08/02-13:49:36 (guym)
 *   Added a <forcetimeout> tag to allow for testing of the error handling/timeouts wihtin the EmailTemplate
 *
 * 1.19 01/08/01-16:36:57 (guym)
 *   Merged changes between child workspace "/home/guym/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.17.1.1 01/08/01-16:30:20 (guym)
 *   Standardized the error property returned when one of the email tags generates an error - the property is prefix.fatalMailError
 *
 * 1.18 01/07/17-17:20:57 (suhler)
 *   RewriteContenxt.hr.get() now has Format.subst() built in
 *
 * 1.17 01/07/12-14:16:51 (guym)
 *   Made 'foldername' the consistent name for paramaters in <message> and <folder> tags - Also changed the implementation of deletemsg,undeletemsg,and refilemsg to send requests to the server in (hopefully) one chunk to improve performance.
 *
 * 1.16 01/07/12-11:58:41 (suhler)
 *   use jdk1.1 compatible methods of Vector()
 *
 * 1.15 01/07/10-19:36:18 (guym)
 *   Added getnewheaders and undeletemsg operations to the <message> tag
 *
 * 1.14 01/06/29-13:33:44 (guym)
 *   Merged changes between child workspace "/home/guym/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.12.1.1 01/06/29-13:16:16 (guym)
 *   Added support for a folder parameter in the <message> tag, allowing multiple open folders to be operated on safely.  Also added support for returning
 *
 * 1.13 01/06/28-11:46:27 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.11.1.1 01/06/28-11:24:48 (suhler)
 *   timestamps in brazil are "seconds", not "ms"
 *
 * 1.12 01/06/25-20:27:13 (guym)
 *   Merged changes between child workspace "/home/guym/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.10.1.1 01/06/25-20:18:34 (guym)
 *   Fixed msglimit 'off-by-one' bug - also added a 'filename' component to the parturl property, allowing the netscape 'Save As' dialog to give a sensible name on athments it doesn't know what to do with.
 *
 * 1.11 01/06/25-10:04:38 (suhler)
 *   added additional fields to "getheaders" as a "stop-gap" to let me
 *   proceeed with the GUI
 *
 * 1.10 01/06/21-14:01:23 (guym)
 *   Merged changes between child workspace "/home/guym/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.8.1.1 01/06/21-13:38:21 (guym)
 *   Added support for date and address-parsing of headers, cleaned up code (moved often used portions in seperate methods), added support for lists of messages to be deleted via the message tag.  See the javadoc generated doc for full details.
 *
 * 1.9 01/06/18-08:42:27 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.6.1.1 01/06/18-08:41:32 (suhler)
 *   "startmsg=" in <message> whould do the right thing
 *
 * 1.8 01/06/15-16:58:21 (guym)
 *   Fixed attachment handling for multipart/DIGEST and multipart/ALTERNATIVE.
 *
 * 1.7 01/06/15-15:17:22 (guym)
 *   Added support for multiple open folder connections, plus added a reconnect() routine to be called in case of 'FolderClosedExceptions'.
 *
 * 1.6 01/06/13-15:44:43 (guym)
 *   Fixed performance issue in header retrieval, and added support for retrieving all headers instead of just a specific list.
 *
 * 1.5 01/06/12-17:24:13 (guym)
 *   Added support for 'D' and 'R' status flags to be placed in the msgstatus property
 *
 * 1.4 01/06/12-12:40:04 (guym)
 *   Fixed some broken tags in the javadocs.
 *
 * 1.3 01/06/07-13:14:08 (guym)
 *   Added javadoc comments.
 *
 * 1.2 01/06/05-14:09:39 (guym)
 *   Added SCCS ID information.
 *
 * 1.2 01/06/05-14:00:22 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 email/EmailTemplate.java
 *
 * 1.1 01/06/05-14:00:21 (guym)
 *   date and time created 01/06/05 14:00:21 by guym
 *
 */

package sunlabs.brazil.email;

import sunlabs.brazil.template.Template;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.filter.Filter;
import sunlabs.brazil.util.http.MimeHeaders;
import sunlabs.brazil.util.http.HttpUtil;
import sunlabs.brazil.util.Format;
import sunlabs.brazil.util.Glob;

import java.io.IOException;

import java.lang.Math;

import java.util.Date;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.mail.Folder;
import javax.mail.Flags;
import javax.mail.FetchProfile;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.FolderNotFoundException;
import javax.mail.ReadOnlyFolderException;
import javax.mail.NoSuchProviderException;
import javax.mail.AuthenticationFailedException;
import javax.mail.IllegalWriteException;
import javax.mail.SendFailedException;
import javax.mail.Multipart;
import javax.mail.BodyPart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.InternetAddress;

/**
 * The <code>EmailTemplate</code> takes an HTML document with embedded "Email"
 * markup tags in it and evaluates those special tags to produce resulting 
 * properties that allow email reading/composing functionality to be embedded 
 * within the document.
 * 
 * <p>
 * The email functionality is implemented using four tags: <ul>
 * <li> <code>&lt;email&gt;</code>
 * <li> <code>&lt;folder&gt;</code>
 * <li> <code>&lt;message&gt;</code>
 * <li> <code>&lt;sendmail&gt;</code>
 * </ul>
 * <p>
 * The <code>&lt;email&gt;</code> tag handles 'state control'.
 * The <code>&lt;folder&gt;</code> tag provides a means to manipulate mail folders.
 * The <code>&lt;message&gt;</code> tag provides viewing capabilities for email 
 * messages.
 * The <code>&lt;sendmail&gt;</code> tag allows outgoing messages to be sent via SMTP.
 * There is an additional tag (<code>&lt;forcetimeout&gt;</code>) that is used mainly for
 * debugging.
 * <p>
 *
 * <hr>
 * <h3>&lt;email&gt; TAG</h3>
 * The <code>&lt;email&gt;</code> tag provides 'state control' for the connection
 * to the email server.  It handles opening and closing the connection, as well
 * as providing authentication parameters to the server.  The general format of the 
 * <code>&lt;email&gt;</code> tag is as follows:
 * <pre>
 * &lt;email server=servername user=username password=userpassword 
 *  action=[open,close] onError="somefile.html" connhandle=xyz&gt;</pre>
 *
 * <p>
 * The <var>connhandle</var> parameter is optional, and is used to be able to 
 * identify a unique server connection if more than one <code>&lt;email&gt;</code> tag
 * is present in an HTML page (for example, an email client that connects to more
 * than one email server to retrieve messages).  The <var>connhandle</var> parameter
 * is also used as the prefix for properties returned from this and all of the email
 * tags.  See {@link sunlabs.brazil.template.PropsTemplate} for more 
 * information on the <code>&lt;property&gt;</code> tag.  If no <var>connhandle</var>
 * is present, the properties returned are prefixed with the identifier used for
 * the <code>EmailTemplate</code> in the configuration file.  The string 
 * <var>prefix</var> will be used throughout this document to refer to this 
 * prepended string.
 *
 * <p>
 * Note that although <var>server</var>, <var>user</var>, and <var>password</var> are
 * not listed as optional, they can be left out after their first use.  For example,
 * after performing an <var>action=open</var> command, the connection to the server 
 * can be closed with the following command:
 * <pre>
 * &lt;email action=close&gt;
 * </pre>
 * <p>
 * The <var>onError</var> parameter is also optional, but if present, will give a location
 * that the HTML client expects to be redirected to in case of a fatal error within
 * the processing for this tag.  In an error condition, this parameter does an implicit
 * abort on all further processing of the HTML page.
 * <p>
 *
 * <hr>
 * <h3>&lt;folder&gt; TAG</h3>
 * The <code>&lt;folder&gt;</code> tag provides functionality necessary to manipulate
 * email folders.  It allows for the selection of a particular folder, listing of all
 * folders within a folder directory, as well as for other utility functions.  The 
 * general format of the <code>&lt;folder&gt;</code> tag is as follows:
 * <pre>
 * &lt;folder dir=/xyz foldername=[INBOX,anyfoldername] 
 *  action=[list,purge,msgcount,create,delete] onError="somefile.html" 
 *  connhandle=xyz&gt;</pre>
 *
 * <p>
 * The <var>onError</var> parameter is optional, but if present, will give a location
 * that the HTML client expects to be redirected to in case of a fatal error within
 * the processing for this tag.  In an error condition, this parameter does an implicit
 * abort on all further processing of the HTML page.
 * <p>
 * The <var>connhandle</var> parameter is also optional, and its use is specified in the
 * documentation for the <code>&lt;email&gt;</code> tag.
 * <p>
 * The <var>dir</var> parameter is used to establish a base directory where a user's
 * email folders reside.  For example, on some UNIX systems, this is /home/username/Mail.
 * <p>
 * The <var>foldername</var> parameter is used to specify a foldername to operate on 
 * for the various operations (except <var>action=list</var>).
 * <p>
 * The <var>action=list</var> operation will create a list all mail folders within the 
 * directory specified by <var>dir</var>.  This list is returned in property:
 * <code><var>prefix</var>.folders</code> - where each folder
 * is delimited by the <code>delim</code> character, which defaults
 * to "/".  An optional <code>glob</code> attribute restircts the
 * folders to those matching the glob pattern.
 * <p>
 * The <var>action=msgcount</var> operation returns the total count of messages and the
 * count of new messages in the selected folder in the following properties:
 * <p>
 * <code><var>prefix</var>.totalmsgcount</code><br>
 * <code><var>prefix</var>.newmsgcount</code>
 * <p> 
 * This operation works on either a specified folder name (if provided), or the
 * last selected folder. 
 * <p>
 * The <var>action=create</var> operation creates a new folder with the name given.
 * The name of the folder is required, but the dir parameter is optional.  If not
 * specified, the current default directory will be used as the root location of
 * the new folder.
 * <p>
 * The <var>action=delete</var> operation deletes the named folder. The name of the 
 * folder is required, but the dir parameter is optional.  If not given, the tag
 * assumes that the folder to be deleted is located in the default directory.
 *
 *
 * <hr>
 * <h3>&lt;message&gt; TAG</h3>
 * The <code>&lt;message&gt;</code> tag is the workhorse of the Brazil email suite.
 * It provides the ability to get message header listings, and to retrieve/delete/refile
 * individual messages and their attachments.  The general format of the
 * <code>&lt;message&gt;</code> tag is as follows:
 * <pre>
 * &lt;message action=[getheaders,getnewheaders,getmsg,deletemsg,undeletemsg,refilemsg] 
 *  startmsg=nnn msglimit=nnn msgnum="n n n" headerlist="From, To, Date"
 *  foldername=xyz refilefolder=xyz onError="somefile.html" connhandle=xyz&gt;</pre>
 *
 * <p>
 * The <var>onError</var> parameter is optional, but if present, will give a location
 * that the HTML client expects to be redirected to in case of a fatal error within
 * the processing for this tag.  In an error condition, this parameter does an implicit
 * abort on all further processing of the HTML page.
 * <p>
 * The <var>connhandle</var> parameter is also optional, and its use is specified in the
 * documentation for the <code>&lt;email&gt;</code> tag.
 * <p>
 * The <var>foldername</var> parameter is required for all operations, so that
 * multiple open folders can be supported in seperate browser windows.
 * <p>
 * The <var>action=getheaders</var> operation uses the <var>headerlist</var>
 * parameter to determine which header values to retrieve for each message.
 * The header names in this list are case-sensitive.  If <var>headerlist</var> isn't
 * present, <b>ALL</b> headers will be retrieved.  This operation takes the
 * optional parameters <var>msglimit</var> and/or <var>startmsg</var>.  If 
 * <var>msglimit</var> is specified without <var>startmsg</var>, the headers returned
 * will be the last <var>msglimit</var> number.  
 * <p>
 * The <var>action=getnewheaders</var> operation takes all of the same parameters
 * as the <var>action=getheaders</var> operation, except <var>msglimit</var>, and 
 * returns values in the same properties listed below.  As its name suggests, however, 
 * it only returns header information for the newly arrived messages in the folder. 
 * <p>
 * The header listing information is returned in the following properties:
 * <p>
 * <code><var>prefix</var>.n.msgnum</code> - where 'n' = 1,2,3... (this is the msg 
 * number as defined by the mail server)
 * <p>
 * <code><var>prefix</var>.n.msgstatus</code> (this is either a 'U' for unread, an 
 * 'N' for new, a 'R' for read, or a 'D' for deleted)
 * <p>
 * <code><var>prefix</var>.n.Headername</code> - where 'Headername' = one of the header
 * names returned or one of those within the <var>headerlist</var> parameter 
 * (for example <code><var>prefix</var>.1.From</code>)
 * <p>
 * <code><var>prefix</var>.n.Headername.n</code> - where 'Headername' = one of the 
 * header names returned or one of those within the <var>headerlist</var> parameter 
 * and 'n' = msg number. (This format is only returned for headers that occur
 * multiple times in an email message with unique values (such as 'Received').  Also
 * returned in this case is a property named:
 * <code><var>prefix</var>.n.Headername.count</code> which contains the enumerated
 * count of the multivalue header.
 * <p>
 * <code><var>prefix</var>.n.[To,From,Cc]</code> - This is a special case property that returns a comma seperated list of rfc822 style (user@domain) addresses
 * for the particular header (To,From,Cc) that is used to name the property.
 * <p>
 * <code><var>prefix</var>.n.[To,From,Cc].address.n</code> - This is a special case property that
 * is returned for headers that are used for addresses (To,From,Cc).  This property
 * contains the rfc822 'user@domain' portion of the header.  Since it is a multivalue
 * header, (like Received), it also has a count portion (the last 'n') representing an
 * enumeration of which address it was in the header (e.g. '0 1 2').  
 * <p>
 * <code><var>prefix</var>.n.[To,From,Cc].personal.n</code> - This is a special case property that
 * corresponds to the 'address' property above.  It contains the 'Joe A. User' portion
 * of the address.  It is also a multivalue property like address. Also returned in these
 * cases is a property named:
 * <code><var>prefix</var>.n.To.count</code> which contains the enumerated
 * count of the multivalue header (To,From,Cc).
 * <p>
 * <code><var>prefix</var>.n.Date</code> - This is a special case property that
 * returns the message sent date as a string representation.
 * <p>
 * <code><var>prefix</var>.n.Datestamp</code> - This is a special case property that
 * returns the message sent date as a string representation of the number of seconds 
 * elapsed since the epoch (Jan 1, 1970).  This is useful in providing additional
 * formatting via the Brazil {@link sunlabs.brazil.sunlabs.DateTemplate}.  
 * <p>
 * Both of the above properties are returned for every 'Date' header requested in the
 * <var>action=getheaders</var> operation. 
 * <p>
 * <code><var>prefix</var>.n.msgindex</code> (this contains a list of the msg
 * numbers - for example '1 2 3 4 ...' - this is useful in iterating over the 
 * properties returned here with the {@link sunlabs.brazil.template.BSLTemplate}
 * <code>&lt;foreach&gt;</code> tag)
 * <p>
 * The <var>action=getmsg</var> operation requires the <var>msgnum</var> parameter
 * and returns the following properties:
 * <p>
 * <code><var>prefix</var>.msgnum</code> (contains the message number)
 * <p>
 * <code><var>prefix</var>.n.Headername</code> - where 'Headername' = one of the names
 * within the <var>headerlist</var> parameter and 'n' represents the msgnum 
 * (multi-value headers like 'Received' are handled in the same manner as described
 * above for <var>action=getheaders</var>).
 * <p>
 * <code><var>prefix</var>.n.body</code> (contains the text of the email msg 
 * (if available))
 * <p>
 * <code><var>prefix</var>.n.msgmimetype</code> (contains the string that specifies what
 * MIME type the message is - useful for deciding if you'll need to convert plaintext to
 * HTML before displaying it) 
 * <p>
 * <code><var>prefix</var>.n.j.partname</code> - where 'n' is the msgnum, and 'j' is a 
 * part number (for multipart messages/attachments) (this contains the text string name of
 * the multipart attachment)
 * <p>
 * <code><var>prefix</var>.n.j.parturl</code> - where 'n' is the msgnum, and 'j' is a 
 * part number (for multipart messages/attachments) (this contains the URL that is needed
 * to retrieve the multipart attachment - this URL can be used in an &lt;href&gt or 
 * &lt;form&gt; tag)
 * <p>
 * <code><var>prefix</var>.n.partcount</code> (this contains a list of the part numbers 
 * and is useful for iterating over the parts properties with the 
 * {@link sunlabs.brazil.template.BSLTemplate} <code>&lt;foreach&gt;</code> tag)
 * <p>
 * The <var>action=deletemsg</var> and <var>action=undeletemsg</var> operations requires the <var>msgnum</var> parameter,
 * which can contain either a single message number or a space seperated list 
 * (e.g. '1 2 3 4') of numbers.
 * <p>
 * The <var>action=refilemsg</var> operation requires the <var>msgnum</var> and 
 * <var>refilefolder</var> parameters.  The value of <var>refilefolder</var> is
 * a relative path from the <var>dir</var> parameter value provided in the 
 * <code>&lt;folder&gt;</code> tag.  The <var>msgnum</var> parameter here can also
 * accept a space seperated list (as in the <var>action=deletemsg</var> tag).
 *
 * <hr>
 * <h3>&lt;sendmail&gt; TAG</h3>
 * The <code>&lt;sendmail&gt;</code> tag provides the ability to send outgoing
 * email messages using the SMTP protocol.  The simplest form of the
 * <code>&lt;sendmail&gt;</code> tag is as follows:
 * <pre>
 * &lt;sendmail to=user@domain cc=user@domain bcc=user@domain from=user@domain subject="test subject" 
 *  body="msg text" onError="somefile.html" connhandle=xyz&gt;</pre>
 * <p>
 * This form of the tag provides basic functionality (no attachments) to send simple
 * messages.  The complete form of the tag is as follows:
 * <pre>
 * &lt;sendmail to=user@domain cc=user@domain from=user@domain subject="test subject" 
 *  body="msg text" onError="somefile.html" connhandle=xyz&gt;
 *     &lt;part body=partdata name=bodypartfilename encoding=none|qp|base64
 *      content-type=mimetype&gt;
 *     &lt;part ....&gt;
 * &lt;/sendmail&gt;
 * </pre>
 * <p>
 * This form of the tag allows attachments to be added to outgoing messages (via the
 * &lt;part&gt; tag.  The &lt;part&gt; tag requires parameters for the actual data
 * for each part, a filename (optional), an encoding type and a content-type (optional).
 *
 * <p>
 * The <var>onError</var> parameter is optional, but if present, will give a location
 * that the HTML client expects to be redirected to in case of a fatal error within
 * the processing for this tag.  In an error condition, this parameter does an implicit
 * abort on all further processing of the HTML page.
 * <p>
 * The <var>connhandle</var> parameter is also optional, and its use is specified in the
 * documentation for the <code>&lt;email&gt;</code> tag.
 * <p>
 * The <var>cc</var> parameter is also optional.
 * <p>
 * The <var>from</var>, <var>subject</var>, and <var>body</var> parameters should all
 * be set to their respective values.
 * <p>
 * In the event of an error during an attempt to send a message (as denoted by a 
 * 'mailError' property being set (see Error Handling)), the following property
 * will be set:
 * <p>
 * <code><var>prefix</var>.sendmailerror</code> - contains the specific reason that the
 * sending of this message failed.
 *
 * <hr>
 * <h3>&lt;forcetimeout&gt; TAG</h3>
 *
 * This tag provides the ability to 'force' a timeout of the connection to the email server.
 * It is generally only used for debugging purposes.  The complete form of the tag is 
 * as follows:
 * <pre>
 * &lt;forcetimeout connhandle=xyz&gt;
 * </pre>
 * <p>
 * The <var>connhandle</var> parameter is also optional, and its use is specified in the
 * documentation for the <code>&lt;email&gt;</code> tag.
 * <p>
 * After this tag is processed, calls to any of the other tags will result in errors
 * being generated (see Error Handling below).
 * 
 * <hr>
 * <h3>Error Handling</h3>
 *
 * In the case of a fatal error (one that requires re-authentication with the email 
 * server) during any email operation, the following property is set (unless you used 
 * the per tag 'onError' parameter for error handling):
 * <p>
 * <code><var>prefix</var>.fatalMailError</code> - this contains a string with the
 * error message returned from the email server.
 * <p>
 * Some non-fatal errors may be returned during the processing of any of the tags
 * and these are returned in the following property (note that these are returned 
 * regardless of the existence of the 'onError' parameter in a particular tag):
 * <p>
 * <code><var>prefix</var>.mailError</code> - this contains a string with the non-fatal
 * error message returned from the email server.
 * 
 */ 

public class EmailTemplate extends Template implements Filter {

  public boolean shouldFilter(Request request, MimeHeaders headers) {return false;}
  public byte[] filter(Request request, MimeHeaders headers, byte[] content) {return content;}

  /* Instance variables */

  private String prefix;
  private Hashtable serverConnections;
  private Hashtable refileFolders;
  private Properties sessionProps;
  private MimeMessage sendMsg;
  private MimeMultipart sendMultipart;
  private boolean msgPending;
  private Session defMailSession;
  private String session;


  public EmailTemplate() {

    /* Misc. initializations */

    serverConnections = new Hashtable();
    refileFolders = new Hashtable();
    
    /* Setup our SMTP properties */

    sessionProps = new Properties();
    sessionProps.put("mail.transport.protocol","smtp");
    sessionProps.put("mail.smtp.host","localhost");
    defMailSession = Session.getInstance(sessionProps,null);
  }

  /* Utility routine to determine our connhandle for a particular tag */

  private static String getConnHandle(RewriteContext hr) {
    String connHandle;
    
    connHandle = hr.get("connhandle");

    if (connHandle == null) {
      connHandle = hr.prefix;
    }

    if (!connHandle.endsWith(".")) {
      connHandle += ".";
    }

    return connHandle;
  }

  /* Utility routine to parse addrs */

  private static void parseAddrHdr(String connHandle,Properties props,
                                   Message msg,String hdrName) 
                                   throws MessagingException {
    String msgNum;
    String delim = "";
    StringBuffer hdrIndex = null; 
    StringBuffer sb = null;
    InternetAddress []addr = null; 
    int hdrCount = 0;

    /* Get our message number */

    msgNum = Integer.toString(msg.getMessageNumber());

    /* Determine what we have to parse */

    if ("To".equals(hdrName)) {
      addr = (InternetAddress []) msg.getRecipients(Message.RecipientType.TO);
    } else if ("From".equals(hdrName)) {
      addr = (InternetAddress []) msg.getFrom();
    } else if ("Cc".equals(hdrName)) {
      addr = (InternetAddress []) msg.getRecipients(Message.RecipientType.CC);
    }

    /* Populate our properties */

    if (addr != null) {
      hdrIndex = new StringBuffer();
      sb = new StringBuffer();

      for (int i=0;i<addr.length;i++) {
        props.put(connHandle + msgNum + "." + hdrName + ".address." + i,
                  addr[i].getAddress());
	sb.append(delim).append(addr[i].getAddress());
	delim=", ";
        if (addr[i].getPersonal() != null) {
          props.put(connHandle + msgNum + "." + hdrName + ".personal." + i,
                    addr[i].getPersonal());
        } else {
	  props.put(connHandle + msgNum + "." + hdrName + ".personal." + i,
                  addr[i].getAddress());
	}
        hdrIndex.append(hdrCount + " ");
        hdrCount++;
      }
      props.put(connHandle + msgNum + "." + hdrName,sb.toString());
      props.put(connHandle + msgNum + "." + hdrName + ".count",hdrIndex.toString());
    }
  }

  /* Utility routine to parse date hdrs */

  private static void parseDateHdr(String connHandle,Properties props,
                                   Message msg) throws MessagingException {
    String msgNum;
    String[] DateString;
    Date sentDate;

    /* Get our message number */

    msgNum = Integer.toString(msg.getMessageNumber());

    /* Get the string representation of the date and put it in property 'Date' */

    DateString = msg.getHeader("Date");

    if (DateString != null && DateString[0] != null) {
      props.put(connHandle + msgNum + ".Date",DateString[0]);
    }

    /* Get the 'sent' date from this email, and return it as a Datestamp */

    sentDate = msg.getSentDate();

    if (sentDate != null) {
      props.put(connHandle + msgNum + ".Datestamp",
	  Long.toString(sentDate.getTime()/1000));
    }
  }

  /* Utility routine to retrieve and open a folder */

  private static void selectFolder(RewriteContext hr,EmailConnection tmpEmailConn,
                                   String folderName) throws MessagingException {
    Folder tmpFldr = null;
    Properties props = hr.request.props;
    String connHandle = getConnHandle(hr);

    if (folderName != null) {
      /* Get the selected folder */

      tmpEmailConn.currentFolder = (FolderInfo) tmpEmailConn.openFolders.get(folderName);

      if (tmpEmailConn.currentFolder != null) {
        tmpEmailConn.currentFolder.newFolder = true;
      } else {
        tmpFldr = tmpEmailConn.userMsgStore.getFolder(folderName); 
        tmpEmailConn.currentFolder = new FolderInfo(tmpFldr);
        tmpEmailConn.openFolders.put(folderName,tmpEmailConn.currentFolder);
      }

      hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"tmpEmailConn.currentFolder = " +
                       tmpEmailConn.currentFolder.getFolder().getName());
    } else {
      throw new FolderNotFoundException(
                "Foldername passed to EmailTemplate.getFolder() is NULL!",null);
    }
  }

  /* Utility routine to retrieve header info for an array of Messages */

  private static void fetchHeaderInfo(RewriteContext hr,Message[] msgs,
                                      EmailConnection tmpEmailConn,
                                      String headerlist) throws MessagingException {

    Properties props = hr.request.props;
    FetchProfile fp = new FetchProfile(); 
    StringBuffer msgindex = new StringBuffer();
    String connHandle = getConnHandle(hr);
    StringTokenizer headerNames;
    String msgNum;
    long startmillis,endmillis;

    /* Build the prefetch profile */

    startmillis = System.currentTimeMillis();

    if (headerlist != null) {
      headerNames = new StringTokenizer(headerlist,", ");

      fp.add(FetchProfile.Item.FLAGS);

      while (headerNames.hasMoreTokens()) {
        fp.add(headerNames.nextToken());
      }
    } else {
      hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"no header list specified" +
                     " - retrieving all headers");

      fp.add(FetchProfile.Item.FLAGS);
      fp.add(FetchProfile.Item.ENVELOPE);
      fp.add(FetchProfile.Item.CONTENT_INFO);
    } 

    /* Do a prefetch of the header/envelope information */

    tmpEmailConn.currentFolder.getFolder().fetch(msgs,fp);

    endmillis = System.currentTimeMillis();

    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,(endmillis-startmillis) + 
                   " milliseconds to prefetch headers");

    startmillis = System.currentTimeMillis();

    for (int i=0;i<msgs.length;i++) {
      /* Get the Message Number (relative to Folder) */

      msgNum = Integer.toString(msgs[i].getMessageNumber());
      props.put(connHandle + msgNum + ".msgnum",msgNum);

      /* Get the Message Status (System Flags) */

      if (msgs[i].isSet(Flags.Flag.DELETED)) {
        props.put(connHandle + msgNum + ".msgstatus","D");
      } else if ((msgs[i].isSet(Flags.Flag.RECENT)) && 
                (!msgs[i].isSet(Flags.Flag.SEEN))) {
        props.put(connHandle + msgNum + ".msgstatus","N");
      } else if (!msgs[i].isSet(Flags.Flag.SEEN)) {
        props.put(connHandle + msgNum + ".msgstatus","U");
      } else {
        props.put(connHandle + msgNum + ".msgstatus","R");
      }

      /* Get the headers requested */
        
      getMsgHeaders(hr,msgs[i],headerlist);

      msgindex.append(msgNum + " ");
    }

    props.put(connHandle + "msgindex",msgindex.toString());

    endmillis = System.currentTimeMillis();
    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,(endmillis-startmillis) + 
                   " milliseconds to populate properties");
  }

  /* Utility routine to get headers for a single message */

  private static void getMsgHeaders(RewriteContext hr,Message msg,String headerlist) 
                                    throws MessagingException {

    Properties props = hr.request.props;
    String connHandle = getConnHandle(hr);
    String msgNum = Integer.toString(msg.getMessageNumber());
    Enumeration allHeaders;
    StringTokenizer headerNamesParser;
    Vector headerNames = null;
    String hdrName;
    String[] hdrValue;
    StringBuffer hdrValueBuffer;
    Header hdrObject;

    /* Get all of the headers */

    allHeaders = msg.getAllHeaders();

    /* Build our headerlist (if passed in) */

    if (headerlist != null) {
      headerNames = new Vector();
      headerNamesParser = new StringTokenizer(headerlist,", "); 
      
      while (headerNamesParser.hasMoreTokens()) {
        headerNames.addElement(headerNamesParser.nextToken());
      }
    }

    /* Go through the enumeration and put the requested headers into properties */

    while (allHeaders.hasMoreElements()) {
      hdrObject = (Header) allHeaders.nextElement();

      /* Check to see if we are being asked for all headers, or a subset */

      if (headerNames == null || headerNames.contains(hdrObject.getName())) {
        hdrName = hdrObject.getName();
      } else {
        /* Not a header we asked for, go to the next header */

        continue;
      }

      /* Check for special parsing cases */

      if ("To".equals(hdrName)) {
        parseAddrHdr(connHandle,props,msg,"To");
      } else if ("From".equals(hdrName)) {
        parseAddrHdr(connHandle,props,msg,"From");
      } else if ("Cc".equals(hdrName)) {
        parseAddrHdr(connHandle,props,msg,"Cc");
      } else if ("Date".equals(hdrName)) {
        parseDateHdr(connHandle,props,msg);
      } else {
        hdrValue = msg.getHeader(hdrName);
        
        if (hdrValue != null) {
          if (hdrValue.length == 1) {
            props.put(connHandle + msgNum + "." + hdrName,hdrValue[0]);
          } else {
            hdrValueBuffer = new StringBuffer();
            hdrValueBuffer.append(hdrValue[0]);

            for (int i=1;i<hdrValue.length;i++) {
              hdrValueBuffer.append(", ");
              hdrValueBuffer.append(hdrValue[i]);
            }
            props.put(connHandle + msgNum + "." + hdrName,hdrValueBuffer.toString());
          }
        }
      }
    }
  }

  /* Utility method to handle reporting of fatal errors */
 
  private void handleFatalError(RewriteContext hr,String errmsg) {
      handleFatalError(hr,errmsg, null);
  }
  private void handleFatalError(RewriteContext hr,String errmsg, Exception oops) {
    String connHandle = getConnHandle(hr);
    String redirectLocation;
    EmailConnection tmpEmailConn;
    Properties props = hr.request.props;

    String error = oops==null ? "Unknown Exception" : oops.getMessage();
    if (error == null) {
	error = "no message: " + oops;
    }
    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,errmsg + "("+ error +
           ")\n\tcalled from: <" + hr.getBody() +
	   "> [<" + Format.subst(props, hr.getBody()) + ">] " +
	   "\n\tat html tag: " + hr.tagsSeen());

    // XXX for debugging
    if (oops != null && hr.server.logLevel >= Server.LOG_DIAGNOSTIC) {
        oops.printStackTrace();
    }
    int index = error.indexOf(":");
    if (index > 0) {
	error = error.substring(index+1);
    }
    props.put(connHandle + "mailError",error); 
    props.put(connHandle + "fatalMailError",errmsg); 

    /* Try to close things down gracefully */

    tmpEmailConn = (EmailConnection) serverConnections.get(connHandle);

    if (tmpEmailConn != null) {
      tmpEmailConn.isAuthenticated = false;

      try {
        if (tmpEmailConn.currentFolder != null) {
          tmpEmailConn.currentFolder.getFolder().close(false);
        }

        tmpEmailConn.userMsgStore.close(); 
        tmpEmailConn.currentFolder = null;
        tmpEmailConn.userMsgStore = null;
      } catch (MessagingException e) {
        hr.request.log(Server.LOG_ERROR,connHandle, e.getMessage());
      } catch (IllegalStateException e) {
        hr.request.log(Server.LOG_ERROR,connHandle, e.getMessage());
      }

      serverConnections.remove(connHandle);
    }

    /* Check to see if the onError attribute is set for this tag */
    /* If it is set, perform a server-side redirect and tell the */
    /* current RewriteContext to abort processing, otherwise just*/
    /* set the fataMailError property, and let the client take   */
    /* care of calling the <abort> explicitly.                   */

    if ((redirectLocation = hr.get("onError",false)) != null) {
      hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Fatal error redirecting to: " + 
	      redirectLocation);
      hr.abort();

      try {
	String redirect = HttpUtil.urlEncode(Format.subst(props,redirectLocation));
        hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Redirecting to: " + 
	      redirect);
        hr.request.redirect(redirect, null); 
      } catch (IOException e) {
        hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,
                     "IOException while attempting a redirect - Executing standard " + 
                     " props problem recovery");

      }
    } else {
      hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,
                     "Executing standard props problem recovery");

    }
  }

  /* Init method for our Template (this is invoked for *every* HTML page processed) */

  public boolean init(RewriteContext hr) {

    /* Make sure msgPending's initial state is FALSE (used later for determining if
     * we have outgoing email waiting to be sent.
     */

    msgPending = false;

    return true;
  }

  /* Init method for our Handler (invoked *once* only - when the server starts) */

  public boolean init(Server server, String prefix) {

    /* Grab our prefix & session ID for later use */

    this.prefix = prefix;
    session = server.props.getProperty(prefix + "session", "SessionID");

    return true;
  }

  /* Our respond method for the Handler (processes requests for retrieving email 
   * attachments).
   */

  public boolean respond(Request request) throws IOException {
    Hashtable queryData;
    BodyPart tmpPart;
    Properties props = request.props;

    /* Retrieve our query data string */

    queryData = request.getQueryData();
   
    /* Determine if this is a getPart request */

    if (request.url.startsWith(props.getProperty(prefix + "parturl","/getPart"))) {

      /* Use the partNum to retrieve the BodyPart object from the Session Manager */
      request.log(Server.LOG_DIAGNOSTIC, prefix, "Looking for Part " + props.getProperty(session)
	+ " " + queryData.get("PartNum") + " [" + session + "]");

      tmpPart = (BodyPart) SessionManager.get(props.getProperty(session),
                                              queryData.get("PartNum"));

      /* If we have no BodyPart, we can't do anything, return false */

      if (tmpPart == null) {
        request.log(Server.LOG_DIAGNOSTIC, prefix, "no part: " + queryData.get("PartNum"));
        return false;
      }

      try {
        /* Use BodyPart object to stream attachment to browser for handling */

        request.sendResponse(tmpPart.getInputStream(),-1,tmpPart.getContentType(),-1);
     
        /* Return true to denote we've handled this request */

        return true;
      } catch (MessagingException e) {
        /* Nothing we can do with a JavaMail API exception here - just return false */

        return false;
      } catch (NullPointerException e) {
        /* We get here because the browser tries to get images within an HTML attachment */
        /* relative to the 'parturl' URL.  Just return false and let the upstream code   */
        /* handle returning a 404 error.                                                 */

        return false;
      }
    } else {
      /* URL wasn't for us - return false */

      return false;
    }
  }

  /**
   * The &lt;forcetimeout&gt; tag will cause an immediate timeout of the connection
   * to the email server.  This tag is mainly intended for debugging.
   */
 
  public void tag_forcetimeout(RewriteContext hr) {
    String connHandle = getConnHandle(hr);
    EmailConnection tmpEmailConn;

    hr.killToken();

    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Timeout being forced for debugging");

    tmpEmailConn = (EmailConnection) serverConnections.get(connHandle);

    if (tmpEmailConn != null) {
      tmpEmailConn.isAuthenticated = false;
    } else {
      handleFatalError(hr,"Couldn't find a server connection to retrieve!");
    }
  }

  /**
   * Handles the &lt;email&gt; tag.
   */

  public void tag_email(RewriteContext hr) {
    String connHandle = getConnHandle(hr);
    EmailConnection tmpEmailConn;
    Properties props = hr.request.props;

    hr.killToken();

    /* Determine our action */

    if ("open".equals(hr.get("action"))) {
      /* Create a hashtable entry for this connHandle, if we don't have one */

      tmpEmailConn = (EmailConnection) serverConnections.get(connHandle);

      if (tmpEmailConn == null) {
        tmpEmailConn = new EmailConnection(hr);
        serverConnections.put(connHandle,tmpEmailConn);
      } 

      /* open the connection to the server */

      try {
        tmpEmailConn.userMsgStore = defMailSession.getStore(tmpEmailConn.serverURL);
        tmpEmailConn.userMsgStore.connect();
      } catch (NoSuchProviderException e) {
        handleFatalError(hr,"Invalid mail server", e);
      } catch (AuthenticationFailedException e) {
        handleFatalError(hr,"Incorrect username/password", e);
      } catch (MessagingException e) {
        handleFatalError(hr,"Invalid mail server", e);
      } catch (IllegalStateException e) {
        hr.request.log(Server.LOG_ERROR,connHandle,"Mail server already connected");
      }
 
      /* Successfully opened a server connection - we are authenticated */

      tmpEmailConn.isAuthenticated = true;
    } else if ("isconnected".equals(hr.get("action"))) {
      /* Get a copy of the requested server connection */

      tmpEmailConn = (EmailConnection) serverConnections.get(connHandle);

      if (tmpEmailConn == null) {
        handleFatalError(hr,"Couldn't find a server connection to retrieve!");
        return;
      }

      /* Check to see if we are still connected */
  
      if (!tmpEmailConn.isAuthenticated) {
        handleFatalError(hr,"Not authenticated");
        return;
      }
    } else if ("close".equals(hr.get("action"))) {
      tmpEmailConn = (EmailConnection) serverConnections.get(connHandle);

      if (tmpEmailConn != null) {
        try {
          if (tmpEmailConn.currentFolder != null) {
            tmpEmailConn.currentFolder.getFolder().close(false);
/*
java.lang.NullPointerException
   at javax.mail.Flags.<init>(Flags.java:163)
   at com.sun.mail.imap.protocol.MailboxInfo.<init>(MailboxInfo.java:80)
   at com.sun.mail.imap.protocol.IMAPProtocol.examine(IMAPProtocol.java:351)
   at com.sun.mail.imap.IMAPFolder.close(IMAPFolder.java:874)
   at sunlabs.brazil.email.EmailTemplate.tag_email(EmailTemplate.java:950)
   ...
*/
            tmpEmailConn.currentFolder = null;
          }

          tmpEmailConn.userMsgStore.close(); 
	  tmpEmailConn.userMsgStore = null;
        } catch (MessagingException e) {
          props.put(connHandle + "mailError","Error closing INBOX"); 
          hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,
                         "Error closing INBOX for user: " + tmpEmailConn.user);
        } catch (IllegalStateException e) {
          hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,
                         "Error closing INBOX for user: " + tmpEmailConn.user);
        }
        serverConnections.remove(connHandle);
        tmpEmailConn.isAuthenticated = false;
      } else {
        hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,
                       "Couldn't find a server connection to close!");
      }
    }
  }

  /**
   * Handles the &lt;folder&gt; tag.
   */

  public void tag_folder(RewriteContext hr) {
    String connHandle = getConnHandle(hr);
    Properties props = hr.request.props;
    EmailConnection tmpEmailConn;
    String folderName;
    String tmpDefDir;
    Folder[] mailFolders;
    Message[] tmpMsgs;
    Folder tmpFldr;

    hr.killToken();

    /* Get a copy of the requested server connection */

    tmpEmailConn = (EmailConnection) serverConnections.get(connHandle);

    if (tmpEmailConn == null) {
      handleFatalError(hr,"Couldn't find a server connection to retrieve!");
      return;
    }

    /* Check to see if we are still authenticated */

    if (!tmpEmailConn.isAuthenticated) {
      handleFatalError(hr,"Not Authenticated");
      return;
    }

    /* Check to see if we have a mailbox dir specified */

    tmpDefDir = hr.get("dir");

    if (tmpDefDir != null) {
      tmpEmailConn.defaultDir = tmpDefDir;

      hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Setting defaultDir in <folder> tag = " +                     tmpEmailConn.defaultDir);

      try {
        tmpEmailConn.folderRoot = tmpEmailConn.userMsgStore.getFolder(
                                  tmpEmailConn.defaultDir);
      } catch (MessagingException e) {
        handleFatalError(hr,"Folder directory: " + tmpDefDir, e);
        return;
      }
    }

    // XXX if we never defined a folder directory We're dead here!

    /* Check to see if we just want to list folders */

    
    if ("list".equals(hr.get("action"))) {
      String delim = hr.get("delim", "/");
      String glob = hr.get("glob");
      StringBuffer sb = new StringBuffer("INBOX");
      try {
        /* Get an array of the Folders under folderRoot */

        mailFolders = tmpEmailConn.folderRoot.list();

        /* Parse through that list, getting the names of the folders */

        for (int i=0;i<mailFolders.length;i++) {
          String name = mailFolders[i].getName();
	  if (glob!= null && !Glob.match(glob, name)) {
	      continue;
	  }
	  try {
             mailFolders[i].getMessageCount();
	     sb.append(delim).append(name);
	  } catch (Exception ex) {
	     // System.out.println("  BAD: " + name);
             hr.request.log(Server.LOG_ERROR,connHandle,
                       ex.getMessage());
	  }
        }
        props.put(connHandle + "folders",sb.toString());
	// System.out.println("Got: " + connHandle + ".folders" + " => " + sb.toString());
      } catch (FolderNotFoundException e) {
        props.put(connHandle + "mailError","Couldn't find default mail folder dir for: " +
                  tmpEmailConn.user);

        hr.request.log(Server.LOG_ERROR,connHandle,
                       "Couldn't find default mail folder dir for: " + tmpEmailConn.user);
      } catch (MessagingException e) {
        handleFatalError(hr,"Looking for folders", e);
      }
      return;
    }

    /* Get the folder name to operate on */

    folderName = hr.get("foldername");

    if (folderName == null) {
      debug(hr,"NULL foldername passed to the folder tag!");
      return;
    }

    /* Check our special case (INBOX) */

    if (!("INBOX".equalsIgnoreCase(folderName)) && (tmpEmailConn.defaultDir != null)) {
      folderName = tmpEmailConn.defaultDir + "/" + folderName;
    }

    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"folderName in <folder> tag = " + 
                   folderName);
    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"defaultDir in <folder> tag = " + 
                   tmpEmailConn.defaultDir);
 
    /* Check to see if we are creating a new folder.               */
    /*        							   */
    /* Note: the JavaMail API is a little weird here with regard   */
    /*       to how folders are created.  The getFolder() method   */
    /*       of the Store object does not create a folder on the   */
    /*       server - it only retrieves a 'handle' to a folder.    */
    /*       To create a folder, you must first check if it exists */
    /*       already, then call the create() method on the object  */
    /*       returned from the getFolder() call.                   */

    if ("create".equals(hr.get("action"))) {
      try {
        tmpFldr = tmpEmailConn.userMsgStore.getFolder(folderName);

        if (!tmpFldr.exists()) {
            tmpFldr.create(Folder.HOLDS_MESSAGES);
        } else {
          props.put(connHandle + "mailError","Folder: " + folderName + " already exists");

          hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,
                         "Folder: " + folderName + " already exists!");
        }
      } catch (FolderNotFoundException e) {
        props.put(connHandle + "mailError","Unable to create folder: " + folderName);

        hr.request.log(Server.LOG_ERROR,connHandle,"Unable to create folder: " + folderName);
      } catch (MessagingException e) {
        handleFatalError(hr,"folder creation: " + folderName, e);
      }
    }
   
    /* Get the selected folder */

    try {
      doRefile(hr);
      selectFolder(hr,tmpEmailConn,folderName);
    } catch (FolderNotFoundException e) {
      props.put(connHandle + "mailError","Folder: " + folderName + " was not found");

      hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Folder: " + folderName + " not found");
      return;
    } catch (ReadOnlyFolderException e) {
      props.put(connHandle + "mailError","Folder: " + folderName +
	   " is not a valid mailbox");

      hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Folder: " + folderName + e.getMessage());
      return;
    } catch (MessagingException e) {
      handleFatalError(hr,"Selecting folder " + folderName, e);
      return;
    }

    /* Determine our action */

    if ("delete".equals(hr.get("action"))) {
      try {
        /* We must first close the folder */

        tmpEmailConn.currentFolder.getFolder().close(false);

        /* Now remove the folder from the open folders list */
      
        tmpEmailConn.openFolders.remove(folderName);

        /* Now actually delete the folder.                   */
        /* Note: the JavaMail API removes all messages in    */
        /*       the folder before it is deleted.  The false */
        /*       parameter tells the API not to recurse back */
        /*       to parent folders.                          */

        tmpEmailConn.currentFolder.getFolder().delete(false);
      } catch (IllegalStateException e) {
        props.put(connHandle + "mailError","Folder: " + folderName + 
                  " cannot be removed because it is currently open");

        hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Folder: " + folderName + 
                       " cannot be removed because it is current open");
        return;
      } catch (MessagingException e) {
        handleFatalError(hr,"Deleting folder " + folderName, e);
        return;
      }
    } else if ("purge".equals(hr.get("action"))) {
      try {
        tmpMsgs = tmpEmailConn.currentFolder.getFolder().expunge(); 
        tmpEmailConn.currentFolder.lastoldmsgnum -= tmpMsgs.length;
      } catch (IllegalStateException e) {
        props.put(connHandle + "mailError","Folder: " + folderName + 
                  " was in an illegal state");

        hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Folder: " + folderName + 
                       " was in an illegal state");
        return;
      } catch (MessagingException e) {
        handleFatalError(hr,"Purging folder " + folderName, e);
        return;
      }
    } else if ("msgcount".equals(hr.get("action"))) {
      try {
        props.put(connHandle + "totalmsgcount",Integer.toString(
                  tmpEmailConn.currentFolder.getFolder().getMessageCount())); 
        props.put(connHandle + "newmsgcount",Integer.toString(
                  tmpEmailConn.currentFolder.getFolder().getNewMessageCount())); 
      } catch (MessagingException e) {
        handleFatalError(hr,"Counting messages", e);
        return;
      }
    }
  }

  /**
   * Handles the &lt;message&gt; tag.
   */

  public void tag_message(RewriteContext hr) {
    String connHandle = getConnHandle(hr);
    EmailConnection tmpEmailConn;
    String folderName;
    Object msgContent,tmpMsgContent;
    Folder tmpFolder;
    String msgNum;
    int partCount,partIndex;
    StringTokenizer msglist;
    String headerlist,startmsg,msglimit;
    String[] tmpHdr;
    long startmillis,endmillis;
    int[] msgnumbers;
    int i = 0;
    BodyPart tmpPart;
    String tmpToken;
    StringBuffer partcount;
    Message[] tmpMsgs = null;
    RefileInfo tmpRefile = null;
    Properties props = hr.request.props;
    String partURL = hr.get("parturl","/getPart");

    hr.killToken();

    /* Get a copy of the requested server connection */

    tmpEmailConn = (EmailConnection) serverConnections.get(connHandle);

    if (tmpEmailConn == null) {
      handleFatalError(hr,"No server connection");
      return;
    }

    /* Check to see if we are still authenticated */

    if (!tmpEmailConn.isAuthenticated) {
      handleFatalError(hr,"Not authenticated");
      return;
    }

    /* Get the folder name to operate on */

    folderName = hr.get("foldername");

    /* Check our special case (INBOX) */

    if (!("INBOX".equals(folderName)) && !("inbox".equals(folderName))) {
      if ((tmpEmailConn.defaultDir != null) && (folderName != null)) {
        folderName = tmpEmailConn.defaultDir + "/" + folderName;
      }
    }

    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"folderName in <message> tag = " +
                   folderName);
    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"defaultDir in <message> tag = " +
                   tmpEmailConn.defaultDir);

    /* Get the selected folder */

    try {
      selectFolder(hr,tmpEmailConn,folderName);
    } catch (FolderNotFoundException e) {
      props.put(connHandle + "mailError","Folder: " + folderName + " was not found");

      hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Folder: " + folderName + " not found");
      return;
    } catch (ReadOnlyFolderException e) {
      props.put(connHandle + "mailError","Folder: " + folderName +
	   " is not a valid mailbox");

      hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Folder: " + folderName + e.getMessage());
      return;
    } catch (MessagingException e) {
      handleFatalError(hr,"selecting folder " + folderName, e);
      return;
    }

    /* Determine our action */

    if ("getheaders".equals(hr.get("action"))) {


      /* Get the requested message objects and do a prefetch of our requested headers */

      headerlist = hr.get("headerlist"); 

      try {
        startmillis = System.currentTimeMillis();

        tmpMsgs = tmpEmailConn.currentFolder.getMessageObjects(hr); 

        endmillis = System.currentTimeMillis();

        hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,(endmillis-startmillis) + 
                       " milliseconds to retrieve message objects");

        fetchHeaderInfo(hr,tmpMsgs,tmpEmailConn,headerlist);
      } catch (MessagingException e) {
        handleFatalError(hr,"Fetching messages", e);
        return;
      }
    } else if ("getnewheaders".equals(hr.get("action"))) {
      /* Get the new message objects and do a prefetch of our requested headers */

      headerlist = hr.get("headerlist"); 

      try {
        tmpMsgs = tmpEmailConn.currentFolder.getNewMessageObjects(hr); 

        if (tmpMsgs != null) {
          fetchHeaderInfo(hr,tmpMsgs,tmpEmailConn,headerlist);
        }
      } catch (MessagingException e) {
        handleFatalError(hr,"Fetching new messages", e);
        return;
      }
    } else if ("getmsg".equals(hr.get("action"))) {
      msgNum = hr.get("msgnum");
      headerlist = hr.get("headerlist");
      hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"MsgNum = " + msgNum);

      /* Get the message & headers requested */

      try {
        tmpMsgs = tmpEmailConn.currentFolder.getMessageObjects(hr,msgNum); 
        getMsgHeaders(hr,tmpMsgs[0],headerlist);
      } catch (MessagingException e) {
        handleFatalError(hr,"Fetching message headers", e);
        return;
      }

      /* Put the message server's msg number for this message into a return prop */

      props.put(connHandle + "msgnum",Integer.toString(tmpMsgs[0].getMessageNumber()));

      try {
        /* Get the content type and put it into a property */

        props.put(connHandle + msgNum + ".msgmimetype",tmpMsgs[0].getContentType());

        /* Get the content object for the request message */

        msgContent = tmpMsgs[0].getContent();

        /* Determine if it is a multipart message */ /* XXXX */
       
        if (msgContent instanceof String) {
          props.put(connHandle + msgNum + ".body",msgContent);
        } else if (msgContent instanceof Multipart) {
          partcount = new StringBuffer();
    
          /* Take care of the body */

          tmpMsgContent = (((Multipart) msgContent).getBodyPart(0)).getContent();

          if (tmpMsgContent instanceof String) {
            props.put(connHandle + msgNum + ".body",tmpMsgContent);
            partIndex = 1;
          } else {
            partIndex = 0;
          }

          /* Populate multipart attachment properties - and put our objects into
           * the sessionManager for the handler to pickup. 
           */

          partCount = ((Multipart) msgContent).getCount();

          for (;partIndex<partCount;partIndex++) {
            tmpPart = ((Multipart) msgContent).getBodyPart(partIndex);

            /* put our BodyPart object into the session manager for the
             * Handler to find
             */

            SessionManager.put(hr.sessionId,"Part" + partIndex,tmpPart);
            hr.request.log(Server.LOG_DIAGNOSTIC, hr.prefix,
                       "creating part: " + hr.sessionId + " " + "Part" +  partIndex);

            /* Populate the properties for partname & parturl */

            if (tmpPart.getFileName() != null) {
              props.put(connHandle + msgNum + "." + partIndex + 
                        ".partname",tmpPart.getFileName());
              props.put(connHandle + msgNum + "." + partIndex + ".parturl",
                        partURL + "/" + tmpPart.getFileName());
            } else if (tmpPart.getDescription() != null) {
              props.put(connHandle + msgNum + "." + partIndex + 
                        ".partname",tmpPart.getDescription());
              props.put(connHandle + msgNum + "." + partIndex + ".parturl",
                        partURL + "/" + tmpPart.getDescription());
            } else {
              props.put(connHandle + msgNum + "." + partIndex + 
                        ".partname","No Name");
              props.put(connHandle + msgNum + "." + partIndex + ".parturl",
                        partURL + "/NoName");
            }
         
            /* Build our partcount property */

            partcount.append(partIndex + " ");
          }
          if (!partcount.toString().equals("")) {
            props.put(connHandle + msgNum + ".partcount",partcount.toString());
          }
        }
      } catch (IOException e) {
        props.put(connHandle + "mailError","Error retrieving mail message content");

        hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,
                       "Error retrieving mail message content");
        return;
      } catch (MessagingException e) {
        handleFatalError(hr,"Extracting message body", e);
        return;
      }
    } else if ("undeletemsg".equals(hr.get("action"))) {
      /* Mark the msgnum(s) as deleted */
      
      msgNum = hr.get("msgnum");
      msglist = new StringTokenizer(msgNum,", ");
      msgnumbers = new int[msglist.countTokens()];

      hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Messages to undelete: " + msgNum);

      while (msglist.hasMoreTokens()) {
        try {
          msgnumbers[i] = Integer.parseInt(msglist.nextToken());
          i++;
        } catch (NumberFormatException e) {
          /* Just skip this particular token */
    
          continue;
        }
      }

      try {
        tmpEmailConn.currentFolder.getFolder().setFlags(msgnumbers,
                                               new Flags(Flags.Flag.DELETED),false);
      } catch (MessagingException e) {
        handleFatalError(hr,"Deleting messages", e);
        return;
      }
    } else if ("deletemsg".equals(hr.get("action"))) {
      /* Mark the msgnum(s) as deleted */
      
      msgNum = hr.get("msgnum");
      msglist = new StringTokenizer(msgNum,", ");
      msgnumbers = new int[msglist.countTokens()];

      hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Messages to delete: " + msgNum);

      while (msglist.hasMoreTokens()) {
        try {
          msgnumbers[i] = Integer.parseInt(msglist.nextToken());
          i++;
        } catch (NumberFormatException e) {
          /* Just skip this particular token */
    
          continue;
        }
      }

      try {
        tmpEmailConn.currentFolder.getFolder().setFlags(msgnumbers,
                                               new Flags(Flags.Flag.DELETED),true);
      } catch (MessagingException e) {
        handleFatalError(hr,"Deleting messages", e);
        return;
      }
    } else if ("refilemsg".equals(hr.get("action"))) {
      folderName = hr.get("refilefolder");
      msgNum = hr.get("msgnum");

      /* Check to see if we already have a record for this folder */

      tmpRefile = (RefileInfo) refileFolders.get(folderName);

      if (tmpRefile != null) {
        tmpRefile.msgList.append(" ").append(msgNum);
      } else {
        refileFolders.put(folderName,new RefileInfo(tmpEmailConn,new StringBuffer(msgNum))); 
      }
    }
  }

  /* Utility routine to build up the common parts of our MimeMessage */

  private boolean buildMimeMessage(RewriteContext hr) throws MessagingException {
    String connHandle = getConnHandle(hr);
    String toStr;
    String ccStr;
    String fromStr;
    String subjectStr;
    String msgBody;
    MimeBodyPart tmpBodyPart;
    Properties props = hr.request.props;

    /* Get our necessary arguments - set connHandle.sendmailerror if we are 
     * missing any that are mandatory
     */

    toStr = hr.get("to");

    if (toStr == null) {
     props.put(connHandle + "sendmailerror","Missing 'to' field");
     hr.request.log(Server.LOG_ERROR,connHandle,"Missing 'to' field in <sendmail>");
     return false;
    }

    fromStr = hr.get("from");

    if (fromStr == null) {
     props.put(connHandle + "sendmailerror","Missing 'from' field");
     hr.request.log(Server.LOG_ERROR,connHandle,"Missing 'from' field in <sendmail>");
     return false;
    }

    subjectStr = hr.get("subject");
    ccStr = hr.get("cc");

    /* Get our message body */

    msgBody = hr.get("body");
    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Message body = " + msgBody);

    /* Create our sendMsg object */

    sendMsg = new MimeMessage(defMailSession);

    /* Fill in the necessary message parts */

    sendMsg.setHeader("To",toStr);
    sendMsg.setHeader("From",fromStr);
  
    if (subjectStr != null) {
      sendMsg.setSubject(subjectStr);
    } else {
      sendMsg.setSubject("(no subject)");
    }

    if (ccStr != null) {
      sendMsg.setHeader("Cc",ccStr);
    }

    String bccStr = hr.get("bcc");
    if (bccStr != null && !bccStr.equals("")) {
      sendMsg.setHeader("Bcc",bccStr);
    }

    if (sendMultipart == null) {
      sendMultipart = new MimeMultipart();
    }

    tmpBodyPart = new MimeBodyPart();

    if (msgBody != null) {
      tmpBodyPart.setText(msgBody);
    } else {
      tmpBodyPart.setText("");
    }

    sendMultipart.addBodyPart(tmpBodyPart,0);

    /* Succeeded in building the message - return true */

    return true;
  }

  /**
   * Handles the &lt;sendmail&gt; tag.
   */

  public void tag_sendmail(RewriteContext hr) {
    String connHandle = getConnHandle(hr);
    Properties props = hr.request.props;

    /* Check to see if we have mail from a previous <sendmail> pending */
    /* If so, send that before processing the current <sendmail> tag   */

    tag_slash_sendmail(hr);
    debug(hr);

    /* Build up the common parts (to, from, subject, cc, body) of our msg */

    try { 
      if (!buildMimeMessage(hr)) {
        return;
      }
    } catch (IllegalWriteException e) {
      props.put(connHandle + "mailError","Unable to set outgoing mail headers");

      hr.request.log(Server.LOG_WARNING,connHandle,"Unable to set outgoing mail headers");
      return;
    } catch (MessagingException e) {
      handleFatalError(hr,"sending message", e);
      return;
    }

    if (hr.isSingleton()) {
      msgPending = false;

      try {
        sendMsg.setContent(sendMultipart);
        Transport.send(sendMsg);
        hr.request.log(Server.LOG_WARNING,connHandle, "Sending mail");
        sendMultipart = null;
      } catch (SendFailedException e) {
        hr.request.log(Server.LOG_WARNING,connHandle,
	     "Unable to send mail: " + e.getMessage());
        props.put(connHandle + "mailError","Unable to send outgoing mail message");

      } catch (MessagingException e) {
        handleFatalError(hr,"sending message", e);
        hr.request.log(Server.LOG_WARNING,connHandle,
	     "Unexpected error when sending mail: " + e.getMessage());
        return;
      }
    } else {
      msgPending = true;
    }
  }

  /**
   * Handles the &lt;/sendmail&gt; tag.
   */

  public void tag_slash_sendmail(RewriteContext hr) {
    String connHandle = getConnHandle(hr);
    Properties props = hr.request.props;

    hr.killToken();
    debug(hr);

    if (msgPending) {
      msgPending = false;

      try {
        sendMsg.setContent(sendMultipart);
        Transport.send(sendMsg);
        hr.request.log(Server.LOG_WARNING,connHandle, "Sending mail");
        sendMultipart = null;
      } catch (SendFailedException e) {
        props.put(connHandle + "mailError","Unable to send outgoing mail message");

        hr.request.log(Server.LOG_WARNING,connHandle,
                       "Unable to send outgoing mail message: " + e.getMessage());
      } catch (Exception e) {
        handleFatalError(hr,"sending message", e);
        hr.request.log(Server.LOG_WARNING,connHandle,
                       "Unexpected error sending mail: " + e.getMessage());
        return;
      }
    } else {
      debug(hr,"Ooops, mail already sent!");
    }
  }

  /* Private utility class to convert a String to a byte array       */
  /* Note to future developers:  We *do* need this code - it acts    */
  /* like the old getBytes() method, which returns the lower 8 bits  */
  /* of each character in the String (instead of the current         */
  /* getBytes(), which uses the default locale's encoding scheme.    */
  /* We need this to be able to correctly construct MultiPart        */
  /* messages that have had content pre-encoded.                     */

  private static byte[] getBytes(String s) {
    char [] chars= s.toCharArray();
    int size = chars.length;
    byte[] bytes = new byte[size];

    for (int i = 0; i < size;) {
      bytes[i] = (byte) chars[i++];
    }

    return bytes;
  }

  /**
   * Handles the &lt;part&gt; tag.
   */

  public void tag_part(RewriteContext hr) {
    String connHandle = getConnHandle(hr);
    Properties props = hr.request.props;
    MimeBodyPart tmpBodyPart;
    String contentType;
    String partData;
    String partName;
    String partEncoding;
    byte[] partDataBytes;

    hr.killToken();
    debug(hr);

    /* Check to see if we need to create a new MimeMultipart */

    if (sendMultipart == null) {
      sendMultipart = new MimeMultipart();
    }

    /* Collect our parameter values */

    partData = hr.get("body");

    if (partData == null) {
      props.put(connHandle + "sendmailerror","Missing 'body' field in <part>");
      hr.request.log(Server.LOG_ERROR,connHandle,"Missing 'body' field in <part>");
      return;
    }

    partEncoding = hr.get("encoding");

    if (partEncoding == null) {
      props.put(connHandle + "sendmailerror","Missing 'encoding' field in <part>");
      hr.request.log(Server.LOG_ERROR,connHandle,"Missing 'encoding' field in <part>");
      return;
    } 

    partName = hr.get("name");

    if (partName == null) {
      partName = "No Name";
    }

    contentType = hr.get("content-type"); 

    if (contentType == null) {
      contentType = "application/octet-stream";
    }
    
    /* Get the bytes from our String partData */

    partDataBytes = getBytes(partData);

    /* Now let's create a tmp MimeBodyPart and populate it */

    try {
      tmpBodyPart = new MimeBodyPart(new InternetHeaders(),partDataBytes);

      /* Now setup whatever headers we need */

      tmpBodyPart.setHeader("Content-Type",contentType);
      tmpBodyPart.setFileName(partName);
      tmpBodyPart.setDescription(partName);
     
      if (!partEncoding.equals("none")) {
        tmpBodyPart.setHeader("Content-Transfer-Encoding",partEncoding);
      }

      /* Attach this tmpBodyPart to our current sendMultipart */

      sendMultipart.addBodyPart(tmpBodyPart);
    } catch (MessagingException e) {
      handleFatalError(hr,"creating message to send", e);
      return;
    }
  }

  /* Private utility class to process pending message refiles */

  private void doRefile(RewriteContext hr) {
    String connHandle = getConnHandle(hr);
    Properties props = hr.request.props;
    Enumeration keys = null;
    RefileInfo tmpRefile = null;
    String tmpKey = null;
    Folder tmpFolder = null;
    Message[] tmpMsgs = null;

    /* Check to see if we need to process any pending msg refiles */

    if (!refileFolders.isEmpty()) {
      keys = refileFolders.keys();

      while (keys.hasMoreElements()) {
        tmpKey = (String) keys.nextElement();
        tmpRefile = (RefileInfo) refileFolders.get(tmpKey);

        /* Check to see if we are still authenticated */

        if (!tmpRefile.tmpEmailConn.isAuthenticated) {
          handleFatalError(hr,"Refiling message (not authenticated)");
          return;
        }

        try {
          /* Get the folder to move to */

          hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Refile Folder: " + tmpKey);

          if (tmpRefile.tmpEmailConn.defaultDir != null) {
            tmpFolder = tmpRefile.tmpEmailConn.userMsgStore.getFolder(
                        tmpRefile.tmpEmailConn.defaultDir + "/" + tmpKey);
          } else {
            tmpFolder = tmpRefile.tmpEmailConn.userMsgStore.getFolder(tmpKey);
          }

          hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Messages to move: " + 
                         tmpRefile.msgList);

          /* Copy the messages and mark them as deleted */

          tmpMsgs = tmpRefile.tmpEmailConn.currentFolder.getMessageObjects(hr,
                    tmpRefile.msgList.toString().trim());

          tmpRefile.tmpEmailConn.currentFolder.getFolder().copyMessages(tmpMsgs,tmpFolder);
          tmpRefile.tmpEmailConn.currentFolder.getFolder().setFlags(tmpMsgs,
                                                           new Flags(Flags.Flag.DELETED),
                                                           true);
        } catch (FolderNotFoundException e) {
          props.put(connHandle + "mailError","Folder: " + tmpKey + " was not found");

          hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"Folder: " + tmpKey + " not found");
        } catch (MessagingException e) {   
          /* Fatal mail error */
	   
	  /*
	   * XXX this can happen for bogus folders.  Try to guess, if thats the case, 
	   * and don't throw a fatal error (SAU)
	   */
	
	  if (e.toString().indexOf("NO COPY") != -1) {
            hr.request.log(Server.LOG_ERROR,connHandle,"Bogus re-file folder: " + 
		 tmpKey);
            props.put(connHandle + "mailError","Folder: " + tmpKey +
	         " is not a valid mailbox");
	  } else {
            handleFatalError(hr,"Refiling message(s)", e);
	  }
        }
      }

      /* Clear our refile hashtable for next time */

      refileFolders.clear();
    }
  }

  /**
   * If we run off the end of the page, but there is email pending
   * to be sent, send it anyway.  Also, check to see if we have pending
   * message refiles that need to happen.
   */

  public boolean done(RewriteContext hr) {
    /* Check to see if we need to process any pending outgoing mail */
 
    if (msgPending) {
      tag_slash_sendmail(hr);
    }

    /* Check to see if we need to process any pending message refiles */

    doRefile(hr);

    return true;
  }

/* An internal class used to keep track of refile information for a particular
 * connHandle.
 */

private static class RefileInfo {

  /* Instance variables */

  EmailConnection tmpEmailConn;
  StringBuffer msgList;

  public RefileInfo(EmailConnection tmpConn,StringBuffer messages) {

    tmpEmailConn = tmpConn;
    msgList = messages; 
  }
}

/* An internal class used to keep track of connection information for 
 * a particular connHandle.
 */

private class EmailConnection {
  
  /* Instance variables */

  String user;
  String defaultDir;
  URLName serverURL; 
  Store userMsgStore;
  FolderInfo currentFolder;
  Hashtable openFolders;
  Folder folderRoot;
  boolean isAuthenticated;

  public EmailConnection(RewriteContext hr) {
    String connHandle = getConnHandle(hr);
    PasswordAuthentication pa;
    final String IMAP_PORT = "143";
    String tmpServer;
    String server;
    String port;

    /* Setup our internal values */

    user = hr.get("user");
    tmpServer = hr.get("server");

    if (tmpServer.indexOf(":") != -1) {
      server = tmpServer.substring(0,tmpServer.indexOf(":"));
      port = tmpServer.substring(tmpServer.indexOf(":")+1);
    } else {
      server = tmpServer;
      port = IMAP_PORT;
    }

    openFolders = new Hashtable();

    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"user = " + user);
    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"server = " + server);
    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"port = " + port);

    /* Create our URLName for the server connection */

    serverURL = new URLName("imap://" + user + "@" + server + ":" + port);

    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"serverURL = " + serverURL.toString());

    pa = new PasswordAuthentication(user,hr.get("password"));

    /* Register our authentication object with the mail session */

    defMailSession.setPasswordAuthentication(serverURL,pa);
  }
}

/* An internal class used to keep track of the message cache and new messages
 * for a particular folder.
 */

private static class FolderInfo {

  /* Instance variables */

  private Folder myFolder;
  int lastoldmsgnum;
  boolean newFolder;

  /* Our Constructor */

  public FolderInfo(Folder fldr) throws MessagingException {

    /* Keep our internal state */

    myFolder = fldr;

    /* Open our folder (if necessary) */

    if (!myFolder.isOpen()) {
      myFolder.open(Folder.READ_WRITE);
    }

    newFolder = true;
  }

  /* return an instance of our folder */

  public Folder getFolder() {
    return myFolder;
  }
 
  /* Utility routine to get new message objects */

  public Message[] getNewMessageObjects(RewriteContext hr) throws MessagingException {
    String connHandle = getConnHandle(hr);
    Message[] tmpMsgs = null;
    int currentMsgCount;

    /* Get our current msg count */

    currentMsgCount = myFolder.getMessageCount();

    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,
                   "getNewMessageObjects(): currentCount = " +
                   currentMsgCount + " - lastoldmsgnum = " + lastoldmsgnum);

    /* Compare our lastoldmsgnum with the current message count -   */
    /* If they are the same, we have no newly arrived messages      */
    /* If lastoldmsgnum is greater than our current count (or it is */
    /* a 'new' folder) - return all of the header objs.             */
    /* If neither of these cases is true, return all of msgs        */
    /* that have arrived since lastoldmsgnum.                       */

    if (newFolder || (lastoldmsgnum > currentMsgCount)) {
      lastoldmsgnum = myFolder.getMessageCount();
      tmpMsgs = myFolder.getMessages();
      newFolder = false;
    } else if (lastoldmsgnum == currentMsgCount) {
      return null;
    } else {
      tmpMsgs = myFolder.getMessages(lastoldmsgnum+1,currentMsgCount);
      lastoldmsgnum = currentMsgCount;
    }

    return tmpMsgs;
  }

  /* Utility routine to get non-new message objects from a space seperated 
   * list of inputs.
   */

  public Message[] getMessageObjects(RewriteContext hr,String msglist) 
                                     throws MessagingException {
    String connHandle = getConnHandle(hr);
    Properties props = hr.request.props;
    int[] msgnums = null;
    int i = 0;
    StringTokenizer msgtokens;
    Message[] tmpMsgArray;

    /* Break up our msglist and put the values into an int array */

    msgtokens = new StringTokenizer(msglist,", ");
    msgnums = new int[msgtokens.countTokens()]; 

    while (msgtokens.hasMoreTokens()) {
      try {
        msgnums[i] = Integer.parseInt(msgtokens.nextToken());
        i++;
      } catch (NumberFormatException e) {
        /* Just skip this particular token */
   
        continue;
      }
    }

    /* Now make one call to the mail server to get these message objects */
  
    try {
       tmpMsgArray = myFolder.getMessages(msgnums);
    } catch (IndexOutOfBoundsException e) {
       props.put(connHandle + "mailError", "Message have vanished!");
       throw new MessagingException(e.getMessage());
    }

    /* Return our tmpMsgArray */

    return tmpMsgArray; 
  }

  /* Utility routine to get non-new message objects using a startmsg and 
   * msglimit.
   */

  public Message[] getMessageObjects(RewriteContext hr)
	    throws MessagingException {

    int startmsgnum,msglimitnum,prevmsgnum,nextmsgnum;
    String connHandle = getConnHandle(hr);
    Properties props = hr.request.props;
    int msgCount = myFolder.getMessageCount();

    /* Check to see if the folder is empty */

    if (msgCount == 0) {
      return null;
    }

    String startmsg = hr.get("startmsg");
    if (startmsg != null && startmsg.length()==0) {
        startmsg=null;
    }
    String msglimit = hr.get("msglimit");
    if (msglimit != null && msglimit.length()==0) {
        msglimit=null;
    }

    /* Convert our startmsg and msglimit strings to ints */

    try {
      startmsgnum = Integer.parseInt(startmsg);
    } catch (NumberFormatException e) {
      startmsgnum = 0;
    }

    try {
      msglimitnum = (Integer.parseInt(msglimit)-1);
    } catch (NumberFormatException e) {
      msglimitnum = 0;
    }

    /* Adjust message limits */

    if (msglimitnum == 0 || msglimitnum > msgCount) {
      msglimitnum = msgCount;   
    }

    if (startmsgnum == 0 || startmsgnum > msgCount) {
      if (msglimitnum == msgCount) {
        startmsgnum = (msgCount - msglimitnum) + 1;
      } else {
        startmsgnum = (msgCount - msglimitnum);
      }
    }     

    if ((startmsgnum + msglimitnum) > msgCount) {
      msglimitnum = (msgCount - startmsgnum);
    }

    /* Generate our previous and next message values */

    prevmsgnum = Math.max(1,(startmsgnum - msglimitnum)) - 1;
    nextmsgnum = Math.min(msgCount,(startmsgnum + msglimitnum)) + 1;

    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"prevmsgnum = " + prevmsgnum);
    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"nextmsgnum = " + nextmsgnum);

    /* Decided if we need to populate the prev and next msg props */

    if (startmsgnum != 1) {
      if (prevmsgnum == 0 && nextmsgnum < msgCount) {
        props.put(connHandle + "prevmsgnum",Integer.toString(prevmsgnum)+1);
      } else if (prevmsgnum > 0) {
        props.put(connHandle + "prevmsgnum",Integer.toString(prevmsgnum));
      }
    }

    if (nextmsgnum < msgCount) {
      props.put(connHandle + "nextmsgnum",Integer.toString(nextmsgnum));
    }

    /* Return the requested message(s) */

    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"startmsg = " + startmsgnum);
    hr.request.log(Server.LOG_DIAGNOSTIC,connHandle,"endmsgnum = " + 
                  (startmsgnum + msglimitnum));

    return myFolder.getMessages(startmsgnum,(startmsgnum + msglimitnum));
  }
}
}
