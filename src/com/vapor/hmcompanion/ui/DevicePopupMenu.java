package com.vapor.hmcompanion.ui;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import com.vapor.hmcompanion.*;

public class DevicePopupMenu extends JPopupMenu
{
	public DevicePopupMenu(Device d)
	{
		super();

		addEntries(d,d.getValueParamsetDescription());
		for(Device sd:d.subdevices.values())
		{
			addEntries(sd,sd.getValueParamsetDescription());
		}
	}

	private void addSetter(final JMenuItem mit,final Device d,final String n,final Object val)
	{
		mit.addActionListener(new ActionListener(){
			@SuppressWarnings("boxing")
			public void actionPerformed(ActionEvent e)
			{
				if(val==null)
				{
					// Checkbox or action
					if(mit instanceof JCheckBoxMenuItem)
					{
						Boolean sel=mit.isSelected();
						d.setValue(n,sel);
					}
					else
						d.setValue(n,Boolean.TRUE);
				}
				else
				{
					d.setValue(n,val);
				}
			}
		});
	}

	private void addEntries(Device d,HMXRMap v)
	{
		HMXRMap vals=d.getValues();

		if(v.getString("faultString")!=null)
			return;

		JMenu m=new JMenu(d.getName());
		boolean didAdd=false;
System.out.println(v);
		for(Map.Entry<String,Object> e:v.entrySet())
		{
			String lab=e.getKey();
			HMXRMap parms=(HMXRMap)e.getValue();
			String type=parms.getString("TYPE");
			int ops=parms.getInt("OPERATIONS");
			if((ops&2)!=0)
			{
				// Ok this parameter is settable
				if("BOOL".equals(type))
				{
					JCheckBoxMenuItem mit=new JCheckBoxMenuItem(lab);
					mit.setSelected(vals.getBool(lab));
					addSetter(mit,d,lab,null);
					m.add(mit);
					didAdd=true;
				}
				else if("ACTION".equals(type))
				{
					JMenuItem mit=new JMenuItem("\u21D2"+lab);
					addSetter(mit,d,lab,null);
					m.add(mit);
					didAdd=true;
				}
				else if("FLOAT".equals(type))
				{
					String unit=parms.getString("UNIT");

					if("100%".equals(unit))
					{
						JMenu mg=new JMenu(lab+" "+Math.round(vals.getDouble(lab)*100)+"%");
						for(int c=0;c<10;c++)
						{
							JMenuItem mit=new JMenuItem((c*10)+"%");
							addSetter(mit,d,lab,new Double(c/10.0));
							mg.add(mit);
						}
						m.add(mg);
						didAdd=true;
					}
					else
					{

						JMenu mg=new JMenu(lab+" "+vals.get(lab));
						double min=parms.getDouble("MIN");
						double max=parms.getDouble("MAX");

						for(int c=0;c<10;c++)
						{
							double sv=min + ((max-min)/10)*c;

							JMenuItem mit=new JMenuItem(String.valueOf(sv));
							mg.add(mit);
						}
						m.add(mg);
						didAdd=true;
					}
				}
			}
			else
			{
				// Display only
				Object vlab=vals.get(lab);

				if("ENUM".equals(type))
				{
					List<?> vl=(List<?>)parms.get("VALUE_LIST");
					if(vl!=null)
						vlab=vl.get(vals.getInt(lab));
				}

				String unit=parms.getString("UNIT");
				unit=unit.replace("&#176;","o");
				if(unit==null || unit.length()==0)
					unit="";

				JMenuItem mit=new JMenuItem(lab+": "+vlab+unit);
				mit.setEnabled(false);
				m.add(mit);
				didAdd=true;
			}
		}

		if(didAdd)
			add(m);
	}
}
