/*
 * $Id: ServerCommand.java,v 1.9 2010-09-26 22:02:11 owagner Exp $
 */

package com.vapor.hmcompanion;

import java.util.*;

public abstract class ServerCommand
{
	static Map<String,ServerCommand> commands=new HashMap<String,ServerCommand>();

	static ServerCommand get(String cmd)
	{
		return commands.get(cmd);
	}
	static private void add(ServerCommand sc)
	{
		commands.put(sc.cmd,sc);
	}
	static void initCommands()
	{
		add(new ServerCommandHelp());
		add(new ServerCommandQuit());
		add(new ServerCommandCGet());
		add(new ServerCommandGet());
		add(new ServerCommandMGet());
		add(new ServerCommandSet());
		add(new ServerCommandSetParam());
		add(new ServerCommandReq());
		add(new ServerCommandHMScript());
		add(new ServerCommandHMSet());
		add(new ServerCommandHMGet());
		add(new ServerCommandHMRun());
		add(new ServerCommandNames());
		add(new ServerCommandStats());
	}

	/* Helpers */
	static String parseAddress(String name)
	{
		// Is name already a BidCoS address?
		if(name.matches("[A-Z]{3}[0-9]{7}(:[0-9]+)?"))
			return name;
		ReGaItem it=ReGaDeviceCache.getItemByName(name);
		if(it!=null)
		{
			//System.out.println(name +" -> "+it.address);
			return it.address;
		}
		else
		{
			HMC.l.warning("Unable to resolve name "+name);
			return name;
		}
	}

	String cmd;
	String desc;

	ServerCommand(String cmd,String desc)
	{
		this.cmd=cmd;
		this.desc=desc;
	}

	public abstract void exec(Server s,ArgSplitter args) throws Exception;
}
