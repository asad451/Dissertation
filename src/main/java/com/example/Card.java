package com.example;


public class Card
{
	private String card_name, card_number, card_cvv, card_exp_date;
	
	//Setters    	    
	public void setCardName(String name) 
	{    
	    this.card_name = name;    
	}    
	public void setCardNumber(String number) 
	{    
	    this.card_number = number;    
	}    
	public void setCardCVV(String cvv) 
	{    
	    this.card_cvv = cvv;    
	}    
	public void setCardExpDate(String exp_date) 
	{    
	    this.card_exp_date = exp_date;    
	}
	 
	//Getters    
	public String getCardName() 
	{    
	    return card_name;    
	}    
	public String getCardNumber() 
	{    
	    return card_number;    
	}    
	public String getCardCVV() 
	{    
	    return card_cvv;    
	} 
	public String getCardExpDate() 
	{    
	    return card_exp_date;    
	}
}