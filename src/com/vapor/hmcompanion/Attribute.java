/*
 * Represents one attribute and it's metadata
 *
 * $Id: Attribute.java,v 1.2 2010-06-02 23:06:01 owagner Exp $
 *
 */

package com.vapor.hmcompanion;

import java.io.*;
import java.util.*;

public class Attribute implements Serializable
{
	Object value;
	Date lastUpdate;
	Date lastChange;

	public void set(Object nv)
	{
		lastUpdate=new Date();
		if(value==null||!value.equals(nv))
			lastChange=lastUpdate;
		value=nv;
	}

	@Override
	public String toString()
	{
		return value.toString();
	}
}
