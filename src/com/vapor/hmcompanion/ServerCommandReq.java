/*
 * $Id: ServerCommandReq.java,v 1.4 2012-09-16 07:42:34 owagner Exp $
 */

package com.vapor.hmcompanion;

public class ServerCommandReq extends ServerCommand
{
	ServerCommandReq()
	{
		super("REQ","<RF|WIRE|SYS|CUXD> <method> <parameters> - send xmlrpc request");
	}

	@Override
	public void exec(Server s, ArgSplitter args) throws Exception
	{
		if(args.args.length<2)
		{
			s.send("-Missing arguments <type> <method>");
			return;
		}
		int dix;
		switch(args.args[0].charAt(0))
		{
			case 'c':
			case 'C':
				dix=3;
				break;
			case 'r':
			case 'R':
				dix=1;
				break;
			case 'w':
			case 'W':
				dix=0;
				break;
			case 's':
			case 'S':
				dix=2;
				break;
			default:
				s.send("-Invalid type, must be RF, WIRE, CUXD or SYS");
				return;
		}

		// Add arguments, with some type guessing:
		// on|true off|false -> Boolean
		// Integer number (without point)
		// Decimal number (with point)
		// everything else: String
		HMXRMsg m=new HMXRMsg(args.args[1]);
		for(int c=2;c<args.args.length;c++)
		{
			String v=args.args[c];
			m.addArgWithTypeGuessing(v);
		}

		HMXRResponse r=HMC.connections[dix].sendRequest(m);
		s.send(r.toString());
		s.send(".");
	}

}
