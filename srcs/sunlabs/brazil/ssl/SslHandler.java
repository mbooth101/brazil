/*
 * SslHandler.java
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
 * Version:  2.5
 * Created by suhler on 01/06/06
 * Last modified by suhler on 04/11/30 15:19:42
 *
 * Version Histories:
 *
 * 2.5 04/11/30-15:19:42 (suhler)
 *   fixed sccs version string
 *
 * 2.4 04/11/30-10:58:59 (suhler)
 *   doc fix
 *
 * 2.3 03/08/01-16:21:02 (suhler)
 *   lint
 *
 * 2.2 03/07/28-09:25:20 (suhler)
 *   Merged changes between child workspace "/home/suhler/brazil/naws" and
 *   parent workspace "/net/mack.eng/export/ws/brazil/naws".
 *
 * 1.6.1.1 02/11/14-14:28:51 (suhler)
 *   make client cert fingerprint available as a property
 *
 * 2.1 02/10/01-16:39:49 (suhler)
 *   version change
 *
 * 1.6 02/02/08-16:56:01 (suhler)
 *   added docs
 *
 * 1.5 01/11/18-17:26:58 (suhler)
 *   added IAIK copyright notice
 *
 * 1.4 01/08/03-18:23:38 (suhler)
 *   remove renegotiate tests
 *
 * 1.3 01/07/20-10:37:39 (suhler)
 *   added option to request client cert
 *   added option to use cert ser# as sessionId
 *
 * 1.2 01/07/19-21:31:16 (suhler)
 *   doc fixes
 *
 * 1.2 01/06/06-12:44:07 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 ssl/SslHandler.java
 *
 * 1.1 01/06/06-12:44:06 (suhler)
 *   date and time created 01/06/06 12:44:06 by suhler
 *
 */

package sunlabs.brazil.ssl;

import iaik.asn1.structures.Name;
import iaik.pkcs.pkcs8.EncryptedPrivateKeyInfo;
import iaik.security.rsa.RSAPrivateKey;
import iaik.security.ssl.CipherSuite;
import iaik.security.ssl.ClientTrustDecider;
import iaik.security.ssl.SSLCertificate;
import iaik.security.ssl.SSLServerContext;
import iaik.security.ssl.SSLServerSocket;
import iaik.security.ssl.SSLSocket;
import iaik.security.ssl.ServerTrustDecider;
import iaik.security.ssl.TrustDecider;
import iaik.utils.KeyAndCertificate;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.security.KeyPair;
import java.security.Principal;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.crypto.spec.DHParameterSpec;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.util.Base64;

/**
 * Handler for installing SSL into the server.
 * the server's <code>listen</code> socket is replaced with a socket that
 * implements SSL, using the implementation from
 * <a href="http://jcewww.iaik.at/products/">Iaik</a>.
 * <p>The server requests a certificate from the user, and if provided, 
 * incorporates the user's certificate information into
 * <code>request.props</code>.
 * <p>Properties:
 * <dl class=props>
 * <dt>certDir<dd>The absolute pathname of the directory containing the
 * server's certificates.  The "main" in this class should generate
 * a representitive sample (but doesn't).
 * In the mean time, you can run "java CreateCertificates" with the Iaik
 * jar in your path, to create a set of server test certificates in the
 * <code>certs</code> directory.
 * <dt>certRequired<dd>If set, the server will ask the user for a client cert.
 * <dt>issuer.*
 * <dd>Information about the certificate issuer.
 * <dt>owner.*
 * <dd>Information about the certificate owner.
 * <dt>fingerprint.*
 * <dd>The base64 encoded fingerprints of the clients certificates
 * <dt>session
 * <dd>The property to put the certificate serial number into.
 * Defaults to "cert.id".
 * </dl>
 * <p>
 * NOTE: portions of this code were adapted from from the IAIK examples,
 * used with permission.
 *
 * @author		Stephen Uhler
 * @version		2.5
 */

public class SslHandler implements Handler, ServerTrustDecider {

  boolean certRequired;		// true if we ask for a client cert
  String session;		// what prop to put cert id into

  public boolean isTrustedPeer(SSLCertificate cert) {
      System.out.println("Accepting client certificate: " + cert);
      return true;
  }

  public boolean
  init(Server server, String prefix) {
    System.out.println("Listen is: " + server.listen);
    int port = server.listen.getLocalPort();
    String certDir = server.props.getProperty(prefix + "certDir");
    if (certDir == null) {
        System.out.println("No certDir specified");
        return false;
    }
    String session = server.props.getProperty(prefix + "session", "cert.id");
    certRequired = (server.props.getProperty(prefix + "certRequired") != null);

    try {
      Class provider = Class.forName("iaik.security.provider.IAIK");
      Provider iaik = (Provider)provider.newInstance();
      System.out.println("add Provider "+iaik.getInfo()+"...");
    	Security.addProvider(iaik);
    } catch (ClassNotFoundException ex) {
      System.out.println("Provider IAIK not found. Add iaik_jce.jar or iaik_jce_full.jar to your classpath.");
      System.out.println("If you are going to use a different provider please take a look at Readme.html!");
      return false;
    } catch (Exception ex) {
      return false;
    }

    SSLSocket ssl = null;
    SSLServerContext serverContext = new SSLServerContext();
    boolean generateDHParameters = false;     // use pre-generated Diffi-Hellman parameters

    DHParameterSpec dhparam = null;

    // pre-generated Diffi-Hellman parameters
    BigInteger p = new BigInteger("da583c16d9852289d0e4af756f4cca92dd4be533b804fb0fed94ef9c8a4403ed574650d36999db29d776276ba2d3d412e218f4dd1e084cf6d8003e7c4774e833", 16);
    BigInteger g = BigInteger.valueOf(2);
    dhparam = new DHParameterSpec(p, g);

    // set the DH parameter for empherial and anon cipher suites
    serverContext.setDHParameter(dhparam);
    
    KeyAndCertificate kac;
    EncryptedPrivateKeyInfo epki;
    String password = getPassword("Certificate password");

    try {
      kac = new KeyAndCertificate(certDir + "/serverRSA1024.pem");
      epki = (EncryptedPrivateKeyInfo)kac.getPrivateKey();
      epki.decrypt(password);
      serverContext.setRSACertificate(kac.getCertificateChain(), (RSAPrivateKey)epki.getPrivateKeyInfo());
    } catch (Exception ex) {
      System.out.println("Unable to set RSA server certificate.");
      System.out.println("RSA cipher-suites can not be used. " + ex);
    }

    try {
        // set the DSA certificate/private key for DSA cipher suites
      kac = new KeyAndCertificate(certDir + "/serverDSA1024.pem");
      epki = (EncryptedPrivateKeyInfo)kac.getPrivateKey();
      epki.decrypt(password);
      serverContext.setDSACertificate(kac.getCertificateChain(), epki.getPrivateKeyInfo());
    } catch (Exception ex) {
      System.out.println("Unable to set DSA server certificate.");
      System.out.println("DSA cipher-suites can not be used. " + ex);
    }

    try {
        // set the DH certificate/private key for DH cipher suites
      kac = new KeyAndCertificate(certDir + "/serverDH1024.pem");
      epki = (EncryptedPrivateKeyInfo)kac.getPrivateKey();
      epki.decrypt(password);
      serverContext.setDHCertificate(kac.getCertificateChain(), epki.getPrivateKeyInfo());
    } catch (Exception ex) {
      System.out.println("Unable to set Diffie-Hellman server certificate.");
      System.out.println("Diffie-Hellman cipher-suites can not be used. " + ex);
    }

    try {
        // set the temporary RSA key pair for RSA_EXPORT cipher suites
      RSAPrivateKey tsk = new RSAPrivateKey(new FileInputStream(certDir + "/tempRSAPrivateKey.der"));
      PublicKey tpk = tsk.getPublicKey();
      KeyPair tempKeyPair = new KeyPair(tpk, tsk);
      serverContext.setRSATempKeyPair(tempKeyPair);
    } catch (Exception ex) {
      System.out.println("Unable to set 512 bit temporary RSA key pair.");
      System.out.println("RSA exportable cipher-suites can not be used.");
    }

    CipherSuite[] enabledCS = serverContext.updateCipherSuites();

    ServerTrustDecider trustDecider = (ServerTrustDecider) this;

    Vector acceptedCAs = new Vector();
    serverContext.setTrustDecider(trustDecider);

    if (certRequired) {
	Name[] cas = new Name[acceptedCAs.size()];
	acceptedCAs.copyInto(cas);
	byte[] types = {
	  ClientTrustDecider.rsa_sign,
	  ClientTrustDecider.dss_sign
	};
	serverContext.setRequireClientCertificate(types, cas);
    } else {
	serverContext.setRequireClientCertificate(null, null);
    }

    System.out.println(serverContext);

    SSLServerSocket listener = null;
    try {
	server.listen.close();
        listener = new SSLServerSocket(port, serverContext);
    } catch (IOException e) {
        System.out.println("Fatal Error creating new server socket  " + e);
        System.exit(1);
    }
    server.listen = (ServerSocket) listener;
    server.protocol="https";
    System.out.println("Installing ssl server");
    return true;
    }

    /**
     * Extract client and issuer certificate information.
     * Insert intro request properties.
     */

    public boolean
    respond(Request request) throws IOException {
        SSLSocket s = (SSLSocket) (request.getSocket());
        X509Certificate[]  chain = s.getPeerCertificateChain();
        if (chain != null) {
	    for (int i=0;i < chain.length; i++) {
               String issuer = chain[i].getIssuerDN().toString();
               String subject = chain[i].getSubjectDN().toString();
	       String indx = (i==0 ? "" : i + ".");
               burst(request.props, "issuer." + indx, issuer);
               burst(request.props, "owner." + indx, subject);
	       request.props.put("certid." +
			indx, chain[i].getSerialNumber().toString());
	       try {
	           byte[] fp = ((iaik.x509.X509Certificate)chain[i])
				.getFingerprint();
	           request.props.put("fingerprint" + (i==0 ? "" : "." + i),
			   Base64.encode(fp));
	       } catch (Exception e) {}
	   }
	}
	return false;
    }

    /**
     * Burst open cert info and place in hash table with the supplied prefix
     */

    private void burst(Hashtable result, String prefix, String cert) {
        StringTokenizer st = new StringTokenizer(cert,",");
        while (st.hasMoreTokens()) {
            String s = st.nextToken().trim();
            int index = s.indexOf("=");
            result.put(prefix + s.substring(0,index), s.substring(index+1));
        }
    }


    /**
     * Get a password from the command line (assumes vt100)
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
}
