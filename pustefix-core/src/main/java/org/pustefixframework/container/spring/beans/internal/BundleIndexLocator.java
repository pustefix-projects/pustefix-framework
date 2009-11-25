package org.pustefixframework.container.spring.beans.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.pustefixframework.config.customization.RuntimeProperties;

/**
 * BundleSourceLocator implementation looking for bundle source directories
 * in a bundle index file having the following property format:
 * 
 *   Bundle-SymbolicName>@<Bundle-Version> = path/to/bundle 
 * 
 * By default the locator will search for the index file ".bundle-index" in 
 * the user's Maven folder (".m2").
 * 
 * @author mleidig@schlund.de
 *
 */
public class BundleIndexLocator implements BundleSourceLocator {
	
	private final static String DEFAULT_INDEX_FILE = System.getProperty("user.home") + "/.m2/.bundle-index";

	private Map<String, File> bundleToLocation = new HashMap<String, File>();
	
	public BundleIndexLocator() throws IOException {
		String mode = RuntimeProperties.getProperties().getProperty("mode");
		if(mode != null && !mode.equals("prod")) {
		    File file = new File(DEFAULT_INDEX_FILE);
		    if(file.exists() && file.isFile()) loadIndexFile(file);
		}
	}
	
	public BundleIndexLocator(File indexFile) throws IOException {
		loadIndexFile(indexFile);
	}
	
	public File getSourceLocation(String bundleSymbolicName, String bundleVersion) {
		synchronized(bundleToLocation) {
			return bundleToLocation.get(bundleSymbolicName+"@"+bundleVersion);
		}
	}
	
	private void loadIndexFile(File file) throws IOException {
		Properties props = new Properties();
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		FileLock lock = raf.getChannel().lock();
		try {
			FileInputStream in = new FileInputStream(raf.getFD());
			props.load(in);
			Enumeration<?> keys = (Enumeration<?>)props.propertyNames();
			while(keys.hasMoreElements()) {
				String key = (String)keys.nextElement();
				String value = props.getProperty(key);
				String bundle = key.trim();
				String location = value.trim();
				File bundleDir;
				if(location.startsWith("/") || file.getParentFile() == null) bundleDir = new File(location);
				else bundleDir = new File(file.getParentFile(), location);
				if(bundleDir.exists()) bundleToLocation.put(bundle, bundleDir);
			}
		} finally {
			lock.release();
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(String name: bundleToLocation.keySet()) {
			sb.append(name+"="+bundleToLocation.get(name)+"\n");
		}
		return sb.toString();
	}

}
