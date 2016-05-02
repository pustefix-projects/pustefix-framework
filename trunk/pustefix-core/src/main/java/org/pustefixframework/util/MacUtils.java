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

package org.pustefixframework.util;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import de.schlund.pfixxml.util.MD5Utils;

/**
 * Utilities for generating message authentication codes (MACs).
 * 
 * @author Sebastian Marsching
 */
public class MacUtils {

    /**
     * Name MD5-based HMAC algorithm.
     */
    public final static String HMAC_MD5 = "HmacMD5";

    /**
     * Name of SHA-1-based HMAC algorithm.
     */
    public final static String HMAC_SHA1 = "HmacSHA1";

    /**
     * Name of SHA-256-based HMAC algorithm.
     */
    public final static String HMAC_SHA256 = "HmacSHA256";

    /**
     * Name of SHA-384-based HMAC algorithm.
     */
    public final static String HMAC_SHA384 = "HmacSHA384";

    /**
     * Name of SHA-512-based HMAC algorithm.
     */
    public final static String HMAC_SHA512 = "HmacSHA512";

    /**
     * UTF-8 charset.
     */
    public final static Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    /**
     * Generates a key suitable for use with the specified message
     * authentication code (MAC) algorithm. This key can than be used in calls
     * to {@link #hexMac(String, String, SecretKey)} in order to generate a MAC
     * using the specified algorithm. This algorithm must be an algorithm that
     * is supported in calls to {@link KeyGenerator#getInstance(String)} and
     * {@link Mac#getInstance(String)}.
     * 
     * @param macAlgorithm
     *            name of the MAC algorithm to be used (e.g. "HmacMD5" or
     *            "HmacSHA1").
     * @return a newly generated random key suitable for use with the specified
     *         algorithm.
     * @see #hexMac(String, String, SecretKey)
     * @see KeyGenerator#getInstance(String)
     * @see KeyGenerator#generateKey()
     */
    public static SecretKey generateMacKey(String macAlgorithm) {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(macAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MAC algorithm " + macAlgorithm
                    + " is not supported by default key generator provider.", e);
        }
        return keyGenerator.generateKey();
    }

    /**
     * Generates a message authentication code (MAC) for a given string message
     * using the supplied secret key. The message is converted to its byte
     * representation using the specified encoding before the MAC is calculated.
     * When generating a MAC for a message, the specified encoding, secret key
     * and MAC algorithm have to match in order to generate the same MAC for the
     * same message. The MAC algorithm is not specified explicitly but is
     * determined from the secret key by calling the
     * {@link SecretKey#getAlgorithm()} method.
     * 
     * @param message
     *            message that should be authenticated using the MAC.
     * @param charset
     *            encoding to use for serializing the message.
     * @param secretKey
     *            secret key to use for calculating the MAC. This also
     *            implicitly specifies the algorithm to be used.
     * @return MAC for the byte representation of the given message, generated
     *         using the specified secret key and algorithm.
     */
    public static String hexMac(String message, Charset charset,
            SecretKey secretKey) {
        Mac mac;
        try {
            mac = Mac.getInstance(secretKey.getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MAC algorithm "
                    + secretKey.getAlgorithm()
                    + " is not supported by default MAC provider.", e);
        }
        try {
            mac.init(secretKey);
        } catch (InvalidKeyException e) {
            // We specifically requested the MAC algorithm using the algorithm
            // name from the key, so this should never happen. However, we still
            // have to handle it.
            throw new RuntimeException("Specified key is invalid.", e);
        }
        byte[] macResult;
        macResult = mac.doFinal(message.getBytes(charset));
        return MD5Utils.byteToHex(macResult);
    }

}
