package de.schlund.util.statuscodes;

import org.w3c.dom.Document;
import de.schlund.pfixxml.util.Xml;
import de.schlund.util.statuscodes.PartIndex;
import junit.framework.TestCase;

public class PartIndexTest extends TestCase {
    private Document doc;
    private PartIndex pi;

    protected void setUp() throws Exception {
        super.setUp();
        
        doc = Xml.parseString(
                "<include_parts>" +
                "  <part name='one'>" + 
                "    <product name='default'>" + 
                "      <lang name='default'>msg</lang>" +
                "    </product>"+ 
                "  </part>" + 
                "  <part name='a.b'>" + 
                "    <product name='default'>" + 
                "      <lang name='default'>msg</lang>" +
                "    </product>"+ 
                "  </part>" + 
                "  <part name='invisible'>" + 
                "    <product name='none-default'>" + 
                "      <lang name='default'>msg</lang>" +
                "    </product>"+ 
                "  </part>" + 
                "</include_parts>" 
                );
        pi = new PartIndex();
        pi.addAll(doc);
    }

    public void testNormal() throws Exception {
        check("a.b");
        check("one");
    }

    public void testNotFound() throws Exception {
        assertNull(pi.lookup("doesnotexist"));
    }
    
    public void testInvisible() throws Exception {
        assertNull(pi.lookup("invisible"));
    }

    private void check(String name) {
        assertEquals(name, pi.lookup(name).getPart());
    }
}
