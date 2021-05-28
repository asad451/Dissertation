package com.example;


public class User
{
	String name, uname, password, code;

	//Setters
	public void setName(String n)
	{
		name = n;
	}
	public void setuName(String un)
	{
		uname = un;
	}
	public void setPassword(String pwd)
	{
		password = pwd;
	}
	public void setCode(String c)
	{
		code = c;
	}

	//Getters
	public String getName()
	{
		return name;
	}
	public String getuName()
	{
		return uname;
	}
	public String getPassword()
	{
		return password;
	}
	public String getCode()
	{
		return code;
	}
}