package org.pustefixframework.resource.support;

import java.net.URI;
import java.net.URL;

import org.pustefixframework.resource.DynamicResource;

public class DynamicResourceImpl extends URLResourceImpl implements DynamicResource {

	private URI dynamicURI;
	
	public DynamicResourceImpl(URI uri, URI originallyRequestedURI, URL url, URI dynamicURI) {
		super(uri, originallyRequestedURI, url);
		this.dynamicURI = dynamicURI;
	}
	
	public URI getDynamicURI() {
		return dynamicURI;
	}
	
}
