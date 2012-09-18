package com.vapor.hmcompanion.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class RSSIRenderer extends DefaultTableCellRenderer
{
	JCheckBox cb_def=new JCheckBox("Default?");
	JLabel lab_from,lab_to;

	JPanel pan;

	public class RSSIEditor extends AbstractCellEditor implements TableCellEditor
	{
		RSSIRenderer r;

		@Override
		public boolean isCellEditable(EventObject arg0)
		{
			return true;
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
		{
			r=new RSSIRenderer();
			r.cb_def.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0)
				{
					SwingUtilities.invokeLater(new Runnable(){
						public void run()
						{
							stopCellEditing();
						}
					});
				}
			});
			return r.getTableCellRendererComponent(table, value, isSelected, true, row, column);
		}

		@SuppressWarnings("boxing")
		public Object getCellEditorValue()
		{
			return r.cb_def.isSelected();
		}
	}

	RSSIEditor editor=new RSSIEditor();

	public RSSIRenderer()
	{
		pan=new JPanel(new BorderLayout());

		JPanel pan_lab=new JPanel(new GridLayout(1,2, 2,0));

		lab_from=new JLabel("-");
		lab_from.setOpaque(true);
		lab_from.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

		lab_to=new JLabel("-");
		lab_to.setOpaque(true);
		lab_to.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		lab_to.setHorizontalTextPosition(RIGHT);

		pan_lab.add(lab_from);
		pan_lab.add(lab_to);

		pan.add(cb_def,BorderLayout.NORTH);
		pan.add(pan_lab,BorderLayout.CENTER);

	}

	private int rssic(int rssi,int lower_bound,int upper_bound)
	{
		int result=256*(rssi-lower_bound)/(upper_bound-lower_bound);
		if(result<0)
			return 0;
		else if(result>255)
			return 255;
		else
			return result;
	}

	private void setBGColor(JLabel l,int v)
	{
		if(v==65536)
		{
			l.setBackground(new Color(220,220,220));
			l.setText(" ");
			return;
		}

		l.setText(v+"dBm");

		l.setBackground(new Color(
			rssic(v,-20,-100),
			rssic(v,-120,-80),
		0));
	}
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		RSSI.RSSIInfo ri=(RSSI.RSSIInfo)value;

		cb_def.setSelected(ri.isDef);

		setBGColor(lab_from,ri.from);
		setBGColor(lab_to,ri.to);


		if(ri.isBest)
			cb_def.setBackground(new Color(230,255,230));
		else if(ri.isWrong)
			cb_def.setBackground(new Color(255,230,230));
		else
			cb_def.setBackground(Color.white);

		return pan;
	}
}
