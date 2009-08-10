package org.pustefixframework.xmlgenerator.config.model;


public class NamespaceDeclaration extends AbstractModelElement {

	private String prefix;
	private String url;
	
	public NamespaceDeclaration(String prefix, String url) {
		this.prefix = prefix;
		this.url = url;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String getURL() {
		return url;
	}
	
}
