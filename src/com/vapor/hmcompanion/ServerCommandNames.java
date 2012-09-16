/*
 * $Id: ServerCommandNames.java,v 1.1 2010-05-30 21:13:04 owagner Exp $
 */

package com.vapor.hmcompanion;

import java.util.*;

public class ServerCommandNames extends ServerCommand
{
	ServerCommandNames()
	{
		super("NAMES","Dump all ReGa names");
	}

	@Override
	public void exec(Server s, ArgSplitter args) throws Exception
	{
		// Sort by name
		List<ReGaItem> templist=new ArrayList<ReGaItem>(ReGaDeviceCache.itemsByName.values());
		Collections.sort(templist);

		for(ReGaItem it:templist)
			s.send(it.name+"\t"+it.address+"\t"+it.interf);
		s.send(".");
	}
}
