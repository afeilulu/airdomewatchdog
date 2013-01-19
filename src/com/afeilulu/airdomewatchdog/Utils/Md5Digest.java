package com.afeilulu.airdomewatchdog.Utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Digest {
	
	public static String MD5(String text){
		try {
			return converted2Hex(text);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static String converted2Hex(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(text.getBytes());
		byte[] digest = md.digest();
		StringBuffer sb = new StringBuffer();
		for (byte b : digest) {
			sb.append(Integer.toHexString((int) (b & 0xff)));
		}
		return sb.toString();
	}

	public static String MD5String(String text){
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(text.getBytes());
			byte[] digest = md.digest();
			char buf[] = new char[digest.length * 2];
	        for (int i = 0, x = 0; i < digest.length; i++) {
	            buf[x++] = HEX_CHARS[(digest[i] >>> 4) & 0xf];
	            buf[x++] = HEX_CHARS[digest[i] & 0xf];
	        }
	        return new String(buf);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static final char[] HEX_CHARS = {'0', '1', '2', '3',
        '4', '5', '6', '7',
        '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f',};
	
	
}
