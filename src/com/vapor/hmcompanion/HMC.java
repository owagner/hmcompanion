/*
 * HomeMatic Companion
 * -------------------
 * 
 * Simple server which provides utility functions for dealing with a HomeMatic CCU for 
 * integration into a broader home automation environment.
 * 
 * Written by Oliver Wagner <owagner@vapor.com>
 * 
 * $Id: HMC.java,v 1.20 2012-09-16 07:42:34 owagner Exp $
 * 
 */

package com.vapor.hmcompanion;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import com.vapor.hmcompanion.ui.*;

public class HMC
{
	static HMXRConnection connections[];
	static public Logger l;
	static public String version="0.19";
	static public Timer t=new Timer(true);
	
	static void reInit()
	{
		for(HMXRConnection c:connections)
			c.sendInit();
	}
	
	protected static void initWatchog()
	{
		long now=System.currentTimeMillis();
		// If we didn't get a message for more than 60s, reinit
		if(now-XMLRPCServer.lastRequest>(3*60*1000))
		{
			l.warning("No XML-RPC request since "+new Date(XMLRPCServer.lastRequest)+", reiniting");
			reInit();
		}
	}
	
	private static void serverMode(String hmhost,int port,String authkey) throws Exception
	{
		l=Logger.getLogger("hmcompanion");
		l.info("HMCompanion V"+version+" (C) 2012 Oliver Wagner <owagner@vapor.com>, All Rights Reserved");
		AttributeCache.load();
		TCLRegaHandler.setHMHost(hmhost);
		String serverurl=XMLRPCServer.init();
		l.info("Listening for XMLRPC callbacks on "+serverurl+", now init-ing");
		connections=new HMXRConnection[4];
		for(int c=0;c<3;c++)
		{
			connections[c]=new HMXRConnection(hmhost,2000+c,serverurl,c);
		}
		connections[3]=new HMXRConnection(hmhost,8701,serverurl,3);
		reInit();
		t.schedule(new TimerTask(){
			@Override
			public void run()
			{
				initWatchog();
			}
		},30*1000,30*1000);
		ReGaDeviceCache.loadDeviceCache();
		Server.acceptIt(port,authkey);
	}
	
	private static void loadProperties() throws IOException
	{
		File f=new File("hmcompanion.cfg");
		if(f.exists())
		{
			BufferedReader br=new BufferedReader(new FileReader(f));
			String l;
			while((l=br.readLine())!=null)
			{
				String p[]=l.split("=",2);
				if(p.length==2)
					System.setProperty(p[0],p[1]);
			}
			br.close();
		}
	}
	
	public static void main(String args[]) throws Exception
	{
		l=Logger.getLogger("hmc");
		
		loadProperties();
		
		if(args.length>=2 && "-server".equals(args[1]))
		{
			serverMode(args[0],args.length>2?Integer.parseInt(args[2]):6770,args.length>3?args[3]:null);
		}
		else if(args.length<3)
		{
			// UI Mode
			MainWin.launchUI(args.length>0?args[0]:null, args.length>1?args[1]:null);
		}
		else
		{
			HMXRConnection conn=new HMXRConnection(args[0],Integer.parseInt(args[1]),null,-1);
			HMXRMsg m=new HMXRMsg(args[2]);
			for(int ix=3;ix<args.length;ix++)
				m.addArg(args[ix]);
			HMXRResponse r=conn.sendRequest(m);
			System.out.println(r);
		}
	}
}
