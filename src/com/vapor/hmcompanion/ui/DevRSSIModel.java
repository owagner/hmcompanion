/*
 * $Id: DevRSSIModel.java,v 1.4 2012-09-16 07:42:34 owagner Exp $
 */

package com.vapor.hmcompanion.ui;

import javax.swing.table.*;

@SuppressWarnings("serial")
public class DevRSSIModel extends AbstractTableModel
{
	public int getColumnCount()
	{
		return 1+BidcosInterface.interfaces.size()+1;
	}

	public int getRowCount()
	{
		return Device.deviceList.size();
	}

	public Object getValueAt(int row, int col)
	{
		Device d=Device.deviceList.get(row);

		if(col==0)
			return d.makeLabel();
		else if(col>=1 && col<=BidcosInterface.interfaces.size())
			return d.ifrssi[col-1];
		else if(col==1+BidcosInterface.interfaces.size())
			return d.roaming;
		else
			return "?";
	}

	@Override
	public String getColumnName(int col)
	{
		if(col==0)
			return "Device";
		else if(col>=1 && col<=BidcosInterface.interfaces.size())
		{
			BidcosInterface i=BidcosInterface.interfaceList.get(col-1);

			if(i.description!=null)
				return "IF:"+i.description+" ("+i.address+")";

			return "IF:"+i.address;
		}
		else if(col==BidcosInterface.interfaces.size()+1)
			return "Roaming?";
		else
			return "?";
	}

	@Override
	@SuppressWarnings({
		"rawtypes", "unchecked"
	})
	public Class getColumnClass(int col)
	{
		if(col>=1 && col<BidcosInterface.interfaces.size())
		{
			return RSSI.RSSIInfo.class;
		}
		else if(col==1+BidcosInterface.interfaces.size())
			return Boolean.class;
		return String.class;
	}

	@Override
	public boolean isCellEditable(int row, int col)
	{
		return col>=1 && col<=1+BidcosInterface.interfaces.size();
	}

	@Override
	public void setValueAt(Object val, int row, int col)
	{
		Device d=Device.deviceList.get(row);

		if(col==1+BidcosInterface.interfaces.size())
			d.setRoaming((Boolean)val);
		if(col>=1&&col<1+BidcosInterface.interfaces.size())
		{
			d.setInterface(BidcosInterface.interfaceList.get(col-1).address);
			fireTableRowsUpdated(row,row);
		}
	}

}
