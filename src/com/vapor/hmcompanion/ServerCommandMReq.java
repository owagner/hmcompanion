package com.vapor.hmcompanion;

public class ServerCommandMReq extends ServerCommand
{
	ServerCommandMReq()
	{
		super("REQ","<RF|WIRE|SYS> <method> <parameters> - send xmlrpc request");
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
				s.send("-Invalid type, must be RF, WIRE or SYS");
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
