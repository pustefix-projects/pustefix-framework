package org.pustefixframework.maven.plugins.launcher;

import java.io.File;

public class BundleConfig {

	private File file;
	private String bundleSymbolicName;
	private boolean start;
	private int startLevel;
	
	public BundleConfig(File file, String bundleSymbolicName, boolean start, int startLevel) {
		if(file == null) throw new IllegalArgumentException("File argument must not be null");
		this.file = file;
		this.bundleSymbolicName = bundleSymbolicName;
		this.start = start;
		this.startLevel = startLevel;
	}
	
	public File getFile() {
		return file;
	}
	
	public boolean doStart() {
		return start;
	}
	
	public int getStartLevel() {
	    return startLevel;
	}
	
	public String getBundleSymbolicName() {
	    return bundleSymbolicName;
	}
	
}
