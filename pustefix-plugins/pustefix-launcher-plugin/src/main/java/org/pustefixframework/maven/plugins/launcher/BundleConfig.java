package org.pustefixframework.maven.plugins.launcher;

import java.io.File;

public class BundleConfig {

	private File file;
	private boolean start;
	
	public BundleConfig(File file, boolean start) {
		if(file == null) throw new IllegalArgumentException("File argument must not be null");
		this.file = file;
		this.start = start;
	}
	
	public File getFile() {
		return file;
	}
	
	public boolean doStart() {
		return start;
	}
	
}
