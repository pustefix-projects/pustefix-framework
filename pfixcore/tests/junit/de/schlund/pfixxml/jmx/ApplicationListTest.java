package de.schlund.pfixxml.jmx;

import java.io.File;
import java.io.Serializable;

import junit.framework.TestCase;

import org.w3c.dom.Document;

import de.schlund.pfixxml.util.Xml;

public class ApplicationListTest extends TestCase {
    public void testJmxPrereqs() throws Exception {
        assertTrue(Serializable.class.isAssignableFrom(ApplicationList.class));
        assertTrue(Serializable.class.isAssignableFrom(Application.class));
    }

    public void testApplication() throws Exception {
        Application app;
        ApplicationList lst;
        
        app = new Application("foo", "bar", true, "/a", "mhm");
        assertEquals("http://bar:8080/a;jsessionid=nosuchsession.mhm&__forcelocal=1", app.getUrl().toString());
        assertEquals("http://bar:8080/xy;jsessionid=gg&__forcelocal=1", app.getUrl("/xy", "gg").toString());
    }

    public void testNotFound() throws Exception {
        assertNull(new ApplicationList().lookup("nosuchapp"));
    }

    public void testFound() throws Exception {
        Application app;
        ApplicationList lst;
        
        app = new Application("foo", "bar", true, "/a", "mhm");
        lst = new ApplicationList();
        lst.add(app);
        assertSame(app, lst.lookup("foo"));
    }

    public void testLoad() throws Exception {
        Document doc;
        ApplicationList lst;
        Application app;

        lst = ApplicationList.load(Xml.parse(new File("tests/junit/de/schlund/pfixxml/jmx/projects.xml")), true, "foo");
        assertEquals(6, lst.size());
        app = (Application) lst.getApplications().get(0);
        assertEquals("sample1", app.getName());
        assertTrue(app.getServer().startsWith("sample1."));
        assertNotNull(lst.get("simplelink"));
        assertNotNull(lst.get("simplepage"));
    }
}
