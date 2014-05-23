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
package org.pustefixframework.http.dereferer;

import javax.crypto.SecretKey;

import org.pustefixframework.util.MacUtils;

public abstract class SignUtil {
    
    private final static SecretKey SIGN_KEY = MacUtils.generateMacKey(MacUtils.HMAC_MD5);
    
    public static String getSignature(String str, long timeStamp) {
        StringBuilder sb = new StringBuilder();
        sb.append(timeStamp);
        sb.append(' ');
        sb.append(str);
        return MacUtils.hexMac(sb.toString(), MacUtils.CHARSET_UTF8, SIGN_KEY);
    }
    
    public static boolean checkSignature(String str, long timeStamp, String signature) {
        return signature.equals(getSignature(str, timeStamp));
    }
    
    public static String getFakeSessionIdArgument(String sessionId) {
        // if the session id is empty, there is no fake session id
        if (sessionId == null || sessionId.trim().length() == 0) {
            return "";
        }
        // if the session id does not contain a jvm route, a fake session id
        // makes no sense
        int dotPos = sessionId.lastIndexOf('.');
        if (dotPos == -1) {
            return "";
        }
        // otherwise, keep the part of the session id after the last dot
        return ";jsessionid=nosession" + sessionId.substring(dotPos);
    }
    
    public static String getTimeStamp() {
        return String.valueOf(System.currentTimeMillis());
    }
    
}
