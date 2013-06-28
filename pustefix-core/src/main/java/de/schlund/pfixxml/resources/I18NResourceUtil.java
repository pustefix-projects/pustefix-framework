package de.schlund.pfixxml.resources;

import java.net.URI;


public class I18NResourceUtil {

	public static Resource getResource(String resourceURI, String tenantName, String language) {

		I18NIterator it = new I18NIterator(tenantName, language, resourceURI);
		while(it.hasNext()) {
			String uri = it.next();
			Resource res = ResourceUtil.getResource(uri);
			if(res.exists()) {
				return res;
			}
		}
		return ResourceUtil.getResource(resourceURI);
	}

	public static FileResource getFileResourceFromDocroot(String resourcePath, String tenantName, String language) {
		
		I18NIterator it = new I18NIterator(tenantName, language, resourcePath);
		while(it.hasNext()) {
			String path = it.next();
			FileResource res = ResourceUtil.getFileResourceFromDocroot(path);
			if(res.exists()) {
				return res;
			}
		}
		return ResourceUtil.getFileResourceFromDocroot(resourcePath);
	}
	
	public static FileResource getFileResource(FileResource parent, String fileName, String tenantName, String language) {
		
		I18NIterator it = new I18NIterator(tenantName, language, fileName);
		while(it.hasNext()) {
			String path = it.next();
			FileResource res = ResourceUtil.getFileResource(parent, path);
			if(res.exists()) {
				return res;
			}
		}
		return ResourceUtil.getFileResource(parent, fileName);
	}

	public static String getURLPath(URI uri) {
    	if(uri.getScheme().equals("module")) {
    		return "modules/" + uri.getAuthority() + uri.getPath();
    	} else {
    		return uri.getPath().substring(1);
    	}
    }

}