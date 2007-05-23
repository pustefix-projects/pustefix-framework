package de.schlund.pfixcore.util;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TokenUtils {

    private static Random RANDOM=new Random();
    
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
