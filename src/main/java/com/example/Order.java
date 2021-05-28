package com.example;


public class Order
{
	String total, subTotal, tax, name;

	//Setters    
	public void setName(String n)
	{
		name = n;
	}
	public void setTax(String x)
	{
		tax = x;
	}
	public void setSubTotal(String s)
	{
		subTotal = s;
	}
	public void setTotal(String t)
	{
		total = t;
	}

	//Getters    
	public String getName()
	{
		return name;
	}
	public String getTax()
	{
		return tax;
	}	
	public String getSubTotal()
	{
		return subTotal;
	}
	public String getTotal()
	{
		return total;
	}
}