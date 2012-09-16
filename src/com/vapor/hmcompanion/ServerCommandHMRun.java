/*
 * $Id: ServerCommandHMRun.java,v 1.1 2010-07-18 17:58:26 owagner Exp $
 */

package com.vapor.hmcompanion;

public class ServerCommandHMRun extends ServerCommand
{
	ServerCommandHMRun()
	{
		super("HMRUN","<program> [<program2>...] - execute HMScript programs");
	}
	
	@Override
	public void exec(Server s, ArgSplitter args) throws Exception
	{
		if(args.args.length<1)
		{
			s.send("-Missing argument <program>");
			return;
		}
		for(String a:args.args)
		{
			StringBuilder script=new StringBuilder();
			script.append("Write(dom.GetObject(\"");
			script.append(a);
			script.append("\").ProgramExecute());");
			s.send(TCLRegaHandler.sendHMScript(script.toString()));
		}
		s.send(".");
	}

}
