package org.pustefixframework.http;

import java.util.regex.Pattern;

import junit.framework.TestCase;

public class AbstractPustefixXMLRequestHandlerTest extends TestCase {

    public void testRenderParamSyntaxCheck() {
        
        Pattern pattern = AbstractPustefixXMLRequestHandler.PARAM_RENDER_PART_PATTERN;
        assertTrue(pattern.matcher("foo").matches());
        assertTrue(pattern.matcher("foo.bar").matches());
        assertTrue(pattern.matcher("foo_bar").matches());
        assertTrue(pattern.matcher("foo-bar").matches());
        assertTrue(pattern.matcher("fooBar").matches());
        assertTrue(pattern.matcher("123").matches());
        assertFalse(pattern.matcher("").matches());
        assertFalse(pattern.matcher("foo/bar").matches());
        assertFalse(pattern.matcher("foo$").matches());
        
        pattern = AbstractPustefixXMLRequestHandler.PARAM_RENDER_HREF_PATTERN;
        assertTrue(pattern.matcher("/foo.xml").matches());
        assertTrue(pattern.matcher("foo.xml").matches());
        assertTrue(pattern.matcher("/xml/foo.xml").matches());
        assertTrue(pattern.matcher("xml/foo.xml").matches());
        assertTrue(pattern.matcher("xml/FooBar.xml").matches());
        assertTrue(pattern.matcher("/xml/foo/bar.xml").matches());
        assertTrue(pattern.matcher("/xml/foo/bar_baz.xml").matches());
        assertTrue(pattern.matcher("/xml/foo/bar-baz.xml").matches());
        assertTrue(pattern.matcher("xml/foo/bar.xml").matches());
        assertFalse(pattern.matcher("").matches());
        assertFalse(pattern.matcher("/").matches());
        assertFalse(pattern.matcher("//foo.xml").matches());
        assertFalse(pattern.matcher("../foo.xml").matches());
        assertFalse(pattern.matcher("/../foo.xml").matches());
        assertFalse(pattern.matcher("/foo$.xml").matches());
        
        pattern = AbstractPustefixXMLRequestHandler.PARAM_RENDER_MODULE_PATTERN;
        assertTrue(pattern.matcher("foo").matches());
        assertTrue(pattern.matcher("foo_bar").matches());
        assertTrue(pattern.matcher("foo-bar").matches());
        assertTrue(pattern.matcher("fooBar").matches());
        assertFalse(pattern.matcher("foo.bar").matches());
        assertFalse(pattern.matcher("").matches());
        assertFalse(pattern.matcher("foo/bar").matches());
        assertFalse(pattern.matcher("foo$").matches());
    }
    
}
