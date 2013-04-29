package com.vapor.hmcompanion;

import java.net.*;
import java.util.logging.*;

public class GraphiteInterface
{
	private static String carbon_host;
	private static String carbon_prefix;
	private static int carbon_port;

	public static void init()
	{
		String carbonHost=System.getProperty("hmc.carbon.host");
		if(carbonHost==null)
			return;
		String cbp[]=carbonHost.split(":",2);
		carbon_host=cbp[0];
		if(cbp.length==2)
			carbon_port=Integer.parseInt(cbp[1]);
		else
			carbon_port=2003;

		carbon_prefix=System.getProperty("hmc.carbon.prefix","hm.");
		if(!carbon_prefix.endsWith("."))
			carbon_prefix+=".";

		HMC.l.info("Using graphite/carbon server "+carbon_host+":"+carbon_port+" with metric prefix \""+carbon_prefix+"\"");
	}

	private static String sanitizeName(String name)
	{
		name=name.toLowerCase();
		name=name.replace("ä","ae");
		name=name.replace("ö","oe");
		name=name.replace("ü","ue");
		name=name.replace("ß","ss");
		StringBuilder nn=new StringBuilder(name);
		for(int ix=0;ix<nn.length();ix++)
		{
			char ch=nn.charAt(ix);
			if(	!(ch>='a'&&ch<='z') && !(ch>='0'&&ch<='9'))
			{
				nn.setCharAt(ix, '_');
			}
		}
		return nn.toString();
	}

	public static void sendToCarbon(String address,String item,String val)
	{
		if(carbon_host==null)
			return;

		ReGaItem rit=ReGaDeviceCache.getItemByName(address);
		if(rit==null)
			return;

		// Transform value
		String saneValue=val;
		try
		{
			Double.parseDouble(val);
		}
		catch(NumberFormatException nfe)
		{
			val=Boolean.parseBoolean(val)?"1":"0";
		}

		// Transform name -- everything which isn't good as a metric char is transformed to "_"
		StringBuilder msg=new StringBuilder(carbon_prefix);
		msg.append(sanitizeName(rit.name));
		msg.append('.');
		msg.append(sanitizeName(item));
		msg.append(' ');
		msg.append(saneValue);
		msg.append(' ');
		msg.append(System.currentTimeMillis()/1000);
		msg.append('\n');

		try
		{
			Socket s=new Socket(carbon_host,carbon_port);
			s.getOutputStream().write(msg.toString().getBytes("ASCII"));
			s.getOutputStream().flush();
			s.close();
		}
		catch(Exception e)
		{
			HMC.l.log(Level.WARNING,"Unable to send data to carbon host",e);
		}
	}
}
