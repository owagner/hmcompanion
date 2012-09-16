package com.vapor.hmcompanion;

class ReGaItem implements Comparable<ReGaItem>
{
	int id;
	String name;
	String address;
	String interf;
	ReGaItem(int id, String name, String address, String interf)
	{
		this.id = id;
		this.name = name;
		this.address = address;
		this.interf = interf;
	}

	@Override
	public String toString()
	{
		return "["+id+":"+name+"/"+address+"/"+interf+"]";
	}

	public int compareTo(ReGaItem o)
	{
		return name.compareTo(o.name);
	}
}