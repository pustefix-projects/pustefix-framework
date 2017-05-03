package de.schlund.pfixcore.util;

import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

public class TokenUtilsTest extends TestCase {

   public void testUniqueness() throws Exception {
       ConcurrentHashMap<String, String> generatedTokens = new ConcurrentHashMap<>();
       int threadNo = 50;
       int tokenNo = 1000;
       Thread[] threads = new Thread[threadNo];
       for(int i=0; i<threadNo; i++) {
           threads[i] = new Thread() {
               @Override
               public void run() {
                   for(int j=0; j<tokenNo; j++) {
                       String str=TokenUtils.createRandomToken();
                       String old = generatedTokens.putIfAbsent(str, str);
                       assertNull("Duplicate token found!!!", old);
                   }
               }
           };
           threads[i].start();
       }
       for(int i=0; i<threadNo; i++) {
           threads[i].join();
       }
       assertEquals(threadNo * tokenNo, generatedTokens.size());
   }

}
