/*
 * $Id: ServerCommandCGet.java,v 1.1 2010-06-02 23:22:47 owagner Exp $
 */

package com.vapor.hmcompanion;

import java.util.*;

public class ServerCommandCGet extends ServerCommand
{
	ServerCommandCGet()
	{
		super("CGET","<channel name or address> get channel attributes in Cacti script format");
	}
	
	@Override
	public void exec(Server s, ArgSplitter args) throws Exception
	{
		if(args.args.length<1)
		{
			s.send("-Missing argument <channel>");
			return;
		}
		String dev=parseAddress(args.args[0]);
		Map<String,Attribute> attr=AttributeCache.getAllAttributes(dev);
		StringBuilder res=new StringBuilder();
		if(attr!=null)
			for(Map.Entry<String,Attribute> me:attr.entrySet())
			{
				if(res.length()>0)
					res.append(' ');
				res.append(me.getKey());
				res.append(':');
				Attribute a=me.getValue();
				if(a.value instanceof Boolean)
					res.append(((Boolean)a.value).booleanValue()?"1":"0");
				else
					res.append(me.getValue());
			}
		s.send(res.toString());
	}

}
