package org.pustefixframework.xmlgenerator.config.model;

import java.util.List;

public class ModelChangeEvent {

	public enum Type { ADD, REMOVE, UPDATE };
	
	private Type type;
	private List<ModelElement> elements;
	private ModelElement source;
	
	public ModelChangeEvent(Type type, ModelElement source, List<ModelElement> elements) {
		this.type = type;
		this.source = source;
		this.elements = elements;
	}
	
	public Type getType() {
		return type;
	}
	
	public ModelElement getSource() {
		return source;
	}
	
	public List<ModelElement> getAffectedElements() {
		return elements;
	}
	
}
