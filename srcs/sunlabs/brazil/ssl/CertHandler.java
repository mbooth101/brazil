/*
 * CertHandler.java
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
 * The Initial Developer of the Original Code is: suhler.
 * Portions created by suhler are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): suhler.
 *
 * Version:  2.4
 * Created by suhler on 01/07/19
 * Last modified by suhler on 04/11/30 15:12:12
 *
 * Version Histories:
 *
 * 2.4 04/11/30-15:12:12 (suhler)
 *   fixed sccs version string
 *
 * 2.3 04/11/30-15:11:43 (suhler)
 *   fixed sccs version string
 *
 * 2.2 04/11/30-15:11:27 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:39:50 (suhler)
 *   version change
 *
 * 1.3 02/01/29-14:24:11 (suhler)
 *   doc lint
 *
 * 1.2 01/07/19-20:17:18 (suhler)
 *   docs
 *
 * 1.2 01/07/19-11:18:18 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 ssl/CertHandler.java
 *
 * 1.1 01/07/19-11:18:17 (suhler)
 *   date and time created 01/07/19 11:18:17 by suhler
 *
 */

package sunlabs.brazil.ssl;

import iaik.asn1.ASN1;
import iaik.asn1.CodingException;
import iaik.asn1.ObjectID;
import iaik.asn1.structures.AlgorithmID;
import iaik.asn1.structures.Name;
import iaik.pkcs.PKCS7CertList;
import iaik.pkcs.PKCSException;
import iaik.pkcs.pkcs8.EncryptedPrivateKeyInfo;
import iaik.pkcs.pkcs8.PrivateKeyInfo;
import iaik.security.provider.IAIK;
import iaik.utils.KeyAndCertificate;
import iaik.utils.Util;
import iaik.x509.NetscapeCertRequest;
import iaik.x509.X509Certificate;
import iaik.x509.extensions.netscape.NetscapeCertType;
import iaik.x509.extensions.netscape.NetscapeComment;
import iaik.x509.extensions.netscape.NetscapeSSLServerName;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Random;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

/**
 * Handler for issuing A netscape certificate.
 * Get a certificate request from the user as form data, generate the
 * certificate, and sign it with the CA's certificate.
 *
 * There is currently no certificate management.  The serial number is obtained
 * from the form (if available), otherwise it is made up.
 * [This should be converted into a template].
 *
 * The following fields are required query info, either from a GET or POST:
 * <dl>
 * <dt>	commonname	<dd> The name of the certificate owner
 * <dt>	country		<dd> The country of origin (2 char abbreviation
 * <dt>	email		<dd> The email address
 * <dt>	expires		<dd> The cert lifetime (in months)
 * <dt>	key		<dd> The netscape key information (from KEYGEN)
 * <dt>	locality	<dd> The local address of the cert holder
 * <dt>	org		<dd> The company
 * <dt>	orgunit		<dd> The division
 * <dt>	state		<dd> The state or provice
 * </dl>
 * The following fields are optional:
 * <dl>
 * <dt> serial		<dd> the cert serial # (this is temporary)
 * <dt> can_sign	<dd> cert may be used for code signing
 * <dt> can_email	<dd> cert may be used for S/mime
 * <dt> can_ssl		<dd> cert may be used for client SSL
 * </dl>
 *
 * The follow server properties are used:
 * <dl class=props>
 * <dt> prefix	<dd> The url prefix for this handler to generate a cert
 * <dt> cert	<dd> The file name containing the CA certificate
 * <dt> id	<dd> If set,  The the request property "id" MUST be set
 *		     in order to generate a cert.  This allows upstream
 *		     handlers to do authentication before a cert is issued.
 * </dl>
 *
 * @author		Stephen Uhler
 * @version	2.4
 */

public class CertHandler implements Handler {
    X509Certificate[] serverChain;	// our CA's cert chain
    PrivateKey serverKey;		// our CA's private key
    String prefix;			// url prefix
    boolean needID = false;		// if true, an id property is required
    static long serialNo;		// default serial num.

    /**
     * Get the CA's certificate chain. prompting for the
     * certificate password on the command line.
     */

    public boolean
    init(Server server, String prefix) {
	this.prefix = server.props.getProperty(prefix + "prefix","/");
	String cert =  server.props.getProperty(prefix + "cert");
	// Temporarily start at seconds since 1/1/99
	serialNo = System.currentTimeMillis()/1000 - 915177600;
	System.out.println("Starting serialno: " + serialNo);
	needID = (server.props.getProperty(prefix + "id") != null);
	Security.addProvider(new IAIK());
	KeyAndCertificate kac;

	/*
	 * read the certificate from a file
	 */

	try {
	    kac = new KeyAndCertificate(cert);
	} catch (IOException e) {
	    System.out.println("Oops: " + e);
	    e.printStackTrace();
	    return false;
	}
	serverChain = kac.getCertificateChain();
	System.out.println("SERVER CHAIN ---------------------------");
	for (int i = 0; i<serverChain.length;i++) {
	    System.out.println("Cert:\n" + serverChain[i].toString(true));
	}
	System.out.println("END SERVER CHAIN ---------------------------");

	/*
	 * Prompt the user for the password, and decrypt the key
	 */

	EncryptedPrivateKeyInfo epk =
	    (EncryptedPrivateKeyInfo) kac.getPrivateKey();
	System.out.println("CaHandler Key: " + epk);
	String passwd = getPassword(cert);
	try {
	    serverKey = epk.decrypt(passwd);
	} catch (Exception e) {	// stupid exceptions get thrown with bad keys
	    System.out.println("Error decrypting key Server's key: " + e);
	    return false;
	} finally {
	    passwd = null;	// hide the password
	    System.gc();
	}
	return true;
    }

    /**
     * Specify the required query parameters
     */

    static String[] required = {
	"commonname",
	"country",
	"email", 
	"expires",
	"key",
	"locality",
	"org", 
	"orgunit",
	// "serial",
	"state",
    };

    public boolean
    respond(Request request) throws IOException {

	/*
	 * See if mine
	 */

	if (!request.url.startsWith(prefix)) {
	    return false;
	}

	/*
	 * Make sure request is validated
	 */

	String id = request.props.getProperty("id");
	if (needID && id == null) {
	    String msg ="Invalid credentials supplied with certificate request";
	    msg += "  (" + request.props.getProperty("error","??") + ")";
	    request.sendError(400, msg);
	    return true;
	}

	Hashtable h = request.getQueryData();

	/*
	 * make sure we have all needed info
	 */

	for (int i=0;i<required.length;i++) {
	   if (!h.containsKey(required[i])) {
		request.sendError(400, "Missing value: " + required[i]);
		return true;
	    }
	}

	/*
	 * Get the subject and issuer info XXX check info
	 */

	Name subject = new Name();
	subject.addRDN(ObjectID.country, (String) h.get("country"));
	subject.addRDN(ObjectID.organization ,(String) h.get("org"));
	subject.addRDN(ObjectID.organizationalUnit, (String) h.get("orgunit"));
	subject.addRDN(ObjectID.commonName ,(String) h.get("commonname"));
	subject.addRDN(ObjectID.emailAddress ,(String) h.get("email"));
	subject.addRDN(ObjectID.stateOrProvince,(String) h.get("state"));
	subject.addRDN(ObjectID.locality,(String) h.get("locality"));

	X509Certificate cert = new X509Certificate();
	if (h.containsKey("serial")) {
	    cert.setSerialNumber(new BigInteger((String) h.get("serial")));
	} else {
	    cert.setSerialNumber(new BigInteger("" + serialNo++));
	}
	cert.setSubjectDN(subject);
	cert.setIssuerDN(serverChain[0].getIssuerDN());

	System.out.println("User cert request generated");
	// System.out.println("Dummy cert request " + cert.toString());

	/*
	 * Get the signed public key from the user.
	 * This is the base64 decoded string that came back from
	 * netscape's KEYGEN param, the SignedPublicKeyAndChallenge.
	 */

	String key = (String) h.get("key");
	byte bytes[] = Util.Base64Decode(key.getBytes());
	NetscapeCertRequest nc = null;
	try {
	    nc = new  NetscapeCertRequest(bytes);
	} catch (CodingException e) {
	    System.out.println("OOPS " + e);
	    e.printStackTrace();
	}
	try {
	    nc.verify();
	} catch (java.security.SignatureException e) {
	    System.out.println("OOPS " + e);
	    e.printStackTrace();
	}
	System.out.println("Got cert req: " + nc);
	try {
	    cert.setPublicKey(nc.getPublicKey());
	} catch (java.security.InvalidKeyException e) {
	    System.out.println("OOPS " + e);
	    e.printStackTrace();
	}

	/*
	 * set up the validity dates
	 */

	GregorianCalendar date = new GregorianCalendar();
	date.add(Calendar.DATE, -1);
	cert.setValidNotBefore(date.getTime());  
	date.add(Calendar.MONTH, 
		Integer.parseInt((String) h.get("expires")));
	cert.setValidNotAfter(date.getTime());

	/*
	 * Add in any cert options.  If none are specified, then the default, 
	 * which is everything but object signing is used.
	 */
	
	int options = 0;
	if (h.get("can_sign") != null) {
	    options |= NetscapeCertType.OBJECT_SIGNING;
	}
	if (h.get("can_email") != null) {
	    options |= NetscapeCertType.S_MIME;
	}
	if (h.get("can_ssl") != null) {
	    options |= NetscapeCertType.SSL_CLIENT;
	}
	if (options != 0) {
	    cert.addExtension(new NetscapeCertType(options));
	}

	/*
	 * This is the private key out of the server's certificate, 
	 */

	System.out.println("About to sign cert");
	try {
	    cert.sign(AlgorithmID.md5WithRSAEncryption, serverKey);
	} catch (InvalidKeyException e) {
	    System.out.println("OOPS " + e);
	    e.printStackTrace();
	} catch (NoSuchAlgorithmException e) {
	    System.out.println("OOPS " + e);
	    e.printStackTrace();
	} catch (CertificateException e) {
	    System.out.println("OOPS " + e);
	    e.printStackTrace();
	}

	System.out.println("Generated CERT:" + cert.toString(true));

	/*
	 * Construct a chain by adding on the server chain
	 */

	X509Certificate[] chain = new X509Certificate[serverChain.length+1];
	for (int i = 0; i<serverChain.length; i++) {
	    chain[i+1] = serverChain[i];
	}
	chain[0] = cert;

	/*
	 * Turn into a pkcs certificate chain, and deliver to the client
	 */

	PKCS7CertList pkcs7 = new PKCS7CertList();
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	try {
	    pkcs7.createCertificateList(chain);
	    pkcs7.writeTo(bos);
	} catch (iaik.pkcs.PKCSException e) {
	    System.out.println("OOPS " + e);
	    request.sendError(400, "Sorry, can't create certificate");
	    return true;
	}
	byte[] content = bos.toByteArray();
	request.sendHeaders(200, "application/x-x509-user-cert",content.length);
	request.out.write(content);

	// XXX we should record the certificate info here

	return true;
    }

    /**
     * Get a password from the command line
     */

    static String
    getPassword(String msg) {
	System.out.print(msg + "\nEnter password: ");
	String passwd = "";
	try {
	    passwd = (new DataInputStream(System.in)).readLine();
	} catch (IOException e) {}
	System.out.println("\033[A\r                                        ");
	return passwd;
    }

    /**
     * Get a string from the user on stdin
     */

    static String
    getEntry(String prompt, String dflt) {
	String value = "";
	System.out.print(prompt + " [" + dflt + "]: ");
	try {
	    value = (new DataInputStream(System.in)).readLine();
	} catch (IOException e) {}
	if (value.trim().equals("")) {
	    return dflt;
	} else {
	    return value;
	}
    }

    /**
     * Generate a sample self-signed server certificate to use for
     * signing client certificate requests.  We'll choose an arbitrary
     * suite of algorithms.
     */

    public static void main(String args[]) {
	if (args.length != 1) {
	    System.out.println("usage: main <cert_file_name>");
	    System.exit(1);
	}
	System.out.println("Creating server CA test certificate");

	Security.addProvider(new IAIK());

	/* generate a key-pair */

	KeyPair kp = null;
	try {
	    KeyPairGenerator generator =
		    KeyPairGenerator.getInstance("RSA", "IAIK");
	    generator.initialize(512);
	    kp = generator.generateKeyPair();
	} catch (NoSuchAlgorithmException e) {
	    System.out.println("Can't create RSA/512 key pair: " + e);
	    System.exit(1);
	} catch (NoSuchProviderException e) {
	    System.out.println("Can't create RSA/512 key pair: " + e);
	    System.exit(1);
	}

	/* get the cert info from the user via the command line */

	Name issuer = new Name();
	issuer.addRDN(ObjectID.country, getEntry("country","US"));
	issuer.addRDN(ObjectID.organization,
		getEntry("company","Sun Microsystems Laboratories"));
	issuer.addRDN(ObjectID.organizationalUnit,
		getEntry("division","Brazil Project"));
	issuer.addRDN(ObjectID.commonName,
		getEntry("server name","foo.bar.com"));
		
	/* create the cert */

	X509Certificate cert =  new X509Certificate();
	try {
	    cert.setSerialNumber(new BigInteger(20, new Random()));
	    cert.setSubjectDN(issuer);
	    cert.setIssuerDN(issuer);
	    cert.setPublicKey(kp.getPublic());

	    GregorianCalendar date = new GregorianCalendar();
	    date.add(Calendar.DATE, -1);
	    cert.setValidNotBefore(date.getTime()); 
	    date.add(Calendar.MONTH,
		Integer.parseInt(getEntry("time of validity (months)","6")));
	    cert.setValidNotAfter(date.getTime());

	    /*
	     * Specify what functions this certificate is good for.  If this
	     * (Or the basicConstraint CA field) is not set, then netscape
	     * will not recognize this as a CA cert. See:
	     * http://home.netscape.com/eng/security/comm4-cert-exts.html
	     * for details
	     */

	    cert.addExtension(new NetscapeCertType(
		    NetscapeCertType.SSL_CA |
		    NetscapeCertType.SSL_SERVER |
		    NetscapeCertType.S_MIME_CA |
		    NetscapeCertType.OBJECT_SIGNING_CA)
	    );

	    /*
	     * If this certificate is to be used by an ssl server, 
	     * then add the following:
	     */

	    cert.addExtension(new NetscapeSSLServerName(
		    getEntry("host name of server", "*.eng.sun.com")));

	    String comment = getEntry("A comment for the certificate user", "");
	    if (!comment.equals("")) {
		cert.addExtension(new NetscapeComment(comment));
	    }
	    cert.sign(AlgorithmID.md5WithRSAEncryption,kp.getPrivate());

	    X509Certificate[] chain = new X509Certificate[1];
	    chain[0] = cert;

	    /* encrypt the key and save the cert */

	    EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(
		    (PrivateKeyInfo)kp.getPrivate());
	    epki.encrypt(getPassword("Certificate password"),
		    AlgorithmID.pbeWithMD5AndDES_CBC, null);
	    new KeyAndCertificate(epki, chain).saveTo(args[0], ASN1.PEM);
	} catch (Exception e) {
	    System.out.println("OOPS: " + e);
	    e.printStackTrace();
	}
	System.out.println("Saved server CA test certificate to: " + args[0]);
    }
}
