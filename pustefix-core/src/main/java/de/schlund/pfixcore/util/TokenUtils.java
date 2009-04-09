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
package de.schlund.pfixcore.util;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TokenUtils {

    private final static Random RANDOM = new Random();
    
    public static String createRandomToken() {
        return Long.toString(System.currentTimeMillis(),36).toUpperCase()+
            new BigInteger(64, RANDOM).toString(36).toUpperCase();
    }
    

    public static void main(String[] args) {
        final Set<String> tokens=new HashSet<String>();
        for(int i=0;i<50;i++) {
            Thread t=new Thread() {
                @Override
                public void run() {
                    for(int j=0;j<1000;j++) {
                        String str=createRandomToken();
                        if(tokens.contains(str)) System.out.println("Duplicate token found!!!");
                        tokens.add(str);
                    }
                }
            };
            t.start();
        }
        System.out.println(createRandomToken());
    }
    
}
