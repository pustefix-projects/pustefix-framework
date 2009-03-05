package de.schlund.pfixxml.jmx;

import java.io.File;
import java.io.Serializable;

import junit.framework.TestCase;
import de.schlund.pfixxml.testrecording.Application;
import de.schlund.pfixxml.testrecording.ApplicationList;
import de.schlund.pfixxml.util.Xml;

public class ApplicationListTest extends TestCase {
    public void testJmxPrereqs() throws Exception {
        assertTrue(Serializable.class.isAssignableFrom(ApplicationList.class));
        assertTrue(Serializable.class.isAssignableFrom(Application.class));
    }

    public void testApplicationTomcat() throws Exception {
        Application app;
        
        app = new Application("foo", "bar", true, "/a", "mhm");
        assertEquals("https://bar:8443/foo;jsessionid=nosuchsession.mhm", app.getUrl(true, "/foo").toString());
        assertEquals("http://bar:8080/xy;jsessionid=gg", app.getUrl(false, "/xy", "gg").toString());
    }

    public void testApplicationApache() throws Exception {
        Application app;
        
        app = new Application("foo", "bar", false, "/a", "mhm");
        assertEquals("http://bar/back;jsessionid=nosuchsession.mhm", app.getUrl(false, "/back").toString());
        assertEquals("http://bar/xy;jsessionid=gg", app.getUrl(false, "/xy", "gg").toString());
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
      
        ApplicationList lst;
        Application app;

        lst = ApplicationList.load(Xml.parseMutable(new File("src/test/java/de/schlund/pfixxml/jmx/projects.xml")), true, "foo");
       
        assertEquals(6, lst.size());
        app = (Application) lst.getApplications().get(0);
        assertEquals("sample1", app.getName());
        assertTrue(app.getServer().startsWith("sample1."));
        assertNotNull(lst.get("simplelink"));
        assertNotNull(lst.get("simplepage"));
    }
}
