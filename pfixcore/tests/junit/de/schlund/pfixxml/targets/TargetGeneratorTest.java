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
import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.util.Xml;
import junit.framework.TestCase;

public class TargetGeneratorTest extends TestCase {
    // TODO
    private static final File DOCROOT = new File("example").getAbsoluteFile();

    public void testDocroot() {
        assertEquals(new File("/projects"), TargetGenerator.findDocroot(new File("/projects/foo/depend.xml")));
        assertEquals(new File("/foo/example"), TargetGenerator.findDocroot(new File("/foo/example/core/editor/depend.xml")));
    }
    public void testEmpty() throws Exception {

        TargetGenerator gen;
        
        gen = create("<make project='foo' lang='bar'/>");
        assertEquals(0, gen.getAllTargets().size());
        assertEquals(DOCROOT, gen.getDocroot());
        assertEquals("foo", gen.getName());
        assertEquals("bar", gen.getLanguage());
        assertNotNull(Path.getRelativeString(DOCROOT, gen.getDisccachedir().getPath()));
        assertNotNull(Path.getRelativeString(DOCROOT, gen.getRecorddir().getPath()));
    }

    public void testTarget() throws Exception {
        TargetGenerator gen;
        
        gen = create("<make project='foo' lang='bar'>" + 
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
        createInvalid("<make project='foo' lang='bar'>" + 
                     "  <target name='master.xsl' type='xsl'>" +
                     "    <depxsl name='core/xsl/customizemaster.xsl'/>" +
                     "  </target>" +
                     "</make>",
                     "without [depxml]"
                );
    }
    
    public void testXslMissing() throws Exception {
        createInvalid("<make project='foo' lang='bar'>" + 
                     "  <target name='master.xsl' type='xsl'>" +
                     "    <depxml name='core/xsl/master.xsl'/>" + 
                     "  </target>" +
                     "</make>",
                     "without [depxsl]"
                );
    }
    

    public void testDocrootNoLongerNeeded() throws Exception {
        createInvalid("<make project='foo' lang='bar'>" +
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
        
        Document doc = Xml.parseStringMutable(str);
        file = File.createTempFile("depend", "xml", new File("example"));
        file.deleteOnExit();
        Xml.serialize(doc, file, true, true);
        gen = new TargetGenerator(file);
        return gen;
    }
}
