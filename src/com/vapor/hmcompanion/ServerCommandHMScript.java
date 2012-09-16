/*
 * $Id: ServerCommandHMScript.java,v 1.2 2010-05-30 21:13:04 owagner Exp $
 */

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
