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
