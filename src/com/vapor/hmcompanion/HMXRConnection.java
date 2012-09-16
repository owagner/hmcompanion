/*
 * Handles a outgoing "XML-RPC" connection with the binary encoded CCU format
 */

package com.vapor.hmcompanion;

import java.io.IOException;
import java.net.*;
import java.text.ParseException;
import java.util.logging.*;

public class HMXRConnection extends Thread
{
	Socket s;
	String host;
	int port;
	String serverurl;
	int instance;

	public HMXRConnection(String host,int port,String serverurl,int instance)
	{
		this.host=host;
		this.port=port;
		this.serverurl=serverurl;
		this.instance=instance;
	}

	// Deinit on shutdown
	private class Deiniter extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				HMXRMsg m=new HMXRMsg("init");
				m.addArg(serverurl);
				sendRequest(m,false);
			}
			catch(Exception e)
			{
				/* Ignore, we're dying anyway */
			}
		}
	}

	public void sendInit()
	{
		HMXRMsg m=new HMXRMsg("init");
		m.addArg(serverurl);
		m.addArg("CB"+instance);
		try
		{
			sendRequest(m);
		}
		catch(Exception e)
		{
			/* Ignore */
		}
		// Queue a deinit on shutdown
		Runtime.getRuntime().addShutdownHook(new Deiniter());
	}


	public synchronized HMXRResponse sendRequest(HMXRMsg m,boolean retry) throws IOException, ParseException
	{
		try
		{
			if(s==null)
				s=new Socket(host,port);

			s.getOutputStream().write(m.prepareData());
			return HMXRResponse.readMsg(s,false);
		}
		catch(IOException ioe)
		{
			if(!retry)
				throw ioe; // Just rethrow
			HMC.l.log(Level.WARNING,"Error during transaction handling",ioe);
			try
			{
				sleep(30000);
			}
			catch(InterruptedException ie)
			{
				/* Ignore */
			}
			s=null;
			return sendRequest(m,true);
		}
	}
	public HMXRResponse sendRequest(HMXRMsg m) throws IOException, ParseException
	{
		return sendRequest(m,true);
	}
}
