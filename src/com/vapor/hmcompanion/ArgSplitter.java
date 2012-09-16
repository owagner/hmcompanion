/*
 * $Id: ArgSplitter.java,v 1.1 2010-05-30 21:13:04 owagner Exp $
 */

package com.vapor.hmcompanion;

import java.util.*;

public class ArgSplitter
{
	String args[];
	String fullLine;
	
	public ArgSplitter(String line)
	{
		ArrayList<String> parsed=new ArrayList<String>();
		this.fullLine=line;
		
		StringBuilder arg=null;
		boolean inQuotedString=false;
		for(int ix=0;ix<line.length();ix++)
		{
			if(arg==null)
				arg=new StringBuilder();
			char ch=line.charAt(ix);
			
			if(ch=='\\')
			{
				ix++;
				arg.append(line.charAt(ix));
				continue;
			}
			
			if(inQuotedString)
			{
				if(ch=='"')
				{
					// We also add empty strings here, unlike in the unquoted branch
					parsed.add(arg.toString());
					arg=null;
					inQuotedString=false;
				}
				else
					arg.append(ch);
			}
			else
			{
				if(ch==' ')
				{
					if(arg.length()>0)
					{
						parsed.add(arg.toString());
					}
					arg=null;
				}
				else if(ch=='"' && arg.length()==0)
				{
					// Quote at beginning of string only
					inQuotedString=true;
					continue;
				}
				else
					arg.append(ch);
			}
		}
		if(arg!=null)
			parsed.add(arg.toString());
		args=parsed.toArray(new String[parsed.size()]);
	}
}
