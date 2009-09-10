package org.pustefixframework.util.io;

import java.io.File;
import java.io.IOException;

import de.schlund.pfixxml.util.MD5Utils;

import junit.framework.TestCase;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class FileUtilsTest extends TestCase {

	private File baseDir;
	
	@Override
	protected void setUp() throws Exception {
		String name = "pustefix-test-" + MD5Utils.hex_md5((new File(".")).getAbsolutePath() + 
				FileUtilsTest.class.getName() + System.currentTimeMillis());
		baseDir = new File(System.getProperty("java.io.tmpdir") + "/" + name);
		baseDir.mkdir();
	}
	
	public void testSymbolicLinks() throws IOException {
		
		File fooDir = new File(baseDir, "foo");
		fooDir.mkdir();
		File barDir = new File(fooDir, "bar");
		barDir.mkdir();
		File bazDir = new File(barDir, "baz");
		bazDir.mkdir();
		File fooLink = new File(baseDir, "foolink");
		File barLink = new File(baseDir, "barlink");
		File bazLink = new File(fooDir, "bazlink");
		
		fooLink = FileUtils.createSymbolicLink(fooLink, fooDir, true);
		assertEquals(fooLink.getCanonicalPath(), fooDir.getCanonicalPath());
		
		barLink = FileUtils.createSymbolicLink(barLink, barDir, true);
		assertEquals(barLink.getCanonicalPath(), barDir.getAbsolutePath());
		
		bazLink = FileUtils.createSymbolicLink(bazLink, bazDir, true);
		assertEquals(bazLink.getCanonicalPath(), bazDir.getAbsolutePath());
		
	}
	
	@Override
	protected void tearDown() throws Exception {
		FileUtils.delete(baseDir);
	}
	
	
}
