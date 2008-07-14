/*
 * Place license here
 */

package org.pustefixframework.http.dereferer;

import de.schlund.pfixxml.util.MD5Utils;

public abstract class SignUtil {
    public static String signString(String str, String key) {
        return MD5Utils.hex_md5(str + key, "utf8");
    }
}
