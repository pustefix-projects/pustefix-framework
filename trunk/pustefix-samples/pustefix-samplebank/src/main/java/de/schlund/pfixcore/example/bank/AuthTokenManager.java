/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.schlund.pfixcore.example.bank;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import de.schlund.pfixxml.util.Base64Utils;
import de.schlund.pfixxml.util.MD5Utils;

public class AuthTokenManager {

    private static SecretKey secretKey;
    private static Object    keyLock     = new Object();
    private static long      keyLifeTime = 60 * 60 * 1000;
    private static long      keyGenTime  = 0;
    private static String    signKey     = "j45Nh&$jd§Jd99(z";

    public static String[] decodeAuthToken(String str) {
        String decStr = decrypt(str);
        String[] parts = decStr.split(":");
        if (parts.length < 2) throw new IllegalArgumentException("No values found.");
        String[] values = new String[parts.length - 1];
        for (int i = 0; i < values.length; i++)
            values[i] = parts[i];
        return values;
    }

    public static String createAuthToken(String[] values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value);
            sb.append(":");
        }
        String hash = MD5Utils.hex_md5(sb.toString() + signKey, "utf8");
        String token = sb.toString() + hash;
        String encToken = encrypt(token);
        return encToken;
    }

    private static SecretKey getSecretKey() {
        synchronized (keyLock) {
            if (secretKey == null || (System.currentTimeMillis() - keyLifeTime) > keyGenTime) {
                try {
                    KeyGenerator keyGen = KeyGenerator.getInstance("DES");
                    secretKey = keyGen.generateKey();
                    keyGenTime = System.currentTimeMillis();
                } catch (NoSuchAlgorithmException x) {
                    throw new RuntimeException("Can't generate key.", x);
                }
            }
            return secretKey;
        }
    }

    private static String encrypt(String str) {
        try {
            SecretKey key = getSecretKey();
            Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            desCipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cleartext = str.getBytes("UTF-8");
            byte[] ciphertext = desCipher.doFinal(cleartext);
            return Base64Utils.encode(ciphertext,false);
        } catch (Exception x) {
            throw new RuntimeException("Encrypting token failed.", x);
        }
    }

    private static String decrypt(String base64Str) {
        try {
            SecretKey key = getSecretKey();
            byte[] ciphertext = Base64Utils.decode(base64Str);
            Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            desCipher.init(Cipher.DECRYPT_MODE, key);
            byte[] cleartext = desCipher.doFinal(ciphertext);
            String str = new String(cleartext, "UTF-8");
            return str;
        } catch (Exception x) {
            throw new RuntimeException("Decrypting token failed.", x);
        }
    }

}
