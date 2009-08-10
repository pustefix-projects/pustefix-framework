package org.pustefixframework.xmlgenerator.config.model;

import java.net.URI;

public class IncludeDef extends AbstractModelElement {

	private URI uri;
	
	public IncludeDef(URI uri) {
		this.uri = uri;
	}
	
	public URI getURI() {
		return uri;
	}
	
}
