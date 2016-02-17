package org.pustefixframework.util;

import de.schlund.pfixxml.IncludeDocumentExtensionSaxon1;
import junit.framework.TestCase;

public class LogUtilsTest extends TestCase {

    public void testFormatName() {
        
        assertEquals("java.lang.String", LogUtils.shortenClassName(String.class, 18));
        assertEquals("java.lang.String", LogUtils.shortenClassName(String.class, 17));
        assertEquals("java.lang.String", LogUtils.shortenClassName(String.class, 16));
        assertEquals("j.lang.String", LogUtils.shortenClassName(String.class, 15));
        assertEquals("j.lang.String", LogUtils.shortenClassName(String.class, 14));
        assertEquals("j.lang.String", LogUtils.shortenClassName(String.class, 13));
        for(int i=-1; i<13; i++) {
            assertEquals("j.l.String", LogUtils.shortenClassName(String.class, i));
        }
        
        for(int i=-1; i<8; i++) {
            assertEquals("float", LogUtils.shortenClassName(float.class, i));
        }
        
        assertEquals("de.schlund.pfixxml.IncludeDocumentExtensionSaxon1", LogUtils.shortenClassName(IncludeDocumentExtensionSaxon1.class, 50));
        assertEquals("de.schlund.pfixxml.IncludeDocumentExtensionSaxon1", LogUtils.shortenClassName(IncludeDocumentExtensionSaxon1.class, 49));
        assertEquals("d.schlund.pfixxml.IncludeDocumentExtensionSaxon1", LogUtils.shortenClassName(IncludeDocumentExtensionSaxon1.class, 48));
        for(int i=42; i<48; i++) {
            assertEquals("d.s.pfixxml.IncludeDocumentExtensionSaxon1", LogUtils.shortenClassName(IncludeDocumentExtensionSaxon1.class, i));
        }
        for(int i=36; i<42; i++) {
            assertEquals("d.s.p.IncludeDocumentExtensionSaxon1", LogUtils.shortenClassName(IncludeDocumentExtensionSaxon1.class, i));
        }
        for(int i=-1; i<36; i++) {
            assertEquals("d.s.p.IncludeDocumentExtensionSaxon1", LogUtils.shortenClassName(IncludeDocumentExtensionSaxon1.class, i));
        }
    }

}