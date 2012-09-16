package com.vapor.hmcompanion;

public class ServerCommandStats extends ServerCommand
{
	ServerCommandStats()
	{
		super("STATS","Get statistics");
	}

	@Override
	public void exec(Server s, ArgSplitter args) throws Exception
	{
		StringBuilder res=new StringBuilder();

		res.append("MSGWIRED:");
		res.append(XMLRPCServer.getStats("CB0"));
		res.append(" MSGRF:");
		res.append(XMLRPCServer.getStats("CB1"));
		res.append(" MSGPFMD:");
		res.append(XMLRPCServer.getStats("CB2"));

		s.send(res.toString());
	}
}
