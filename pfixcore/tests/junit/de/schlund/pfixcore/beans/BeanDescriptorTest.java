package de.schlund.pfixcore.beans;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.schlund.pfixcore.beans.BeanDescriptor;
import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.beans.InitException;
import de.schlund.pfixcore.beans.metadata.Bean;
import de.schlund.pfixcore.beans.metadata.Beans;
import de.schlund.pfixcore.beans.metadata.DefaultLocator;
import de.schlund.pfixcore.beans.metadata.Locator;

public class BeanDescriptorTest extends TestCase {
    
    public void testPropertyDetection() throws InitException {
        
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("%p: %m\n"));
        Logger logger=Logger.getRootLogger();
        logger.setLevel((Level)Level.ERROR);
        logger.removeAllAppenders();
        logger.addAppender(appender);
        
        URL url=getClass().getResource("beanmetadata.xml");
        if(url==null) {
            try {
                url=new URL("file:/tests/junit/de/schlund/pfixcore/webservice/beans/beanmetadata.xml");
            } catch(MalformedURLException x) {
                throw new RuntimeException(x);
            }
        }
        Locator locator=new DefaultLocator(url);
        BeanDescriptorFactory beanDescFactory=new BeanDescriptorFactory(locator);    
        
        //BeanA + BeanD
        
        Set<String> expProps=new HashSet<String>();
        expProps.add("foo");
        expProps.add("baz");
        expProps.add("mytest");
        
        //BeanA annotations
        BeanDescriptor beanDesc=new BeanDescriptor(BeanA.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //BeanA metadata
        Beans beans=new Beans();
        Bean bean=new Bean(BeanA.class.getName());
        bean.excludeProperty("bar");
        bean.setPropertyAlias("test","mytest");
        beans.setBean(bean);
        beanDesc=new BeanDescriptor(BeanA.class,beans);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //BeanA xml metadata
        beanDesc=beanDescFactory.getBeanDescriptor(BeanA.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //BeanD annotations
        beanDesc=new BeanDescriptor(BeanD.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //BeanD metadata
        beans=new Beans();
        bean=new Bean(BeanD.class.getName());
        bean.excludeByDefault();
        bean.includeProperty("foo");
        bean.includeProperty("baz");
        bean.includeProperty("test");
        bean.setPropertyAlias("test","mytest");
        beans.setBean(bean);
        beanDesc=new BeanDescriptor(BeanD.class,beans);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //BeanD xml metadata
        beanDesc=beanDescFactory.getBeanDescriptor(BeanD.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        
        //BeanB + BeanE
        
        expProps=new HashSet<String>();
        expProps.add("foo");
        expProps.add("baz");
        expProps.add("mytest");
        expProps.add("my");
        
        //BeanB annotations
        beanDesc=new BeanDescriptor(BeanB.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //BeanB metadata
        beans=new Beans();
        bean=new Bean(BeanB.class.getName());
        bean.excludeByDefault();
        bean.includeProperty("my");
        beans.setBean(bean);
        beanDesc=new BeanDescriptor(BeanB.class,beans);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //BeanB xml metadata
        beanDesc=beanDescFactory.getBeanDescriptor(BeanB.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //BeanE annotations
        beanDesc=new BeanDescriptor(BeanE.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //BeanE metadata
        beans=new Beans();
        bean=new Bean(BeanE.class.getName());
        bean.excludeProperty("hey");
        bean.excludeProperty("ho");
        beans.setBean(bean);
        beanDesc=new BeanDescriptor(BeanE.class,beans);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //BeanE xml metadata
        beanDesc=beanDescFactory.getBeanDescriptor(BeanE.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //Test with beanmetadata from META-INF
        locator=new DefaultLocator();
        beanDescFactory=new BeanDescriptorFactory(locator);    
        
        //BeanC + BeanF
        
        expProps=new HashSet<String>();
        expProps.add("mybar");
        expProps.add("baz");
        expProps.add("mytest");
        expProps.add("my");
        expProps.add("hey");
        
        //BeanC annotations
        beanDesc=new BeanDescriptor(BeanC.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //BeanC metadata
        beans=new Beans();
        bean=new Bean(BeanC.class.getName());
        bean.excludeProperty("foo");
        bean.setPropertyAlias("bar","mybar");
        beans.setBean(bean);
        beanDesc=new BeanDescriptor(BeanC.class,beans);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //BeanC xml metadata
        beanDesc=beanDescFactory.getBeanDescriptor(BeanC.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //BeanF annotations
        beanDesc=new BeanDescriptor(BeanF.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //BeanF metadata
        beans=new Beans();
        bean=new Bean(BeanF.class.getName());
        bean.excludeByDefault();
        bean.includeProperty("hey");
        bean.includeProperty("bar");
        bean.setPropertyAlias("bar","mybar");
        beans.setBean(bean);
        beanDesc=new BeanDescriptor(BeanF.class,beans);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
        //BeanF xml metadata
        beanDesc=beanDescFactory.getBeanDescriptor(BeanF.class);
        assertEquals(expProps,beanDesc.getReadableProperties());
        
    }
    
    public static void main(String[] args) throws Exception {
        BeanDescriptorTest test=new BeanDescriptorTest();
        test.testPropertyDetection();
    }

}
