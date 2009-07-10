package org.pustefixframework.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class FileSystemResourceImpl extends AbstractResource implements FileSystemResource, InputStreamResource, OutputStreamResource {

	private File file;
	
	public FileSystemResourceImpl(File file) {
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
	
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}
	
	public OutputStream getOutputStream() throws IOException {
		return new FileOutputStream(file);
	}
	
	public URI getURI() {
		return file.toURI();
	}
	
	public boolean exists() {
		return file.exists();
	}
	
	public long lastModified() throws IOException {
		return file.lastModified();
	}
	
	public URI getOriginalURI() {
		return getURI();
	}
	
	public URI[] getSupplementaryURIs() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
