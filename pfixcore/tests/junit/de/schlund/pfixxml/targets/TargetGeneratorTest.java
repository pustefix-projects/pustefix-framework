/*
 * Created on May 24, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixxml.targets;

import java.io.File;
import org.w3c.dom.Document;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.util.Xml;
import junit.framework.TestCase;

public class TargetGeneratorTest extends TestCase {
    public void testEmpty() throws Exception {
        TargetGenerator gen;
        
        gen = create("<make docroot='.' cachedir='.' record_dir='.'/>");
        assertEquals(0, gen.getAllTargets().size());
        assertEquals(gen.getDocroot(), ".");
        assertEquals(gen.getDisccachedir(), ".");
    }

    public void testTarget() throws Exception {
        TargetGenerator gen;
        
        gen = create("<make docroot='.' cachedir='.' record_dir='.'>" + 
                     "  <target name='master.xsl' type='xsl'>" +
                     "    <depxml name='core/xsl/master.xsl'/>" + 
                     "    <depxsl name='core/xsl/customizemaster.xsl'/>" +
                     "  </target>" +
                     "</make>"
                );
        assertEquals(3, gen.getAllTargets().size());
        // TODO: more tests
    }

    public void testXmlMissing() throws Exception {
        createInvalid("<make docroot='.' cachedir='.' record_dir='.'>" + 
                     "  <target name='master.xsl' type='xsl'>" +
                     "    <depxsl name='core/xsl/customizemaster.xsl'/>" +
                     "  </target>" +
                     "</make>",
                     "without [depxml]"
                );
    }
    
    public void testXslMissing() throws Exception {
        createInvalid("<make docroot='.' cachedir='.' record_dir='.'>" + 
                     "  <target name='master.xsl' type='xsl'>" +
                     "    <depxml name='core/xsl/master.xsl'/>" + 
                     "  </target>" +
                     "</make>",
                     "without [depxsl]"
                );
    }
    

    public void testDocrootMissing() throws Exception {
        createInvalid("<make cachedir='.' record_dir='.'/>", "docroot");
    }

    public void testCachedirMissing() throws Exception {
        createInvalid("<make docroot='.' record_dir='.'/>", "cachedir");
    }

    public void testRecorddirMissing() throws Exception {
        createInvalid("<make docroot='.' cachedir='.'/>", "record_dir");
    }

    public void testDocrootNoLongerNeeded() throws Exception {
        createInvalid("<make cachedir='.' docroot='.' record_dir='.'>" +
                      "  <target name='master.xsl' type='xsl'>" +
                      "    <depxml name='core/xsl/master.xsl'/>" + 
                      "    <depxsl name='core/xsl/customizemaster.xsl'/>" +
                      "    <param name='docroot' value='foo'/>" +
                      "  </target>" +
                      "</make>",                
                      "docroot is no longer needed");
    }

    public void createInvalid(String str, String msg) throws Exception {
        String full;
        
        try {
            create(str);
            fail("'" + str + "' does not raise exception with '" + msg + "'");
        } catch (XMLException e) {
            full = e.getMessage();
            if (full.indexOf(msg) != -1) {
                // ok
            } else {
                fail("exception message '" + full + "' does not contain '" + msg + "'");
            }
        }
    }

    private TargetGenerator create(String str) throws Exception {
        TargetGenerator gen;
        File file;
        
        Document doc = Xml.parseString(str);
        file = File.createTempFile("depend", "xml", new File("."));
        file.deleteOnExit();
        Xml.serialize(doc, file, true, true);
        gen = new TargetGenerator(file);
        return gen;
    }
}
