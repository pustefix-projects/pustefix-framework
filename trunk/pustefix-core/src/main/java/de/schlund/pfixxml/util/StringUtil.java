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

/**
 * Describe class StringUtil here.
 *
 *
 * Created: Tue Apr 19 11:37:01 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class StringUtil {

    public static String replaceAll(String input, String regexp, String replacement) {
        if (input == null) {
            return input;
        }
        return input.replaceAll(regexp, replacement);
    }

    public static String sign(String input, String secret) {
        String sign = MD5Utils.hex_md5(input + secret);
        return input + "SIGN=" + sign;
    }
    
}
