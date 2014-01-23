package de.schlund.pfixxml.resources;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import junit.framework.TestCase;

public class ResourceIndexMapTest extends TestCase {

	public void testReading() throws Exception {

		URL url = getClass().getResource("pustefix-resource.index");
		InputStream in = url.openStream();
		ResourceIndexMap map = ResourceIndexMap.read(in);
		in.close();

		assertNotNull(map.getEntry("xsl"));
		assertNotNull(map.getEntry("xsl/mypage.xml"));
		assertNotNull(map.getEntry("img/sample1/test.gif"));
		assertNotNull(map.getEntry("/script"));
		assertNotNull(map.getEntry("script/"));

		assertNull(map.getEntry("xslt"));
		assertNull(map.getEntry("test.gif"));
		assertNull(map.getEntry("/"));
		assertNull(map.getEntry(null));

	}

	public void testCreation() {

		ResourceIndexMap map = new ResourceIndexMap();
		map.addEntry("xyz/foo/bar/baz.gif", new Date(), 0);
		map.addEntry("foo.png", new Date(), 0);
		map.addEntry("mydir1/", new Date(), 0);
		map.addEntry("mydir1/mydir2/", new Date(), 0);
		map.addEntry("xyz/foo/bbb.txt", new Date(), 0);

		assertNotNull(map.getEntry("foo.png"));
		assertNotNull(map.getEntry("mydir1/mydir2"));
		assertNotNull(map.getEntry("xyz/foo/bbb.txt"));
		assertNotNull(map.getEntry("xyz/foo/bar/baz.gif"));
		assertNotNull(map.getEntry("xyz/foo"));

		assertNull(map.getEntry("xxx"));
		assertNull(map.getEntry("xxx.gif"));
		assertNull(map.getEntry(""));
		assertNull(map.getEntry("//"));

	}

}
