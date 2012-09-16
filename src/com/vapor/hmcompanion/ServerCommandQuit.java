/*
 * $Id: ServerCommandQuit.java,v 1.3 2010-09-26 22:02:11 owagner Exp $
 */

package com.vapor.hmcompanion;

import java.io.*;

public class ServerCommandQuit extends ServerCommand
{
	ServerCommandQuit()
	{
		super("QUIT","Terminate this session (-exit to terminate HMC)");
	}

	@Override
	public void exec(Server s, ArgSplitter args) throws Exception
	{
		if(args.args.length>0)
		{
			if("-exit".equalsIgnoreCase(args.args[0]))
			{
				HMC.l.info("Shutting down on command");
				System.exit(0);
			}
		}
		throw new EOFException();
	}

}
