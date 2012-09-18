package com.vapor.hmcompanion;

import java.util.*;

public class ServerCommandGet extends ServerCommand
{
	ServerCommandGet()
	{
		super("GET","<channel name or address> [<attribute> [<attributes...>]\tget attribute(s) from channel");
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
		if(args.args.length==1)
		{
			Map<String,Attribute> attr=AttributeCache.getAllAttributes(dev);
			if(attr!=null)
				for(Map.Entry<String,Attribute> me:attr.entrySet())
					s.send(me.getKey()+":"+me.getValue());
			s.send(".");
		}
		else
		{
			for(int ix=1;ix<args.args.length;ix++)
			{
				Object v=AttributeCache.getAttribute(dev,args.args[ix].toUpperCase());
				if(v!=null)
					s.send(v.toString());
				else
					s.send("");
			}
		}
	}

}
