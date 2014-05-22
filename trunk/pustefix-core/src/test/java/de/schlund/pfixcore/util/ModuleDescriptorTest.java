package de.schlund.pfixcore.util;

import java.io.File;
import java.net.URL;

import de.schlund.pfixxml.util.FileUtils;

import junit.framework.TestCase;


public class ModuleDescriptorTest extends TestCase {

    public void test() throws Exception {

        ModuleDescriptor info = ModuleDescriptor.read(getModuleDescriptor("foo", "/path"));
        assertEquals("foo", info.getName());
        assertEquals("/path", info.getResourcePath());

        info = ModuleDescriptor.read(getModuleDescriptor("foo", "/path/"));
        assertEquals("foo", info.getName());
        assertEquals("/path", info.getResourcePath());

        info = ModuleDescriptor.read(getModuleDescriptor("foo", "path"));
        assertEquals("foo", info.getName());
        assertEquals("/path", info.getResourcePath());

        info = ModuleDescriptor.read(getModuleDescriptor("foo", ""));
        assertEquals("foo", info.getName());
        assertEquals(ModuleDescriptor.DEFAULT_RESOURCE_PATH, info.getResourcePath());

        info = ModuleDescriptor.read(getModuleDescriptor("foo", "/"));
        assertEquals("foo", info.getName());
        assertEquals("", info.getResourcePath());

        info = ModuleDescriptor.read(getModuleDescriptor("foo", null));
        assertEquals("foo", info.getName());
        assertEquals(ModuleDescriptor.DEFAULT_RESOURCE_PATH, info.getResourcePath());

    }

    private URL getModuleDescriptor(String moduleName, String moduleResourcePath) throws Exception {

        File file = File.createTempFile("pustefix-module-", ".xml");
        file.deleteOnExit();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        sb.append("<module-descriptor xmlns=\"http://www.pustefix-framework.org/2008/namespace/module-descriptor\" ");
        sb.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        sb.append("xsi:schemaLocation=\"http://www.pustefix-framework.org/2008/namespace/module-descriptor ");
        sb.append("http://www.pustefix-framework.org/2008/namespace/module-descriptor-0_16.xsd\">\n");
        if(moduleName != null) {
            sb.append("<module-name>").append(moduleName).append("</module-name>\n");
        }
        if(moduleResourcePath != null) {
            sb.append("<resource-path>").append(moduleResourcePath).append("</resource-path>\n");
        }
        sb.append("</module-descriptor>");

        FileUtils.save(sb.toString(), file, "utf-8");
        return file.toURI().toURL();
    }

}
