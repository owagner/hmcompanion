/*
 * $Id: ServerCommandHMGet.java,v 1.4 2012-09-16 07:42:34 owagner Exp $
 */

package com.vapor.hmcompanion;

import java.util.*;
import java.text.*;

public class ServerCommandHMGet extends ServerCommand
{
	ServerCommandHMGet()
	{
		super("HMGET","<variable> [<variable2>...|-timestamp|-timestampts] - get HMScript system variable/timestamps");
	}
	
	private DateFormat hmcformat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Override
	public void exec(Server s, ArgSplitter args) throws Exception
	{
		boolean timestamp=false, timestampts=false, state=false;
		
		if(args.args.length<1)
		{
			s.send("-Missing argument <variable>");
			return;
		}
		for(String a:args.args)
		{
			if("-timestamp".equalsIgnoreCase(a))
			{
				timestamp=true;
				continue;
			}
			else if("-timestampts".equalsIgnoreCase(a))
			{
				timestampts=true;
				continue;
			}
			else if("-state".equalsIgnoreCase(a))
			{
				state=true;
				continue;
			}
			
			StringBuilder script=new StringBuilder();
			script.append("Write(dom.GetObject(\"");
			script.append(a);
			script.append("\").");
			script.append((timestamp||timestampts)?"Timestamp":(state?"State":"Variable"));
			script.append("());");
			String reply=TCLRegaHandler.sendHMScript(script.toString());
			if(timestampts)
			{
				Date d=hmcformat.parse(reply);
				if(d!=null)
					s.send(String.valueOf(d.getTime()/1000));
				else
					s.send("");
			}
			else
				s.send(reply);
		}
		s.send(".");
	}
}
