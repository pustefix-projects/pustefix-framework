package org.pustefixframework.util.javascript.internal;

import java.io.StringReader;
import java.io.StringWriter;

import org.pustefixframework.util.javascript.Compressor;
import org.pustefixframework.util.javascript.CompressorException;

public class YUICompressorAdapterTest extends AbstractAdapterTest {

    String[] outputs = new String[] {
            "alert(\"hey\");",
            "function foo(){var a=3};",
            "function bar(a){alert(a)};",
            "function bar(a){FOO.bar=a};"
    };
    
    public void testCompression() throws CompressorException {
        
        Compressor compressor = new YUICompressorAdapter();
        for(int i=0; i<inputs.length; i++) {
            StringWriter writer = new StringWriter();
            compressor.compress(new StringReader(inputs[i]), writer);
            assertEquals(outputs[i], writer.toString());
        }
        
    }
    
}
