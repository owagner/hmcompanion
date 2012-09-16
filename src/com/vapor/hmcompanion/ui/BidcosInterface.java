package com.vapor.hmcompanion.ui;

import java.util.*;

import com.vapor.hmcompanion.*;

public class BidcosInterface
{
	String address;
	boolean connected;
	boolean def;
	String description;

	static Map<String,BidcosInterface> interfaces=new TreeMap<String,BidcosInterface>();
	static List<BidcosInterface> interfaceList=Collections.emptyList();

	private BidcosInterface(String address, String description, boolean connected, boolean def)
	{
		this.address = address;
		this.connected = connected;
		this.def = def;
		this.description = description;
	}

	@SuppressWarnings("unchecked")
	static void loadInterfacelist()
	{
		HMXRMsg m=new HMXRMsg("listBidcosInterfaces");
		HMXRResponse r=MainWin.doRequest(m);
		Map<String,BidcosInterface> interfaces=new TreeMap<String,BidcosInterface>();
		for(Object mi:(List<Object>)r.getData().get(0))
		{
			HMXRMap o=(HMXRMap)mi;

			BidcosInterface bi=new BidcosInterface(
				o.getString("ADDRESS"),
				o.getString("DESCRIPTION"),
				o.getBool("CONNECTED"),
				o.getBool("DEFAULT")
			);

			interfaces.put(bi.address,bi);
		}
		BidcosInterface.interfaces=interfaces;
		BidcosInterface.interfaceList=new ArrayList<BidcosInterface>(interfaces.values());
	}
}
