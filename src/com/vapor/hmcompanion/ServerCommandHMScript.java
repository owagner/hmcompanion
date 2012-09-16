package com.vapor.hmcompanion;

public class ServerCommandHMScript extends ServerCommand
{
	ServerCommandHMScript()
	{
		super("HMSCRIPT","<script> - execute HMScript");
	}

	@Override
	public void exec(Server s, ArgSplitter args) throws Exception
	{
		if(args.args.length<1)
		{
			s.send("-Missing argument <script>");
			return;
		}
		s.send(TCLRegaHandler.sendHMScript(args.fullLine));
		s.send(".");
	}

}
