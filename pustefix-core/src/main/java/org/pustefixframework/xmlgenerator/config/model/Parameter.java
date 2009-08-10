package org.pustefixframework.xmlgenerator.config.model;

public class Parameter extends AbstractModelElement {

	private String name;
	private String value;
	
	public Parameter(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
}
