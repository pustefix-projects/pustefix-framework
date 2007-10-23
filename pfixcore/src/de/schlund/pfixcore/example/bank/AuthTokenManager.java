package de.schlund.pfixcore.example.bank;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import de.schlund.pfixxml.util.MD5Utils;

public class AuthTokenManager {

	private static SecretKey secretKey;
	private static Object keyLock=new Object();
	private static long keyLifeTime=60*60*1000;
	private static long keyGenTime=0;
	private static String signKey="j45Nh&$jd§Jd99(z";
	
	public static String[] decodeAuthToken(String str) {
		String decStr=decrypt(str);
		String[] parts=decStr.split(":");
		if(parts.length<2) throw new IllegalArgumentException("No values found.");
		String[] values=new String[parts.length-1];
		for(int i=0;i<values.length;i++) values[i]=parts[i];
		return values;
	}
	
	public static String createAuthToken(String[] values) {
		StringBuilder sb=new StringBuilder();
		for(String value:values) {
			sb.append(value);
			sb.append(":");
		}
		String hash=MD5Utils.hex_md5(sb.toString()+signKey,"utf8");
		String token=sb.toString()+hash;
		String encToken=encrypt(token);
		return encToken;
	}
	
	private static SecretKey getSecretKey() {
		synchronized(keyLock) {
			if(secretKey==null || (System.currentTimeMillis()-keyLifeTime)>keyGenTime) {
				try {
					KeyGenerator keyGen = KeyGenerator.getInstance("DES");
				    secretKey = keyGen.generateKey();
				    keyGenTime=System.currentTimeMillis();
				} catch(NoSuchAlgorithmException x) {
					throw new RuntimeException("Can't generate key.",x);
				}
			}
			return secretKey;
		}
	}
	
	private static String encrypt(String str) {
		try {
			SecretKey key=getSecretKey();
			Cipher desCipher=Cipher.getInstance("DES/ECB/PKCS5Padding");
			desCipher.init(Cipher.ENCRYPT_MODE,key);
			byte[] cleartext = str.getBytes("UTF-8");
			byte[] ciphertext = desCipher.doFinal(cleartext);
			BASE64Encoder enc=new BASE64Encoder();
			return enc.encode(ciphertext);
		} catch(Exception x) {
			throw new RuntimeException("Encrypting token failed.",x);
		}
	}
	
	private static String decrypt(String base64Str) {
		try {
			SecretKey key=getSecretKey();
			BASE64Decoder dec=new BASE64Decoder();
			byte[] ciphertext=dec.decodeBuffer(base64Str);
		    Cipher desCipher=Cipher.getInstance("DES/ECB/PKCS5Padding");
		    desCipher.init(Cipher.DECRYPT_MODE,key);
		    byte[] cleartext = desCipher.doFinal(ciphertext);
		    String str=new String(cleartext,"UTF-8");
		    return str;
		} catch(Exception x) {
			throw new RuntimeException("Decrypting token failed.",x);
		}
	}
	
}
