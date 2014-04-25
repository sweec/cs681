package edu.umb.cs.threads.tinyhttpd;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Test {
    static final String HEX = "0123456789abcdef";
    
	public static void main(String[] args) {
		String s ="abc";
		MessageDigest md = null;
		byte[] nonce = null;

		try {
			md = MessageDigest.getInstance("MD5");
			nonce = md.digest(s.getBytes()); // 128-bit digest (8 bits * 16)
			
			int sh7 = 1 << 7;
			for(byte b : nonce){
				for(int k=0; k < 8; k++){
					System.out.print( ((b << k) & sh7) >>> 7 );
				}
				System.out.print(" ");
			}
			System.out.println("");
			
			System.out.println( getHex(nonce) );

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
    static String getHex(byte[] md5binaryDigest) {
        StringBuffer sb = new StringBuffer();
        for (byte b : md5binaryDigest){
        	
//        	System.out.print( (int) ((b >>> 4) & 0x0f) + " ");
//        	System.out.print( (int) (b & 0x0f) + " ");
        	
//        	System.out.print( Integer.toBinaryString((b >>> 4) & 0x0f) + " " );
//        	System.out.print( Integer.toBinaryString(b & 0x0f) + " " );
        	
            sb.append(HEX.charAt((b >>> 4) & 0x0f));
            sb.append(HEX.charAt(b & 0x0f));
        }
        System.out.println("");
        return sb.toString();
    }
}
