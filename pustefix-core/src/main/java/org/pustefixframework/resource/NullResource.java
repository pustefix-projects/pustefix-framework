package org.pustefixframework.resource;

import java.net.URI;

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
