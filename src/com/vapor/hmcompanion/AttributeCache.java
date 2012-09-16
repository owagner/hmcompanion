/*
 * Caches received attributes
 *
 * $Id: AttributeCache.java,v 1.5 2012-09-16 07:42:34 owagner Exp $
 *
 */

package com.vapor.hmcompanion;

import java.io.*;
import java.util.*;
import java.util.logging.*;

public class AttributeCache
{
	static Map<String,Map<String,Attribute>> attrCache=new HashMap<String,Map<String,Attribute>>();

	static synchronized void putAttribute(String device,String attr,Object value)
	{
		Map<String,Attribute> dev=attrCache.get(device);
		if(dev==null)
		{
			dev=new HashMap<String,Attribute>();
			attrCache.put(device.intern(),dev);
		}
		Attribute a=dev.get(attr);
		if(a==null)
		{
			a=new Attribute();
			dev.put(attr.intern(),a);
		}
		a.set(value);
	}
	static public synchronized Attribute getAttribute(String device,String attr)
	{
		Map<String,Attribute> dev=attrCache.get(device);
		if(dev==null)
			return null;
		return dev.get(attr);
	}
	static synchronized Map<String,Attribute> getAllAttributes(String device)
	{
		Map<String,Attribute> dev=attrCache.get(device);
		return dev;
	}

	@SuppressWarnings("unchecked")
	static void load()
	{
		try
		{
			ObjectInputStream ois=new ObjectInputStream(new FileInputStream("hmc.cache"));
			attrCache=(Map<String,Map<String,Attribute>>)ois.readObject();
			ois.close();
			HMC.l.info("Read "+attrCache.size()+" entries from hmc.cache");
		}
		catch(FileNotFoundException fnfe)
		{
			/* Ignore, can happen */
		}
		catch(Exception e)
		{
			HMC.l.log(Level.WARNING,"Unable to load attribute cache",e);
		}
		Runtime.getRuntime().addShutdownHook(new Saver());
	}

	static class Saver extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream("hmc.cache"));
				oos.writeObject(attrCache);
				oos.close();
			}
			catch(Exception e)
			{
				HMC.l.log(Level.WARNING,"Unable to write attribute cache",e);
			}
		}
	}

}
