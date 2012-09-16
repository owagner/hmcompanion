/*
 * $Id: MainWin.java,v 1.6 2012-09-16 07:42:34 owagner Exp $
 */

package com.vapor.hmcompanion.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import com.vapor.hmcompanion.*;

public class MainWin extends JFrame
{
	static MainWin win;
	static HMXRConnection con;
	
	private JLabel statusbar;
	
	JTable tab_rssi;
	DevRSSIModel model_rssi;
	
	private MainWin()
	{
		super("HMCompanion V"+HMC.version+" - (C) 2012 Oliver Wagner <owagner@vapor.com>, All Rights Reserved");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		ImageIcon appiconIcon=new ImageIcon(getClass().getResource("icon.png"));
		setIconImage(appiconIcon.getImage());
		
		JPanel panel_buttons=new JPanel();
		
		JButton bt_reload=new JButton("Refresh RSSI");
		panel_buttons.add(bt_reload);
		
		panel_buttons.add(new JSeparator(SwingConstants.VERTICAL));
		
		JButton bt_best=new JButton("Default To Best");
		panel_buttons.add(bt_best);
		
		JButton bt_roamingoff=new JButton("Roaming Off All");
		panel_buttons.add(bt_roamingoff);
		
		JButton bt_roamingon=new JButton("Roaming On HM-RC");
		panel_buttons.add(bt_roamingon);
		
		JPanel panel_main=new JPanel(new BorderLayout());
		
		statusbar=new JLabel("Starting up...");
		statusbar.setBorder(BorderFactory.createLoweredBevelBorder());
		getContentPane().add(statusbar,BorderLayout.SOUTH);
		
		model_rssi=new DevRSSIModel();
		tab_rssi=new JTable(model_rssi);
		
		tab_rssi.addMouseListener(new MouseAdapter(){

			@Override
			public void mousePressed(MouseEvent e)
			{
				checkPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				checkPopup(e);
			}

			private void checkPopup(MouseEvent e)
			{
				if(e.isPopupTrigger())
					showDevicePopup(e);
			}
			
		});
		
		panel_main.add(panel_buttons,BorderLayout.NORTH);
		panel_main.add(new JScrollPane(tab_rssi),BorderLayout.CENTER);
		
		getContentPane().add(panel_main);
		
		// Events
		bt_reload.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
			{
				refreshRSSI();
			}
		});
		bt_best.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
			{
				setDefaultToBest();
			}
		});
		bt_roamingon.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
			{
				setRoamingOn();
			}
		});
		bt_roamingoff.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
			{
				setRoamingOff();
			}
		});
	}
	
	protected void showDevicePopup(MouseEvent e)
	{
		// Determine the currently selected device
		int row=tab_rssi.rowAtPoint(e.getPoint());
		Device d=Device.deviceList.get(row);
		
		JPopupMenu m=new DevicePopupMenu(d);
		m.show(e.getComponent(),e.getX(),e.getY());
	}

	protected void setRoamingOff()
	{
		if(JOptionPane.showConfirmDialog(this,"<html>Set all Devices to <b>Roaming off</b>?","Roaming Off",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)
			return;
		
		for(Device d:Device.deviceList)
			d.setRoaming(false);
		tab_rssi.repaint();
	}

	protected void setRoamingOn()
	{
		if(JOptionPane.showConfirmDialog(this,"<html>Set all Remotes (HM-RC-*) to <b>Roaming on</b>?","Roaming On HM-RC",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)
			return;
		
		for(Device d:Device.deviceList)
		{
			if(d.type.startsWith("HM-RC"))
				d.setRoaming(true);
		}
		tab_rssi.repaint();
	}

	protected void setDefaultToBest()
	{
		if(JOptionPane.showConfirmDialog(this,"<html>Set all Devices default IF to <b>Best RSSI</b>?","Default To Best RSSI",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)
			return;
		
		for(Device d:Device.deviceList)
		{
			d.setBestInterface();
		}
		tab_rssi.repaint();
	}

	void refreshRSSI()
	{
		new Thread(){
			@Override
			public void run()
			{
				RSSI.loadRSSIInfo();
				tab_rssi.repaint();
			}
		}.start();
	}
	
	public static void launchUI(String host,String port)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			JFrame.setDefaultLookAndFeelDecorated(true);
		}
		catch(Exception e)
		{
			/* Ignore */
		}
		
		win=new MainWin();
		win.pack();
		win.setLocationByPlatform(true);
		win.setVisible(true);
		
		win.doLogin(host,port);
	}

	public static HMXRResponse doRequest(HMXRMsg m)
	{
		try
		{
			win.statusbar.setText("Executing "+m.getMethodName()+"...");
			win.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			HMXRResponse r=con.sendRequest(m);
			win.statusbar.setText("Method "+m.getMethodName()+" completed");
			return r;
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(win,
				"Connection to BidCoS-Server/CCU failed:\n"+e.getLocalizedMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE
			);
			return null;
		}
		finally
		{
			win.setCursor(Cursor.getDefaultCursor());
		}
	}
	
	private boolean tryLogin(String host,String port) throws IOException
	{
		String ps[]=port.split(" ");
		
		con=new HMXRConnection(
			host,
			Integer.parseInt(ps[0]),
			null,
			0
		);

		// Check connection
		HMXRMsg m=new HMXRMsg("system.listMethods");
		doRequest(m);

		TCLRegaHandler.setHMHost(host);
		
		BidcosInterface.loadInterfacelist();
		Device.loadDeviceList();
		
		Map<String,String> names=ReGaDeviceCache.loadDeviceNames();
		Device.setNames(names);
		
		RSSI.loadRSSIInfo();
		
		model_rssi.fireTableStructureChanged();
		
		RSSIRenderer r=new RSSIRenderer();
		for(int c=1;c<1+BidcosInterface.interfaceList.size();c++)
		{
			tab_rssi.getColumnModel().getColumn(c).setCellRenderer(r);
			tab_rssi.getColumnModel().getColumn(c).setCellEditor(r.editor);
		}
		
		tab_rssi.setRowHeight(tab_rssi.getRowHeight()*3);
		
		return false;
	}
	
	private void doLogin(String host,String port)
	{
		JComboBox comb_host=new JComboBox();
		JComboBox comb_port=new JComboBox();
		
		comb_host.setEditable(true);
		comb_port.setEditable(true);
		
		comb_port.addItem("2001 (BidCoS-RF)");
		comb_port.addItem("2000 (BidCoS-Wired)");
		comb_port.addItem("2002 (pfmd)");
		comb_port.addItem("8701 (CUxD)");
		
		if(host!=null)
			comb_host.addItem(host);
		
		JPanel selpanel=new JPanel(new GridLayout(2,2, 4,4));
		
		selpanel.add(new JLabel("CCU/BidCoS service hostname or IP address:"));
		selpanel.add(comb_host);
		selpanel.add(new JLabel("Service name or port number:",SwingConstants.RIGHT));
		selpanel.add(comb_port);
		
		int rc=JOptionPane.showConfirmDialog(win,
			selpanel,
			"Connect to CCU",
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.QUESTION_MESSAGE
		);
		if(rc==JOptionPane.CANCEL_OPTION)
			System.exit(0);
		
		try
		{
			tryLogin(comb_host.getSelectedItem().toString(),comb_port.getSelectedItem().toString());
		}
		catch(Exception e)
		{
			/* Ignore */
			HMC.l.log(Level.WARNING,"Login failed",e);
		}
		
	}

}
