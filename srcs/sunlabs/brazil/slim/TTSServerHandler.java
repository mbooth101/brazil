/*
 * TTSServerHandler.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2002-2006 Sun Microsystems, Inc.
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
 * The Initial Developer of the Original Code is: lc138592.
 * Portions created by lc138592 are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): lc138592, suhler.
 *
 * Version:  2.2
 * Created by lc138592 on 02/07/29
 * Last modified by suhler on 06/11/13 15:31:21
 *
 * Version Histories:
 *
 * 2.2 06/11/13-15:31:21 (suhler)
 *   MatchString moved packages from "handler" to "util"
 *
 * 2.1 02/10/01-16:36:03 (suhler)
 *   version change
 *
 * 1.9 02/08/12-15:36:27 (lc138592)
 *   Javadocs
 *
 * 1.8 02/08/12-15:13:55 (lc138592)
 *   Changed how the server is setup, now needs voice= option and provides two defaults.
 *
 * 1.7 02/08/07-16:37:31 (lc138592)
 *   Added scaling with the scale query/config parameter
 *
 * 1.6 02/08/07-14:50:39 (lc138592)
 *   Added an option to swap the bytes or not, PCM16 for au works, not for wav on Solaris though
 *
 * 1.5 02/08/06-17:56:22 (lc138592)
 *   Added byte swapping for 16-bit values, this is a temp fix
 *
 * 1.4 02/08/06-11:24:42 (suhler)
 *   remove * imports
 *   add "mustHave" iption
 *
 * 1.3 02/08/05-17:02:25 (lc138592)
 *   Added Javadocs
 *
 * 1.2 02/07/31-13:57:05 (lc138592)
 *   Added support to specify the response type, WAV, AU or raw
 *
 * 1.2 70/01/01-00:00:02 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 slim/TTSServerHandler.java
 *
 * 1.1 02/07/29-16:28:49 (lc138592)
 *   date and time created 02/07/29 16:28:49 by lc138592
 *
 */

package sunlabs.brazil.slim;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.en.us.CMULexicon;
import java.util.Hashtable;
import javax.sound.sampled.AudioFormat;
import sunlabs.brazil.util.MatchString;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import sunlabs.brazil.slim.BasicAudioTools;
// import sunlabs.brazil.util.http.HttpInputStream;
// import sunlabs.brazil.util.regexp.Regexp;

 /**
 * This class provides an interface to a FreeTTS text to speech engine that 
 * resides locally with the server (as part of brazil).  An alternative to the
 * intrinsically high bandwidth Free TTS server, this was developed to allow the
 * expensive processing to be done in the server space.
 *
 * Like TTSHandler this utilizes the following query parameters:<p>
 * <li> <b>transtext</b> = text to be translated
 * <p>
 * Configuration File Parameters:
 * <dl class=props>
 * <dt>prefix, suffix, glob, match
 * <dd>Specify the URL that triggers this handler.
 * (See {@link sunlabs.brazil.handler.MatchString}).
 * <dt>mustHave</dt>
 * <dd>If defined, it specifies a variable that must be set in the
 * request properties in order for this handler to run.
 * <dt>encoding</dt>
 * <dd>specify one of ML8 (8-bit mulaw 8kHz), PCM8 (16-bit linear PCM, 8-kHz), PCM16 (.16-bit linear PCM, 16-kHz)</dd>
 * <dt>format</dt>
 * <dd>specify one of "wav" or "au" for Microsoft AIFF Wave format, or Sun Audio format</dd>
 * </dl>
 *
 * @author Leo Chao
 * @version @(#)TTSServerHandler.java	2.2
 */
public class TTSServerHandler implements Handler
{
    private Voice cVoice;
    private String cVoiceClassName;
    private String cVoiceDatabaseName;
    private String cPre;
    private Server cSer;
    private String cFormat;
    private String cEncoding;
    private int cML8Scaling;
    private boolean cSwap;
    private int cSampling;
    private MatchString cMatchString;    
    private String cMustHave;    
    private HTMLAudioPlayer htmlAudioPlayer;

    private Voice loadVoice(String voiceClassName, String databaseName) 
    {
	Voice voice = null;
	Class voiceClass;
	try {
	    voiceClass = Class.forName(voiceClassName);	
	    voice = (Voice) voiceClass.newInstance();
	    cSer.log(Server.LOG_DIAGNOSTIC, this, "voice initialized");
	} catch (Exception e) {
	    cSer.log(Server.LOG_ERROR, this, "Cannot initialize voice with: " + voiceClassName);
	    cSer.log(Server.LOG_ERROR, this, "Exception thrown:\n"+e);
	}

	if (voice != null) {
	    cSer.log(Server.LOG_DIAGNOSTIC, this, "database is: " + databaseName);
	    voice.setLexicon(new CMULexicon());
      	    voice.getFeatures().setString(Voice.DATABASE_NAME, databaseName);
	    voice.load();
	}

	return voice;
    }

    /**
     * Initializes the handler and the HTML Audio Player for transferring the data
     * back to the user.  Checks for the voice8 and voice16 defaults, if prefix.voice
     * is set to these the system will provide a default 8 or 16 kHz voice.  Otherwise
     * voice.class, voice.db and voice.sampling will need to be set.
     * 
     * @param server {@link sunlabs.brazil.server.Server Server} object provided by the webserver.
     * @param prefix String indicating the URL-prefix expected for processing
     * @return Returns true if the TTS server parameters are present
     */
    public boolean init(Server server, String prefix)
    {
	String voiceTag;
	cPre = prefix;
	cSer = server;
	cMatchString = new MatchString(prefix, server.props);
	cMustHave = server.props.getProperty(prefix + "mustHave");
	htmlAudioPlayer = new HTMLAudioPlayer();

	// Initialize voices
	voiceTag = server.props.getProperty(prefix+"voice", "voice8");
	if (voiceTag.equals("voice8")) {
	    cVoiceClassName = "com.sun.speech.freetts.en.us.CMUDiphoneVoice";
	    cVoiceDatabaseName = "cmu_kal/diphone_units.bin";
	    cSampling = 8000;
	    cSer.log(Server.LOG_DIAGNOSTIC, "TTS init: ", "Using 8kHz Voice");
	}
	else if (voiceTag.equals("voice16")) {
	    cVoiceClassName = "com.sun.speech.freetts.en.us.CMUDiphoneVoice";
	    cVoiceDatabaseName = "cmu_kal/diphone_units16.bin";
	    cSampling = 16000;
	    cSer.log(Server.LOG_DIAGNOSTIC, "TTS init: ", "Using 16kHz Voice");
	}
	else {
	    cVoiceClassName = server.props.getProperty(voiceTag+".class");
	    if (cVoiceClassName == null) {
		cSer.log(Server.LOG_ERROR, "TTS init: ", "Could not load voice class for voice " + voiceTag);
		return false;
	    }

	    cVoiceDatabaseName = server.props.getProperty(voiceTag+".db");
	    if (cVoiceDatabaseName == null) {
		cSer.log(Server.LOG_ERROR, "TTS init: ", "Could not load voice database for voice " + voiceTag);
		return false;
	    }

	    try {
		cSampling=Integer.parseInt(server.props.getProperty(voiceTag+".sampling"));
	    } catch (Exception e) {
		cSer.log(Server.LOG_WARNING, "TTS init: ", "sampling not an integer");
		cSampling=8000;
	    }	    
	    cSer.log(Server.LOG_DIAGNOSTIC, "TTS init: ", "Using Custom Voice:\nclass="+cVoiceClassName+"\ndatabase="+cVoiceDatabaseName+"\nsampling="+cSampling);
	}

	cVoice = loadVoice(cVoiceClassName, cVoiceDatabaseName);
	return true;
    }
    
    private void overrideDefaults(Request req)
    {
	String format, encoding, swap, scale;

	format = (String)((req.getQueryData()).get("format"));
	if (format == null) {
	    format = (req.props.getProperty(cPre+"format", BasicAudioTools.AU_FILE));
	}
	cFormat = format;
    
	encoding = (String)((req.getQueryData()).get("encoding"));
	if (encoding == null) {
	    encoding = (req.props.getProperty(cPre+"encoding", "ML8"));
	    //valid such is: 8-bit Mulaw (ML8), 16 bit PCM (PCM16)
	}
	cEncoding = encoding;

	swap = (String)((req.getQueryData()).get("swap"));
	if (swap == null) 
	    cSwap = false;
	else
	    cSwap = true;

	scale = (String)((req.getQueryData()).get("scale"));
	if (scale == null) {
	    scale = req.props.getProperty(cPre+"scale", "3");
	}
	try {
	    cML8Scaling = Integer.parseInt(scale.trim());
	} catch (Exception e) {
	    cML8Scaling = 3;
	}
    }

    /**
     * Responds to a text to speech request.  Query parameter overriding is done
     * for:<p>
     * <li><i>encoding</i>: ML8, PCM16
     * <li><i>format</i>:au, wav
     * <li><i>swap</i>: non-null
     * <li><i>scale</i>: valid only for ML8 encoding, scales the conversion bitshift
     * <p>
     * If successful an audio file response is sent back to the page, allowing
     * the browser/client to fire off plug-ins/programs to play the clip.
     *
     * @param req {@link sunlabs.brazil.server.Request Request} object passed in by the webserver.
     * @return Returns true if an audio file is successfully returned.
     */
    public boolean respond(Request req)
    {
	String text;

	if(!isMyRequest(req)) {
	    return false;
	}
	
	//Okay this is redundant and could be placed in isMyRequest as a side effect
	//but oh well.
	text = (String)((req.getQueryData()).get("transtext"));
	if (text.equals("")) {
	    cSer.log(Server.LOG_WARNING,": TTS Error","Empty String, NOT processing");
	    return false;
	}
	overrideDefaults(req);

	cSer.log(Server.LOG_DIAGNOSTIC, ": TTS Text", text);
	cSer.log(Server.LOG_DIAGNOSTIC, ": TTS Format", cFormat);
	cSer.log(Server.LOG_DIAGNOSTIC, ": TTS Encoding", cEncoding);
	
	if (cVoice != null) {
	    htmlAudioPlayer.setRequest(req);
	    cVoice.setAudioPlayer(htmlAudioPlayer);
	    text = text.replace('.', ';');
	    cVoice.speak(text);
	} else {
	    cSer.log(Server.LOG_ERROR, this, "No voice to speak with!");
	}
	
	return true;
    }

    private boolean isMyRequest(Request req)
    {	
	if (!cMatchString.match(req.url))
	    return false;
	if (cMustHave != null && 
		(req.props.getProperty(cMustHave)==null)) {
	    req.log(Server.LOG_DIAGNOSTIC, cPre, cMustHave + " not set");
	    return false;
	}
	
	String text = (String)((req.getQueryData()).get("transtext"));
	if (text == null)
	    return false;

	return true;
    }




    /*
     * HTML Audio Player - probably should be a seperate class since its
     * so big, but oh well
     */
    private class HTMLAudioPlayer implements AudioPlayer
    {
	private AudioFormat audioFormat;
	private int bytesToPlay = 0;
	private int bytesPlayed = 0;
	private boolean firstByteSent = false;
	private long firstByteTime = -1;	
	private Request cR;
	private String filetype;
	
	public void setRequest(Request req)
	{
	    cR = req;	    
	}

	public void setAudioFormat(AudioFormat format) {
	    this.audioFormat = format;
	}
	
	public AudioFormat getAudioFormat() {
	    return this.audioFormat;
	}
	
	public void pause() {}	
	public void resume() {}	
	public void reset() {}	
	public boolean drain() {
	    return true;
	}

	public void begin(int size) {
	    String type = null;
	    byte[] audiohead = null;
	    firstByteSent = false;
	    
	    // 8-bit mu-law at 8khz, will require conversion
	    if (cEncoding.equals(BasicAudioTools.ML8))
		size = size /2;
	    
	    bytesToPlay = size;
	    if (cFormat.equals(BasicAudioTools.WAV_FILE)) {
		audiohead = BasicAudioTools.getWaveHeader(size, cEncoding, cSampling);
		cSer.log(Server.LOG_DIAGNOSTIC, this, 
			 "header size: " + audiohead.length + 
			 "  encoding: " + cEncoding);
		type = "audio/wav";
		
	    }
	    else if (cFormat.equals(BasicAudioTools.AU_FILE)) {
		audiohead = BasicAudioTools.getBasicHeader(size,cEncoding, cSampling);
		cSer.log(Server.LOG_DIAGNOSTIC, this, 
			 "header size: " + audiohead.length + 
			 "  encoding: " + cEncoding);
		type = "audio/basic";
	    }
	    else {
		type = "application/octet-stream";
		audiohead = new byte[0];
	    }
	    
	    try {
		cR.sendHeaders(200, 
			       type, 
			       size + audiohead.length);
		cR.out.write(audiohead, 0, audiohead.length);
		cSer.log(Server.LOG_INFORMATIONAL, this, "Begin: " + size);
	    } catch (Exception e) {
		cSer.log(Server.LOG_ERROR, this, "Cannot send response to server");
	    }
	}
		
	public void startFirstSampleTimer() {
	}
			
	public boolean end() {
	    cSer.log(Server.LOG_DIAGNOSTIC, this, "ending audio player");

	    if (bytesPlayed < bytesToPlay) {
		int bytesNotPlayed = bytesToPlay - bytesPlayed;
		writeZero(bytesNotPlayed);
		cSer.log(Server.LOG_DIAGNOSTIC,this, bytesNotPlayed + " not played.");
	    }
	    
	    bytesToPlay = 0;
	    bytesPlayed = 0;
	    return true;
	}
		
	public void cancel() {}		
	public void close() {}		
	public float getVolume() {
	    return -1;
	}			
	public void setVolume(float volume) {}			
	public long getTime() {
	    return -1;
	}
			
	public void resetTime() {}
	
	public boolean writeZero(int numZeros) {
	    cSer.log(Server.LOG_INFORMATIONAL, ": writing voice", 
		     "Writing " + numZeros + " zeros");
	    try {
		if (numZeros > 0) cR.out.write(new byte[numZeros], 0, numZeros);
		else return false;
	    } catch (Exception e) {
		cSer.log(Server.LOG_ERROR, this, "Cannot write to server");
		return false;
	    }
	    return true;
	}

	public boolean write(byte[] audioData) {
	    return write(audioData, 0, audioData.length);
	}
	
	public boolean write(byte[] audioData, int offset, int size) {

	    cSer.log(Server.LOG_DIAGNOSTIC, this, "offset: " + offset + "  size: " + size);
	    if (!firstByteSent) {
		firstByteTime = System.currentTimeMillis();
		firstByteSent = true;
	    }
	    
	    //If we desire 8-bit mulaw, convert
	    if (cEncoding.equals(BasicAudioTools.ML8)) {
		audioData = BasicAudioTools.convertToML8(audioData,size, cML8Scaling);
		size = size /2;
		cSer.log(Server.LOG_DIAGNOSTIC, this, "performing conversion w/ scaling of " + cML8Scaling);
	    }

	    if (cFormat.equals(BasicAudioTools.WAV_FILE)) {
		audioData = BasicAudioTools.swapBytes(audioData, offset, size);
		cSer.log(Server.LOG_DIAGNOSTIC, this, "wave file, swapping bytes");
	    }
	    
	    if (cSwap) {
		audioData = BasicAudioTools.swapBytes(audioData, offset, size);
		cSer.log(Server.LOG_DIAGNOSTIC, this, "swapping bytes");
	    }

	    bytesPlayed += size;
	    
	    try {
		cR.out.write(audioData, offset, size);
		cSer.log(Server.LOG_INFORMATIONAL, 
			 ": writing voice", 
			 "sent " + size + " bytes" + "   total: " + bytesPlayed);
	    } catch (Exception e) {
		cSer.log(Server.LOG_ERROR, this, "Cannot send data to server");
	    }
	    	   
	    return true;
	}

	public void showMetrics() {}	    
	public long getFirstByteSentTime() {
	    return firstByteTime;
	}
    }
}
