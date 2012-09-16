/*
 * $Id: RSSI.java,v 1.5 2012-09-16 07:42:34 owagner Exp $
 */

package com.vapor.hmcompanion.ui;

import java.util.*;

import javax.swing.*;

import com.vapor.hmcompanion.*;

public class RSSI
{
	static class RSSIInfo
	{
		int from=65536;
		int to=65536;
		boolean isDef,isBest,isWrong;
		private RSSIInfo(int from, int to)
		{
			this.from = from;
			this.to = to;
		}
		public RSSIInfo()
		{
			/* Ignore */
		}
		
		public int cmpVal()
		{
			if(from!=65536 && to!=65536)
				return (from+to)/2;
			else if(from!=65536)
				return from;
			else if(to!=65536)
				return to;
			else
				return Integer.MIN_VALUE;
		}
	}
	
	static Map<String,Map<String,RSSIInfo>> rssi=Collections.emptyMap();
	
	@SuppressWarnings({
		"unchecked", "boxing"
	})
	static void loadRSSIInfo()
	{
		HMXRMsg m=new HMXRMsg("rssiInfo");
		HMXRResponse r=MainWin.doRequest(m);

		Map<String,Map<String,RSSIInfo>> rssi=new HashMap<String,Map<String,RSSIInfo>>();
		
		HMXRMap map=(HMXRMap)r.getData().get(0);
		for(Map.Entry<String,Object> im:map.entrySet())
		{
			// Create the inner map
			Map<String,RSSIInfo> rm=new HashMap<String,RSSIInfo>();
			rssi.put(im.getKey(),rm);

			for(Map.Entry<String,Object> cim:((HMXRMap)im.getValue()).entrySet())
			{
				String ik=cim.getKey();
				
				List<Object> l=(List<Object>)cim.getValue();
				
				RSSIInfo rsi=new RSSIInfo((Integer)l.get(0),(Integer)l.get(1));
				rm.put(ik,rsi);
			}

		}
		
		RSSI.rssi=rssi;
		Device.setRSSI(rssi);
	}


}
