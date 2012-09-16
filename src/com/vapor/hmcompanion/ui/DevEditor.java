/*
 * $Id: DevEditor.java,v 1.1 2010-10-24 20:11:25 owagner Exp $
 */

package com.vapor.hmcompanion.ui;

import java.util.*;

import javax.swing.*;

public class DevEditor extends JFrame
{
	static Map<Device,DevEditor> editors=new HashMap<Device,DevEditor>();

	private Device myDevice;
	public DevEditor(Device d)
	{
		super("Editing "+d.address+ "("+d.name+")");
		myDevice=d;
	}

	static public void editDevice(Device d)
	{
		DevEditor de=editors.get(d);
		if(de!=null)
		{
			de.setVisible(true);
			return;
		}
		de=new DevEditor(d);
		editors.put(d,de);
	}
}
