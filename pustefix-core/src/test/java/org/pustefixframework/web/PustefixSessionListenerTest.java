package org.pustefixframework.web;

import java.util.concurrent.ConcurrentHashMap;

import org.pustefixframework.web.PustefixSessionListener.VisitIdGenerator;

import junit.framework.TestCase;

public class PustefixSessionListenerTest extends TestCase {

    public void testVisitIdGenerating() throws Exception {

        //test format
        assertTrue(new VisitIdGenerator().generateId("1234").matches("\\d{14}-000"));
        assertTrue(new VisitIdGenerator().generateId("1234.foo").matches("\\d{14}-000.foo"));

        //test uniqueness
        VisitIdGenerator generator = new VisitIdGenerator();
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        int threadNo = 20;
        int generateNo = 5000;
        Thread[] threads = new Thread[threadNo];
        for(int i=0; i<threadNo; i++) {
            threads[i] = new Thread() {
                public void run() {
                    for(int j=0; j<generateNo; j++) {
                        String id = generator.generateId("1234.foo");
                        map.put(id, id);
                    }
                }
            };
            threads[i].start();
        }
        for(int i=0; i<threadNo; i++) {
            threads[i].join();
        }
        assertEquals(threadNo * generateNo, map.size());
    }

}
