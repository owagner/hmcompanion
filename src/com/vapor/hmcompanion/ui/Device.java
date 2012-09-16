/*
 * $Id: Device.java,v 1.5 2012-09-16 07:42:34 owagner Exp $
 */

package com.vapor.hmcompanion.ui;

import java.util.*;

import com.vapor.hmcompanion.*;
import com.vapor.hmcompanion.ui.RSSI.RSSIInfo;

public class Device implements Comparable<Device>
{
	String address;
	String interf;
	String type;
	String firmware;
	boolean roaming;
	boolean usesAES;
	
	String name; // ReGa name
	
	RSSIInfo ifrssi[];
	
	Map<String,RSSIInfo> rssi=Collections.emptyMap();
	
	private Device(String address, String interf, String type, boolean roaming, String firmware, Device parent)
	{
		this.address = address;
		this.interf = interf;
		this.type = type;
		this.roaming = roaming;
		this.firmware = firmware;
		this.parent = parent;

		makeRSSIInfo();
	}

	private void makeRSSIInfo()
	{
		ifrssi=new RSSIInfo[BidcosInterface.interfaceList.size()];
		int bestValue=Integer.MIN_VALUE;
		int bestIndex=-1;
		for(int c=0;c<ifrssi.length;c++)
		{
			String ifadr=BidcosInterface.interfaceList.get(c).address;
			
			ifrssi[c]=rssi.get(ifadr);
			if(ifrssi[c]==null)
				ifrssi[c]=new RSSIInfo();
			ifrssi[c].isDef=ifadr.equals(interf);
			
			if(ifrssi[c].to!=65536&&ifrssi[c].to>bestValue)
			{
				bestValue=ifrssi[c].to;
				bestIndex=c;
			}
		}
		for(int c=0;c<ifrssi.length;c++)
		{
			ifrssi[c].isBest=(c==bestIndex);
			ifrssi[c].isWrong=(ifrssi[c].isDef && !ifrssi[c].isBest && bestIndex!=-1);
		}
	}

	public Map<String,Device> subdevices;
	Device parent;
	
	static Map<String,Device> devices=new TreeMap<String,Device>();
	static List<Device> deviceList=Collections.emptyList();
	
	private Device(String address)
	{
		this.address = address;
	}
	
	HMXRMap valueParamsetDescription;
	
	public HMXRMap getValueParamsetDescription()
	{
		if(valueParamsetDescription!=null)
			return valueParamsetDescription;
		
		HMXRMsg m=new HMXRMsg("getParamsetDescription");
		m.addArg(address);
		m.addArg("VALUES");
		HMXRResponse r=MainWin.doRequest(m);
		
		valueParamsetDescription=(HMXRMap)r.getData().get(0);
		return valueParamsetDescription; 
	}
	
	public HMXRMap getValues()
	{
		HMXRMsg m=new HMXRMsg("getParamset");
		m.addArg(address);
		m.addArg("VALUES");
		HMXRResponse r=MainWin.doRequest(m);
		
		return (HMXRMap)r.getData().get(0);
	}
	
	public void setValue(String n,Object v)
	{
		HMXRMsg m=new HMXRMsg("setValue");
		m.addArg(address);
		m.addArg(n);
		m.addUntypedArg(v);
		MainWin.doRequest(m);
	}

	@SuppressWarnings("unchecked")
	static void loadDeviceList()
	{
		HMXRMsg m=new HMXRMsg("listDevices");
		HMXRResponse r=MainWin.doRequest(m);
		Map<String,Device> devices=new HashMap<String,Device>();
		for(Object mi:(List<Object>)r.getData().get(0))
		{
			HMXRMap o=(HMXRMap)mi;
			Device bi=new Device(
				o.getString("ADDRESS"),
				o.getString("INTERFACE"),
				o.getString("TYPE"),
				o.getInt("ROAMING")!=0,
				o.getString("FIRMWARE"),
				devices.get(o.getString("PARENT"))
			);
			
			int aes=o.getInt("AES_ACTIVE");
			if(aes==1)
				bi.usesAES=true;
			
			if(bi.parent!=null)
			{
				bi.parent.addChild(bi);
			}
			else
			{
				devices.put(bi.address,bi);
			}
			
		}
		Device.devices=devices;
		Device.deviceList=new ArrayList<Device>(devices.values());
	}

	private void addChild(Device bi)
	{
		if(subdevices==null)
			subdevices=new TreeMap<String,Device>();
		subdevices.put(bi.address,bi);
		if(bi.usesAES)
			usesAES=true;
	}

	public String makeLabel()
	{
		StringBuilder l=new StringBuilder();
		l.append("<html>");
		l.append(address);
		if(usesAES)
		{
			if(roaming)
				l.append("<font color='red'>");
			else
				l.append("<font color='blue'>");
			l.append(" (AES)");
			l.append("</font>");
		}
		l.append("<br/>");
		l.append(name!=null?name:"(no ReGa name)");
		l.append("<br/>");
		l.append(type);
		l.append(" <small>(");
		l.append(firmware);
		l.append(")</small>");

		return l.toString();
	}

	public String getName()
	{
		if(name!=null)
			return name;
		else
			return address;
	}
	
	public static void setNames(Map<String, String> names)
	{
		for(Device d:deviceList)
		{
			d.name=names.get(d.address);
			for(Device sd:d.subdevices.values())
				sd.name=names.get(sd.address);
		}
		List<Device> l=new ArrayList<Device>(deviceList);
		Collections.sort(l);
		Device.deviceList=l;
	}
	
	public static void setRSSI(Map<String,Map<String,RSSIInfo>> rssi)
	{
		for(Device d:deviceList)
		{
			d.rssi=rssi.get(d.address);
			if(d.rssi==null)
				d.rssi=Collections.emptyMap();
			d.makeRSSIInfo();
		}
	}

	private void setBidcosInterface()
	{
		
		HMXRMsg m=new HMXRMsg("setBidcosInterface");
		m.addArg(address);
		m.addArg(interf);
		m.addArg(roaming);
		MainWin.doRequest(m);
		
	}
	
	public void setRoaming(boolean roaming)
	{
		if(roaming!=this.roaming)
		{
			this.roaming=roaming;
			setBidcosInterface();
		}
	}
	public void setInterface(String ifname)
	{
		if(!interf.equals(ifname))
		{
			this.interf=ifname;
			makeRSSIInfo();
			setBidcosInterface();
		}
	}

	public int compareTo(Device o)
	{
		if(name!=null && o.name!=null)
		{
			int rc=name.compareTo(o.name);
			if(rc!=0)
				return rc;
		}
		return address.compareTo(o.address);
	}

	public void setBestInterface()
	{
		int bestix=0;
		for(int ix=0;ix<ifrssi.length;ix++)
		{
			int v=ifrssi[ix].cmpVal();
			if(v>ifrssi[bestix].cmpVal())
				bestix=ix;
		}
		if(ifrssi[bestix].cmpVal()>Integer.MIN_VALUE)
			setInterface(BidcosInterface.interfaceList.get(bestix).address);
	}
	

}
