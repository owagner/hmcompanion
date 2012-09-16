/*
 * Represents one attribute and it's metadata
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
