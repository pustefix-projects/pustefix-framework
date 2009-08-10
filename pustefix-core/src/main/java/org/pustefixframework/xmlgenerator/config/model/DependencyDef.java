package org.pustefixframework.xmlgenerator.config.model;

public class DependencyDef extends AbstractModelElement {

	private String reference;
	
	public DependencyDef(String reference) {
		this.reference = reference;
	}
	
	public String getReference() {
		return reference;
	}
	
}
