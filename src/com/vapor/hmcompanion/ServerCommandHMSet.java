package com.vapor.hmcompanion;

public class ServerCommandHMSet extends ServerCommand
{
	ServerCommandHMSet()
	{
		super("HMSET","<variable> <value> - Set HMScript system variable");
	}

	@Override
	public void exec(Server s, ArgSplitter args) throws Exception
	{
		if(args.args.length!=2)
		{
			s.send("-specify two arguments: variable value");
			return;
		}
		StringBuilder script=new StringBuilder();
		script.append("Write(dom.GetObject(\"");
		script.append(args.args[0]);
		script.append("\").State(\"");

		String a=args.args[1];
		a=a.replace("\\","\\\\");
		a=a.replace("\"","\\\"");

		script.append(a);
		script.append("\"));");

		s.send(TCLRegaHandler.sendHMScript(script.toString()));
		s.send(".");
	}

}
