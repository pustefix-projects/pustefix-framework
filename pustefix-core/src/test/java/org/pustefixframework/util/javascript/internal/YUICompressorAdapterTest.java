package org.pustefixframework.util.javascript.internal;

import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.pustefixframework.util.javascript.Compressor;
import org.pustefixframework.util.javascript.CompressorException;

public class YUICompressorAdapterTest extends TestCase {

    String[] inputs = new String[] {
            "//comment\n      alert(\"hey\");  //afdfadfs\n",
            "function foo() {var xxxxxx=3;};"
    };
    
    String[] outputs = new String[] {
            "alert(\"hey\");",
            "function foo(){var a=3};"
    };
    
    @Override
    protected void setUp() throws Exception {
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("%p: %m\n"));
        Logger logger = Logger.getRootLogger();
        logger.setLevel((Level) Level.DEBUG);
        logger.removeAllAppenders();
        logger.addAppender(appender);
    }
    
    public void testCompression() throws CompressorException {
        
        Compressor compressor = new YUICompressorAdapter();
        for(int i=0; i<inputs.length; i++) {
            StringWriter writer = new StringWriter();
            compressor.compress(new StringReader(inputs[i]), writer);
            assertEquals(outputs[i], writer.toString());
        }
        
    }
    
}
