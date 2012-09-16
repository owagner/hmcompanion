/*
 * $Id: ServerCommandHelp.java,v 1.2 2010-05-30 21:13:04 owagner Exp $
 */

package com.vapor.hmcompanion;

public class ServerCommandHelp extends ServerCommand
{
	ServerCommandHelp()
	{
		super("HELP","Show all commands");
	}

	@Override
	public void exec(Server s, ArgSplitter args) throws Exception
	{
		for(ServerCommand sc:commands.values())
		{
			s.send(sc.cmd+"\t"+sc.desc);
		}
	}

}
