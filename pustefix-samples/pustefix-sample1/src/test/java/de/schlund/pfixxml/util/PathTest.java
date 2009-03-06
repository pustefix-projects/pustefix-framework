package de.schlund.pfixxml.util;

import java.io.*;
import junit.framework.TestCase;

@SuppressWarnings("deprecation")
public class PathTest extends TestCase {
    private Path path;
    
    public void testNameOnly() {
        path = Path.create("foo.bar");
        assertEquals(Path.HERE, path.getBase());
        assertEquals("foo.bar", path.getRelative());
    }
    
    public void testNormal() {
        path = Path.create(Path.HERE, "foo/bar.tgz");
        assertEquals(new File(Path.HERE, "foo/bar.tgz"), path.resolve());
    }

    public void testAbsolute() {
        path = Path.create(new File("/"),"foo");
        assertEquals(new File("/foo"), path.resolve());
    }
    
    public void testEmptyRelative() {
        path = Path.create(Path.HERE, "");
        assertEquals("", path.getRelative());
        assertEquals(Path.HERE, path.resolve());
    }
    
    public void testRoot() {
        assertEquals("", Path.ROOT.getName());
        path = Path.create("");
        assertEquals(Path.ROOT, new File("/"));
        assertEquals("", path.getRelative());
    }

    public void testAbsoluteBase() throws IOException {
    	System.out.println(Path.HERE.getAbsolutePath());
        assertEquals(Path.HERE, new File(".").getCanonicalFile());
    }
    //--
    
    public void testGetName() {
        assertEquals("foo.tgz", Path.create("foo.tgz").getName());
        assertEquals("", Path.create("").getName());
    }

    public void testGetSuffix() {
        assertEquals(".tgz", Path.create("foo.tgz").getSuffix());
        assertEquals("", Path.create("foo").getSuffix());
        assertEquals("", Path.create("").getSuffix());
    }

    public void testGetDir() {
        assertEquals("baz", Path.create(new File("/bar"), "baz/foo.tgz").getDir());
        assertNull(Path.create("foo").getDir());
        assertNull(Path.create("").getDir());
    }
}
