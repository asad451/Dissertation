package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.view.RedirectView;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Map;
import java.util.*;
import java.math.BigInteger;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


import com.paypal.api.payments.*;
import com.paypal.base.rest.*;
import com.paypal.base.rest.PayPalRESTException;

@Controller
@SpringBootApplication
public class Main {

  User user;
  Card card;

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  //HomePage
  @RequestMapping("/")
  String index() {
    return "loginpage";
  }


 //Creating Table containing user details
 @RequestMapping("/table1")
  String db1(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE USER_DETAILS(USER_ID SERIAL PRIMARY KEY,NAME TEXT NOT NULL,UNAME TEXT NOT NULL,PWORD TEXT NOT NULL)");
      return "success";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

 //Creating Table containing card details
 @RequestMapping("/table2")
  String db4(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE CARD_DETAIL(CARD_ID SERIAL PRIMARY KEY,CARD_NAME TEXT NOT NULL,CARD_NUMBER TEXT NOT NULL,CARD_CVV TEXT NOT NULL,CARD_EXP TEXT NOT NULL,UID INTEGER REFERENCES USER_DETAILS(USER_ID))");
      return "success";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

 //updating user detail table to add another column
 @RequestMapping("/table6")
  String dba(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("ALTER TABLE USER_DETAILS ADD COLUMN CODE TEXT;");
      return "success";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  //Method containg singup functionality
  @RequestMapping("/signup")
  String db2(Map<String, Object> model, @RequestParam("fname") String name1, @RequestParam("uname") String uname1, @RequestParam("password") String password1, @RequestParam("mfa_email") String toEmail) {
    
    //Generating random 4-digit integer for MFA - Multi Factor Authentication 
    Random r = new Random();
	int c = r.nextInt((9999 - 1000) + 1) + 1000;
	String code = Integer.toString(c);

	//username and password of mail ID for setting up the connection
	final String username = "asadmasood596@gmail.com";
	final String password = "asad12345";

	String fromEmail = "asadmasood451@gmail.com";
	
	//setting up the connection with the mail server SMTP
	Properties properties = new Properties();
	properties.put("mail.smtp.auth", "true");
	properties.put("mail.smtp.starttls.enable", "true");
	properties.put("mail.smtp.host", "smtp.gmail.com");
	properties.put("mail.smtp.port", "587");
	
	Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username,password);
		}
	});

    session.setDebug(true);

    try {

    	//Mail Body
    	MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        message.setSubject("Authentication Code");
        message.setText("The Authentication code is: "+code);

        Transport.send(message);
    } catch (MessagingException e) {
        e.printStackTrace();
    }

    try (Connection connection = dataSource.getConnection()) {
    	
    	//Inserting user details from signup web form into database table 
    	Statement stmt = connection.createStatement();
      	stmt.executeUpdate("INSERT INTO USER_DETAILS (NAME,UNAME,PWORD,CODE) VALUES ('"+name1+"', '"+uname1+"','"+password1+"','"+code+"')");
      	user = new User();
      	user.setName(name1);
      	user.setuName(uname1);
      	user.setPassword(password1);
      	user.setCode(code);
      	model.put("message","You have successfully signed up! Please login using authentication code sent to your email");
      	
      	return "loginpage";

      } catch (Exception e) {
      		model.put("success_message", e.getMessage());
      		return "error";
    	}
  }

 //Method containg login functionality
 @RequestMapping("/login")
 String db3(Map<String, Object> model, @RequestParam("uname") String uname1, @RequestParam("password") String password1, @RequestParam("code") String code) {
    
    try (Connection connection = dataSource.getConnection()) {

    	PreparedStatement stmt = connection.prepareStatement("SELECT * FROM USER_DETAILS where UNAME=? AND PWORD=? AND CODE=?;");
     	stmt.setString(1, uname1);
     	stmt.setString(2,password1);
     	stmt.setString(3,code);
     	
     	ResultSet rs = stmt.executeQuery();
     	user = new User();
    
    	if (rs.next() == false) { 
      		model.put("message", "Invalid Credentials!!! Please Try Again");
      		return "loginpage";
    	}

	    else {
     		do {
        		user.setName(rs.getString("NAME"));
        		user.setuName(uname1);
        		user.setPassword(password1);
        		user.setCode(code);
      		}
      		while (rs.next());
    	}
     	
     	model.put("u",user);
     	model.put("user_name",uname1);
     	
     	return "Home";

      	} catch (Exception e) {
      		model.put("message", e.getMessage());
      		return "loginpage";
    	}
 }

 //Function to check if the user has already entered the card details when logged in previously
 @RequestMapping("/paynow")
 String db6(Map<String, Object> model, @RequestParam("user__name") String username1, @RequestParam("e_mail") String email) {

 	try (Connection connection = dataSource.getConnection()) {

 		int uid = 0;
	    
	    PreparedStatement statement = connection.prepareStatement("SELECT USER_ID FROM USER_DETAILS WHERE UNAME = ?;"); //logged in user
	    statement.setString(1,username1);
	    ResultSet rs = statement.executeQuery();
	    
	    if (rs.next()) {

	    	uid = rs.getInt("USER_ID");        
	    	PreparedStatement statement2 = connection.prepareStatement("SELECT CARD_ID FROM CARD_DETAIL WHERE UID = ?;"); 
	    	statement2.setInt(1,uid);
	    	ResultSet rs2 = statement2.executeQuery();
	    
	    	if (rs2.next()) {
	    		card = new Card();
		      	PreparedStatement statement3 = connection.prepareStatement("SELECT * FROM CARD_DETAIL WHERE UID = ?;");
		      	statement3.setInt(1,uid);
		      	ResultSet rs3 = statement3.executeQuery();

		      	while(rs3.next()) {

			      	RSA rsa = new RSA();

			        //Getting values from the database against a logged in (particular) user
			        String db_name = rs3.getString("CARD_NAME");
			        String db_number = rs3.getString("CARD_NUMBER");
			        String db_exp = rs3.getString("CARD_EXP");
			        String db_cvv = rs3.getString("CARD_CVV");
			         
			        //Un-appending string saved in database to retrieve the values of d and n for decryption
			        String c_name_withoutDN = rsa.unAppend(db_name);
			        String c_number_withoutDN = rsa.unAppend(db_number);
			        String c_exp_withoutDN = rsa.unAppend(db_exp);
			        String c_cvv_withoutDN = rsa.unAppend(db_cvv);
			         
			        //Converting n and d to BigInteger
			        BigInteger new_n = rsa.valueOfN(db_name);
			        BigInteger new_d = rsa.valueOfD(db_name);
			         
			        //Converting String to byte [] for decryption
			        byte[] decodedString1 = Base64.getDecoder().decode(new String(c_name_withoutDN).getBytes("UTF-8"));
			        byte[] decodedString2 = Base64.getDecoder().decode(new String(c_number_withoutDN).getBytes("UTF-8"));
			        byte[] decodedString3 = Base64.getDecoder().decode(new String(c_exp_withoutDN).getBytes("UTF-8"));
			        byte[] decodedString4 = Base64.getDecoder().decode(new String(c_cvv_withoutDN).getBytes("UTF-8"));

			        //decrypting values retrieved from the database
			        byte [] b_name = rsa.decrypt(decodedString1, new_d, new_n);
			        byte [] b_number = rsa.decrypt(decodedString2, new_d, new_n);
			        byte [] b_exp = rsa.decrypt(decodedString3, new_d, new_n);
			        byte [] b_cvv = rsa.decrypt(decodedString4, new_d, new_n);

			        //converting the resulting byte[] values to string for authentication (To check if the user has entered the same values that are stored in database)
			        String s_name = new String(b_name);
			        String s_number = new String(b_number);
			        String s_exp = new String(b_exp);
			        String s_cvv = new String(b_cvv);

			        card.setCardName(s_name);
			        card.setCardNumber(s_number);
			        card.setCardExpDate(s_exp);
			        card.setCardCVV(s_cvv);
		    	}

			    model.put("username1",username1);
		        model.put("address",email);
	    	    model.put("c",card);
	        	return "autofill_pay";	//return to page which autofills all the card details except CVV
	    	}

		    else {
		    	model.put("username1",username1);
	    	    model.put("address",email);
	        	return "pay";	//redirecting the user to page where he has to enter the card details for the first 
	    	}
		}
		else {
			return "error";
		}
	} catch (Exception e) {
		model.put("message", e.getMessage());
		return "error";
	}
}

 //Function to insert the card details into database for the first time after logging in
 @RequestMapping("/pay")
  String db5(Map<String, Object> model, @RequestParam("c_name") String card_name, @RequestParam("u_name") String u_name, @RequestParam("c_num") String card_num, @RequestParam("c_date") String card_date, @RequestParam("c_cvv") String card_cvv, @RequestParam("e_mail") String toEmail) {
  	try (Connection connection = dataSource.getConnection()) {
  		
  		int uid = 0;
      	
      	PreparedStatement statement = connection.prepareStatement("SELECT USER_ID FROM USER_DETAILS WHERE UNAME = ?;"); //logged in user
      	statement.setString(1,u_name);
      	ResultSet rs = statement.executeQuery();
      	if (rs.next()) {
        	uid = rs.getInt("USER_ID");

        	//encryption
        	RSA rsa = new RSA();

	        //Encrypting using RSA after converting string params to byte[]
	        byte [] byte_card_name = rsa.encrypt(card_name.getBytes());
	        byte [] byte_card_num = rsa.encrypt(card_num.getBytes());
	        byte [] byte_card_cvv = rsa.encrypt(card_cvv.getBytes());
	        byte [] byte_card_date = rsa.encrypt(card_date.getBytes());

	        //converting byte[] back to string to store in a database
	        String db_card_name = Base64.getEncoder().encodeToString(byte_card_name);
	        String db_card_num = Base64.getEncoder().encodeToString(byte_card_num);
	        String db_card_cvv = Base64.getEncoder().encodeToString(byte_card_cvv);
	        String db_card_date = Base64.getEncoder().encodeToString(byte_card_date);

	        //Appending the values of n and d to use later for decryption
	        String d_n_val = rsa.append();
	        String appended_card_name = d_n_val + db_card_name;
	        String appended_card_num = d_n_val + db_card_num;
	        String appended_card_cvv = d_n_val + db_card_cvv;
	        String appended_card_date = d_n_val + db_card_date;

	        PreparedStatement stmt = connection.prepareStatement("INSERT INTO CARD_DETAIL(CARD_NAME,CARD_NUMBER,CARD_CVV,CARD_EXP,UID) VALUES (?,?,?,?,?);");
	        stmt.setString(1, appended_card_name);
	        stmt.setString(2, appended_card_num);
	        stmt.setString(3, appended_card_cvv);
	        stmt.setString(4, appended_card_date);
	        stmt.setInt(5, uid);
	        stmt.executeUpdate();
	        card = new Card();
	        card.setCardName(card_name);
	        card.setCardNumber(card_num);
	        card.setCardCVV(card_cvv);
	        card.setCardExpDate(card_date);
	        
	        model.put("c",card);
	        model.put("u1",appended_card_name);
	        model.put("u2",appended_card_num);
	        model.put("u3",appended_card_cvv);
	        model.put("u4",appended_card_date);
	        model.put("u5",uid);

	        //username and password of mail ID for setting up the connection
			final String username = "asadmasood596@gmail.com";
			final String password = "asad12345";
			
			String fromEmail = "asadmasood451@gmail.com";
			
			//Setting up the connection using mail server SMTP
			Properties properties = new Properties();
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.starttls.enable", "true");
			properties.put("mail.smtp.host", "smtp.gmail.com");
			properties.put("mail.smtp.port", "587");
			
			Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username,password);
				}
			});

			
			//Mail Body
			MimeMessage message = new MimeMessage(session);
			try {

				message.setFrom(new InternetAddress(fromEmail));
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
				message.setSubject("Your Flight Itinerary");
				
				Multipart content = new MimeMultipart();
				
				MimeBodyPart textBodyPart = new MimeBodyPart();
				textBodyPart.setText("Welcome on board! Please find attached your flight Itinerary");
				
				MimeBodyPart attachment = new MimeBodyPart();
				attachment.attachFile("Itinerary.pdf");
				
				content.addBodyPart(textBodyPart);
				content.addBodyPart(attachment);
				
				message.setContent(content);
				
				Transport.send(message);

			} catch (MessagingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

	        return "done";
      	}
      	else {
        	model.put("message","maslay");
        	return "error";
      	}
    } catch (Exception e) {
    	model.put("message", e.getMessage());
      	return "error";
    }
 }

 //Function to pay if the user has already entered the card details previously
 @RequestMapping("/autopay")
  String db7(Map<String, Object> model, @RequestParam("c_name") String card_name, @RequestParam("u_name") String u_name, @RequestParam("c_num") String card_num, @RequestParam("c_date") String card_date, @RequestParam("c_cvv") String card_cvv, @RequestParam("e_mail") String toEmail) {
    
    try (Connection connection = dataSource.getConnection()) {

    	int uid = 0;

	    String s_name = "",s_number = "",s_exp = "",s_cvv = "";

	    PreparedStatement statement = connection.prepareStatement("SELECT USER_ID FROM USER_DETAILS WHERE UNAME = ?;"); //logged in user
	    statement.setString(1,u_name);
	    ResultSet rs = statement.executeQuery();
	    
	    if (rs.next()) {
	
	    	uid = rs.getInt("USER_ID");        
      
    	    PreparedStatement statement2 = connection.prepareStatement("SELECT * FROM CARD_DETAIL WHERE UID = ?;"); 
        	statement2.setInt(1,uid);
        	ResultSet rs2 = statement2.executeQuery();

	        while(rs2.next()) {

	        	RSA rsa = new RSA();

	          	//Getting values from the database against a logged in (particular) user
	          	//Using hardcode values as login functionality is not the part of this application
	          	String db_name = rs2.getString("CARD_NAME");
	          	String db_number = rs2.getString("CARD_NUMBER");
	          	String db_exp = rs2.getString("CARD_EXP");
	          	String db_cvv = rs2.getString("CARD_CVV");
	           
	          	//Un-appending string saved in database to retrieve the values of d and n for decryption
	          	String c_name_withoutDN = rsa.unAppend(db_name);
	          	String c_number_withoutDN = rsa.unAppend(db_number);
	          	String c_exp_withoutDN = rsa.unAppend(db_exp);
	          	String c_cvv_withoutDN = rsa.unAppend(db_cvv);
	           
	          	//Converting n and d to BigInteger
	         	BigInteger new_n = rsa.valueOfN(db_name);
	          	BigInteger new_d = rsa.valueOfD(db_name);
	           
	          	//Converting String to byte [] for decryption
	          	byte[] decodedString1 = Base64.getDecoder().decode(new String(c_name_withoutDN).getBytes("UTF-8"));
	          	byte[] decodedString2 = Base64.getDecoder().decode(new String(c_number_withoutDN).getBytes("UTF-8"));
	          	byte[] decodedString3 = Base64.getDecoder().decode(new String(c_exp_withoutDN).getBytes("UTF-8"));
	          	byte[] decodedString4 = Base64.getDecoder().decode(new String(c_cvv_withoutDN).getBytes("UTF-8"));

	          	//decrypting values retrieved from the database
	          	byte [] b_name = rsa.decrypt(decodedString1, new_d, new_n);
	          	byte [] b_number = rsa.decrypt(decodedString2, new_d, new_n);
	          	byte [] b_exp = rsa.decrypt(decodedString3, new_d, new_n);
	          	byte [] b_cvv = rsa.decrypt(decodedString4, new_d, new_n);

	          	//converting the resulting byte[] values to string for authentication (To check if the user has entered the same values that are stored in database)
	          	s_name = new String(b_name);
	          	s_number = new String(b_number);
	          	s_exp = new String(b_exp);
	          	s_cvv = new String(b_cvv);
	        }

	        if (s_name.equals(card_name) && s_number.equals(card_num) && s_exp.equals(card_date) && s_cvv.equals(card_cvv)) {
		        
		    	//username and password of mail ID for setting up the connection
		        final String username = "asadmasood596@gmail.com";
				final String password = "asad12345";
				
				String fromEmail = "asadmasood451@gmail.com";
				
				//Setting up the connection using mail server SMTP
				Properties properties = new Properties();
				properties.put("mail.smtp.auth", "true");
				properties.put("mail.smtp.ssl.enable", "true");
				properties.put("mail.smtp.host", "smtp.gmail.com");
				properties.put("mail.smtp.port", "465");
				
				Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username,password);
					}
				});
				
				//Mail Body
				MimeMessage message = new MimeMessage(session);
				try {

					message.setFrom(new InternetAddress(fromEmail));
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
					message.setSubject("Your Flight Itinerary");
					
					Multipart content = new MimeMultipart();
					
					MimeBodyPart textBodyPart = new MimeBodyPart();
					textBodyPart.setText("Welcome on board! Please find attached your flight Itinerary");
					
					MimeBodyPart attachment = new MimeBodyPart();
					attachment.attachFile("Itinerary.pdf");
					
					content.addBodyPart(textBodyPart);
					content.addBodyPart(attachment);
					
					message.setContent(content);
					
					Transport.send(message);

				} catch (MessagingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
          		model.put("msg","Payment is Successful");
          		return "done";
        	}
        	else {
        		model.put("msg","Payment is Unsuccessful");
          		return "errorpayment"; 
        	}
    	}
    	else {
        	model.put("message","maslay");
        	return "error";
    	}
    } catch (Exception e) {
    	model.put("message", e.getMessage());
    	return "error";
    }
 }

  //Paypal Integration function
  @RequestMapping("/authorize_payment")
  public RedirectView paypalPayment(Map<String, Object> model, @RequestParam("product") String product, @RequestParam("subtotal") String subtotal, @RequestParam("tax") String tax, @RequestParam("total") String total) {

  	Order order = new Order();
  	order.setName(product);
  	order.setTax(tax);
  	order.setSubTotal(subtotal);
  	order.setTotal(total);

  	String paypalLink = null;

  	try{

  		PaypalPayment paypalPayment = new PaypalPayment();
  		paypalLink = paypalPayment.pay(order);

  	} catch(PayPalRESTException e) {
  		e.printStackTrace();
  	}
  	
  	RedirectView redirectView = new RedirectView();
    redirectView.setUrl(paypalLink);
    
    return redirectView;
  }

 @RequestMapping("/review_payment")
  String db32(Map<String, Object> model) {
      return "done";
  }

   @RequestMapping("/cancel.html")
  String cancel(Map<String, Object> model) {
      return "loginpage";
  }

  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }

}
