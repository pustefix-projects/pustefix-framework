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
package de.schlund.pfixcore.example.webservices;

import java.util.Random;

public class ContextDataImpl implements ContextData {

    String   data;
    String[] dataArray;

    public String exchangeData(String data, int strSize) throws Exception {
        this.data = data;
        if (strSize < 0) strSize = 0;
        return generateString(strSize);
    }

    public String[] exchangeDataArray(String[] data, int arrSize, int strSize) throws Exception {
        this.dataArray = data;
        if (arrSize < 0) arrSize = 0;
        if (strSize < 0) strSize = 0;
        String[] ret = new String[arrSize];
        for (int i = 0; i < arrSize; i++)
            ret[i] = generateString(strSize);
        return ret;
    }

    final static Random random = new Random();

    public static String generateString(int length) {
        StringBuffer sb = new StringBuffer();
        int val = 0;
        for (int i = 0; i < length; i++) {
            while (!((val > 47 && val < 58) || (val > 64 && val < 91) || (val > 96 && val < 123))) {
                val = random.nextInt(123);
            }
            sb.append((char) val);
            val = 0;
        }
        return sb.toString();
    }

}
