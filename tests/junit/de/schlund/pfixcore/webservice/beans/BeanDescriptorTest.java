package de.schlund.pfixcore.webservice.beans;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class BeanDescriptorTest extends TestCase {
    
    public void testPropertyDetection() {
        
        Set<String> expProps=new HashSet<String>();
        expProps.add("foo");
        expProps.add("baz");
        BeanDescriptor beanDesc=new BeanDescriptor(BeanA.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        beanDesc=new BeanDescriptor(BeanD.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        expProps=new HashSet<String>();
        expProps.add("foo");
        expProps.add("baz");
        expProps.add("my");
        beanDesc=new BeanDescriptor(BeanB.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        beanDesc=new BeanDescriptor(BeanE.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        expProps=new HashSet<String>();
        expProps.add("bar");
        expProps.add("baz");
        expProps.add("my");
        expProps.add("hey");
        beanDesc=new BeanDescriptor(BeanC.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        beanDesc=new BeanDescriptor(BeanF.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
    }
    
    public static void main(String[] args) throws Exception {
        BeanDescriptorTest test=new BeanDescriptorTest();
        test.testPropertyDetection();
    }

}
