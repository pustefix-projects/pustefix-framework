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
package de.schlund.pfixxml.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.mail.internet.MimeUtility;

/**
 * Helper class for decoding/encoding bytes using the Base64 algorithm.
 * Therefore it uses javax.mail.internet.MimeUtility. The implementation doesn't
 * perform as well as other specialized low-level implementations of the
 * algorithm and shouldn't be used in performance critical scenarios.
 * 
 * @author mleidig@schlund.de
 * 
 */
public class Base64Utils {

    /**
     * Encode bytes using the Base64 algorithm (the linebreak after 76 signs can
     * be optionally removed).
     */
    public static String encode(byte[] bytes, boolean withNewLines) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStream os = MimeUtility.encode(baos, "base64");
            os.write(bytes);
            os.close();
            String str = new String(baos.toByteArray(), "utf8");
            if (!withNewLines) str = str.replaceAll("\r\n", "");
            return str;
        } catch (Exception x) {
            throw new RuntimeException("Base64 encoding failed", x);
        }
    }

    /**
     * Decode string representation of Base64 encoded bytes.
     */
    public static byte[] decode(String str) {
        try {
            byte[] bytes = str.getBytes("utf8");
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            InputStream is = MimeUtility.decode(bais, "Base64");
            byte[] tmp = new byte[bytes.length];
            int n = is.read(tmp);
            byte[] res = new byte[n];
            System.arraycopy(tmp, 0, res, 0, n);
            return res;
        } catch (Exception x) {
            throw new RuntimeException("Base64 decoding failed", x);
        }
    }

    public static void main(String[] args) throws Exception {
        String url = "http://pustefix-framework.org";
        System.out.println(url);
        String enc = encode(url.getBytes("utf8"), false);
        System.out.println(enc);
        String dec = new String(decode(enc), "utf8");
        System.out.println(dec);
        System.out.println(dec.equals(url));
    }

}
