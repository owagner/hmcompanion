/*
 * $Id: ServerCommandSet.java,v 1.4 2012-02-09 11:50:13 owagner Exp $
 */

package com.vapor.hmcompanion;

import java.util.*;

public class ServerCommandSet extends ServerCommand
{
	ServerCommandSet()
	{
		super("SET","<channel name or address> <attribute> <value> - shortcut for setValue xmlrpc request");
	}
	
	@Override
	public void exec(Server s, ArgSplitter args) throws Exception
	{
		if(args.args.length<3)
		{
			s.send("-Missing arguments");
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
			int dix;
			if("BidCos-RF".equals(it.interf))
				dix=1;
			else if("BidCos-Wired".equals(it.interf))
				dix=0;
			else if("CUxD".equals(it.interf))
				dix=3;
			else
				dix=2;
			
			HMXRMsg m=new HMXRMsg("setValue");
			m.addArg(it.address);
			m.addArg(args.args[1]);
			m.addArgWithTypeGuessing(args.args[2]);
			
			HMXRResponse r=HMC.connections[dix].sendRequest(m);
			// Ignore response
		}
	}
}
