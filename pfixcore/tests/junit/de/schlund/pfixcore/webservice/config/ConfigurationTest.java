package de.schlund.pfixcore.webservice.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import junit.framework.TestCase;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

public class ConfigurationTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        GlobalConfigurator.setDocroot(new File("projects").getAbsoluteFile().getAbsolutePath());
    }
    
    public void testSerialization() throws Exception {
        FileResource file=ResourceUtil.getFileResourceFromDocroot("webservice/conf"+"/"+"webservice.conf.xml");
        Configuration conf=ConfigurationReader.read(file);
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        ConfigurationReader.serialize(conf,out);
        ByteArrayInputStream in=new ByteArrayInputStream(out.toByteArray());
        Configuration refConf=ConfigurationReader.deserialize(in);
        assertEquals(conf,refConf);
    }
    
}
