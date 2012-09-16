/*
 * This is the client-side server process, used for sending commands to HMCompanion.
 *
 * Use a simple line-based protocol suitable for use with netcat or PHP
 */

package com.vapor.hmcompanion;

import java.io.*;
import java.net.*;
import java.util.logging.*;

public class Server extends Thread
{
	private static ServerSocket ss;
	private static String authkey;
	public static void acceptIt(int port,String authkey) throws IOException
	{
		Server.authkey=authkey;
		ServerCommand.initCommands();
		ss=new ServerSocket();
		ss.setReuseAddress(true);
		ss.bind(new InetSocketAddress(port));
		HMC.l.info("Accepting connections on "+ss);
		for(;;)
		{
			Socket s=ss.accept();
			new Server(s).start();
		}
	}

	Socket s;
	PrintWriter pw;
	Server(Socket s)
	{
		super("Handler for "+s);
		this.s=s;
	}

	void send(String txt)
	{
		pw.println(txt);
	}

	private void handleCommand(String l) throws Exception
	{
		String cmdparts[]=l.split(" ",2);
		String cmd=cmdparts[0].toUpperCase();

		ServerCommand sc=ServerCommand.get(cmd);
		if(sc!=null)
		{
			sc.exec(this,new ArgSplitter(cmdparts.length>1?cmdparts[1]:""));
		}
		else
			pw.println("-Unknown command "+cmd);
	}

	@Override
	public void run()
	{
		HMC.l.info("Accepted connection from "+s.getRemoteSocketAddress());
		try
		{
			// Active TCP keepalive
			s.setKeepAlive(true);
			BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
			pw=new PrintWriter(s.getOutputStream());
			String l;

			if(authkey!=null)
			{
				// Require Authentication first
				l=br.readLine();
				if(!l.equals("AUTH "+authkey))
				{
					pw.println("-AUTH failed");
					pw.flush();
					HMC.l.log(Level.WARNING,"Authentication failed: "+l+" expecting: AUTH "+authkey);
					return;
				}
			}

			while((l=br.readLine())!=null)
			{
				handleCommand(l);
				pw.flush();
			}
		}
		catch(EOFException eof)
		{
			/* Don't log */
		}
		catch(Exception e)
		{
			pw.println("-INTERNAL ERROR");
			pw.flush();
			HMC.l.log(Level.FINE,"Closing connection",e);
		}
		finally
		{
			try
			{
				s.close();
			}
			catch(Exception e)
			{
				// We don't care here
			}
		}
	}
}
