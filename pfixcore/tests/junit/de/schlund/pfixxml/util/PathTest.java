package de.schlund.pfixxml.util;

import java.io.File;
import junit.framework.TestCase;

public class PathTest extends TestCase {
    private Path path;
    
    public void testNormal() {
        path = Path.create(Path.HERE, "foo/bar.tgz");
        assertEquals(new File(Path.HERE, "foo/bar.tgz"), path.resolve());
    }

    public void testAbsolute() {
        path = Path.create("/foo/bar.tgz");
        assertEquals(Path.ROOT, path.getBase());
    }
    
    public void testEmptyRelative() {
        path = Path.create(Path.HERE, "");
        assertEquals(Path.HERE, path.resolve());
    }
    
    public void testRoot() {
        path = Path.create(Path.ROOT.getPath());
        assertEquals(Path.ROOT, path.resolve());
    }

    public void testAbsoluteBase() {
        assertEquals(Path.HERE, Path.create(new File("."), "").getBase());
    }
    //--
    
    public void testGetName() {
        assertEquals("foo.tgz", Path.create("/foo.tgz").getName());
        assertEquals("", Path.create("/").getName());
    }

    public void testGetSuffix() {
        assertEquals(".tgz", Path.create("/foo.tgz").getSuffix());
        assertEquals("", Path.create("/foo").getSuffix());
        assertEquals("", Path.create("/").getSuffix());
    }

    public void testGetDir() {
        assertEquals("bar", Path.create("/bar/foo.tgz").getDir());
        assertNull(Path.create("/foo").getDir());
        assertNull(Path.create("/").getDir());
    }
}
