package com.vapor.hmcompanion;

import java.util.*;

public class ServerCommandSetParam extends ServerCommand
{
	ServerCommandSetParam()
	{
		super("SETPARAM","<channel name or address> <attribute> <value> - shortcut for putParamset xmlrpc request");
	}

	@Override
	public void exec(Server s, ArgSplitter args) throws Exception
	{
		if(args.args.length<4)
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

			HMXRMsg m=new HMXRMsg("putParamset");
			m.addArg(it.address);
			m.addArg(args.args[1]); // Paramset name (MASTER)
			// Build a map with the Paramset
			Map<String,Object> map=new HashMap<String,Object>();
			map.put(args.args[2],m.guessType(args.args[3]));
			m.addArg(map);

			HMXRResponse r=HMC.connections[dix].sendRequest(m);
			// Ignore response
		}
	}
}
