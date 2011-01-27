/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
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
        path = Path.create(new File(File.listRoots()[0].getAbsolutePath()),"foo");
        assertEquals(new File(File.listRoots()[0].getAbsolutePath() + "foo"), path.resolve());
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
