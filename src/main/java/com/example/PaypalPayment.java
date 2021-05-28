package com.example;

import java.util.*;

import com.paypal.api.payments.*;
import com.paypal.base.rest.*;

public class PaypalPayment
{
	//Sandbox account credentials
	private static final String CLIENT_ID = "ATiRZz-nqrdQNKXK0W83oLKjeszV-7nfUD3Qo9C3Qj0jXhc3UpeZsQ4idAcFqbpZXgtORqhK2HZV1p2k";
	private static final String CLIENT_SECRET = "EM6IEN-wBx5MRswYk_qH0q-uoBvOm1eDtT-XN7PDdaAzbcr4g8zXUTSsg930L0Ni4E6q_iWx5GW60HxX";
	private static final String MODE = "sandbox";

	//Main function carrying out paypal payment
	public String pay(Order order) throws PayPalRESTException {

		Payer payer = getPayerInformation();
		RedirectUrls redirectUrls = getRedirectURLs();
		List<Transaction> listOfTransaction = getTicketInformation(order);

		Payment payment = new Payment();
		payment.setTransactions(listOfTransaction);
		payment.setRedirectUrls(redirectUrls);
		payment.setPayer(payer);
		payment.setIntent("authorize");

		APIContext apicontext = new APIContext(CLIENT_ID, CLIENT_SECRET, MODE);
		Payment payment2 = payment.create(apicontext);

		return getLink(payment2);	
	}

	//Getting the informtation of payer (Random Info)
	private Payer getPayerInformation() {

		Payer payer = new Payer();
		payer.setPaymentMethod("paypal");

		PayerInfo payerInfo = new PayerInfo();
		payerInfo.setFirstName("Asad");
		payerInfo.setLastName("Masood");
		payerInfo.setEmail("asadmasood@example.com");
		//Password: Testing23

		payer.setPayerInfo(payerInfo);

		return payer;
	}
	
	//After paying through paypal, the user will be redirected to the main website which confirms the payment
	private RedirectUrls getRedirectURLs() {

		RedirectUrls redirectUrls = new RedirectUrls();
		redirectUrls.setCancelUrl("https://airline-payment.herokuapp.com/cancel.html");
		redirectUrls.setReturnUrl("https://airline-payment.herokuapp.com/review_payment");

		return redirectUrls;
	}

	//getting the detail of transaction information includes ticket's total price, tax, currency etc. 
	private List<Transaction> getTicketInformation(Order order) {

		Details details = new Details();
		details.setSubtotal(order.getSubTotal());
		details.setTax(order.getTax());

		Amount amount = new Amount();
		amount.setCurrency("GBP");
		amount.setTotal(order.getTotal());
		amount.setDetails(details);

		Transaction transaction = new Transaction();
		transaction.setAmount(amount);
		transaction.setDescription(order.getName());

		ItemList itemList = new ItemList();
		List<Item> items = new ArrayList<Item>();

		Item item = new Item();
		item.setCurrency("GBP");
		item.setName(order.getName());
		item.setPrice(order.getSubTotal());
		item.setTax(order.getTax());
		item.setQuantity("1");

		items.add(item);
		itemList.setItems(items);
		transaction.setItemList(itemList);

		List<Transaction> listOfTransaction = new ArrayList<Transaction>();
		listOfTransaction.add(transaction);

		return listOfTransaction;	

	}

	//After payment is approved, getting link to redirect to main website
	private String getLink(Payment payment) {

		List<Links> links = payment.getLinks();
		String mainLink = null;

		for (Links l : links) {
			if (l.getRel().equalsIgnoreCase("approval_url")) {
				mainLink = l.getHref();
			}
		}
		return mainLink;
	}

	//getting payment details using the payment ID
	public Payment getPaymentDetails(String paymentID) throws PayPalRESTException {
		
		APIContext apiContext = new APIContext(CLIENT_ID, CLIENT_SECRET, MODE);
		return Payment.get(apiContext, paymentID);
	}
}