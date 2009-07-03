package org.pustefixframework.maven.plugins.launcher;

import java.io.File;
import java.net.URI;

public interface URIToFileResolver {

	public File resolve(URI uri);
	
}
