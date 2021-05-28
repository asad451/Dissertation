package com.example;

import java.math.BigInteger;
import java.util.Random;

public class RSA {

	private BigInteger p, q, n, phi, e, d, one;
	private int bits = 100;//bitLength - can be increased to make the algo more secure
	private Random r;

	public RSA() {

		r = new Random();
		one = BigInteger.ONE;
		
		//generating prime numbers of length less than equal to 100
		p = BigInteger.probablePrime(bits, r);
		q = BigInteger.probablePrime(bits, r);
		
		// n = p * q;
		n = p.multiply(q);
		
		// phi = (p - 1) * (q - 1)
		phi = p.subtract(one).multiply(q.subtract(one));
		
		
		/* Calculating e such that:
		 *  1 < e < phi &
		 *  gcd (phi, e) = 1
		 */
		e = BigInteger.probablePrime(bits / 2, r);	//random value: To make e < n
		
		while (phi.gcd(e).compareTo(one) > 0) //add 1 to e until gcd(phi, e) = 1
		{
			e.add(one);
		}
		
		/* Calculating d using the formula:
		 * d = e^-1 mod phi
		 */ 
		d = e.modInverse(phi);
		
	}

	/* Encrypt the byte message using the formula:
	 * CipherText = PlainText^e mod N
	 */
	public byte[] encrypt(byte[] data)
	{
		BigInteger encrypt;

		encrypt = new BigInteger(data).modPow(e, n);

		byte[] encrypted = encrypt.toByteArray();

		return encrypted;
	}

	/* Decrypt the byte message using the formula:
	 * PlainText = CipherText^d mod N
	 */
	public byte[] decrypt(byte[] data, BigInteger newD, BigInteger newN)
	{
		BigInteger decrypt;

		decrypt = new BigInteger(data).modPow(newD, newN);

		byte[] decrypted = decrypt.toByteArray();

		return decrypted;
	}
	
	//Append the value of d and n to the encrypted message
	public String append() {
		
		String d_value = d.toString();
		String n_value = n.toString();
		String res = "(" + d_value +"-" + n_value + ")";
		
		return res;
	}
	
	//UnAppend the value of d and n from the encrypted message
	public String unAppend(String appended) {
		
	    int index = appended.indexOf(")");
	    String str = appended.substring(index + 1, appended.length());
		return str;
	}
	
	//Retrieving the value of N
	public BigInteger valueOfN(String appended) {
		
	    int index = appended.indexOf(")");
	    String str1 = appended.substring(0, index+1);
	    int index1 = str1.indexOf("-");
	    int index2 = str1.indexOf(")");
	    String str2 = str1.substring(index1+1, index2);
	    BigInteger n_value = new BigInteger(str2);
	    
	    return n_value;
	}

	//Retrieving the value of d
	public BigInteger valueOfD(String appended) {
		
	    int index = appended.indexOf(")");
	    String str1 = appended.substring(0, index+1);
	    int index1 = str1.indexOf("-");
	    String str2 = str1.substring(1, index1);
	    BigInteger d_value = new BigInteger(str2);
	    
	    return d_value;
	}
}
