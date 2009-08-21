package org.pustefixframework.resource.support;

import java.net.URI;

import org.pustefixframework.resource.Resource;

public class NullResource implements Resource {

	private URI uri;
	
	public NullResource(URI uri) {
		this.uri = uri;
	}
	
	public URI getOriginalURI() {
		return uri;
	}

	public URI[] getSupplementaryURIs() {
		return null;
	}

	public URI getURI() {
		return uri;
	}
	
}
