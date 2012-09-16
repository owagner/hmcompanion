/*
 * Caches ReGa channel names
 *
 * $Id: ReGaDeviceCache.java,v 1.7 2012-09-16 07:42:34 owagner Exp $
 *
 */

package com.vapor.hmcompanion;

import java.util.*;
import java.util.regex.*;

public class ReGaDeviceCache
{
	static Map<String,ReGaItem> itemsByName=new HashMap<String,ReGaItem>();
	static Map<String,ReGaItem> itemsByAddress=new HashMap<String,ReGaItem>();

	static ReGaItem getItemByName(String name)
	{
		ReGaItem it=itemsByName.get(name);
		if(it==null)
			it=itemsByAddress.get(name);
		return it;
	}

	static Collection<ReGaItem> getItemsByName(String name)
	{
		if(name.indexOf("*")<0)
		{
			ReGaItem it=getItemByName(name);
			if(it!=null)
				return Collections.singleton(it);
			else
				return null;
		}

		Set<ReGaItem> res=new HashSet<ReGaItem>();
		String wname=name.replace("*",".*");
		Pattern p=Pattern.compile(wname);
		for(String k:itemsByName.keySet())
		{
			if(p.matcher(k).matches())
				res.add(itemsByName.get(k));
		}
		for(String k:itemsByAddress.keySet())
		{
			if(p.matcher(k).matches())
				res.add(itemsByName.get(k));
		}
		return res;
	}

	static private void putRegaItem(String id,String address,String interf,String name)
	{
		ReGaItem r=new ReGaItem(Integer.parseInt(id),name,address,interf);
		itemsByName.put(name,r);
		itemsByAddress.put(address,r);
	}

	/*
	 * Obtain the High Level Device IDs
	 */
	static public void loadDeviceCache()
	{
		HMC.l.info("Obtaining ReGa channel items");
		String r=TCLRegaHandler.sendHMScript(
			"string id;"+
			"foreach(id, root.Channels().EnumUsedIDs())"+
			"  {"+
			"   var ch=dom.GetObject(id);" +
			"	var i=dom.GetObject(ch.Interface());"+
			"   WriteLine(id+\"\t\"+ch.Address()+\"\t\"+i.Name()+\"\t\"+ch.Name());"+
			"  }"
		);
		if(r==null)
		{
			// Request failed, refuse to run
			System.exit(1);
		}
		String lines[]=r.split("\n");
		for(String l:lines)
		{
			String p[]=l.split("\t");
			if(p.length==4)
				putRegaItem(p[0],p[1],p[2],p[3]);
		}
		HMC.l.info("Obtained "+itemsByName.size()+" ReGa Channel items");
	}

	static public Map<String,String> loadDeviceNames()
	{
		Map<String,String> m=new TreeMap<String,String>();

		HMC.l.info("Obtaining ReGa device names");
		String r=TCLRegaHandler.sendHMScript(
			"string id;"+
			"foreach(id, root.Devices().EnumUsedIDs())"+
			"  {"+
			"   var d=dom.GetObject(id);" +
			"   WriteLine(d.Address()+\"\t\"+d.Name());"+
			"  }" +
			"foreach(id, root.Channels().EnumUsedIDs())"+
			"  {"+
			"   var d=dom.GetObject(id);" +
			"   WriteLine(d.Address()+\"\t\"+d.Name());"+
			"  }"
		);
		if(r==null)
		{
			return m;
		}
		String lines[]=r.split("\n");
		for(String l:lines)
		{
			String p[]=l.split("\t",2);
			if(p.length==2)
				m.put(p[0],p[1]);
		}
		HMC.l.info("Obtained "+m.size()+" ReGa device names");
		return m;
	}
}
