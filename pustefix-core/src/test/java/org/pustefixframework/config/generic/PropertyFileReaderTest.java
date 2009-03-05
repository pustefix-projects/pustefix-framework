package org.pustefixframework.config.generic;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Before;
import org.junit.Test;

import de.schlund.pfixxml.config.BuildTimeProperties;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;

/**
 * 
 * @author mleidig
 *
 */
public class PropertyFileReaderTest {

    @Before
    public void setUp() {
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("%p: %m\n"));
        Logger logger=Logger.getRootLogger();
        logger.setLevel((Level)Level.WARN);
        logger.removeAllAppenders();
        logger.addAppender(appender);
        
        if(GlobalConfig.getDocroot()==null)
            GlobalConfigurator.setDocroot(new File("projects").getAbsoluteFile().getAbsolutePath());
    }
    
    @Test
    public void test() throws Exception {
        InputStream in = getClass().getResourceAsStream("properties.xml");
        if(in==null) in = new FileInputStream(new File("src/test/java/org/pustefixframework/config/generic/properties.xml"));
        Properties props = new Properties();
        PropertyFileReader.read(in, props);
        Properties refProps = new Properties();
        refProps.setProperty("foo", "bar");
        refProps.setProperty("hey", "ho");
        
        String mode = BuildTimeProperties.getProperties().getProperty("mode");
        refProps.setProperty("mode", mode);
        String fqdn = BuildTimeProperties.getProperties().getProperty("fqdn");
        refProps.setProperty("fqdn", fqdn);
        
        assertEquals(props, refProps);
        
    }
  
}
