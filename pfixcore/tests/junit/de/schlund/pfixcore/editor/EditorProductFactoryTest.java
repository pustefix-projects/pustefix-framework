package de.schlund.pfixcore.editor;

import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.util.Xml;
import java.io.File;
import junit.framework.TestCase;
import org.w3c.dom.Document;

public class EditorProductFactoryTest extends TestCase {

    static {
        PathFactory.getInstance().init(new File("example").getAbsolutePath());
    }

    public void testProject() throws Exception {
        Document doc = Xml.parseString(
            "<project name='foo'>" + 
            "  <comment>c1</comment>" + 
            "  <depend>simplepage/conf/depend.xml</depend>" + 
            "  <servlet name='foo'/>" + 
            "  <servlet name='bar'/>" +
            "  <documentation>example/core/xsl/navigation.xsl.in</documentation>" +
            "</project>");
        EditorProduct p = EditorProductFactory.createProduct(doc.getDocumentElement(), null);
        assertEquals("foo", p.getName());
        assertEquals("c1", p.getComment());
        assertEquals("simplepage/conf/depend.xml", p.getDepend().getRelative());
        assertNotNull(p.getDocumentation());
        
        PfixcoreServlet[] servlets = p.getPfixcoreServlets();
        assertEquals(2, servlets.length);
        assertEquals("foo", servlets[0].getName());
        assertEquals("bar", servlets[1].getName());
    }

    public void testStandard() throws Exception {
        EditorProductFactory factory = new EditorProductFactory();
        factory.readFile("servletconf/projects.xml");
        assertNull(factory.getEditorProduct("nosuchproduct"));
        EditorProduct proj = factory.getEditorProduct("simpleform");
        assertNotNull(proj);
        assertEquals("simpleform", proj.getName());
    }

    public void testIgnore() throws Exception {
        EditorProductFactory factory = create(
            "<projects>" +
            "  <project name='foo'>" + 
            "    <comment>c1</comment>" + 
            "    <depend>simplepage/conf/depend.xml</depend>" + 
            "    <servlet name='xxx'/>" + 
            "  </project>" +
            "  <project name='bar'>" + 
            "    <comment>c1</comment>" + 
            "    <depend>simplepage/conf/depend.xml</depend>" + 
            "    <servlet name='yyy' useineditor='true'/>" + 
            "  </project>" +
            "</projects>");
        assertNull(factory.getEditorProduct("foo"));
        assertNotNull(factory.getEditorProduct("bar"));
    }

    public void testNoDocumentation() throws Exception {
        EditorProductFactory factory = create(
            "<projects>" +
            "  <project name='bar'>" + 
            "    <comment>c1</comment>" + 
            "    <depend>simplepage/conf/depend.xml</depend>" + 
            "    <servlet name='yyy' useineditor='true'/>" + 
            "  </project>" +
            "</projects>");
        EditorProduct prod = factory.getEditorProduct("bar");
        assertEquals(0, prod.getDocumentation().getDocumentationValues().length);
    }

    public void testCommonDocumentation() throws Exception {
        EditorProductFactory factory = create(
            "<projects>" +
            "  <project name='bar'>" + 
            "    <comment>c1</comment>" + 
            "    <depend>simplepage/conf/depend.xml</depend>" + 
            "    <servlet name='yyy' useineditor='true'/>" + 
            "  </project>" +
            "  <common><documentation>" +
            "    <doc_file>core/xsl/navigation.xsl.in</doc_file>" +
            "  </documentation></common>" +
            "</projects>");
        EditorProduct prod = factory.getEditorProduct("bar");
        assertEquals(15, prod.getDocumentation().getDocumentationValues().length);
    }
    
    public void testProjectDocumentation() throws Exception {
        EditorProductFactory factory = create(
            "<projects>" +
            "  <project name='bar'>" + 
            "    <comment>c1</comment>" + 
            "    <depend>simplepage/conf/depend.xml</depend>" + 
            "    <servlet name='yyy' useineditor='true'/>" + 
            "    <documentation>core/xsl/navigation.xsl.in</documentation>" +
            "  </project>" +
            "</projects>");
        EditorProduct prod = factory.getEditorProduct("bar");
        assertEquals(15, prod.getDocumentation().getDocumentationValues().length);
    }
    
    //--
    
    private static EditorProductFactory create(String str) throws Exception {
        Document doc = Xml.parseString(str);
        EditorProductFactory factory = new EditorProductFactory();
        factory.configure(doc);
        return factory;
    }
}
