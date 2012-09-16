/*
 * $Id: ServerCommandMGet.java,v 1.1 2010-08-01 19:37:28 owagner Exp $
 */

package com.vapor.hmcompanion;

import java.util.*;

public class ServerCommandMGet extends ServerCommand
{
	ServerCommandMGet()
	{
		super("MGET","<channel name wildcard> <attribute>\tget attribute from multiple channels");
	}
	
	@Override
	public void exec(Server s, ArgSplitter args) throws Exception
	{
		if(args.args.length!=2)
		{
			s.send("-Missing argument <channel> <attribute>");
			return;
		}
		Collection<ReGaItem> items=ReGaDeviceCache.getItemsByName(args.args[0]);
		if(items==null)
		{
			s.send("-Unable to identify channel "+args.args[0]);
			return;
		}
		for(ReGaItem it:items)
		{
			Object v=AttributeCache.getAttribute(it.address,args.args[1].toUpperCase());
			if(v!=null)
				s.send(it.name+":"+v.toString());
			else
				s.send(it.name+":");
		}
		s.send(".");
	}
}
