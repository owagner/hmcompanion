/*
 * This is the client-side server process, used for sending commands to HMCompanion.
 *
 * Use a simple line-based protocol suitable for use with netcat or PHP
 *
 * $Id: XMLRPCServer.java,v 1.8 2012-09-16 07:42:34 owagner Exp $
 *
 */

package com.vapor.hmcompanion;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

public class XMLRPCServer extends Thread
{
	private static ServerSocket ss;
	static final int port=6778;
	public static String init() throws IOException
	{
		ss=new ServerSocket();
		ss.setReuseAddress(true);
		ss.bind(new InetSocketAddress(port));
		new XMLRPCAcceptor().start();
        InetAddress addr=InetAddress.getLocalHost();
        return "binary://"+System.getProperty("hmc.localhost",addr.getHostAddress())+":"+port;
	}

	static long lastRequest;

	static final class XMLRPCAcceptor extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				for(;;)
				{
					Socket s=ss.accept();
					s.setKeepAlive(true);
					new XMLRPCServer(s).start();
				}
			}
			catch(Exception e)
			{
				/* Ignore */
			}
		}
	}

	static Map<String,Long> stats=new HashMap<String,Long>();
	@SuppressWarnings("boxing")
	static void incMsg(String key)
	{
		Long l=stats.get(key);
		if(l==null)
			stats.put(key,new Long(1));
		else
			stats.put(key,l+1);
	}

	@SuppressWarnings("boxing")
	static long getStats(String which)
	{
		Long l=stats.get(which);
		if(l==null)
			return 0;
		else
			return l;
	}

	Socket s;
	OutputStream os;
	XMLRPCServer(Socket s)
	{
		super("Handler for "+s);
		this.s=s;
	}

	private static Map<String,String> buttonlinks=new HashMap<String,String>();
	private static Set<String> buttonlong=new HashSet<String>();
	static {
		String bm=System.getProperty("hmc.buttonlinks");
		if(bm!=null)
		{
			String bms[]=bm.split(",");
			for(String b:bms)
			{
				String parts[]=b.split("=",2);
				buttonlinks.put(parts[0],parts[1]);
			}
		}
	}

	private void doExec(String exec, String address, String item, String val)
	{
		String cmd=exec+" \""+address+"\" \""+item+"\"";
		if(val!=null)
			cmd=cmd+" \""+val+"\"";
		try
		{
			HMC.l.info("Executing '"+cmd+"'");
			Runtime.getRuntime().exec(cmd);
		}
		catch (IOException ioe)
		{
			HMC.l.log(Level.WARNING, "Failed to execute '"+cmd+"'", ioe);
		}
	}

	private void tryExec(String address, String item, String val)
	{
		String key=address+"."+item+"."+val;
		String exec=System.getProperty(key);
		if(exec!=null)
		{
			doExec(exec, address, item, val);
			return;
		}
		ReGaItem dev=ReGaDeviceCache.getItemByName(address);
		if(dev==null)
			return;
		key=dev.name+"."+item+"."+val;
		exec=System.getProperty(key);
		if(exec==null)
			return;
		doExec(exec, dev.name, item, val);
	}

	private String handleEvent(List<Object> parms)
	{
		String address=parms.get(1).toString();
		String item=parms.get(2).toString();
		Object val=parms.get(3);
		AttributeCache.putAttribute(address,item,val);
		tryExec(address, item, val==null?null:val.toString());

		// Check button mappings
		if(item.startsWith("PRESS_")||("INSTALL_TEST".equals(item)&&buttonlong.contains(address)))
		{
			String dest=buttonlinks.get(address);
			if(dest!=null)
			{
				if("PRESS_LONG".equals(item))
					buttonlong.add(address);
				else if("PRESS_SHORT".equals(item))
					buttonlong.remove(address);
				else
					item="PRESS_LONG";

				Collection<ReGaItem> items=ReGaDeviceCache.getItemsByName(dest);
				HMC.l.fine("Mapping button "+item+"="+val+" from "+address+" to "+dest+" ("+items+")");
				if(items!=null)
				{
					for(ReGaItem it:items)
					{
						final int dix;
						if("BidCos-RF".equals(it.interf))
							dix=1;
						else if("BidCos-Wired".equals(it.interf))
							dix=0;
						else
							dix=2;

						final HMXRMsg m=new HMXRMsg("setValue");
						m.addArg(it.address);
						m.addArg(item);
						m.addArg(Boolean.TRUE);
						// Send on second thread
						new Thread(){
							@Override
							public void run()
							{
								try
								{
									HMC.connections[dix].sendRequest(m);
								}
								catch(Exception e)
								{
									/* Ignore, don't care */
								}
							}
						}.start();
					}
				}
			}
		}

		return parms.get(0).toString();
	}

	@SuppressWarnings("unchecked")
	private void handleMethodCall(HMXRResponse r) throws IOException,ParseException
	{
		lastRequest=System.currentTimeMillis();
		if("event".equals(r.methodName))
		{
			String cb=handleEvent(r.rd);
			incMsg(cb);
			os.write(bEmptyString);
		}
		else if("listDevices".equals(r.methodName))
		{
			os.write(bEmptyArray);
		}
		else if("newDevices".equals(r.methodName))
		{
			// Hm we ignore that
			os.write(bEmptyArray);
		}
		else if("system.multicall".equals(r.methodName))
		{
			HMXRMsg m=new HMXRMsg(null);

			List<Object> result=new ArrayList<Object>();

			String cb=null;

			for(Object o:(List<Object>)r.rd.get(0))
			{
				Map<String,Object> call=(Map<String,Object>)o;
				String method=call.get("methodName").toString();
				if("event".equals(method))
				{
					cb=handleEvent((List<Object>)call.get("params"));
				}
				else
					HMC.l.warning("Unknown method in multicall called by CCU:"+method);

				result.add(Collections.singletonList(""));
			}

			incMsg(cb);

			m.addArg(result);
			os.write(m.prepareData());
		}
		else
		{
			HMC.l.warning("Unknown method called by CCU: "+r.methodName);
		//	os.write(bfalse);
		}
	}

	static final byte bEmptyString[]={'B','i','n',0, 0,0,0,8, 0,0,0,3, 0,0,0,0};
	static final byte bEmptyArray[]={'B','i','n',0, 0,0,0,8, 0,0,1,0, 0,0,0,0};

	static final byte btrue[]= {'B','i','n',0, 0,0,0,5, 0,0,0,2, 1};
	static final byte bfalse[]={'B','i','n',0, 0,0,0,5, 0,0,0,2, 1};

	@Override
	public void run()
	{
		HMC.l.fine("Accepted XMLRPC-BIN connection from "+s.getRemoteSocketAddress());
		try
		{
			os=s.getOutputStream();
			for(;;)
			{
				HMXRResponse r=HMXRResponse.readMsg(s,true);
				handleMethodCall(r);
			}
		}
		catch(EOFException eof)
		{
			/* Client closed, ignore */
		}
		catch(Exception e)
		{
			HMC.l.log(Level.INFO,"Closing connection",e);
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
